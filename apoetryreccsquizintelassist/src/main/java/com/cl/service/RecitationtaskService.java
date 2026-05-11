package com.cl.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.cl.entity.RecitationtaskEntity;
import com.cl.entity.view.RecitationtaskView;
import com.cl.utils.PageUtils;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 背诵任务
 */
public interface RecitationtaskService extends IService<RecitationtaskEntity> {
    PageUtils queryPage(Map<String, Object> params);

    List<RecitationtaskView> selectListView(Wrapper<RecitationtaskEntity> wrapper);

    RecitationtaskView selectView(@Param("ew") Wrapper<RecitationtaskEntity> wrapper);

    PageUtils queryPage(Map<String, Object> params, Wrapper<RecitationtaskEntity> wrapper);

    List<Map<String, Object>> selectValue(Map<String, Object> params, Wrapper<RecitationtaskEntity> wrapper);

    List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params, Wrapper<RecitationtaskEntity> wrapper);

    List<Map<String, Object>> selectGroup(Map<String, Object> params, Wrapper<RecitationtaskEntity> wrapper);
}
