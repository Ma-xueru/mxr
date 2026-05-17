package com.cl.entity.vo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StudentPortraitVO {

    // 模块一：基础信息 + 雷达图
    private String studentname;
    private String studentaccount;
    private String avatar;
    private String classname;
    private String grade;
    private int overallRank;
    private int classTotal;
    private Map<String, Integer> radarScores = new LinkedHashMap<>();
    private String weakAdvice;

    // 模块二：班级作业与测验
    private int recitationTotal;
    private int recitationDone;
    private int recitationPassRate;
    private int quizTotal;
    private int quizPassed;
    private int quizPassRate;
    private List<RecentTaskItem> recentTasks = new ArrayList<>();

    // 模块三：自主特训
    private int selfStudyPoems;
    private int totalPoems;
    private int followReadCount;
    private int followReadMaxScore;
    private int reviewWrongCount;
    private int wrongFixedCount;
    private int deriveBreakCount;
    private String weakTags;

    // 模块四：顽固盲区
    private List<WrongItem> topWrongItems = new ArrayList<>();

    // ===== 内嵌类 =====
    public static class RecentTaskItem {
        private String title;
        private String poem;
        private Integer score;
        private String recognizedText;
        private String deadline;
        private String status;
        private String type;
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getPoem() { return poem; }
        public void setPoem(String poem) { this.poem = poem; }
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
        public String getRecognizedText() { return recognizedText; }
        public void setRecognizedText(String recognizedText) { this.recognizedText = recognizedText; }
        public String getDeadline() { return deadline; }
        public void setDeadline(String deadline) { this.deadline = deadline; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class WrongItem {
        private String question;
        private String poemTitle;
        private String errorType;
        private int wrongCount;
        private String lastWrongTime;
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public String getPoemTitle() { return poemTitle; }
        public void setPoemTitle(String poemTitle) { this.poemTitle = poemTitle; }
        public String getErrorType() { return errorType; }
        public void setErrorType(String errorType) { this.errorType = errorType; }
        public int getWrongCount() { return wrongCount; }
        public void setWrongCount(int wrongCount) { this.wrongCount = wrongCount; }
        public String getLastWrongTime() { return lastWrongTime; }
        public void setLastWrongTime(String lastWrongTime) { this.lastWrongTime = lastWrongTime; }
    }

    // ===== Getters / Setters =====
    public String getStudentname() { return studentname; }
    public void setStudentname(String studentname) { this.studentname = studentname; }
    public String getStudentaccount() { return studentaccount; }
    public void setStudentaccount(String studentaccount) { this.studentaccount = studentaccount; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getClassname() { return classname; }
    public void setClassname(String classname) { this.classname = classname; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public int getOverallRank() { return overallRank; }
    public void setOverallRank(int overallRank) { this.overallRank = overallRank; }
    public int getClassTotal() { return classTotal; }
    public void setClassTotal(int classTotal) { this.classTotal = classTotal; }
    public Map<String, Integer> getRadarScores() { return radarScores; }
    public void setRadarScores(Map<String, Integer> radarScores) { this.radarScores = radarScores; }
    public String getWeakAdvice() { return weakAdvice; }
    public void setWeakAdvice(String weakAdvice) { this.weakAdvice = weakAdvice; }
    public int getRecitationTotal() { return recitationTotal; }
    public void setRecitationTotal(int recitationTotal) { this.recitationTotal = recitationTotal; }
    public int getRecitationDone() { return recitationDone; }
    public void setRecitationDone(int recitationDone) { this.recitationDone = recitationDone; }
    public int getRecitationPassRate() { return recitationPassRate; }
    public void setRecitationPassRate(int recitationPassRate) { this.recitationPassRate = recitationPassRate; }
    public int getQuizTotal() { return quizTotal; }
    public void setQuizTotal(int quizTotal) { this.quizTotal = quizTotal; }
    public int getQuizPassed() { return quizPassed; }
    public void setQuizPassed(int quizPassed) { this.quizPassed = quizPassed; }
    public int getQuizPassRate() { return quizPassRate; }
    public void setQuizPassRate(int quizPassRate) { this.quizPassRate = quizPassRate; }
    public List<RecentTaskItem> getRecentTasks() { return recentTasks; }
    public void setRecentTasks(List<RecentTaskItem> recentTasks) { this.recentTasks = recentTasks; }
    public int getSelfStudyPoems() { return selfStudyPoems; }
    public void setSelfStudyPoems(int selfStudyPoems) { this.selfStudyPoems = selfStudyPoems; }
    public int getTotalPoems() { return totalPoems; }
    public void setTotalPoems(int totalPoems) { this.totalPoems = totalPoems; }
    public int getFollowReadCount() { return followReadCount; }
    public void setFollowReadCount(int followReadCount) { this.followReadCount = followReadCount; }
    public int getFollowReadMaxScore() { return followReadMaxScore; }
    public void setFollowReadMaxScore(int followReadMaxScore) { this.followReadMaxScore = followReadMaxScore; }
    public int getReviewWrongCount() { return reviewWrongCount; }
    public void setReviewWrongCount(int reviewWrongCount) { this.reviewWrongCount = reviewWrongCount; }
    public int getWrongFixedCount() { return wrongFixedCount; }
    public void setWrongFixedCount(int wrongFixedCount) { this.wrongFixedCount = wrongFixedCount; }
    public int getDeriveBreakCount() { return deriveBreakCount; }
    public void setDeriveBreakCount(int deriveBreakCount) { this.deriveBreakCount = deriveBreakCount; }
    public String getWeakTags() { return weakTags; }
    public void setWeakTags(String weakTags) { this.weakTags = weakTags; }
    public List<WrongItem> getTopWrongItems() { return topWrongItems; }
    public void setTopWrongItems(List<WrongItem> topWrongItems) { this.topWrongItems = topWrongItems; }
}
