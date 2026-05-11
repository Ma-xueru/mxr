package com.cl.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.cl.dao.RecitationtaskDao;
import com.cl.entity.RecitationtaskEntity;
import com.cl.entity.view.RecitationtaskView;
import com.cl.service.RecitationtaskService;
import com.cl.utils.PageUtils;
import com.cl.utils.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("recitationtaskService")
public class RecitationtaskServiceImpl extends ServiceImpl<RecitationtaskDao, RecitationtaskEntity> implements RecitationtaskService {
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<RecitationtaskEntity> page = this.selectPage(
                new Query<RecitationtaskEntity>(params).getPage(),
                new EntityWrapper<RecitationtaskEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Wrapper<RecitationtaskEntity> wrapper) {
        Page<RecitationtaskView> page = new Query<RecitationtaskView>(params).getPage();
        page.setRecords(baseMapper.selectListView(page, wrapper));
        return new PageUtils(page);
    }

    @Override
    public List<RecitationtaskView> selectListView(Wrapper<RecitationtaskEntity> wrapper) {
        return baseMapper.selectListView(wrapper);
    }

    @Override
    public RecitationtaskView selectView(Wrapper<RecitationtaskEntity> wrapper) {
        return baseMapper.selectView(wrapper);
    }

    @Override
    public List<Map<String, Object>> selectValue(Map<String, Object> params, Wrapper<RecitationtaskEntity> wrapper) {
        return baseMapper.selectValue(params, wrapper);
    }

    @Override
    public List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params, Wrapper<RecitationtaskEntity> wrapper) {
        return baseMapper.selectTimeStatValue(params, wrapper);
    }

    @Override
    public List<Map<String, Object>> selectGroup(Map<String, Object> params, Wrapper<RecitationtaskEntity> wrapper) {
        return baseMapper.selectGroup(params, wrapper);
    }
}
