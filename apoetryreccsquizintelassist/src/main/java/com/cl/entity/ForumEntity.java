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
 * 学习社区
 * 数据库通用操作实体类（普通增删改查）
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
@TableName("forum")
public class ForumEntity<T> implements Serializable {
	private static final long serialVersionUID = 1L;


	public ForumEntity() {
		
	}
	
	public ForumEntity(T t) {
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
	 * 图片
	 */
					
	private String picture;
	
	/**
	 * 贴子标题
	 */
					
	private String forumtitle;
	
	/**
	 * 帖子内容
	 */
					
	private String forumcontent;
	
	/**
	 * 发布时间
	 */
				
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd")
	@DateTimeFormat 		
	private Date releasetime;
	
	/**
	 * 用户id
	 */
					
	private Long userid;
	
	/**
	 * 赞
	 */
					
	private Integer thumbsupnum;
	
	/**
	 * 踩
	 */
					
	private Integer crazilynum;
	
	
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
	 * 设置：图片
	 */
	public void setPicture(String picture) {
		this.picture = picture;
	}
	/**
	 * 获取：图片
	 */
	public String getPicture() {
		return picture;
	}
	/**
	 * 设置：贴子标题
	 */
	public void setForumtitle(String forumtitle) {
		this.forumtitle = forumtitle;
	}
	/**
	 * 获取：贴子标题
	 */
	public String getForumtitle() {
		return forumtitle;
	}
	/**
	 * 设置：帖子内容
	 */
	public void setForumcontent(String forumcontent) {
		this.forumcontent = forumcontent;
	}
	/**
	 * 获取：帖子内容
	 */
	public String getForumcontent() {
		return forumcontent;
	}
	/**
	 * 设置：发布时间
	 */
	public void setReleasetime(Date releasetime) {
		this.releasetime = releasetime;
	}
	/**
	 * 获取：发布时间
	 */
	public Date getReleasetime() {
		return releasetime;
	}
	/**
	 * 设置：用户id
	 */
	public void setUserid(Long userid) {
		this.userid = userid;
	}
	/**
	 * 获取：用户id
	 */
	public Long getUserid() {
		return userid;
	}
	/**
	 * 设置：赞
	 */
	public void setThumbsupnum(Integer thumbsupnum) {
		this.thumbsupnum = thumbsupnum;
	}
	/**
	 * 获取：赞
	 */
	public Integer getThumbsupnum() {
		return thumbsupnum;
	}
	/**
	 * 设置：踩
	 */
	public void setCrazilynum(Integer crazilynum) {
		this.crazilynum = crazilynum;
	}
	/**
	 * 获取：踩
	 */
	public Integer getCrazilynum() {
		return crazilynum;
	}

}
