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
 * 用户
 * 数据库通用操作实体类（普通增删改查）
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
@TableName("mystudent")
public class MystudentEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;


	public MystudentEntity() {
		
	}
	
	public MystudentEntity(T t) {
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
	 * 教师账号
	 */
					
	private String teacheraccount;
	
	/**
	 * 教师姓名
	 */
					
	private String teachername;
	
	
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
	 * 设置：教师账号
	 */
	public void setTeacheraccount(String teacheraccount) {
		this.teacheraccount = teacheraccount;
	}
	/**
	 * 获取：教师账号
	 */
	public String getTeacheraccount() {
		return teacheraccount;
	}
	/**
	 * 设置：教师姓名
	 */
	public void setTeachername(String teachername) {
		this.teachername = teachername;
	}
	/**
	 * 获取：教师姓名
	 */
	public String getTeachername() {
		return teachername;
	}

}
