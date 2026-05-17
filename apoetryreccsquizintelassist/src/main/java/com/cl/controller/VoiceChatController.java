package com.cl.controller;

import com.cl.utils.AIChatUtil;
import com.cl.utils.AIRecitationReviewUtil;
import com.cl.utils.R;
import com.cl.utils.VolcengineSpeechUtil;
import com.cl.utils.VolcengineTtsUtil;
import com.cl.utils.VolcengineTtsV3Util;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/voice")
public class VoiceChatController {

    // 教师选择接口
    @RequestMapping("/teacher/select")
    public R selectTeacher(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String username = String.valueOf(request.getSession().getAttribute("username"));
        String modelKey = String.valueOf(body.getOrDefault("modelKey", ""));
        String systemPrompt = String.valueOf(body.getOrDefault("systemPrompt", ""));
        if ("null".equals(username) || !org.springframework.util.StringUtils.hasText(modelKey)) return R.error("参数错误");
        AIChatUtil.setUserTeacher(username, modelKey, systemPrompt);
        return R.ok().put("data", "已切换至 " + modelKey);
    }

    @RequestMapping("/tts/select")
    public R selectTtsVoice(@RequestBody Map<String, Object> body) {
        String voice = String.valueOf(body.getOrDefault("voice", ""));
        if (!org.springframework.util.StringUtils.hasText(voice)) return R.error("请提供音色标识");
        VolcengineTtsUtil.setVoiceType(voice);
        return R.ok().put("data", "TTS音色已切换至 " + voice);
    }

    // 对话历史（按用户session存储，保留最近10轮）
    private static final ConcurrentHashMap<String, List<AIChatUtil.Message>> historyMap = new ConcurrentHashMap<>();

    private static final String SYSTEM_PROMPT_DEFAULT = "你是一个幽默的百科小老师。回答要生动有趣，可以加入一些有趣的科学事实或历史小故事。逻辑要清晰，可以用'第一、第二'来拆解。如果孩子问古诗，请尝试把诗句描绘成一幅画讲给他们听。字数100-150字。禁止负面暴力成人内容。";

    @RequestMapping("/chat")
    public R chat(@RequestParam("audio") MultipartFile audio,
                  @RequestParam(required = false) String characterId,
                  HttpServletRequest request) {
        if (audio == null || audio.isEmpty()) return R.error("请说话");
        try {
            // 1. 保存音频
            String fn = "voice_" + System.currentTimeMillis() + ".aac";
            File af = new File(getAudioDir(), fn);
            af.getParentFile().mkdirs();
            audio.transferTo(af);
            System.out.println("[语音助手] 收到录音: " + af.length() + " bytes");

            // 2. 语音识别
            String recognized = VolcengineSpeechUtil.speechToText(af);
            System.out.println("[语音助手] 识别: " + recognized);

            if (!StringUtils.hasText(recognized)) {
                return R.ok().put("data", mapOf("reply", "我没听清，再说一遍好吗？"));
            }

            // 3. AI对话（带历史记忆 + 角色提示词）
            String reply = chatWithDoubao(recognized, characterId, request);
            System.out.println("[语音助手] 回复: " + reply);

            // 4. TTS (V3优先，失败回退V1)
            String ttsFile = VolcengineTtsUtil.textToSpeech(reply, getTtsDir());
            if (ttsFile == null) ttsFile = VolcengineTtsV3Util.textToSpeech(reply, getTtsDir());
            String ttsUrl = ttsFile != null ? "/file/" + new File(ttsFile).getName() : null;

            // 5. 清理
            af.delete();

            Map<String, Object> result = mapOf("reply", reply, "recognized", recognized);
            if (ttsUrl != null) result.put("ttsUrl", ttsUrl);
            return R.ok().put("data", result);

        } catch (Exception e) {
            e.printStackTrace();
            return R.error("出错了: " + e.getMessage());
        }
    }

