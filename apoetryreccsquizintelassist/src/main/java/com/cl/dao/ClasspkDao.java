package com.cl.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.cl.entity.ClasspkEntity;
import com.cl.entity.view.ClasspkView;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ClasspkDao extends BaseMapper<ClasspkEntity> {
    List<ClasspkView> selectListView(@Param("ew") Wrapper<ClasspkEntity> wrapper);

    List<ClasspkView> selectListView(Pagination page, @Param("ew") Wrapper<ClasspkEntity> wrapper);

    ClasspkView selectView(@Param("ew") Wrapper<ClasspkEntity> wrapper);

    List<Map<String, Object>> selectValue(@Param("params") Map<String, Object> params, @Param("ew") Wrapper<ClasspkEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(@Param("params") Map<String, Object> params, @Param("ew") Wrapper<ClasspkEntity> wrapper);

    List<Map<String, Object>> selectGroup(@Param("params") Map<String, Object> params, @Param("ew") Wrapper<ClasspkEntity> wrapper);
}
