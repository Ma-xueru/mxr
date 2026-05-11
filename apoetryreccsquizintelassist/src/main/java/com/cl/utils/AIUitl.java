package com.cl.utils;

import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;

import java.util.ArrayList;
import java.util.List;

public class AIUitl {

    private static String API_KEY = "";
    private static String MODEL = "";
    static {
        try {
            java.util.Properties props = new java.util.Properties();
            props.load(AIUitl.class.getClassLoader().getResourceAsStream("asr.properties"));
            API_KEY = props.getProperty("ark.api.key", "");
            MODEL = props.getProperty("ark.model", "doubao-seed-1-8-251228");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static String getResponse(String question) {
        ArkService service = new ArkService(API_KEY);
        System.out.println("\n----- standard request -----");
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content("你是智能学习助手").build();
        final ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(question+",简略回答，一定不超过100字").build();
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(MODEL)
                .messages(messages)
                .build();
        StringBuilder result = new StringBuilder();
        service.createChatCompletion(chatCompletionRequest).getChoices().forEach(choice -> result.append(choice.getMessage().getContent()));

        System.out.println(result);
        // shutdown service
        service.shutdownExecutor();
        return result.toString();
    }

    public static String getPoemContentByTitle(String title) {
        if (title == null || title.trim().length() == 0) {
            return "";
        }
        ArkService service = new ArkService(API_KEY);
        try {
            final List<ChatMessage> messages = new ArrayList<ChatMessage>();
            messages.add(ChatMessage.builder()
                    .role(ChatMessageRole.SYSTEM)
                    .content("你是古诗文检索助手。请尽量依据公开常见版本返回古诗原文。只返回诗文正文，不要作者、题解、标点说明、解释、前后缀。查不到就只返回“未找到”。")
                    .build());
            messages.add(ChatMessage.builder()
                    .role(ChatMessageRole.USER)
                    .content("请联网检索并返回《" + title + "》的完整原文，只返回正文。")
                    .build());

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(MODEL)
                    .messages(messages)
                    .build();

            StringBuilder result = new StringBuilder();
            service.createChatCompletion(request).getChoices().forEach(choice -> result.append(choice.getMessage().getContent()));
            String content = result.toString();
            if (content == null) {
                return "";
            }
            content = content.replace("未找到。", "未找到").replace("《" + title + "》", "").trim();
            if ("未找到".equals(content)) {
                return "";
            }
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            service.shutdownExecutor();
        }
    }
}
