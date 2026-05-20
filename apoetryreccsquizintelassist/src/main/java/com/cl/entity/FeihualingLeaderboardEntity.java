package com.cl.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import java.io.Serializable;
import java.util.Date;

@TableName("feihualing_leaderboard")
public class FeihualingLeaderboardEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId private String userId;
    private String username;
    private String classname;
    private Integer maxScore;
    private Integer totalRounds;
    private Integer totalWins;
    private Integer totalGames;
    private String title;
    private Date updateTime;

    public String getUserId() { return userId; } public void setUserId(String v) { this.userId = v; }
    public String getUsername() { return username; } public void setUsername(String v) { this.username = v; }
    public String getClassname() { return classname; } public void setClassname(String v) { this.classname = v; }
    public Integer getMaxScore() { return maxScore; } public void setMaxScore(Integer v) { this.maxScore = v; }
    public Integer getTotalRounds() { return totalRounds; } public void setTotalRounds(Integer v) { this.totalRounds = v; }
    public Integer getTotalWins() { return totalWins; } public void setTotalWins(Integer v) { this.totalWins = v; }
    public Integer getTotalGames() { return totalGames; } public void setTotalGames(Integer v) { this.totalGames = v; }
    public String getTitle() { return title; } public void setTitle(String v) { this.title = v; }
    public Date getUpdateTime() { return updateTime; } public void setUpdateTime(Date v) { this.updateTime = v; }
}
