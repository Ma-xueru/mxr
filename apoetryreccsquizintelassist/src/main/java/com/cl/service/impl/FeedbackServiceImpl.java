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


import com.cl.dao.FeedbackDao;
import com.cl.entity.FeedbackEntity;
import com.cl.service.FeedbackService;
import com.cl.entity.view.FeedbackView;

@Service("feedbackService")
public class FeedbackServiceImpl extends ServiceImpl<FeedbackDao, FeedbackEntity> implements FeedbackService {
	
	@Override
	public PageUtils queryPageGroupBy(Map<String, Object> params, Wrapper<FeedbackEntity> wrapper) {
		Page<FeedbackView> page =new Query<FeedbackView>(params).getPage();
        page.setRecords(baseMapper.selectGroupBy(page,wrapper));
    	PageUtils pageUtil = new PageUtils(page);
    	return pageUtil;
	}
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<FeedbackEntity> page = this.selectPage(
                new Query<FeedbackEntity>(params).getPage(),
                new EntityWrapper<FeedbackEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<FeedbackEntity> wrapper) {
		  Page<FeedbackView> page =new Query<FeedbackView>(params).getPage();
	        page.setRecords(baseMapper.selectListView(page,wrapper));
	    	PageUtils pageUtil = new PageUtils(page);
	    	return pageUtil;
 	}
    
	@Override
	public List<FeedbackView> selectListView(Wrapper<FeedbackEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}

	@Override
	public FeedbackView selectView(Wrapper<FeedbackEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}


}
