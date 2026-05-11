package com.cl.utils;

import java.util.LinkedHashSet;
import java.util.Set;

public class RecitationReviewUtil {

    public static class ReviewResult {
        private String expectedText;
        private String recognizedText;
        private Integer score;
        private String comment;

        public String getExpectedText() {
            return expectedText;
        }

        public void setExpectedText(String expectedText) {
            this.expectedText = expectedText;
        }

        public String getRecognizedText() {
            return recognizedText;
        }

        public void setRecognizedText(String recognizedText) {
            this.recognizedText = recognizedText;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }

    public static ReviewResult review(String expectedText, String recognizedText) {
        ReviewResult result = new ReviewResult();
        String cleanExpected = normalize(expectedText);
        String cleanRecognized = normalize(recognizedText);
        result.setExpectedText(cleanExpected);
        result.setRecognizedText(cleanRecognized);

        if (cleanRecognized.length() == 0) {
            result.setScore(0);
            result.setComment("AI初评：未能识别出有效音频内容，请老师人工试听后确认。");
            return result;
        }

        if (cleanExpected.length() == 0) {
            result.setScore(70);
            result.setComment("AI初评：已完成语音转写，但当前任务缺少标准背诵文本，建议老师人工确认。识别内容：" + recognizedText);
            return result;
        }

        int distance = levenshtein(cleanExpected, cleanRecognized);
        double similarity = 1 - (distance * 1.0 / Math.max(cleanExpected.length(), cleanRecognized.length()));
        similarity = Math.max(0, similarity);
        double completeness = Math.min(cleanRecognized.length() * 1.0 / cleanExpected.length(), 1.0);
        int score = (int) Math.round(similarity * 75 + completeness * 25);
        score = Math.max(0, Math.min(100, score));

        Set<Character> missingChars = getMissingChars(cleanExpected, cleanRecognized);
        StringBuilder comment = new StringBuilder();
        comment.append("AI初评：自动识别文本为“").append(recognizedText).append("”。");
        comment.append(" 文本匹配度").append((int) Math.round(similarity * 100)).append("%，完整度").append((int) Math.round(completeness * 100)).append("%。");
        if (!missingChars.isEmpty()) {
            comment.append(" 可能遗漏/错误的字包括：");
            int index = 0;
            for (Character item : missingChars) {
                if (index >= 8) {
                    break;
                }
                if (index > 0) {
                    comment.append("、");
                }
                comment.append(item);
                index++;
            }
            comment.append("。");
        }
        if (score >= 90) {
            comment.append(" 整体背诵较准确，可重点关注语气和节奏。");
        } else if (score >= 75) {
            comment.append(" 背诵基本正确，建议再熟悉个别字句。");
        } else if (score >= 60) {
            comment.append(" 背诵有部分偏差，建议对照原文继续练习。");
        } else {
            comment.append(" 当前偏差较大，建议重新听录音并加强朗读训练。");
        }
        result.setScore(score);
        result.setComment(comment.toString());
        return result;
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replaceAll("[\\s\\p{Punct}，。！？；：“”‘’、《》…（）()【】\\-—_]", "")
                .trim();
    }

    private static Set<Character> getMissingChars(String expected, String actual) {
        Set<Character> missingChars = new LinkedHashSet<Character>();
        int expectedIndex = 0;
        int actualIndex = 0;
        while (expectedIndex < expected.length() && actualIndex < actual.length()) {
            if (expected.charAt(expectedIndex) == actual.charAt(actualIndex)) {
                expectedIndex++;
                actualIndex++;
            } else {
                missingChars.add(expected.charAt(expectedIndex));
                expectedIndex++;
            }
        }
        while (expectedIndex < expected.length()) {
            missingChars.add(expected.charAt(expectedIndex));
            expectedIndex++;
        }
        return missingChars;
    }

    private static int levenshtein(String source, String target) {
        int[][] dp = new int[source.length() + 1][target.length() + 1];
        for (int i = 0; i <= source.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= target.length(); j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= source.length(); i++) {
            for (int j = 1; j <= target.length(); j++) {
                int cost = source.charAt(i - 1) == target.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[source.length()][target.length()];
    }
}
