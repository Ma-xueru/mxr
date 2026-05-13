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

    @RequestMapping("/deleteRecord")
    public R deleteRecord(@RequestBody Long[] ids) {
        quizRecordDao.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
}
