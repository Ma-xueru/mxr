package com.cl.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.cl.dao.ClassinfoDao;
import com.cl.entity.ClassinfoEntity;
import com.cl.entity.view.ClassinfoView;
import com.cl.service.ClassinfoService;
import com.cl.utils.PageUtils;
import com.cl.utils.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("classinfoService")
public class ClassinfoServiceImpl extends ServiceImpl<ClassinfoDao, ClassinfoEntity> implements ClassinfoService {
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<ClassinfoEntity> page = this.selectPage(
                new Query<ClassinfoEntity>(params).getPage(),
                new EntityWrapper<ClassinfoEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Wrapper<ClassinfoEntity> wrapper) {
        Page<ClassinfoView> page = new Query<ClassinfoView>(params).getPage();
        page.setRecords(baseMapper.selectListView(page, wrapper));
        return new PageUtils(page);
    }

    @Override
    public List<ClassinfoView> selectListView(Wrapper<ClassinfoEntity> wrapper) {
        return baseMapper.selectListView(wrapper);
    }

    @Override
    public ClassinfoView selectView(Wrapper<ClassinfoEntity> wrapper) {
        return baseMapper.selectView(wrapper);
    }

    @Override
    public List<Map<String, Object>> selectValue(Map<String, Object> params, Wrapper<ClassinfoEntity> wrapper) {
        return baseMapper.selectValue(params, wrapper);
    }

    @Override
    public List<Map<String, Object>> selectTimeStatValue(Map<String, Object> params, Wrapper<ClassinfoEntity> wrapper) {
        return baseMapper.selectTimeStatValue(params, wrapper);
    }

    @Override
    public List<Map<String, Object>> selectGroup(Map<String, Object> params, Wrapper<ClassinfoEntity> wrapper) {
        return baseMapper.selectGroup(params, wrapper);
    }
}
