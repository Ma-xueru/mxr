package com.cl.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.cl.entity.ClassinfoEntity;
import com.cl.entity.view.ClassinfoView;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ClassinfoDao extends BaseMapper<ClassinfoEntity> {
    List<ClassinfoView> selectListView(@Param("ew") Wrapper<ClassinfoEntity> wrapper);

    List<ClassinfoView> selectListView(Pagination page, @Param("ew") Wrapper<ClassinfoEntity> wrapper);

    ClassinfoView selectView(@Param("ew") Wrapper<ClassinfoEntity> wrapper);

    List<Map<String, Object>> selectValue(@Param("params") Map<String, Object> params, @Param("ew") Wrapper<ClassinfoEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(@Param("params") Map<String, Object> params, @Param("ew") Wrapper<ClassinfoEntity> wrapper);

    List<Map<String, Object>> selectGroup(@Param("params") Map<String, Object> params, @Param("ew") Wrapper<ClassinfoEntity> wrapper);
}
