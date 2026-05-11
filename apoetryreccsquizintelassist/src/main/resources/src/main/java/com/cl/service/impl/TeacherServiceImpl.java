package com.cl.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.cl.utils.PageUtils;
import com.cl.utils.Query;


import com.cl.dao.TeacherDao;
import com.cl.entity.TeacherEntity;
import com.cl.service.TeacherService;
import com.cl.entity.view.TeacherView;

@Service("teacherService")
public class TeacherServiceImpl extends ServiceImpl<TeacherDao, TeacherEntity> implements TeacherService {
	
	@Override
	public PageUtils queryPageGroupBy(Map<String, Object> params, Wrapper<TeacherEntity> wrapper) {
		Page<TeacherView> page =new Query<TeacherView>(params).getPage();
        page.setRecords(baseMapper.selectGroupBy(page,wrapper));
    	PageUtils pageUtil = new PageUtils(page);
    	return pageUtil;
	}
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<TeacherEntity> page = this.selectPage(
                new Query<TeacherEntity>(params).getPage(),
                new EntityWrapper<TeacherEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<TeacherEntity> wrapper) {
		  Page<TeacherView> page =new Query<TeacherView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}
    
	@Override
	public List<TeacherView> selectListView(Wrapper<TeacherEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public TeacherView selectView(Wrapper<TeacherEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

    @Override
    public List<Map<String, Object>> selectValue(Map<String, Object> params, Wrapper<TeacherEntity> wrapper) {
        return baseMapper.selectValue(params, wrapper);
    }

    @Override
    public List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params, Wrapper<TeacherEntity> wrapper) {
        return baseMapper.selectTimeStatValue(params, wrapper);
    }

    @Override
    public List<Map<String, Object>> selectGroup(Map<String, Object> params, Wrapper<TeacherEntity> wrapper) {
        return baseMapper.selectGroup(params, wrapper);
    }




}
