package com.cl.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.cl.entity.RecitationtaskEntity;
import com.cl.entity.view.RecitationtaskView;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 背诵任务
 */
public interface RecitationtaskDao extends BaseMapper<RecitationtaskEntity> {
    List<RecitationtaskView> selectListView(@Param("ew") Wrapper<RecitationtaskEntity> wrapper);

    List<RecitationtaskView> selectListView(Pagination page, @Param("ew") Wrapper<RecitationtaskEntity> wrapper);

    RecitationtaskView selectView(@Param("ew") Wrapper<RecitationtaskEntity> wrapper);

    List<Map<String, Object>> selectValue(@Param("params") Map<String, Object> params, @Param("ew") Wrapper<RecitationtaskEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(@Param("params") Map<String, Object> params, @Param("ew") Wrapper<RecitationtaskEntity> wrapper);

    List<Map<String, Object>> selectGroup(@Param("params") Map<String, Object> params, @Param("ew") Wrapper<RecitationtaskEntity> wrapper);
}
