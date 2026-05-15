package com.cl.controller;

import com.cl.annotation.IgnoreAuth;
import com.cl.utils.AIChatUtil;
import com.cl.utils.R;
import com.cl.utils.VolcengineSpeechUtil;
import com.cl.utils.VolcengineTtsUtil;
import com.cl.utils.VolcengineTtsV3Util;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@RestController
@RequestMapping("/poem-creator")
public class PoemCreatorController {

    // ========== 统一多模态处理（图片/语音/文字 → 豆包多模态模型 → 诗） ==========
    @IgnoreAuth
    @RequestMapping("/process")
    public R process(@RequestParam(value="image", required=false) MultipartFile image,
                     @RequestParam(value="audio", required=false) MultipartFile audio,
                     @RequestParam(value="text", required=false) String text) {
        try {
            org.json.JSONArray contentArr = new org.json.JSONArray();

            if (image != null && !image.isEmpty()) {
                byte[] bytes = image.getBytes();
                String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
                contentArr.put(new org.json.JSONObject()
                    .put("type", "image_url")
                    .put("image_url", new org.json.JSONObject().put("url", "data:image/jpeg;base64," + base64)));
            }
            if (audio != null && !audio.isEmpty()) {
                byte[] bytes = audio.getBytes();
                String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
                contentArr.put(new org.json.JSONObject()
                    .put("type", "input_audio")
                    .put("input_audio", new org.json.JSONObject()
                        .put("data", base64).put("format", "aac")));
            }

            String userPrompt = "请根据我提供的场景，发挥你的诗词才华，为小学生创作一首五言或七言诗。\n" +
                "要求：1.语言通俗优美，不要生僻字 2.必须包含诗名、作者（AI小诗人）、正文\n" +
                "3.提供一段'诗人老师说'用大白话解释诗意 4.标明[诗名][正文][诗人老师说]各部分。";
            if (text != null && !text.trim().isEmpty()) userPrompt = "场景关键词：" + text + "。" + userPrompt;
            contentArr.put(new org.json.JSONObject().put("type", "text").put("text", userPrompt));

            AIChatUtil.ChatResult cr = callMultimodalAPI(contentArr);
            String poem = cr != null && cr.getContent() != null ? cr.getContent().trim() : "";
            if (poem.isEmpty()) {
                if (image != null) return R.error("图片识别失败，请重试");
                if (audio != null) return R.error("语音识别失败，请重试");
                return R.error("AI作诗失败");
            }
            System.out.println("[诗词小诗人] 多模态创作完成");
            return R.ok().put("data", poem);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("处理失败: " + e.getMessage());
        }
    }

    private AIChatUtil.ChatResult callMultimodalAPI(org.json.JSONArray contentArr) throws Exception {
        String body = new org.json.JSONObject()
            .put("model", "doubao-seed-2-0-lite-260215")
            .put("messages", new org.json.JSONArray().put(
                new org.json.JSONObject().put("role", "user").put("content", contentArr)))
            .put("max_tokens", 800).put("thinking", new org.json.JSONObject().put("type", "disabled")).toString();

        java.net.URL url = new java.net.URL("https://ark.cn-beijing.volces.com/api/v3/chat/completions");
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + getArkKey());
        conn.setConnectTimeout(30000); conn.setReadTimeout(90000);
        conn.setDoOutput(true);
        conn.getOutputStream().write(body.getBytes("UTF-8"));

        int code = conn.getResponseCode();
        java.io.InputStream is = code == 200 ? conn.getInputStream() : conn.getErrorStream();
        String resp = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A").next();
        System.out.println("[诗词小诗人] Multimodal HTTP " + code + ": " + resp.substring(0, Math.min(300, resp.length())));
        if (code != 200) return null;

