package com.cl.dao;

import com.cl.entity.ReservecancelEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.cl.entity.view.ReservecancelView;


/**
 * 预约取消
 * 
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
public interface ReservecancelDao extends BaseMapper<ReservecancelEntity> {
	
	List<ReservecancelView> selectListView(@Param("ew") Wrapper<ReservecancelEntity> wrapper);

	List<ReservecancelView> selectListView(Pagination page,@Param("ew") Wrapper<ReservecancelEntity> wrapper);
	
	ReservecancelView selectView(@Param("ew") Wrapper<ReservecancelEntity> wrapper);
	
	List<ReservecancelView> selectGroupBy(Pagination page,@Param("ew") Wrapper<ReservecancelEntity> wrapper);

}
