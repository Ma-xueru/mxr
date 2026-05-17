package com.cl.controller;

import java.util.Arrays;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.cl.entity.FollowreadRecordEntity;
import com.cl.dao.FollowreadRecordDao;
import com.cl.utils.PageUtils;
import com.cl.utils.R;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/followreadrecord")
public class FollowreadrecordController {
    @Autowired
    private FollowreadRecordDao followreadRecordDao;

    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, FollowreadRecordEntity record, HttpServletRequest request) {
        EntityWrapper<FollowreadRecordEntity> ew = new EntityWrapper<>();
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        if ("teacher".equals(tableName)) {
            java.util.List<String> classnames = (java.util.List<String>) request.getSession().getAttribute("classnames");
            if (classnames != null && !classnames.isEmpty()) ew.in("classname", classnames);
        }
        ew.orderBy("addtime", false);
        int page = Integer.parseInt(String.valueOf(params.getOrDefault("page", "1")));
        int limit = Integer.parseInt(String.valueOf(params.getOrDefault("limit", "10")));
        int total = followreadRecordDao.selectCount(ew);
        ew.last("LIMIT " + ((page - 1) * limit) + "," + limit);
        PageUtils pu = new PageUtils(followreadRecordDao.selectList(ew), total, limit, page);
        return R.ok().put("data", pu);
    }

    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, FollowreadRecordEntity record, HttpServletRequest request) {
        return page(params, record, request);
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        for (Long id : ids) followreadRecordDao.deleteById(id);
        return R.ok();
    }
}
