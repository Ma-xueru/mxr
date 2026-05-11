package com.cl.dao;

import com.cl.entity.CourseEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.CourseView;


/**
 * 古诗词
 * 
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
public interface CourseDao extends BaseMapper<CourseEntity> {
	
	List<CourseView> selectListView(@Param("ew") Wrapper<CourseEntity> wrapper);

	List<CourseView> selectListView(Pagination page,@Param("ew") Wrapper<CourseEntity> wrapper);
	
	CourseView selectView(@Param("ew") Wrapper<CourseEntity> wrapper);
	
	List<CourseView> selectGroupBy(Pagination page,@Param("ew") Wrapper<CourseEntity> wrapper);

}