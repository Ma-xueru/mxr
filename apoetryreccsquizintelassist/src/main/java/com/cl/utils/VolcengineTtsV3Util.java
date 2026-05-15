package com.cl.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import org.json.JSONObject;

/**
 * TTS 文字转语音 — 火山引擎语音合成 V3 (HTTP SSE 单向流式)
 * 支持 1.0/2.0 音色，通过 X-Api-Resource-Id 切换
 */
public class VolcengineTtsV3Util {

    private static String APP_ID = "";
    private static String ACCESS_TOKEN = "";
    private static String API_KEY = "";
    private static String RESOURCE_ID = "seed-tts-1.0";
    private static String VOICE_TYPE = "zh_female_peiqi_mars_bigtts";
    private static String AUTH_MODE = "new";

    static {
        try {
            java.util.Properties props = new java.util.Properties();
            props.load(VolcengineTtsV3Util.class.getClassLoader().getResourceAsStream("asr.properties"));
            APP_ID = props.getProperty("volc.asr.appid", "");
            ACCESS_TOKEN = props.getProperty("volc.asr.token", "");
            API_KEY = props.getProperty("volc.tts.apikey", "");
            RESOURCE_ID = props.getProperty("volc.tts.resource.id", "seed-tts-2.0");
            VOICE_TYPE = props.getProperty("volc.tts.v3.voice", "zh_female_cancan_mars_bigtts");
            AUTH_MODE = props.getProperty("volc.tts.auth.mode", "new");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static final String TTS_URL = "https://openspeech.bytedance.com/api/v3/tts/unidirectional";

    /**
     * @param text      合成文本
     * @param outputDir 输出目录
     * @return 音频文件绝对路径，失败返回 null
     */
    public static String textToSpeech(String text, String outputDir) {
        return textToSpeech(text, outputDir, VOICE_TYPE);
    }

    /**
     * @param text      合成文本
     * @param outputDir 输出目录
     * @param voiceType 音色（如 zh_female_cancan_mars_bigtts）
     */
    public static String textToSpeech(String text, String outputDir, String voiceType) {
        if (text == null || text.trim().isEmpty()) {
            System.out.println("[TTS-V3] text empty");
            return null;
        }
        System.out.println("[TTS-V3] voice=" + voiceType + " resource=" + RESOURCE_ID + " text=" + text.substring(0, Math.min(50, text.length())));

        try {
            JSONObject body = new JSONObject();
            body.put("user", new JSONObject().put("uid", "poem_app"));
            body.put("req_params", new JSONObject()
                    .put("text", text)
                    .put("speaker", voiceType)
                    .put("audio_params", new JSONObject()
                            .put("format", "mp3")
                            .put("sample_rate", 24000)));

            System.out.println("[TTS-V3] req: " + body.toString());

            HttpURLConnection conn = (HttpURLConnection) new URL(TTS_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            // 鉴权：优先用新版 API Key，否则用旧版 appid+token
            if (API_KEY != null && !API_KEY.isEmpty() && "new".equals(AUTH_MODE)) {
                conn.setRequestProperty("X-Api-Key", API_KEY);
            } else {
                conn.setRequestProperty("X-Api-App-Id", APP_ID);
                conn.setRequestProperty("X-Api-Access-Key", ACCESS_TOKEN);
            }
            conn.setRequestProperty("X-Api-Resource-Id", RESOURCE_ID);

            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            conn.setDoOutput(true);
            conn.getOutputStream().write(body.toString().getBytes("UTF-8"));

            int code = conn.getResponseCode();
            System.out.println("[TTS-V3] HTTP " + code);

            if (code != 200) {
                InputStream es = conn.getErrorStream();
                if (es != null) {
                    java.util.Scanner s = new java.util.Scanner(es, "UTF-8").useDelimiter("\\A");
                    System.out.println("[TTS-V3] err: " + (s.hasNext() ? s.next() : ""));
                }
                return null;
            }

            // HTTP Chunked 响应：JSON行（每行一个JSON，data字段是base64音频）
            InputStream is = conn.getInputStream();
            String fileName = "tts_" + System.currentTimeMillis() + ".mp3";
            File outFile = new File(outputDir, fileName);
            java.io.ByteArrayOutputStream audioBos = new java.io.ByteArrayOutputStream();

            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    org.json.JSONObject chunk = new org.json.JSONObject(line);
                    String b64 = chunk.optString("data", null);
                    if (b64 != null && !b64.isEmpty()) {
                        audioBos.write(Base64.getDecoder().decode(b64));
                    }
                } catch (Exception je) { /* skip */ }
            }
            reader.close();

            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                fos.write(audioBos.toByteArray());
            }

            long len = outFile.length();
            if (len < 100) {
                // Read the content to debug
                byte[] content = java.nio.file.Files.readAllBytes(outFile.toPath());
                System.out.println("[TTS-V3] audio too small: " + len + " bytes, content=" + new String(content, "UTF-8"));
                outFile.delete();
                return null;
            }
            System.out.println("[TTS-V3] ok: " + outFile.getAbsolutePath() + " (" + len + " bytes)");
            return outFile.getAbsolutePath();
        } catch (Exception e) {
            System.out.println("[TTS-V3] ex: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /** 切换音色（运行时动态切换） */
    public static void setVoiceType(String voice) {
        VOICE_TYPE = voice;
        System.out.println("[TTS-V3] voice switched to: " + voice);
    }

    /** 切换资源ID（1.0/2.0） */
    public static void setResourceId(String resourceId) {
        RESOURCE_ID = resourceId;
        System.out.println("[TTS-V3] resource switched to: " + resourceId);
    }

    public static String getVoiceType() { return VOICE_TYPE; }
    public static String getResourceId() { return RESOURCE_ID; }
}
