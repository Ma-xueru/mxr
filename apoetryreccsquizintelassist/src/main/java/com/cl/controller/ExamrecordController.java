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

import com.cl.entity.ExamrecordEntity;
import com.cl.entity.MystudentEntity;
import com.cl.entity.StudentEntity;
import com.cl.entity.view.ExamrecordView;

import com.cl.service.ExamrecordService;
import com.cl.service.MystudentService;
import com.cl.service.StudentService;
import com.cl.service.TokenService;
import com.cl.utils.PageUtils;
import com.cl.utils.R;
import com.cl.utils.MPUtil;
import com.cl.utils.CommonUtil;
import java.io.IOException;

/**
 * 测试记录
 * 后端接口
 * @author 
 * @email 
 */
@RestController
@RequestMapping("/examrecord")
public class ExamrecordController {
    @Autowired
    private ExamrecordService examrecordService;
    @Autowired
    private MystudentService mystudentService;
    @Autowired
    private StudentService studentService;

    private String getSessionTableName(HttpServletRequest request) {
        Object tableName = request.getSession().getAttribute("tableName");
        return tableName == null ? "" : tableName.toString();
    }

    private void applyUserScope(EntityWrapper<ExamrecordEntity> ew, HttpServletRequest request) {
        String tableName = getSessionTableName(request);
        if("student".equals(tableName)) {
            ew.eq("userid", (Long) request.getSession().getAttribute("userId"));
            return;
        }
        if(!"teacher".equals(tableName)) {
            return;
        }
        String teacherAccount = (String) request.getSession().getAttribute("username");
        List<MystudentEntity> relations = mystudentService.selectList(
            new EntityWrapper<MystudentEntity>().eq("teacheraccount", teacherAccount)
        );
        List<String> studentAccounts = new ArrayList<String>();
        List<String> studentNames = new ArrayList<String>();
        for (MystudentEntity relation : relations) {
            if(relation != null && StringUtils.isNotBlank(relation.getStudentaccount())) {
                studentAccounts.add(relation.getStudentaccount());
            }
            if(relation != null && StringUtils.isNotBlank(relation.getStudentname())) {
                studentNames.add(relation.getStudentname());
            }
        }
        if(studentAccounts.isEmpty() && studentNames.isEmpty()) {
            ew.eq("userid", -1L);
            return;
        }
        EntityWrapper<StudentEntity> studentWrapper = new EntityWrapper<StudentEntity>();
        if(!studentAccounts.isEmpty() && !studentNames.isEmpty()) {
            studentWrapper.andNew();
            studentWrapper.in("studentaccount", studentAccounts).or().in("studentname", studentNames);
        } else if(!studentAccounts.isEmpty()) {
            studentWrapper.in("studentaccount", studentAccounts);
        } else {
            studentWrapper.in("studentname", studentNames);
        }
        List<StudentEntity> students = studentService.selectList(studentWrapper);
        List<Long> userIds = new ArrayList<Long>();
        for (StudentEntity student : students) {
            if(student != null && student.getId() != null) {
                userIds.add(student.getId());
            }
        }
        if(userIds.isEmpty()) {
            ew.eq("userid", -1L);
            return;
        }
        ew.in("userid", userIds);
    }



    

   	    /**
     * 测试记录接口
     */
    @RequestMapping("/groupby")
    public R page2(@RequestParam Map<String, Object> params,ExamrecordEntity examrecord, HttpServletRequest request){
        EntityWrapper<ExamrecordEntity> ew = new EntityWrapper<ExamrecordEntity>();
        applyUserScope(ew, request);
		PageUtils page = examrecordService.queryPageGroupBy(params, MPUtil.between(MPUtil.likeOrEq(ew, examrecord), params));
        return R.ok().put("data", page);
    }

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,ExamrecordEntity examrecord,
		HttpServletRequest request){
        EntityWrapper<ExamrecordEntity> ew = new EntityWrapper<ExamrecordEntity>();
        applyUserScope(ew, request);

		PageUtils page = examrecordService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, examrecord), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,ExamrecordEntity examrecord, 
		HttpServletRequest request){
        EntityWrapper<ExamrecordEntity> ew = new EntityWrapper<ExamrecordEntity>();
        applyUserScope(ew, request);

		PageUtils page = examrecordService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, examrecord), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( ExamrecordEntity examrecord){
       	EntityWrapper<ExamrecordEntity> ew = new EntityWrapper<ExamrecordEntity>();
      	ew.allEq(MPUtil.allEQMapPre( examrecord, "examrecord")); 
        return R.ok().put("data", examrecordService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(ExamrecordEntity examrecord){
        EntityWrapper< ExamrecordEntity> ew = new EntityWrapper< ExamrecordEntity>();
 		ew.allEq(MPUtil.allEQMapPre( examrecord, "examrecord")); 
		ExamrecordView examrecordView =  examrecordService.selectView(ew);
		return R.ok("查询测试记录成功").put("data", examrecordView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        ExamrecordEntity examrecord = examrecordService.selectById(id);
		examrecord = examrecordService.selectView(new EntityWrapper<ExamrecordEntity>().eq("id", id));
        return R.ok().put("data", examrecord);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        ExamrecordEntity examrecord = examrecordService.selectById(id);
		examrecord = examrecordService.selectView(new EntityWrapper<ExamrecordEntity>().eq("id", id));
        return R.ok().put("data", examrecord);
    }
    



    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody ExamrecordEntity examrecord, HttpServletRequest request){
    	examrecord.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(examrecord);
    	examrecord.setUserid((Long)request.getSession().getAttribute("userId"));
        examrecordService.insert(examrecord);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody ExamrecordEntity examrecord, HttpServletRequest request){
    	examrecord.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(examrecord);
    	examrecord.setUserid((Long)request.getSession().getAttribute("userId"));
        examrecordService.insert(examrecord);
        return R.ok();
    }



    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody ExamrecordEntity examrecord, HttpServletRequest request){
        //ValidatorUtils.validateEntity(examrecord);
        examrecordService.updateById(examrecord);//全部更新
        return R.ok();
    }



    

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        examrecordService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
    
	


    /**
     * 当重新练习时，删除考生的某个古诗词测试的所有测试记录
     */
    @RequestMapping("/deleteRecords")
    public R deleteRecords(@RequestParam Long userid,@RequestParam Long paperid){
    	examrecordService.delete(new EntityWrapper<ExamrecordEntity>().eq("paperid", paperid).eq("userid", userid));
        return R.ok();
    }






}
