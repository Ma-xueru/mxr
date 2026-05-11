package com.cl.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.cl.utils.PageUtils;
import com.cl.entity.TranscriptEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.TranscriptView;


/**
 * 成绩信息
 *
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
public interface TranscriptService extends IService<TranscriptEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<TranscriptView> selectListView(Wrapper<TranscriptEntity> wrapper);
   	
   	TranscriptView selectView(@Param("ew") Wrapper<TranscriptEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<TranscriptEntity> wrapper);
   	
   	PageUtils queryPageGroupBy(Map<String, Object> params,Wrapper<TranscriptEntity> wrapper);

    List<Map<String, Object>> selectValue(Map<String, Object> params,Wrapper<TranscriptEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params,Wrapper<TranscriptEntity> wrapper);

    List<Map<String, Object>> selectGroup(Map<String, Object> params,Wrapper<TranscriptEntity> wrapper);

    List<Map<String, Object>> kaoshichengjiSectionStat(Map<String, Object> params,Wrapper<TranscriptEntity> wrapper);


}

