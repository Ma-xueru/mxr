package com.cl.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.cl.annotation.IgnoreAuth;
import com.cl.dao.FollowreadRecordDao;
import com.cl.dao.QuizRecordDao;
import com.cl.entity.FollowreadRecordEntity;
import com.cl.entity.QuizRecordEntity;
import com.cl.utils.AIChatUtil;
import com.cl.utils.PageUtils;
import com.cl.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/quiz")
public class QuizController {

    @Autowired
    private QuizRecordDao quizRecordDao;
    @Autowired
    private FollowreadRecordDao followreadRecordDao;

    /** 保存测验记录 */
    @RequestMapping("/saveRecord")
    public R saveRecord(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        QuizRecordEntity record = new QuizRecordEntity();
        record.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        record.setStudentaccount(String.valueOf(body.getOrDefault("studentaccount", "")));
        record.setStudentname(String.valueOf(body.getOrDefault("studentname", "")));
        record.setCourseid(Long.valueOf(String.valueOf(body.getOrDefault("courseid", "0"))));
        record.setCoursetitle(String.valueOf(body.getOrDefault("coursetitle", "")));
        record.setScore(Integer.valueOf(String.valueOf(body.getOrDefault("score", "0"))));
        record.setDuration(Integer.valueOf(String.valueOf(body.getOrDefault("duration", "0"))));
        record.setQuestionsCount(Integer.valueOf(String.valueOf(body.getOrDefault("questionsCount", "5"))));
        record.setCorrectCount(Integer.valueOf(String.valueOf(body.getOrDefault("correctCount", "0"))));
        record.setWrongListJson(String.valueOf(body.getOrDefault("wrongListJson", "[]")));
        record.setAddtime(new Date());
        quizRecordDao.insert(record);
        return R.ok().put("data", record.getId());
    }

    /** 查询某首古诗的测验记录 */
    @RequestMapping("/records")
    public R records(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        EntityWrapper<QuizRecordEntity> ew = new EntityWrapper<>();
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
        List<QuizRecordEntity> list = quizRecordDao.selectPage(
                new com.baomidou.mybatisplus.plugins.Page<>(page, limit), ew);
        int total = quizRecordDao.selectCount(ew);
        return R.ok().put("data", new PageUtils(list, total, limit, page));
    }

