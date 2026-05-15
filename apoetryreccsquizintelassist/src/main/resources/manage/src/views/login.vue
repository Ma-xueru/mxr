<template>
	<div>
		<div class="login_view">
			<el-form :model="loginForm" class="login_form">
				<div class="title_view">乡村儿童的古诗文背诵与国学问答智能助手后台登录</div>
				<div class="list_item" v-if="loginType==1">
					<div class="list_label">
						账号：
					</div>
					<input class="list_inp" v-model="loginForm.username" placeholder="请输入账号" />
				</div>
				<div class="list_item" v-if="loginType==1">
					<div class="list_label">
						密码：
					</div>
					<input class="list_inp" v-model="loginForm.password" type="password" placeholder="请输入密码" @keydown.enter.native="handleLogin"  />
				</div>
				<div class="list_type" v-if="userList.length>1">
					<div class="list_label">
						用户类型：
					</div>
				  <el-select v-model="loginForm.role" placeholder="请选择用户类型">
				    <el-option v-for="(item,index) in userList" :label="item.roleName" :value="item.roleName"></el-option>
				  </el-select>
				</div>
				<div class="btn_view">
					<el-button class="login" v-if="loginType==1" type="success" @click="handleLogin">登录</el-button>
					<el-button class="register" type="primary" @click="handleRegister('teacher')">注册教师</el-button>
				</div>
			</el-form>
		</div>
		<Vcode :show="isShow" @success="success" @close="close" @fail='fail'></Vcode>
	</div>
