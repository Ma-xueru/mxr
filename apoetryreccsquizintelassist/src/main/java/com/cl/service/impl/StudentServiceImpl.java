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


import com.cl.dao.StudentDao;
import com.cl.entity.StudentEntity;
import com.cl.service.StudentService;
import com.cl.entity.view.StudentView;

@Service("studentService")
public class StudentServiceImpl extends ServiceImpl<StudentDao, StudentEntity> implements StudentService {
	
	@Override
	public PageUtils queryPageGroupBy(Map<String, Object> params, Wrapper<StudentEntity> wrapper) {
		Page<StudentView> page =new Query<StudentView>(params).getPage();
        page.setRecords(baseMapper.selectGroupBy(page,wrapper));
    	PageUtils pageUtil = new PageUtils(page);
    	return pageUtil;
	}
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<StudentEntity> page = this.selectPage(
                new Query<StudentEntity>(params).getPage(),
                new EntityWrapper<StudentEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<StudentEntity> wrapper) {
		  Page<StudentView> page =new Query<StudentView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}
    
	@Override
	public List<StudentView> selectListView(Wrapper<StudentEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public StudentView selectView(Wrapper<StudentEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

    @Override
    public List<Map<String, Object>> selectValue(Map<String, Object> params, Wrapper<StudentEntity> wrapper) {
        return baseMapper.selectValue(params, wrapper);
    }

    @Override
    public List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params, Wrapper<StudentEntity> wrapper) {
        return baseMapper.selectTimeStatValue(params, wrapper);
    }

    @Override
    public List<Map<String, Object>> selectGroup(Map<String, Object> params, Wrapper<StudentEntity> wrapper) {
        return baseMapper.selectGroup(params, wrapper);
    }




}
