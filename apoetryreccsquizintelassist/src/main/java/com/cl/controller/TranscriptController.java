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

import com.cl.entity.TranscriptEntity;
import com.cl.entity.MystudentEntity;
import com.cl.entity.view.TranscriptView;

import com.cl.service.TranscriptService;
import com.cl.service.MystudentService;
import com.cl.service.TokenService;
import com.cl.utils.PageUtils;
import com.cl.utils.R;
import com.cl.utils.MPUtil;
import com.cl.utils.CommonUtil;
import java.io.IOException;

/**
 * 成绩信息
 * 后端接口
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
@RestController
@RequestMapping("/transcript")
public class TranscriptController {
    @Autowired
    private TranscriptService transcriptService;
    @Autowired
    private MystudentService mystudentService;

    private String getSessionTableName(HttpServletRequest request) {
        Object tableName = request.getSession().getAttribute("tableName");
        return tableName == null ? "" : tableName.toString();
    }

    private void applyTeacherStudentScope(EntityWrapper<TranscriptEntity> ew, HttpServletRequest request) {
        String tableName = getSessionTableName(request);
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
            ew.eq("studentaccount", "__NO_MATCH__");
            return;
        }
        if(!studentAccounts.isEmpty() && !studentNames.isEmpty()) {
            ew.andNew();
            ew.in("studentaccount", studentAccounts).or().in("studentname", studentNames);
            return;
        }
        if(!studentAccounts.isEmpty()) {
            ew.in("studentaccount", studentAccounts);
            return;
        }
        ew.in("studentname", studentNames);
    }



    


    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,TranscriptEntity transcript,
		HttpServletRequest request){
		String tableName = getSessionTableName(request);
		if(tableName.equals("student")) {
			transcript.setStudentaccount((String)request.getSession().getAttribute("username"));
		}
        EntityWrapper<TranscriptEntity> ew = new EntityWrapper<TranscriptEntity>();
        applyTeacherStudentScope(ew, request);

		PageUtils page = transcriptService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, transcript), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,TranscriptEntity transcript, 
		HttpServletRequest request){
        EntityWrapper<TranscriptEntity> ew = new EntityWrapper<TranscriptEntity>();

		PageUtils page = transcriptService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, transcript), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( TranscriptEntity transcript){
       	EntityWrapper<TranscriptEntity> ew = new EntityWrapper<TranscriptEntity>();
      	ew.allEq(MPUtil.allEQMapPre( transcript, "transcript")); 
        return R.ok().put("data", transcriptService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(TranscriptEntity transcript){
        EntityWrapper< TranscriptEntity> ew = new EntityWrapper< TranscriptEntity>();
 		ew.allEq(MPUtil.allEQMapPre( transcript, "transcript")); 
		TranscriptView transcriptView =  transcriptService.selectView(ew);
		return R.ok("查询成绩信息成功").put("data", transcriptView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        TranscriptEntity transcript = transcriptService.selectById(id);
		transcript = transcriptService.selectView(new EntityWrapper<TranscriptEntity>().eq("id", id));
        return R.ok().put("data", transcript);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        TranscriptEntity transcript = transcriptService.selectById(id);
		transcript = transcriptService.selectView(new EntityWrapper<TranscriptEntity>().eq("id", id));
        return R.ok().put("data", transcript);
    }
    



    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody TranscriptEntity transcript, HttpServletRequest request){
    	transcript.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(transcript);
        transcriptService.insert(transcript);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody TranscriptEntity transcript, HttpServletRequest request){
    	transcript.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(transcript);
        transcriptService.insert(transcript);
        return R.ok();
    }



    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody TranscriptEntity transcript, HttpServletRequest request){
        //ValidatorUtils.validateEntity(transcript);
        transcriptService.updateById(transcript);//全部更新
        return R.ok();
    }



    

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        transcriptService.deleteBatchIds(Arrays.asList(ids));
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
        EntityWrapper<TranscriptEntity> ew = new EntityWrapper<TranscriptEntity>();
		String tableName = getSessionTableName(request);
		if(tableName.equals("student")) {
            ew.eq("studentaccount", (String)request.getSession().getAttribute("username"));
		}
        applyTeacherStudentScope(ew, request);
        List<Map<String, Object>> result = transcriptService.selectValue(params, ew);
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
        EntityWrapper<TranscriptEntity> ew = new EntityWrapper<TranscriptEntity>();
        String tableName = getSessionTableName(request);
        if(tableName.equals("student")) {
            ew.eq("studentaccount", (String)request.getSession().getAttribute("username"));
        }
        applyTeacherStudentScope(ew, request);
        for(int i=0;i<yColumnNames.length;i++) {
            params.put("yColumn", yColumnNames[i]);
            List<Map<String, Object>> result = transcriptService.selectValue(params, ew);
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
        EntityWrapper<TranscriptEntity> ew = new EntityWrapper<TranscriptEntity>();
        String tableName = getSessionTableName(request);
        if(tableName.equals("student")) {
            ew.eq("studentaccount", (String)request.getSession().getAttribute("username"));
        }
        applyTeacherStudentScope(ew, request);
        List<Map<String, Object>> result = transcriptService.selectTimeStatValue(params, ew);
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
        EntityWrapper<TranscriptEntity> ew = new EntityWrapper<TranscriptEntity>();
        String tableName = getSessionTableName(request);
        if(tableName.equals("student")) {
            ew.eq("studentaccount", (String)request.getSession().getAttribute("username"));
        }
        applyTeacherStudentScope(ew, request);
        for(int i=0;i<yColumnNames.length;i++) {
            params.put("yColumn", yColumnNames[i]);
            List<Map<String, Object>> result = transcriptService.selectTimeStatValue(params, ew);
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
        EntityWrapper<TranscriptEntity> ew = new EntityWrapper<TranscriptEntity>();
        String tableName = getSessionTableName(request);
        if(tableName.equals("student")) {
            ew.eq("studentaccount", (String)request.getSession().getAttribute("username"));
        }
        applyTeacherStudentScope(ew, request);
        List<Map<String, Object>> result = transcriptService.selectGroup(params, ew);
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
     * 分段统计
     */
    @RequestMapping("/sectionStat/kaoshichengji")
    @IgnoreAuth
    public R kaoshichengjiSectionStat(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();
        EntityWrapper<TranscriptEntity> ew = new EntityWrapper<TranscriptEntity>();
        String tableName = getSessionTableName(request);
        if(tableName.equals("student")) {
            ew.eq("studentaccount", (String)request.getSession().getAttribute("username"));
        }
        applyTeacherStudentScope(ew, request);
        List<Map<String, Object>> result = transcriptService.kaoshichengjiSectionStat(params, ew);
        return R.ok().put("data", result);
    }



    /**
     * 总数量
     */
    @RequestMapping("/count")
    public R count(@RequestParam Map<String, Object> params,TranscriptEntity transcript, HttpServletRequest request){
        String tableName = getSessionTableName(request);
        if(tableName.equals("student")) {
            transcript.setStudentaccount((String)request.getSession().getAttribute("username"));
        }
        EntityWrapper<TranscriptEntity> ew = new EntityWrapper<TranscriptEntity>();
        applyTeacherStudentScope(ew, request);
        int count = transcriptService.selectCount(MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, transcript), params), params));
        return R.ok().put("data", count);
    }


}
