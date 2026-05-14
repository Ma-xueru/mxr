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

    // ========== 教师模式切换（按用户session） ==========
    private static final Map<String, String[]> USER_TEACHER = new java.util.concurrent.ConcurrentHashMap<>();
    private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();

    public static void setUserTeacher(String userId, String modelKey, String systemPrompt) {
        USER_TEACHER.put(userId, new String[]{modelKey, systemPrompt});
        System.out.println("[AIChatUtil] 用户" + userId + " 切换教师: " + modelKey);
    }
    public static void setCurrentUser(String userId) { CURRENT_USER.set(userId); }
    public static void clearCurrentUser() { CURRENT_USER.remove(); }

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
        // 检查教师模式覆盖
        String userId = CURRENT_USER.get();
        String[] override = userId != null ? USER_TEACHER.get(userId) : null;
        String useKey = override != null ? override[0] : MODEL_KEY;
        String overridePrompt = override != null ? override[1] : null;

        String label = "[" + useKey + "] ";
        System.out.println(label + "请求 msgs=" + messages.size());

        // 如果有教师系统提示词，替换或插入
        if (overridePrompt != null && !messages.isEmpty() && "system".equals(messages.get(0).getRole())) {
            messages.set(0, new Message("system", overridePrompt));
        }

        // 使用对应模型配置
        String url, modelName, auth;
        if (override != null) {
            ModelConfig mc = loadModelConfigByKey(useKey);
            url = mc.url; modelName = mc.model; auth = mc.auth;
        } else {
            url = API_URL; modelName = MODEL_NAME; auth = AUTH_HEADER;
        }
        System.out.println(label + "model=" + modelName);

        try {
            JSONArray msgsArr = new JSONArray();
            for (Message m : messages) {
                msgsArr.put(new JSONObject().put("role", m.getRole()).put("content", m.getContent()));
            }

            JSONObject body = new JSONObject();
            body.put("model", modelName);
            body.put("messages", msgsArr);
            body.put("temperature", temperature);
            body.put("max_tokens", maxTokens);
            body.put("stream", false);

            if ("mimo".equals(useKey)) {
                body.remove("max_tokens");
                body.put("max_completion_tokens", maxTokens);
            }
            if ("deepseek".equals(useKey)) {
                body.put("thinking", new JSONObject().put("type", "disabled"));
            }

            String resp = httpPostWithAuth(url, body.toString(), useKey, auth);
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

    // ========== 模型配置查询 ==========

    static class ModelConfig { String url, model, auth; ModelConfig(String u, String m, String a) { url=u; model=m; auth=a; } }

    private static ModelConfig loadModelConfigByKey(String key) {
        java.util.Properties p = new java.util.Properties();
        try { p.load(AIChatUtil.class.getClassLoader().getResourceAsStream("asr.properties")); } catch (Exception e) {}
        switch (key) {
            case "doubao": return new ModelConfig("https://ark.cn-beijing.volces.com/api/v3/chat/completions", p.getProperty("ark.model","doubao-seed-1-8-251228"), "Bearer "+p.getProperty("ark.api.key",""));
            case "zhipu": return new ModelConfig("https://open.bigmodel.cn/api/paas/v4/chat/completions", p.getProperty("ai.zhipu.model","glm-4.7"), "Bearer "+p.getProperty("ai.zhipu.api_key",""));
            case "qwen": return new ModelConfig("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions", p.getProperty("ai.qwen.model","qwen-plus"), "Bearer "+p.getProperty("ai.qwen.api_key",""));
            case "mimo": return new ModelConfig("https://api.xiaomimimo.com/v1/chat/completions", p.getProperty("ai.mimo.model","mimo-v2.5-pro"), p.getProperty("ai.mimo.api_key",""));
            case "hunyuan": return new ModelConfig("https://tokenhub.tencentmaas.com/v1/chat/completions", p.getProperty("ai.hunyuan.model","hy3-preview"), "Bearer "+p.getProperty("ai.hunyuan.api_key",""));
            default: return new ModelConfig("https://api.deepseek.com/chat/completions", p.getProperty("ai.deepseek.model","deepseek-v4-flash"), "Bearer "+p.getProperty("ai.deepseek.api_key",""));
        }
    }

    // ========== HTTP 工具 ==========

    private static String httpPostWithAuth(String urlStr, String body, String modelKey, String authHeader) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            if ("mimo".equals(modelKey)) {
                conn.setRequestProperty("api-key", authHeader);
            } else {
                conn.setRequestProperty("Authorization", authHeader);
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
