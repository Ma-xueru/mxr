package com.cl.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.cl.annotation.IgnoreAuth;
import com.cl.entity.ClasspkEntity;
import com.cl.entity.view.ClasspkView;
import com.cl.service.ClasspkService;
import com.cl.utils.MPUtil;
import com.cl.utils.PageUtils;
import com.cl.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/classpk")
public class ClasspkController {
    @Autowired
    private ClasspkService classpkService;

    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, ClasspkEntity classpk, HttpServletRequest request) {
        String tableName = request.getSession().getAttribute("tableName").toString();
        if (tableName.equals("student")) {
            String username = (String) request.getSession().getAttribute("username");
            EntityWrapper<ClasspkEntity> ew = new EntityWrapper<ClasspkEntity>();
            ew.andNew().eq("studentaccount", username).or().eq("opponentaccount", username);
            PageUtils page = classpkService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, classpk), params), params));
            return R.ok().put("data", page);
        }
        EntityWrapper<ClasspkEntity> ew = new EntityWrapper<ClasspkEntity>();
        PageUtils page = classpkService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, classpk), params), params));
        return R.ok().put("data", page);
    }

    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, ClasspkEntity classpk) {
        EntityWrapper<ClasspkEntity> ew = new EntityWrapper<ClasspkEntity>();
        PageUtils page = classpkService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, classpk), params), params));
        return R.ok().put("data", page);
    }

    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        ClasspkEntity entity = classpkService.selectView(new EntityWrapper<ClasspkEntity>().eq("id", id));
        return R.ok().put("data", entity);
    }

    @IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id) {
        ClasspkEntity entity = classpkService.selectView(new EntityWrapper<ClasspkEntity>().eq("id", id));
        return R.ok().put("data", entity);
    }

    @RequestMapping("/save")
    public R save(@RequestBody ClasspkEntity classpk) {
        classpk.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        classpkService.insert(classpk);
        return R.ok();
    }

    @RequestMapping("/add")
    public R add(@RequestBody ClasspkEntity classpk) {
        classpk.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        classpkService.insert(classpk);
        return R.ok();
    }

    @RequestMapping("/update")
    @Transactional
    public R update(@RequestBody ClasspkEntity classpk) {
        classpkService.updateById(classpk);
        return R.ok();
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        classpkService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }

    @RequestMapping("/value/{xColumnName}/{yColumnName}")
    public R value(@PathVariable("yColumnName") String yColumnName, @PathVariable("xColumnName") String xColumnName) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xColumn", xColumnName);
        params.put("yColumn", yColumnName);
        EntityWrapper<ClasspkEntity> ew = new EntityWrapper<ClasspkEntity>();
        List<Map<String, Object>> result = classpkService.selectValue(params, ew);
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