    /** 错题本 — 返回所有错题（按学生过滤） */
    @IgnoreAuth
    @RequestMapping("/wrongbook")
    public R wrongbook(HttpServletRequest request) {
        EntityWrapper<QuizRecordEntity> ew = new EntityWrapper<>();
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        if ("student".equals(tableName)) {
            ew.eq("studentaccount", String.valueOf(request.getSession().getAttribute("username")));
        }
        ew.isNotNull("wrongListJson").ne("wrongListJson", "[]").orderBy("addtime", false);
        List<QuizRecordEntity> list = quizRecordDao.selectList(ew);

        List<Map<String, Object>> wrongList = new ArrayList<>();
        for (QuizRecordEntity r : list) {
            try {
                org.json.JSONArray arr = new org.json.JSONArray(r.getWrongListJson());
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject w = arr.getJSONObject(i);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("poemTitle", r.getCoursetitle());
                    item.put("question", w.optString("question"));
                    item.put("options", w.optJSONArray("options") != null ? w.optJSONArray("options").toList() : new ArrayList<>());
                    item.put("answer", w.optInt("answer"));
                    item.put("selected", w.optInt("selected"));
                    item.put("analysis", w.optString("analysis"));
                    wrongList.add(item);
                }
            } catch (Exception e) { /* skip bad json */ }
        }
        return R.ok().put("data", wrongList);
    }

    /** 批量迁移云数据库记录到 MySQL */
    @RequestMapping("/migrate")
    public R migrate(@RequestBody List<Map<String, Object>> records) {
        for (Map<String, Object> r : records) {
            QuizRecordEntity entity = new QuizRecordEntity();
            entity.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
            entity.setStudentaccount(String.valueOf(r.getOrDefault("studentaccount", "")));
            entity.setStudentname(String.valueOf(r.getOrDefault("studentname", "")));
            entity.setCourseid(Long.valueOf(String.valueOf(r.getOrDefault("courseid", "0"))));
            entity.setCoursetitle(String.valueOf(r.getOrDefault("coursetitle", "")));
            entity.setScore(Integer.valueOf(String.valueOf(r.getOrDefault("score", "0"))));
            entity.setDuration(Integer.valueOf(String.valueOf(r.getOrDefault("duration", "0"))));
            entity.setQuestionsCount(Integer.valueOf(String.valueOf(r.getOrDefault("questionsCount", "5"))));
            entity.setCorrectCount(Integer.valueOf(String.valueOf(r.getOrDefault("correctCount", "0"))));
            entity.setWrongListJson(String.valueOf(r.getOrDefault("wrongListJson", "[]")));
            entity.setAddtime(new Date());
            try { quizRecordDao.insert(entity); } catch (Exception e) { /* skip dup */ }
        }
        return R.ok().put("data", records.size());
    }

    /** AI 智适应学习中心 — 弱点雷达图 + 自适应排序推荐 */
    @SuppressWarnings("unchecked")
    @RequestMapping("/smart-center")
    public R smartCenter(HttpServletRequest request) {
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        String username = String.valueOf(request.getSession().getAttribute("username"));

        // 查询所有测验记录
        EntityWrapper<QuizRecordEntity> ew = new EntityWrapper<>();
        if ("student".equals(tableName)) ew.eq("studentaccount", username);
        ew.orderBy("addtime", false);
        List<QuizRecordEntity> all = quizRecordDao.selectList(ew);

        // 四维弱点分析: 字词释义、意境感悟、文学常识、格律对仗
        String[] dims = {"字词释义", "意境感悟", "文学常识", "格律对仗"};
        Map<String, Integer> wrongCount = new LinkedHashMap<>();
        Map<String, Integer> totalCount = new LinkedHashMap<>();
        for (String d : dims) { wrongCount.put(d, 0); totalCount.put(d, 0); }

        // 能力值: 初始100，每个错题扣分
        Map<String, Integer> ability = new LinkedHashMap<>();
        for (String d : dims) ability.put(d, 100);

        for (QuizRecordEntity r : all) {
            try {
                org.json.JSONArray arr = new org.json.JSONArray(r.getWrongListJson());
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject w = arr.getJSONObject(i);
                    String q = w.optString("question", "");
                    String dim = classifyQuestion(q);
                    wrongCount.put(dim, wrongCount.get(dim) + 1);
                }
            } catch (Exception e) { /* skip */ }
        }

        // 能力值 = max(0, 100 - 错题数 * 15)
        for (String d : dims) {
            int wc = wrongCount.getOrDefault(d, 0);
            ability.put(d, Math.max(0, 100 - wc * 15));
        }

        // 自适应排序: 对古诗按W值排序
        // 先按 courseid 聚合，取最近得分和时间
        Map<Long, Integer> latestScores = new LinkedHashMap<>();
        Map<Long, Long> latestTimes = new LinkedHashMap<>();
        Map<Long, String> latestTitles = new LinkedHashMap<>();
        for (QuizRecordEntity r : all) {
            Long cid = r.getCourseid();
            if (!latestScores.containsKey(cid)) {
                latestScores.put(cid, r.getScore());
                latestTimes.put(cid, r.getAddtime() != null ? r.getAddtime().getTime() : 0L);
                latestTitles.put(cid, r.getCoursetitle());
            }
        }

        List<Map<String, Object>> recommended = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (Long cid : latestScores.keySet()) {
            int score = latestScores.get(cid);
            long lastTime = latestTimes.get(cid);
            int daysAgo = (int) ((now - lastTime) / 86400000L);
            // W = (100 - score) * 0.6 + daysAgo * 10
            int w = (int) Math.round((100 - score) * 0.6 + daysAgo * 10);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("courseId", cid);
            item.put("courseTitle", latestTitles.get(cid));
            item.put("latestScore", score);
            item.put("daysAgo", daysAgo);
            item.put("weight", w);
            recommended.add(item);
        }
        recommended.sort((a, b) -> Integer.compare((int) b.get("weight"), (int) a.get("weight")));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ability", ability);
        result.put("dimensions", dims);
        result.put("recommended", recommended);
        result.put("totalQuizzes", all.size());
        return R.ok().put("data", result);
    }

    private String classifyQuestion(String q) {
        if (q.contains("意思") || q.contains("解释") || q.contains("释") || q.contains("义")) return "字词释义";
        if (q.contains("情感") || q.contains("意境") || q.contains("感受") || q.contains("情怀")) return "意境感悟";
        if (q.contains("诗人") || q.contains("作者") || q.contains("背景") || q.contains("常识")) return "文学常识";
        if (q.contains("对仗") || q.contains("平仄") || q.contains("押韵") || q.contains("格律")) return "格律对仗";
        return "字词释义"; // default
    }

    /** 温故知新 — 聚合近14天跟读+测验记录，由 AI 生成复习题 */
    @IgnoreAuth
    @SuppressWarnings("unchecked")
    @RequestMapping("/history-review")
    public R historyReview(HttpServletRequest request) {
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        String username = String.valueOf(request.getSession().getAttribute("username"));
        long fourteenDaysAgo = System.currentTimeMillis() - 14 * 86400000L;

        // 聚合跟读记录
        EntityWrapper<FollowreadRecordEntity> fw = new EntityWrapper<>();
        if ("student".equals(tableName)) fw.eq("studentaccount", username);
        fw.ge("addtime", new java.util.Date(fourteenDaysAgo));
        List<FollowreadRecordEntity> followList = followreadRecordDao.selectList(fw);

        // 聚合测验记录
        EntityWrapper<QuizRecordEntity> qw = new EntityWrapper<>();
        if ("student".equals(tableName)) qw.eq("studentaccount", username);
        qw.ge("addtime", new java.util.Date(fourteenDaysAgo));
        List<QuizRecordEntity> quizList = quizRecordDao.selectList(qw);

        // 按 poemId 去重聚合
        Map<Long, Map<String, Object>> poemMap = new LinkedHashMap<>();
        for (FollowreadRecordEntity r : followList) {
            poemMap.computeIfAbsent(r.getCourseid(), k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("courseId", k); m.put("courseTitle", r.getCoursetitle());
                m.put("types", new ArrayList<String>());
                return m;
            });
            ((List<String>) poemMap.get(r.getCourseid()).get("types")).add("recitation");
        }
        for (QuizRecordEntity r : quizList) {
            poemMap.computeIfAbsent(r.getCourseid(), k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("courseId", k); m.put("courseTitle", r.getCoursetitle());
                m.put("types", new ArrayList<String>());
                return m;
            });
            String type = r.getScore() != null && r.getScore() < 80 ? "comprehension" : "quiz";
            ((List<String>) poemMap.get(r.getCourseid()).get("types")).add(type);
        }

        if (poemMap.isEmpty()) return R.ok().put("data", new org.json.JSONArray().toString());

        // 构建 AI prompt 出题
        StringBuilder ctx = new StringBuilder();
        for (Map<String, Object> pm : poemMap.values()) {
            ctx.append("《").append(pm.get("courseTitle")).append("》维度：").append(pm.get("types")).append("；");
        }

        String prompt = "根据学生近14天学习记录出10道题：\n" + ctx + "\n\n规则：\n" +
            "1. 标recitation的诗必须出读音/断句题\n2. 标comprehension的出意境理解题\n" +
            "3. 返回JSON数组：[{\"question\":\"题?\",\"options\":[\"A\",\"B\",\"C\",\"D\"],\"answer\":0,\"analysis\":\"解析\",\"knowledge_tag\":\"标签\"}]。只返回JSON。";

        List<AIChatUtil.Message> msgs = new ArrayList<>();
        msgs.add(new AIChatUtil.Message("system", "你是古诗词教学专家，严格按JSON返回5道题。"));
        msgs.add(new AIChatUtil.Message("user", prompt));
        AIChatUtil.ChatResult cr = AIChatUtil.chatWithMessages(msgs, 0.5, 3500);
        String resp = cr != null ? cr.getContent() : null;
        System.out.println("[温故知新] AI(" + (resp != null ? resp.length() : 0) + ")");

        if (resp == null || resp.isEmpty()) return R.error("AI未返回题目");
        return R.ok().put("data", cleanQuizJson(resp));
    }

    /** 举一反三 — 从错题标签提取薄弱点，AI 匹配同类诗词出题 */
    @IgnoreAuth
    @RequestMapping("/analogy-training")
    public R analogyTraining(HttpServletRequest request) {
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        String username = String.valueOf(request.getSession().getAttribute("username"));

        EntityWrapper<QuizRecordEntity> qw = new EntityWrapper<>();
        if ("student".equals(tableName)) qw.eq("studentaccount", username);
        qw.isNotNull("wrong_list_json").ne("wrong_list_json", "[]").orderBy("addtime", false).last("LIMIT 5");
        List<QuizRecordEntity> quizList = quizRecordDao.selectList(qw);

        if (quizList.isEmpty()) return R.error("暂无错题记录");

        // 提取错题标签
        Set<String> tags = new LinkedHashSet<>();
        Set<String> wrongPoemTitles = new LinkedHashSet<>();
        for (QuizRecordEntity r : quizList) {
            try {
                org.json.JSONArray arr = new org.json.JSONArray(r.getWrongListJson());
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject w = arr.getJSONObject(i);
                    String q = w.optString("question", "");
                    String tag = inferTag(q);
                    if (!tag.isEmpty()) tags.add(tag);
                }
            } catch (Exception e) {}
            if (r.getCoursetitle() != null) wrongPoemTitles.add(r.getCoursetitle());
        }

        if (tags.isEmpty()) return R.error("无法识别薄弱标签");

        String tagList = String.join(",", tags);
        String forbiddenList = String.join(",", wrongPoemTitles);

        String prompt = "薄弱标签：" + tagList + "。\n严禁出现这些诗：" + forbiddenList + "。\n" +
            "请选取2-3首包含这些标签但不在禁止列表中的古诗，出10道对比选择题。\n" +
            "返回JSON：[{\"question\":\"题?\",\"options\":[\"A\",\"B\",\"C\",\"D\"],\"answer\":0,\"analysis\":\"解析\",\"knowledge_tag\":\"标签\"}]。只返回JSON。";

        List<AIChatUtil.Message> msgs = new ArrayList<>();
        msgs.add(new AIChatUtil.Message("system", "你是古诗词教学专家，擅长类比出题，严格按JSON返回5道题。"));
        msgs.add(new AIChatUtil.Message("user", prompt));
        AIChatUtil.ChatResult cr = AIChatUtil.chatWithMessages(msgs, 0.5, 3500);
        String resp = cr != null ? cr.getContent() : null;
        System.out.println("[举一反三] AI(" + (resp != null ? resp.length() : 0) + ")");

        if (resp == null || resp.isEmpty()) return R.error("AI未返回题目");
        return R.ok().put("data", cleanQuizJson(resp));
    }

    private String inferTag(String q) {
        if (q.contains("月") || q.contains("花") || q.contains("鸟")) return "意象：自然";
        if (q.contains("意思") || q.contains("解释")) return "字词释义";
        if (q.contains("情感") || q.contains("意境")) return "意境感悟";
        if (q.contains("诗人") || q.contains("背景")) return "文学常识";
        if (q.contains("对仗") || q.contains("平仄")) return "格律对仗";
        if (q.contains("读音") || q.contains("断句")) return "诵读节奏";
        return "";
    }

    private String cleanQuizJson(String resp) {
        String s = resp.trim();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```").matcher(s);
        if (m.find()) s = m.group(1).trim();
        int start = s.indexOf('['), end = s.lastIndexOf(']');
        if (start >= 0 && end > start) s = s.substring(start, end + 1);
        return s;
    }

    @RequestMapping("/deleteRecord")
    public R deleteRecord(@RequestBody Long[] ids) {
        quizRecordDao.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
}
