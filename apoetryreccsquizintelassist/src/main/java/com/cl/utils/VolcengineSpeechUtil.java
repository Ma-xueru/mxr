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

    private static final String[] UNSUPPORTED_FORMATS = {"aac", "m4a", "ogg", "wma", "flac"};

    public static String speechToText(File audioFile) {
        if (audioFile == null) {
            System.out.println("[AI评测] 语音识别：audioFile为null");
            return "";
        }
        if (!audioFile.exists()) {
            System.out.println("[AI评测] 语音识别：文件不存在 - " + audioFile.getAbsolutePath());
            return "";
        }

        File fileToProcess = audioFile;
        String originalFormat = getFormat(audioFile.getName());
        if (isUnsupportedFormat(originalFormat)) {
            System.out.println("[AI评测] 语音识别：格式 " + originalFormat + " 不受ASR支持，用FFmpeg转WAV...");
            fileToProcess = convertToWav(audioFile);
            if (fileToProcess == null) {
                System.out.println("[AI评测] 语音识别：FFmpeg转换失败，尝试直接发送原始文件");
                fileToProcess = audioFile;
            }
        }

        System.out.println("[AI评测] 语音识别：开始处理 - " + fileToProcess.getAbsolutePath() + " (" + fileToProcess.length() + " bytes)");
        System.out.println("[AI评测] 语音识别：配置 appid=" + (APP_ID.isEmpty() ? "(空)" : APP_ID) + " cluster=" + CLUSTER);

        try {
            byte[] audioBytes = readFile(fileToProcess);
            String base64Audio = Base64.getEncoder().encodeToString(audioBytes);
            String format = getFormat(fileToProcess.getName());
            String reqId = java.util.UUID.randomUUID().toString();
            System.out.println("[AI评测] 语音识别：格式=" + format + " base64长度=" + base64Audio.length() + " reqId=" + reqId);

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

            System.out.println("[AI评测] 语音识别：发送请求到 " + ASR_URL);
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
            System.out.println("[AI评测] 语音识别：HTTP响应码 " + code);

            if (code != 200) {
                java.io.InputStream es = conn.getErrorStream();
                if (es != null) {
                    java.util.Scanner s = new java.util.Scanner(es, "UTF-8").useDelimiter("\\A");
                    String errBody = s.hasNext() ? s.next() : "";
                    System.out.println("[AI评测] 语音识别：错误响应 - " + errBody);
                } else {
                    System.out.println("[AI评测] 语音识别：无错误响应体");
                }
                return "";
            }

            java.io.InputStream is = conn.getInputStream();
            java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
            String resp = s.hasNext() ? s.next() : "";
            is.close();
            System.out.println("[AI评测] 语音识别：响应 - " + (resp.length() > 300 ? resp.substring(0, 300) + "..." : resp));

            JSONObject respJson = new JSONObject(resp);
            // result 可能是单个对象，也可能是数组
            JSONObject resultObj = respJson.optJSONObject("result");
            if (resultObj != null) {
                String text = resultObj.optString("text", "");
                if (!text.isEmpty()) {
                    System.out.println("[AI评测] 语音识别：识别成功(单对象)，文本为 - " + text);
                    return text.trim();
                }
            }
            org.json.JSONArray resultArr = respJson.optJSONArray("result");
            if (resultArr != null && resultArr.length() > 0) {
                String text = resultArr.optJSONObject(0).optString("text", "");
                if (!text.isEmpty()) {
                    System.out.println("[AI评测] 语音识别：识别成功(数组)，文本为 - " + text);
                    return text.trim();
                }
            }
            String text = respJson.optString("text", "");
            System.out.println("[AI评测] 语音识别：兜底解析text - " + (text.isEmpty() ? "(空)" : text));
            return text.trim();
        } catch (Exception e) {
            System.out.println("[AI评测] 语音识别：异常 - " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            return "";
        } finally {
            if (fileToProcess != audioFile && fileToProcess != null) {
                fileToProcess.delete();
                System.out.println("[AI评测] 语音识别：清理临时WAV文件 - " + fileToProcess.getName());
            }
        }
    }

    private static boolean isUnsupportedFormat(String format) {
        for (String uf : UNSUPPORTED_FORMATS) {
            if (uf.equalsIgnoreCase(format)) return true;
        }
        return false;
    }

    private static File convertToWav(File audioFile) {
        File wavFile = new File(audioFile.getParent(), audioFile.getName().replaceFirst("\\.[^.]+$", "") + "_conv.wav");
        System.out.println("[AI评测] FFmpeg：转换 " + audioFile.getName() + " -> " + wavFile.getName());

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-y",
                    "-i", audioFile.getAbsolutePath(),
                    "-ar", "16000",
                    "-ac", "1",
                    "-sample_fmt", "s16",
                    wavFile.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();

            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getInputStream()));
            StringBuilder ffmpegLog = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                ffmpegLog.append(line).append("\n");
            }
            int exitCode = p.waitFor();
            if (exitCode != 0) {
                System.out.println("[AI评测] FFmpeg：转换失败 exitCode=" + exitCode);
                System.out.println("[AI评测] FFmpeg：日志 - " + ffmpegLog.toString());
                return null;
            }

            if (!wavFile.exists() || wavFile.length() == 0) {
                System.out.println("[AI评测] FFmpeg：转换后文件不存在或为空 - " + wavFile.getAbsolutePath());
                return null;
            }

            System.out.println("[AI评测] FFmpeg：转换成功 - " + wavFile.length() + " bytes");
            return wavFile;
        } catch (Exception e) {
            System.out.println("[AI评测] FFmpeg：异常 - " + e.getMessage());
            e.printStackTrace();
            return null;
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
