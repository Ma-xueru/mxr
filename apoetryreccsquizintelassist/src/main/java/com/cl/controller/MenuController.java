package com.cl.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import com.cl.utils.ValidatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.cl.annotation.IgnoreAuth;

import com.cl.entity.MenuEntity;
import com.cl.entity.view.MenuView;

import com.cl.service.MenuService;
import com.cl.service.TokenService;
import com.cl.utils.PageUtils;
import com.cl.utils.R;
import com.cl.utils.MPUtil;
import com.cl.utils.CommonUtil;
import java.io.IOException;

/**
 * 菜单
 * 后端接口
 * @author 
 * @email 
 * @date 2026-01-25 11:35:29
 */
@RestController
@RequestMapping("/menu")
public class MenuController {
    @Autowired
    private MenuService menuService;

    /** 确保跟读记录和测验管理菜单项存在 */
    private void ensureExtraMenus(PageUtils page) {
        if (page == null || page.getList() == null) return;
        for (Object row : page.getList()) {
            if (row instanceof MenuEntity) {
                MenuEntity<?> menuEntity = (MenuEntity<?>) row;
                menuEntity.setMenujson(ensureExtraMenuJson(menuEntity.getMenujson()));
            }
        }
    }

    private String ensureExtraMenuJson(String menuJson) {
        if (StringUtils.isBlank(menuJson)) return menuJson;
        try {
            com.alibaba.fastjson.JSONArray roles = com.alibaba.fastjson.JSON.parseArray(menuJson);
            for (int i = 0; i < roles.size(); i++) {
                com.alibaba.fastjson.JSONObject role = roles.getJSONObject(i);
                com.alibaba.fastjson.JSONArray backMenu = role.getJSONArray("backMenu");
                if (backMenu == null) continue;
                // 第一遍：删除不需要的菜单组和子项
                for (int j = backMenu.size() - 1; j >= 0; j--) {
                    com.alibaba.fastjson.JSONObject group = backMenu.getJSONObject(j);
                    String gname = group.getString("menu");
                    if ("预约课程管理".equals(gname) || "预约课程学习管理".equals(gname) || "学习社区管理".equals(gname) || "题库管理".equals(gname)) {
                        backMenu.remove(j); continue;
                    }
                    com.alibaba.fastjson.JSONArray child = group.getJSONArray("child");
                    if (child != null) {
                        for (int k = child.size() - 1; k >= 0; k--) {
                            String tn = child.getJSONObject(k).getString("tableName");
                            if ("coursereserve".equals(tn) || "reservecancel".equals(tn) || "mystudent".equals(tn) || "forum".equals(tn) || "discussforum".equals(tn) || "feedback".equals(tn) || "examquestion".equals(tn) || "exampaper".equals(tn) || "news".equals(tn) || "examrecord".equals(tn) || "examfailrecord".equals(tn)) {
                                child.remove(k);
                            }
                        }
                        if (child.size() == 0 && !"学习任务管理".equals(gname) && !"成绩信息管理".equals(gname)) {
                            backMenu.remove(j);
                        }
                    }
                }
                // 第二遍：添加跟读记录和测验管理
                for (int j = 0; j < backMenu.size(); j++) {
                    com.alibaba.fastjson.JSONObject group = backMenu.getJSONObject(j);
                    String name = group.getString("menu");
                    if ("学习任务管理".equals(name) || "成绩信息管理".equals(name)) {
                        group.put("menu", "学习任务管理");
                        com.alibaba.fastjson.JSONArray child = group.getJSONArray("child");
                        if (child == null) continue;
                        boolean hasFollow = false, hasQuiz = false;
                        for (int k = 0; k < child.size(); k++) {
                            String tn = child.getJSONObject(k).getString("tableName");
                            if ("followreadrecord".equals(tn)) hasFollow = true;
                            if ("quiztask".equals(tn)) hasQuiz = true;
                        }
                        if (!hasFollow) {
                            com.alibaba.fastjson.JSONObject item = new com.alibaba.fastjson.JSONObject();
                            item.put("appFrontIcon", "cuIcon-group");
                            item.put("buttons", java.util.Arrays.asList("查看","删除"));
                            item.put("menu", "跟读记录");
                            item.put("menuJump", "列表");
                            item.put("tableName", "followreadrecord");
                            child.add(item);
                        }
                        if (!hasQuiz) {
                            com.alibaba.fastjson.JSONObject item = new com.alibaba.fastjson.JSONObject();
                            item.put("appFrontIcon", "cuIcon-edit");
                            item.put("buttons", java.util.Arrays.asList("新增","查看","修改","删除"));
                            item.put("menu", "测验管理");
                            item.put("menuJump", "列表");
                            item.put("tableName", "quiztask");
                            child.add(item);
                        }
                    }
                }
                // 清理 frontMenu
                com.alibaba.fastjson.JSONArray frontMenu = role.getJSONArray("frontMenu");
                if (frontMenu != null) {
                    for (int j = frontMenu.size() - 1; j >= 0; j--) {
                        com.alibaba.fastjson.JSONObject fgroup = frontMenu.getJSONObject(j);
                        String fgname = fgroup.getString("menu");
                        if ("教师管理".equals(fgname) || "教师预约".equals(fgname)) {
                            frontMenu.remove(j); continue;
                        }
                        com.alibaba.fastjson.JSONArray fchild = fgroup.getJSONArray("child");
                        if (fchild != null) {
                            for (int k = fchild.size() - 1; k >= 0; k--) {
                                String tn = fchild.getJSONObject(k).getString("tableName");
                                if ("teacher".equals(tn) || "coursereserve".equals(tn) || "reservecancel".equals(tn) || "mystudent".equals(tn) || "forum".equals(tn)) {
                                    fchild.remove(k);
                                }
                            }
                            if (fchild.size() == 0) frontMenu.remove(j);
                        }
                    }
                }
            }
            return roles.toJSONString();
        } catch (Exception e) { return menuJson; }
    }






    


    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,MenuEntity menu,
		HttpServletRequest request){
        EntityWrapper<MenuEntity> ew = new EntityWrapper<MenuEntity>();

		PageUtils page = menuService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, menu), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,MenuEntity menu, 
		HttpServletRequest request){
        EntityWrapper<MenuEntity> ew = new EntityWrapper<MenuEntity>();

		PageUtils page = menuService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, menu), params), params));
        ensureExtraMenus(page);
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( MenuEntity menu){
       	EntityWrapper<MenuEntity> ew = new EntityWrapper<MenuEntity>();
      	ew.allEq(MPUtil.allEQMapPre( menu, "menu")); 
        return R.ok().put("data", menuService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(MenuEntity menu){
        EntityWrapper< MenuEntity> ew = new EntityWrapper< MenuEntity>();
 		ew.allEq(MPUtil.allEQMapPre( menu, "menu")); 
		MenuView menuView =  menuService.selectView(ew);
		return R.ok("查询菜单成功").put("data", menuView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        MenuEntity menu = menuService.selectById(id);
		menu = menuService.selectView(new EntityWrapper<MenuEntity>().eq("id", id));
        return R.ok().put("data", menu);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        MenuEntity menu = menuService.selectById(id);
		menu = menuService.selectView(new EntityWrapper<MenuEntity>().eq("id", id));
        return R.ok().put("data", menu);
    }
    



    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MenuEntity menu, HttpServletRequest request){
    	menu.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(menu);
        menuService.insert(menu);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody MenuEntity menu, HttpServletRequest request){
    	menu.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(menu);
        menuService.insert(menu);
        return R.ok();
    }



    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    @IgnoreAuth
    public R update(@RequestBody MenuEntity menu, HttpServletRequest request){
        //ValidatorUtils.validateEntity(menu);
        menuService.updateById(menu);//全部更新
        return R.ok();
    }



    

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        menuService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
    
	
	/**
     * 前端智能排序
     */
	@IgnoreAuth
    @RequestMapping("/autoSort")
    public R autoSort(@RequestParam Map<String, Object> params,MenuEntity menu, HttpServletRequest request,String pre){
        EntityWrapper<MenuEntity> ew = new EntityWrapper<MenuEntity>();
        Map<String, Object> newMap = new HashMap<String, Object>();
        Map<String, Object> param = new HashMap<String, Object>();
		Iterator<Map.Entry<String, Object>> it = param.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> entry = it.next();
			String key = entry.getKey();
			String newKey = entry.getKey();
			if (pre.endsWith(".")) {
				newMap.put(pre + newKey, entry.getValue());
			} else if (StringUtils.isEmpty(pre)) {
				newMap.put(newKey, entry.getValue());
			} else {
				newMap.put(pre + "." + newKey, entry.getValue());
			}
		}
		params.put("sort", "clicktime");
        params.put("order", "desc");
		PageUtils page = menuService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, menu), params), params));
        return R.ok().put("data", page);
    }








}
