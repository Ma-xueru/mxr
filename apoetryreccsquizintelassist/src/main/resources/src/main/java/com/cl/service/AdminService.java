package com.cl.service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.cl.entity.AdminEntity;
import com.cl.utils.PageUtils;


/**
 * 系统用户
 */
public interface AdminService extends IService<AdminEntity> {
 	PageUtils queryPage(Map<String, Object> params);
    
   	List<AdminEntity> selectListView(Wrapper<AdminEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<AdminEntity> wrapper);
	   	
}
