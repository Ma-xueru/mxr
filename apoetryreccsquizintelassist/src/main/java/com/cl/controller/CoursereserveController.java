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

import com.cl.entity.CoursereserveEntity;
import com.cl.entity.StudentEntity;
import com.cl.entity.TeacherEntity;
import com.cl.entity.view.CoursereserveView;

import com.cl.service.CoursereserveService;
import com.cl.service.StudentService;
import com.cl.service.TeacherService;
import com.cl.service.TokenService;
import com.cl.utils.PageUtils;
import com.cl.utils.R;
import com.cl.utils.MPUtil;
import com.cl.utils.CommonUtil;
import java.io.IOException;

/**
 * 预约课程
 * 后端接口
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
@RestController
@RequestMapping("/coursereserve")
public class CoursereserveController {
    @Autowired
    private CoursereserveService coursereserveService;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private StudentService studentService;



    


    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,CoursereserveEntity coursereserve,
		HttpServletRequest request){
        EntityWrapper<CoursereserveEntity> ew = new EntityWrapper<CoursereserveEntity>();
		Object tableNameObj = request.getSession().getAttribute("tableName");
		Object usernameObj = request.getSession().getAttribute("username");
		String tableName = tableNameObj == null ? "" : String.valueOf(tableNameObj);
		String username = usernameObj == null ? "" : String.valueOf(usernameObj);
		if("student".equals(tableName) && StringUtils.isNotBlank(username)) {
			ew.eq("studentaccount", username);
		}
		if("teacher".equals(tableName) && StringUtils.isNotBlank(username)) {
			TeacherEntity loginTeacher = teacherService.selectOne(
					new EntityWrapper<TeacherEntity>().eq("teacheraccount", username)
			);
			if(loginTeacher != null && StringUtils.isNotBlank(loginTeacher.getTeachername())) {
				ew.andNew()
						.eq("teacheraccount", username)
						.or()
						.eq("teachername", loginTeacher.getTeachername());
			} else {
				ew.eq("teacheraccount", username);
			}
		}

		PageUtils page = coursereserveService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, coursereserve), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,CoursereserveEntity coursereserve, 
		HttpServletRequest request){
        EntityWrapper<CoursereserveEntity> ew = new EntityWrapper<CoursereserveEntity>();

		PageUtils page = coursereserveService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, coursereserve), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( CoursereserveEntity coursereserve){
       	EntityWrapper<CoursereserveEntity> ew = new EntityWrapper<CoursereserveEntity>();
      	ew.allEq(MPUtil.allEQMapPre( coursereserve, "coursereserve")); 
        return R.ok().put("data", coursereserveService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(CoursereserveEntity coursereserve){
        EntityWrapper< CoursereserveEntity> ew = new EntityWrapper< CoursereserveEntity>();
 		ew.allEq(MPUtil.allEQMapPre( coursereserve, "coursereserve")); 
		CoursereserveView coursereserveView =  coursereserveService.selectView(ew);
		return R.ok("查询预约课程成功").put("data", coursereserveView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        CoursereserveEntity coursereserve = coursereserveService.selectById(id);
		coursereserve = coursereserveService.selectView(new EntityWrapper<CoursereserveEntity>().eq("id", id));
        return R.ok().put("data", coursereserve);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        CoursereserveEntity coursereserve = coursereserveService.selectById(id);
		coursereserve = coursereserveService.selectView(new EntityWrapper<CoursereserveEntity>().eq("id", id));
        return R.ok().put("data", coursereserve);
    }
    



    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CoursereserveEntity coursereserve, HttpServletRequest request){
    	coursereserve.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(coursereserve);
        coursereserveService.insert(coursereserve);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody CoursereserveEntity coursereserve, HttpServletRequest request){
    	coursereserve.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(coursereserve);
        coursereserveService.insert(coursereserve);
        return R.ok();
    }

    /**
     * 学生预约教师（带业务校验）
     */
    @RequestMapping("/book")
    @Transactional
    public R book(@RequestBody CoursereserveEntity coursereserve, HttpServletRequest request){
        Object tableNameObj = request.getSession().getAttribute("tableName");
        Object usernameObj = request.getSession().getAttribute("username");
        if(tableNameObj == null || usernameObj == null) {
            return R.error("请先登录后再预约");
        }
        String tableName = String.valueOf(tableNameObj);
        if(!"student".equals(tableName)) {
            return R.error("仅学生可发起预约");
        }

        if(coursereserve.getReservetime() == null) {
            return R.error("预约时间不能为空");
        }
        if(StringUtils.isBlank(coursereserve.getTeacheraccount())) {
            return R.error("请选择预约教师");
        }
        Integer reserveCount = 1;
        if(StringUtils.isNotBlank(coursereserve.getReservecount())) {
            try {
                reserveCount = Integer.parseInt(coursereserve.getReservecount());
            } catch (Exception e) {
                return R.error("预约人数格式不正确");
            }
        }
        if(reserveCount <= 0) {
            return R.error("预约人数必须大于0");
        }

        String studentAccount = String.valueOf(usernameObj);
        StudentEntity student = studentService.selectOne(
                new EntityWrapper<StudentEntity>().eq("studentaccount", studentAccount)
        );
        if(student == null) {
            return R.error("未找到当前学生信息");
        }
        coursereserve.setStudentaccount(studentAccount);
        if(StringUtils.isBlank(coursereserve.getStudentname())) {
            coursereserve.setStudentname(student.getStudentname());
        }

        TeacherEntity teacher = teacherService.selectOne(
                new EntityWrapper<TeacherEntity>().eq("teacheraccount", coursereserve.getTeacheraccount())
        );
        if(teacher == null) {
            return R.error("教师不存在");
        }
        if("禁用".equals(teacher.getPermissionstatus())) {
            return R.error("当前教师不可预约");
        }
        coursereserve.setTeachername(teacher.getTeachername());

        int duplicateCount = coursereserveService.selectCount(
                new EntityWrapper<CoursereserveEntity>()
                        .eq("studentaccount", studentAccount)
                        .eq("teacheraccount", coursereserve.getTeacheraccount())
                        .eq("reservetime", coursereserve.getReservetime())
                        .ne("reservestatus", "已取消")
                        .ne("reservestatus", "已拒绝")
        );
        if(duplicateCount > 0) {
            return R.error("同一时间已预约该教师，请勿重复预约");
        }

        int usedCount = 0;
        List<CoursereserveEntity> sameSlotList = coursereserveService.selectList(
                new EntityWrapper<CoursereserveEntity>()
                        .eq("teacheraccount", coursereserve.getTeacheraccount())
                        .eq("reservetime", coursereserve.getReservetime())
                        .ne("reservestatus", "已取消")
                        .ne("reservestatus", "已拒绝")
        );
        for(CoursereserveEntity item : sameSlotList) {
            try {
                usedCount += Integer.parseInt(StringUtils.defaultIfBlank(item.getReservecount(), "1"));
            } catch (Exception e) {
                usedCount += 1;
            }
        }
        int teacherCapacity = teacher.getReservecount() == null ? 0 : teacher.getReservecount();
        if(teacherCapacity > 0 && (usedCount + reserveCount > teacherCapacity)) {
            return R.error("该时段教师可预约人数不足");
        }

        coursereserve.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
        coursereserve.setSfsh("待审核");
        if(StringUtils.isBlank(coursereserve.getReservestatus())) {
            coursereserve.setReservestatus("待确认");
        }
        coursereserve.setReservecount(String.valueOf(reserveCount));
        coursereserveService.insert(coursereserve);
        return R.ok();
    }



    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody CoursereserveEntity coursereserve, HttpServletRequest request){
        //ValidatorUtils.validateEntity(coursereserve);
        coursereserveService.updateById(coursereserve);//全部更新
        return R.ok();
    }

    /**
     * 审核
     */
    @RequestMapping("/shBatch")
    @Transactional
    public R update(@RequestBody Long[] ids, @RequestParam String sfsh, @RequestParam String shhf){
        List<CoursereserveEntity> list = new ArrayList<CoursereserveEntity>();
        for(Long id : ids) {
            CoursereserveEntity coursereserve = coursereserveService.selectById(id);
            coursereserve.setSfsh(sfsh);
            coursereserve.setShhf(shhf);
            list.add(coursereserve);
        }
        coursereserveService.updateBatchById(list);
        return R.ok();
    }


    

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        coursereserveService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
    
	








}
