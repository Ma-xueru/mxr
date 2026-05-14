package com.cl.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import java.io.Serializable;

@TableName("quiz_questions")
public class QuizQuestionEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId private Long id;
    private Long taskId; private Long courseId;
    private String question; private String optionsJson;
    private Integer answer; private String analysis; private Integer sortOrder;
    public Long getId(){return id;} public void setId(Long v){this.id=v;}
    public Long getTaskId(){return taskId;} public void setTaskId(Long v){this.taskId=v;}
    public Long getCourseId(){return courseId;} public void setCourseId(Long v){this.courseId=v;}
    public String getQuestion(){return question;} public void setQuestion(String v){this.question=v;}
    public String getOptionsJson(){return optionsJson;} public void setOptionsJson(String v){this.optionsJson=v;}
    public Integer getAnswer(){return answer;} public void setAnswer(Integer v){this.answer=v;}
    public String getAnalysis(){return analysis;} public void setAnalysis(String v){this.analysis=v;}
    public Integer getSortOrder(){return sortOrder;} public void setSortOrder(Integer v){this.sortOrder=v;}
}
