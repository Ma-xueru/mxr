package com.cl.controller;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
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
import javax.servlet.http.HttpServletResponse;

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

import com.cl.entity.CourseEntity;
import com.cl.entity.view.CourseView;

import com.cl.service.CourseService;
import com.cl.service.TokenService;
import com.cl.utils.PageUtils;
import com.cl.utils.R;
import com.cl.utils.MPUtil;
import com.cl.utils.CommonUtil;
import java.io.IOException;

/**
 * 古诗词
 * 后端接口
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
@RestController
@RequestMapping("/course")
public class CourseController {
    @Autowired
    private CourseService courseService;

    
    


    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,CourseEntity course,
		HttpServletRequest request){
        EntityWrapper<CourseEntity> ew = new EntityWrapper<CourseEntity>();

		PageUtils page = courseService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, course), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,CourseEntity course, 
		HttpServletRequest request){
        EntityWrapper<CourseEntity> ew = new EntityWrapper<CourseEntity>();

		PageUtils page = courseService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, course), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( CourseEntity course){
        	EntityWrapper<CourseEntity> ew = new EntityWrapper<CourseEntity>();
       	ew.allEq(MPUtil.allEQMapPre( course, "course")); 
        return R.ok().put("data", courseService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(CourseEntity course){
        EntityWrapper< CourseEntity> ew = new EntityWrapper< CourseEntity>();
     	ew.allEq(MPUtil.allEQMapPre( course, "course")); 
		CourseView courseView =  courseService.selectView(ew);
		return R.ok("查询古诗词成功").put("data", courseView);
    }
    
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        CourseEntity course = courseService.selectById(id);
        course.setClicknum((course.getClicknum()==null?0:course.getClicknum())+1);
        course.setClicktime(new Date());
        course.setAddtime(new Date());
        courseService.updateById(course);
        return R.ok().put("data", course);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        CourseEntity course = courseService.selectById(id);
        course.setClicknum((course.getClicknum()==null?0:course.getClicknum())+1);
        course.setClicktime(new Date());
        course.setAddtime(new Date());
        courseService.updateById(course);
        return R.ok().put("data", course);
    }
    


    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CourseEntity course, HttpServletRequest request){
    	course.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(course);
        courseService.insert(course);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody CourseEntity course, HttpServletRequest request){
    	course.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(course);
        courseService.insert(course);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody CourseEntity course, HttpServletRequest request){
        //ValidatorUtils.validateEntity(course);
        course.setAddtime(new Date());
        courseService.updateById(course);//全部更新
        return R.ok();
    }

    /** 微信云存储封面代理 — cloud:// → 临时HTTPS */
    @IgnoreAuth
    @RequestMapping("/coverProxy")
    public void coverProxy(@RequestParam String fileId, HttpServletResponse response) throws Exception {
        if (fileId == null || !fileId.startsWith("cloud://")) { response.sendError(400); return; }
        String token = getWxAccessToken();
        String apiUrl = "https://api.weixin.qq.com/tcb/batchgetdownloadurl?access_token=" + token;
        org.json.JSONObject body = new org.json.JSONObject();
        String env = com.cl.utils.AsrPropertiesUtil.get("wechat.cloud.env", "cloudbase-d6g5mlt4tbaa32ec2");
        body.put("env", env);
        body.put("file_list", new org.json.JSONArray().put(new org.json.JSONObject().put("fileid", fileId).put("max_age", 7200)));
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(apiUrl).openConnection();
        conn.setRequestMethod("POST"); conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true); conn.getOutputStream().write(body.toString().getBytes("UTF-8"));
        java.io.InputStream is = conn.getResponseCode() == 200 ? conn.getInputStream() : conn.getErrorStream();
        String resp = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A").next(); is.close();
        org.json.JSONObject j = new org.json.JSONObject(resp);
        if (j.has("file_list")) {
            String url = j.getJSONArray("file_list").getJSONObject(0).optString("temp_file_url", "");
            if (!url.isEmpty()) { response.sendRedirect(url); return; }
        }
        response.sendError(404);
    }

    private static String wxAccessToken = null;
    private static long wxTokenExpire = 0;
    private synchronized String getWxAccessToken() throws Exception {
        if (wxAccessToken != null && System.currentTimeMillis() < wxTokenExpire) return wxAccessToken;
        String appid = com.cl.utils.AsrPropertiesUtil.get("wechat.appid", "wx57dec8b4d87f5f37");
        String secret = com.cl.utils.AsrPropertiesUtil.get("wechat.appsecret", "");
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appid + "&secret=" + secret;
        java.net.HttpURLConnection c = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
        java.io.InputStream is = c.getResponseCode() == 200 ? c.getInputStream() : c.getErrorStream();
        String resp = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A").next(); is.close();
        org.json.JSONObject j = new org.json.JSONObject(resp);
        wxAccessToken = j.getString("access_token");
        wxTokenExpire = System.currentTimeMillis() + (j.optInt("expires_in", 7200) - 300) * 1000L;
        System.out.println("[WxToken] refreshed, expires in " + j.optInt("expires_in") + "s");
        return wxAccessToken;
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        courseService.deleteBatchIds(Arrays.asList(ids));
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

		Wrapper<CourseEntity> wrapper = new EntityWrapper<CourseEntity>();
		if(map.get("remindstart")!=null) {
			wrapper.ge(columnName, map.get("remindstart"));
		}
		if(map.get("remindend")!=null) {
			wrapper.le(columnName, map.get("remindend"));
		}

		int count = courseService.selectCount(wrapper);
		return R.ok().put("count", count);
	}

}