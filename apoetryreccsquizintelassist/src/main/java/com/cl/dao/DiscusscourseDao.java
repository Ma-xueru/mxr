package com.cl.dao;

import com.cl.entity.DiscusscourseEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.DiscusscourseView;


/**
 * 古诗词评论表
 * 
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
public interface DiscusscourseDao extends BaseMapper<DiscusscourseEntity> {
	
	List<DiscusscourseView> selectListView(@Param("ew") Wrapper<DiscusscourseEntity> wrapper);

	List<DiscusscourseView> selectListView(Pagination page,@Param("ew") Wrapper<DiscusscourseEntity> wrapper);
	
	DiscusscourseView selectView(@Param("ew") Wrapper<DiscusscourseEntity> wrapper);
	
	List<DiscusscourseView> selectGroupBy(Pagination page,@Param("ew") Wrapper<DiscusscourseEntity> wrapper);

}