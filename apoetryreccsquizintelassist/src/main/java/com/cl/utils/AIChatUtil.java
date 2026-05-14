package com.cl.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 统一AI对话工具 — 支持豆包(Doubao)和DeepSeek，通过 asr.properties 切换
 *
 * 配置项 (asr.properties):
 *   ai.provider=doubao          # doubao 或 deepseek
 *   ai.deepseek.api_key=sk-xxx
 *   ai.deepseek.model=deepseek-v4-pro
 *   ai.deepseek.base_url=https://api.deepseek.com
 */
public class AIChatUtil {

    private static String PROVIDER;
    private static String DOUBAO_API_KEY;
    private static String DOUBAO_MODEL;
    private static String DEEPSEEK_API_KEY;
    private static String DEEPSEEK_MODEL;
    private static String DEEPSEEK_BASE_URL;

    static {
        try {
            java.util.Properties props = new java.util.Properties();
            props.load(AIChatUtil.class.getClassLoader().getResourceAsStream("asr.properties"));
            PROVIDER = props.getProperty("ai.provider", "doubao").trim().toLowerCase();
            DOUBAO_API_KEY = props.getProperty("ark.api.key", "");
            DOUBAO_MODEL = props.getProperty("ark.model", "doubao-seed-1-8-251228");
            DEEPSEEK_API_KEY = props.getProperty("ai.deepseek.api_key", "");
            DEEPSEEK_MODEL = props.getProperty("ai.deepseek.model", "deepseek-v4-pro");
            DEEPSEEK_BASE_URL = props.getProperty("ai.deepseek.base_url", "https://api.deepseek.com");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static String getProvider() { return PROVIDER; }
    public static String getModel() { return "deepseek".equals(PROVIDER) ? DEEPSEEK_MODEL : DOUBAO_MODEL; }

    // ---------- 简单消息结构 ----------
    public static class Message {
        private String role;
        private String content;
        public Message(String role, String content) { this.role = role; this.content = content; }
        public String getRole() { return role; }
        public String getContent() { return content; }
    }

    // ---------- 聊天结果 ----------
    public static class ChatResult {
        private String content;
        private String thinking; // DeepSeek 思考链
        public ChatResult(String content) { this.content = content; }
        public ChatResult(String content, String thinking) { this.content = content; this.thinking = thinking; }
        public String getContent() { return content; }
        public String getThinking() { return thinking; }
    }

    // ========== 公开 API ==========

    /** 单轮对话 */
    public static String chat(String systemPrompt, String userMessage) {
        List<Message> msgs = new ArrayList<>();
        msgs.add(new Message("system", systemPrompt));
        msgs.add(new Message("user", userMessage));
        ChatResult r = chatWithMessages(msgs, 0.3, 500);
        return r != null ? r.getContent() : "";
    }

    /** 多轮对话 (带历史) */
    public static ChatResult chatWithMessages(List<Message> messages, double temperature, int maxTokens) {
        if ("deepseek".equals(PROVIDER)) {
            return chatDeepSeek(messages, temperature, maxTokens);
        } else {
            return chatDoubao(messages, temperature, maxTokens);
        }
    }

    // ========== DeepSeek (OpenAI 兼容 HTTP) ==========

    private static ChatResult chatDeepSeek(List<Message> messages, double temperature, int maxTokens) {
        String label = "[DeepSeek] ";
        System.out.println(label + "请求 model=" + DEEPSEEK_MODEL + " msgs=" + messages.size());

        try {
            JSONArray msgsArr = new JSONArray();
            for (Message m : messages) {
                msgsArr.put(new JSONObject().put("role", m.getRole()).put("content", m.getContent()));
            }

            JSONObject body = new JSONObject();
            body.put("model", DEEPSEEK_MODEL);
            body.put("messages", msgsArr);
            body.put("temperature", temperature);
            body.put("max_tokens", maxTokens);
            body.put("stream", false);
            body.put("thinking", new JSONObject().put("type", "disabled"));

            String resp = httpPost(DEEPSEEK_BASE_URL + "/chat/completions",
                    "Bearer " + DEEPSEEK_API_KEY, body.toString());
            if (resp == null) return null;

            JSONObject json = new JSONObject(resp);
            JSONArray choices = json.optJSONArray("choices");
            if (choices == null || choices.length() == 0) {
                System.out.println(label + "无choices: " + resp.substring(0, Math.min(200, resp.length())));
                return null;
            }

            JSONObject choice = choices.getJSONObject(0);
            JSONObject msg = choice.optJSONObject("message");
            if (msg == null) {
                System.out.println(label + "无message: " + resp.substring(0, Math.min(200, resp.length())));
                return null;
            }

            String content = msg.optString("content", "");
            String thinking = msg.optString("reasoning_content", "");
            System.out.println(label + "完成 content=" + (content.length()) + "chars thinking=" + (thinking.length()) + "chars");
            return new ChatResult(content, thinking);
        } catch (Exception e) {
            System.out.println(label + "异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ========== Doubao (Ark SDK) ==========

    private static ChatResult chatDoubao(List<Message> messages, double temperature, int maxTokens) {
        String label = "[Doubao] ";
        System.out.println(label + "请求 model=" + DOUBAO_MODEL + " msgs=" + messages.size());

        com.volcengine.ark.runtime.service.ArkService service =
                new com.volcengine.ark.runtime.service.ArkService(DOUBAO_API_KEY);
        try {
            List<com.volcengine.ark.runtime.model.completion.chat.ChatMessage> arkMsgs = new ArrayList<>();
            for (Message m : messages) {
                com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole role;
                switch (m.getRole()) {
                    case "system": role = com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole.SYSTEM; break;
                    case "assistant": role = com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole.ASSISTANT; break;
                    default: role = com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole.USER;
                }
                arkMsgs.add(com.volcengine.ark.runtime.model.completion.chat.ChatMessage.builder()
                        .role(role).content(m.getContent()).build());
            }

            com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest req =
                    com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest.builder()
                            .model(DOUBAO_MODEL).messages(arkMsgs)
                            .temperature(temperature).maxTokens(maxTokens).build();

            StringBuilder sb = new StringBuilder();
            service.createChatCompletion(req).getChoices()
                    .forEach(c -> sb.append(c.getMessage().getContent()));
            System.out.println(label + "完成 content=" + sb.length() + "chars");
            return new ChatResult(sb.toString().trim());
        } catch (Exception e) {
            System.out.println(label + "异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            service.shutdownExecutor();
        }
    }

    // ========== HTTP 工具 ==========

    private static String httpPost(String urlStr, String authHeader, String body) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", authHeader);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(120000);
            conn.setDoOutput(true);

            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            conn.getOutputStream().write(bytes);
            conn.getOutputStream().close();

            int code = conn.getResponseCode();
            InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
            if (is == null) return null;

            Scanner s = new Scanner(is, "UTF-8").useDelimiter("\\A");
            String resp = s.hasNext() ? s.next() : "";
            s.close();

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
