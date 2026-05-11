
<template>
	<div>
		<div class="app-contain">
			<div class="overview_panel" v-if="btnAuth('forum','查看')">
				<div class="overview_header">
					<div>
						<div class="overview_badge">学习社区</div>
						<div class="overview_title">社区互动数据总览</div>
						<div class="overview_desc">统一查看帖子数量、热度和图文内容分布，让社区内容管理更直观。</div>
					</div>
					<div class="overview_stat">
						<div class="overview_stat_label">当前帖子数</div>
						<div class="overview_stat_value">{{ total }}</div>
					</div>
				</div>
				<div class="overview_cards">
					<div class="overview_card">
						<div class="overview_value">{{ total }}</div>
						<div class="overview_label">帖子总数</div>
					</div>
					<div class="overview_card warm">
						<div class="overview_value">{{ forumStats.withImage }}</div>
						<div class="overview_label">图文帖子</div>
					</div>
					<div class="overview_card green">
						<div class="overview_value">{{ forumStats.totalLikes }}</div>
						<div class="overview_label">累计点赞</div>
					</div>
					<div class="overview_card blue">
						<div class="overview_value">{{ forumStats.hotPosts }}</div>
						<div class="overview_label">热门帖子</div>
					</div>
				</div>
			</div>
			<div class="list_search_view">
				<el-form :model="searchQuery" class="search_form" >
					<div class="search_view">
						<div class="search_label">
							贴子标题：
						</div>
						<div class="search_box">
							<el-input class="search_inp" v-model="searchQuery.forumtitle" placeholder="贴子标题"
								clearable>
							</el-input>
						</div>
					</div>
					<div class="search_btn_view">
						<el-button class="search_btn" type="primary" @click="searchClick()" size="small">搜索</el-button>
					</div>
				</el-form>
				<br>
				<div class="btn_view">
					<el-button class="action_btn create_btn" type="success" @click="addClick" v-if="btnAuth('forum','新增')">新增</el-button>
					<el-button class="action_btn" v-if=" btnAuth('forum','查看')" type="info"  :disabled="selRows.length==1?false:true" @click="infoClick(null)">详情</el-button>
					<el-button class="action_btn" type="primary" :disabled="selRows.length==1?false:true" @click="editClick" v-if=" btnAuth('forum','修改')">修改</el-button>
					<el-button class="action_btn" type="danger" :disabled="selRows.length?false:true" @click="delClick(null)"  v-if="btnAuth('forum','删除')">删除</el-button>
				</div>
			</div>
			<br>
			<el-table
				class="beauty_table"
				v-loading="listLoading"
				border 
				:stripe='false'
				@selection-change="handleSelectionChange" 
				ref="table"
				v-if="btnAuth('forum','查看')"
				:data="list"
				@row-click="listChange">
				<el-table-column :resizable='true' align="left" header-align="left" type="selection" width="55" />
				<el-table-column label="序号" width="70" :resizable='true' :sortable='true' align="left" header-align="left">
					<template #default="scope">{{ scope.$index + 1}}</template>
				</el-table-column>
				<el-table-column label="图片" width="120" :resizable='true' :sortable='true' align="left" header-align="left">
					<template #default="scope">
						<div v-if="scope.row.picture">
							<el-image v-if="scope.row.picture.substring(0,4)=='http'" preview-teleported
								:preview-src-list="[scope.row.picture.split(',')[0]]"
								:src="scope.row.picture.split(',')[0]" style="width:100px;height:100px"></el-image>
							<el-image v-else preview-teleported
								:preview-src-list="[$config.url+scope.row.picture.split(',')[0]]"
								:src="$config.url+scope.row.picture.split(',')[0]" style="width:100px;height:100px">
							</el-image>
						</div>
						<div v-else>无图片</div>
					</template>
				</el-table-column>
				<el-table-column
					 :resizable='true' 
					 :sortable='true' 
					 align="left" 
					 header-align="left"
					 prop="forumtitle"
					label="贴子标题">
					<template #default="scope">
						{{scope.row.forumtitle}}
					</template>
				</el-table-column>
				<el-table-column
					 :resizable='true' 
					 :sortable='true' 
					 align="left" 
					 header-align="left"
					 prop="releasetime"
					label="发布时间">
					<template #default="scope">
						{{scope.row.releasetime}}
					</template>
				</el-table-column>
				<el-table-column
					 :resizable='true' 
					 :sortable='true' 
					 align="left" 
					 header-align="left"
					 prop="thumbsupnum"
					label="赞">
					<template #default="scope">
						{{scope.row.thumbsupnum}}
					</template>
				</el-table-column>

				<el-table-column label="操作" width="300" :resizable='true' :sortable='true' align="left" header-align="left">
					<template #default="scope">
						<el-button type="info" v-if=" btnAuth('forum','查看')" @click="infoClick(scope.row.id)">详情</el-button>
						<el-button v-if="btnAuth('forum','查看评论')" type="warning" @click="commentClick(scope.row.id)">查看评论</el-button>
					</template>
				</el-table-column>
			</el-table>
			<el-pagination 
				background
				:layout="layouts.join(',')"
				:total="total" 
				:page-size="listQuery.limit"
				prev-text="上一页"
				next-text="下一页"
				:hide-on-single-page="false"
				:style='{"padding":"0","margin":"20px auto","whiteSpace":"nowrap","color":"#333","alignItems":"center","textAlign":"center","display":"flex","width":"100%","fontWeight":"500","justifyContent":"center"}'
				@size-change="sizeChange"
				@current-change="currentChange" 
				@prev-click="prevClick"
				@next-click="nextClick"  />
		</div>
		<formModel ref="formRef" @formModelChange="formModelChange"></formModel>
	</div>
