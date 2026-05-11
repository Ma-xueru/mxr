package com.cl.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.cl.utils.PageUtils;
import com.cl.entity.CoursereserveEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.CoursereserveView;


/**
 * 预约课程
 *
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
public interface CoursereserveService extends IService<CoursereserveEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<CoursereserveView> selectListView(Wrapper<CoursereserveEntity> wrapper);
   	
   	CoursereserveView selectView(@Param("ew") Wrapper<CoursereserveEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<CoursereserveEntity> wrapper);
   	
   	PageUtils queryPageGroupBy(Map<String, Object> params,Wrapper<CoursereserveEntity> wrapper);

}