    private String chatWithDoubao(String userText, String characterId, HttpServletRequest request) {
        try {
            String uid = "voice_" + (StringUtils.hasText(request.getSession().getAttribute("username") + "")
                ? request.getSession().getAttribute("username") : request.getSession().getId());

            // 获取或创建历史
            List<AIChatUtil.Message> msgs = historyMap.get(uid);
            // 获取角色专属 System Prompt
            String charPrompt = com.cl.utils.CharacterPromptUtil.assistantPrompt(characterId);
            if (msgs == null) {
                msgs = new ArrayList<>();
                msgs.add(new AIChatUtil.Message("system", charPrompt));
                historyMap.put(uid, msgs);
            } else if (!msgs.isEmpty() && "system".equals(msgs.get(0).getRole())) {
                // 动态替换角色提示词：删旧补新
                msgs.remove(0);
                msgs.add(0, new AIChatUtil.Message("system", charPrompt));
            }

            // 添加用户消息
            msgs.add(new AIChatUtil.Message("user", userText));

            // 保留最近30轮（60条=30问+30答）+1条system
            while (msgs.size() > 61) msgs.remove(1);

            // 统一AI调用 — 自动根据 asr.properties 的 ai.provider 选择豆包或DeepSeek
            AIChatUtil.ChatResult result = AIChatUtil.chatWithMessages(
                    new ArrayList<>(msgs), 0.7, 300);
            String reply = result != null && result.getContent() != null
                    ? result.getContent().trim()
                    : "哎呀，我的小脑袋卡住了～再问我一次吧！";

            // 保存AI回复到历史
            msgs.add(new AIChatUtil.Message("assistant", reply));

            return reply;
        } catch (Exception e) {
            e.printStackTrace();
            return "哎呀，我的小脑袋卡住了～再问我一次吧！";
        }
    }

