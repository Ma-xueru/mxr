package com.cl.controller;

import com.cl.annotation.IgnoreAuth;
import com.cl.utils.AIChatUtil;
import com.cl.utils.R;
import com.cl.utils.VolcengineTtsUtil;
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

    // ========== 图片识别 (Volcengine Vision) ==========
    @IgnoreAuth
    @RequestMapping("/vision")
    public R vision(@RequestParam("image") MultipartFile image) {
        if (image == null || image.isEmpty()) return R.error("请上传图片");
        try {
            // 转 Base64
            byte[] bytes = image.getBytes();
            String base64 = java.util.Base64.getEncoder().encodeToString(bytes);

            // 调用豆包 Vision 模型识别图片
            List<AIChatUtil.Message> msgs = new ArrayList<>();
            msgs.add(new AIChatUtil.Message("user", "请用简洁的中文描述这张图片的场景元素（如：春天、柳树、燕子、湖水等），只返回描述，不超过50字。"));
            // 用 DeepSeek 做图片理解（通过 base64 image 或直接请求描述）
            // DeepSeek V4 支持图片理解，用 OpenAI 兼容格式
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
        // 使用 DeepSeek 的 vision 能力(OpenAI 兼容格式)
        String body = new org.json.JSONObject()
            .put("model", AIChatUtil.getModel())
            .put("messages", new org.json.JSONArray().put(
                new org.json.JSONObject()
                    .put("role", "user")
                    .put("content", new org.json.JSONArray()
                        .put(new org.json.JSONObject().put("type", "text").put("text", "请用简洁的中文描述这张图片的场景元素（如：春天、柳树、燕子、湖水等），只返回描述，不超过50字。"))
                        .put(new org.json.JSONObject().put("type", "image_url").put("image_url",
                            new org.json.JSONObject().put("url", "data:image/jpeg;base64," + base64)))
                    )
            ))
            .put("max_tokens", 200).put("stream", false).toString();

        URL url = new URL("https://api.deepseek.com/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + getDeepSeekKey());
        conn.setConnectTimeout(30000); conn.setReadTimeout(60000);
        conn.setDoOutput(true);
        conn.getOutputStream().write(body.getBytes("UTF-8"));

        int code = conn.getResponseCode();
        InputStream is = code == 200 ? conn.getInputStream() : conn.getErrorStream();
        String resp = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
        if (code != 200) return null;

        org.json.JSONObject json = new org.json.JSONObject(resp);
        String content = json.getJSONArray("choices").getJSONObject(0)
            .getJSONObject("message").optString("content", "");
        return new AIChatUtil.ChatResult(content);
    }

    private String getDeepSeekKey() {
        try {
            java.util.Properties p = new java.util.Properties();
            p.load(getClass().getClassLoader().getResourceAsStream("asr.properties"));
            return p.getProperty("ai.deepseek.api_key", "");
        } catch (Exception e) { return ""; }
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
    public R tts(@RequestParam String text) {
        if (!StringUtils.hasText(text)) return R.error("文本为空");
        try {
            String path = VolcengineTtsUtil.textToSpeech(text,
                new java.io.File(org.springframework.util.ResourceUtils.getFile("classpath:static"), "file").getAbsolutePath());
            if (path == null) return R.error("TTS合成失败");
            return R.ok().put("data", "/file/" + new java.io.File(path).getName());
        } catch (Exception e) { return R.error("TTS错误: " + e.getMessage()); }
    }
}
