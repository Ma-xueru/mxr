<template>
	<div class="menu_theme" :class="themeClass">
		<div class="menu_brand" :class="{ collapsed: collapse }">
			<div class="brand_mark">{{ roleName === '教师' ? '师' : '管' }}</div>
			<div v-if="!collapse" class="brand_text">
				<div class="brand_title">{{ roleName === '教师' ? '教师工作台' : '管理控制台' }}</div>
				<div class="brand_subtitle">{{ roleName === '教师' ? 'Teaching Workspace' : 'Admin Workspace' }}</div>
			</div>
		</div>
		<el-scrollbar wrap-class="scrollbar-wrapper" class="menu_scrollbar">
			<el-menu :default-openeds="[]" :unique-opened="false" default-active="0" class="menu_view"
				:collapse="collapse">
				<el-sub-menu :index="0" @click="menuHandler('')">
					<template #title>
						<i class="iconfont icon-zhuye2" v-if="collapse?true:true"></i>
						<span>首页</span>
					</template>
				</el-sub-menu>
				<el-sub-menu v-for=" (menu,index) in menuList.backMenu" :key="menu.menu" :index="index+2+''">
					<template #title>
						<i class="iconfont" :class="menu.fontClass" v-if="collapse?true:true"></i>
						<span>{{ menu.menu }}</span>
					</template>
					<el-menu-item class="menu_item_view" v-for=" (child,sort) in menu.child" :key="sort"
						:index="(index+2)+'-'+sort" @click="menuHandler(child.tableName,child.menuJump)">{{ child.menu }}
					</el-menu-item>
				</el-sub-menu>
			</el-menu>
		</el-scrollbar>
	</div>
</template>

