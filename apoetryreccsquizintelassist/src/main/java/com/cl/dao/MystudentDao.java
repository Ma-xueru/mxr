package com.cl.dao;

import com.cl.entity.MystudentEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.MystudentView;


/**
 * 用户
 * 
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
public interface MystudentDao extends BaseMapper<MystudentEntity> {
	
	List<MystudentView> selectListView(@Param("ew") Wrapper<MystudentEntity> wrapper);

	List<MystudentView> selectListView(Pagination page,@Param("ew") Wrapper<MystudentEntity> wrapper);
	
	MystudentView selectView(@Param("ew") Wrapper<MystudentEntity> wrapper);
	
	List<MystudentView> selectGroupBy(Pagination page,@Param("ew") Wrapper<MystudentEntity> wrapper);

}
