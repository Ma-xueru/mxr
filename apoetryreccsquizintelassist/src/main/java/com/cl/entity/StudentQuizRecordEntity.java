package com.cl.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable; import java.util.Date;

@TableName("student_quiz_record")
public class StudentQuizRecordEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId private Long id;
    private Long taskId; private String studentaccount; private String studentname;
    private Long courseId; private String courseTitle;
    private Integer score; private Integer totalQuestions; private Integer correctCount;
    private String answersJson; private String aiReport; private String wrongListJson;
    @JsonFormat(locale="zh",timezone="GMT+8",pattern="yyyy-MM-dd HH:mm:ss") @DateTimeFormat private Date addtime;
    public Long getId(){return id;} public void setId(Long v){this.id=v;}
    public Long getTaskId(){return taskId;} public void setTaskId(Long v){this.taskId=v;}
    public String getStudentaccount(){return studentaccount;} public void setStudentaccount(String v){this.studentaccount=v;}
    public String getStudentname(){return studentname;} public void setStudentname(String v){this.studentname=v;}
    public Long getCourseId(){return courseId;} public void setCourseId(Long v){this.courseId=v;}
    public String getCourseTitle(){return courseTitle;} public void setCourseTitle(String v){this.courseTitle=v;}
    public Integer getScore(){return score;} public void setScore(Integer v){this.score=v;}
    public Integer getTotalQuestions(){return totalQuestions;} public void setTotalQuestions(Integer v){this.totalQuestions=v;}
    public Integer getCorrectCount(){return correctCount;} public void setCorrectCount(Integer v){this.correctCount=v;}
    public String getAnswersJson(){return answersJson;} public void setAnswersJson(String v){this.answersJson=v;}
    public String getAiReport(){return aiReport;} public void setAiReport(String v){this.aiReport=v;}
    public String getWrongListJson(){return wrongListJson;} public void setWrongListJson(String v){this.wrongListJson=v;}
    public Date getAddtime(){return addtime;} public void setAddtime(Date v){this.addtime=v;}
}
