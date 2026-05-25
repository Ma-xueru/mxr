package com.cl.controller;


import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cl.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.cl.entity.TokenEntity;
import com.cl.entity.AdminEntity;
import com.cl.service.TokenService;
import com.cl.service.AdminService;
import com.cl.utils.CommonUtil;
import com.cl.utils.MPUtil;
import com.cl.utils.PageUtils;
import com.cl.utils.PasswordUtil;
import com.cl.utils.R;
import com.cl.utils.ValidatorUtils;

/**
 * 登录相关
 */
@RequestMapping("admin")
@RestController
public class AdminController{

	@Autowired
	private AdminService adminervice;

	@Autowired
	private TokenService tokenService;

	/**
	 * 登录
	 */
	@IgnoreAuth
	@RequestMapping(value = "/login")
	public R login(String username, String password, String captcha, HttpServletRequest request) {
		AdminEntity user = adminervice.selectOne(new EntityWrapper<AdminEntity>().eq("username", username));
		if(user==null || !PasswordUtil.verify(password, user.getPassword())) {
			return R.error("账号或密码不正确");
		}
		String token = tokenService.generateToken(user.getId(),username, "admin", user.getRole());
		return R.ok().put("token", token);
	}

	/**
	 * 注册
	 */
	@IgnoreAuth
	@PostMapping(value = "/register")
	public R register(@RequestBody AdminEntity user){
    	ValidatorUtils.validateEntity(user);
    	if(adminervice.selectOne(new EntityWrapper<AdminEntity>().eq("username", user.getUsername())) !=null) {
    		return R.error("用户已存在");
    	}
    	user.setPassword(PasswordUtil.hash(user.getPassword()));
        adminervice.insert(user);
        return R.ok();
    }

	/**
	 * 退出
	 */
	@GetMapping(value = "logout")
	public R logout(HttpServletRequest request) {
		request.getSession().invalidate();
		return R.ok("退出成功");
	}

	/**
     * 密码重置
     */
    @IgnoreAuth
	@RequestMapping(value = "/resetPass")
    public R resetPass(String username, HttpServletRequest request){
    	AdminEntity user = adminervice.selectOne(new EntityWrapper<AdminEntity>().eq("username", username));
    	if(user==null) {
    		return R.error("账号不存在");
    	}
    	String newPwd = "123456";
    	user.setPassword(PasswordUtil.hash(newPwd));
        adminervice.update(user,null);
        return R.ok("密码已重置为：" + newPwd);
    }

	/**
     * 列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,AdminEntity user){
        EntityWrapper<AdminEntity> ew = new EntityWrapper<AdminEntity>();
    	PageUtils page = adminervice.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.allLike(ew, user), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/list")
    public R list( AdminEntity user){
       	EntityWrapper<AdminEntity> ew = new EntityWrapper<AdminEntity>();
      	ew.allEq(MPUtil.allEQMapPre( user, "user"));
        return R.ok().put("data", adminervice.selectListView(ew));
    }

	/**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") String id){
        AdminEntity user = adminervice.selectById(id);
        return R.ok().put("data", user);
    }

    /**
     * 获取用户的session用户信息
     */
    @RequestMapping("/session")
    public R getCurrUser(HttpServletRequest request){
    	Long id = (Long)request.getSession().getAttribute("userId");
        AdminEntity user = adminervice.selectById(id);
        return R.ok().put("data", user);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    public R save(@RequestBody AdminEntity user){
    	ValidatorUtils.validateEntity(user);
    	if(adminervice.selectOne(new EntityWrapper<AdminEntity>().eq("username", user.getUsername())) !=null) {
    		return R.error("用户已存在");
    	}
    	user.setPassword(PasswordUtil.hash(user.getPassword()));
        adminervice.insert(user);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AdminEntity user){
        ValidatorUtils.validateEntity(user);
    	AdminEntity u = adminervice.selectOne(new EntityWrapper<AdminEntity>().eq("username", user.getUsername()));
    	if(u!=null && u.getId()!=user.getId() && u.getUsername().equals(user.getUsername())) {
    		return R.error("用户名已存在。");
    	}
    	if (user.getPassword() != null && !PasswordUtil.isHashed(user.getPassword())) {
    		user.setPassword(PasswordUtil.hash(user.getPassword()));
    	}
        adminervice.updateById(user);//全部更新
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        adminervice.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
}
