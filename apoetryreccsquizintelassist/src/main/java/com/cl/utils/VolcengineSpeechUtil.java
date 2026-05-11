package com.cl.utils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;

public class VolcengineSpeechUtil {

    private static String APP_ID = "";
    private static String ACCESS_TOKEN = "";
    private static String CLUSTER = "";
    static {
        try {
            java.util.Properties props = new java.util.Properties();
            props.load(VolcengineSpeechUtil.class.getClassLoader().getResourceAsStream("asr.properties"));
            APP_ID = props.getProperty("volc.asr.appid", "");
            ACCESS_TOKEN = props.getProperty("volc.asr.token", "");
            CLUSTER = props.getProperty("volc.asr.cluster", "volcengine_input_common");
        } catch (Exception e) { e.printStackTrace(); }
    }
    private static final String ASR_URL = "https://openspeech.bytedance.com/api/v1/asr";

    public static String speechToText(File audioFile) {
        if (audioFile == null || !audioFile.exists()) {
            System.out.println("[VolcengineSpeech] File not found");
            return "";
        }
        System.out.println("[VolcengineSpeech] Processing: " + audioFile.length() + " bytes");

        try {
            byte[] audioBytes = readFile(audioFile);
            String base64Audio = Base64.getEncoder().encodeToString(audioBytes);
            String format = getFormat(audioFile.getName());
            String reqId = java.util.UUID.randomUUID().toString();

            JSONObject body = new JSONObject();
            body.put("app", new JSONObject()
                    .put("appid", APP_ID)
                    .put("token", ACCESS_TOKEN)
                    .put("cluster", CLUSTER));
            body.put("user", new JSONObject().put("uid", "recitation_student"));
            body.put("audio", new JSONObject()
                    .put("format", format)
                    .put("rate", 16000)
                    .put("bits", 16)
                    .put("channel", 1)
                    .put("language", "zh-CN")
                    .put("data", base64Audio));
            body.put("request", new JSONObject()
                    .put("model_name", "onesentence_recognition")
                    .put("reqid", reqId)
                    .put("sequence", 1)
                    .put("enable_itn", true)
                    .put("enable_punc", true));

            java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                    new java.net.URL(ASR_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer;" + ACCESS_TOKEN);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.getOutputStream().write(body.toString().getBytes("UTF-8"));

            int code = conn.getResponseCode();
            System.out.println("[VolcengineSpeech] HTTP " + code);

            if (code != 200) {
                java.io.InputStream es = conn.getErrorStream();
                if (es != null) {
                    java.util.Scanner s = new java.util.Scanner(es, "UTF-8").useDelimiter("\\A");
                    System.out.println("[VolcengineSpeech] Error: " + (s.hasNext() ? s.next() : ""));
                }
                return "";
            }

            java.io.InputStream is = conn.getInputStream();
            java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
            String resp = s.hasNext() ? s.next() : "";
            is.close();
            System.out.println("[VolcengineSpeech] Resp: " + resp.substring(0, Math.min(200, resp.length())));

            JSONObject respJson = new JSONObject(resp);
            JSONObject result = respJson.optJSONObject("result");
            if (result != null) {
                String text = result.optString("text", "");
                if (!text.isEmpty()) {
                    System.out.println("[VolcengineSpeech] Text: " + text);
                    return text.trim();
                }
            }
            String text = respJson.optString("text", "");
            return text.trim();
        } catch (Exception e) {
            System.out.println("[VolcengineSpeech] Exception: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    private static String getFormat(String fileName) {
        int dot = fileName.lastIndexOf(".");
        return (dot == -1 || dot == fileName.length() - 1) ? "mp3" : fileName.substring(dot + 1).toLowerCase();
    }

    private static byte[] readFile(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            return data;
        }
    }
}
