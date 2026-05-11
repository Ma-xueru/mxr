<template>
	<div class="layout_shell" :class="themeClass">
		<index-aside :collapse="collapse" :class="collapse ? 'index-aside-collapse' : 'index-aside'"></index-aside>
		<el-main class="main_view index_transition" style="max-width:100%" :class="collapse ? 'main_view-collapse' : ''">
			<index-header
				class="index_header index_transition"
				:collapse="collapse"
				@collapseChange="collapseChange"
				:style="{ width: '100%', 'max-width': '100%' }"
			>
			</index-header>
			<index-tags class="index_tags" :style="{ width: '100%', 'max-width': '100%' }">
			</index-tags>
			<div class="content_shell">
				<router-view class="router-view index_transition" style="background: transparent;max-width:100%">
				</router-view>
			</div>
		</el-main>
	</div>
</template>

<script setup>
	import IndexAside from '@/components/index/indexMenu'
	import IndexHeader from '@/components/index/indexTop'
	import IndexTags from '@/components/index/indexTags'
	import menu from "@/utils/menu";
	import router from '../../router'
	import {
		ref,
		getCurrentInstance,
		computed
	} from 'vue'
	const context = getCurrentInstance()?.appContext.config.globalProperties;
	const collapse = ref( false)
	const currentRole = ref(context?.$toolUtil.storageGet('role') || '管理员')
	const themeClass = computed(() => currentRole.value === '教师' ? 'theme-teacher' : 'theme-admin')
	const collapseChange = () => {
		collapse.value = !collapse.value
	}
	const menuList = ref(null)
	const role = ref('')
	const init = () => {
		const menus = menu.list()
		if (menus) {
			menuList.value = menus
		}
		role.value = context?.$toolUtil.storageGet('role')
		for (let i = 0; i < menuList.value.length; i++) {
			if (menuList.value[i].roleName == role.value) {
				menuList.value = menuList.value[i].backMenu;
				break;
			}
		}
		let arr = makeMenu(menuList.value)

		router.addRoute(arr)
	}
	const makeMenu = (menu) => {
		let brr = {
			path: '/1',
			component: () => import('../../views/index'),
			children: []
		}
		for (let x in menu) {
			for (let i in menu[x].child) {
				brr.children.push({
					path: '/' + menu[x].child[i].tableName,
					name: menu[x].child[i].menu,
					component: () => import(`../../views/${menu[x].child[i].tableName}/list.vue`)
				})
			}
		}
		return brr
	}
	// init()
</script>
<style lang="scss" scoped>
	.layout_shell {
		position: relative;
		min-height: 100%;
		background:
			radial-gradient(circle at top left, rgba(72, 123, 255, 0.08), transparent 24%),
			linear-gradient(180deg, #c3d2e6 0%, #b6c8df 100%);
	}

	.el-main {
		padding: 0;
		margin: 0 0 0 220px;
		overflow: hidden;
		background: transparent;
	}
	.main_view-collapse {
		padding: 0;
		margin: 0 0 0 88px;
		overflow: hidden;
		background: transparent;
	}
	.main_view {
		position: relative;
		padding:0;
	}

	.content_shell {
		padding: 0 18px 24px;
	}

	.index-aside {
		padding: 18px 0 24px;
		overflow: hidden;
		top: 16px;
		left: 16px;
		background: transparent;
		width: 220px;
		position: fixed;
		height: calc(100% - 32px);
	}
	.index-aside-collapse {
		box-shadow: 0 20px 38px rgba(33, 47, 87, 0.16);
		overflow: hidden;
		top: 16px;
		left: 16px;
		background: transparent;
		width: 88px;
		position: fixed;
		height: calc(100% - 32px);
		border-radius: 28px;
	}

	.index_header {
		width: 100%;
		z-index: 999;
	}

	.index_tags {
		width: 100%;
		z-index: 999;
	}

	.index_transition{
		transition:all .35s;
	}

	.theme-admin {
		background:
			radial-gradient(ellipse at 18% 12%, rgba(164, 74, 54, 0.055), transparent 28%),
			radial-gradient(ellipse at 88% 14%, rgba(78, 126, 88, 0.075), transparent 30%),
			linear-gradient(180deg, #fbf6e8 0%, #f5f0df 46%, #edf3e6 100%);
		&::before {
			content: "";
			position: fixed;
			right: 36px;
			bottom: 28px;
			width: 420px;
			height: 190px;
			pointer-events: none;
			opacity: 0.09;
			background:
				radial-gradient(ellipse at 18% 100%, rgba(55, 88, 68, 0.8) 0 30%, transparent 31%),
				radial-gradient(ellipse at 48% 100%, rgba(55, 88, 68, 0.62) 0 38%, transparent 39%),
				radial-gradient(ellipse at 78% 100%, rgba(55, 88, 68, 0.5) 0 28%, transparent 29%);
		}
		&::after {
			content: "";
			position: fixed;
			left: 250px;
			top: 110px;
			width: calc(100% - 300px);
			height: calc(100% - 150px);
			pointer-events: none;
			opacity: 0.16;
			background:
				linear-gradient(90deg, transparent 0 96%, rgba(142, 114, 65, 0.16) 96% 97%, transparent 97%) 0 0 / 44px 44px,
				linear-gradient(0deg, transparent 0 96%, rgba(142, 114, 65, 0.12) 96% 97%, transparent 97%) 0 0 / 44px 44px;
		}
	}

	.theme-teacher {
		background:
			radial-gradient(ellipse at 18% 12%, rgba(164, 74, 54, 0.055), transparent 28%),
			radial-gradient(ellipse at 88% 14%, rgba(78, 126, 88, 0.075), transparent 30%),
			linear-gradient(180deg, #fbf6e8 0%, #f5f0df 46%, #edf3e6 100%);
		&::before {
			content: "";
			position: fixed;
			right: 36px;
			bottom: 28px;
			width: 420px;
			height: 190px;
			pointer-events: none;
			opacity: 0.09;
			background:
				radial-gradient(ellipse at 18% 100%, rgba(55, 88, 68, 0.8) 0 30%, transparent 31%),
				radial-gradient(ellipse at 48% 100%, rgba(55, 88, 68, 0.62) 0 38%, transparent 39%),
				radial-gradient(ellipse at 78% 100%, rgba(55, 88, 68, 0.5) 0 28%, transparent 29%);
			filter: blur(0.2px);
		}
		&::after {
			content: "";
			position: fixed;
			left: 250px;
			top: 110px;
			width: calc(100% - 300px);
			height: calc(100% - 150px);
			pointer-events: none;
			opacity: 0.16;
			background:
				linear-gradient(90deg, transparent 0 96%, rgba(142, 114, 65, 0.16) 96% 97%, transparent 97%) 0 0 / 44px 44px,
				linear-gradient(0deg, transparent 0 96%, rgba(142, 114, 65, 0.12) 96% 97%, transparent 97%) 0 0 / 44px 44px;
		}
	}
</style>