</template>
<script setup>
	import axios from 'axios'
	import {
		reactive,
		ref,
		getCurrentInstance,
		nextTick,
		onMounted,
		watch,
	} from 'vue'
	import {
		useRoute,
		useRouter
	} from 'vue-router'
	import {
		ElMessageBox
	} from 'element-plus'
	const context = getCurrentInstance()?.appContext.config.globalProperties;
	import formModel from './formModel.vue'
	
	//基础信息
	const tableName = 'forum'
	const formName = '学习社区'
	const route = useRoute()
	//基础信息
	onMounted(()=>{
	})
	//列表数据
	const list = ref(null)
	const table = ref(null)
	const listQuery = ref({
		page: 1,
		limit: 20,
		sort: 'id',
		order: 'desc'
	})
	const searchQuery = ref({})
	const selRows = ref([])
	const listLoading = ref(false)
	const forumStats = ref({ withImage: 0, totalLikes: 0, hotPosts: 0 })
	const listChange = (row) =>{
		nextTick(()=>{
			table.value.clearSelection()
			table.value.toggleRowSelection(row)
		})
	}
	//列表
	const getList = () => {
		listLoading.value = true
		let params = JSON.parse(JSON.stringify(listQuery.value))
		params['sort'] = 'id'
		params['order'] = 'desc'
		if(searchQuery.value.forumtitle&&searchQuery.value.forumtitle!=''){
			params['forumtitle'] = '%' + searchQuery.value.forumtitle + '%'
		}
		context?.$http({
			url: `${tableName}/page`,
			method: 'get',
			params: params
		}).then(res => {
			listLoading.value = false
			list.value = res.data.data.list
			total.value = Number(res.data.data.total)
			buildForumStats(list.value || [])
		})
	}
	const buildForumStats = (rows=[]) => {
		let withImage = 0
		let totalLikes = 0
		let hotPosts = 0
		rows.forEach(item => {
			if (item.picture) withImage += 1
			const likes = Number(item.thumbsupnum || 0)
			totalLikes += likes
			if (likes >= 10) hotPosts += 1
		})
		forumStats.value = { withImage, totalLikes, hotPosts }
	}
	//删
	const delClick = (id) => {
		let ids = ref([])
		if (id) {
			ids.value = [id]
		} else {
			if (selRows.value.length) {
				for (let x in selRows.value) {
					ids.value.push(selRows.value[x].id)
				}
			} else {
				return false
			}
		}
		ElMessageBox.confirm(`是否删除选中${formName}`, '提示', {
			confirmButtonText: '是',
			cancelButtonText: '否',
			type: 'warning',
		}).then(() => {
			context?.$http({
				url: `${tableName}/delete`,
				method: 'post',
				data: ids.value
			}).then(res => {
				context?.$toolUtil.message('删除成功', 'success',()=>{
					getList()
				})
			})
		})
	}
	//多选
	const handleSelectionChange = (e) => {
		selRows.value = e
	}
	//列表数据
	//分页
	const total = ref(0)
	const layouts = ref(["total","prev","pager","next","sizes"])
	const sizeChange = (size) => {
		listQuery.value.limit = size
		getList()
	}
	const currentChange = (page) => {
		listQuery.value.page = page
		getList()
	}
	const prevClick = () => {
		listQuery.value.page = listQuery.value.page - 1
		getList()
	}
	const nextClick = () => {
		listQuery.value.page = listQuery.value.page + 1
		getList()
	}
	//分页
	//权限验证
	const btnAuth = (e,a)=>{
		return context?.$toolUtil.isAuth(e,a)
	}
	//搜索
	const searchClick = () => {
		listQuery.value.page = 1
		getList()
	}
	//表单
	const formRef = ref(null)
	const formModelChange=()=>{
		searchClick()
	}
	const addClick = ()=>{
		formRef.value.init()
	}
	const editClick = ()=>{
		if(selRows.value.length){
			formRef.value.init(selRows.value[0].id,'edit')
		}
	}
	
	const infoClick = (id=null)=>{
		if(id){
			formRef.value.init(id,'info')
		}
		else if(selRows.value.length){
			formRef.value.init(selRows.value[0].id,'info')
		}
	}
	// 表单
	// 预览文件
	const preClick = (file) =>{
		if(!file){
			context?.$toolUtil.message('文件不存在','error')
		}
		window.open(context?.$config.url + file)
		// const a = document.createElement('a');
		// a.style.display = 'none';
		// a.setAttribute('target', '_blank');
		// file && a.setAttribute('download', file);
		// a.href = context?.$config.url + file;
		// document.body.appendChild(a);
		// a.click();
		// document.body.removeChild(a);
	}
	// 下载文件
	const download = (file) => {
		if(!file){
			context?.$toolUtil.message('文件不存在','error')
		}
		let arr = file.replace(new RegExp('file/', "g"), "")
		axios.get((location.href.split(context?.$config.name).length>1 ? location.href.split(context?.$config.name)[0] :'') + context?.$config.name + '/file/download?fileName=' + arr, {
			headers: {
				token: context?.$toolUtil.storageGet('Token')
			},
			responseType: "blob"
		}).then(({
			data
		}) => {
			const binaryData = [];
			binaryData.push(data);
			const objectUrl = window.URL.createObjectURL(new Blob(binaryData, {
				type: 'application/pdf;chartset=UTF-8'
			}))
			const a = document.createElement('a')
			a.href = objectUrl
			a.download = arr
			// a.click()
			// 下面这个写法兼容火狐
			a.dispatchEvent(new MouseEvent('click', {
				bubbles: true,
				cancelable: true,
				view: window
			}))
			window.URL.revokeObjectURL(data)
		})
	}


    // 查看评论
	const commentClick=(id)=>{
		context?.$router.push('/discussforum?refid=' + id)
	}
	//初始化
	const init = () => {
		getList()
	}
	init()
