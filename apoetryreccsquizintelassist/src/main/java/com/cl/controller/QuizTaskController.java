package com.cl.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.cl.annotation.IgnoreAuth;
import com.cl.dao.*;
import com.cl.entity.*;
import com.cl.service.CourseService;
import com.cl.utils.AIChatUtil;
import com.cl.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/quiztask")
public class QuizTaskController {

    @Autowired private RecitationtaskDao recitationtaskDao;
    @Autowired private QuizQuestionDao quizQuestionDao;
    @Autowired private StudentQuizRecordDao studentQuizRecordDao;
    @Autowired private CourseService courseService;

    /** 测验任务列表（仅task_type=2） */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest req) {
        EntityWrapper<RecitationtaskEntity> ew = new EntityWrapper<>();
        ew.like("tasktitle", "测验：");
        String tasktitle = String.valueOf(params.getOrDefault("tasktitle", ""));
        if (StringUtils.hasText(tasktitle) && !"null".equals(tasktitle)) ew.like("tasktitle", tasktitle);
        String studentaccount = String.valueOf(params.getOrDefault("studentaccount", ""));
        if (StringUtils.hasText(studentaccount) && !"null".equals(studentaccount)) ew.eq("studentaccount", studentaccount);
        ew.orderBy("id", false);
        int page = Integer.parseInt(String.valueOf(params.getOrDefault("page", "1")));
        int limit = Integer.parseInt(String.valueOf(params.getOrDefault("limit", "10")));
        List<RecitationtaskEntity> list = recitationtaskDao.selectPage(
                new com.baomidou.mybatisplus.plugins.Page<>(page, limit), ew);
        int total = recitationtaskDao.selectCount(ew);
        return R.ok().put("data", new com.cl.utils.PageUtils(list, total, limit, page));
    }

    /** save/update/delete 委托给 recitationtask */
    @RequestMapping("/save") public R save(@RequestBody RecitationtaskEntity e) {
        e.setTaskType(2); e.setId(System.currentTimeMillis()); e.setReleasetime(new Date());
        if (e.getTasktitle() != null && !e.getTasktitle().startsWith("测验：")) e.setTasktitle("测验：" + e.getTasktitle());
        recitationtaskDao.insert(e);

        // 触发AI出题
        String courseIds = e.getCourseids();
        String courseTitles = e.getCoursetitles();
        if (StringUtils.hasText(courseIds) && StringUtils.hasText(courseTitles)) {
            try {
                Long cid = Long.valueOf(courseIds.split(",")[0].trim());
                CourseEntity course = courseService.selectById(cid);
                if (course != null && StringUtils.hasText(course.getContent())) {
                    generateQuestions(e.getId(), cid, course.getCoursetitle(), course.getContent());
                }
            } catch (Exception ex) { System.out.println("[测验] AI出题失败: " + ex.getMessage()); }
        }
        return R.ok();
    }

    private void generateQuestions(Long taskId, Long courseId, String title, String content) {
        String prompt = "针对古诗《" + title + "》出5道单选题。原文：" + content +
            "\n涵盖：字词释义、意境理解、作者情感、格律常识、文学常识。返回JSON：[{\"question\":\"题?\",\"options\":[\"A\",\"B\",\"C\",\"D\"],\"answer\":0,\"analysis\":\"解析\"}]。只返回JSON。";
        List<AIChatUtil.Message> msgs = new ArrayList<>();
        msgs.add(new AIChatUtil.Message("system", "你是特级语文老师，严格按JSON返回5道单选题。"));
        msgs.add(new AIChatUtil.Message("user", prompt));
        AIChatUtil.ChatResult cr = AIChatUtil.chatWithMessages(msgs, 0.5, 2000);
        String resp = cr != null ? cr.getContent() : null;
        if (resp == null || resp.isEmpty()) return;
        String json = resp.trim();
        int s = json.indexOf('['), e = json.lastIndexOf(']');
        if (s >= 0 && e > s) json = json.substring(s, e + 1);
        try {
            org.json.JSONArray arr = new org.json.JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                org.json.JSONObject q = arr.getJSONObject(i);
                QuizQuestionEntity qe = new QuizQuestionEntity();
                qe.setTaskId(taskId); qe.setCourseId(courseId);
                qe.setQuestion(q.optString("question"));
                qe.setOptionsJson(q.optJSONArray("options").toString());
                qe.setAnswer(q.optInt("answer"));
                qe.setAnalysis(q.optString("analysis"));
                qe.setSortOrder(i + 1);
                quizQuestionDao.insert(qe);
            }
            System.out.println("[测验] AI出题成功 taskId=" + taskId + " 题数=" + arr.length());
        } catch (Exception ex) { System.out.println("[测验] AI出题解析失败: " + ex.getMessage()); }
    }
    @RequestMapping("/update") public R update(@RequestBody RecitationtaskEntity e) { recitationtaskDao.updateById(e); return R.ok(); }
    @RequestMapping("/delete") public R delete(@RequestBody Long[] ids) { recitationtaskDao.deleteBatchIds(java.util.Arrays.asList(ids)); return R.ok(); }

    /** 获取已完成的测验结果（雷达图+AI报告） */
    @RequestMapping("/result")
    public R result(@RequestParam Long taskId, HttpServletRequest req) {
        String username = String.valueOf(req.getSession().getAttribute("username"));
        EntityWrapper<StudentQuizRecordEntity> ew = new EntityWrapper<>();
        ew.eq("task_id", taskId).eq("studentaccount", username).orderBy("addtime", false).last("LIMIT 1");
        List<StudentQuizRecordEntity> list = studentQuizRecordDao.selectList(ew);
        if (list.isEmpty()) return R.error("暂无结果");
        StudentQuizRecordEntity r = list.get(0);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("score", r.getScore()); m.put("correctCount", r.getCorrectCount());
        m.put("totalQuestions", r.getTotalQuestions()); m.put("aiReport", r.getAiReport());
        return R.ok().put("data", m);
    }

    /** 教师发布AI测验 — AI预生成5道题 */
    @RequestMapping("/publish")
    public R publish(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        Long courseId = Long.valueOf(String.valueOf(body.getOrDefault("courseid", "0")));
        String courseTitle = String.valueOf(body.getOrDefault("coursetitle", ""));
        String courseContent = String.valueOf(body.getOrDefault("content", ""));
        String studentIds = String.valueOf(body.getOrDefault("studentids", "")); // 逗号分隔

        if (courseId == 0 || !StringUtils.hasText(courseContent)) return R.error("古诗信息不完整");

        // 1. AI 出5道题
        String prompt = "针对古诗《" + courseTitle + "》出5道单选题。原文：" + courseContent +
            "\n涵盖：字词释义(1题)、意境理解(1题)、作者情感(1题)、格律常识(1题)、文学常识(1题)。" +
            "\n返回JSON：[{\"question\":\"题?\",\"options\":[\"A\",\"B\",\"C\",\"D\"],\"answer\":0,\"analysis\":\"解析\"}]。只返回JSON。";
        List<AIChatUtil.Message> msgs = new ArrayList<>();
        msgs.add(new AIChatUtil.Message("system", "你是特级语文老师，严格按JSON返回5道单选题。"));
        msgs.add(new AIChatUtil.Message("user", prompt));
        AIChatUtil.ChatResult cr = AIChatUtil.chatWithMessages(msgs, 0.5, 2000);
        String resp = cr != null ? cr.getContent() : null;
        if (resp == null || resp.isEmpty()) return R.error("AI出题失败");

        String json = cleanJson(resp);
        System.out.println("[测验发布] AI题目(" + json.length() + ")");

        // 2. 创建任务记录
        RecitationtaskEntity task = new RecitationtaskEntity();
        Long taskId = System.currentTimeMillis();
        task.setId(taskId); task.setCourseids(String.valueOf(courseId)); task.setCoursetitles(courseTitle);
        task.setTasktitle("测验：" + courseTitle);
        task.setStudentaccount(studentIds); task.setReleasetime(new Date());
        task.setTaskType(2); // 2=测验
        task.setCompletionstatus("1"); // 1=待完成
        recitationtaskDao.insert(task);

        // 3. 保存题目
        try {
            org.json.JSONArray arr = new org.json.JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                org.json.JSONObject q = arr.getJSONObject(i);
                QuizQuestionEntity qe = new QuizQuestionEntity();
                qe.setTaskId(taskId); qe.setCourseId(courseId);
                qe.setQuestion(q.optString("question"));
                qe.setOptionsJson(q.optJSONArray("options").toString());
                qe.setAnswer(q.optInt("answer"));
                qe.setAnalysis(q.optString("analysis"));
                qe.setSortOrder(i + 1);
                quizQuestionDao.insert(qe);
            }
        } catch (Exception e) { return R.error("题目解析失败: " + e.getMessage()); }

        return R.ok().put("data", taskId);
    }

    /** 学生端 — 获取待完成的测验 */
    @RequestMapping("/student-pending")
    public R studentPending(HttpServletRequest req) {
        String username = String.valueOf(req.getSession().getAttribute("username"));
        EntityWrapper<RecitationtaskEntity> ew = new EntityWrapper<>();
        ew.like("tasktitle", "测验：").like("studentaccount", username);
        List<RecitationtaskEntity> list = recitationtaskDao.selectList(ew);

        List<Map<String, Object>> result = new ArrayList<>();
        for (RecitationtaskEntity t : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            String cids = t.getCourseids();
            Long cid = 0L;
            if (StringUtils.hasText(cids)) { try { cid = Long.valueOf(cids.split(",")[0].trim()); } catch(Exception ex) {} }
            m.put("taskId", t.getId()); m.put("courseTitle", t.getCoursetitles());
            m.put("courseId", cid); m.put("taskTitle", t.getTasktitle());
            m.put("status", "已完成".equals(t.getCompletionstatus()) ? "completed" : "pending");
            // 查最新得分
            EntityWrapper<StudentQuizRecordEntity> srw = new EntityWrapper<>();
            srw.eq("task_id", t.getId()).eq("studentaccount", username).orderBy("addtime", false).last("LIMIT 1");
            List<StudentQuizRecordEntity> srs = studentQuizRecordDao.selectList(srw);
            m.put("latestScore", srs.isEmpty() ? null : srs.get(0).getScore());
            // 查询题目
            EntityWrapper<QuizQuestionEntity> qw = new EntityWrapper<>();
            qw.eq("task_id", t.getId()).orderBy("sort_order");
            List<QuizQuestionEntity> qs = quizQuestionDao.selectList(qw);
            List<Map<String, Object>> questions = new ArrayList<>();
            for (QuizQuestionEntity q : qs) {
                Map<String, Object> qm = new LinkedHashMap<>();
                qm.put("id", q.getId()); qm.put("question", q.getQuestion());
                try { qm.put("options", new org.json.JSONArray(q.getOptionsJson()).toList()); } catch (Exception e) {}
                qm.put("answer", q.getAnswer()); qm.put("analysis", q.getAnalysis());
                questions.add(qm);
            }
            m.put("questions", questions);
            result.add(m);
        }
        return R.ok().put("data", result);
    }

    /** 学生提交答案 + AI评分 */
    @SuppressWarnings("unchecked")
    @RequestMapping("/submit")
    public R submit(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        Long taskId = Long.valueOf(String.valueOf(body.getOrDefault("taskId", "0")));
        String studentaccount = String.valueOf(body.getOrDefault("studentaccount", ""));
        String studentname = String.valueOf(body.getOrDefault("studentname", ""));
        Long courseId = 0L;
        try { courseId = Long.valueOf(String.valueOf(body.getOrDefault("courseId", "0"))); } catch(Exception e) {}
        String courseTitle = String.valueOf(body.getOrDefault("courseTitle", ""));
        List<Map<String, Object>> answers = (List<Map<String, Object>>) body.getOrDefault("answers", new ArrayList<>());

        if (taskId == 0) return R.error("缺少任务ID");

        // 加载题目及答案
        EntityWrapper<QuizQuestionEntity> qw = new EntityWrapper<>();
        qw.eq("task_id", taskId).orderBy("sort_order");
        List<QuizQuestionEntity> questions = quizQuestionDao.selectList(qw);
        Map<Long, QuizQuestionEntity> qMap = new LinkedHashMap<>();
        for (QuizQuestionEntity q : questions) qMap.put(q.getId(), q);

        int correct = 0, total = questions.size();
        List<Map<String, Object>> wrongList = new ArrayList<>();
        StringBuilder wrongCtx = new StringBuilder();

        for (Map<String, Object> ans : answers) {
            Long qid = Long.valueOf(String.valueOf(ans.getOrDefault("qId", "0")));
            int selected = Integer.parseInt(String.valueOf(ans.getOrDefault("selected", "-1")));
            QuizQuestionEntity q = qMap.get(qid);
            if (q == null) continue;
            boolean isCorrect = selected == q.getAnswer();
            if (isCorrect) correct++;
            else {
                Map<String, Object> w = new LinkedHashMap<>();
                w.put("question", q.getQuestion());
                try { w.put("options", new org.json.JSONArray(q.getOptionsJson()).toList()); } catch (Exception e) {}
                w.put("answer", q.getAnswer()); w.put("selected", selected);
                w.put("analysis", q.getAnalysis());
                wrongList.add(w);
                wrongCtx.append("题：").append(q.getQuestion()).append(" 正确答案：").append(q.getAnswer()).append(" 学生选：").append(selected).append("；");
            }
        }

        int score = total > 0 ? Math.round((float) correct / total * 100) : 0;

        // AI 多维度报告
        String aiReport = "";
        if (wrongList.size() > 0) {
            String aiPrompt = "学生测验《" + courseTitle + "》得分" + score + "分，错" + wrongList.size() + "题。" +
                wrongCtx + "\n请按四维度(知识掌握度/答题准确率/理解深度/学习建议)给出JSON报告：" +
                "{\"dimensions\":[{\"name\":\"x\",\"score\":80,\"comment\":\"...\"}],\"overallComment\":\"总评\"}。只返回JSON。";
            List<AIChatUtil.Message> aiMsgs = new ArrayList<>();
            aiMsgs.add(new AIChatUtil.Message("system", "你是学习评估专家，严格按JSON返回。"));
            aiMsgs.add(new AIChatUtil.Message("user", aiPrompt));
            AIChatUtil.ChatResult aiCr = AIChatUtil.chatWithMessages(aiMsgs, 0.5, 800);
            aiReport = aiCr != null ? aiCr.getContent() : "";
            if (aiReport != null) { int s = aiReport.indexOf('{'), e = aiReport.lastIndexOf('}'); if (s>=0&&e>s) aiReport = aiReport.substring(s,e+1); }
        }

        // 保存记录
        StudentQuizRecordEntity record = new StudentQuizRecordEntity();
        record.setId(System.currentTimeMillis()); record.setTaskId(taskId);
        record.setStudentaccount(studentaccount); record.setStudentname(studentname);
        record.setCourseId(courseId); record.setCourseTitle(courseTitle);
        record.setScore(score); record.setTotalQuestions(total); record.setCorrectCount(correct);
        try { record.setAnswersJson(new org.json.JSONArray(answers).toString()); } catch (Exception e) {}
        record.setAiReport(aiReport);
        try { record.setWrongListJson(new org.json.JSONArray(wrongList).toString()); } catch (Exception e) {}
        record.setAddtime(new Date());
        studentQuizRecordDao.insert(record);

        // 标记任务完成 + 同步AI报告到任务记录（供详情页雷达图展示）
        RecitationtaskEntity task = recitationtaskDao.selectById(taskId);
        if (task != null) { task.setCompletionstatus("已完成"); task.setAiscorecomment(aiReport); task.setKaoshichengji(score); recitationtaskDao.updateById(task); }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("score", score); result.put("correctCount", correct);
        result.put("totalQuestions", total); result.put("wrongList", wrongList);
        result.put("aiReport", aiReport);
        return R.ok().put("data", result);
    }

    /** 教师端 — 查看某任务下所有学生的测验结果 */
    @RequestMapping("/teacher-results")
    public R teacherResults(@RequestParam Long taskId) {
        EntityWrapper<StudentQuizRecordEntity> ew = new EntityWrapper<>();
        ew.eq("task_id", taskId).orderBy("addtime", false);
        List<StudentQuizRecordEntity> list = studentQuizRecordDao.selectList(ew);
        List<Map<String, Object>> results = new ArrayList<>();
        for (StudentQuizRecordEntity r : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId()); m.put("studentname", r.getStudentname());
            m.put("studentaccount", r.getStudentaccount());
            m.put("score", r.getScore()); m.put("correctCount", r.getCorrectCount());
            m.put("totalQuestions", r.getTotalQuestions()); m.put("aiReport", r.getAiReport());
            m.put("addtime", r.getAddtime());
            results.add(m);
        }
        return R.ok().put("data", results);
    }

    private String cleanJson(String resp) {
        String s = resp.trim();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```").matcher(s);
        if (m.find()) s = m.group(1).trim();
        int start = s.indexOf('['), end = s.lastIndexOf(']');
        if (start >= 0 && end > start) s = s.substring(start, end + 1);
        return s;
    }
}
