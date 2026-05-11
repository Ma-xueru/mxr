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


import com.cl.dao.ReservecancelDao;
import com.cl.entity.ReservecancelEntity;
import com.cl.service.ReservecancelService;
import com.cl.entity.view.ReservecancelView;

@Service("reservecancelService")
public class ReservecancelServiceImpl extends ServiceImpl<ReservecancelDao, ReservecancelEntity> implements ReservecancelService {
	
	@Override
	public PageUtils queryPageGroupBy(Map<String, Object> params, Wrapper<ReservecancelEntity> wrapper) {
		Page<ReservecancelView> page =new Query<ReservecancelView>(params).getPage();
        page.setRecords(baseMapper.selectGroupBy(page,wrapper));
    	PageUtils pageUtil = new PageUtils(page);
    	return pageUtil;
	}
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<ReservecancelEntity> page = this.selectPage(
                new Query<ReservecancelEntity>(params).getPage(),
                new EntityWrapper<ReservecancelEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<ReservecancelEntity> wrapper) {
		  Page<ReservecancelView> page =new Query<ReservecancelView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}
    
	@Override
	public List<ReservecancelView> selectListView(Wrapper<ReservecancelEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public ReservecancelView selectView(Wrapper<ReservecancelEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}


}
