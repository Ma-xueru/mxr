package com.cl.dao;

import com.cl.entity.TranscriptEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.TranscriptView;


/**
 * 成绩信息
 * 
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
public interface TranscriptDao extends BaseMapper<TranscriptEntity> {
	
	List<TranscriptView> selectListView(@Param("ew") Wrapper<TranscriptEntity> wrapper);

	List<TranscriptView> selectListView(Pagination page,@Param("ew") Wrapper<TranscriptEntity> wrapper);
	
	TranscriptView selectView(@Param("ew") Wrapper<TranscriptEntity> wrapper);
	
	List<TranscriptView> selectGroupBy(Pagination page,@Param("ew") Wrapper<TranscriptEntity> wrapper);

    List<Map<String, Object>> selectValue(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<TranscriptEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<TranscriptEntity> wrapper);

    List<Map<String, Object>> selectGroup(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<TranscriptEntity> wrapper);

    List<Map<String, Object>> kaoshichengjiSectionStat(@Param("params") Map<String, Object> params,@Param("ew") Wrapper<TranscriptEntity> wrapper);


}