</template>
<script setup>
	import {
		ref,
		getCurrentInstance,
		nextTick,
		onMounted,
	} from "vue";
	const userList = ref([])
	const menus = ref([])
	const loginForm = ref({
		role: '',
		username: '',
		password: ''
	})
	const tableName = ref('')
	const loginType = ref(1)
	const context = getCurrentInstance()?.appContext.config.globalProperties;
	const ensureTeacherMenus = (menuTree) => {
			const teacherMenu = menuTree.find(item => item.tableName === 'teacher')
			if (!teacherMenu) return menuTree

			// 清理教师菜单中不需要的分组
			teacherMenu.backMenu = teacherMenu.backMenu.filter(group => {
				const name = group.menu || ''
				return !name.includes('题库') && !name.includes('师生绑定') && !name.includes('学习社区') && !name.includes('公告') && !name.includes('反馈')
			})

			// 确保工作台存在
			if (!teacherMenu.backMenu.some(g => (g.menu||'') === '工作台')) {
				teacherMenu.backMenu.unshift({ child: [{ appFrontIcon: "cuIcon-home", buttons: ["查看"], menu: "工作台", menuJump: "dashboard", tableName: "teacher" }], fontClass: "icon-common30", menu: "工作台", unicode: "&#xee47;" })
			}
			// 确保班级管理存在
			if (!teacherMenu.backMenu.some(g => g.child && g.child.some(c => c.tableName === 'classinfo'))) {
				teacherMenu.backMenu.unshift({ child: [{ appFrontIcon: "cuIcon-group", buttons: ["新增","查看","修改","删除"], menu: "班级管理", menuJump: "列表", tableName: "classinfo" }], fontClass: "icon-common50", menu: "班级管理", unicode: "&#xef96;" })
			}
			// 确保学生管理存在
			if (!teacherMenu.backMenu.some(g => g.child && g.child.some(c => c.tableName === 'student'))) {
				teacherMenu.backMenu.splice(1, 0, { child: [{ appFrontIcon: "cuIcon-full", buttons: ["新增","查看","修改","删除"], menu: "学生管理", menuJump: "列表", tableName: "student" }], fontClass: "icon-common38", menu: "学生管理", unicode: "&#xeeb2;" })
			}
			// 确保古诗文库存在
			if (!teacherMenu.backMenu.some(g => g.child && g.child.some(c => c.tableName === 'course'))) {
				teacherMenu.backMenu.splice(2, 0, { child: [{ appFrontIcon: "cuIcon-pay", buttons: ["查看","查看评论"], menu: "古诗文库", menuJump: "列表", tableName: "course" }], fontClass: "icon-common49", menu: "古诗文库管理", unicode: "&#xef3d;" })
			}
			// 确保学习任务管理存在(背诵任务/跟读记录/测验管理)
			if (!teacherMenu.backMenu.some(g => g.child && g.child.some(c => c.tableName === 'recitationtask'))) {
				const taskGroup = teacherMenu.backMenu.find(g => (g.menu||'').includes('学习任务') || (g.menu||'').includes('成绩信息'))
				if (taskGroup) {
					taskGroup.menu = '学习任务管理'
					if (!taskGroup.child.some(c => c.tableName === 'transcript')) taskGroup.child.push({ appFrontIcon: "cuIcon-attentionfavor", buttons: ["查看","修改","删除","成绩统计","新增"], menu: "成绩信息", menuJump: "列表", tableName: "transcript" })
				} else {
					teacherMenu.backMenu.push({ child: [{ appFrontIcon: "cuIcon-book", buttons: ["新增","查看","修改","删除"], menu: "背诵任务", menuJump: "列表", tableName: "recitationtask" }, { appFrontIcon: "cuIcon-edit", buttons: ["新增","查看","修改","删除"], menu: "测验管理", menuJump: "列表", tableName: "quiztask" }, { appFrontIcon: "cuIcon-group", buttons: ["查看","删除"], menu: "跟读记录", menuJump: "列表", tableName: "followreadrecord" }], fontClass: "icon-common31", menu: "学习任务管理", unicode: "&#xee48;" })
				}
			}
			return menuTree
		}

	const ensureAdminMenus = (menuTree) => {
		const adminMenu = menuTree.find(item => item.tableName === 'admin')
		if (!adminMenu) return menuTree
		const ensureGroup = (groupName, fontClass, unicode) => {
			let group = adminMenu.backMenu.find(item => item.menu === groupName)
			if (!group) {
				group = { child: [], fontClass, menu: groupName, unicode }
				adminMenu.backMenu.push(group)
			}
			return group
		}
		const poetryGroup = ensureGroup('古诗文库管理', 'icon-common49', '&#xef3d;')
		if (!poetryGroup.child.some(child => child.tableName === 'course')) {
			poetryGroup.child.push({
				appFrontIcon: "cuIcon-pay",
				buttons: ["新增", "查看", "修改", "删除", "查看评论"],
				menu: "古诗文库",
				menuJump: "列表",
				tableName: "course",
			})
		} else {
			poetryGroup.child = poetryGroup.child.map(child => child.tableName === 'course'
				? { ...child, menu: '古诗文库', menuJump: '列表' }
				: child
			)
		}

		const classGroup = ensureGroup('班级管理', 'icon-common50', '&#xef96;')
		if (!classGroup.child.some(child => child.tableName === 'classinfo')) {
			classGroup.child.push({
				appFrontIcon: "cuIcon-group",
				buttons: ["新增", "查看", "修改", "删除"],
				menu: "班级管理",
				menuJump: "列表",
				tableName: "classinfo",
			})
		}

		adminMenu.backMenu = adminMenu.backMenu.map(group => {
			if (group.menu === '管理员管理') {
				group.menu = '用户信息管理'
			}
			if (group.menu === '古诗词管理') {
				group.menu = '古诗文库管理'
				group.child = (group.child || []).map(child => child.tableName === 'course'
					? { ...child, menu: '古诗文库', menuJump: '列表' }
					: child
				)
			}
			if (group.menu === '题目管理' || group.menu === '题库管理' || group.menu === '古诗词测试管理') {
				if (group.child) {
					group.child = group.child.map(child => {
						if (child.tableName === 'examquestion') return { ...child, menu: '问答题库管理', menuJump: '列表' }
						if (child.tableName === 'exampaper') return { ...child, menu: '答题试卷', menuJump: '列表' }
						return child
					})
				}
			}
			if (group.menu === '用户管理') {
				group.menu = '师生绑定管理'
				group.child = (group.child || []).map(child => child.tableName === 'mystudent'
					? { ...child, menu: '师生绑定', menuJump: '列表' }
					: child
				)
			}
			return group
		})
		menuTree = menuTree.map(item => ({
			...item,
			backMenu: (item.backMenu || []).map(group => {
				const nextGroup = { ...group }
				if (nextGroup.menu === '用户管理') {
					nextGroup.menu = '师生绑定管理'
				}
				if (nextGroup.child) {
					nextGroup.child = nextGroup.child.map(child => {
						if (child.tableName === 'mystudent') {
							return { ...child, menu: '师生绑定', menuJump: '列表' }
						}
						return child
					})
				}
				return nextGroup
			})
		}))
		return menuTree
	}
	const normalizeTeacherTaskMenu = (menuTree) => {
		const teacherMenu = menuTree.find(item => item.tableName === 'teacher')
		if (!teacherMenu) return menuTree
		const taskGroup = teacherMenu.backMenu.find(group => group.menu === '成绩信息管理' || group.menu === '学习任务管理')
		if (taskGroup && !taskGroup.child.some(child => child.tableName === 'recitationtask')) {
			taskGroup.child.push({
				appFrontIcon: "cuIcon-book",
				buttons: ["新增", "查看", "修改", "删除"],
				menu: "背诵任务",
				menuJump: "列表",
				tableName: "recitationtask",
			})
		}
		return menuTree
	}
	const ensureReserveMenus = (menuTree) => {
		return menuTree.map(item => {
			const nextItem = { ...item, backMenu: [...(item.backMenu || [])] }
			const exists = nextItem.backMenu.some(group =>
				Array.isArray(group.child) && group.child.some(child => child.tableName === 'teacher' && child.menu === '教师预约')
			)
			if (!exists) {
				nextItem.backMenu.splice(1, 0, {
					child: [{
						appFrontIcon: 'cuIcon-time',
						buttons: ['查看'],
						menu: '教师预约',
						menuJump: 'reserve',
						tableName: 'teacher'
					}],
					fontClass: 'icon-common50',
					menu: '教师预约',
					unicode: '&#xef96;'
				})
			}
			nextItem.backMenu = nextItem.backMenu.map(group => ({
				...group,
				child: (group.child || []).filter(child => child.tableName !== 'coursereserve')
			})).filter(group => (group.child || []).length)
			return nextItem
		})
	}
	const hideReserveMenus = (menuTree) => menuTree.map(item => {
		const nextItem = { ...item }
		nextItem.backMenu = (item.backMenu || []).filter(group => group.menu !== '预约课程管理')
		nextItem.frontMenu = (item.frontMenu || []).map(group => ({
			...group,
			child: (group.child || []).map(child => {
				if (child.tableName === 'teacher' && Array.isArray(child.buttons)) {
					return {
						...child,
						buttons: child.buttons.filter(button => button !== '预约')
					}
				}
				return child
			})
		}))
		return nextItem
	})
	//注册
    const handleRegister = (tableName) => {
    	context?.$router.push(`/${tableName}Register`)
    	
    }
	const handleLogin = () => {
		if (!loginForm.value.username) {
			context?.$toolUtil.message('请输入用户名', 'error')
			
			return;
		}
		if (!loginForm.value.password) {
			context?.$toolUtil.message('请输入密码', 'error')
			
			return;
		}
		if (userList.value.length > 1) {
			if (!loginForm.value.role) {
				context?.$toolUtil.message('请选择角色', 'error')
				verifySlider.value.reset()
				return;
			}
			for (let i = 0; i < menus.value.length; i++) {
				if (menus.value[i].roleName == loginForm.value.role) {
					tableName.value = menus.value[i].tableName;
				}
			}
		} else {
			tableName.value = userList.value[0].tableName;
			loginForm.value.role = userList.value[0].roleName;
		}
		login()
	}
	const login = () => {
		context?.$http({
			url: `${tableName.value}/login?username=${loginForm.value.username}&password=${loginForm.value.password}`,
			method: 'post'
		}).then(res => {
			context?.$toolUtil.storageSet("Token", res.data.token);
			context?.$toolUtil.storageSet("role", loginForm.value.role);
			context?.$toolUtil.storageSet("sessionTable", tableName.value);
			context?.$toolUtil.storageSet("adminName", loginForm.value.username);
			context?.$router.push('/')
		}, err => {
		})
	}
	//获取菜单
	const getMenu=()=> {
      let params = {
        page: 1,
        limit: 1,
        sort: 'id',
      }

      context?.$http({
        url: "menu/list",
        method: "get",
        params: params
      }).then(res => {
          menus.value = normalizeTeacherTaskMenu(ensureAdminMenus(ensureTeacherMenus(JSON.parse(res.data.data.list[0].menujson))))
          for (let i = 0; i < menus.value.length; i++) {
            if (menus.value[i].hasBackLogin=='是') {
              userList.value.push(menus.value[i])
            }
          }
			loginForm.value.role = userList.value[0].roleName
          context?.$toolUtil.storageSet("menus", JSON.stringify(menus.value));
      })
    }
	//初始化
	const init = () => {
		getMenu();
	}
	onMounted(()=>{
		init()
		
	})
