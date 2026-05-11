package com.cl.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.cl.dao.ClasspkDao;
import com.cl.entity.ClasspkEntity;
import com.cl.entity.view.ClasspkView;
import com.cl.service.ClasspkService;
import com.cl.utils.PageUtils;
import com.cl.utils.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("classpkService")
public class ClasspkServiceImpl extends ServiceImpl<ClasspkDao, ClasspkEntity> implements ClasspkService {
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<ClasspkEntity> page = this.selectPage(
                new Query<ClasspkEntity>(params).getPage(),
                new EntityWrapper<ClasspkEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Wrapper<ClasspkEntity> wrapper) {
        Page<ClasspkView> page = new Query<ClasspkView>(params).getPage();
        page.setRecords(baseMapper.selectListView(page, wrapper));
        return new PageUtils(page);
    }

    @Override
    public List<ClasspkView> selectListView(Wrapper<ClasspkEntity> wrapper) {
        return baseMapper.selectListView(wrapper);
    }

    @Override
    public ClasspkView selectView(Wrapper<ClasspkEntity> wrapper) {
        return baseMapper.selectView(wrapper);
    }

    @Override
    public List<Map<String, Object>> selectValue(Map<String, Object> params, Wrapper<ClasspkEntity> wrapper) {
        return baseMapper.selectValue(params, wrapper);
    }

    @Override
    public List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params, Wrapper<ClasspkEntity> wrapper) {
        return baseMapper.selectTimeStatValue(params, wrapper);
    }

    @Override
    public List<Map<String, Object>> selectGroup(Map<String, Object> params, Wrapper<ClasspkEntity> wrapper) {
        return baseMapper.selectGroup(params, wrapper);
    }
}
