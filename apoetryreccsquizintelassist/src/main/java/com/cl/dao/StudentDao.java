package com.cl.dao;

import com.cl.entity.StudentEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.StudentView;


/**
 * 用户
 * 
 * @author 
 * @email 
 */
public interface StudentDao extends BaseMapper<StudentEntity> {
	
	List<StudentView> selectListView(@Param("ew") Wrapper<StudentEntity> wrapper);

	List<StudentView> selectListView(Pagination page,@Param("ew") Wrapper<StudentEntity> wrapper);
	
	StudentView selectView(@Param("ew") Wrapper<StudentEntity> wrapper);
	
	List<StudentView> selectGroupBy(Pagination page,@Param("ew") Wrapper<StudentEntity> wrapper);

    List<Map<String, Object>> selectValue(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<StudentEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<StudentEntity> wrapper);

    List<Map<String, Object>> selectGroup(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<StudentEntity> wrapper);



}
