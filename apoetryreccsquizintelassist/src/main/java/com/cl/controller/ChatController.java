package com.cl.controller;

import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

import com.cl.entity.StudentEntity;
import com.cl.service.StudentService;
import com.cl.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.cl.annotation.IgnoreAuth;

import com.cl.entity.ChatEntity;
import com.cl.entity.view.ChatView;

import com.cl.service.ChatService;
import com.cl.service.TokenService;


@RestController
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;
    


    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,ChatEntity chat,
		HttpServletRequest request){
    	if(!request.getSession().getAttribute("role").toString().equals("管理员")) {
    		chat.setUserid((Long)request.getSession().getAttribute("userId"));
    	}
        EntityWrapper<ChatEntity> ew = new EntityWrapper<ChatEntity>();
		PageUtils page = chatService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, chat), params), params));

        return R.ok().put("data", page);
    }

	@RequestMapping("/page2")
	public R page2(@RequestParam Map<String, Object> params,ChatEntity chat,
				  HttpServletRequest request){

		EntityWrapper<ChatEntity> ew = new EntityWrapper<ChatEntity>();
		ew.orderDesc(Arrays.asList("addtime"));
		PageUtils page = chatService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, chat), params), params));
// 获取当前页的数据列表
		List<ChatEntity> records = (List<ChatEntity>) page.getList();
		// 对数据列表按时间正序排序
		Collections.sort(records, Comparator.comparing(ChatEntity::getAddtime));

		// 更新 PageUtils 中的数据列表
		page.setList(records);
		return R.ok().put("data", page);
	}
    
    /**
     * 前端列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,ChatEntity chat, 
		HttpServletRequest request){

        EntityWrapper<ChatEntity> ew = new EntityWrapper<ChatEntity>();
		PageUtils page = chatService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, chat), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( ChatEntity chat){
       	EntityWrapper<ChatEntity> ew = new EntityWrapper<ChatEntity>();
      	ew.allEq(MPUtil.allEQMapPre( chat, "chat")); 
        return R.ok().put("data", chatService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(ChatEntity chat){
        EntityWrapper< ChatEntity> ew = new EntityWrapper< ChatEntity>();
 		ew.allEq(MPUtil.allEQMapPre( chat, "chat")); 
		ChatView chatView =  chatService.selectView(ew);
		return R.ok("查询在线客服成功").put("data", chatView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        ChatEntity chat = chatService.selectById(id);
        return R.ok().put("data", chat);
    }

    /**
     * 前端详情
     */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        ChatEntity chat = chatService.selectById(id);
        return R.ok().put("data", chat);
    }
    
@Autowired
private StudentService younghuService;


    /**
     * 后端保存
     */

    @RequestMapping("/save")
    public R save(@RequestBody ChatEntity chat, HttpServletRequest request){
		System.out.println( request.getSession().getAttribute("userId"));
    	chat.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(chat);
		ChatEntity chatEntity = new ChatEntity();
    	if(StringUtils.isNotBlank(chat.getAsk())) {
    		chat.setIsreply(0);
			StudentEntity student = younghuService.selectById(chat.getUserid());
			chat.setName(student.getStudentname());
			chat.setZhaopian(student.getAvatar());


			chatEntity.setUserid(chat.getUserid());
			chatEntity.setIsreply(1);
			chatEntity.setAsk(AIUitl.getResponse(chat.getAsk()));

    	}

        chatService.insert(chat);
		chatService.insert(chatEntity);




        return R.ok().put("data", chatEntity.getAsk());
    }

	@RequestMapping("/save2")
	public R save2(@RequestBody ChatEntity chat, HttpServletRequest request){
		chat.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());

		chatService.insert(chat);
		return R.ok();
	}
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody ChatEntity chat, HttpServletRequest request){
    	chat.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(chat);
    	chat.setUserid((Long)request.getSession().getAttribute("userId"));
    	if(StringUtils.isNotBlank(chat.getAsk())) {
			chatService.updateForSet("isreply=0", new EntityWrapper<ChatEntity>().eq("userid", request.getSession().getAttribute("userId")));
    		chat.setUserid((Long)request.getSession().getAttribute("userId"));
    		chat.setIsreply(1);
    	}
    	if(StringUtils.isNotBlank(chat.getReply())) {
    		chatService.updateForSet("isreply=0", new EntityWrapper<ChatEntity>().eq("userid", chat.getUserid()));
    		chat.setAdminid((Long)request.getSession().getAttribute("userId"));
    	}
        chatService.insert(chat);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody ChatEntity chat, HttpServletRequest request){
        //ValidatorUtils.validateEntity(chat);
        chatService.updateById(chat);//全部更新
        return R.ok();
    }
    

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        chatService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
    
    /**
     * 提醒接口
     */
	@RequestMapping("/remind/{columnName}/{type}")
	public R remindCount(@PathVariable("columnName") String columnName, HttpServletRequest request, 
						 @PathVariable("type") String type,@RequestParam Map<String, Object> map) {
		map.put("column", columnName);
		map.put("type", type);
		
		if(type.equals("2")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			Date remindStartDate = null;
			Date remindEndDate = null;
			if(map.get("remindstart")!=null) {
				Integer remindStart = Integer.parseInt(map.get("remindstart").toString());
				c.setTime(new Date()); 
				c.add(Calendar.DAY_OF_MONTH,remindStart);
				remindStartDate = c.getTime();
				map.put("remindstart", sdf.format(remindStartDate));
			}
			if(map.get("remindend")!=null) {
				Integer remindEnd = Integer.parseInt(map.get("remindend").toString());
				c.setTime(new Date());
				c.add(Calendar.DAY_OF_MONTH,remindEnd);
				remindEndDate = c.getTime();
				map.put("remindend", sdf.format(remindEndDate));
			}
		}
		
		Wrapper<ChatEntity> wrapper = new EntityWrapper<ChatEntity>();
		if(map.get("remindstart")!=null) {
			wrapper.ge(columnName, map.get("remindstart"));
		}
		if(map.get("remindend")!=null) {
			wrapper.le(columnName, map.get("remindend"));
		}


		int count = chatService.selectCount(wrapper);
		return R.ok().put("count", count);
	}
	


}