</script>
<style lang="scss" scoped>
	
	// 操作盒子
	.list_search_view {
		margin: 20px auto;
		display: flex;
		width: 100%;
		flex-wrap: wrap;
		// 搜索盒子
		.search_form {
			display: flex;
			align-items: center;
			order: 2;
			// 子盒子
			.search_view {
				margin: 0 10px 0 0;
				display: flex;
				align-items: center;
				// 搜索label
				.search_label {
					margin: 0 10px 0 0;
					color: #666;
					background: none;
					font-weight: 500;
					display: inline-block;
					width: auto;
					font-size: 14px;
					line-height: 40px;
					text-align: right;
					min-width: 100px;
					height: 40px;
				}
				// 搜索item
				.search_box {
					display: inline-block;
					width: auto;
					// 输入框
					:deep(.search_inp) {
						border: 1px solid #999;
						border-radius: 0px;
						padding: 0 10px;
						background: #fff;
						width: auto;
						line-height: 34px;
						box-sizing: border-box;
						//去掉默认样式
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
			// 搜索按钮盒子
			.search_btn_view {
				width: 20%;
				display: flex;
				padding: 0 20px;
				// 搜索按钮
				.search_btn {
					border: 0px solid #f69a28;
					cursor: pointer;
					border-radius: 0px;
					padding: 0 24px;
					color: #fff;
					background: linear-gradient(270deg, rgba(130,196,209,1) 0%, rgba(115,186,200,1) 24%, rgba(174,210,217,1) 100%);
					width: auto;
					font-size: 14px;
					height: 36px;
				}
				// 搜索按钮-悬浮
				.search_btn:hover {
					background: linear-gradient(30deg, rgba(130,196,209,1) 0%, rgba(115,186,200,1) 24%, rgba(174,210,217,1) 100%);
				}
			}
		}
		//头部按钮盒子
		.btn_view {
			margin: 0;
			display: flex;
			// 其他
			:deep(.el-button--default){
				border: 1px solid #666;
				cursor: pointer;
				border-radius: 0px;
				padding: 0 16px;
				margin: 0 10px 0 0;
				color: #fff;
				background: linear-gradient(270deg, rgba(51,51,51,1) 0%, rgba(102,102,102,1) 50%, rgba(51,51,51,1) 100%);
				width: auto;
				font-size: 14px;
				height: 36px;
			}
			// 其他-悬浮
			:deep(.el-button--default:hover){
				background: linear-gradient(30deg, rgba(51,51,51,1) 0%, rgba(102,102,102,1) 50%, rgba(51,51,51,1) 100%);
			}
			// 新增
			:deep(.el-button--success){
				border: 1px solid #f69a28;
				cursor: pointer;
				border-radius: 0px;
				padding: 0 16px;
				margin: 0 10px 0 0;
				color: #fff;
				background: linear-gradient(270deg, rgba(246,154,40,1) 0%, rgba(255,186,101,1) 50%, rgba(246,154,40,1) 100%);
				width: auto;
				font-size: 14px;
				height: 36px;
			}
			// 新增-悬浮
			:deep(.el-button--success:hover){
				background: linear-gradient(30deg, rgba(246,154,40,1) 0%, rgba(255,186,101,1) 50%, rgba(246,154,40,1) 100%);
			}
			// 修改
			:deep(.el-button--primary){
				border: 1px solid #139666;
				cursor: pointer;
				border-radius: 0px;
				padding: 0 16px;
				margin: 0 10px 0 0;
				color: #fff;
				background: linear-gradient(270deg, rgba(25,169,123,1) 0%, rgba(58,214,164,1) 50%, rgba(25,169,123,1) 100%),#19a97b;
				width: auto;
				font-size: 14px;
				height: 36px;
			}
			// 修改-悬浮
			:deep(.el-button--primary:hover){
				background: linear-gradient(30deg, rgba(25,169,123,1) 0%, rgba(58,214,164,1) 50%, rgba(25,169,123,1) 100%),#19a97b;
			}
			// 详情
			:deep(.el-button--info){
				border: 1px solid #28acf6;
				cursor: pointer;
				border-radius: 0px;
				padding: 0 16px;
				margin: 0 10px 0 0;
				color: #fff;
				background: linear-gradient(270deg, rgba(40,172,246,1) 0%, rgba(111,203,255,1) 50%, rgba(40,172,246,1) 100%);
				width: auto;
				font-size: 14px;
				height: 36px;
			}
			// 详情-悬浮
			:deep(.el-button--info:hover){
				background: linear-gradient(30deg, rgba(40,172,246,1) 0%, rgba(111,203,255,1) 50%, rgba(40,172,246,1) 100%);
			}
			// 删除
			:deep(.el-button--danger){
				border: 1px solid #f62828;
				cursor: pointer;
				border-radius: 0px;
				padding: 0 16px;
				margin: 0 10px 0 0;
				color: #fff;
				background: linear-gradient(270deg, rgba(246,40,40,1) 0%, rgba(255,107,107,1) 50%, rgba(246,40,40,1) 100%);
				width: auto;
				font-size: 14px;
				height: 36px;
			}
			// 删除-悬浮
			:deep(.el-button--danger:hover){
				background: linear-gradient(30deg, rgba(246,40,40,1) 0%, rgba(255,107,107,1) 50%, rgba(246,40,40,1) 100%);
			}
			// 统计
			:deep(.el-button--warning){
				border: 1px solid #00cdec;
				cursor: pointer;
				border-radius: 0px;
				padding: 0 12px;
				margin: 0 10px 0 0;
				color: #fff;
				background: linear-gradient(270deg, rgba(0,205,236,1) 0%, rgba(66,230,255,1) 50%, rgba(0,205,236,1) 100%);
				width: auto;
				font-size: 14px;
				height: 36px;
			}
			// 统计-悬浮
			:deep(.el-button--warning:hover){
				background: linear-gradient(30deg, rgba(0,205,236,1) 0%, rgba(66,230,255,1) 50%, rgba(0,205,236,1) 100%);
			}
		}
	}
	// 表格样式
	.el-table {
		border-radius: 0px;
		padding: 0;
		margin: 0 auto;
		background: #fff;
		width: 100%;
		border-color: #c7d6e5;
		border-width: 1px 0 0 1px;
		border-style: solid;
		:deep(.el-table__header-wrapper) {
			thead {
				color: #999;
				font-weight: 500;
				width: 100%;
				tr {
					background: #f8f8f8;
					th {
						padding: 4px 0;
						background: linear-gradient(180deg, rgba(255,255,255,1) 0%, rgba(226,240,249,1) 100%);
						border-color: #c7d6e5;
						border-width: 0 0px 1px 0;
						border-style: solid;
						text-align: left;
						.cell {
							padding: 0 0 0 5px;
							word-wrap: normal;
							white-space: normal;
							font-weight: bold;
							display: flex;
							vertical-align: middle;
							font-size: 12px;
							line-height: 24px;
							text-overflow: ellipsis;
							word-break: break-all;
							width: 100%;
							align-items: center;
							position: relative;
						}
					}
				}
			}
		}
		:deep(.el-table__body-wrapper) {
			tbody {
				width: 100%;
				tr {
					background: #fff;
					td {
						padding: 6px 0;
						color: #555;
						background: #fff;
						border-color: #fff;
						border-width: 1px 0px 1px 0;
						border-style: solid;
						text-align: left;
						.cell {
							padding: 0 5px;
							overflow: hidden;
							word-break: break-all;
							white-space: normal;
							line-height: 24px;
							text-overflow: ellipsis;
							// 编辑
							.el-button--primary {
								border: 0;
								cursor: pointer;
								border-radius: 4px;
								padding: 0 5px;
								margin: 0 5px 5px 0;
								color: #01b70e;
								background: #01b70e10;
								width: auto;
								font-size: 14px;
								height: 24px;
							}
							// 编辑-悬浮
							.el-button--primary:hover {
							}
							// 详情
							.el-button--info {
								border: 0;
								cursor: pointer;
								border-radius: 4px;
								padding: 0 5px;
								margin: 0 5px 5px 0;
								color: #0172b7;
								background: #0172b710;
								width: auto;
								font-size: 14px;
								line-height: 24px;
								height: 24px;
							}
							// 详情-悬浮
							.el-button--info:hover {
							}
							// 删除
							.el-button--danger {
								border: 0;
								cursor: pointer;
								border-radius: 4px;
								padding: 0 5px;
								margin: 0 5px 5px 0;
								color: #f51403;
								background: #f5140310;
								width: auto;
								font-size: 14px;
								height: 24px;
							}
							// 删除-悬浮
							.el-button--danger:hover {
							}
							// 跨表
							.el-button--success {
								border: 0;
								cursor: pointer;
								border-radius: 4px;
								padding: 0 5px;
								margin: 0 5px 5px 0;
								color: #f3842c;
								background: #f3842c20;
								width: auto;
								font-size: 14px;
								height: 24px;
							}
							// 跨表-悬浮
							.el-button--success:hover {
							}
							// 操作
							.el-button--warning {
								border: 0;
								cursor: pointer;
								border-radius: 4px;
								padding: 0 5px;
								margin: 0 5px 5px 0;
								color: #cd0456;
								background: #cd045610;
								width: auto;
								font-size: 14px;
								height: 24px;
							}
							// 操作-悬浮
							.el-button--warning:hover {
							}
						}
					}
				}
				tr:hover {
					td {
						padding: 6px 0;
						color: #555;
						background: #f5fbfe;
						border-color: #d0e1f1;
						border-width: 1px 0px 1px 0;
						border-style: solid;
						text-align: left;
					}
				}
			}
		}
	}
	// 分页器
	.el-pagination {
		// 总页码
		:deep(.el-pagination__total) {
			margin: 0 10px 0 0;
			color: #666;
			font-weight: 400;
			display: inline-block;
			vertical-align: top;
			font-size: 13px;
			line-height: 28px;
			height: 28px;
		}
		// 上一页
		:deep(.btn-prev) {
			border: none;
			border-radius: 0px;
			padding: 0 5px;
			margin: 0 5px;
			color: #fff;
			background: #19a97b90;
			display: inline-block;
			vertical-align: top;
			font-size: 13px;
			line-height: 26px;
			min-width: 35px;
			height: 26px;
		}
		// 下一页
		:deep(.btn-next) {
			border: none;
			border-radius: 0px;
			padding: 0 5px;
			margin: 0 5px;
			color: #fff;
			background: #19a97b90;
			display: inline-block;
			vertical-align: top;
			font-size: 13px;
			line-height: 26px;
			min-width: 35px;
			height: 26px;
		}
		// 上一页禁用
		:deep(.btn-prev:disabled) {
			border: none;
			cursor: not-allowed;
			border-radius: 0px;
			padding: 0 5px;
			margin: 0 5px;
			color: #fff;
			background: #19a97b90;
			display: inline-block;
			vertical-align: top;
			font-size: 13px;
			line-height: 26px;
			height: 26px;
		}
		// 下一页禁用
		:deep(.btn-next:disabled) {
			border: none;
			cursor: not-allowed;
			border-radius: 0px;
			padding: 0 5px;
			margin: 0 5px;
			color: #fff;
			background: #19a97b90;
			display: inline-block;
			vertical-align: top;
			font-size: 13px;
			line-height: 26px;
			height: 26px;
		}
		// 页码
		:deep(.el-pager) {
			padding: 0;
			margin: 0;
			display: flex;
			vertical-align: top;
			align-items: center;
			// 数字
			.number {
				cursor: pointer;
				padding: 0 4px;
				margin: 0 5px;
				color: #333;
				display: inline-block;
				vertical-align: top;
				font-size: 13px;
				line-height: 26px;
				border-radius: 0px;
				background: #eee;
				text-align: center;
				min-width: 30px;
				height: 26px;
			}
			// 数字悬浮
			.number:hover {
				cursor: pointer;
				padding: 0 4px;
				margin: 0 5px;
				color: #fff;
				display: inline-block;
				vertical-align: top;
				font-size: 13px;
				line-height: 26px;
				border-radius: 0px;
				background: #11a274;
				text-align: center;
				min-width: 30px;
				height: 26px;
			}
			// 选中
			.number.is-active {
				cursor: default;
				padding: 0 4px;
				margin: 0 5px;
				color: #fff;
				display: inline-block;
				vertical-align: top;
				font-size: 13px;
				line-height: 26px;
				border-radius: 0px;
				background: #11a274;
				text-align: center;
				min-width: 30px;
				height: 26px;
			}
		}
		// sizes
		:deep(.el-pagination__sizes) {
			display: inline-block;
			vertical-align: top;
			font-size: 13px;
			line-height: 28px;
			height: 28px;
			.el-select {
				border: 0px solid #ddd;
				cursor: pointer;
				padding: 0;
				color: #606266;
				display: inline-block;
				font-size: 13px;
				line-height: 26px;
				border-radius: 3px;
				box-shadow: none;
				background: #fff;
				width: 100%;
				text-align: center;
				height: 26px;
			}
		}
		// 跳页
		:deep(.el-pagination__jump) {
			margin: 0 0 0 24px;
			color: #606266;
			display: inline-block;
			vertical-align: top;
			font-size: 13px;
			line-height: 28px;
			height: 28px;
			// 输入框
			.el-input {
				border: 1px solid #DCDFE6;
				cursor: pointer;
				padding: 0 3px;
				color: #606266;
				display: inline-block;
				font-size: 14px;
				line-height: 28px;
				border-radius: 3px;
				outline: 0;
				background: #FFF;
				width: 100%;
				text-align: center;
				height: 28px;
				//去掉默认样式
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
</style>
<style lang="scss" scoped>
.app-contain .overview_panel {
	background: linear-gradient(135deg, #eff8ff 0%, #fff8ef 100%) !important;
	border: 1px solid rgba(157, 184, 205, 0.35) !important;
	border-radius: 26px !important;
	padding: 24px !important;
	margin-bottom: 20px !important;
	box-shadow: 0 18px 40px rgba(61, 95, 122, 0.1) !important;
}
.app-contain .overview_header { display: flex !important; justify-content: space-between !important; align-items: flex-start !important; gap: 20px !important; margin-bottom: 18px !important; }
.app-contain .overview_badge { display: inline-flex !important; align-items: center !important; padding: 6px 12px !important; border-radius: 999px !important; background: rgba(39, 123, 192, 0.12) !important; color: #1c6ca8 !important; font-size: 13px !important; font-weight: 700 !important; margin-bottom: 10px !important; }
.app-contain .overview_title { font-size: 28px !important; line-height: 1.3 !important; font-weight: 800 !important; color: #17324a !important; }
.app-contain .overview_desc { margin-top: 8px !important; color: #4a6477 !important; line-height: 1.8 !important; font-size: 18px !important; }
.app-contain .overview_stat { min-width: 180px !important; padding: 16px 18px !important; border-radius: 20px !important; background: rgba(255, 255, 255, 0.9) !important; box-shadow: inset 0 0 0 1px rgba(210, 225, 236, 0.85) !important; }
.app-contain .overview_stat_label { color: #6b8292 !important; font-size: 16px !important; }
.app-contain .overview_stat_value { margin-top: 8px !important; font-size: 36px !important; font-weight: 800 !important; color: #204966 !important; }
.app-contain .overview_cards { display: grid !important; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)) !important; gap: 14px !important; }
.app-contain .overview_card { background: rgba(255, 255, 255, 0.92) !important; border-radius: 20px !important; padding: 18px 20px !important; box-shadow: inset 0 0 0 1px rgba(221, 232, 241, 0.95) !important; }
.app-contain .overview_card.warm { background: linear-gradient(135deg, #fff7eb 0%, #fff1d9 100%) !important; }
.app-contain .overview_card.green { background: linear-gradient(135deg, #eefbf4 0%, #dcf5e7 100%) !important; }
.app-contain .overview_card.blue { background: linear-gradient(135deg, #eef6ff 0%, #deedff 100%) !important; }
.app-contain .overview_value { font-size: 34px !important; font-weight: 800 !important; color: #21455d !important; line-height: 1.1 !important; }
.app-contain .overview_label { margin-top: 8px !important; color: #6c8293 !important; font-size: 18px !important; }

.app-contain .list_search_view { background: rgba(255, 255, 255, 0.94) !important; border: 1px solid rgba(157, 184, 205, 0.3) !important; border-radius: 22px !important; padding: 18px 20px !important; box-shadow: 0 12px 30px rgba(79, 112, 138, 0.08) !important; }
.app-contain .btn_view { display: flex !important; flex-wrap: wrap !important; gap: 12px !important; margin-top: 14px !important; }
.app-contain .action_btn,
.app-contain .search_btn_view :deep(.el-button) { border-radius: 999px !important; padding: 10px 22px !important; border: none !important; box-shadow: 0 10px 22px rgba(72, 114, 146, 0.12) !important; font-weight: 600 !important; }

.app-contain .beauty_table {
	border-radius: 22px !important;
	overflow: hidden !important;
	border: 1px solid #e5eef5 !important;
	box-shadow: 0 14px 34px rgba(63, 92, 117, 0.08) !important;
}
.app-contain .beauty_table :deep(th.el-table__cell) { background: linear-gradient(180deg, #f4fbff 0%, #eaf5fb 100%) !important; color: #375063 !important; font-weight: 700 !important; }
.app-contain .beauty_table :deep(.el-table__row td.el-table__cell) { padding-top: 14px !important; padding-bottom: 14px !important; }
.app-contain .beauty_table :deep(.el-image) { border-radius: 16px !important; overflow: hidden !important; box-shadow: 0 10px 24px rgba(55, 85, 111, 0.16) !important; }
</style>
