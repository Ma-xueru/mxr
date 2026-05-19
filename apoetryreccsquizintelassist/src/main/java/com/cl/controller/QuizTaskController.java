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
    @Autowired private com.cl.dao.StudentDao studentDao;
    @Autowired private CourseService courseService;

    /** 测验任务大盘 — 按批次分组，一个任务占一行 */
    @RequestMapping("/taskGroups")
    public R taskGroups(@RequestParam Map<String, Object> params, HttpServletRequest req) {
        EntityWrapper<RecitationtaskEntity> ew = new EntityWrapper<>();
        ew.like("tasktitle", "测验：");
        Object tableNameObj = req.getSession().getAttribute("tableName");
        if (tableNameObj != null && "teacher".equals(String.valueOf(tableNameObj))) {
            String username = (String) req.getSession().getAttribute("username");
            if (org.springframework.util.StringUtils.hasText(username)) ew.eq("teacheraccount", username);
        }
        ew.orderBy("id", false);
        List<RecitationtaskEntity> all = recitationtaskDao.selectList(ew);
        Map<String, Map<String, Object>> groups = new LinkedHashMap<>();
        for (RecitationtaskEntity t : all) {
            String key = t.getCourseids() != null ? t.getCourseids() : (t.getCoursetitles() != null ? t.getCoursetitles() : String.valueOf(t.getId()));
            Map<String, Object> g = groups.get(key);
            if (g == null) {
                g = new LinkedHashMap<>();
                g.put("tasktitle", t.getTasktitle()); g.put("coursetitles", t.getCoursetitles());
                g.put("teacheraccount", t.getTeacheraccount()); g.put("teachername", t.getTeachername());
                g.put("deadline", t.getDeadline()); g.put("total", 0); g.put("done", 0);
                g.put("classnames", new LinkedHashSet<>()); g.put("courseids", t.getCourseids());
                groups.put(key, g);
            }
            g.put("total", (int)g.get("total") + 1);
            if ("已完成".equals(t.getCompletionstatus())) g.put("done", (int)g.get("done") + 1);
            if (t.getClassname() != null) ((java.util.Set<String>)g.get("classnames")).add(t.getClassname());
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> g : groups.values()) {
            g.put("classes", String.join(", ", (java.util.Set<String>)g.get("classnames")));
            g.remove("classnames");
            result.add(g);
        }
        return R.ok().put("data", result);
    }

    /** 测验任务列表（仅task_type=2） */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest req) {
        EntityWrapper<RecitationtaskEntity> ew = new EntityWrapper<>();
        ew.like("tasktitle", "测验：");
        // 教师scope
        Object tableNameObj = req.getSession().getAttribute("tableName");
        if (tableNameObj != null && "teacher".equals(String.valueOf(tableNameObj))) {
            String username = (String) req.getSession().getAttribute("username");
            if (org.springframework.util.StringUtils.hasText(username)) ew.eq("teacheraccount", username);
        }
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
    /** AI智能出题 — 返回题目列表供教师审查 */
    @RequestMapping("/generate")
    public R generate(@RequestBody Map<String, Object> body) {
        String title = String.valueOf(body.getOrDefault("title", ""));
        Object poetryIdsObj = body.get("poetryIds");
        int questionCount = Integer.parseInt(String.valueOf(body.getOrDefault("questionCount", "5")));
        if (!StringUtils.hasText(title)) return R.error("请输入测验标题");
        if (poetryIdsObj == null) return R.error("请选择古诗");
        List<Long> poetryIds = new ArrayList<>();
        if (poetryIdsObj instanceof List) {
            for (Object o : (List<?>)poetryIdsObj) { try { poetryIds.add(Long.valueOf(String.valueOf(o))); } catch(Exception e){} }
        }
        if (poetryIds.isEmpty()) return R.error("古诗ID无效");

        // 收集所有古诗内容
        StringBuilder ctx = new StringBuilder();
        for (Long cid : poetryIds) {
            CourseEntity c = courseService.selectById(cid);
            if (c != null && StringUtils.hasText(c.getContent())) {
                ctx.append("《").append(c.getCoursetitle()).append("》\n").append(c.getContent()).append("\n\n");
            }
        }
        if (ctx.length() == 0) return R.error("所选古诗无内容");

        int perPoem = Math.max(2, questionCount / poetryIds.size());
        String prompt = "针对以下古诗出" + questionCount + "道测验题（每首约" + perPoem + "题）：\n" + ctx +
            "题型：单选题或填空题。维度：字词释义、意境理解、作者情感、格律常识、文学常识。\n" +
            "返回JSON：[{\"poetryId\":诗ID,\"questionType\":1,\"content\":\"题?\",\"options\":{\"A\":\"\",\"B\":\"\",\"C\":\"\",\"D\":\"\"},\"correctAnswer\":\"A\",\"analysis\":\"解析\"}]。只返回JSON数组。";

        List<AIChatUtil.Message> msgs = new ArrayList<>();
        msgs.add(new AIChatUtil.Message("system", "你是特级语文教研员。严格按JSON数组返回题目，不带任何markdown标记。"));
        msgs.add(new AIChatUtil.Message("user", prompt));
        AIChatUtil.ChatResult cr = AIChatUtil.chatWithMessages(msgs, 0.5, 3000);
        String resp = cr != null ? cr.getContent() : null;
        if (resp == null || resp.isEmpty()) return R.error("AI出题失败");

        String json = cleanJson(resp);
        System.out.println("[测验AI] 题目(" + json.length() + ")");

        // 暂存到quiz_question（taskId用临时负值，审核发布时更新）
        long tempTaskId = System.currentTimeMillis();
        List<Map<String, Object>> questions = new ArrayList<>();
        try {
            org.json.JSONArray arr = new org.json.JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                org.json.JSONObject q = arr.getJSONObject(i);
                QuizQuestionEntity qe = new QuizQuestionEntity();
                long qid = tempTaskId + i; qe.setId(qid);
                qe.setTaskId(tempTaskId); qe.setCourseId(q.optLong("poetryId", 0L));
                qe.setQuestion(q.optString("content", q.optString("question", "")));
                qe.setOptionsJson(q.optJSONObject("options") != null ? q.optJSONObject("options").toString() : q.optJSONArray("options").toString());
                String ans = q.optString("correctAnswer", ""); qe.setAnswer(ans.isEmpty() ? q.optInt("answer", 0) : ans.charAt(0) - 'A');
                qe.setAnalysis(q.optString("analysis", ""));
                qe.setSortOrder(i + 1);
                quizQuestionDao.insert(qe);

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", qid); item.put("taskId", tempTaskId);
                item.put("poetryId", qe.getCourseId()); item.put("questionType", q.optInt("questionType", 1));
                item.put("content", qe.getQuestion()); item.put("optionsJson", qe.getOptionsJson());
                item.put("correctAnswer", qe.getAnswer()); item.put("analysis", qe.getAnalysis());
                item.put("sortOrder", i + 1);
                questions.add(item);
            }
        } catch (Exception e) { return R.error("题目解析失败: " + e.getMessage()); }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tempTaskId", tempTaskId);
        result.put("questions", questions);
        return R.ok().put("data", result);
    }

    /** 教师审核通过一键发布 */
    @RequestMapping("/release")
    public R release(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        String title = String.valueOf(body.getOrDefault("title", ""));
        long tempTaskId = Long.parseLong(String.valueOf(body.getOrDefault("tempTaskId", "0")));
        Object classIdsObj = body.get("classIds");
        Object questionsObj = body.get("questions");
        String teacheraccount = String.valueOf(req.getSession().getAttribute("username"));

        System.out.println("[测验发布] tempTaskId=" + tempTaskId + " classIds=" + classIdsObj + " questionsCount=" + (questionsObj instanceof List ? ((List<?>)questionsObj).size() : 0));
        if (tempTaskId == 0) return R.error("任务ID无效");
        if (!(classIdsObj instanceof List) || ((List<?>)classIdsObj).isEmpty()) return R.error("请选择班级");
        if (!(questionsObj instanceof List) || ((List<?>)questionsObj).isEmpty()) return R.error("题目列表为空");

        List<String> classIds = new ArrayList<>();
        for (Object o : (List<?>)classIdsObj) classIds.add(String.valueOf(o));
        List<Map<String, Object>> questions = new ArrayList<>();
        for (Object o : (List<?>)questionsObj) questions.add((Map<String, Object>)o);

        // 题目taskId保持不变(tempTaskId)，作为任务分组键
        // 1. 更新题目内容（老师可能修改了）
        for (Map<String, Object> q : questions) {
            Long qid = Long.valueOf(String.valueOf(q.get("id")));
            QuizQuestionEntity qe = new QuizQuestionEntity();
            qe.setId(qid); qe.setTaskId(tempTaskId);
            qe.setQuestion(String.valueOf(q.getOrDefault("content", q.getOrDefault("question", ""))));
            qe.setOptionsJson(String.valueOf(q.getOrDefault("optionsJson", "{}")));
            try { qe.setAnswer(Integer.parseInt(String.valueOf(q.getOrDefault("correctAnswer", "0")))); } catch(Exception e) { qe.setAnswer(0); }
            qe.setAnalysis(String.valueOf(q.getOrDefault("analysis", "")));
            quizQuestionDao.updateById(qe);
        }

        // 2. 为每个班级的每个学生创建任务记录
        int totalStudents = 0;
        for (String cn : classIds) {
            EntityWrapper<com.cl.entity.StudentEntity> se = new EntityWrapper<>();
            se.eq("classname", cn);
            List<com.cl.entity.StudentEntity> slist = studentDao.selectList(se);
            for (com.cl.entity.StudentEntity s : slist) {
                RecitationtaskEntity task = new RecitationtaskEntity();
                task.setId(System.currentTimeMillis() + totalStudents);
                task.setTasktitle("测验：" + title);
                task.setTaskType(2);
                task.setStudentaccount(s.getStudentaccount());
                task.setStudentname(s.getStudentname());
                task.setClassname(s.getClassname());
                task.setTeacheraccount(teacheraccount);
                task.setReleasetime(new Date());
                task.setCompletionstatus("待完成");
                task.setCourseids(String.valueOf(tempTaskId)); // 存原taskId用于查题
                task.setCoursetitles(title); // 存测验标题
                recitationtaskDao.insert(task);
                totalStudents++;
            }
        }

        // 3. 清理临时题目中的tempTaskId（已更新为realTaskId）
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskId", tempTaskId);
        result.put("studentCount", totalStudents);
        result.put("questionCount", questions.size());
        return R.ok().put("data", result);
    }

    /** 查询某任务的题目列表 */
    @RequestMapping("/questions")
    public R questions(@RequestParam Long taskId) {
        EntityWrapper<QuizQuestionEntity> ew = new EntityWrapper<>();
        ew.eq("task_id", taskId).orderBy("sort_order", true);
        return R.ok().put("data", quizQuestionDao.selectList(ew));
    }

    @RequestMapping("/update") public R update(@RequestBody RecitationtaskEntity e) { recitationtaskDao.updateById(e); return R.ok(); }
    @RequestMapping("/delete") public R delete(@RequestBody Long[] ids) { recitationtaskDao.deleteBatchIds(java.util.Arrays.asList(ids)); return R.ok(); }

    /** 获取已完成的测验结果（雷达图+AI报告） */
    @RequestMapping("/result")
    public R result(@RequestParam Long taskId, @RequestParam(required = false) String student, HttpServletRequest req) {
        String username = StringUtils.hasText(student) ? student : String.valueOf(req.getSession().getAttribute("username"));
        EntityWrapper<StudentQuizRecordEntity> ew = new EntityWrapper<>();
        ew.eq("task_id", taskId).eq("studentaccount", username).orderBy("addtime", false).last("LIMIT 1");
        List<StudentQuizRecordEntity> list = studentQuizRecordDao.selectList(ew);
        if (list.isEmpty()) return R.error("暂无结果");
        StudentQuizRecordEntity r = list.get(0);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("score", r.getScore()); m.put("correctCount", r.getCorrectCount());
        m.put("totalQuestions", r.getTotalQuestions()); m.put("aiReport", enforceThreeDimensions(r.getAiReport()));
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
            // 查询题目 — 优先用courseids分组键，回退到id
            Long groupKey = t.getId();
            if (StringUtils.hasText(cids)) { try { groupKey = Long.valueOf(cids.split(",")[0].trim()); } catch(Exception ex) {} }
            EntityWrapper<QuizQuestionEntity> qw = new EntityWrapper<>();
            qw.eq("task_id", groupKey).orderBy("sort_order");
            List<QuizQuestionEntity> qs = quizQuestionDao.selectList(qw);
            if (qs.isEmpty() && !groupKey.equals(t.getId())) {
                qw = new EntityWrapper<>(); qw.eq("task_id", t.getId()).orderBy("sort_order");
                qs = quizQuestionDao.selectList(qw);
            }
            List<Map<String, Object>> questions = new ArrayList<>();
            for (QuizQuestionEntity q : qs) {
                Map<String, Object> qm = new LinkedHashMap<>();
                qm.put("id", q.getId()); qm.put("question", q.getQuestion());
                try {
                    String optJson = q.getOptionsJson();
                    if (optJson != null) {
                        if (optJson.trim().startsWith("{")) {
                            org.json.JSONObject optObj = new org.json.JSONObject(optJson);
                            List<String> opts = new ArrayList<>();
                            for (String k : optObj.keySet()) opts.add(k + ". " + optObj.optString(k));
                            qm.put("options", opts);
                        } else if (optJson.trim().startsWith("[")) {
                            qm.put("options", new org.json.JSONArray(optJson).toList());
                        }
                    }
                } catch (Exception e) { qm.put("options", new ArrayList<>()); }
                qm.put("answer", q.getAnswer()); qm.put("analysis", q.getAnalysis());
                questions.add(qm);
            }
            m.put("questions", questions);
            result.add(m);
        }
        return R.ok().put("data", result);
    }

    /** 学生提交答案 + AI评分 */
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

        // 加载题目及答案 — 先查taskId，没找到则从recitationtask的courseids取分组键
        EntityWrapper<QuizQuestionEntity> qw = new EntityWrapper<>();
        qw.eq("task_id", taskId).orderBy("sort_order");
        List<QuizQuestionEntity> questions = quizQuestionDao.selectList(qw);
        if (questions.isEmpty()) {
            RecitationtaskEntity rt = recitationtaskDao.selectById(taskId);
            if (rt != null && StringUtils.hasText(rt.getCourseids())) {
                try {
                    Long gk = Long.valueOf(rt.getCourseids().split(",")[0].trim());
                    qw = new EntityWrapper<>(); qw.eq("task_id", gk).orderBy("sort_order");
                    questions = quizQuestionDao.selectList(qw);
                } catch(Exception e) {}
            }
        }
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
                try {
                    String oj = q.getOptionsJson();
                    if (oj != null && oj.trim().startsWith("{")) {
                        org.json.JSONObject oo = new org.json.JSONObject(oj); List<String> ol = new ArrayList<>();
                        for (String k : oo.keySet()) ol.add(k + ". " + oo.optString(k));
                        w.put("options", ol);
                    } else if (oj != null) { w.put("options", new org.json.JSONArray(oj).toList()); }
                } catch (Exception e) {}
                w.put("answer", q.getAnswer()); w.put("selected", selected);
                w.put("analysis", q.getAnalysis());
                wrongList.add(w);
                wrongCtx.append("题：").append(q.getQuestion()).append(" 正确答案：").append(q.getAnswer()).append(" 学生选：").append(selected).append("；");
            }
        }

        int score = total > 0 ? Math.round((float) correct / total * 100) : 0;

        // AI 多维度报告（3量化维度 + 独立学习建议 + 总评）
        String aiReport = "";
        if (wrongList.size() > 0) {
            String aiPrompt = "学生测验《" + courseTitle + "》得分" + score + "分，错" + wrongList.size() + "题。" +
                wrongCtx + "\n请按三量化维度评估（严格仅输出3个维度！不要输出第4个）：" +
                "{\"dimensions\":[{\"name\":\"知识掌握度\",\"score\":80,\"comment\":\"...\"},{\"name\":\"答题准确率\",\"score\":70,\"comment\":\"...\"},{\"name\":\"理解深度\",\"score\":60,\"comment\":\"...\"}],\"suggestion\":\"针对薄弱点的具体学习行动计划\",\"overallComment\":\"总评收尾\"}。" +
                "维度仅限3个。suggestion和overallComment为纯文本。只返回JSON。";
            List<AIChatUtil.Message> aiMsgs = new ArrayList<>();
            aiMsgs.add(new AIChatUtil.Message("system", "你是学习评估专家，严格按JSON返回。dimensions数组里只能有3个元素"));
            aiMsgs.add(new AIChatUtil.Message("user", aiPrompt));
            AIChatUtil.ChatResult aiCr = AIChatUtil.chatWithMessages(aiMsgs, 0.5, 800);
            aiReport = aiCr != null ? aiCr.getContent() : "";
            if (aiReport != null) { int s = aiReport.indexOf('{'), e = aiReport.lastIndexOf('}'); if (s>=0&&e>s) aiReport = aiReport.substring(s,e+1); }
            aiReport = enforceThreeDimensions(aiReport);
            // AI调用失败时兜底生成报告
            if (aiReport == null || aiReport.isEmpty() || !aiReport.contains("dimensions")) {
                aiReport = buildDefaultReport(score, correct, total);
            }
        } else {
            aiReport = buildDefaultReport(score, correct, total);
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
            m.put("totalQuestions", r.getTotalQuestions()); m.put("aiReport", enforceThreeDimensions(r.getAiReport()));
            m.put("addtime", r.getAddtime());
            results.add(m);
        }
        return R.ok().put("data", results);
    }

    /** 强制裁剪dimensions为3个（AI有时不遵守指令） */
    private String enforceThreeDimensions(String report) {
        if (report == null || report.isEmpty()) return report;
        try {
            org.json.JSONObject obj = new org.json.JSONObject(report);
            if (obj.has("dimensions")) {
                org.json.JSONArray dims = obj.getJSONArray("dimensions");
                String[] standardNames = {"知识掌握度", "答题准确率", "理解深度"};
                org.json.JSONArray trimmed = new org.json.JSONArray();
                for (int i = 0; i < Math.min(dims.length(), 3); i++) {
                    org.json.JSONObject d = dims.getJSONObject(i);
                    if (i < standardNames.length) d.put("name", standardNames[i]);
                    trimmed.put(d);
                }
                obj.put("dimensions", trimmed);
            }
            String result = obj.toString();
            if (report != null && !report.isEmpty() && result.isEmpty()) return report;
            return result;
        } catch (Exception e) { return report; }
    }

    private String buildDefaultReport(int score, int correct, int total) {
        int s = Math.max(0, Math.min(100, score));
        String comment80 = "掌握扎实，表现优秀！";
        String comment60 = "基本掌握，仍有提升空间。";
        String comment40 = "基础薄弱，需要加强学习。";
        String c = s >= 80 ? comment80 : s >= 60 ? comment60 : comment40;
        return "{\"dimensions\":[" +
            "{\"name\":\"知识掌握度\",\"score\":" + s + ",\"comment\":\"" + c + "\"}," +
            "{\"name\":\"答题准确率\",\"score\":" + s + ",\"comment\":\"" + c + "\"}," +
            "{\"name\":\"理解深度\",\"score\":" + s + ",\"comment\":\"" + c + "\"}]," +
            "\"suggestion\":\"" + (s >= 80 ? "表现优异！继续挑战更高难度。" : s >= 60 ? "建议针对错题进行专项练习。" : "建议重新学习相关古诗，打好基础。") + "\"," +
            "\"overallComment\":\"答对" + correct + "/" + total + "题，得分" + score + "分。\"}";
    }

    private String cleanJson(String resp) {
        String s = resp.trim();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```").matcher(s);
        if (m.find()) s = m.group(1).trim();
        int start = s.indexOf('['), end = s.lastIndexOf(']');
        if (start >= 0 && end > start) s = s.substring(start, end + 1);
        // 修复常见JSON错误: 尾部多余逗号, 单引号转双引号
        s = s.replaceAll(",\\s*]", "]").replaceAll(",\\s*}", "}");
        s = s.replaceAll("(?<!\\\\)'", "\"");
        return s;
    }
}
