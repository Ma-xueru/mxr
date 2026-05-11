package com.cl.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.cl.entity.ClassinfoEntity;
import com.cl.entity.view.ClassinfoView;
import com.cl.utils.PageUtils;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ClassinfoService extends IService<ClassinfoEntity> {
    PageUtils queryPage(Map<String, Object> params);

    List<ClassinfoView> selectListView(Wrapper<ClassinfoEntity> wrapper);

    ClassinfoView selectView(@Param("ew") Wrapper<ClassinfoEntity> wrapper);

    PageUtils queryPage(Map<String, Object> params, Wrapper<ClassinfoEntity> wrapper);

    List<Map<String, Object>> selectValue(Map<String, Object> params, Wrapper<ClassinfoEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params, Wrapper<ClassinfoEntity> wrapper);

    List<Map<String, Object>> selectGroup(Map<String, Object> params, Wrapper<ClassinfoEntity> wrapper);
}
