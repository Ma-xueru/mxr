package com.cl.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.util.Date;

@TableName("student_score_log")
public class StudentScoreLogEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;
    private Long studentId;
    private String studentaccount;
    private String studentname;
    private String classname;
    private Long poetryId;
    private String poetryTitle;
    private Integer sourceType;
    private Integer score;
    private Integer knowledgeScore;
    private Integer accuracyScore;
    private Integer depthScore;
    private String audioUrl;
    private String learningSuggestion;
    private String overallSummary;
    private String reportJson;
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long v) { this.studentId = v; }
    public String getStudentaccount() { return studentaccount; }
    public void setStudentaccount(String v) { this.studentaccount = v; }
    public String getStudentname() { return studentname; }
    public void setStudentname(String v) { this.studentname = v; }
    public String getClassname() { return classname; }
    public void setClassname(String v) { this.classname = v; }
    public Long getPoetryId() { return poetryId; }
    public void setPoetryId(Long v) { this.poetryId = v; }
    public String getPoetryTitle() { return poetryTitle; }
    public void setPoetryTitle(String v) { this.poetryTitle = v; }
    public Integer getSourceType() { return sourceType; }
    public void setSourceType(Integer v) { this.sourceType = v; }
    public Integer getScore() { return score; }
    public void setScore(Integer v) { this.score = v; }
    public Integer getKnowledgeScore() { return knowledgeScore; }
    public void setKnowledgeScore(Integer v) { this.knowledgeScore = v; }
    public Integer getAccuracyScore() { return accuracyScore; }
    public void setAccuracyScore(Integer v) { this.accuracyScore = v; }
    public Integer getDepthScore() { return depthScore; }
    public void setDepthScore(Integer v) { this.depthScore = v; }
    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String v) { this.audioUrl = v; }
    public String getLearningSuggestion() { return learningSuggestion; }
    public void setLearningSuggestion(String v) { this.learningSuggestion = v; }
    public String getOverallSummary() { return overallSummary; }
    public void setOverallSummary(String v) { this.overallSummary = v; }
    public String getReportJson() { return reportJson; }
    public void setReportJson(String v) { this.reportJson = v; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date v) { this.createTime = v; }
}
