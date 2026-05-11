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


import com.cl.dao.DiscussforumDao;
import com.cl.entity.DiscussforumEntity;
import com.cl.service.DiscussforumService;
import com.cl.entity.view.DiscussforumView;

@Service("discussforumService")
public class DiscussforumServiceImpl extends ServiceImpl<DiscussforumDao, DiscussforumEntity> implements DiscussforumService {
	
	@Override
	public PageUtils queryPageGroupBy(Map<String, Object> params, Wrapper<DiscussforumEntity> wrapper) {
		Page<DiscussforumView> page =new Query<DiscussforumView>(params).getPage();
        page.setRecords(baseMapper.selectGroupBy(page,wrapper));
    	PageUtils pageUtil = new PageUtils(page);
    	return pageUtil;
	}
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<DiscussforumEntity> page = this.selectPage(
                new Query<DiscussforumEntity>(params).getPage(),
                new EntityWrapper<DiscussforumEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<DiscussforumEntity> wrapper) {
		  Page<DiscussforumView> page =new Query<DiscussforumView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}
    
	@Override
	public List<DiscussforumView> selectListView(Wrapper<DiscussforumEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public DiscussforumView selectView(Wrapper<DiscussforumEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}


}
