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


import com.cl.dao.MystudentDao;
import com.cl.entity.MystudentEntity;
import com.cl.service.MystudentService;
import com.cl.entity.view.MystudentView;

@Service("mystudentService")
public class MystudentServiceImpl extends ServiceImpl<MystudentDao, MystudentEntity> implements MystudentService {
	
	@Override
	public PageUtils queryPageGroupBy(Map<String, Object> params, Wrapper<MystudentEntity> wrapper) {
		Page<MystudentView> page =new Query<MystudentView>(params).getPage();
        page.setRecords(baseMapper.selectGroupBy(page,wrapper));
    	PageUtils pageUtil = new PageUtils(page);
    	return pageUtil;
	}
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<MystudentEntity> page = this.selectPage(
                new Query<MystudentEntity>(params).getPage(),
                new EntityWrapper<MystudentEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<MystudentEntity> wrapper) {
		  Page<MystudentView> page =new Query<MystudentView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}
    
	@Override
	public List<MystudentView> selectListView(Wrapper<MystudentEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public MystudentView selectView(Wrapper<MystudentEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}


}
