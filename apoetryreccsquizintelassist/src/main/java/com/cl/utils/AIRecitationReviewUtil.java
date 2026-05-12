package com.cl.utils;

import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AIRecitationReviewUtil {

    private static String API_KEY = "";
    private static String MODEL = "";
    static {
        try {
            java.util.Properties props = new java.util.Properties();
            props.load(AIRecitationReviewUtil.class.getClassLoader().getResourceAsStream("asr.properties"));
            API_KEY = props.getProperty("ark.api.key", "");
            MODEL = props.getProperty("ark.model", "doubao-seed-1-8-251228");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static String getApiKey() { return API_KEY; }
    public static String getModel() { return MODEL; }

    public static class DimensionScore {
        private String name; private int score; private int weight;
        private String comment; private String encourage;
        public String getName() { return name; } public void setName(String v) { name = v; }
        public int getScore() { return score; } public void setScore(int v) { score = v; }
        public int getWeight() { return weight; } public void setWeight(int v) { weight = v; }
        public String getComment() { return comment; } public void setComment(String v) { comment = v; }
        public String getEncourage() { return encourage; } public void setEncourage(String v) { encourage = v; }
    }

    public static class ReviewResult {
        private int totalScore; private String expectedTitle;
        private List<DimensionScore> dimensions; private String overallComment; private String rawJson;
        public int getTotalScore() { return totalScore; } public void setTotalScore(int v) { totalScore = v; }
        public String getExpectedTitle() { return expectedTitle; } public void setExpectedTitle(String v) { expectedTitle = v; }
        public List<DimensionScore> getDimensions() { return dimensions; } public void setDimensions(List<DimensionScore> v) { dimensions = v; }
        public String getOverallComment() { return overallComment; } public void setOverallComment(String v) { overallComment = v; }
        public String getRawJson() { return rawJson; } public void setRawJson(String v) { rawJson = v; }
    }

    public static ReviewResult review(String expectedText, String recognizedText, String poemTitle) {
        return doReview(expectedText, recognizedText, poemTitle, "recitation");
    }

    /** 跟读评测 — 使用跟读专用prompt */
    public static ReviewResult reviewFollowRead(String expectedText, String recognizedText, String poemTitle) {
        return doReview(expectedText, recognizedText, poemTitle, "followread");
    }

    private static ReviewResult doReview(String expectedText, String recognizedText, String poemTitle, String mode) {
        if (recognizedText == null || recognizedText.trim().isEmpty()) {
            System.out.println("[AI评测] 识别文本为空，跳过");
            return null;
        }

        String modeLabel = "followread".equals(mode) ? "跟读" : "背诵";
        System.out.println("[AI评测] 豆包AI：开始" + modeLabel + "评测 - 《" + poemTitle + "》识别文本" + recognizedText.length() + "字");
        System.out.println("[AI评测] 豆包AI：API_KEY=" + (API_KEY.isEmpty() ? "(空!!!)" : API_KEY.substring(0, Math.min(8, API_KEY.length())) + "...") + " MODEL=" + MODEL);

        ArkService service = new ArkService(API_KEY);
        try {
            String prompt = "followread".equals(mode)
                    ? buildFollowReadPrompt(expectedText, recognizedText, poemTitle)
                    : buildPrompt(expectedText, recognizedText, poemTitle);
            String systemRole = "followread".equals(mode)
                    ? "你是语文跟读评审助手。请严格按JSON格式返回评审结果，不要添加任何说明文字。"
                    : "你是古诗背诵评审助手。请严格按JSON格式返回评审结果，不要添加任何说明文字。";
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.builder().role(ChatMessageRole.SYSTEM)
                    .content(systemRole).build());
            messages.add(ChatMessage.builder().role(ChatMessageRole.USER).content(prompt).build());

            ChatCompletionRequest req = ChatCompletionRequest.builder()
                    .model(MODEL).messages(messages).temperature(0.3).build();

            System.out.println("[AI评测] 豆包AI：发送请求...");
            StringBuilder sb = new StringBuilder();
            service.createChatCompletion(req).getChoices().forEach(c -> sb.append(c.getMessage().getContent()));
            String response = sb.toString();
            System.out.println("[AI评测] 豆包AI：响应长度=" + response.length() + " 内容=" + (response.length() > 200 ? response.substring(0, 200) + "..." : response));

            ReviewResult result = parseResponse(response, poemTitle);
            if (result == null) {
                System.out.println("[AI评测] 豆包AI：解析响应失败，返回null");
            } else {
                System.out.println("[AI评测] 豆包AI：解析成功，总分=" + result.getTotalScore());
            }
            return result;
        } catch (Exception e) {
            System.out.println("[AI评测] 豆包AI：异常 - " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            service.shutdownExecutor();
        }
    }

    private static String buildPrompt(String expectedText, String recognizedText, String poemTitle) {
        StringBuilder sb = new StringBuilder();
        sb.append("学生背诵了《").append(poemTitle).append("》。请根据标准原文和语音识别文本进行全面评审。\n\n");
        sb.append("【标准原文】\n");
        String exp = expectedText != null && !expectedText.isEmpty() ? expectedText : "（暂未收录原文）";
        if (exp.length() > 1500) exp = exp.substring(0, 1497) + "...";
        sb.append(exp).append("\n\n");
        sb.append("【识别文本】\n").append(recognizedText).append("\n\n");
        sb.append("请按照4个维度评分（每项0-100分），并给出具体、鼓励性反馈：\n");
        sb.append("- 准确性(40%): 字词是否读对，有无多读漏读错读\n");
        sb.append("- 完整度(25%): 是否覆盖了原文的主要部分\n");
        sb.append("- 流利度(20%): 背诵是否流畅\n");
        sb.append("- 发音建议(15%): 针对可能发音不准的字词给出建议\n\n");
        sb.append("严格返回以下JSON（不要markdown代码块标记）：\n");
        sb.append("{\"totalScore\":85,\"dimensions\":[");
        sb.append("{\"name\":\"准确性\",\"score\":88,\"weight\":40,\"comment\":\"...\",\"encourage\":\"...\"},");
        sb.append("{\"name\":\"完整度\",\"score\":80,\"weight\":25,\"comment\":\"...\",\"encourage\":\"...\"},");
        sb.append("{\"name\":\"流利度\",\"score\":85,\"weight\":20,\"comment\":\"...\",\"encourage\":\"...\"},");
        sb.append("{\"name\":\"发音建议\",\"score\":82,\"weight\":15,\"comment\":\"...\",\"encourage\":\"...\"}");
        sb.append("],\"overallComment\":\"语气温和鼓励的总评\"}");
        return sb.toString();
    }

    private static String buildFollowReadPrompt(String expectedText, String recognizedText, String poemTitle) {
        StringBuilder sb = new StringBuilder();
        sb.append("学生跟读了《").append(poemTitle).append("》中的句子。请根据标准原文和语音识别文本进行跟读评测。\n\n");
        sb.append("跟读是看着原文朗读，不是背诵，所以重点考察发音和模仿能力，不考察记忆。\n\n");
        sb.append("【标准原文】\n");
        String exp = expectedText != null && !expectedText.isEmpty() ? expectedText : "（暂未收录原文）";
        if (exp.length() > 1500) exp = exp.substring(0, 1497) + "...";
        sb.append(exp).append("\n\n");
        sb.append("【识别文本】\n").append(recognizedText).append("\n\n");
        sb.append("请按照4个维度评分（每项0-100分），并给出具体、鼓励性反馈：\n");
        sb.append("- 发音准确度(40%): 每个字读音是否标准，声母韵母是否清晰\n");
        sb.append("- 语调模仿(25%): 是否能模仿标准语音的抑扬顿挫\n");
        sb.append("- 流利度(20%): 朗读是否流畅自然，不磕巴\n");
        sb.append("- 节奏感(15%): 语速是否适中，断句是否正确\n\n");
        sb.append("严格返回以下JSON（不要markdown代码块标记）：\n");
        sb.append("{\"totalScore\":85,\"dimensions\":[");
        sb.append("{\"name\":\"发音准确度\",\"score\":88,\"weight\":40,\"comment\":\"...\",\"encourage\":\"...\"},");
        sb.append("{\"name\":\"语调模仿\",\"score\":80,\"weight\":25,\"comment\":\"...\",\"encourage\":\"...\"},");
        sb.append("{\"name\":\"流利度\",\"score\":85,\"weight\":20,\"comment\":\"...\",\"encourage\":\"...\"},");
        sb.append("{\"name\":\"节奏感\",\"score\":82,\"weight\":15,\"comment\":\"...\",\"encourage\":\"...\"}");
        sb.append("],\"overallComment\":\"语气温和鼓励的总评，强调跟读的优点\"}");
        return sb.toString();
    }

    private static ReviewResult parseResponse(String response, String poemTitle) {
        if (response == null || response.trim().isEmpty()) return null;
        try {
            String json = response.trim();
            int s = json.indexOf('{'), e = json.lastIndexOf('}');
            if (s >= 0 && e > s) json = json.substring(s, e + 1);

            JSONObject obj = new JSONObject(json);
            ReviewResult result = new ReviewResult();
            result.setTotalScore(obj.optInt("totalScore", 0));
            result.setExpectedTitle(poemTitle);
            result.setOverallComment(obj.optString("overallComment", ""));
            result.setRawJson(json);

            JSONArray dims = obj.optJSONArray("dimensions");
            if (dims != null) {
                List<DimensionScore> list = new ArrayList<>();
                for (int i = 0; i < dims.length(); i++) {
                    JSONObject d = dims.getJSONObject(i);
                    DimensionScore ds = new DimensionScore();
                    ds.setName(d.optString("name", ""));
                    ds.setScore(d.optInt("score", 0));
                    ds.setWeight(d.optInt("weight", 0));
                    ds.setComment(d.optString("comment", ""));
                    ds.setEncourage(d.optString("encourage", ""));
                    list.add(ds);
                }
                result.setDimensions(list);
            }
            if (result.getTotalScore() == 0 && dims != null && dims.length() > 0) {
                int w = 0;
                for (DimensionScore d : result.getDimensions()) w += d.getScore() * d.getWeight();
                result.setTotalScore(Math.round(w / 100f));
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
