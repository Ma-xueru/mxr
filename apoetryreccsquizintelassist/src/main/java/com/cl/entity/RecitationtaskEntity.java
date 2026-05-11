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
 * 背诵任务
 * 数据库通用操作实体类（普通增删改查）
 */
@TableName("recitationtask")
public class RecitationtaskEntity<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public RecitationtaskEntity() {
    }

    public RecitationtaskEntity(T t) {
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
    private String courseids;
    private String coursetitles;
    private String tasktitle;
    private String taskcontent;
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date deadline;
    private String completionstatus;
    private String completionremark;
    private String recitationaudio;
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date completiontime;
    private Integer kaoshichengji;
    private String recognizedtext;
    private String aiscorecomment;
    private String teachercomment;
    private String teacheraccount;
    private String teachername;
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date releasetime;
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
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

    public String getCourseids() {
        return courseids;
    }

    public void setCourseids(String courseids) {
        this.courseids = courseids;
    }

    public String getCoursetitles() {
        return coursetitles;
    }

    public void setCoursetitles(String coursetitles) {
        this.coursetitles = coursetitles;
    }

    public String getTasktitle() {
        return tasktitle;
    }

    public void setTasktitle(String tasktitle) {
        this.tasktitle = tasktitle;
    }

    public String getTaskcontent() {
        return taskcontent;
    }

    public void setTaskcontent(String taskcontent) {
        this.taskcontent = taskcontent;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public String getCompletionstatus() {
        return completionstatus;
    }

    public void setCompletionstatus(String completionstatus) {
        this.completionstatus = completionstatus;
    }

    public String getCompletionremark() {
        return completionremark;
    }

    public void setCompletionremark(String completionremark) {
        this.completionremark = completionremark;
    }

    public Date getCompletiontime() {
        return completiontime;
    }

    public void setCompletiontime(Date completiontime) {
        this.completiontime = completiontime;
    }

    public String getRecitationaudio() {
        return recitationaudio;
    }

    public void setRecitationaudio(String recitationaudio) {
        this.recitationaudio = recitationaudio;
    }

    public Integer getKaoshichengji() {
        return kaoshichengji;
    }

    public void setKaoshichengji(Integer kaoshichengji) {
        this.kaoshichengji = kaoshichengji;
    }

    public String getRecognizedtext() {
        return recognizedtext;
    }

    public void setRecognizedtext(String recognizedtext) {
        this.recognizedtext = recognizedtext;
    }

    public String getAiscorecomment() {
        return aiscorecomment;
    }

    public void setAiscorecomment(String aiscorecomment) {
        this.aiscorecomment = aiscorecomment;
    }

    public String getTeachercomment() {
        return teachercomment;
    }

    public void setTeachercomment(String teachercomment) {
        this.teachercomment = teachercomment;
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
