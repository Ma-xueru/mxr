package com.cl.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.cl.annotation.IgnoreAuth;
import com.cl.dao.FollowreadRecordDao;
import com.cl.dao.QuizRecordDao;
import com.cl.entity.CourseEntity;
import com.cl.entity.FollowreadRecordEntity;
import com.cl.entity.QuizRecordEntity;
import com.cl.service.CourseService;
import com.cl.utils.AIRecitationReviewUtil;
import com.cl.utils.PageUtils;
import com.cl.utils.R;
import com.cl.utils.VolcengineSpeechUtil;
import com.cl.utils.VolcengineTtsUtil;
import com.cl.utils.VolcengineTtsV3Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/followread")
public class FollowReadController {

    @Autowired
    private CourseService courseService;
    @Autowired
    private FollowreadRecordDao followreadRecordDao;
    @Autowired
    private QuizRecordDao quizRecordDao;

    /**
     * 获取古诗分行内容 + TTS音频
     */
    @IgnoreAuth
    @RequestMapping("/lines")
    public R lines(@RequestParam Long courseId) {
        CourseEntity course = courseService.selectById(courseId);
        if (course == null || !StringUtils.hasText(course.getContent())) {
            return R.error("古诗不存在");
        }

        String content = course.getContent();
        String[] rawLines = content.split("[\\n。？?！!，,；;]");
        List<Map<String, Object>> lineList = new ArrayList<>();

        int idx = 1;
        for (String line : rawLines) {
            String cleaned = line.replaceAll("[\\s\\p{Punct}\\u201c\\u201d\\u2018\\u2019\\uff0c\\u3002\\uff01\\uff1f\\uff1b\\uff1a\\u300a\\u300b\\u2026\\uff08\\uff09\\u3010\\u3011]", "").trim();
            if (cleaned.isEmpty()) continue;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("index", idx);
            item.put("text", line.trim());
            item.put("cleanText", cleaned);

            // 生成TTS音频
            String ttsPath = VolcengineTtsUtil.textToSpeech(line.trim(), getTtsDir());
            if (ttsPath == null) ttsPath = VolcengineTtsV3Util.textToSpeech(line.trim(), getTtsDir());
            item.put("ttsUrl", ttsPath != null ? "/file/" + new File(ttsPath).getName() : null);

            lineList.add(item);
            idx++;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("courseTitle", course.getCoursetitle());
        result.put("courseId", course.getId());
        result.put("totalLines", lineList.size());
        result.put("lines", lineList);

        return R.ok().put("data", result);
    }

    /**
     * 单句跟读评分
     */
    @RequestMapping("/score")
    public R score(@RequestParam("audio") MultipartFile audio,
                   @RequestParam("expectedText") String expectedText,
                   @RequestParam(value = "lineIndex", defaultValue = "1") int lineIndex) {
        if (audio == null || audio.isEmpty()) return R.error("请上传录音");
        if (!StringUtils.hasText(expectedText)) return R.error("缺少标准文本");

        try {
            // 1. 保存上传的音频
            String fileName = "follow_" + System.currentTimeMillis() + "_" + lineIndex + ".aac";
            File audioFile = new File(getAudioDir(), fileName);
            audioFile.getParentFile().mkdirs();
            audio.transferTo(audioFile);
            System.out.println("[跟读] 录音保存: " + audioFile.getAbsolutePath() + " (" + audioFile.length() + " bytes)");

            // 2. 语音识别
            String recognized = VolcengineSpeechUtil.speechToText(audioFile);
            System.out.println("[跟读] 识别结果: " + recognized);

            // 3. AI评分
            String cleanExpected = expectedText.replaceAll("[\\s\\p{Punct}]", "").trim();
            String cleanRecognized = recognized != null ? recognized.replaceAll("[\\s\\p{Punct}]", "").trim() : "";
            int score = 0;
            String comment = "";

            if (StringUtils.hasText(recognized)) {
                AIRecitationReviewUtil.ReviewResult aiResult =
                        AIRecitationReviewUtil.reviewFollowRead(expectedText, recognized, "跟读第" + lineIndex + "句");
                if (aiResult != null && aiResult.getTotalScore() > 0) {
                    score = aiResult.getTotalScore();
                    comment = aiResult.getOverallComment();
                }
            }

            if (score == 0) {
                // 简单匹配
                int dist = levenshtein(cleanExpected, cleanRecognized);
                double sim = 1 - (dist * 1.0 / Math.max(cleanExpected.length(), cleanRecognized.length()));
                score = Math.max(0, (int) Math.round(sim * 100));
                comment = "匹配度 " + (int)(sim*100) + "%";
            }

            // 4. 删除临时文件
            audioFile.delete();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("score", score);
            result.put("comment", comment);
            result.put("recognized", recognized);
            result.put("expected", expectedText);
            return R.ok().put("data", result);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("评分失败: " + e.getMessage());
        }
    }

    /**
     * 综合学习报告 - 复用AI背诵评分多维度评测
     */
    @RequestMapping("/report")
    public R report(@RequestBody Map<String, String> body) {
        String poemTitle = body.getOrDefault("poemTitle", "");
        String linesData = body.getOrDefault("linesData", "");
        String fullText = body.getOrDefault("fullText", "");
        if (!StringUtils.hasText(linesData)) return R.error("数据为空");
        try {
            System.out.println("[跟读报告] 生成中...");

            // 拼接所有识别文本作为完整内容
            StringBuilder recognizedAll = new StringBuilder();
            String[] lineArr = linesData.split("\n");
            for (String line : lineArr) {
                String[] parts = line.split("\\|");
                if (parts.length >= 2) recognizedAll.append(parts[1]);
            }

            // 调用同一套AI多维度评测
            com.cl.utils.AIRecitationReviewUtil.ReviewResult aiResult =
                    com.cl.utils.AIRecitationReviewUtil.reviewFollowRead(fullText, recognizedAll.toString(), poemTitle);

            Map<String, Object> result = new LinkedHashMap<>();
            if (aiResult != null && aiResult.getTotalScore() > 0) {
                result.put("overallScore", aiResult.getTotalScore());
                result.put("overallComment", aiResult.getOverallComment());
                java.util.List<Map<String, Object>> dims = new java.util.ArrayList<>();
                if (aiResult.getDimensions() != null) {
                    for (com.cl.utils.AIRecitationReviewUtil.DimensionScore d : aiResult.getDimensions()) {
                        Map<String, Object> dim = new LinkedHashMap<>();
                        dim.put("name", d.getName());
                        dim.put("score", d.getScore());
                        dim.put("weight", d.getWeight());
                        dim.put("comment", d.getComment());
                        dim.put("encourage", d.getEncourage());
                        dims.add(dim);
                    }
                }
                result.put("dimensions", dims);
            } else {
                result.put("overallScore", 0);
                result.put("overallComment", "AI评分未就绪，请重试");
                result.put("dimensions", new java.util.ArrayList<>());
            }
            System.out.println("[跟读报告] 完成");
            return R.ok().put("data", result);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("报告生成失败: " + e.getMessage());
        }
    }

    /**
     * 生成TTS音频（单句）
     */
    @IgnoreAuth
    @RequestMapping("/tts")
    public R tts(@RequestBody Map<String, Object> body) {
        String text = (String) body.get("text");
        if (!StringUtils.hasText(text)) return R.error("文本为空");
        String path = VolcengineTtsUtil.textToSpeech(text, getTtsDir());
        if (path == null) path = VolcengineTtsV3Util.textToSpeech(text, getTtsDir());
        if (path == null) return R.error("TTS合成失败");
        return R.ok().put("data", "/file/" + new File(path).getName());
    }

    private String getTtsDir() {
        try {
            File base = org.springframework.util.ResourceUtils.getFile("classpath:static");
            File ttsDir = new File(base, "file");
            ttsDir.mkdirs();
            return ttsDir.getAbsolutePath();
        } catch (Exception e) {
            return System.getProperty("java.io.tmpdir");
        }
    }

    private String getAudioDir() {
        File dir = new File("file");
        dir.mkdirs();
        return dir.getAbsolutePath();
    }

    /**
     * 保存跟读报告
     */
    @RequestMapping("/saveRecord")
    public R saveRecord(@RequestBody Map<String, Object> body) {
        FollowreadRecordEntity record = new FollowreadRecordEntity();
        record.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        record.setStudentaccount(String.valueOf(body.getOrDefault("studentaccount", "")));
        record.setStudentname(String.valueOf(body.getOrDefault("studentname", "")));
        record.setCourseid(Long.valueOf(String.valueOf(body.getOrDefault("courseid", "0"))));
        record.setCoursetitle(String.valueOf(body.getOrDefault("coursetitle", "")));
        record.setTotalscore(Integer.valueOf(String.valueOf(body.getOrDefault("totalscore", "0"))));
        record.setReportjson(String.valueOf(body.getOrDefault("reportjson", "")));
        record.setRecognizedtext(String.valueOf(body.getOrDefault("recognizedtext", "")));
        record.setAddtime(new Date());
        record.setRecordTimestamp(System.currentTimeMillis());
        followreadRecordDao.insert(record);
        return R.ok().put("data", record.getId());
    }

    /**
     * 查询跟读记录
     */
    @RequestMapping("/records")
    public R records(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        EntityWrapper<FollowreadRecordEntity> ew = new EntityWrapper<>();
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        if ("student".equals(tableName)) {
            ew.eq("studentaccount", String.valueOf(request.getSession().getAttribute("username")));
        }
        String studentaccount = String.valueOf(params.getOrDefault("studentaccount", ""));
        if (StringUtils.hasText(studentaccount)) ew.eq("studentaccount", studentaccount);
        String courseid = String.valueOf(params.getOrDefault("courseid", ""));
        if (StringUtils.hasText(courseid)) ew.eq("courseid", courseid);
        ew.orderBy("addtime", false);
        int page = Integer.parseInt(String.valueOf(params.getOrDefault("page", "1")));
        int limit = Integer.parseInt(String.valueOf(params.getOrDefault("limit", "10")));
        List<FollowreadRecordEntity> list = followreadRecordDao.selectPage(
                new com.baomidou.mybatisplus.plugins.Page<>(page, limit), ew);
        int total = followreadRecordDao.selectCount(ew);
        return R.ok().put("data", new PageUtils(list, total, limit, page));
    }

    /** 最新一次跟读成绩 */
    @RequestMapping("/records/latest")
    public R latest(@RequestParam Long courseid, HttpServletRequest request) {
        EntityWrapper<FollowreadRecordEntity> ew = new EntityWrapper<>();
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        if ("student".equals(tableName)) {
            ew.eq("studentaccount", String.valueOf(request.getSession().getAttribute("username")));
        }
        ew.eq("courseid", courseid);
        ew.orderBy("record_timestamp", false).last("LIMIT 1");
        List<FollowreadRecordEntity> list = followreadRecordDao.selectList(ew);
        return R.ok().put("data", list.isEmpty() ? null : list.get(0));
    }

    /** 最近10次成绩趋势（时间正序） */
    @RequestMapping("/records/trend")
    public R trend(@RequestParam Long courseid, HttpServletRequest request) {
        EntityWrapper<FollowreadRecordEntity> ew = new EntityWrapper<>();
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        if ("student".equals(tableName)) {
            ew.eq("studentaccount", String.valueOf(request.getSession().getAttribute("username")));
        }
        ew.eq("courseid", courseid);
        ew.orderBy("record_timestamp", true).last("LIMIT 10");
        List<FollowreadRecordEntity> list = followreadRecordDao.selectList(ew);
        List<Map<String, Object>> trend = new ArrayList<>();
        for (FollowreadRecordEntity r : list) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("score", r.getTotalscore());
            p.put("timestamp", r.getRecordTimestamp() != null ? r.getRecordTimestamp() : r.getAddtime().getTime());
            trend.add(p);
        }
        return R.ok().put("data", trend);
    }

