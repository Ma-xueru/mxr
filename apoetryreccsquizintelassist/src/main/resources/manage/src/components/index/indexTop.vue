<template>
	<div class="top_view" :class="themeClass">
		<div class="top_left_view">
			<div class="fold_view" @click="toggleClick">
				<el-icon class="icons">
					<Fold v-if="!collapse" />
					<Expand v-else />
				</el-icon>
			</div>
		</div>

		<div class="title_block">
			<div class="projectBadge">{{ roleName === '教师' ? 'Teacher Portal' : 'Admin Portal' }}</div>
			<div class="projectTitle">乡村儿童的古诗文背诵与国学问答智能助手后台</div>
			<div class="projectDesc">{{ roleName === '教师' ? '聚焦教学与班级日常管理' : '聚焦系统治理与业务总览' }}</div>
		</div>
		<div class="top_right_view">
			<div class="top_meta_card">
				<div class="meta_label">当前角色</div>
				<div class="meta_value">{{ roleName }}</div>
			</div>
			<el-dropdown class="avatar-container right-menu-item" trigger="hover">
				<div class="avatar-wrapper">
					<div class="nickname">欢迎 {{$toolUtil.storageGet('adminName')}}</div>
					<div class="avatar_text">{{ userInitial }}</div>
					<el-icon class="el-icon--right">
						<arrow-down />
					</el-icon>
				</div>
				<template #dropdown>
					<el-dropdown-menu slot="dropdown">
						<el-dropdown-item @click="centerClick" v-if="roleName!='管理员'">
							个人中心
						</el-dropdown-item>
						<el-dropdown-item @click="updatepasswordClick">
							修改密码
						</el-dropdown-item>
						<el-dropdown-item>
							<span style="display:block;" @click="onLogout">退出登录</span>
						</el-dropdown-item>
					</el-dropdown-menu>
				</template>
			</el-dropdown>
		</div>
	</div>
</template>

<script setup>
	import axios from 'axios'
	import {
		ElMessageBox
	} from 'element-plus'
	import {
		toRefs,
		defineEmits,
		getCurrentInstance,
		ref,
		computed
	} from 'vue';
	import { useStore } from 'vuex'
	const store = useStore()
	import {
		useRouter
	} from 'vue-router';
	const props = defineProps({
		collapse: Boolean
	})
	const {
		collapse,
		
	} = toRefs(props)
	
	const router = useRouter()
	const context = getCurrentInstance()?.appContext.config.globalProperties;
	const emit = defineEmits(['collapseChange'])
	const role = context?.$toolUtil.storageGet('sessionTable')
	const roleName = context?.$toolUtil.storageGet('role')
	const themeClass = computed(() => roleName === '教师' ? 'teacher_theme' : 'admin_theme')
	const userInitial = computed(() => {
		const name = context?.$toolUtil.storageGet('adminName') || roleName || 'A'
		return name.substring(0, 1)
	})
	const toggleClick = () => {
		emit('collapseChange')
	}
	const getSession = () => {
		context?.$http({
			url: `${context?.$toolUtil.storageGet('sessionTable')}/session`,
			method: 'get'
		}).then(res=>{
			context?.$toolUtil.storageSet('userid',res.data.data.id)
			if(context?.$toolUtil.storageGet('sessionTable') == 'student'){
				context?.$toolUtil.storageSet('adminHeadportrait',res.data.data.avatar)
			}
			if(context?.$toolUtil.storageGet('sessionTable') == 'teacher'){
				context?.$toolUtil.storageSet('adminHeadportrait',res.data.data.zhaopian)
			}
		})
	}
	// 退出登录
	const onLogout = () => {
		let toolUtil = context?.$toolUtil
		store.dispatch('delAllCachedViews')
		store.dispatch('delAllVisitedViews')
		toolUtil.storageClear()
		router.replace({
			name: "login"
		});
	}
	// 个人中心
	const centerClick = () => {
		router.push(`/${role}Center`)
	}
	// 修改密码
	const updatepasswordClick = () => {
		router.push(`/updatepassword`)
	}
	getSession()
</script>

