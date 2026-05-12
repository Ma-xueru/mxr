package com.cl.controller;

import com.cl.utils.AIRecitationReviewUtil;
import com.cl.utils.R;
import com.cl.utils.VolcengineSpeechUtil;
import com.cl.utils.VolcengineTtsUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/voice")
public class VoiceChatController {

    // 对话历史（按用户session存储，保留最近10轮）
    private static final ConcurrentHashMap<String, List<com.volcengine.ark.runtime.model.completion.chat.ChatMessage>> historyMap = new ConcurrentHashMap<>();

    private static final String SYSTEM_PROMPT = "你是一个幽默的百科小老师。回答要生动有趣，可以加入一些有趣的科学事实或历史小故事。逻辑要清晰，可以用'第一、第二'来拆解。如果孩子问古诗，请尝试把诗句描绘成一幅画讲给他们听。字数100-150字。禁止负面暴力成人内容。";

    @RequestMapping("/chat")
    public R chat(@RequestParam("audio") MultipartFile audio, HttpServletRequest request) {
        if (audio == null || audio.isEmpty()) return R.error("请说话");
        try {
            // 1. 保存音频
            String fn = "voice_" + System.currentTimeMillis() + ".aac";
            File af = new File(getAudioDir(), fn);
            af.getParentFile().mkdirs();
            audio.transferTo(af);
            System.out.println("[语音助手] 收到录音: " + af.length() + " bytes");

            // 2. 语音识别
            String recognized = VolcengineSpeechUtil.speechToText(af);
            System.out.println("[语音助手] 识别: " + recognized);

            if (!StringUtils.hasText(recognized)) {
                return R.ok().put("data", mapOf("reply", "我没听清，再说一遍好吗？"));
            }

            // 3. 豆包对话（带历史记忆）
            String reply = chatWithDoubao(recognized, request);
            System.out.println("[语音助手] 回复: " + reply);

            // 4. TTS
            String ttsFile = VolcengineTtsUtil.textToSpeech(reply, getTtsDir());
            String ttsUrl = ttsFile != null ? "/file/" + new File(ttsFile).getName() : null;

            // 5. 清理
            af.delete();

            Map<String, Object> result = mapOf("reply", reply, "recognized", recognized);
            if (ttsUrl != null) result.put("ttsUrl", ttsUrl);
            return R.ok().put("data", result);

        } catch (Exception e) {
            e.printStackTrace();
            return R.error("出错了: " + e.getMessage());
        }
    }

    private String chatWithDoubao(String userText, HttpServletRequest request) {
        try {
            String uid = "voice_" + (StringUtils.hasText(request.getSession().getAttribute("username") + "")
                ? request.getSession().getAttribute("username") : request.getSession().getId());

            // 获取或创建历史
            List<com.volcengine.ark.runtime.model.completion.chat.ChatMessage> msgs = historyMap.get(uid);
            if (msgs == null) {
                msgs = new ArrayList<>();
                msgs.add(com.volcengine.ark.runtime.model.completion.chat.ChatMessage.builder()
                    .role(com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole.SYSTEM)
                    .content(SYSTEM_PROMPT).build());
                historyMap.put(uid, msgs);
            }

            // 添加用户消息
            msgs.add(com.volcengine.ark.runtime.model.completion.chat.ChatMessage.builder()
                .role(com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole.USER)
                .content(userText).build());

            // 保留最近10轮（20条=10问+10答）+1条system
            while (msgs.size() > 21) msgs.remove(1); // 保留system prompt

            com.volcengine.ark.runtime.service.ArkService service =
                new com.volcengine.ark.runtime.service.ArkService(AIRecitationReviewUtil.getApiKey());

            com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest req =
                com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest.builder()
                .model(AIRecitationReviewUtil.getModel()).messages(new ArrayList<>(msgs)).temperature(0.7)
                .maxTokens(300).build();

            StringBuilder sb = new StringBuilder();
            service.createChatCompletion(req).getChoices().forEach(c -> sb.append(c.getMessage().getContent()));
            String reply = sb.toString().trim();
            service.shutdownExecutor();

            // 保存AI回复到历史
            msgs.add(com.volcengine.ark.runtime.model.completion.chat.ChatMessage.builder()
                .role(com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole.ASSISTANT)
                .content(reply).build());

            return reply;
        } catch (Exception e) {
            e.printStackTrace();
            return "哎呀，我的小脑袋卡住了～再问我一次吧！";
        }
    }

    private Map<String, Object> mapOf(String k1, Object v1) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(k1, v1);
        return m;
    }

    private Map<String, Object> mapOf(String k1, Object v1, String k2, Object v2) {
        Map<String, Object> m = mapOf(k1, v1);
        m.put(k2, v2);
        return m;
    }

    private Map<String, Object> mapOf(String k1, Object v1, String k2, Object v2, String k3, Object v3) {
        Map<String, Object> m = mapOf(k1, v1, k2, v2);
        m.put(k3, v3);
        return m;
    }

    private String getAudioDir() { File d = new File("file"); d.mkdirs(); return d.getAbsolutePath(); }
    private String getTtsDir() {
        try { return new File(org.springframework.util.ResourceUtils.getFile("classpath:static"), "file").getAbsolutePath(); }
        catch (Exception e) { return System.getProperty("java.io.tmpdir"); }
    }
}
