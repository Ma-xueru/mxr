package com.cl.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

@TableName("teacher_class")
public class TeacherClassEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teacherId;
    private String teacherAccount;
    private String classname;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTeacherAccount() { return teacherAccount; }
    public void setTeacherAccount(String teacherAccount) { this.teacherAccount = teacherAccount; }
    public String getClassname() { return classname; }
    public void setClassname(String classname) { this.classname = classname; }
}
