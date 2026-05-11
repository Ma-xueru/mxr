package com.cl.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.cl.utils.PageUtils;
import com.cl.entity.StudentEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.StudentView;


/**
 * 用户
 *
 * @author 
 * @email 
 */
public interface StudentService extends IService<StudentEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<StudentView> selectListView(Wrapper<StudentEntity> wrapper);
   	
   	StudentView selectView(@Param("ew") Wrapper<StudentEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<StudentEntity> wrapper);
   	
   	PageUtils queryPageGroupBy(Map<String, Object> params,Wrapper<StudentEntity> wrapper);

    List<Map<String, Object>> selectValue(Map<String, Object> params,Wrapper<StudentEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params,Wrapper<StudentEntity> wrapper);

    List<Map<String, Object>> selectGroup(Map<String, Object> params,Wrapper<StudentEntity> wrapper);



}

