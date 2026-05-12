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
	 * 古诗词
	 * 数据库通用操作实体类（普通增删改查）
	 * @author
	 * @email
	 * @date 2026-01-25 11:35:29
	 */
	@TableName("course")
	public class CourseEntity<T> implements Serializable {
		private static final long serialVersionUID = 1L;


		public CourseEntity() {

		}

		public CourseEntity(T t) {
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
		 * 古诗词号
		 */

		private String courseno;

		/**
		 * 古诗词标题
		 */

		private String coursetitle;

		/**
		 * 古诗词类型
		 */

		private String coursetype;

		/**
		 * 适用年级
		 */

		private String grade;

		/**
		 * 古诗词封面
		 */

		private String picture;

		/**
		 * 古诗词简介
		 */

		private String intro;

		/**
		 * 古诗词详情
		 */

		private String content;

		/**
		 * 视频
		 */

		private String video;

		/**
		 * 拼音内容
		 */

		private String contentpinyin;

		/**
		 * 点赞数
		 */

		private Integer thumbsupnum;

		/**
		 * 点踩数
		 */

		private Integer crazilynum;

		/**
		 * 点击数
		 */

		private Integer clicknum;

		/**
		 * 最后更新时间
		 */

		@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
		@DateTimeFormat
		private Date addtime;
		@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
		@DateTimeFormat
		private Date clicktime;
		public Date getAddtime() {
			return addtime;
		}
		public void setAddtime(Date addtime) {
			this.addtime = addtime;
		}

		/**
		 * 设置：主键id
		 */
		public void setId(Long id) {
			this.id = id;
		}
		/**
		 * 获取：主键id
		 */
		public Long getId() {
			return id;
		}
		/**
		 * 设置：古诗词号
		 */
		public void setCourseno(String courseno) {
			this.courseno = courseno;
		}
		/**
		 * 获取：古诗词号
		 */
		public String getCourseno() {
			return courseno;
		}
		/**
		 * 设置：古诗词标题
		 */
		public void setCoursetitle(String coursetitle) {
			this.coursetitle = coursetitle;
		}
		/**
		 * 获取：古诗词标题
		 */
		public String getCoursetitle() {
			return coursetitle;
		}
		/**
		 * 设置：古诗词类型
		 */
		public void setCoursetype(String coursetype) {
			this.coursetype = coursetype;
		}
		/**
		 * 获取：古诗词类型
		 */
		public String getCoursetype() {
			return coursetype;
		}
		/**
		 * 设置：适用年级
		 */
		public void setGrade(String grade) {
			this.grade = grade;
		}
		/**
		 * 获取：适用年级
		 */
		public String getGrade() {
			return grade;
		}
		/**
		 * 设置：古诗词封面
		 */
		public void setPicture(String picture) {
			this.picture = picture;
		}
		/**
		 * 获取：古诗词封面
		 */
		public String getPicture() {
			return picture;
		}
		/**
		 * 设置：古诗词简介
		 */
		public void setIntro(String intro) {
			this.intro = intro;
		}
		/**
		 * 获取：古诗词简介
		 */
		public String getIntro() {
			return intro;
		}
		/**
		 * 设置：古诗词详情
		 */
		public void setContent(String content) {
			this.content = content;
		}
		/**
		 * 获取：古诗词详情
		 */
		public String getContent() {
			return content;
		}
		/**
		 * 设置：视频
		 */
		public void setVideo(String video) {
			this.video = video;
		}
		/**
		 * 获取：视频
		 */
		public String getVideo() {
			return video;
		}
		/**
		 * 设置：点赞数
		 */
		public void setThumbsupnum(Integer thumbsupnum) {
			this.thumbsupnum = thumbsupnum;
		}
		/**
		 * 获取：点赞数
		 */
		public Integer getThumbsupnum() {
			return thumbsupnum;
		}
		/**
		 * 设置：点踩数
		 */
		public void setCrazilynum(Integer crazilynum) {
			this.crazilynum = crazilynum;
		}
		/**
		 * 获取：点踩数
		 */
		public Integer getCrazilynum() {
			return crazilynum;
		}
		/**
		 * 设置：点击数
		 */
		public void setClicknum(Integer clicknum) {
			this.clicknum = clicknum;
		}
		/**
		 * 获取：点击数
		 */
		public Integer getClicknum() {
			return clicknum;
		}

		/**
		 * 设置：最后点击时间
		 */
		public void setClicktime(Date clicktime) {
			this.clicktime = clicktime;
		}
		/**
		 * 获取：最后点击时间
		 */
		public Date getClicktime() {
			return clicktime;
		}
	}
