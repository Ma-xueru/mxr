package com.cl.dao;

import com.cl.entity.ChatEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;

import com.cl.entity.view.ChatView;


/**
 * 在线客服
 * 
 * @author 
 * @email 
 * @date
 */
public interface ChatDao extends BaseMapper<ChatEntity> {
	

	
	List<ChatView> selectListView(@Param("ew") Wrapper<ChatEntity> wrapper);

	List<ChatView> selectListView(Pagination page,@Param("ew") Wrapper<ChatEntity> wrapper);
	
	ChatView selectView(@Param("ew") Wrapper<ChatEntity> wrapper);
	
}
