package com.cl.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.cl.utils.PageUtils;
import com.cl.entity.FeedbackEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.FeedbackView;


/**
 * 意见反馈
 *
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
public interface FeedbackService extends IService<FeedbackEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<FeedbackView> selectListView(Wrapper<FeedbackEntity> wrapper);
   	
   	FeedbackView selectView(@Param("ew") Wrapper<FeedbackEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<FeedbackEntity> wrapper);
   	
   	PageUtils queryPageGroupBy(Map<String, Object> params,Wrapper<FeedbackEntity> wrapper);

}

