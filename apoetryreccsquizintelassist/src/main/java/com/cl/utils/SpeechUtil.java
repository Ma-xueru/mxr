package com.cl.utils;

import com.baidu.aip.speech.AipSpeech;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

public class SpeechUtil {

    private static AipSpeech speechClient = null;

    private static AipSpeech getSpeechClient() {
        if (speechClient == null) {
            speechClient = new AipSpeech(BaiduUtil.APP_ID, BaiduUtil.API_KEY, BaiduUtil.SECRET_KEY);
            speechClient.setConnectionTimeoutInMillis(5000);
            speechClient.setSocketTimeoutInMillis(60000);
        }
        return speechClient;
    }

    public static String speechToText(File audioFile) {
        if (audioFile == null || !audioFile.exists()) {
            System.out.println("[BaiduSpeech] File not found: " + (audioFile != null ? audioFile.getAbsolutePath() : "null"));
            return "";
        }
        System.out.println("[BaiduSpeech] Processing: " + audioFile.getAbsolutePath() + " size=" + audioFile.length());
        try {
            String format = getFormat(audioFile.getName());
            System.out.println("[BaiduSpeech] Format: " + format);
            HashMap<String, Object> options = new HashMap<String, Object>();
            options.put("dev_pid", 1537);
            JSONObject result = getSpeechClient().asr(audioFile.getAbsolutePath(), format, 16000, options);
            System.out.println("[BaiduSpeech] Result: " + (result != null ? result.toString() : "null"));
            if (result == null) return "";
            if (result.has("err_no") && result.getInt("err_no") != 0) {
                System.out.println("[BaiduSpeech] Error: " + result.optString("err_msg", ""));
                return "";
            }
            if (result.has("result")) {
                JSONArray array = result.getJSONArray("result");
                if (array.length() > 0) {
                    String text = array.getString(0).trim();
                    System.out.println("[BaiduSpeech] Recognized: " + text);
                    return text;
                }
            }
        } catch (Exception e) {
            System.out.println("[BaiduSpeech] Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return "";
    }

    private static String getFormat(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return "wav";
        }
        return fileName.substring(dotIndex + 1).toLowerCase();
    }
}
