package com.cl.entity.view;

import com.baomidou.mybatisplus.annotations.TableName;
import com.cl.entity.ClasspkEntity;
import org.apache.commons.beanutils.BeanUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

@TableName("classpk")
public class ClasspkView extends ClasspkEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    public ClasspkView() {
    }

    public ClasspkView(ClasspkEntity entity) {
        try {
            BeanUtils.copyProperties(this, entity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
