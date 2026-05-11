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

import com.cl.entity.FeedbackEntity;
import com.cl.entity.view.FeedbackView;

import com.cl.service.FeedbackService;
import com.cl.service.TokenService;
import com.cl.utils.PageUtils;
import com.cl.utils.R;
import com.cl.utils.MPUtil;
import com.cl.utils.CommonUtil;
import java.io.IOException;

/**
 * 意见反馈
 * 后端接口
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
@RestController
@RequestMapping("/feedback")
public class FeedbackController {
    @Autowired
    private FeedbackService feedbackService;



    


    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,FeedbackEntity feedback,
		HttpServletRequest request){
		String tableName = request.getSession().getAttribute("tableName").toString();
		if(tableName.equals("student")) {
			feedback.setStudentaccount((String)request.getSession().getAttribute("username"));
		}
        EntityWrapper<FeedbackEntity> ew = new EntityWrapper<FeedbackEntity>();

		PageUtils page = feedbackService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, feedback), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,FeedbackEntity feedback, 
		HttpServletRequest request){
        EntityWrapper<FeedbackEntity> ew = new EntityWrapper<FeedbackEntity>();

		PageUtils page = feedbackService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, feedback), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( FeedbackEntity feedback){
       	EntityWrapper<FeedbackEntity> ew = new EntityWrapper<FeedbackEntity>();
      	ew.allEq(MPUtil.allEQMapPre( feedback, "feedback")); 
        return R.ok().put("data", feedbackService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(FeedbackEntity feedback){
        EntityWrapper< FeedbackEntity> ew = new EntityWrapper< FeedbackEntity>();
 		ew.allEq(MPUtil.allEQMapPre( feedback, "feedback")); 
		FeedbackView feedbackView =  feedbackService.selectView(ew);
		return R.ok("查询意见反馈成功").put("data", feedbackView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        FeedbackEntity feedback = feedbackService.selectById(id);
		feedback = feedbackService.selectView(new EntityWrapper<FeedbackEntity>().eq("id", id));
        return R.ok().put("data", feedback);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        FeedbackEntity feedback = feedbackService.selectById(id);
		feedback = feedbackService.selectView(new EntityWrapper<FeedbackEntity>().eq("id", id));
        return R.ok().put("data", feedback);
    }
    



    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody FeedbackEntity feedback, HttpServletRequest request){
    	feedback.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(feedback);
        feedbackService.insert(feedback);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody FeedbackEntity feedback, HttpServletRequest request){
    	feedback.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(feedback);
        feedbackService.insert(feedback);
        return R.ok();
    }



    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody FeedbackEntity feedback, HttpServletRequest request){
        //ValidatorUtils.validateEntity(feedback);
        feedbackService.updateById(feedback);//全部更新
        return R.ok();
    }

    /**
     * 审核
     */
    @RequestMapping("/shBatch")
    @Transactional
    public R update(@RequestBody Long[] ids, @RequestParam String sfsh, @RequestParam String shhf){
        List<FeedbackEntity> list = new ArrayList<FeedbackEntity>();
        for(Long id : ids) {
            FeedbackEntity feedback = feedbackService.selectById(id);
            feedback.setSfsh(sfsh);
            feedback.setShhf(shhf);
            list.add(feedback);
        }
        feedbackService.updateBatchById(list);
        return R.ok();
    }


    

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        feedbackService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
    
	








}
