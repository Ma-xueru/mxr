package com.cl.entity.view;

import com.cl.entity.DiscusscourseEntity;

import com.baomidou.mybatisplus.annotations.TableName;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

import java.io.Serializable;
import com.cl.utils.EncryptUtil;
 

/**
 * 古诗词评论表
 * 后端返回视图实体辅助类   
 * （通常后端关联的表或者自定义的字段需要返回使用）
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
@TableName("discusscourse")
public class DiscusscourseView  extends DiscusscourseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public DiscusscourseView(){
	}
 
 	public DiscusscourseView(DiscusscourseEntity discusscourseEntity){
 	try {
			BeanUtils.copyProperties(this, discusscourseEntity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}


}