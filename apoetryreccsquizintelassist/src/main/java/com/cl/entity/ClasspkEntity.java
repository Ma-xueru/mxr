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
 * 班级PK
 */
@TableName("classpk")
public class ClasspkEntity<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public ClasspkEntity() {
    }

    public ClasspkEntity(T t) {
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
    private String classname;
    private String opponentaccount;
    private String opponentname;
    private Integer myscore;
    private Integer opponentscore;
    private String winneraccount;
    private String winnername;
    private Integer medalreward;
    private String pkstatus;
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date pktime;
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addtime;

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

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getOpponentaccount() {
        return opponentaccount;
    }

    public void setOpponentaccount(String opponentaccount) {
        this.opponentaccount = opponentaccount;
    }

    public String getOpponentname() {
        return opponentname;
    }

    public void setOpponentname(String opponentname) {
        this.opponentname = opponentname;
    }

    public Integer getMyscore() {
        return myscore;
    }

    public void setMyscore(Integer myscore) {
        this.myscore = myscore;
    }

    public Integer getOpponentscore() {
        return opponentscore;
    }

    public void setOpponentscore(Integer opponentscore) {
        this.opponentscore = opponentscore;
    }

    public String getWinneraccount() {
        return winneraccount;
    }

    public void setWinneraccount(String winneraccount) {
        this.winneraccount = winneraccount;
    }

    public String getWinnername() {
        return winnername;
    }

    public void setWinnername(String winnername) {
        this.winnername = winnername;
    }

    public Integer getMedalreward() {
        return medalreward;
    }

    public void setMedalreward(Integer medalreward) {
        this.medalreward = medalreward;
    }

    public String getPkstatus() {
        return pkstatus;
    }

    public void setPkstatus(String pkstatus) {
        this.pkstatus = pkstatus;
    }

    public Date getPktime() {
        return pktime;
    }

    public void setPktime(Date pktime) {
        this.pktime = pktime;
    }

    public Date getAddtime() {
        return addtime;
    }

    public void setAddtime(Date addtime) {
        this.addtime = addtime;
    }
}
