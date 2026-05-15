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

import com.cl.entity.TeacherEntity;
import com.cl.entity.StudentEntity;
import com.cl.entity.RecitationtaskEntity;
import com.cl.entity.FollowreadRecordEntity;
import com.cl.entity.view.TeacherView;

import com.cl.service.TeacherService;
import com.cl.service.StudentService;
import com.cl.service.RecitationtaskService;
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
    private FollowreadRecordDao followreadRecordDao;
    @Autowired
    private QuizRecordDao quizRecordDao;
    @Autowired
    private StudentQuizRecordDao studentQuizRecordDao;



    
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
        return R.ok();
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
