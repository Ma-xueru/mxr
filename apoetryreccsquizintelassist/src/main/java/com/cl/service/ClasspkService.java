package com.cl.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.cl.entity.ClasspkEntity;
import com.cl.entity.view.ClasspkView;
import com.cl.utils.PageUtils;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ClasspkService extends IService<ClasspkEntity> {
    PageUtils queryPage(Map<String, Object> params);

    List<ClasspkView> selectListView(Wrapper<ClasspkEntity> wrapper);

    ClasspkView selectView(@Param("ew") Wrapper<ClasspkEntity> wrapper);

    PageUtils queryPage(Map<String, Object> params, Wrapper<ClasspkEntity> wrapper);

    List<Map<String, Object>> selectValue(Map<String, Object> params, Wrapper<ClasspkEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params, Wrapper<ClasspkEntity> wrapper);

    List<Map<String, Object>> selectGroup(Map<String, Object> params, Wrapper<ClasspkEntity> wrapper);
}
