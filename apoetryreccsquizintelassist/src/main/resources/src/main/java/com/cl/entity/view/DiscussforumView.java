package com.cl.entity.view;

import com.cl.entity.DiscussforumEntity;

import com.baomidou.mybatisplus.annotations.TableName;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

import java.io.Serializable;
import com.cl.utils.EncryptUtil;
 

/**
 * 学习社区评论表
 * 后端返回视图实体辅助类   
 * （通常后端关联的表或者自定义的字段需要返回使用）
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
@TableName("discussforum")
public class DiscussforumView  extends DiscussforumEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public DiscussforumView(){
	}
 
 	public DiscussforumView(DiscussforumEntity discussforumEntity){
 	try {
			BeanUtils.copyProperties(this, discussforumEntity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		
	}


}
