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
 */
@TableName("student")
public class StudentEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;


	public StudentEntity() {
		
	}
	
	public StudentEntity(T t) {
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
	 * 用户密码
	 */
					
	private String studentpassword;
	
	/**
	 * 用户姓名
	 */
					
	private String studentname;
	
	/**
	 * 头像
	 */
					
	private String avatar;
	
	/**
	 * 性别
	 */
					
	private String gender;
	
	/**
	 * 手机号码
	 */
					
	private String telephone;
	
	
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
	 * 设置：用户密码
	 */
	public void setStudentpassword(String studentpassword) {
		this.studentpassword = studentpassword;
	}
	/**
	 * 获取：用户密码
	 */
	public String getStudentpassword() {
		return studentpassword;
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
	 * 设置：头像
	 */
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	/**
	 * 获取：头像
	 */
	public String getAvatar() {
		return avatar;
	}
	/**
	 * 设置：性别
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}
	/**
	 * 获取：性别
	 */
	public String getGender() {
		return gender;
	}
	/**
	 * 设置：手机号码
	 */
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	/**
	 * 获取：手机号码
	 */
	public String getTelephone() {
		return telephone;
	}

}
