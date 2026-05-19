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

import com.cl.entity.StudentEntity;
import com.cl.entity.view.StudentView;

import com.cl.service.StudentService;
import com.cl.service.TokenService;
import com.cl.utils.PageUtils;
import com.cl.utils.R;
import com.cl.utils.MPUtil;
import com.cl.utils.CommonUtil;
import java.io.IOException;

/**
 * 用户
 * 后端接口
 * @author 
 * @email 
 */
@RestController
@RequestMapping("/student")
public class StudentController {
    @Autowired
    private StudentService studentService;



    
	@Autowired
	private TokenService tokenService;
	
	/**
	 * 登录
	 */
	@IgnoreAuth
	@RequestMapping(value = "/login")
	public R login(String username, String password, String captcha, HttpServletRequest request) {
		StudentEntity u = studentService.selectOne(new EntityWrapper<StudentEntity>().eq("studentaccount", username));
        if(u==null || !u.getStudentpassword().equals(password)) {
            return R.error("账号或密码不正确");
        }
        if("禁用".equals(u.getPermissionstatus())) {
            return R.error("当前学生账号已被系统管理员禁用");
        }
		String token = tokenService.generateToken(u.getId(), username,"student",  "用户" );
		return R.ok().put("token", token);
	}


	
	/**
     * 注册
     */
	@IgnoreAuth
    @RequestMapping("/register")
    public R register(@RequestBody StudentEntity student){
    	//ValidatorUtils.validateEntity(student);
        fillDefaultClassname(student);
        fillDefaultPermission(student);
    	StudentEntity u = studentService.selectOne(new EntityWrapper<StudentEntity>().eq("studentaccount", student.getStudentaccount()));
		if(u!=null) {
			return R.error("注册用户已存在");
		}
		Long uId = new Date().getTime();
		student.setId(uId);
        studentService.insert(student);
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
        StudentEntity u = studentService.selectById(id);
        return R.ok().put("data", u);
    }
    
