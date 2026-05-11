package com.cl.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.cl.utils.PageUtils;
import com.cl.utils.Query;


import com.cl.dao.CourseDao;
import com.cl.entity.CourseEntity;
import com.cl.service.CourseService;
import com.cl.entity.view.CourseView;

@Service("courseService")
public class CourseServiceImpl extends ServiceImpl<CourseDao, CourseEntity> implements CourseService {
	
	@Override
	public PageUtils queryPageGroupBy(Map<String, Object> params, Wrapper<CourseEntity> wrapper) {
		Page<CourseView> page =new Query<CourseView>(params).getPage();
        page.setRecords(baseMapper.selectGroupBy(page,wrapper));
     	PageUtils pageUtil = new PageUtils(page);
     	return pageUtil;
	}
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<CourseEntity> page = this.selectPage(
                new Query<CourseEntity>(params).getPage(),
                new EntityWrapper<CourseEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<CourseEntity> wrapper) {
		  Page<CourseView> page =new Query<CourseView>(params).getPage();
        page.setRecords(baseMapper.selectListView(page,wrapper));
     	PageUtils pageUtil = new PageUtils(page);
     	return pageUtil;
  	}
    
	@Override
	public List<CourseView> selectListView(Wrapper<CourseEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}
    
	@Override
	public CourseView selectView(@Param("ew") Wrapper<CourseEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

}