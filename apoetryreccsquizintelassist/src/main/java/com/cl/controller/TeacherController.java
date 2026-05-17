package com.cl.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import com.cl.utils.ValidatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.cl.annotation.IgnoreAuth;

import com.cl.dao.StudentScoreLogDao;
import com.cl.entity.StudentScoreLogEntity;
import com.cl.entity.TeacherEntity;
import com.cl.utils.R;
import com.cl.entity.TeacherClassEntity;
import com.cl.entity.StudentEntity;
import com.cl.entity.RecitationtaskEntity;
import com.cl.entity.FollowreadRecordEntity;
import com.cl.entity.view.TeacherView;

import com.cl.service.TeacherService;
import com.cl.service.StudentService;
import com.cl.service.RecitationtaskService;
import com.cl.dao.TeacherClassDao;
import com.cl.dao.FollowreadRecordDao;
import com.cl.dao.QuizRecordDao;
import com.cl.dao.StudentQuizRecordDao;
import com.cl.entity.QuizRecordEntity;
import com.cl.entity.StudentQuizRecordEntity;
import com.cl.service.TokenService;
import com.cl.utils.PageUtils;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import com.cl.utils.R;
import com.cl.utils.MPUtil;
import com.cl.utils.CommonUtil;
import java.io.IOException;

/**
 * 教师
 * 后端接口
 * @author 
 * @email 
 */
@RestController
@RequestMapping("/teacher")
public class TeacherController {
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private RecitationtaskService recitationtaskService;
    @Autowired
    private TeacherClassDao teacherClassDao;
    @Autowired
    private FollowreadRecordDao followreadRecordDao;
    @Autowired
    private QuizRecordDao quizRecordDao;
    @Autowired
    private StudentQuizRecordDao studentQuizRecordDao;
    @Autowired
    private StudentScoreLogDao studentScoreLogDao;



    
	@Autowired
	private TokenService tokenService;
	
	/**
	 * 登录
	 */
	@IgnoreAuth
	@RequestMapping(value = "/login")
	public R login(String username, String password, String captcha, HttpServletRequest request) {
		TeacherEntity u = teacherService.selectOne(new EntityWrapper<TeacherEntity>().eq("teacheraccount", username));
        if(u==null || !u.getTeacherpassword().equals(password)) {
            return R.error("账号或密码不正确");
        }
        if("禁用".equals(u.getPermissionstatus())) {
            return R.error("当前教师账号已被系统管理员禁用");
        }
		String token = tokenService.generateToken(u.getId(), username,"teacher",  "管理员" );
		return R.ok().put("token", token);
	}


	
	/**
     * 注册
     */
	@IgnoreAuth
    @RequestMapping("/register")
    public R register(@RequestBody TeacherEntity teacher){
    	//ValidatorUtils.validateEntity(teacher);
        fillDefaultPermission(teacher);
    	TeacherEntity u = teacherService.selectOne(new EntityWrapper<TeacherEntity>().eq("teacheraccount", teacher.getTeacheraccount()));
		if(u!=null) {
			return R.error("注册用户已存在");
		}
		Long uId = new Date().getTime();
		teacher.setId(uId);
        teacherService.insert(teacher);
        syncTeacherClasses(teacher);
        return R.ok();
    }

	
	/**
	 * 退出
	 */
	@RequestMapping("/logout")
	public R logout(HttpServletRequest request) {
		request.getSession().invalidate();
		return R.ok("退出成功");
	}
	
	/**
     * 获取用户的session用户信息
     */
    @RequestMapping("/session")
    public R getCurrUser(HttpServletRequest request){
    	Long id = (Long)request.getSession().getAttribute("userId");
        TeacherEntity u = teacherService.selectById(id);
        return R.ok().put("data", u);
    }
    
