package com.cl.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.cl.annotation.IgnoreAuth;
import com.cl.entity.ClassinfoEntity;
import com.cl.service.ClassinfoService;
import com.cl.utils.MPUtil;
import com.cl.utils.PageUtils;
import com.cl.utils.R;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/classinfo")
public class ClassinfoController {
    @Autowired
    private ClassinfoService classinfoService;

    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, ClassinfoEntity classinfo, HttpServletRequest request) {
        EntityWrapper<ClassinfoEntity> ew = new EntityWrapper<ClassinfoEntity>();
        // 教师scope：只看自己年级
        String tableName = (String) request.getSession().getAttribute("tableName");
        if ("teacher".equals(tableName)) {
            String grade = (String) request.getSession().getAttribute("grade");
            if (org.apache.commons.lang3.StringUtils.isNotBlank(grade)) ew.eq("grade", grade);
        }
        PageUtils page = classinfoService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, classinfo), params), params));
        return R.ok().put("data", page);
    }

    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, ClassinfoEntity classinfo, HttpServletRequest request) {
        EntityWrapper<ClassinfoEntity> ew = new EntityWrapper<ClassinfoEntity>();
        // 教师scope：只看自己年级
        String tableName = (String) request.getSession().getAttribute("tableName");
        if ("teacher".equals(tableName)) {
            String grade = (String) request.getSession().getAttribute("grade");
            if (org.apache.commons.lang3.StringUtils.isNotBlank(grade)) ew.eq("grade", grade);
        }
        PageUtils page = classinfoService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, classinfo), params), params));
        return R.ok().put("data", page);
    }

    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        ClassinfoEntity entity = classinfoService.selectView(new EntityWrapper<ClassinfoEntity>().eq("id", id));
        return R.ok().put("data", entity);
    }

    @IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id) {
        ClassinfoEntity entity = classinfoService.selectView(new EntityWrapper<ClassinfoEntity>().eq("id", id));
        return R.ok().put("data", entity);
    }

    @RequestMapping("/save")
    public R save(@RequestBody ClassinfoEntity classinfo) {
        if (StringUtils.isBlank(classinfo.getClassname())) {
            return R.error("班级名称不能为空");
        }
        classinfo.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        classinfoService.insert(classinfo);
        return R.ok();
    }

    @RequestMapping("/add")
    public R add(@RequestBody ClassinfoEntity classinfo) {
        if (StringUtils.isBlank(classinfo.getClassname())) {
            return R.error("班级名称不能为空");
        }
        classinfo.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        classinfoService.insert(classinfo);
        return R.ok();
    }

    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody ClassinfoEntity classinfo) {
        classinfoService.updateById(classinfo);
        return R.ok();
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        classinfoService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }

    @RequestMapping("/group/{columnName}")
    public R group(@PathVariable("columnName") String columnName) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("column", columnName);
        EntityWrapper<ClassinfoEntity> ew = new EntityWrapper<ClassinfoEntity>();
        List<Map<String, Object>> result = classinfoService.selectGroup(params, ew);
        return R.ok().put("data", result);
    }

    @RequestMapping("/value/{xColumnName}/{yColumnName}")
    public R value(@PathVariable("yColumnName") String yColumnName, @PathVariable("xColumnName") String xColumnName) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xColumn", xColumnName);
        params.put("yColumn", yColumnName);
        EntityWrapper<ClassinfoEntity> ew = new EntityWrapper<ClassinfoEntity>();
        List<Map<String, Object>> result = classinfoService.selectValue(params, ew);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Map<String, Object> m : result) {
            for (String k : m.keySet()) {
                if (m.get(k) instanceof Date) {
                    m.put(k, sdf.format((Date) m.get(k)));
                }
            }
        }
        return R.ok().put("data", result);
    }
}
