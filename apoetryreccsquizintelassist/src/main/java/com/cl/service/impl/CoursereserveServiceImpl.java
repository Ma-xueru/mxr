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


import com.cl.dao.CoursereserveDao;
import com.cl.entity.CoursereserveEntity;
import com.cl.service.CoursereserveService;
import com.cl.entity.view.CoursereserveView;

@Service("coursereserveService")
public class CoursereserveServiceImpl extends ServiceImpl<CoursereserveDao, CoursereserveEntity> implements CoursereserveService {
	
	@Override
	public PageUtils queryPageGroupBy(Map<String, Object> params, Wrapper<CoursereserveEntity> wrapper) {
		Page<CoursereserveView> page =new Query<CoursereserveView>(params).getPage();
        page.setRecords(baseMapper.selectGroupBy(page,wrapper));
    	PageUtils pageUtil = new PageUtils(page);
    	return pageUtil;
	}
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<CoursereserveEntity> page = this.selectPage(
                new Query<CoursereserveEntity>(params).getPage(),
                new EntityWrapper<CoursereserveEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<CoursereserveEntity> wrapper) {
		  Page<CoursereserveView> page =new Query<CoursereserveView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}
    
	@Override
	public List<CoursereserveView> selectListView(Wrapper<CoursereserveEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public CoursereserveView selectView(Wrapper<CoursereserveEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}


}