<script setup>
	import menu from '@/utils/menu'
	import {
		ref,
		toRefs,
		getCurrentInstance,
		nextTick,
		computed
	} from 'vue';
	const context = getCurrentInstance()?.appContext.config.globalProperties;
	//props
	const props = defineProps({
		collapse: Boolean
	})
	const {
		collapse
	} = toRefs(props)
	//data
	const menuList = ref([])
	const role = ref('')
	const roleName = computed(() => role.value || '管理员')
	const themeClass = computed(() => role.value === '教师' ? 'teacher_theme' : 'admin_theme')
	const styleChange = () => {
		nextTick(() => {
			document.querySelectorAll('.el-menu-vertical-demo .el-sub-menu .el-menu').forEach(el => {
				el.removeAttribute('style')
				const icon = {
					"border": "none",
					"padding": "0",
					"margin": "10px auto 0",
					"borderRadius": "0px",
					"background": "none",
					"display": "none",
					"width": "100%"
				}
				Object.keys(icon).forEach((key) => {
					el.style[key] = icon[key]
				})
			})
		})
	}
	//权限验证
	const btnAuth = (e,a)=>{
		return context?.$toolUtil.isAuth(e,a)
	}
	const ensureReserveMenu = (roleMenu) => {
		if (!roleMenu || !Array.isArray(roleMenu.backMenu)) return roleMenu
		const exists = roleMenu.backMenu.some(group =>
			Array.isArray(group.child) && group.child.some(child => child.tableName === 'teacher' && child.menu === '教师预约')
		)
		if (!exists) {
			roleMenu.backMenu.splice(1, 0, {
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
		roleMenu.backMenu = roleMenu.backMenu.map(group => ({
			...group,
			child: (group.child || []).filter(child => child.tableName !== 'coursereserve')
		})).filter(group => (group.child || []).length)
		return roleMenu
	}
	const init = () => {
		const menus = menu.list()
		if (menus) {
			menuList.value = menus
		}
		role.value = context?.$toolUtil.storageGet('role')

		for (let i = 0; i < menuList.value.length; i++) {
			if (menuList.value[i].roleName == role.value) {
				menuList.value = ensureReserveMenu(menuList.value[i]);
				break;
			}
		}
		// styleChange()
	}
	const menuHandler = (name,menuJump) => {
		if(name == 'center'){
			name = `${role.value}Center`
		}
		if(name == 'storeup'){
			name = `storeup?type=${menuJump}`
		}
		if(name == 'exampaper' && menuJump == '12'){
			name = 'exampaperlist'
		}
		if(name == 'examrecord' && menuJump == '22'){
			name = 'examfailrecord'
		}
		let router = context?.$router
		name = '/' + name
		router.push(name)
	}
	init()
</script>

<style lang="scss" scoped>
	// 总盒子
	.menu_theme {
		height: 100%;
		padding: 16px 14px;
		border-radius: 28px;
		box-sizing: border-box;
		box-shadow: 0 20px 38px rgba(33, 47, 87, 0.16);
	}

	.menu_brand {
		display: flex;
		align-items: center;
		gap: 12px;
		padding: 0 4px 16px;
	}

	.menu_brand.collapsed {
		justify-content: center;
		padding-bottom: 18px;
	}

	.brand_mark {
		width: 44px;
		height: 44px;
		border-radius: 16px;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 20px;
		font-weight: 700;
		color: #fff;
		flex-shrink: 0;
	}

	.brand_title {
		font-size: 16px;
		font-weight: 700;
		line-height: 1.2;
	}

	.brand_subtitle {
		margin-top: 4px;
		font-size: 12px;
		opacity: 0.72;
	}

	:deep(.menu_scrollbar) {
		height: calc(100% - 62px);

		// 菜单盒子-展开样式
		.menu_view {
			border: 0;
			padding: 0 0 32px;
			color: #fff;
			background: none;
			height: 100%;

			// 无二级菜单
			.el-menu-item {
				padding: 0 14px;
				margin: 0 0 10px;
				border-radius: 16px;
				color: inherit;
				background: transparent;
				border: 1px solid transparent;
				line-height: 46px;
				height: 46px;
				.iconfont {
					margin: 0 10px 0 0;
					color: inherit;
					width: 22px;
					vertical-align: middle;
					font-size: 18px;
					text-align: center;
				}
			}

			// 无二级悬浮
			.el-menu-item:hover {
				color: #fff;
				line-height: 46px;
				height: 46px;
			}

			// 无二级选中
			.el-menu-item.is-active {
				color: #fff;
				line-height: 46px;
				height: 46px;
			}

			// 有二级盒子
			.el-sub-menu {
				cursor: pointer;
				padding: 0 0;
				color: inherit;
				white-space: nowrap;
				background: none;
				position: relative;

				// 有二级item
				.el-sub-menu__title {
					padding: 0 14px;
					margin: 0 0 10px;
					border-radius: 16px;
					color: inherit;
					background: transparent;
					border: 1px solid transparent;
					line-height: 46px;
					height: 46px;
					.iconfont {
						margin: 0 10px 0 0;
						color: inherit;
						width: 22px;
						vertical-align: middle;
						font-size: 18px;
						text-align: center;
					}
					.el-sub-menu__icon-arrow{
						margin: -3px 0 0 8px;
						color: inherit;
						vertical-align: middle;
						font-size: 12px;
						position: static;
					}
				}

				// 有二级item悬浮
				.el-sub-menu__title:hover {
					color: #fff;
					line-height: 46px;
					height: 46px;
				}
			}
			//二级选中
			.is-active {
				.el-sub-menu__title {
					color: #fff;
					line-height: 46px;
					height: 46px;
				}
			}
			// 二级盒子
			.el-menu--inline {
				border: none;
				padding: 0;
				background: none;
				// 二级菜单
				.menu_item_view {
					padding: 0 18px 0 48px;
					margin: 0 0 8px;
					border-radius: 14px;
					color: inherit;
					background: transparent;
					border: 1px solid transparent;
					line-height: 42px;
					height: 42px;
				}
				// 二级悬浮
				.menu_item_view:hover {
					color: #fff;
					line-height: 42px;
					height: 42px;
				}
				// 二级选中
				.is-active.menu_item_view {
					color: #fff;
					line-height: 42px;
					height: 42px;
				}
			}
		}
		// 菜单盒子-关闭样式
		.el-menu--collapse {
			padding: 0 2px;
			color: inherit;
			background: transparent;
			height: 100%;

			// 无二级菜单
			.el-menu-item {
				padding: 0;
				margin: 0 0 10px;
				border-radius: 18px;
				color: inherit;
				background: transparent;
				line-height: 50px;
				height: 50px;
				.iconfont {
					margin: 0;
					color: inherit;
					width: 100%;
					vertical-align: middle;
					font-size: 20px;
					text-align: center;
				}
			}

			// 无二级悬浮
			.el-menu-item:hover {
				line-height: 50px;
				height: 50px;
			}

			// 无二级选中
			.el-menu-item.is-active {
				line-height: 50px;
				height: 50px;
			}

			// 有二级盒子
			.el-sub-menu {
				cursor: pointer;
				padding: 0 0;
				color: inherit;
				white-space: nowrap;
				background: transparent;
				position: relative;

				// 有二级item
				.el-sub-menu__title {
					padding: 0;
					margin: 0 0 10px;
					border-radius: 18px;
					color: inherit;
					background: transparent;
					line-height: 50px;
					height: 50px;
					.iconfont {
						margin: 0;
						color: inherit;
						width: 100%;
						vertical-align: middle;
						font-size: 20px;
						text-align: center;
					}
					.el-sub-menu__icon-arrow{
						margin: -3px 0 0 8px;
						color: inherit;
						vertical-align: middle;
						font-size: 12px;
						position: static;
					}
				}

				// 有二级item悬浮
				.el-sub-menu__title:hover {
					line-height: 50px;
					height: 50px;
				}
			}
			//二级选中
			.is-active {
				.el-sub-menu__title {
					line-height: 50px;
					height: 50px;
				}
			}
			// 二级盒子
			.el-menu--inline {
				border: none;
				padding: 4px 0 0;
				background: transparent;
				// 二级菜单
				.menu_item_view {
					padding: 0 14px;
					color: inherit;
					background: transparent;
					line-height: 42px;
					height: 42px;
				}
				// 二级悬浮
				.menu_item_view:hover {
					line-height: 42px;
					height: 42px;
				}
				// 二级选中
				.is-active.menu_item_view {
					line-height: 42px;
					height: 42px;
				}
			}
		}
	}

	.admin_theme {
		background:
			radial-gradient(circle at 18% 0%, rgba(171, 77, 55, 0.08), transparent 34%),
			linear-gradient(180deg, #fff8e7 0%, #f5ecd4 48%, #edf4e7 100%);
		border: 1px solid rgba(181, 149, 95, 0.28);
		box-shadow: 0 24px 46px rgba(103, 87, 56, 0.16), inset 0 1px 0 rgba(255, 255, 255, 0.76);
		color: #5b442d;

		.brand_mark {
			background: linear-gradient(135deg, #a94e3d 0%, #c99552 52%, #e8d39a 100%);
			box-shadow: 0 14px 28px rgba(78, 43, 32, 0.2);
			border-radius: 50%;
		}

		.brand_title {
			color: #4f3925;
			font-family: "STKaiti", "KaiTi", "Microsoft YaHei", sans-serif;
			letter-spacing: 0;
		}

		.brand_subtitle {
			color: rgba(91, 68, 45, 0.7);
		}

		:deep(.menu_view .el-sub-menu__title:hover),
		:deep(.menu_view .el-menu-item:hover),
		:deep(.menu_view .el-menu-item.is-active),
		:deep(.menu_view .is-active .el-sub-menu__title),
		:deep(.menu_view .el-menu--inline .menu_item_view:hover),
		:deep(.menu_view .el-menu--inline .is-active.menu_item_view),
		:deep(.el-menu--collapse .el-sub-menu__title:hover),
		:deep(.el-menu--collapse .el-menu-item:hover),
		:deep(.el-menu--collapse .el-menu-item.is-active),
		:deep(.el-menu--collapse .is-active .el-sub-menu__title),
		:deep(.el-menu--collapse .el-menu--inline .menu_item_view:hover),
		:deep(.el-menu--collapse .el-menu--inline .is-active.menu_item_view) {
			background: linear-gradient(135deg, rgba(169, 78, 61, 0.95), rgba(202, 151, 78, 0.86));
			border-color: rgba(255, 238, 190, 0.26);
			box-shadow: 0 12px 22px rgba(75, 52, 30, 0.2);
			color: #fffdf2;
		}

		:deep(.menu_view .el-sub-menu__title),
		:deep(.menu_view .el-menu-item),
		:deep(.menu_view .el-menu--inline .menu_item_view),
		:deep(.el-menu--collapse .el-sub-menu__title),
		:deep(.el-menu--collapse .el-menu-item),
		:deep(.el-menu--collapse .el-menu--inline .menu_item_view) {
			background: rgba(255, 255, 250, 0.55);
			border-color: rgba(177, 145, 91, 0.18);
			box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72);
			color: #5b442d;
		}

		:deep(.menu_view .el-sub-menu__title span),
		:deep(.menu_view .el-sub-menu__title .iconfont),
		:deep(.menu_view .el-sub-menu__title .el-sub-menu__icon-arrow),
		:deep(.menu_view .el-menu-item),
		:deep(.menu_view .menu_item_view),
		:deep(.el-menu--collapse .el-sub-menu__title span),
		:deep(.el-menu--collapse .el-sub-menu__title .iconfont),
		:deep(.el-menu--collapse .el-sub-menu__title .el-sub-menu__icon-arrow) {
			color: #5b442d !important;
		}

		:deep(.menu_view .el-sub-menu__title:hover span),
		:deep(.menu_view .el-sub-menu__title:hover .iconfont),
		:deep(.menu_view .el-sub-menu__title:hover .el-sub-menu__icon-arrow),
		:deep(.menu_view .el-menu-item:hover),
		:deep(.menu_view .el-menu-item.is-active),
		:deep(.menu_view .is-active .el-sub-menu__title span),
		:deep(.menu_view .is-active .el-sub-menu__title .iconfont),
		:deep(.menu_view .is-active .el-sub-menu__title .el-sub-menu__icon-arrow) {
			color: #fffdf2 !important;
		}
	}

	.teacher_theme {
		background:
			radial-gradient(circle at 20% 0%, rgba(191, 92, 66, 0.08), transparent 34%),
			linear-gradient(180deg, #fff9e8 0%, #f5ecd5 48%, #ecf3e5 100%);
		border: 1px solid rgba(181, 149, 95, 0.28);
		box-shadow: 0 24px 46px rgba(103, 87, 56, 0.16), inset 0 1px 0 rgba(255, 255, 255, 0.76);
		color: #5b442d;

		.brand_mark {
			background: linear-gradient(135deg, #b45b45 0%, #d7a65f 48%, #e9d49b 100%);
			box-shadow: 0 14px 28px rgba(78, 43, 32, 0.22);
			border-radius: 50%;
		}

		.brand_title {
			color: #4f3925;
			font-family: "STKaiti", "KaiTi", "Microsoft YaHei", sans-serif;
			letter-spacing: 0;
		}

		.brand_subtitle {
			color: rgba(91, 68, 45, 0.7);
		}

		:deep(.menu_view .el-sub-menu__title:hover),
		:deep(.menu_view .el-menu-item:hover),
		:deep(.menu_view .el-menu-item.is-active),
		:deep(.menu_view .is-active .el-sub-menu__title),
		:deep(.menu_view .el-menu--inline .menu_item_view:hover),
		:deep(.menu_view .el-menu--inline .is-active.menu_item_view),
		:deep(.el-menu--collapse .el-sub-menu__title:hover),
		:deep(.el-menu--collapse .el-menu-item:hover),
		:deep(.el-menu--collapse .el-menu-item.is-active),
		:deep(.el-menu--collapse .is-active .el-sub-menu__title),
		:deep(.el-menu--collapse .el-menu--inline .menu_item_view:hover),
		:deep(.el-menu--collapse .el-menu--inline .is-active.menu_item_view) {
			background: linear-gradient(135deg, rgba(183, 91, 67, 0.95), rgba(219, 166, 92, 0.86));
			border-color: rgba(255, 238, 190, 0.26);
			box-shadow: 0 12px 22px rgba(75, 52, 30, 0.2);
			color: #fffdf2;
		}

		:deep(.menu_view .el-sub-menu__title),
		:deep(.menu_view .el-menu-item),
		:deep(.menu_view .el-menu--inline .menu_item_view),
		:deep(.el-menu--collapse .el-sub-menu__title),
		:deep(.el-menu--collapse .el-menu-item),
		:deep(.el-menu--collapse .el-menu--inline .menu_item_view) {
			background: rgba(255, 255, 250, 0.55);
			border-color: rgba(177, 145, 91, 0.18);
			box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72);
			color: #5b442d;
		}

		:deep(.menu_view .el-sub-menu__title span),
		:deep(.menu_view .el-sub-menu__title .iconfont),
		:deep(.menu_view .el-sub-menu__title .el-sub-menu__icon-arrow),
		:deep(.menu_view .el-menu-item),
		:deep(.menu_view .menu_item_view),
		:deep(.el-menu--collapse .el-sub-menu__title span),
		:deep(.el-menu--collapse .el-sub-menu__title .iconfont),
		:deep(.el-menu--collapse .el-sub-menu__title .el-sub-menu__icon-arrow) {
			color: #5b442d !important;
		}

		:deep(.menu_view .el-sub-menu__title:hover span),
		:deep(.menu_view .el-sub-menu__title:hover .iconfont),
		:deep(.menu_view .el-sub-menu__title:hover .el-sub-menu__icon-arrow),
		:deep(.menu_view .el-menu-item:hover),
		:deep(.menu_view .el-menu-item.is-active),
		:deep(.menu_view .is-active .el-sub-menu__title span),
		:deep(.menu_view .is-active .el-sub-menu__title .iconfont),
		:deep(.menu_view .is-active .el-sub-menu__title .el-sub-menu__icon-arrow) {
			color: #fffdf2 !important;
		}
	}
</style>
<style lang="scss">
	.el-popper{
		.el-menu--popup-container {
			.el-menu--popup{
				border: none;
				padding: 8px;
				background: rgba(19, 33, 55, 0.96);
				border-radius: 16px;
				box-shadow: 0 18px 32px rgba(28, 44, 72, 0.18);
				// 二级菜单
				.menu_item_view {
					padding: 0 18px;
					color: #e6eeff;
					background: transparent;
					border-radius: 12px;
					line-height: 42px;
					height: 42px;
				}
				// 二级悬浮
				.menu_item_view:hover {
					color: #fff;
					background: linear-gradient(135deg, rgba(62, 135, 255, 0.9), rgba(63, 222, 206, 0.72));
					line-height: 42px;
					height: 42px;
				}
				// 二级选中
				.is-active.menu_item_view {
					color: #fff;
					background: linear-gradient(135deg, rgba(62, 135, 255, 0.9), rgba(63, 222, 206, 0.72));
					line-height: 42px;
					height: 42px;
				}
			}
		}
	}
</style>
