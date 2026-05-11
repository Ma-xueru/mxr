package com.cl.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.cl.utils.PageUtils;
import com.cl.entity.TeacherEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.TeacherView;


/**
 * 教师
 *
 * @author 
 * @email 
 */
public interface TeacherService extends IService<TeacherEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<TeacherView> selectListView(Wrapper<TeacherEntity> wrapper);
   	
   	TeacherView selectView(@Param("ew") Wrapper<TeacherEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<TeacherEntity> wrapper);
   	
   	PageUtils queryPageGroupBy(Map<String, Object> params,Wrapper<TeacherEntity> wrapper);

    List<Map<String, Object>> selectValue(Map<String, Object> params,Wrapper<TeacherEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params,Wrapper<TeacherEntity> wrapper);

    List<Map<String, Object>> selectGroup(Map<String, Object> params,Wrapper<TeacherEntity> wrapper);



}