    private Map<String, Object> mapOf(String k1, Object v1) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(k1, v1);
        return m;
    }

    private Map<String, Object> mapOf(String k1, Object v1, String k2, Object v2) {
        Map<String, Object> m = mapOf(k1, v1);
        m.put(k2, v2);
        return m;
    }

    private Map<String, Object> mapOf(String k1, Object v1, String k2, Object v2, String k3, Object v3) {
        Map<String, Object> m = mapOf(k1, v1, k2, v2);
        m.put(k3, v3);
        return m;
    }

    /**
     * AI 生成古诗意境插画 — 根据译文生成水墨画风图片
     */
    @com.cl.annotation.IgnoreAuth
    @RequestMapping("/generateImage")
    public R generateImage(@RequestParam String poemTitle, @RequestParam String translation) {
        if (!StringUtils.hasText(translation)) return R.error("缺少译文内容");
        try {
            // 构建生图 prompt
            String prompt = "中国风水墨画风格，古风意境。根据以下古诗译文创作插画：" +
                translation.substring(0, Math.min(200, translation.length())) +
                "。要求：画面唯美、留白、古典。no text, no words, no letters, clean background";

            System.out.println("[生图] prompt=" + prompt.substring(0, Math.min(120, prompt.length())));

            String apiKey = AIRecitationReviewUtil.getApiKey();
            String model = "doubao-seedream-4-5-251128";
            String body = new org.json.JSONObject()
                .put("model", model)
                .put("prompt", prompt)
                .put("size", "2K")
                .put("response_format", "url")
                .put("sequential_image_generation", "disabled")
                .put("stream", false)
                .put("watermark", true)
                .toString();

            java.net.URL url = new java.net.URL("https://ark.cn-beijing.volces.com/api/v3/images/generations");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(120000);
            conn.setDoOutput(true);
            conn.getOutputStream().write(body.getBytes("UTF-8"));

            int code = conn.getResponseCode();
            System.out.println("[生图] HTTP " + code);
            if (code != 200) {
                java.io.InputStream es = conn.getErrorStream();
                String err = es != null ? new java.util.Scanner(es, "UTF-8").useDelimiter("\\A").next() : "";
                return R.error("AI绘图失败 HTTP" + code + ": " + err.substring(0, Math.min(200, err.length())));
            }

            String resp = new java.util.Scanner(conn.getInputStream(), "UTF-8").useDelimiter("\\A").next();
            org.json.JSONObject respJson = new org.json.JSONObject(resp);
            String imgUrl = respJson.getJSONArray("data").getJSONObject(0).optString("url", "");
            if (imgUrl.isEmpty()) return R.error("API未返回图片URL");

            // 下载图片
            System.out.println("[生图] 下载图片: " + imgUrl.substring(0, Math.min(80, imgUrl.length())));
            java.net.URL imgUrlObj = new java.net.URL(imgUrl);
            java.net.HttpURLConnection imgConn = (java.net.HttpURLConnection) imgUrlObj.openConnection();
            imgConn.setConnectTimeout(30000);
            imgConn.setReadTimeout(60000);
            java.io.InputStream imgIn = imgConn.getInputStream();

            String fileName = "poem_img_" + System.currentTimeMillis() + ".png";
            File outFile = new File(getTtsDir(), fileName);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(outFile);
            byte[] buf = new byte[8192];
            int n;
            while ((n = imgIn.read(buf)) != -1) fos.write(buf, 0, n);
            fos.close(); imgIn.close();
            System.out.println("[生图] 保存: " + outFile.getAbsolutePath() + " (" + outFile.length() + " bytes)");

            return R.ok().put("data", mapOf("imageUrl", "/file/" + fileName));

        } catch (Exception e) {
            e.printStackTrace();
            return R.error("生成失败: " + e.getMessage());
        }
    }

    /**
     * AI 智能出题 — 根据古诗内容生成5道单选题
     */
    @com.cl.annotation.IgnoreAuth
    @RequestMapping("/generateQuiz")
    public R generateQuiz(@RequestParam String poemTitle, @RequestParam String poemContent) {
        if (!StringUtils.hasText(poemContent)) return R.error("缺少古诗内容");
        try {
            String content = poemContent.length() > 400 ? poemContent.substring(0, 397) + "..." : poemContent;

            String systemPrompt = "你是一名资深小学语文老师。请针对提供的古诗，生成5道单选题。考查维度：字词解释(2题)、诗句理解(2题)、情感/背景(1题)。每题4个选项。必须严格只返回JSON数组，严禁任何说明文字或Markdown标记。格式：[{\"question\":\"题?\",\"options\":[\"A选项\",\"B选项\",\"C选项\",\"D选项\"],\"answer\":0,\"analysis\":\"解析\"}]";

            String userPrompt = "古诗标题：《" + poemTitle + "》\n古诗原文：\n" + content;

            // 用 chatWithMessages 手动传参，确保足够的 max_tokens 出5道完整题
            List<AIChatUtil.Message> msgs = new ArrayList<>();
            msgs.add(new AIChatUtil.Message("system", systemPrompt));
            msgs.add(new AIChatUtil.Message("user", userPrompt));
            AIChatUtil.ChatResult cr = AIChatUtil.chatWithMessages(msgs, 0.7, 3000);
            String resp = cr != null ? cr.getContent() : null;
            if (resp == null || resp.trim().isEmpty()) return R.error("AI未返回结果");

            System.out.println("[出题] 原始响应(" + resp.length() + "): " + resp.substring(0, Math.min(500, resp.length())));

            // 清洗JSON
            String json = cleanQuizJSON(resp);
            System.out.println("[出题] 清洗后(" + json.length() + "): " + json.substring(0, Math.min(500, json.length())));

            // 尝试解析，失败则尝试修复尾逗号等问题
            org.json.JSONArray arr;
            try {
                arr = new org.json.JSONArray(json);
            } catch (Exception parseErr) {
                // 尝试修复常见问题：尾逗号、不完整结尾
                String fixed = json.replaceAll(",\\s*]", "]").replaceAll(",\\s*}", "}");
                // 确保以 ] 结尾
                if (!fixed.trim().endsWith("]")) {
                    int lastBrace = fixed.lastIndexOf('}');
                    if (lastBrace > 0) fixed = fixed.substring(0, lastBrace + 1) + "]";
                }
                System.out.println("[出题] 修复后: " + fixed.substring(0, Math.min(500, fixed.length())));
                arr = new org.json.JSONArray(fixed);
            }
            return R.ok().put("data", arr.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("出题失败: " + e.getMessage());
        }
    }

    private String cleanQuizJSON(String str) {
        String s = str.trim();
        // 去掉 ```json ... ``` 包裹
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```").matcher(s);
        if (m.find()) s = m.group(1).trim();
        int start = s.indexOf('[');
        int end = s.lastIndexOf(']');
        if (start >= 0 && end > start) s = s.substring(start, end + 1);
        return s;
    }

    private String getAudioDir() { File d = new File("file"); d.mkdirs(); return d.getAbsolutePath(); }
    private String getTtsDir() {
        try { return new File(org.springframework.util.ResourceUtils.getFile("classpath:static"), "file").getAbsolutePath(); }
        catch (Exception e) { return System.getProperty("java.io.tmpdir"); }
    }
}
