package com.cl.entity.view;

import com.baomidou.mybatisplus.annotations.TableName;
import com.cl.entity.RecitationtaskEntity;
import org.apache.commons.beanutils.BeanUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * 背诵任务
 * 后端返回视图实体辅助类
 */
@TableName("recitationtask")
public class RecitationtaskView extends RecitationtaskEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    public RecitationtaskView() {
    }

    public RecitationtaskView(RecitationtaskEntity entity) {
        try {
            BeanUtils.copyProperties(this, entity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
