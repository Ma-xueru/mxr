package com.cl.entity.view;

import com.cl.entity.ReservecancelEntity;

import com.baomidou.mybatisplus.annotations.TableName;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

import java.io.Serializable;
import com.cl.utils.EncryptUtil;
 

/**
 * 预约取消
 * 后端返回视图实体辅助类   
 * （通常后端关联的表或者自定义的字段需要返回使用）
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
@TableName("reservecancel")
public class ReservecancelView  extends ReservecancelEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public ReservecancelView(){
	}
 
 	public ReservecancelView(ReservecancelEntity reservecancelEntity){
 	try {
			BeanUtils.copyProperties(this, reservecancelEntity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		
	}


}
