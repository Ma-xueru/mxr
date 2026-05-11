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


import com.cl.dao.DiscusscourseDao;
import com.cl.entity.DiscusscourseEntity;
import com.cl.service.DiscusscourseService;
import com.cl.entity.view.DiscusscourseView;

@Service("discusscourseService")
public class DiscusscourseServiceImpl extends ServiceImpl<DiscusscourseDao, DiscusscourseEntity> implements DiscusscourseService {
	
	@Override
	public PageUtils queryPageGroupBy(Map<String, Object> params, Wrapper<DiscusscourseEntity> wrapper) {
		Page<DiscusscourseView> page =new Query<DiscusscourseView>(params).getPage();
        page.setRecords(baseMapper.selectGroupBy(page,wrapper));
     	PageUtils pageUtil = new PageUtils(page);
     	return pageUtil;
	}
	
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<DiscusscourseEntity> page = this.selectPage(
                new Query<DiscusscourseEntity>(params).getPage(),
                new EntityWrapper<DiscusscourseEntity>()
        );
        return new PageUtils(page);
    }
    
    @Override
	public PageUtils queryPage(Map<String, Object> params, Wrapper<DiscusscourseEntity> wrapper) {
		  Page<DiscusscourseView> page =new Query<DiscusscourseView>(params).getPage();
        page.setRecords(baseMapper.selectListView(page,wrapper));
     	PageUtils pageUtil = new PageUtils(page);
     	return pageUtil;
  	}
    
	@Override
	public List<DiscusscourseView> selectListView(Wrapper<DiscusscourseEntity> wrapper) {
		return baseMapper.selectListView(wrapper);
	}
    
	@Override
	public DiscusscourseView selectView(@Param("ew") Wrapper<DiscusscourseEntity> wrapper) {
		return baseMapper.selectView(wrapper);
	}

}