    /**
     * 密码重置
     */
    @IgnoreAuth
	@RequestMapping(value = "/resetPass")
    public R resetPass(String username, HttpServletRequest request){
    	TeacherEntity u = teacherService.selectOne(new EntityWrapper<TeacherEntity>().eq("teacheraccount", username));
    	if(u==null) {
    		return R.error("账号不存在");
    	}
        u.setTeacherpassword("123456");
        teacherService.updateById(u);
        return R.ok("密码已重置为：123456");
    }


    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,TeacherEntity teacher,
		HttpServletRequest request){
        EntityWrapper<TeacherEntity> ew = new EntityWrapper<TeacherEntity>();

		PageUtils page = teacherService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, teacher), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,TeacherEntity teacher, 
		HttpServletRequest request){
        EntityWrapper<TeacherEntity> ew = new EntityWrapper<TeacherEntity>();

		PageUtils page = teacherService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, teacher), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( TeacherEntity teacher){
       	EntityWrapper<TeacherEntity> ew = new EntityWrapper<TeacherEntity>();
      	ew.allEq(MPUtil.allEQMapPre( teacher, "teacher")); 
        return R.ok().put("data", teacherService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(TeacherEntity teacher){
        EntityWrapper< TeacherEntity> ew = new EntityWrapper< TeacherEntity>();
 		ew.allEq(MPUtil.allEQMapPre( teacher, "teacher")); 
		TeacherView teacherView =  teacherService.selectView(ew);
		return R.ok("查询教师成功").put("data", teacherView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        TeacherEntity teacher = teacherService.selectById(id);
		teacher = teacherService.selectView(new EntityWrapper<TeacherEntity>().eq("id", id));
        return R.ok().put("data", teacher);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        TeacherEntity teacher = teacherService.selectById(id);
		teacher = teacherService.selectView(new EntityWrapper<TeacherEntity>().eq("id", id));
        return R.ok().put("data", teacher);
    }
    



    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody TeacherEntity teacher, HttpServletRequest request){
        fillDefaultPermission(teacher);
        if(teacherService.selectCount(new EntityWrapper<TeacherEntity>().eq("teacheraccount", teacher.getTeacheraccount()))>0) {
            return R.error("教师账号已存在");
        }
    	teacher.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(teacher);
    	TeacherEntity u = teacherService.selectOne(new EntityWrapper<TeacherEntity>().eq("teacheraccount", teacher.getTeacheraccount()));
		if(u!=null) {
			return R.error("用户已存在");
		}
		teacher.setId(new Date().getTime());
        teacherService.insert(teacher);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody TeacherEntity teacher, HttpServletRequest request){
        fillDefaultPermission(teacher);
        if(teacherService.selectCount(new EntityWrapper<TeacherEntity>().eq("teacheraccount", teacher.getTeacheraccount()))>0) {
            return R.error("教师账号已存在");
        }
    	teacher.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(teacher);
    	TeacherEntity u = teacherService.selectOne(new EntityWrapper<TeacherEntity>().eq("teacheraccount", teacher.getTeacheraccount()));
		if(u!=null) {
			return R.error("用户已存在");
		}
		teacher.setId(new Date().getTime());
        teacherService.insert(teacher);
        syncTeacherClasses(teacher);
        return R.ok();
    }



    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody TeacherEntity teacher, HttpServletRequest request){
        //ValidatorUtils.validateEntity(teacher);
        fillDefaultPermission(teacher);
        teacherService.updateById(teacher);//全部更新
        syncTeacherClasses(teacher);
        return R.ok();
    }

    private void syncTeacherClasses(TeacherEntity teacher) {
        if (teacher == null || teacher.getTeacheraccount() == null) return;
        // 删除旧绑定
        teacherClassDao.delete(new EntityWrapper<TeacherClassEntity>().eq("teacher_account", teacher.getTeacheraccount()));
        // 解析班级列表并插入新绑定
        String classnameStr = teacher.getClassname();
        if (StringUtils.isNotBlank(classnameStr)) {
            String[] classes = classnameStr.split(",");
            for (String cn : classes) {
                cn = cn.trim();
                if (!cn.isEmpty()) {
                    TeacherClassEntity tc = new TeacherClassEntity();
                    tc.setTeacherId(teacher.getId());
                    tc.setTeacherAccount(teacher.getTeacheraccount());
                    tc.setClassname(cn);
                    teacherClassDao.insert(tc);
                }
            }
        }
    }

    private void fillDefaultPermission(TeacherEntity teacher) {
        if(teacher == null) {
            return;
        }
        if(StringUtils.isBlank(teacher.getPermissionstatus())) {
            teacher.setPermissionstatus("启用");
        }
    }



    

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        teacherService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
    
	





    /**
     * （按值统计）
     */
    @RequestMapping("/value/{xColumnName}/{yColumnName}")
    public R value(@PathVariable("yColumnName") String yColumnName, @PathVariable("xColumnName") String xColumnName,HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xColumn", xColumnName);
        params.put("yColumn", yColumnName);
        EntityWrapper<TeacherEntity> ew = new EntityWrapper<TeacherEntity>();
        List<Map<String, Object>> result = teacherService.selectValue(params, ew);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for(Map<String, Object> m : result) {
            for(String k : m.keySet()) {
                if(m.get(k) instanceof Date) {
                    m.put(k, sdf.format((Date)m.get(k)));
                }
            }
        }
        return R.ok().put("data", result);
    }

    /**
     * （按值统计(多)）
     */
    @RequestMapping("/valueMul/{xColumnName}")
    public R valueMul(@PathVariable("xColumnName") String xColumnName,@RequestParam String yColumnNameMul, HttpServletRequest request) {
        String[] yColumnNames = yColumnNameMul.split(",");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xColumn", xColumnName);
        List<List<Map<String, Object>>> result2 = new ArrayList<List<Map<String,Object>>>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        EntityWrapper<TeacherEntity> ew = new EntityWrapper<TeacherEntity>();
        for(int i=0;i<yColumnNames.length;i++) {
            params.put("yColumn", yColumnNames[i]);
            List<Map<String, Object>> result = teacherService.selectValue(params, ew);
            for(Map<String, Object> m : result) {
                for(String k : m.keySet()) {
                    if(m.get(k) instanceof Date) {
                        m.put(k, sdf.format((Date)m.get(k)));
                    }
                }
            }
            result2.add(result);
        }
        return R.ok().put("data", result2);
    }

    /**
     * （按值统计）时间统计类型
     */
    @RequestMapping("/value/{xColumnName}/{yColumnName}/{timeStatType}")
    public R valueDay(@PathVariable("yColumnName") String yColumnName, @PathVariable("xColumnName") String xColumnName, @PathVariable("timeStatType") String timeStatType,HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xColumn", xColumnName);
        params.put("yColumn", yColumnName);
        params.put("timeStatType", timeStatType);
        EntityWrapper<TeacherEntity> ew = new EntityWrapper<TeacherEntity>();
        List<Map<String, Object>> result = teacherService.selectTimeStatValue(params, ew);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for(Map<String, Object> m : result) {
            for(String k : m.keySet()) {
                if(m.get(k) instanceof Date) {
                    m.put(k, sdf.format((Date)m.get(k)));
                }
            }
        }
        return R.ok().put("data", result);
    }

    /**
     * （按值统计）时间统计类型(多)
     */
    @RequestMapping("/valueMul/{xColumnName}/{timeStatType}")
    public R valueMulDay(@PathVariable("xColumnName") String xColumnName, @PathVariable("timeStatType") String timeStatType,@RequestParam String yColumnNameMul,HttpServletRequest request) {
        String[] yColumnNames = yColumnNameMul.split(",");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xColumn", xColumnName);
        params.put("timeStatType", timeStatType);
        List<List<Map<String, Object>>> result2 = new ArrayList<List<Map<String,Object>>>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        EntityWrapper<TeacherEntity> ew = new EntityWrapper<TeacherEntity>();
        for(int i=0;i<yColumnNames.length;i++) {
            params.put("yColumn", yColumnNames[i]);
            List<Map<String, Object>> result = teacherService.selectTimeStatValue(params, ew);
            for(Map<String, Object> m : result) {
                for(String k : m.keySet()) {
                    if(m.get(k) instanceof Date) {
                        m.put(k, sdf.format((Date)m.get(k)));
                    }
                }
            }
            result2.add(result);
        }
        return R.ok().put("data", result2);
    }

    /**
     * 分组统计
     */
    @RequestMapping("/group/{columnName}")
    public R group(@PathVariable("columnName") String columnName,HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("column", columnName);
        EntityWrapper<TeacherEntity> ew = new EntityWrapper<TeacherEntity>();
        List<Map<String, Object>> result = teacherService.selectGroup(params, ew);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for(Map<String, Object> m : result) {
            for(String k : m.keySet()) {
                if(m.get(k) instanceof Date) {
                    m.put(k, sdf.format((Date)m.get(k)));
                }
            }
        }
        return R.ok().put("data", result);
    }

    /** 教师学情仪表盘 */
    @SuppressWarnings("unchecked")
    @RequestMapping("/dashboard")
    public R dashboard(HttpServletRequest request) {
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        if (!"teacher".equals(tableName)) return R.error("仅教师可访问");
        String username = (String) request.getSession().getAttribute("username");
        String classname = (String) request.getSession().getAttribute("classname");
        String grade = (String) request.getSession().getAttribute("grade");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("teachername", username);
        data.put("grade", grade != null ? grade : "");
        data.put("classname", classname != null ? classname : "");

        // 本班学生数
        EntityWrapper<StudentEntity> se = new EntityWrapper<>();
        if (classname != null) se.eq("classname", classname);
        data.put("studentCount", studentService.selectCount(se));

        // 背诵任务统计
        EntityWrapper<RecitationtaskEntity> re = new EntityWrapper<>();
        re.eq("teacheraccount", username);
        int recitationTotal = recitationtaskService.selectCount(re);
        EntityWrapper<RecitationtaskEntity> reDone = new EntityWrapper<>();
        reDone.eq("teacheraccount", username).eq("completionstatus", "已完成");
        int recitationDone = recitationtaskService.selectCount(reDone);
        data.put("recitationTotal", recitationTotal);
        data.put("recitationDone", recitationDone);

        // 测验统计
        EntityWrapper<RecitationtaskEntity> qe = new EntityWrapper<>();
        qe.eq("teacheraccount", username).like("tasktitle", "测验：");
        int quizTotal = recitationtaskService.selectCount(qe);
        EntityWrapper<RecitationtaskEntity> qeDone = new EntityWrapper<>();
        qeDone.eq("teacheraccount", username).like("tasktitle", "测验：").eq("completionstatus", "已完成");
        int quizDone = recitationtaskService.selectCount(qeDone);
        data.put("quizTotal", quizTotal);
        data.put("quizDone", quizDone);

        // 跟读记录统计
        EntityWrapper<FollowreadRecordEntity> fe = new EntityWrapper<>();
        if (classname != null) {
            // 先查本班学生账号
            List<StudentEntity> students = studentService.selectList(new EntityWrapper<StudentEntity>().eq("classname", classname));
            if (!students.isEmpty()) {
                List<String> accounts = students.stream().map(StudentEntity::getStudentaccount).collect(java.util.stream.Collectors.toList());
                fe.in("studentaccount", accounts);
            }
        }
        int followTotal = followreadRecordDao.selectCount(fe);
        data.put("followTotal", followTotal);
        if (followTotal > 0) {
            List<FollowreadRecordEntity> records = followreadRecordDao.selectList(fe);
            double avgScore = records.stream().mapToInt(FollowreadRecordEntity::getTotalscore).average().orElse(0);
            data.put("followAvgScore", Math.round(avgScore * 10.0) / 10.0);
        } else {
            data.put("followAvgScore", 0);
        }

        // 最近5条活动
        List<Map<String, Object>> activities = new ArrayList<>();
        EntityWrapper<RecitationtaskEntity> recentTasks = new EntityWrapper<>();
        recentTasks.eq("teacheraccount", username).orderBy("addtime", false).last("LIMIT 5");
        for (RecitationtaskEntity t : recitationtaskService.selectList(recentTasks)) {
            Map<String, Object> act = new LinkedHashMap<>();
            act.put("type", (t.getTaskType() != null && t.getTaskType() == 2) ? "测验" : "背诵");
            act.put("student", t.getStudentname());
            act.put("title", t.getTasktitle());
            act.put("status", t.getCompletionstatus());
            act.put("score", t.getKaoshichengji());
            act.put("time", t.getAddtime());
            activities.add(act);
        }
        data.put("recentActivities", activities);

        return R.ok().put("data", data);
    }

    /** 学生错题本 — 教师查看指定学生的错题汇总 */
    @RequestMapping("/studentWrongbook")
    public R studentWrongbook(@RequestParam String studentaccount, HttpServletRequest request) {
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        if (!"teacher".equals(tableName)) return R.error("仅教师可访问");

        List<Map<String, Object>> wrongList = new ArrayList<>();

        // 测验错题 (StudentQuizRecord)
        EntityWrapper<StudentQuizRecordEntity> qe = new EntityWrapper<>();
        qe.eq("studentaccount", studentaccount).isNotNull("wrong_list_json").ne("wrong_list_json", "[]").orderBy("addtime", false);
        for (StudentQuizRecordEntity r : studentQuizRecordDao.selectList(qe)) {
            try {
                org.json.JSONArray arr = new org.json.JSONArray(r.getWrongListJson());
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject w = arr.getJSONObject(i);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("type", "测验");
                    item.put("poemTitle", r.getCourseTitle());
                    item.put("question", w.optString("question"));
                    item.put("options", w.optJSONArray("options") != null ? w.optJSONArray("options").toList() : new ArrayList<>());
                    item.put("answer", w.optInt("answer"));
                    item.put("selected", w.optInt("selected"));
                    item.put("analysis", w.optString("analysis"));
                    item.put("time", r.getAddtime());
                    wrongList.add(item);
                }
            } catch (Exception e) {}
        }

        // 飞花令/其他错题 (QuizRecord)
        EntityWrapper<QuizRecordEntity> qr = new EntityWrapper<>();
        qr.eq("studentaccount", studentaccount).isNotNull("wrong_list_json").ne("wrong_list_json", "[]").orderBy("addtime", false);
        for (QuizRecordEntity r : quizRecordDao.selectList(qr)) {
            try {
                org.json.JSONArray arr = new org.json.JSONArray(r.getWrongListJson());
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject w = arr.getJSONObject(i);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("type", "练习");
                    item.put("poemTitle", r.getCoursetitle());
                    item.put("question", w.optString("question"));
                    item.put("options", w.optJSONArray("options") != null ? w.optJSONArray("options").toList() : new ArrayList<>());
                    item.put("answer", w.optInt("answer"));
                    item.put("selected", w.optInt("selected"));
                    item.put("analysis", w.optString("analysis"));
                    item.put("time", r.getAddtime());
                    wrongList.add(item);
                }
            } catch (Exception e) {}
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("studentaccount", studentaccount);
        result.put("totalWrong", wrongList.size());
        result.put("wrongList", wrongList);
        return R.ok().put("data", result);
    }




    /** 一次性迁移: 旧表记录 → student_score_log */
    @RequestMapping("/migrateScoreLog")
    public R migrateScoreLog() {
        int count = 0;
        // 迁移跟读记录
        EntityWrapper<FollowreadRecordEntity> fw = new EntityWrapper<>();
        List<FollowreadRecordEntity> followList = followreadRecordDao.selectList(fw);
        for (FollowreadRecordEntity r : followList) {
            try {
                // 跳过已迁移的
                EntityWrapper<StudentScoreLogEntity> ck = new EntityWrapper<>();
                ck.eq("studentaccount", r.getStudentaccount()).eq("poetry_id", r.getCourseid())
                  .eq("source_type", 4).last("LIMIT 1");
                if (studentScoreLogDao.selectCount(ck) > 0) continue;
                StudentScoreLogEntity log = new StudentScoreLogEntity();
                log.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
                log.setStudentaccount(r.getStudentaccount());
                log.setStudentname(r.getStudentname());
                log.setClassname(r.getClassname());
                log.setPoetryId(r.getCourseid());
                log.setPoetryTitle(r.getCoursetitle());
                log.setSourceType(4);
                log.setScore(r.getTotalscore() != null ? r.getTotalscore() : 0);
                log.setReportJson(r.getReportjson());
                log.setCreateTime(r.getAddtime());
                // 解析维度分数
                parseDimensions(log, r.getReportjson());
                studentScoreLogDao.insert(log);
                count++;
            } catch (Exception e) {}
        }
        // 迁移测验记录
        EntityWrapper<QuizRecordEntity> qw = new EntityWrapper<>();
        List<QuizRecordEntity> quizList = quizRecordDao.selectList(qw);
        for (QuizRecordEntity r : quizList) {
            try {
                EntityWrapper<StudentScoreLogEntity> ck = new EntityWrapper<>();
                ck.eq("studentaccount", r.getStudentaccount()).eq("poetry_id", r.getCourseid())
                  .eq("source_type", 6).last("LIMIT 1");
                if (studentScoreLogDao.selectCount(ck) > 0) continue;
                StudentScoreLogEntity log = new StudentScoreLogEntity();
                log.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
                log.setStudentaccount(r.getStudentaccount());
                log.setStudentname(r.getStudentname());
                log.setClassname(null);
                // 从学生表查 classname
                try {
                    StudentEntity stu = studentService.selectList(new EntityWrapper<StudentEntity>().eq("studentaccount", r.getStudentaccount()).last("LIMIT 1")).stream().findFirst().orElse(null);
                    if (stu != null && StringUtils.isNotBlank(stu.getClassname())) log.setClassname(stu.getClassname());
                } catch (Exception e) {}
                log.setPoetryId(r.getCourseid());
                log.setPoetryTitle(r.getCoursetitle());
                log.setSourceType(6);
                int s = r.getScore() != null ? r.getScore() : 0;
                log.setScore(s);
                log.setKnowledgeScore(s); log.setAccuracyScore(s); log.setDepthScore(s);
                log.setLearningSuggestion(s >= 80 ? "表现优秀，继续保持！" : s >= 60 ? "还有提升空间。" : "需要加强基础。");
                log.setOverallSummary("答对" + (r.getCorrectCount() != null ? r.getCorrectCount() : 0) + "/" + (r.getQuestionsCount() != null ? r.getQuestionsCount() : 0) + "题，得分" + s + "分。");
                log.setCreateTime(r.getAddtime());
                studentScoreLogDao.insert(log);
                count++;
            } catch (Exception e) {}
        }
        return R.ok().put("data", "迁移完成，共 " + count + " 条");
    }

    private void parseDimensions(StudentScoreLogEntity log, String reportJson) {
        if (StringUtils.isBlank(reportJson)) return;
        try {
            org.json.JSONObject obj = new org.json.JSONObject(reportJson);
            if (obj.has("dimensions")) {
                org.json.JSONArray dims = obj.getJSONArray("dimensions");
                for (int i = 0; i < dims.length(); i++) {
                    org.json.JSONObject d = dims.getJSONObject(i);
                    String name = d.optString("name", "");
                    int ds = d.optInt("score", 0);
                    if (name.contains("知识掌握")) log.setKnowledgeScore(ds);
                    else if (name.contains("答题准确")) log.setAccuracyScore(ds);
                    else if (name.contains("理解深度")) log.setDepthScore(ds);
                }
            }
            if (obj.has("suggestion")) log.setLearningSuggestion(obj.optString("suggestion", ""));
            if (obj.has("overallComment")) log.setOverallSummary(obj.optString("overallComment", ""));
        } catch (Exception e) {}
        // 如果标准维度未匹配到，用总分均分
        int fallback = log.getScore() != null ? log.getScore() : 0;
        if (log.getKnowledgeScore() == null || log.getKnowledgeScore() == 0)
            log.setKnowledgeScore(fallback);
        if (log.getAccuracyScore() == null || log.getAccuracyScore() == 0)
            log.setAccuracyScore(fallback);
        if (log.getDepthScore() == null || log.getDepthScore() == 0)
            log.setDepthScore(fallback);
    }

    /** 自主学习管理 — 按学生聚合大盘 */
    @RequestMapping("/autonomousStudents")
    public R autonomousStudents(HttpServletRequest request) {
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        if (!"teacher".equals(tableName)) return R.error("仅教师可访问");
        java.util.List<String> classnames = (java.util.List<String>) request.getSession().getAttribute("classnames");

        EntityWrapper<StudentScoreLogEntity> ew = new EntityWrapper<>();
        if (classnames != null && !classnames.isEmpty()) ew.in("classname", classnames);
        List<StudentScoreLogEntity> all = studentScoreLogDao.selectList(ew);

        // 按 studentaccount 聚合
        Map<String, Map<String, Object>> agg = new LinkedHashMap<>();
        for (StudentScoreLogEntity log : all) {
            String key = log.getStudentaccount();
            agg.computeIfAbsent(key, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("studentaccount", log.getStudentaccount());
                m.put("studentname", log.getStudentname());
                m.put("classname", log.getClassname());
                m.put("followCount", 0);
                m.put("quizCount", 0);
                m.put("analogyCount", 0);
                m.put("reviewCount", 0);
                m.put("lastActiveTime", null);
                return m;
            });
            Map<String, Object> row = agg.get(key);
            if (log.getSourceType() != null) {
                if (log.getSourceType() == 4) row.put("followCount", (int) row.get("followCount") + 1);
                else if (log.getSourceType() == 6) row.put("quizCount", (int) row.get("quizCount") + 1);
                else if (log.getSourceType() == 7) row.put("analogyCount", (int) row.get("analogyCount") + 1);
                else if (log.getSourceType() == 8) row.put("reviewCount", (int) row.get("reviewCount") + 1);
            }
            if (log.getCreateTime() != null) {
                Date cur = (Date) row.get("lastActiveTime");
                if (cur == null || log.getCreateTime().after(cur)) row.put("lastActiveTime", log.getCreateTime());
            }
        }
        List<Map<String, Object>> list = new ArrayList<>(agg.values());
        list.sort((a, b) -> {
            Date da = (Date) a.get("lastActiveTime"), db = (Date) b.get("lastActiveTime");
            if (da == null && db == null) return 0;
            if (da == null) return 1; if (db == null) return -1;
            return db.compareTo(da);
        });
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("totalFollow", all.stream().filter(l -> l.getSourceType() != null && l.getSourceType() == 4).count());
        result.put("totalQuiz", all.stream().filter(l -> l.getSourceType() != null && l.getSourceType() == 6).count());
        result.put("totalAnalogy", all.stream().filter(l -> l.getSourceType() != null && l.getSourceType() == 7).count());
        result.put("totalReview", all.stream().filter(l -> l.getSourceType() != null && l.getSourceType() == 8).count());
        result.put("totalAll", all.size());
        return R.ok().put("data", result);
    }

    /** 自主学习管理 — 单人全量历史 */
    @RequestMapping("/autonomousHistory")
    public R autonomousHistory(@RequestParam String studentaccount, @RequestParam Integer sourceType,
                               HttpServletRequest request) {
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        if (!"teacher".equals(tableName)) return R.error("仅教师可访问");
        EntityWrapper<StudentScoreLogEntity> ew = new EntityWrapper<>();
        ew.eq("studentaccount", studentaccount).eq("source_type", sourceType)
          .orderBy("create_time", false);
        return R.ok().put("data", studentScoreLogDao.selectList(ew));
    }

    /**
     * 总数量
     */
    @RequestMapping("/count")
    public R count(@RequestParam Map<String, Object> params,TeacherEntity teacher, HttpServletRequest request){
        EntityWrapper<TeacherEntity> ew = new EntityWrapper<TeacherEntity>();
        int count = teacherService.selectCount(MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, teacher), params), params));
        return R.ok().put("data", count);
    }


}
