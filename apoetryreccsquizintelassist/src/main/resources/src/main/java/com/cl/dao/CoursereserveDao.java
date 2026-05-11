package com.cl.dao;

import com.cl.entity.CoursereserveEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.CoursereserveView;


/**
 * 预约课程
 * 
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
public interface CoursereserveDao extends BaseMapper<CoursereserveEntity> {
	
	List<CoursereserveView> selectListView(@Param("ew") Wrapper<CoursereserveEntity> wrapper);

	List<CoursereserveView> selectListView(Pagination page,@Param("ew") Wrapper<CoursereserveEntity> wrapper);
	
	CoursereserveView selectView(@Param("ew") Wrapper<CoursereserveEntity> wrapper);
	
	List<CoursereserveView> selectGroupBy(Pagination page,@Param("ew") Wrapper<CoursereserveEntity> wrapper);

}
