package com.cl.entity.view;

import com.cl.entity.ExamrecordEntity;

import com.baomidou.mybatisplus.annotations.TableName;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Date;

import java.io.Serializable;
import com.cl.utils.EncryptUtil;
 

/**
 * 测试记录
 * 后端返回视图实体辅助类   
 * （通常后端关联的表或者自定义的字段需要返回使用）
 * @author 
 * @email 
 */
@TableName("examrecord")
public class ExamrecordView  extends ExamrecordEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public ExamrecordView(){
	}
 
 	public ExamrecordView(ExamrecordEntity examrecordEntity){
 	try {
			BeanUtils.copyProperties(this, examrecordEntity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		
	}


    private BigDecimal accuracy;

    public BigDecimal getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(BigDecimal accuracy) {
        this.accuracy = accuracy;
    }

    private Integer questionCount;

    private Long totalScore;

    private Date releasetime;

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public Long getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Long totalScore) {
        this.totalScore = totalScore;
    }

    public Date getReleasetime() {
        return releasetime;
    }

    public void setReleasetime(Date releasetime) {
        this.releasetime = releasetime;
    }
}
