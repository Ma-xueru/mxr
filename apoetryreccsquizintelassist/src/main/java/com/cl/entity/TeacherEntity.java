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
 * 教师
 * 数据库通用操作实体类（普通增删改查）
 * @author 
 * @email 
 */
@TableName("teacher")
public class TeacherEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;


	public TeacherEntity() {
		
	}
	
	public TeacherEntity(T t) {
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
	 * 教师账号
	 */
					
	private String teacheraccount;
	
	/**
	 * 教师密码
	 */
					
	private String teacherpassword;
	
	/**
	 * 教师姓名
	 */
					
	private String teachername;
	
	/**
	 * 照片
	 */
					
	private String zhaopian;
	
	/**
	 * 性别
	 */
					
	private String gender;
	
	/**
	 * 联系电话
	 */
					
	private String lianxidianhua;
	
	/**
	 * 可约人数
	 */
					
	private Integer reservecount;

	/**
	 * 权限状态
	 */
	private String permissionstatus;
	private String grade;
	private Long classId;
	private String classname;
	
	
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
	 * 设置：教师密码
	 */
	public void setTeacherpassword(String teacherpassword) {
		this.teacherpassword = teacherpassword;
	}
	/**
	 * 获取：教师密码
	 */
	public String getTeacherpassword() {
		return teacherpassword;
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
	/**
	 * 设置：照片
	 */
	public void setZhaopian(String zhaopian) {
		this.zhaopian = zhaopian;
	}
	/**
	 * 获取：照片
	 */
	public String getZhaopian() {
		return zhaopian;
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
	 * 设置：联系电话
	 */
	public void setLianxidianhua(String lianxidianhua) {
		this.lianxidianhua = lianxidianhua;
	}
	/**
	 * 获取：联系电话
	 */
	public String getLianxidianhua() {
		return lianxidianhua;
	}
	/**
	 * 设置：可约人数
	 */
	public void setReservecount(Integer reservecount) {
		this.reservecount = reservecount;
	}
	/**
	 * 获取：可约人数
	 */
	public Integer getReservecount() {
		return reservecount;
	}

	/**
	 * 设置：权限状态
	 */
	public void setPermissionstatus(String permissionstatus) {
		this.permissionstatus = permissionstatus;
	}
	/**
	 * 获取：权限状态
	 */
	public String getPermissionstatus() {
		return permissionstatus;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}
	public String getGrade() {
		return grade;
	}

	public void setClassId(Long classId) {
		this.classId = classId;
	}
	public Long getClassId() {
		return classId;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}
	public String getClassname() {
		return classname;
	}

}
