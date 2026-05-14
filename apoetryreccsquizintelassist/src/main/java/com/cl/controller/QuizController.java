package com.cl.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.cl.dao.QuizRecordDao;
import com.cl.entity.QuizRecordEntity;
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

    @RequestMapping("/deleteRecord")
    public R deleteRecord(@RequestBody Long[] ids) {
        quizRecordDao.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
}
