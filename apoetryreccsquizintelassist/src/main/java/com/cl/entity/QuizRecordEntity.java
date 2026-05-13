package com.cl.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.util.Date;

@TableName("quiz_record")
public class QuizRecordEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;
    private String studentaccount;
    private String studentname;
    private Long courseid;
    private String coursetitle;
    private Integer score;
    private Integer duration;
    private Integer questionsCount;
    private Integer correctCount;
    private String wrongListJson;
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
    public Integer getScore() { return score; }
    public void setScore(Integer v) { this.score = v; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer v) { this.duration = v; }
    public Integer getQuestionsCount() { return questionsCount; }
    public void setQuestionsCount(Integer v) { this.questionsCount = v; }
    public Integer getCorrectCount() { return correctCount; }
    public void setCorrectCount(Integer v) { this.correctCount = v; }
    public String getWrongListJson() { return wrongListJson; }
    public void setWrongListJson(String v) { this.wrongListJson = v; }
    public Date getAddtime() { return addtime; }
    public void setAddtime(Date v) { this.addtime = v; }
}
