package com.cl.entity.view;

import com.baomidou.mybatisplus.annotations.TableName;
import com.cl.entity.ClassinfoEntity;
import org.apache.commons.beanutils.BeanUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

@TableName("classinfo")
public class ClassinfoView extends ClassinfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    public ClassinfoView() {
    }

    public ClassinfoView(ClassinfoEntity entity) {
        try {
            BeanUtils.copyProperties(this, entity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
