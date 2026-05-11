package com.cl.entity.view;

import com.cl.entity.TranscriptEntity;

import com.baomidou.mybatisplus.annotations.TableName;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

import java.io.Serializable;
import com.cl.utils.EncryptUtil;
 

/**
 * 成绩信息
 * 后端返回视图实体辅助类   
 * （通常后端关联的表或者自定义的字段需要返回使用）
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
@TableName("transcript")
public class TranscriptView  extends TranscriptEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public TranscriptView(){
	}
 
 	public TranscriptView(TranscriptEntity transcriptEntity){
 	try {
			BeanUtils.copyProperties(this, transcriptEntity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		
	}


}
