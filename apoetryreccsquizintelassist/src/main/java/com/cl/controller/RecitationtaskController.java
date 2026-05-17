package com.cl.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.cl.annotation.IgnoreAuth;
import com.cl.entity.CourseEntity;
import com.cl.entity.RecitationtaskEntity;
import com.cl.entity.ClassinfoEntity;
import com.cl.entity.MystudentEntity;
import com.cl.entity.StudentEntity;
import com.cl.entity.TeacherEntity;
import com.cl.entity.view.RecitationtaskView;
import com.cl.service.ClassinfoService;
import com.cl.service.CourseService;
import com.cl.service.MystudentService;
import com.cl.service.RecitationtaskService;
import com.cl.service.StudentService;
import com.cl.service.TeacherService;
import com.cl.utils.MPUtil;
import com.cl.utils.PageUtils;
import com.cl.utils.R;
import com.cl.utils.AIUitl;
import com.cl.utils.AIRecitationReviewUtil;
import com.cl.utils.RecitationReviewUtil;
import com.cl.utils.VolcengineSpeechUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 背诵任务
 * 后端接口
 */
@RestController
@RequestMapping("/recitationtask")
public class RecitationtaskController {
    @Autowired
    private RecitationtaskService recitationtaskService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private MystudentService mystudentService;
    @Autowired
    private ClassinfoService classinfoService;
    @Autowired
    private CourseService courseService;

    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, RecitationtaskEntity recitationtask, HttpServletRequest request) {
        String tableName = request.getSession().getAttribute("tableName").toString();
        if (tableName.equals("student")) {
            recitationtask.setStudentaccount((String) request.getSession().getAttribute("username"));
        }
        EntityWrapper<RecitationtaskEntity> ew = new EntityWrapper<RecitationtaskEntity>();
        applyTeacherScope(ew, request);
        PageUtils page = recitationtaskService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, recitationtask), params), params));
        return R.ok().put("data", page);
    }

    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, RecitationtaskEntity recitationtask, HttpServletRequest request) {
        EntityWrapper<RecitationtaskEntity> ew = new EntityWrapper<RecitationtaskEntity>();
        applyTeacherScope(ew, request);
        PageUtils page = recitationtaskService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, recitationtask), params), params));
        return R.ok().put("data", page);
    }

    @RequestMapping("/lists")
    public R lists(RecitationtaskEntity recitationtask) {
        EntityWrapper<RecitationtaskEntity> ew = new EntityWrapper<RecitationtaskEntity>();
        ew.allEq(MPUtil.allEQMapPre(recitationtask, "recitationtask"));
        return R.ok().put("data", recitationtaskService.selectListView(ew));
    }

    @RequestMapping("/query")
    public R query(RecitationtaskEntity recitationtask) {
        EntityWrapper<RecitationtaskEntity> ew = new EntityWrapper<RecitationtaskEntity>();
        ew.allEq(MPUtil.allEQMapPre(recitationtask, "recitationtask"));
        RecitationtaskView view = recitationtaskService.selectView(ew);
        return R.ok("查询背诵任务成功").put("data", view);
    }

    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        RecitationtaskEntity entity = recitationtaskService.selectView(new EntityWrapper<RecitationtaskEntity>().eq("id", id));
        return R.ok().put("data", entity);
    }

    @IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id) {
        RecitationtaskEntity entity = recitationtaskService.selectView(new EntityWrapper<RecitationtaskEntity>().eq("id", id));
        return R.ok().put("data", entity);
    }

    @RequestMapping("/save")
    public R save(@RequestBody RecitationtaskEntity recitationtask, HttpServletRequest request) {
        recitationtask.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        fillClassname(recitationtask);
        fillAutoReviewResult(recitationtask, request);
        recitationtaskService.insert(recitationtask);
        return R.ok();
    }

    @RequestMapping("/add")
    public R add(@RequestBody RecitationtaskEntity recitationtask, HttpServletRequest request) {
        recitationtask.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        fillClassname(recitationtask);
        fillAutoReviewResult(recitationtask, request);
        recitationtaskService.insert(recitationtask);
        return R.ok();
    }

    private void fillClassname(RecitationtaskEntity task) {
        if (task.getClassname() != null && !task.getClassname().isEmpty()) return;
        if (task.getStudentaccount() != null) {
            StudentEntity s = studentService.selectOne(new EntityWrapper<StudentEntity>().eq("studentaccount", task.getStudentaccount()));
            if (s != null && s.getClassname() != null) task.setClassname(s.getClassname());
        }
    }

    @RequestMapping("/batchAssign")
    @Transactional
    public R batchAssign(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        String tasktitle = payload.get("tasktitle") == null ? "" : String.valueOf(payload.get("tasktitle")).trim();
        if (!StringUtils.hasText(tasktitle)) {
            return R.error("任务标题不能为空");
        }
        String taskcontent = payload.get("taskcontent") == null ? "" : String.valueOf(payload.get("taskcontent")).trim();
        if (!StringUtils.hasText(taskcontent)) {
            return R.error("任务要求不能为空");
        }

        String teacheraccount = payload.get("teacheraccount") == null ? "" : String.valueOf(payload.get("teacheraccount")).trim();
        String teachername = payload.get("teachername") == null ? "" : String.valueOf(payload.get("teachername")).trim();
        if (!StringUtils.hasText(teacheraccount)) {
            Object username = request.getSession().getAttribute("username");
            if (username != null) {
                teacheraccount = String.valueOf(username);
            }
        }

        Date now = new Date();
        Date releasetime = now;
        Object releasetimeObj = payload.get("releasetime");
        Date releasetimeDate = parseDateValue(releasetimeObj);
        if (releasetimeDate != null) {
            releasetime = releasetimeDate;
        }
        Date deadline = null;
        Object deadlineObj = payload.get("deadline");
        deadline = parseDateValue(deadlineObj);

        Set<String> accountSet = new LinkedHashSet<String>();
        Object studentaccountsObj = payload.get("studentaccounts");
        if (studentaccountsObj instanceof List) {
            for (Object item : (List<?>) studentaccountsObj) {
                if (item != null && StringUtils.hasText(String.valueOf(item))) {
                    accountSet.add(String.valueOf(item).trim());
                }
            }
        }
        if (payload.get("studentaccount") != null && StringUtils.hasText(String.valueOf(payload.get("studentaccount")))) {
            accountSet.add(String.valueOf(payload.get("studentaccount")).trim());
        }

        // 支持多班级（classnames）和单班级（classname）两种参数
        Object classnamesObj = payload.get("classnames");
        if (classnamesObj instanceof List) {
            for (Object cn : (List<?>) classnamesObj) {
                if (cn != null && StringUtils.hasText(String.valueOf(cn))) {
                    String cnStr = String.valueOf(cn).trim();
                    List<StudentEntity> classStudents = studentService.selectList(new EntityWrapper<StudentEntity>().eq("classname", cnStr));
                    for (StudentEntity item : classStudents) {
                        if (item != null && StringUtils.hasText(item.getStudentaccount())) {
                            accountSet.add(item.getStudentaccount());
                        }
                    }
                }
            }
        }
        String classname = payload.get("classname") == null ? "" : String.valueOf(payload.get("classname")).trim();
        if (StringUtils.hasText(classname)) {
            List<StudentEntity> classStudents = studentService.selectList(new EntityWrapper<StudentEntity>().eq("classname", classname));
            for (StudentEntity item : classStudents) {
                if (item != null && StringUtils.hasText(item.getStudentaccount())) {
                    accountSet.add(item.getStudentaccount());
                }
            }
        }

        if (accountSet.isEmpty()) {
            return R.error("请至少选择一个学生或班级");
        }

        List<StudentEntity> students = studentService.selectList(new EntityWrapper<StudentEntity>().in("studentaccount", accountSet));
        if (students == null || students.isEmpty()) {
            return R.error("未找到可发布任务的学生");
        }

        int successCount = 0;
        for (StudentEntity student : students) {
            RecitationtaskEntity task = new RecitationtaskEntity();
            task.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue() + successCount);
            task.setStudentaccount(student.getStudentaccount());
            task.setStudentname(student.getStudentname());
            task.setCourseids(payload.get("courseids") == null ? "" : String.valueOf(payload.get("courseids")));
            task.setCoursetitles(payload.get("coursetitles") == null ? "" : String.valueOf(payload.get("coursetitles")));
            task.setTasktitle(tasktitle);
            task.setTaskcontent(taskcontent);
            task.setDeadline(deadline);
            task.setCompletionstatus("待完成");
            task.setCompletionremark("");
            task.setRecitationaudio("");
            task.setCompletiontime(null);
            task.setKaoshichengji(null);
            task.setRecognizedtext("");
            task.setAiscorecomment("");
            task.setTeachercomment("");
            task.setTeacheraccount(teacheraccount);
            task.setTeachername(teachername);
            task.setReleasetime(releasetime);
            task.setClassname(student.getClassname());
            recitationtaskService.insert(task);
            successCount++;
        }
        return R.ok().put("data", successCount);
    }

    private Date parseDateValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date) value;
        }
        String str = String.valueOf(value).trim();
        if (!StringUtils.hasText(str)) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(str);
        } catch (Exception e) {
            return null;
        }
    }

    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody RecitationtaskEntity recitationtask, HttpServletRequest request) {
        fillAutoReviewResult(recitationtask, request);
        recitationtaskService.updateById(recitationtask);
        return R.ok();
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        recitationtaskService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }

    @RequestMapping("/value/{xColumnName}/{yColumnName}")
    public R value(@PathVariable("yColumnName") String yColumnName, @PathVariable("xColumnName") String xColumnName, HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xColumn", xColumnName);
        params.put("yColumn", yColumnName);
        EntityWrapper<RecitationtaskEntity> ew = new EntityWrapper<RecitationtaskEntity>();
        String tableName = request.getSession().getAttribute("tableName").toString();
        if (tableName.equals("student")) {
            ew.eq("studentaccount", (String) request.getSession().getAttribute("username"));
        }
        applyTeacherScope(ew, request);
        List<Map<String, Object>> result = recitationtaskService.selectValue(params, ew);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Map<String, Object> m : result) {
            for (String k : m.keySet()) {
                if (m.get(k) instanceof Date) {
                    m.put(k, sdf.format((Date) m.get(k)));
                }
            }
        }
        return R.ok().put("data", result);
    }

    @RequestMapping("/group/{columnName}")
    public R group(@PathVariable("columnName") String columnName, HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("column", columnName);
        EntityWrapper<RecitationtaskEntity> ew = new EntityWrapper<RecitationtaskEntity>();
        String tableName = request.getSession().getAttribute("tableName").toString();
        if (tableName.equals("student")) {
            ew.eq("studentaccount", (String) request.getSession().getAttribute("username"));
        }
        applyTeacherScope(ew, request);
        List<Map<String, Object>> result = recitationtaskService.selectGroup(params, ew);
        return R.ok().put("data", result);
    }

    @RequestMapping("/count")
    public R count(@RequestParam Map<String, Object> params, RecitationtaskEntity recitationtask, HttpServletRequest request) {
        String tableName = request.getSession().getAttribute("tableName").toString();
        if (tableName.equals("student")) {
            recitationtask.setStudentaccount((String) request.getSession().getAttribute("username"));
        }
        EntityWrapper<RecitationtaskEntity> ew = new EntityWrapper<RecitationtaskEntity>();
        applyTeacherScope(ew, request);
        int count = recitationtaskService.selectCount(MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, recitationtask), params), params));
        return R.ok().put("data", count);
    }

    private void applyTeacherScope(EntityWrapper<RecitationtaskEntity> ew, HttpServletRequest request) {
        Object tableNameObj = request.getSession().getAttribute("tableName");
        if (tableNameObj == null || !"teacher".equals(String.valueOf(tableNameObj))) {
            return;
        }
        java.util.List<String> classnames = (java.util.List<String>) request.getSession().getAttribute("classnames");
        if (classnames != null && !classnames.isEmpty()) {
            ew.in("classname", classnames);
        }
    }

    private void fillAutoReviewResult(RecitationtaskEntity recitationtask, HttpServletRequest request) {
        System.out.println("[AI评测] ========== 开始AI评测流程 ==========");
        System.out.println("[AI评测] 任务ID: " + recitationtask.getId());
        System.out.println("[AI评测] 完成状态: " + recitationtask.getCompletionstatus());
        System.out.println("[AI评测] 录音文件: " + recitationtask.getRecitationaudio());

        if (!"已完成".equals(recitationtask.getCompletionstatus())) {
            System.out.println("[AI评测] 跳过：完成状态不是'已完成'，当前为 '" + recitationtask.getCompletionstatus() + "'");
            return;
        }
        System.out.println("[AI评测] 状态检查通过 ✓");

        if (!StringUtils.hasText(recitationtask.getRecitationaudio())) {
            System.out.println("[AI评测] 跳过：没有上传录音文件");
            return;
        }
        System.out.println("[AI评测] 录音文件检查通过 ✓");

        // 1. 语音转文字
        File audioFile = resolveAudioFile(recitationtask.getRecitationaudio());
        if (audioFile == null) {
            System.out.println("[AI评测] 失败：解析音频文件返回null");
        } else if (!audioFile.exists()) {
            System.out.println("[AI评测] 失败：音频文件不存在 - " + audioFile.getAbsolutePath());
        } else {
            System.out.println("[AI评测] 音频文件找到 - " + audioFile.getAbsolutePath() + " (" + audioFile.length() + " bytes)");
        }

        String recognizedText = VolcengineSpeechUtil.speechToText(audioFile);
        if (!StringUtils.hasText(recognizedText)) {
            System.out.println("[AI评测] 语音识别结果为空，将跳过AI评分直接进入规则引擎");
        } else {
            System.out.println("[AI评测] 语音识别成功，文本长度: " + recognizedText.length() + " 字");
        }

        // 2. 匹配古诗
        MatchedCourse matchedCourse = matchSpecifiedCourse(recitationtask, recognizedText);
        String expectedText = matchedCourse == null ? "" : matchedCourse.getContent();
        String poemTitle = matchedCourse != null ? matchedCourse.getCoursetitle()
                : (StringUtils.hasText(recitationtask.getCoursetitles()) ? recitationtask.getCoursetitles() : recitationtask.getTasktitle());

        if (matchedCourse != null) {
            System.out.println("[AI评测] 匹配到古诗: 《" + matchedCourse.getCoursetitle() + "》 相似度: " + String.format("%.2f", matchedCourse.getSimilarity()));
        } else {
            System.out.println("[AI评测] 未匹配到古诗，使用任务标题: " + poemTitle);
        }

        // 3. 豆包 AI 多维度评测
        if (StringUtils.hasText(recognizedText)) {
            System.out.println("[AI评测] >>> 调用豆包AI多维度评测...");
            AIRecitationReviewUtil.ReviewResult aiResult =
                    AIRecitationReviewUtil.review(expectedText, recognizedText, poemTitle);
            if (aiResult == null) {
                System.out.println("[AI评测] 豆包AI返回null，API调用可能失败");
            } else if (aiResult.getTotalScore() <= 0) {
                System.out.println("[AI评测] 豆包AI总分<=0，解析可能异常，totalScore=" + aiResult.getTotalScore());
            } else {
                System.out.println("[AI评测] 豆包AI评分成功！总分: " + aiResult.getTotalScore());
                recitationtask.setKaoshichengji(aiResult.getTotalScore());
                recitationtask.setRecognizedtext(recognizedText);
                recitationtask.setAiscorecomment(aiResult.getRawJson());
                System.out.println("[AI评测] ========== AI评测完成(豆包) ==========");
                return;
            }
        }

        // 4. Fallback to Levenshtein-based review
        System.out.println("[AI评测] >>> 使用规则引擎兜底评分...");
        RecitationReviewUtil.ReviewResult reviewResult = RecitationReviewUtil.review(expectedText, recognizedText);
        if (matchedCourse != null) {
            String aiComment = reviewResult.getComment();
            if (StringUtils.hasText(aiComment)) {
                reviewResult.setComment("识别古诗：《" + matchedCourse.getCoursetitle() + "》。AI初评(规则引擎)：" + aiComment);
            } else {
                reviewResult.setComment("识别古诗：《" + matchedCourse.getCoursetitle() + "》。");
            }
        } else if (StringUtils.hasText(recitationtask.getCoursetitles())) {
            reviewResult.setComment("未能从指定古诗中识别出对应篇目。请老师人工确认。AI初评(规则引擎)：" + reviewResult.getComment());
        }
        recitationtask.setKaoshichengji(reviewResult.getScore());
        recitationtask.setRecognizedtext(recognizedText);
        recitationtask.setAiscorecomment(reviewResult.getComment());
        System.out.println("[AI评测] 规则引擎评分: " + reviewResult.getScore() + " 分");
        System.out.println("[AI评测] ========== AI评测完成(规则引擎兜底) ==========");
    }

    private File resolveAudioFile(String audioName) {
        System.out.println("[AI评测] 解析音频文件 - 输入: " + audioName);
        try {
            String fileName = audioName.contains("/") ? audioName.substring(audioName.lastIndexOf("/") + 1) : audioName;
            System.out.println("[AI评测] 提取文件名: " + fileName);

            File basePath = org.springframework.util.ResourceUtils.getFile("classpath:static");
            System.out.println("[AI评测] classpath:static 路径: " + basePath.getAbsolutePath() + " (存在:" + basePath.exists() + ")");
            if (!basePath.exists()) {
                basePath = new File("");
                System.out.println("[AI评测] classpath:static 不存在，回退到当前目录: " + basePath.getAbsolutePath());
            }

            File f = new File(basePath.getAbsolutePath() + "/file/" + fileName);
            System.out.println("[AI评测] 尝试路径1: " + f.getAbsolutePath() + " (存在:" + f.exists() + ")");
            if (f.exists()) return f;

            File fallback = new File("file/" + fileName);
            System.out.println("[AI评测] 尝试路径2(回退): " + fallback.getAbsolutePath() + " (存在:" + fallback.exists() + ")");
            if (fallback.exists()) return fallback;

            System.out.println("[AI评测] 所有路径均未找到文件！路径1=" + f.getAbsolutePath() + " 路径2=" + fallback.getAbsolutePath());
            return f;
        } catch (Exception e) {
            System.out.println("[AI评测] 解析音频文件异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private MatchedCourse matchSpecifiedCourse(RecitationtaskEntity recitationtask, String recognizedText) {
        List<CourseEntity> candidateCourses = loadCandidateCourses(recitationtask);
        if (candidateCourses.isEmpty()) {
            return null;
        }
        if (!StringUtils.hasText(recognizedText)) {
            CourseEntity firstCourse = candidateCourses.get(0);
            return new MatchedCourse(firstCourse.getCoursetitle(), firstCourse.getContent(), 0D);
        }
        String normalizedRecognized = normalizePoemText(recognizedText);
        MatchedCourse bestMatch = null;
        for (CourseEntity course : candidateCourses) {
            if (course == null || !StringUtils.hasText(course.getContent())) {
                continue;
            }
            double score = calculateTextSimilarity(normalizedRecognized, normalizePoemText(course.getContent()));
            if (bestMatch == null || score > bestMatch.getSimilarity()) {
                bestMatch = new MatchedCourse(course.getCoursetitle(), course.getContent(), score);
            }
        }
        return bestMatch;
    }

    private List<CourseEntity> loadCandidateCourses(RecitationtaskEntity recitationtask) {
        Map<String, CourseEntity> courseMap = new LinkedHashMap<String, CourseEntity>();
        if (StringUtils.hasText(recitationtask.getCourseids())) {
            String[] courseIds = recitationtask.getCourseids().split(",");
            for (String courseId : courseIds) {
                String trimmedId = courseId == null ? "" : courseId.trim();
                if (!StringUtils.hasText(trimmedId)) {
                    continue;
                }
                try {
                    CourseEntity course = courseService.selectById(Long.valueOf(trimmedId));
                    if (course != null && course.getId() != null) {
                        courseMap.put("id-" + course.getId(), course);
                    }
                } catch (Exception e) {
                    // ignore malformed ids
                }
            }
        }
        List<String> titleList = collectCandidateTitles(recitationtask);
        for (String title : titleList) {
            List<CourseEntity> matchedList = courseService.selectList(
                    new EntityWrapper<CourseEntity>().eq("coursetitle", title)
            );
            if (matchedList == null || matchedList.isEmpty()) {
                matchedList = courseService.selectList(
                        new EntityWrapper<CourseEntity>().like("coursetitle", title)
                );
            }
            if (matchedList == null) {
                continue;
            }
            for (CourseEntity course : matchedList) {
                if (course != null && course.getId() != null) {
                    courseMap.put("id-" + course.getId(), course);
                }
            }
        }
        if (courseMap.isEmpty() && !titleList.isEmpty()) {
            for (String title : titleList) {
                String onlineContent = AIUitl.getPoemContentByTitle(title);
                if (!StringUtils.hasText(onlineContent)) {
                    continue;
                }
                CourseEntity course = new CourseEntity();
                course.setCoursetitle(title);
                course.setContent(onlineContent);
                courseMap.put("online-" + title, course);
            }
        }
        return new ArrayList<CourseEntity>(courseMap.values());
    }

    private List<String> collectCandidateTitles(RecitationtaskEntity recitationtask) {
        List<String> titleList = parsePoemTitles(recitationtask.getCoursetitles());
        if (!titleList.isEmpty()) {
            return titleList;
        }
        appendTitleIfAbsent(titleList, extractTitleFromTaskTitle(recitationtask.getTasktitle()));
        if (!titleList.isEmpty()) {
            return titleList;
        }
        List<String> contentTitles = parsePoemTitles(extractTitleTextFromTaskContent(recitationtask.getTaskcontent()));
        for (String title : contentTitles) {
            appendTitleIfAbsent(titleList, title);
        }
        return titleList;
    }

    private void appendTitleIfAbsent(List<String> titleList, String title) {
        String normalized = normalizeCourseTitle(title);
        if (StringUtils.hasText(normalized) && !titleList.contains(normalized)) {
            titleList.add(normalized);
        }
    }

    private List<String> parsePoemTitles(String courseTitles) {
        List<String> titleList = new ArrayList<String>();
        if (!StringUtils.hasText(courseTitles)) {
            return titleList;
        }
        String[] titles = courseTitles.split("[,，、\\n]+");
        for (String title : titles) {
            String normalized = normalizeCourseTitle(title);
            if (StringUtils.hasText(normalized) && !titleList.contains(normalized)) {
                titleList.add(normalized);
            }
        }
        return titleList;
    }

    private String extractTitleFromTaskTitle(String taskTitle) {
        if (!StringUtils.hasText(taskTitle)) {
            return "";
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("《([^》]+)》").matcher(taskTitle);
        if (matcher.find()) {
            return matcher.group(1);
        }
        String cleaned = taskTitle
                .replace("背诵", "")
                .replace("朗诵", "")
                .replace("古诗", "")
                .replace("诗词", "")
                .replace("任务", "")
                .trim();
        if (cleaned.startsWith("：") || cleaned.startsWith(":")) {
            cleaned = cleaned.substring(1).trim();
        }
        return cleaned;
    }

    private String extractTitleTextFromTaskContent(String taskContent) {
        if (!StringUtils.hasText(taskContent)) {
            return "";
        }
        String cleaned = taskContent.trim()
                .replace("请完成以下古诗背诵：", "")
                .replace("请完成以下古诗背诵:", "")
                .replace("请背诵以下古诗：", "")
                .replace("请背诵以下古诗:", "")
                .replace("请完成以下背诵：", "")
                .replace("请完成以下背诵:", "")
                .replace("背诵内容：", "")
                .replace("背诵内容:", "")
                .trim();
        if (cleaned.endsWith("。")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
        }
        return cleaned;
    }

    private String normalizeCourseTitle(String title) {
        if (title == null) {
            return "";
        }
        return title.replace("《", "").replace("》", "").trim();
    }

    private String normalizePoemText(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\\s\\p{Punct}，。！？；：“”‘’、《》…（）()【】\\-—_]", "").trim();
    }

    private double calculateTextSimilarity(String source, String target) {
        if (!StringUtils.hasText(source) || !StringUtils.hasText(target)) {
            return 0D;
        }
        int distance = levenshtein(source, target);
        return 1 - (distance * 1.0 / Math.max(source.length(), target.length()));
    }

    private int levenshtein(String source, String target) {
        int[][] dp = new int[source.length() + 1][target.length() + 1];
        for (int i = 0; i <= source.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= target.length(); j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= source.length(); i++) {
            for (int j = 1; j <= target.length(); j++) {
                int cost = source.charAt(i - 1) == target.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[source.length()][target.length()];
    }

    private static class MatchedCourse {
        private final String coursetitle;
        private final String content;
        private final double similarity;

        private MatchedCourse(String coursetitle, String content, double similarity) {
            this.coursetitle = coursetitle;
            this.content = content;
            this.similarity = similarity;
        }

        public String getCoursetitle() {
            return coursetitle;
        }

        public String getContent() {
            return content;
        }

        public double getSimilarity() {
            return similarity;
        }
    }
}
