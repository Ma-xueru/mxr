package com.cl.service.impl;


import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.cl.dao.AdminDao;
import com.cl.entity.AdminEntity;
import com.cl.service.AdminService;
import com.cl.utils.PageUtils;
import com.cl.utils.Query;


/**
 * 系统用户
 */
@Service("adminService")
public class AdminServiceImpl extends ServiceImpl<AdminDao, AdminEntity> implements AdminService {

	@Override
	public PageUtils queryPage(Map<String, Object> params) {
		Page<AdminEntity> page = this.selectPage(
                new Query<AdminEntity>(params).getPage(),
                new EntityWrapper<AdminEntity>()
        );
        return new PageUtils(page);
	}

	@Override
	public List<AdminEntity> selectListView(Wrapper<AdminEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public PageUtils queryPage(Map<String, Object> params,
			Wrapper<AdminEntity> wrapper) {
		 Page<AdminEntity> page =new Query<AdminEntity>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
	}
}