    /** 全量学习记录聚合 — 跟读+测验 UNION 合并 */
    @RequestMapping("/all-learning-records")
    public R allLearningRecords(HttpServletRequest request) {
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        String username = String.valueOf(request.getSession().getAttribute("username"));

        List<Map<String, Object>> merged = new ArrayList<>();

        // 1. 跟读记录
        EntityWrapper<FollowreadRecordEntity> fw = new EntityWrapper<>();
        if ("student".equals(tableName)) fw.eq("studentaccount", username);
        fw.orderBy("record_timestamp", false);
        List<FollowreadRecordEntity> followList = followreadRecordDao.selectList(fw);
        for (FollowreadRecordEntity r : followList) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", "follow");
            m.put("score", r.getTotalscore());
            m.put("timestamp", r.getRecordTimestamp() != null ? r.getRecordTimestamp() :
                    (r.getAddtime() != null ? r.getAddtime().getTime() : 0L));
            m.put("courseTitle", r.getCoursetitle());
            m.put("courseId", r.getCourseid());
            m.put("detail", r.getReportjson());
            merged.add(m);
        }

        // 2. 测验记录
        EntityWrapper<QuizRecordEntity> qw = new EntityWrapper<>();
        if ("student".equals(tableName)) qw.eq("studentaccount", username);
        qw.orderBy("addtime", false);
        List<QuizRecordEntity> quizList = quizRecordDao.selectList(qw);
        for (QuizRecordEntity r : quizList) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", "quiz");
            m.put("score", r.getScore());
            m.put("timestamp", r.getAddtime() != null ? r.getAddtime().getTime() : 0L);
            m.put("courseTitle", r.getCoursetitle());
            m.put("courseId", r.getCourseid());
            m.put("detail", r.getWrongListJson());
            m.put("duration", r.getDuration());
            m.put("correctCount", r.getCorrectCount());
            m.put("questionsCount", r.getQuestionsCount());
            merged.add(m);
        }

        // 按 timestamp 倒序排列
        merged.sort((a, b) -> Long.compare(
                (Long) b.getOrDefault("timestamp", 0L),
                (Long) a.getOrDefault("timestamp", 0L)));

        // 正序副本（给前端画图用）
        List<Map<String, Object>> asc = new ArrayList<>(merged);
        java.util.Collections.reverse(asc);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("desc", merged);  // 倒序 — 列表展示
        result.put("asc", asc);      // 正序 — 折线图
        return R.ok().put("data", result);
    }

    @RequestMapping("/deleteRecord")
    public R deleteRecord(@RequestBody Long[] ids) {
        followreadRecordDao.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }

    private int levenshtein(String a, String b) {
        if (a == null) a = ""; if (b == null) b = "";
        int[][] dp = new int[a.length()+1][b.length()+1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++)
            for (int j = 1; j <= b.length(); j++)
                dp[i][j] = Math.min(dp[i-1][j]+1, Math.min(dp[i][j-1]+1, dp[i-1][j-1] + (a.charAt(i-1)==b.charAt(j-1)?0:1)));
        return dp[a.length()][b.length()];
    }
}