    /**
     * 密码重置
     */
    @IgnoreAuth
	@RequestMapping(value = "/resetPass")
    public R resetPass(String username, HttpServletRequest request){
    	StudentEntity u = studentService.selectOne(new EntityWrapper<StudentEntity>().eq("studentaccount", username));
    	if(u==null) {
    		return R.error("账号不存在");
    	}
        u.setStudentpassword("123456");
        studentService.updateById(u);
        return R.ok("密码已重置为：123456");
    }


    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,StudentEntity student,
		HttpServletRequest request){
        EntityWrapper<StudentEntity> ew = new EntityWrapper<StudentEntity>();
		// 教师scope：只看本班学生
		String tableName = (String) request.getSession().getAttribute("tableName");
		if ("teacher".equals(tableName)) {
			java.util.List<String> classnames = (java.util.List<String>) request.getSession().getAttribute("classnames");
			if (classnames != null && !classnames.isEmpty()) ew.in("classname", classnames);
		}

		PageUtils page = studentService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, student), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,StudentEntity student,
		HttpServletRequest request){
        EntityWrapper<StudentEntity> ew = new EntityWrapper<StudentEntity>();
		// 教师scope：只看本班学生
		String tableName = (String) request.getSession().getAttribute("tableName");
		if ("teacher".equals(tableName)) {
			java.util.List<String> classnames = (java.util.List<String>) request.getSession().getAttribute("classnames");
			if (classnames != null && !classnames.isEmpty()) ew.in("classname", classnames);
		}

		PageUtils page = studentService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, student), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( StudentEntity student){
       	EntityWrapper<StudentEntity> ew = new EntityWrapper<StudentEntity>();
      	ew.allEq(MPUtil.allEQMapPre( student, "student"));
        return R.ok().put("data", studentService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(StudentEntity student){
        EntityWrapper< StudentEntity> ew = new EntityWrapper< StudentEntity>();
 		ew.allEq(MPUtil.allEQMapPre( student, "student"));
		StudentView studentView =  studentService.selectView(ew);
		return R.ok("查询用户成功").put("data", studentView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        StudentEntity student = studentService.selectById(id);
		student = studentService.selectView(new EntityWrapper<StudentEntity>().eq("id", id));
        return R.ok().put("data", student);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        StudentEntity student = studentService.selectById(id);
		student = studentService.selectView(new EntityWrapper<StudentEntity>().eq("id", id));
        return R.ok().put("data", student);
    }
    



    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody StudentEntity student, HttpServletRequest request){
        fillDefaultClassname(student);
        fillDefaultPermission(student);
        if(studentService.selectCount(new EntityWrapper<StudentEntity>().eq("studentaccount", student.getStudentaccount()))>0) {
            return R.error("用户账号已存在");
        }
    	student.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(student);
    	StudentEntity u = studentService.selectOne(new EntityWrapper<StudentEntity>().eq("studentaccount", student.getStudentaccount()));
		if(u!=null) {
			return R.error("用户已存在");
		}
		student.setId(new Date().getTime());
        studentService.insert(student);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody StudentEntity student, HttpServletRequest request){
        fillDefaultClassname(student);
        fillDefaultPermission(student);
        if(studentService.selectCount(new EntityWrapper<StudentEntity>().eq("studentaccount", student.getStudentaccount()))>0) {
            return R.error("用户账号已存在");
        }
    	student.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(student);
    	StudentEntity u = studentService.selectOne(new EntityWrapper<StudentEntity>().eq("studentaccount", student.getStudentaccount()));
		if(u!=null) {
			return R.error("用户已存在");
		}
		student.setId(new Date().getTime());
        studentService.insert(student);
        return R.ok();
    }



    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody StudentEntity student, HttpServletRequest request){
        //ValidatorUtils.validateEntity(student);
        fillDefaultClassname(student);
        fillDefaultPermission(student);
        studentService.updateById(student);//全部更新
        return R.ok();
    }

    private void fillDefaultClassname(StudentEntity student) {
        if(student == null) {
            return;
        }
        if(StringUtils.isBlank(student.getClassname()) && StringUtils.isNotBlank(student.getGrade())) {
            student.setClassname(student.getGrade() + "1班");
        }
        if(student.getMedalcount() == null) {
            student.setMedalcount(0);
        }
    }

    private void fillDefaultPermission(StudentEntity student) {
        if(student == null) {
            return;
        }
        if(StringUtils.isBlank(student.getPermissionstatus())) {
            student.setPermissionstatus("启用");
        }
    }



    

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        studentService.deleteBatchIds(Arrays.asList(ids));
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
        EntityWrapper<StudentEntity> ew = new EntityWrapper<StudentEntity>();
        List<Map<String, Object>> result = studentService.selectValue(params, ew);
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
        EntityWrapper<StudentEntity> ew = new EntityWrapper<StudentEntity>();
        for(int i=0;i<yColumnNames.length;i++) {
            params.put("yColumn", yColumnNames[i]);
            List<Map<String, Object>> result = studentService.selectValue(params, ew);
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
        EntityWrapper<StudentEntity> ew = new EntityWrapper<StudentEntity>();
        List<Map<String, Object>> result = studentService.selectTimeStatValue(params, ew);
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
        EntityWrapper<StudentEntity> ew = new EntityWrapper<StudentEntity>();
        for(int i=0;i<yColumnNames.length;i++) {
            params.put("yColumn", yColumnNames[i]);
            List<Map<String, Object>> result = studentService.selectTimeStatValue(params, ew);
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
        EntityWrapper<StudentEntity> ew = new EntityWrapper<StudentEntity>();
        List<Map<String, Object>> result = studentService.selectGroup(params, ew);
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
     * 总数量
     */
    @RequestMapping("/count")
    public R count(@RequestParam Map<String, Object> params,StudentEntity student, HttpServletRequest request){
        EntityWrapper<StudentEntity> ew = new EntityWrapper<StudentEntity>();
        int count = studentService.selectCount(MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, student), params), params));
        return R.ok().put("data", count);
    }


}
