package com.cl.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.cl.utils.PageUtils;
import com.cl.entity.CourseEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.CourseView;


/**
 * 古诗词
 *
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
public interface CourseService extends IService<CourseEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
	List<CourseView> selectListView(Wrapper<CourseEntity> wrapper);
    
	CourseView selectView(@Param("ew") Wrapper<CourseEntity> wrapper);
    
	PageUtils queryPage(Map<String, Object> params,Wrapper<CourseEntity> wrapper);
    
	PageUtils queryPageGroupBy(Map<String, Object> params,Wrapper<CourseEntity> wrapper);

}