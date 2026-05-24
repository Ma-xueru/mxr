package com.cl.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.cl.annotation.IgnoreAuth;
import com.cl.dao.SysUserViewDao;
import com.cl.entity.SysUserView;
import com.cl.entity.TeacherEntity;
import com.cl.service.TeacherService;
import com.cl.service.TokenService;
import com.cl.utils.PasswordUtil;
import com.cl.utils.R;

/**
 * 统一登录
 * 查询 v_sys_user 视图自动鉴权
 */
@RestController
@RequestMapping("/sys")
public class AuthController {

    @Autowired
    private SysUserViewDao sysUserViewDao;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TeacherService teacherService;

    @IgnoreAuth
    @RequestMapping("/login")
    public R login(@RequestParam String username, @RequestParam String password) {
        List<SysUserView> list = sysUserViewDao.selectList(
            new EntityWrapper<SysUserView>().eq("username", username));
        SysUserView user = (list != null && !list.isEmpty()) ? list.get(0) : null;

        if (user == null) {
            return R.error("账号不存在");
        }

        if (!PasswordUtil.verify(password, user.getPassword())) {
            return R.error("账号或密码不正确");
        }

        String roleType = user.getRoleType();
        String tableName;
        String role;

        if ("ADMIN".equals(roleType)) {
            tableName = "admin";
            role = "管理员";
        } else if ("TEACHER".equals(roleType)) {
            // 检查教师是否被禁用
            TeacherEntity teacher = teacherService.selectOne(
                new EntityWrapper<TeacherEntity>().eq("teacheraccount", username));
            if (teacher != null && "禁用".equals(teacher.getPermissionstatus())) {
                return R.error("当前教师账号已被系统管理员禁用");
            }
            tableName = "teacher";
            role = "管理员";
        } else {
            return R.error("未知角色类型");
        }

        String token = tokenService.generateToken(user.getId(), username, tableName, role);
        return R.ok().put("token", token).put("roleType", roleType).put("tableName", tableName);
    }
}
