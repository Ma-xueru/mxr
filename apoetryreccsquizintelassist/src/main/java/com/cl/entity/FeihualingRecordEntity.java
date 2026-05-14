package com.cl.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.util.Date;

@TableName("feihualing_records")
public class FeihualingRecordEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId private Long id;
    private String userId;
    private String username;
    private String keyword;
    private Integer rounds;
    private Integer score;
    private Integer maxCombo;
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat private Date addtime;

    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public String getUserId() { return userId; } public void setUserId(String v) { this.userId = v; }
    public String getUsername() { return username; } public void setUsername(String v) { this.username = v; }
    public String getKeyword() { return keyword; } public void setKeyword(String v) { this.keyword = v; }
    public Integer getRounds() { return rounds; } public void setRounds(Integer v) { this.rounds = v; }
    public Integer getScore() { return score; } public void setScore(Integer v) { this.score = v; }
    public Integer getMaxCombo() { return maxCombo; } public void setMaxCombo(Integer v) { this.maxCombo = v; }
    public Date getAddtime() { return addtime; } public void setAddtime(Date v) { this.addtime = v; }
}