</script>

<style lang="scss" scoped>
	.login_view {
		background-repeat: no-repeat;
		flex-direction: column;
		background-size: 100% 100%;
		background: url(http://clfile.zggen.cn/20240301/cb59505e774a42899501d8d7f1360b75.jpg);
		display: flex;
		min-height: 100vh;
		justify-content: center;
		align-items: center;
		position: relative;
		background-position: center center;
		// 表单盒子
		.login_form {
			border-radius: 0px;
			padding: 50px 80px 30px 40px;
			margin: 0 auto;
			background: url(http://clfile.zggen.cn/20240301/7ac2edfec9b84ae5be0a62f62e8af7bb.png) no-repeat center top / 100% auto,#f7f2ec;
			display: flex;
			width: 600px;
			justify-content: flex-start;
			flex-wrap: wrap;
		}
		.title_view {
			padding: 0px;
			margin: 0 auto 30px;
			color: #333;
			font-weight: 500;
			width: 80%;
			font-size: 22px;
			text-align: center;
		}
		// item盒子
		.list_item {
			margin: 0 0 20px;
			display: flex;
			width: 100%;
			justify-content: flex-start;
			align-items: center;
			// label
			.list_label {
				color: #666;
				background: none;
				width: 130px;
				font-size: 14px;
				line-height: 36px;
				text-align: right;
			}
			// 输入框
			.list_inp {
				border: 1px solid #ddd;
				border-radius: 0px;
				padding: 0 10px;
				color: #666;
				background: #fff;
				width: 100%;
				line-height: 36px;
				height: 36px;
			}
		}
		.list_type {
			margin: 0 0 20px;
			display: flex;
			width: 100%;
			justify-content: flex-start;
			align-items: center;
			order: 3;
			.list_label {
				color: #666;
				background: none;
				width: 130px;
				font-size: 14px;
				line-height: 36px;
				text-align: right;
			}
			// 下拉框样式
			:deep(.el-select) {
				border: 1px solid #ddd;
				border-radius: 0px;
				padding: 0 10px;
				color: #666;
				background: #fff;
				width: 100%;
				font-size: 14px;
				line-height: 36px;
				box-sizing: border-box;
				height: 36px;
				//去掉默认样式
				.select-trigger{
					height: 100%;
					.el-input{
						height: 100%;
						.el-input__wrapper{
							border: none;
							box-shadow: none;
							background: none;
							border-radius: 0;
							height: 100%;
							padding: 0;
						}
						.is-focus {
							box-shadow: none !important;
						}
					}
				}
			}
		}
		// 按钮盒子
		.btn_view {
			padding: 0;
			margin: 20px 0 0;
			background: none;
			display: flex;
			width: 100%;
			justify-content: center;
			align-items: center;
			flex-wrap: wrap;
			order: 5;
			// 登录
			.login {
				border: 0;
				cursor: pointer;
				border-radius: 0px;
				padding: 0 24px;
				margin: 0 10px 10px 0;
				color: #fff;
				background: linear-gradient(270deg, rgba(130,196,209,1) 0%, rgba(115,186,200,1) 24%, rgba(174,210,217,1) 100%);
				width: auto;
				font-size: 16px;
				height: 40px;
			}
			// 注册
			.register {
				border: 1px solid #ddd;
				cursor: pointer;
				border-radius: 0px;
				padding: 0 10px;
				margin: 0 10px 10px 0;
				color: #333;
				background: #fff;
				width: auto;
				font-size: 14px;
				height: 40px;
			}
		}
	}

</style>
