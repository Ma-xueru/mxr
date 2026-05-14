package com.cl.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 统一AI对话工具 — 支持 DeepSeek/豆包/智谱/千问/MiMo 一键切换
 *
 * asr.properties 配置:
 *   ai.global.model=deepseek    # deepseek/doubao/zhipu/qwen/mimo
 */
public class AIChatUtil {

    private static String MODEL_KEY;
    // 模型配置
    private static String API_URL;
    private static String API_KEY;
    private static String MODEL_NAME;
    private static String AUTH_HEADER; // "Authorization: Bearer xxx" or "api-key: xxx"

    static {
        try {
            java.util.Properties p = new java.util.Properties();
            p.load(AIChatUtil.class.getClassLoader().getResourceAsStream("asr.properties"));
            MODEL_KEY = p.getProperty("ai.global.model", "deepseek").trim().toLowerCase();
            loadModelConfig(p);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void loadModelConfig(java.util.Properties p) {
        switch (MODEL_KEY) {
            case "doubao":
                API_URL = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
                API_KEY = p.getProperty("ark.api.key", "");
                MODEL_NAME = p.getProperty("ark.model", "doubao-seed-1-8-251228");
                AUTH_HEADER = "Bearer " + API_KEY;
                break;
            case "zhipu":
                API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
                API_KEY = p.getProperty("ai.zhipu.api_key", "");
                MODEL_NAME = p.getProperty("ai.zhipu.model", "glm-4.7");
                AUTH_HEADER = "Bearer " + API_KEY;
                break;
            case "qwen":
                API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
                API_KEY = p.getProperty("ai.qwen.api_key", "");
                MODEL_NAME = p.getProperty("ai.qwen.model", "qwen-plus");
                AUTH_HEADER = "Bearer " + API_KEY;
                break;
            case "mimo":
                API_URL = "https://api.xiaomimimo.com/v1/chat/completions";
                API_KEY = p.getProperty("ai.mimo.api_key", "");
                MODEL_NAME = p.getProperty("ai.mimo.model", "mimo-v2.5-pro");
                AUTH_HEADER = API_KEY;
                break;
            case "hunyuan":
                API_URL = "https://tokenhub.tencentmaas.com/v1/chat/completions";
                API_KEY = p.getProperty("ai.hunyuan.api_key", "");
                MODEL_NAME = p.getProperty("ai.hunyuan.model", "hy3-preview");
                AUTH_HEADER = "Bearer " + API_KEY;
                break;
            default: // deepseek
                API_URL = "https://api.deepseek.com/chat/completions";
                API_KEY = p.getProperty("ai.deepseek.api_key", "");
                MODEL_NAME = p.getProperty("ai.deepseek.model", "deepseek-v4-flash");
                AUTH_HEADER = "Bearer " + API_KEY;
        }
        System.out.println("[AIChatUtil] 全局模型: " + MODEL_KEY + " -> " + MODEL_NAME + " url=" + API_URL);
    }

    public static String getProvider() { return MODEL_KEY; }
    public static String getModel() { return MODEL_NAME; }

    // ---------- 简单消息结构 ----------
    public static class Message {
        private String role; private String content;
        public Message(String role, String content) { this.role = role; this.content = content; }
        public String getRole() { return role; }
        public String getContent() { return content; }
    }

    // ---------- 聊天结果 ----------
    public static class ChatResult {
        private String content; private String thinking;
        public ChatResult(String content) { this.content = content; }
        public ChatResult(String content, String thinking) { this.content = content; this.thinking = thinking; }
        public String getContent() { return content; }
        public String getThinking() { return thinking; }
    }

    // ========== 公开 API ==========

    public static String chat(String systemPrompt, String userMessage) {
        List<Message> msgs = new ArrayList<>();
        msgs.add(new Message("system", systemPrompt));
        msgs.add(new Message("user", userMessage));
        ChatResult r = chatWithMessages(msgs, 0.3, 500);
        return r != null ? r.getContent() : "";
    }

    public static ChatResult chatWithMessages(List<Message> messages, double temperature, int maxTokens) {
        String label = "[" + MODEL_KEY + "] ";
        System.out.println(label + "请求 model=" + MODEL_NAME + " msgs=" + messages.size());

        try {
            JSONArray msgsArr = new JSONArray();
            for (Message m : messages) {
                msgsArr.put(new JSONObject().put("role", m.getRole()).put("content", m.getContent()));
            }

            JSONObject body = new JSONObject();
            body.put("model", MODEL_NAME);
            body.put("messages", msgsArr);
            body.put("temperature", temperature);
            body.put("max_tokens", maxTokens);
            body.put("stream", false);

            // MiMo 使用 max_completion_tokens 和 api-key 头
            if ("mimo".equals(MODEL_KEY)) {
                body.remove("max_tokens");
                body.put("max_completion_tokens", maxTokens);
            }
            // DeepSeek 关闭思考加速
            if ("deepseek".equals(MODEL_KEY)) {
                body.put("thinking", new JSONObject().put("type", "disabled"));
            }

            String resp = httpPost(API_URL, body.toString());
            if (resp == null) { System.out.println(label + "无响应"); return null; }

            JSONObject json = new JSONObject(resp);
            JSONArray choices = json.optJSONArray("choices");
            if (choices == null || choices.length() == 0) {
                System.out.println(label + "无choices: " + resp.substring(0, Math.min(200, resp.length())));
                return null;
            }

            JSONObject msg = choices.getJSONObject(0).optJSONObject("message");
            if (msg == null) { System.out.println(label + "无message"); return null; }

            String content = msg.optString("content", "");
            String thinking = msg.optString("reasoning_content", "");
            System.out.println(label + "完成 content=" + content.length() + "chars");
            return new ChatResult(content, thinking);
        } catch (Exception e) {
            System.out.println(label + "异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ========== HTTP 工具 ==========

    private static String httpPost(String urlStr, String body) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            // MiMo 用 api-key 头，其余用 Authorization: Bearer
            if ("mimo".equals(MODEL_KEY)) {
                conn.setRequestProperty("api-key", AUTH_HEADER);
            } else {
                conn.setRequestProperty("Authorization", AUTH_HEADER);
            }
            conn.setConnectTimeout(30000); conn.setReadTimeout(120000);
            conn.setDoOutput(true);

            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            conn.getOutputStream().write(bytes); conn.getOutputStream().close();

            int code = conn.getResponseCode();
            InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
            if (is == null) return null;

            Scanner s = new Scanner(is, "UTF-8").useDelimiter("\\A");
            String resp = s.hasNext() ? s.next() : ""; s.close();

            if (code != 200) {
                System.out.println("[AI] HTTP " + code + ": " + resp.substring(0, Math.min(300, resp.length())));
                return null;
            }
            return resp;
        } catch (Exception e) {
            System.out.println("[AI] HTTP异常: " + e.getMessage());
            return null;
        }
    }
}
