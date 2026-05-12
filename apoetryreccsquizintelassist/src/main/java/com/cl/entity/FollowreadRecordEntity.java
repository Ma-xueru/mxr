package com.cl.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.util.Date;

@TableName("followread_record")
public class FollowreadRecordEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;
    private String studentaccount;
    private String studentname;
    private Long courseid;
    private String coursetitle;
    private Integer totalscore;
    private String reportjson;
    private String recognizedtext;
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addtime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStudentaccount() { return studentaccount; }
    public void setStudentaccount(String v) { this.studentaccount = v; }
    public String getStudentname() { return studentname; }
    public void setStudentname(String v) { this.studentname = v; }
    public Long getCourseid() { return courseid; }
    public void setCourseid(Long v) { this.courseid = v; }
    public String getCoursetitle() { return coursetitle; }
    public void setCoursetitle(String v) { this.coursetitle = v; }
    public Integer getTotalscore() { return totalscore; }
    public void setTotalscore(Integer v) { this.totalscore = v; }
    public String getReportjson() { return reportjson; }
    public void setReportjson(String v) { this.reportjson = v; }
    public String getRecognizedtext() { return recognizedtext; }
    public void setRecognizedtext(String v) { this.recognizedtext = v; }
    public Date getAddtime() { return addtime; }
    public void setAddtime(Date v) { this.addtime = v; }
}