        org.json.JSONObject json = new org.json.JSONObject(resp);
        String content = json.getJSONArray("choices").getJSONObject(0)
            .getJSONObject("message").optString("content", "");
        return new AIChatUtil.ChatResult(content);
    }

    // ========== 图片识别 (保留兼容) ==========
    @IgnoreAuth
    @RequestMapping("/vision")
    public R vision(@RequestParam("image") MultipartFile image) {
        if (image == null || image.isEmpty()) return R.error("请上传图片");
        try {
            byte[] bytes = image.getBytes();
            String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
            AIChatUtil.ChatResult cr = callVisionAPI(base64);
            String desc = cr != null && cr.getContent() != null ? cr.getContent().trim() : "";
            if (desc.isEmpty()) desc = "一幅美丽的自然风景画";

            System.out.println("[诗词小诗人] 图片识别: " + desc);
            return R.ok().put("data", desc);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("图片识别失败: " + e.getMessage());
        }
    }

    private AIChatUtil.ChatResult callVisionAPI(String base64) throws Exception {
        // 火山豆包多模态模型，使用标准 chat/completions 端点
        String body = new org.json.JSONObject()
            .put("model", "doubao-seed-2-0-lite-260215")
            .put("messages", new org.json.JSONArray().put(
                new org.json.JSONObject()
                    .put("role", "user")
                    .put("content", new org.json.JSONArray()
                        .put(new org.json.JSONObject().put("type", "image_url").put("image_url",
                            new org.json.JSONObject().put("url", "data:image/jpeg;base64," + base64)))
                        .put(new org.json.JSONObject().put("type", "text").put("text",
                            "请用简洁的中文描述这张图片的场景元素，不超过50字。"))
                    )
            ))
            .put("max_tokens", 300).put("thinking", new org.json.JSONObject().put("type", "disabled")).toString();

        URL url = new URL("https://ark.cn-beijing.volces.com/api/v3/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + getArkKey());
        conn.setConnectTimeout(30000); conn.setReadTimeout(90000);
        conn.setDoOutput(true);
        conn.getOutputStream().write(body.getBytes("UTF-8"));

        int code = conn.getResponseCode();
        InputStream is = code == 200 ? conn.getInputStream() : conn.getErrorStream();
        String resp = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
        System.out.println("[诗词小诗人] Vision HTTP " + code + ": " + resp.substring(0, Math.min(500, resp.length())));
        if (code != 200) return null;

        org.json.JSONObject json = new org.json.JSONObject(resp);
        String content = "";
        if (json.has("choices")) {
            org.json.JSONObject msg = json.getJSONArray("choices").getJSONObject(0).getJSONObject("message");
            content = msg.optString("content", "");
            System.out.println("[诗词小诗人] Vision content(" + content.length() + "): " + content.substring(0, Math.min(200, content.length())));
        } else if (json.has("output")) {
            org.json.JSONArray output = json.getJSONArray("output");
            for (int i = 0; i < output.length(); i++) {
                org.json.JSONObject item = output.getJSONObject(i);
                if (item.has("content")) {
                    org.json.JSONArray ca = item.getJSONArray("content");
                    for (int j = 0; j < ca.length(); j++) {
                        if (ca.getJSONObject(j).has("text")) content += ca.getJSONObject(j).optString("text", "");
                    }
                }
            }
        }
        if (content.isEmpty()) System.out.println("[诗词小诗人] Vision: NO content found!");
        return new AIChatUtil.ChatResult(content);
    }

    private String getArkKey() {
        try {
            java.util.Properties p = new java.util.Properties();
            p.load(getClass().getClassLoader().getResourceAsStream("asr.properties"));
            return p.getProperty("ark.api.key", "");
        } catch (Exception e) { return ""; }
    }

    private String getDeepSeekKey() {
        try {
            java.util.Properties p = new java.util.Properties();
            p.load(getClass().getClassLoader().getResourceAsStream("asr.properties"));
            return p.getProperty("ai.deepseek.api_key", "");
        } catch (Exception e) { return ""; }
    }

    // ========== AI 作诗 (智谱 GLM) ==========
    @IgnoreAuth
    @RequestMapping("/zhipu")
    public R zhipu(@RequestParam String text) {
        if (!StringUtils.hasText(text)) return R.error("请输入内容");
        try {
            String prompt = "你是充满童趣的AI小诗人，专门为小学生创作古诗。请用通俗优美的语言创作诗。\n"
                + "场景：" + text + "\n格式：[诗名]\n[作者]AI小诗人\n[正文]\n[诗人老师说]（大白话解释）";

            org.json.JSONArray msgs = new org.json.JSONArray();
            msgs.put(new org.json.JSONObject().put("role", "system").put("content", "你是AI小诗人。"));
            msgs.put(new org.json.JSONObject().put("role", "user").put("content", prompt));

            String body = new org.json.JSONObject()
                .put("model", "glm-4.7").put("messages", msgs)
                .put("max_tokens", 800).put("temperature", 0.8).toString();

            java.net.URL url = new java.net.URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer 2d31b79946b14428b20dce74a540648b.KTjlLqBE3x65RTiu");
            conn.setConnectTimeout(30000); conn.setReadTimeout(90000);
            conn.setDoOutput(true);
            conn.getOutputStream().write(body.getBytes("UTF-8"));

            int code = conn.getResponseCode();
            java.io.InputStream is = code == 200 ? conn.getInputStream() : conn.getErrorStream();
            String resp = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A").next();
            System.out.println("[智谱] HTTP " + code + ": " + resp.substring(0, Math.min(200, resp.length())));
            if (code != 200) return R.error("智谱API失败");

            org.json.JSONObject json = new org.json.JSONObject(resp);
            String poem = json.getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").optString("content", "");
            return R.ok().put("data", poem);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("智谱错误: " + e.getMessage());
        }
    }

    // ========== AI 作诗 (全局模型) ==========
    @IgnoreAuth @RequestMapping("/generate")
    public R generate(@RequestParam String text) {
        if (!StringUtils.hasText(text)) return R.error("请输入内容");
        String prompt = "你是AI小诗人，专门为小学生创作古诗。请用通俗优美的语言创作。\n场景：" + text
            + "\n格式：[诗名]\n[作者]AI小诗人\n[正文]\n[诗人老师说]（大白话解释）";
        String poem = AIChatUtil.chat("你是AI小诗人。", prompt);
        if (poem == null || poem.isEmpty()) return R.error("AI作诗失败");
        return R.ok().put("data", poem);
    }

    // ========== AI 作诗 (阿里千问) ==========
    @IgnoreAuth
    @RequestMapping("/qwen")
    public R qwen(@RequestParam String text) {
        if (!StringUtils.hasText(text)) return R.error("请输入内容");
        try {
            String prompt = "你是充满童趣的AI小诗人，专门为小学生创作古诗。请用通俗优美的语言创作诗。\n"
                + "场景：" + text + "\n格式：[诗名]\n[作者]AI小诗人\n[正文]\n[诗人老师说]（大白话解释）";

            org.json.JSONArray msgs = new org.json.JSONArray();
            msgs.put(new org.json.JSONObject().put("role", "system").put("content", "你是AI小诗人。"));
            msgs.put(new org.json.JSONObject().put("role", "user").put("content", prompt));

            String body = new org.json.JSONObject()
                .put("model", "qwen-plus").put("messages", msgs)
                .put("max_tokens", 800).put("temperature", 0.8).toString();

            java.net.URL url = new java.net.URL("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer sk-8b86c54172ee4f7891ae823cf94c4e45");
            conn.setConnectTimeout(30000); conn.setReadTimeout(90000);
            conn.setDoOutput(true);
            conn.getOutputStream().write(body.getBytes("UTF-8"));

            int code = conn.getResponseCode();
            java.io.InputStream is = code == 200 ? conn.getInputStream() : conn.getErrorStream();
            String resp = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A").next();
            System.out.println("[千问] HTTP " + code + ": " + resp.substring(0, Math.min(200, resp.length())));
            if (code != 200) return R.error("千问API失败");

            org.json.JSONObject json = new org.json.JSONObject(resp);
            String poem = json.getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").optString("content", "");
            return R.ok().put("data", poem);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("千问错误: " + e.getMessage());
        }
    }

    // ========== AI 作诗 (小米 MiMo) ==========
    @IgnoreAuth
    @RequestMapping("/mimo")
    public R mimo(@RequestParam String text) {
        if (!StringUtils.hasText(text)) return R.error("请输入内容");
        try {
            String prompt = "你是充满童趣的AI小诗人，专门为小学生创作古诗。请用通俗优美的语言创作诗。\n"
                + "场景：" + text + "\n格式：[诗名]\n[作者]AI小诗人\n[正文]\n[诗人老师说]（大白话解释）";

            org.json.JSONArray msgs = new org.json.JSONArray();
            msgs.put(new org.json.JSONObject().put("role", "system").put("content", "你是AI小诗人。"));
            msgs.put(new org.json.JSONObject().put("role", "user").put("content", prompt));

            String body = new org.json.JSONObject()
                .put("model", "mimo-v2.5-pro").put("messages", msgs)
                .put("max_completion_tokens", 800).put("temperature", 1.0).toString();

            java.net.URL url = new java.net.URL("https://api.xiaomimimo.com/v1/chat/completions");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("api-key", "sk-cnvdpympfmemjcr4e3ufrt46kczv7ugrfmno5gdlgt2g8lkc");
            conn.setConnectTimeout(30000); conn.setReadTimeout(90000);
            conn.setDoOutput(true);
            conn.getOutputStream().write(body.getBytes("UTF-8"));

            int code = conn.getResponseCode();
            java.io.InputStream is = code == 200 ? conn.getInputStream() : conn.getErrorStream();
            String resp = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A").next();
            System.out.println("[MiMo] HTTP " + code + ": " + resp.substring(0, Math.min(200, resp.length())));
            if (code != 200) return R.error("MiMo API失败");

            org.json.JSONObject json = new org.json.JSONObject(resp);
            String poem = json.getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").optString("content", "");
            return R.ok().put("data", poem);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("MiMo错误: " + e.getMessage());
        }
    }

    // ========== AI 作诗 (DeepSeek Reasoning) ==========
    @IgnoreAuth
    @RequestMapping("/compose")
    public R compose(@RequestParam String scene) {
        if (!StringUtils.hasText(scene)) return R.error("请输入场景描述");
        try {
            String prompt = "你是一位充满童趣的古代大诗人。请根据以下场景，为小学生创作一首五言或七言诗。\n" +
                "场景：" + scene + "\n" +
                "要求：1.语言通俗优美，不要生僻字 2.必须包含诗名、作者（AI小诗人）、正文\n" +
                "3.提供一段'诗人老师说'用大白话解释诗意 4.标明[诗名][正文][诗人老师说]各部分。";

            List<AIChatUtil.Message> msgs = new ArrayList<>();
            msgs.add(new AIChatUtil.Message("system", "你是AI小诗人，专门为小学生创作通俗优美的古诗。"));
            msgs.add(new AIChatUtil.Message("user", prompt));

            // 开启 reasoning 深度思考模式
            AIChatUtil.ChatResult cr = callDeepSeekWithReasoning(msgs);
            String poem = cr != null && cr.getContent() != null ? cr.getContent().trim() : "";
            String thinking = cr != null ? cr.getThinking() : "";

            if (poem.isEmpty()) {
                poem = "[诗名]《春日即景》\n[正文]柳绿花红燕子飞，春风拂面暖心扉。小桥流水叮咚响，一片生机入翠微。\n[诗人老师说]这首诗描写了春天的美景，柳树变绿、花儿开放、燕子飞翔，到处充满生机。";
            }

            System.out.println("[诗词小诗人] 创作完成 thinking=" + (thinking != null ? thinking.length() : 0));
            return R.ok().put("data", poem);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("作诗失败: " + e.getMessage());
        }
    }

    private AIChatUtil.ChatResult callDeepSeekWithReasoning(List<AIChatUtil.Message> msgs) throws Exception {
        org.json.JSONArray arr = new org.json.JSONArray();
        for (AIChatUtil.Message m : msgs) {
            arr.put(new org.json.JSONObject().put("role", m.getRole()).put("content", m.getContent()));
        }
        String body = new org.json.JSONObject()
            .put("model", AIChatUtil.getModel())
            .put("messages", arr)
            .put("max_tokens", 800)
            .put("temperature", 0.8)
            .put("stream", false)
            .put("thinking", new org.json.JSONObject().put("type", "enabled"))
            .toString();

        URL url = new URL("https://api.deepseek.com/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + getDeepSeekKey());
        conn.setConnectTimeout(30000); conn.setReadTimeout(90000);
        conn.setDoOutput(true);
        conn.getOutputStream().write(body.getBytes("UTF-8"));

        int code = conn.getResponseCode();
        InputStream is = code == 200 ? conn.getInputStream() : conn.getErrorStream();
        String resp = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
        if (code != 200) return null;

        org.json.JSONObject json = new org.json.JSONObject(resp);
        org.json.JSONObject msg = json.getJSONArray("choices").getJSONObject(0).getJSONObject("message");
        String content = msg.optString("content", "");
        String reasoning = msg.optString("reasoning_content", "");
        return new AIChatUtil.ChatResult(content, reasoning);
    }

    // ========== TTS 朗读 ==========
    @IgnoreAuth
    @RequestMapping("/tts")
    public R tts(@RequestBody Map<String, Object> body) {
        String text = (String) body.get("text");
        if (!StringUtils.hasText(text)) return R.error("文本为空");
        try {
            String dir = new java.io.File(org.springframework.util.ResourceUtils.getFile("classpath:static"), "file").getAbsolutePath();
            String path = VolcengineTtsUtil.textToSpeech(text, dir);
            if (path == null) path = VolcengineTtsV3Util.textToSpeech(text, dir);
            if (path == null) return R.error("TTS合成失败");
            return R.ok().put("data", "/file/" + new java.io.File(path).getName());
        } catch (Exception e) { return R.error("TTS错误: " + e.getMessage()); }
    }

    // ========== 纯语音识别 ==========
    @IgnoreAuth
    @RequestMapping("/speech")
    public R speech(@RequestParam("audio") MultipartFile audio) {
        if (audio == null || audio.isEmpty()) return R.error("请说话");
        try {
            String fn = "poem_voice_" + System.currentTimeMillis() + ".aac";
            java.io.File dir = new java.io.File("file");
            dir.mkdirs();
            java.io.File af = new java.io.File(dir, fn);
            audio.transferTo(af);
            String text = VolcengineSpeechUtil.speechToText(af);
            af.delete();
            if (text == null || text.trim().isEmpty()) return R.error("没听清，请再说一次");
            return R.ok().put("data", text.trim());
        } catch (Exception e) {
            return R.error("识别失败: " + e.getMessage());
        }
    }
}
