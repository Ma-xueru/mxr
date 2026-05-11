package com.cl.dao;

import com.cl.entity.TeacherEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.TeacherView;


/**
 * 教师
 * 
 * @author 
 * @email 
 */
public interface TeacherDao extends BaseMapper<TeacherEntity> {
	
	List<TeacherView> selectListView(@Param("ew") Wrapper<TeacherEntity> wrapper);

	List<TeacherView> selectListView(Pagination page,@Param("ew") Wrapper<TeacherEntity> wrapper);
	
	TeacherView selectView(@Param("ew") Wrapper<TeacherEntity> wrapper);
	
	List<TeacherView> selectGroupBy(Pagination page,@Param("ew") Wrapper<TeacherEntity> wrapper);

    List<Map<String, Object>> selectValue(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<TeacherEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<TeacherEntity> wrapper);

    List<Map<String, Object>> selectGroup(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<TeacherEntity> wrapper);



}
