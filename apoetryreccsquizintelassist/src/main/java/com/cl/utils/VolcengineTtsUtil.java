package com.cl.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import org.json.JSONObject;

/**
 * TTS 文字转语音 — 火山引擎语音合成 V1
 */
public class VolcengineTtsUtil {

    private static String APP_ID = "";
    private static String ACCESS_TOKEN = "";
    private static String VOICE_TYPE = "BV002_streaming";
    static {
        try {
            java.util.Properties props = new java.util.Properties();
            props.load(VolcengineTtsUtil.class.getClassLoader().getResourceAsStream("asr.properties"));
            APP_ID = props.getProperty("volc.asr.appid", "");
            ACCESS_TOKEN = props.getProperty("volc.asr.token", "");
            VOICE_TYPE = props.getProperty("volc.tts.voice", "BV002_streaming");
        } catch (Exception e) { e.printStackTrace(); }
    }
    private static final String TTS_URL = "https://openspeech.bytedance.com/api/v1/tts";

    public static String textToSpeech(String text, String outputDir) {
        if (text == null || text.trim().isEmpty()) {
            System.out.println("[TTS] text empty");
            return null;
        }
        System.out.println("[TTS] tts: " + text);

        try {
            String reqId = java.util.UUID.randomUUID().toString();
            JSONObject body = new JSONObject();
            body.put("app", new JSONObject()
                    .put("appid", APP_ID)
                    .put("token", ACCESS_TOKEN)
                    .put("cluster", "volcano_tts"));
            body.put("user", new JSONObject().put("uid", "follow_read"));
            body.put("audio", new JSONObject()
                    .put("voice_type", VOICE_TYPE)
                    .put("encoding", "mp3")
                    .put("speed_ratio", 1.0));
            body.put("request", new JSONObject()
                    .put("reqid", reqId)
                    .put("text", text)
                    .put("text_type", "plain")
                    .put("operation", "query"));

            System.out.println("[TTS] req voice=" + VOICE_TYPE + " cluster=volcano_tts");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                    new java.net.URL(TTS_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer;" + ACCESS_TOKEN);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.getOutputStream().write(body.toString().getBytes("UTF-8"));

            int code = conn.getResponseCode();
            System.out.println("[TTS] HTTP " + code);

            if (code != 200) {
                java.io.InputStream es = conn.getErrorStream();
                if (es != null) {
                    java.util.Scanner s = new java.util.Scanner(es, "UTF-8").useDelimiter("\\A");
                    System.out.println("[TTS] err: " + (s.hasNext() ? s.next() : ""));
                }
                return null;
            }

            java.io.InputStream is = conn.getInputStream();
            java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
            String resp = s.hasNext() ? s.next() : "";
            is.close();

            JSONObject respJson = new JSONObject(resp);
            int respCode = respJson.optInt("code", -1);
            if (respCode != 3000) {
                System.out.println("[TTS] fail code=" + respCode + " msg=" + respJson.optString("message"));
                return null;
            }

            // Response field is "data" per Volcengine docs
            String base64Audio = respJson.optString("data", "");
            if (base64Audio.isEmpty()) {
                System.out.println("[TTS] no audio data");
                return null;
            }

            byte[] audioBytes = Base64.getDecoder().decode(base64Audio);
            String fileName = "tts_" + System.currentTimeMillis() + ".mp3";
            File outFile = new File(outputDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                fos.write(audioBytes);
            }
            System.out.println("[TTS] ok: " + outFile.getAbsolutePath() + " (" + audioBytes.length + " bytes)");
            return outFile.getAbsolutePath();
        } catch (Exception e) {
            System.out.println("[TTS] ex: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
