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

import com.cl.entity.DiscusscourseEntity;
import com.cl.entity.view.DiscusscourseView;

import com.cl.service.DiscusscourseService;
import com.cl.service.TokenService;
import com.cl.utils.PageUtils;
import com.cl.utils.R;
import com.cl.utils.MPUtil;
import com.cl.utils.CommonUtil;
import java.io.IOException;

/**
 * 古诗词评论表
 * 后端接口
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
@RestController
@RequestMapping("/discusscourse")
public class DiscusscourseController {
    @Autowired
    private DiscusscourseService discusscourseService;

    
    


    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,DiscusscourseEntity discusscourse,
		HttpServletRequest request){
        EntityWrapper<DiscusscourseEntity> ew = new EntityWrapper<DiscusscourseEntity>();

		PageUtils page = discusscourseService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, discusscourse), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,DiscusscourseEntity discusscourse, 
		HttpServletRequest request){
        EntityWrapper<DiscusscourseEntity> ew = new EntityWrapper<DiscusscourseEntity>();

		PageUtils page = discusscourseService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, discusscourse), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( DiscusscourseEntity discusscourse){
        	EntityWrapper<DiscusscourseEntity> ew = new EntityWrapper<DiscusscourseEntity>();
       	ew.allEq(MPUtil.allEQMapPre( discusscourse, "discusscourse")); 
        return R.ok().put("data", discusscourseService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(DiscusscourseEntity discusscourse){
        EntityWrapper< DiscusscourseEntity> ew = new EntityWrapper< DiscusscourseEntity>();
     	ew.allEq(MPUtil.allEQMapPre( discusscourse, "discusscourse")); 
		DiscusscourseView discusscourseView =  discusscourseService.selectView(ew);
		return R.ok("查询古诗词评论表成功").put("data", discusscourseView);
    }
    
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        DiscusscourseEntity discusscourse = discusscourseService.selectById(id);
        return R.ok().put("data", discusscourse);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        DiscusscourseEntity discusscourse = discusscourseService.selectById(id);
        return R.ok().put("data", discusscourse);
    }
    


    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody DiscusscourseEntity discusscourse, HttpServletRequest request){
    	discusscourse.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(discusscourse);
        discusscourseService.insert(discusscourse);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody DiscusscourseEntity discusscourse, HttpServletRequest request){
    	discusscourse.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(discusscourse);
        discusscourseService.insert(discusscourse);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody DiscusscourseEntity discusscourse, HttpServletRequest request){
        //ValidatorUtils.validateEntity(discusscourse);
        discusscourseService.updateById(discusscourse);//全部更新
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        discusscourseService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
    
    /**
     * 提醒接口
     */
	@RequestMapping("/remind/{columnName}/{type}")
	public R remindCount(@PathVariable("columnName") String columnName, HttpServletRequest request,
                         @PathVariable("type") String type,@RequestParam Map<String, Object> map) {
		map.put("column", columnName);
		map.put("type", type);

		if(type.equals("2")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			Date remindStartDate = null;
			Date remindEndDate = null;
			if(map.get("remindstart")!=null) {
				Integer remindStart = Integer.parseInt(map.get("remindstart").toString());
				c.setTime(new Date()); 
				c.add(Calendar.DAY_OF_MONTH,remindStart);
				remindStartDate = c.getTime();
				map.put("remindstart", sdf.format(remindStartDate));
			}
			if(map.get("remindend")!=null) {
				Integer remindEnd = Integer.parseInt(map.get("remindend").toString());
				c.setTime(new Date());
				c.add(Calendar.DAY_OF_MONTH,remindEnd);
				remindEndDate = c.getTime();
				map.put("remindend", sdf.format(remindEndDate));
			}
		}

		Wrapper<DiscusscourseEntity> wrapper = new EntityWrapper<DiscusscourseEntity>();
		if(map.get("remindstart")!=null) {
			wrapper.ge(columnName, map.get("remindstart"));
		}
		if(map.get("remindend")!=null) {
			wrapper.le(columnName, map.get("remindend"));
		}

		int count = discusscourseService.selectCount(wrapper);
		return R.ok().put("count", count);
	}

}