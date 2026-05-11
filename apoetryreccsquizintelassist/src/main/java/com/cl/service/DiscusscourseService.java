package com.cl.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.cl.utils.PageUtils;
import com.cl.entity.DiscusscourseEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.DiscusscourseView;


/**
 * 古诗词评论表
 *
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
public interface DiscusscourseService extends IService<DiscusscourseEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
	List<DiscusscourseView> selectListView(Wrapper<DiscusscourseEntity> wrapper);
    
	DiscusscourseView selectView(@Param("ew") Wrapper<DiscusscourseEntity> wrapper);
    
	PageUtils queryPage(Map<String, Object> params,Wrapper<DiscusscourseEntity> wrapper);
    
	PageUtils queryPageGroupBy(Map<String, Object> params,Wrapper<DiscusscourseEntity> wrapper);

}