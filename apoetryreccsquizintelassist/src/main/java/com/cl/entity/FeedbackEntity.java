package com.cl.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.lang.reflect.InvocationTargetException;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.beanutils.BeanUtils;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.enums.FieldFill;
import com.baomidou.mybatisplus.enums.IdType;


/**
 * 意见反馈
 * 数据库通用操作实体类（普通增删改查）
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
@TableName("feedback")
public class FeedbackEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;


	public FeedbackEntity() {
		
	}
	
	public FeedbackEntity(T t) {
		try {
			BeanUtils.copyProperties(this, t);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 主键id
	 */
	@TableId
	private Long id;
	/**
	 * 用户账号
	 */
					
	private String studentaccount;
	
	/**
	 * 用户姓名
	 */
					
	private String studentname;
	
	/**
	 * 反馈标题
	 */
					
	private String feedbacktitle;
	
	/**
	 * 反馈内容
	 */
					
	private String feedbackcontent;
	
	/**
	 * 反馈时间
	 */
				
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat 		
	private Date feedbacktime;
	
	/**
	 * 是否审核
	 */
					
	private String sfsh;
	
	/**
	 * 回复内容
	 */
					
	private String shhf;
	
	
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat
	private Date addtime;

	public Date getAddtime() {
		return addtime;
	}
	public void setAddtime(Date addtime) {
		this.addtime = addtime;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * 设置：用户账号
	 */
	public void setStudentaccount(String studentaccount) {
		this.studentaccount = studentaccount;
	}
	/**
	 * 获取：用户账号
	 */
	public String getStudentaccount() {
		return studentaccount;
	}
	/**
	 * 设置：用户姓名
	 */
	public void setStudentname(String studentname) {
		this.studentname = studentname;
	}
	/**
	 * 获取：用户姓名
	 */
	public String getStudentname() {
		return studentname;
	}
	/**
	 * 设置：反馈标题
	 */
	public void setFeedbacktitle(String feedbacktitle) {
		this.feedbacktitle = feedbacktitle;
	}
	/**
	 * 获取：反馈标题
	 */
	public String getFeedbacktitle() {
		return feedbacktitle;
	}
	/**
	 * 设置：反馈内容
	 */
	public void setFeedbackcontent(String feedbackcontent) {
		this.feedbackcontent = feedbackcontent;
	}
	/**
	 * 获取：反馈内容
	 */
	public String getFeedbackcontent() {
		return feedbackcontent;
	}
	/**
	 * 设置：反馈时间
	 */
	public void setFeedbacktime(Date feedbacktime) {
		this.feedbacktime = feedbacktime;
	}
	/**
	 * 获取：反馈时间
	 */
	public Date getFeedbacktime() {
		return feedbacktime;
	}
	/**
	 * 设置：是否审核
	 */
	public void setSfsh(String sfsh) {
		this.sfsh = sfsh;
	}
	/**
	 * 获取：是否审核
	 */
	public String getSfsh() {
		return sfsh;
	}
	/**
	 * 设置：回复内容
	 */
	public void setShhf(String shhf) {
		this.shhf = shhf;
	}
	/**
	 * 获取：回复内容
	 */
	public String getShhf() {
		return shhf;
	}

}
