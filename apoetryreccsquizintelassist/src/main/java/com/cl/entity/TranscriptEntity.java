package com.cl.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

/**
 * 成绩信息
 * 数据库通用操作实体类（普通增删改查）
 */
@TableName("transcript")
public class TranscriptEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;

	public TranscriptEntity() {
	}

	public TranscriptEntity(T t) {
		try {
			BeanUtils.copyProperties(this, t);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@TableId
	private Long id;
	private String studentaccount;
	private String studentname;
	private Integer kaoshichengji;
	private String teacheraccount;
	private String teachername;
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat
	private Date releasetime;
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

	public String getStudentaccount() {
		return studentaccount;
	}

	public void setStudentaccount(String studentaccount) {
		this.studentaccount = studentaccount;
	}

	public String getStudentname() {
		return studentname;
	}

	public void setStudentname(String studentname) {
		this.studentname = studentname;
	}

	public Integer getKaoshichengji() {
		return kaoshichengji;
	}

	public void setKaoshichengji(Integer kaoshichengji) {
		this.kaoshichengji = kaoshichengji;
	}

	public String getTeacheraccount() {
		return teacheraccount;
	}

	public void setTeacheraccount(String teacheraccount) {
		this.teacheraccount = teacheraccount;
	}

	public String getTeachername() {
		return teachername;
	}

	public void setTeachername(String teachername) {
		this.teachername = teachername;
	}

	public Date getReleasetime() {
		return releasetime;
	}

	public void setReleasetime(Date releasetime) {
		this.releasetime = releasetime;
	}
}