<style lang="scss" scoped>
	// 总盒子
	.top_view {
		z-index: 9;
		top: 16px;
		left: 0;
		display: flex;
		width: calc(100% - 32px);
		justify-content: space-between;
		position: sticky;
		margin: 0 16px 0;
		padding: 18px 26px;
		border-radius: 28px;
		box-sizing: border-box;
		backdrop-filter: blur(12px);
		height: auto;
		box-shadow: 0 18px 36px rgba(33, 47, 87, 0.12);
		// 左边盒子
		.top_left_view {
			display: flex;
			width: auto;
			align-items: center;
			height: auto;
			// 折叠按钮盒子
			.fold_view {
				width: 48px;
				height: 48px;
				border-radius: 16px;
				display: flex;
				align-items: center;
				justify-content: center;
				cursor: pointer;
				// 图标
				.icons {
					font-size: 22px;
				}
			}
		}

		.title_block {
			flex: 1;
			padding: 0 24px;
		}

		.projectBadge {
			display: inline-flex;
			padding: 6px 12px;
			border-radius: 999px;
			font-size: 12px;
			font-weight: 700;
			letter-spacing: 0.6px;
		}

		.projectTitle{
			padding: 0;
			margin: 10px 0 8px;
			font-weight: 700;
			width: 100%;
			font-size: 26px;
			line-height: 1.3;
			text-align: left;
			height: auto;
		}

		.projectDesc {
			font-size: 14px;
			line-height: 1.7;
		}
		// 右部盒子
		.top_right_view{
			display: flex;
			width: auto;
			justify-content: flex-end;
			align-items: center;
			gap: 16px;
			height: auto;
			order: 3;

			.top_meta_card {
				padding: 10px 14px;
				border-radius: 18px;
				min-width: 110px;
			}

			.meta_label {
				font-size: 12px;
				opacity: 0.72;
			}

			.meta_value {
				margin-top: 6px;
				font-size: 18px;
				font-weight: 700;
			}

			// 头像盒子
			.avatar-container {
				cursor: pointer;
				margin: 0;
				display: flex;
				align-items: center;
				height: 56px;
				.avatar-wrapper {
					padding: 8px 10px 8px 16px;
					border-radius: 18px;
					display: flex;
					position: relative;
					align-items: center;
					gap: 10px;
					// 昵称
					.nickname {
						cursor: pointer;
						margin: 0;
						line-height: 44px;
						order: 2;
					}

					.avatar_text {
						width: 40px;
						height: 40px;
						border-radius: 14px;
						display: flex;
						align-items: center;
						justify-content: center;
						font-size: 18px;
						font-weight: 700;
					}
					// 图标
					.el-icon--right {
						order: 3;
					}
				}
			}
		}
	}

	.admin_theme {
		background:
			radial-gradient(circle at 8% 10%, rgba(169, 78, 61, 0.09), transparent 22%),
			radial-gradient(circle at 90% 18%, rgba(79, 126, 93, 0.12), transparent 26%),
			linear-gradient(135deg, rgba(255, 251, 237, 0.98), rgba(239, 248, 232, 0.96));
		border: 1px solid rgba(178, 149, 97, 0.22);
		color: #453625;
		box-shadow: 0 18px 38px rgba(99, 86, 58, 0.12);

		.fold_view,
		.top_meta_card,
		.avatar-wrapper {
			background: rgba(255, 255, 250, 0.72);
			border: 1px solid rgba(185, 154, 103, 0.2);
			box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.65);
		}

		.projectBadge {
			background: rgba(169, 78, 61, 0.1);
			color: #9d533f;
			border: 1px solid rgba(169, 78, 61, 0.14);
		}

		.projectTitle {
			color: #3f3020;
			font-family: "STKaiti", "KaiTi", "Microsoft YaHei", sans-serif;
			font-size: 28px;
		}

		.projectDesc,
		.meta_label,
		.nickname,
		.el-icon--right,
		.icons {
			color: rgba(73, 55, 34, 0.78);
		}

		.avatar_text {
			background: linear-gradient(135deg, #a94e3d, #d1a15b);
			color: #fff;
		}
	}

	.teacher_theme {
		background:
			radial-gradient(circle at 8% 10%, rgba(190, 91, 62, 0.09), transparent 22%),
			radial-gradient(circle at 90% 18%, rgba(77, 128, 92, 0.12), transparent 26%),
			linear-gradient(135deg, rgba(255, 251, 237, 0.98), rgba(239, 248, 232, 0.96));
		border: 1px solid rgba(178, 149, 97, 0.22);
		color: #453625;
		box-shadow: 0 18px 38px rgba(99, 86, 58, 0.12);

		.fold_view,
		.top_meta_card,
		.avatar-wrapper {
			background: rgba(255, 255, 250, 0.72);
			border: 1px solid rgba(185, 154, 103, 0.2);
			box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.65);
		}

		.projectBadge {
			background: rgba(180, 91, 69, 0.1);
			color: #9d533f;
			border: 1px solid rgba(180, 91, 69, 0.14);
		}

		.projectTitle {
			color: #3f3020;
			font-family: "STKaiti", "KaiTi", "Microsoft YaHei", sans-serif;
			font-size: 28px;
		}

		.projectDesc,
		.meta_label,
		.nickname,
		.el-icon--right,
		.icons {
			color: rgba(73, 55, 34, 0.78);
		}

		.avatar_text {
			background: linear-gradient(135deg, #a94e3d, #d1a15b);
			color: #fff;
		}
	}
	// 下拉盒子
	.el-dropdown-menu{
		background: #fff;
		min-width: 100px;
		// 下拉盒子itme
		:deep(.el-dropdown-menu__item){
			color: #555;
			background: none;
		}
		// item悬浮
		:deep(.el-dropdown-menu__item:hover){
			color: #fff;
			background: url(http://clfile.zggen.cn/20240302/950b44c7252c47a58b3b60db0ed87b33.png) no-repeat center top / 100% 100%;
		}
	}

	@media (max-width: 1024px) {
		.top_view {
			flex-wrap: wrap;
			gap: 16px;
		}

		.title_block {
			padding: 0;
			order: 3;
			width: 100%;
		}

		.top_right_view {
			width: 100%;
			justify-content: space-between;
		}
	}
</style>
