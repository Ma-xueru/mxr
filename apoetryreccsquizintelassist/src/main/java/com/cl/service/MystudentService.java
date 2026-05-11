package com.cl.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.cl.utils.PageUtils;
import com.cl.entity.MystudentEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.MystudentView;


/**
 * 用户
 *
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
public interface MystudentService extends IService<MystudentEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<MystudentView> selectListView(Wrapper<MystudentEntity> wrapper);
   	
   	MystudentView selectView(@Param("ew") Wrapper<MystudentEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<MystudentEntity> wrapper);
   	
   	PageUtils queryPageGroupBy(Map<String, Object> params,Wrapper<MystudentEntity> wrapper);

}

