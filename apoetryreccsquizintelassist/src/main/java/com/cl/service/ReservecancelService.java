package com.cl.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.cl.utils.PageUtils;
import com.cl.entity.ReservecancelEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.ReservecancelView;


/**
 * 预约取消
 *
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
public interface ReservecancelService extends IService<ReservecancelEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
   	List<ReservecancelView> selectListView(Wrapper<ReservecancelEntity> wrapper);
   	
   	ReservecancelView selectView(@Param("ew") Wrapper<ReservecancelEntity> wrapper);
   	
   	PageUtils queryPage(Map<String, Object> params,Wrapper<ReservecancelEntity> wrapper);
   	
   	PageUtils queryPageGroupBy(Map<String, Object> params,Wrapper<ReservecancelEntity> wrapper);

}

