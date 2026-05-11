package com.cl.dao;

import com.cl.entity.FeedbackEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.FeedbackView;


/**
 * 意见反馈
 * 
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
public interface FeedbackDao extends BaseMapper<FeedbackEntity> {
	
	List<FeedbackView> selectListView(@Param("ew") Wrapper<FeedbackEntity> wrapper);

	List<FeedbackView> selectListView(Pagination page,@Param("ew") Wrapper<FeedbackEntity> wrapper);
	
	FeedbackView selectView(@Param("ew") Wrapper<FeedbackEntity> wrapper);
	
	List<FeedbackView> selectGroupBy(Pagination page,@Param("ew") Wrapper<FeedbackEntity> wrapper);

}
