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
import com.cl.entity.view.CoursereserveView;

import com.cl.service.CoursereserveService;
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



    


    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,CoursereserveEntity coursereserve,
		HttpServletRequest request){
		String tableName = request.getSession().getAttribute("tableName").toString();
		if(tableName.equals("student")) {
			coursereserve.setStudentaccount((String)request.getSession().getAttribute("username"));
		}
		if(tableName.equals("teacher")) {
			coursereserve.setTeacheraccount((String)request.getSession().getAttribute("username"));
		}
        EntityWrapper<CoursereserveEntity> ew = new EntityWrapper<CoursereserveEntity>();

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
