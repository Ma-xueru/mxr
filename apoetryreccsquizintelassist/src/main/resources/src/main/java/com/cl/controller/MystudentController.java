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

import com.cl.entity.MystudentEntity;
import com.cl.entity.view.MystudentView;

import com.cl.service.MystudentService;
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
 * @date 2026-01-25 11:35:29
 */
@RestController
@RequestMapping("/mystudent")
public class MystudentController {
    @Autowired
    private MystudentService mystudentService;



    


    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,MystudentEntity mystudent,
		HttpServletRequest request){
		String tableName = request.getSession().getAttribute("tableName").toString();
		if(tableName.equals("student")) {
			mystudent.setStudentaccount((String)request.getSession().getAttribute("username"));
		}
		if(tableName.equals("teacher")) {
			mystudent.setTeacheraccount((String)request.getSession().getAttribute("username"));
		}
        EntityWrapper<MystudentEntity> ew = new EntityWrapper<MystudentEntity>();

		PageUtils page = mystudentService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, mystudent), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,MystudentEntity mystudent,
		HttpServletRequest request){
        EntityWrapper<MystudentEntity> ew = new EntityWrapper<MystudentEntity>();

		PageUtils page = mystudentService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, mystudent), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( MystudentEntity mystudent){
       	EntityWrapper<MystudentEntity> ew = new EntityWrapper<MystudentEntity>();
      	ew.allEq(MPUtil.allEQMapPre( mystudent, "mystudent"));
        return R.ok().put("data", mystudentService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(MystudentEntity mystudent){
        EntityWrapper< MystudentEntity> ew = new EntityWrapper< MystudentEntity>();
 		ew.allEq(MPUtil.allEQMapPre( mystudent, "mystudent"));
		MystudentView mystudentView =  mystudentService.selectView(ew);
		return R.ok("查询用户成功").put("data", mystudentView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        MystudentEntity mystudent = mystudentService.selectById(id);
		mystudent = mystudentService.selectView(new EntityWrapper<MystudentEntity>().eq("id", id));
        return R.ok().put("data", mystudent);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        MystudentEntity mystudent = mystudentService.selectById(id);
		mystudent = mystudentService.selectView(new EntityWrapper<MystudentEntity>().eq("id", id));
        return R.ok().put("data", mystudent);
    }
    



    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MystudentEntity mystudent, HttpServletRequest request){
    	mystudent.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(mystudent);
        mystudentService.insert(mystudent);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody MystudentEntity mystudent, HttpServletRequest request){
    	mystudent.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(mystudent);
        mystudentService.insert(mystudent);
        return R.ok();
    }



    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody MystudentEntity mystudent, HttpServletRequest request){
        //ValidatorUtils.validateEntity(mystudent);
        mystudentService.updateById(mystudent);//全部更新
        return R.ok();
    }



    

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        mystudentService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
    
	








}
