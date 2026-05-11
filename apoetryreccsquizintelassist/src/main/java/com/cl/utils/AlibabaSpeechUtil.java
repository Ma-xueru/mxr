package com.cl.utils;

import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 阿里云一句话识别 - 使用 HMAC-SHA1 签名
 */
public class AlibabaSpeechUtil {

    private static String AK_ID = "";
    private static String AK_SECRET = "";
    private static String APP_KEY = "";
    static {
        try {
            java.util.Properties props = new java.util.Properties();
            props.load(AlibabaSpeechUtil.class.getClassLoader().getResourceAsStream("asr.properties"));
            AK_ID = props.getProperty("aliyun.ak.id", "");
            AK_SECRET = props.getProperty("aliyun.ak.secret", "");
            APP_KEY = props.getProperty("aliyun.app.key", "");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static String speechToText(File audioFile) {
        if (audioFile == null || !audioFile.exists()) return "";
        System.out.println("[AliSpeech] Processing: " + audioFile.length() + " bytes");

        try {
            byte[] audioBytes = readFile(audioFile);
            String base64Audio = Base64.getEncoder().encodeToString(audioBytes);
            String format = getFormat(audioFile.getName());

            JSONObject body = new JSONObject();
            body.put("appkey", APP_KEY);
            body.put("format", format);
            body.put("sample_rate", 16000);
            body.put("enable_punctuation_prediction", true);
            body.put("enable_inverse_text_normalization", true);
            body.put("audio_content", base64Audio);

            // Build RPC request with signature
            Map<String, String> params = new TreeMap<>();
            params.put("Action", "CreateTask");
            params.put("Format", "JSON");
            params.put("Version", "2019-08-19");
            params.put("AccessKeyId", AK_ID);
            params.put("SignatureMethod", "HMAC-SHA1");
            params.put("Timestamp", getTimestamp());
            params.put("SignatureVersion", "1.0");
            params.put("SignatureNonce", UUID.randomUUID().toString());
            params.put("TaskName", "recitation_" + System.currentTimeMillis());
            params.put("Appkey", APP_KEY);
            params.put("AudioContent", base64Audio);
            params.put("AudioFormat", format);
            params.put("SampleRate", "16000");
            params.put("EnablePunctuationPrediction", "true");
            params.put("EnableInverseTextNormalization", "true");

            String signature = sign(params, "POST");
            params.put("Signature", signature);

            StringBuilder urlBuilder = new StringBuilder("https://nls-slp.cn-shanghai.aliyuncs.com/?");
            for (Map.Entry<String, String> e : params.entrySet()) {
                urlBuilder.append(encode(e.getKey())).append("=").append(encode(e.getValue())).append("&");
            }
            String url = urlBuilder.substring(0, urlBuilder.length() - 1);

            java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                    new java.net.URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);

            int code = conn.getResponseCode();
            System.out.println("[AliSpeech] HTTP " + code);
            java.io.InputStream is = code == 200 ? conn.getInputStream() : conn.getErrorStream();
            java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
            String resp = s.hasNext() ? s.next() : "";
            is.close();
            System.out.println("[AliSpeech] Resp: " + resp.substring(0, Math.min(300, resp.length())));

            JSONObject respJson = new JSONObject(resp);
            String text = respJson.optString("Result", "");
            if (text.isEmpty()) text = respJson.optString("result", "");
            if (!text.isEmpty()) {
                System.out.println("[AliSpeech] Success: " + text);
                return text.trim();
            }
            // Check for async task ID
            String taskId = respJson.optString("TaskId", "");
            if (!taskId.isEmpty()) {
                System.out.println("[AliSpeech] Async task: " + taskId);
            }
            return "";
        } catch (Exception e) {
            System.out.println("[AliSpeech] Exception: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    private static String sign(Map<String, String> params, String method) throws Exception {
        // Build canonical query string
        StringBuilder canonical = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            canonical.append(encode(e.getKey())).append("=").append(encode(e.getValue())).append("&");
        }
        String queryString = canonical.substring(0, canonical.length() - 1);

        String stringToSign = method + "&" + encode("/") + "&" + encode(queryString);
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec((AK_SECRET + "&").getBytes("UTF-8"), "HmacSHA1"));
        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(signData);
    }

    private static String encode(String value) throws Exception {
        return URLEncoder.encode(value, "UTF-8")
                .replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
    }

    private static String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    private static String getFormat(String fileName) {
        int dot = fileName.lastIndexOf(".");
        return (dot == -1 || dot == fileName.length() - 1) ? "wav" : fileName.substring(dot + 1).toLowerCase();
    }

    private static byte[] readFile(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            return data;
        }
    }
}
