<template>
	<div>
		<div class="app-contain">
			<div class="course_hero">
				<div class="hero_main">
					<div class="hero_badge">古诗词列表</div>
					<div class="hero_title">按年级浏览乡村儿童古诗词</div>
					<div class="hero_desc">在这里统一维护 1-6 年级古诗词资源，老师布置背诵任务时会直接复用这些数据。</div>
				</div>
				<div class="hero_stats">
					<div class="stat_card">
						<div class="stat_label">当前古诗数</div>
						<div class="stat_value">{{ total }}</div>
					</div>
					<div class="stat_card stat_warm">
						<div class="stat_label">当前筛选年级</div>
						<div class="stat_value">{{ searchQuery.grade || '全部' }}</div>
					</div>
				</div>
			</div>
			<div class="grade_quick_filter">
				<el-button
					class="grade_chip"
					:class="{ active: !searchQuery.grade }"
					@click="setGradeFilter('')">
					全部年级
				</el-button>
				<el-button
					v-for="item in gradeLists"
					:key="item"
					class="grade_chip"
					:class="{ active: searchQuery.grade === item }"
					@click="setGradeFilter(item)">
					{{ item }}
				</el-button>
			</div>
			<div class="list_search_view">
				<el-form :model="searchQuery" class="search_form" >
					<div class="search_view">
						<div class="search_label">古诗词号：</div>
						<div class="search_box">
							<el-input class="search_inp" v-model="searchQuery.courseno" placeholder="古诗词号" clearable></el-input>
						</div>
					</div>
					<div class="search_view">
						<div class="search_label">古诗词标题：</div>
						<div class="search_box">
							<el-input class="search_inp" v-model="searchQuery.coursetitle" placeholder="古诗词标题" clearable></el-input>
						</div>
					</div>
					<div class="search_view">
						<div class="search_label">古诗词类型：</div>
						<div class="search_box">
							<el-input class="search_inp" v-model="searchQuery.coursetype" placeholder="古诗词类型" clearable></el-input>
						</div>
					</div>
					<div class="search_view">
						<div class="search_label">适用年级：</div>
						<div class="search_box">
							<el-select class="search_inp" v-model="searchQuery.grade" placeholder="适用年级" clearable>
								<el-option v-for="item in gradeLists" :key="item" :label="item" :value="item"></el-option>
							</el-select>
						</div>
					</div>
					<div class="search_btn_view">
						<el-button class="search_btn" type="primary" @click="searchClick()" size="small">搜索</el-button>
						<el-button class="reset_btn" @click="resetSearch" size="small">重置</el-button>
					</div>
				</el-form>
				<br>
				<div class="btn_view">
					<el-button type="success" @click="addClick" v-if="btnAuth('course','新增')">新增</el-button>
					<el-button type="info" :disabled="selRows.length==1?false:true" @click="infoClick(null)" v-if=" btnAuth('course','查看')">详情</el-button>
					<el-button type="primary" :disabled="selRows.length==1?false:true" @click="editClick" v-if=" btnAuth('course','修改')">修改</el-button>
					<el-button type="danger" :disabled="selRows.length?false:true" @click="delClick(null)" v-if="btnAuth('course','删除')">删除</el-button>
				</div>
			</div>
			<br>
			<el-table
				v-loading="listLoading"
				border 
				:stripe='false'
				@selection-change="handleSelectionChange" 
				ref="table"
				v-if="btnAuth('course','查看')"
				:data="list"
				@row-click="listChange">
				<el-table-column :resizable='true' align="left" header-align="left" type="selection" width="55" />
				<el-table-column label="序号" width="70" :resizable='true' :sortable='true' align="left" header-align="left">
					<template #default="scope">{{ scope.$index + 1}}</template>
				</el-table-column>
				<el-table-column prop="courseno" label="古诗词号" :resizable='true' :sortable='true' align="left" header-align="left"></el-table-column>
				<el-table-column prop="coursetitle" label="古诗词标题" min-width="180" :resizable='true' :sortable='true' align="left" header-align="left">
					<template #default="scope">
						<div class="title_cell">
							<div class="title_text">{{ scope.row.coursetitle }}</div>
							<div class="intro_text">{{ scope.row.intro || '暂无简介' }}</div>
						</div>
					</template>
				</el-table-column>
				<el-table-column prop="coursetype" label="古诗词类型" :resizable='true' :sortable='true' align="left" header-align="left"></el-table-column>
				<el-table-column prop="grade" label="适用年级" :resizable='true' :sortable='true' align="left" header-align="left">
					<template #default="scope">
						<span class="grade_tag">{{ scope.row.grade }}</span>
					</template>
				</el-table-column>
				<el-table-column prop="addtime" label="最后更新时间" :resizable='true' :sortable='true' align="left" header-align="left"></el-table-column>
				<el-table-column label="操作" width="200" :resizable='true' align="left" header-align="left">
					<template #default="scope">
						<el-button type="info" size="small" v-if=" btnAuth('course','查看')" @click="infoClick(scope.row.id)">详情</el-button>
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
	import { reactive, ref, getCurrentInstance, nextTick, onMounted } from 'vue'
	import { useRoute, useRouter } from 'vue-router'
	import { ElMessageBox } from 'element-plus'
	const context = getCurrentInstance()?.appContext.config.globalProperties;
	import formModel from './formModel.vue'
	
	// 基础信息
	const tableName = 'course'
	const formName = '古诗词'
	const route = useRoute()
	const router = useRouter()
	
	// 列表数据
	const list = ref(null)
	const table = ref(null)
	const listQuery = ref({
		page: 1,
		limit: 20,
		sort: 'id',
		order: 'desc'
	})
	const searchQuery = ref({
		courseno: '',
		coursetitle: '',
		coursetype: '',
		grade: ''
	})
	const gradeLists = ref("一年级,二年级,三年级,四年级,五年级,六年级".split(','))
	const selRows = ref([])
	const listLoading = ref(false)
	
	// 行点击事件
	const listChange = (row) =>{
		nextTick(()=>{
			table.value.clearSelection()
			table.value.toggleRowSelection(row)
		})
	}
	
	// 获取列表数据
	const getList = () => {
		listLoading.value = true
		let params = JSON.parse(JSON.stringify(listQuery.value))
		params['sort'] = 'id'
		params['order'] = 'desc'
		
		// 搜索条件
		if(searchQuery.value.courseno) params['courseno'] = searchQuery.value.courseno
		if(searchQuery.value.coursetitle) params['coursetitle'] = '%' + searchQuery.value.coursetitle + '%'
		if(searchQuery.value.coursetype) params['coursetype'] = '%' + searchQuery.value.coursetype + '%'
		if(searchQuery.value.grade) params['grade'] = searchQuery.value.grade
		
		context?.$http({
			url: `${tableName}/page`,
			method: 'get',
			params: params
		}).then(res => {
			listLoading.value = false
			list.value = res.data.data.list
			total.value = Number(res.data.data.total)
		})
	}
	
	// 删除操作
	const delClick = (id) => {
		let ids = []
		if (id) {
			ids = [id]
		} else if (selRows.value.length) {
			ids = selRows.value.map(row => row.id)
		} else {
			return
		}
		
		ElMessageBox.confirm(`是否删除选中${formName}`, '提示', {
			confirmButtonText: '是',
			cancelButtonText: '否',
			type: 'warning',
		}).then(() => {
			context?.$http({
				url: `${tableName}/delete`,
				method: 'post',
				data: ids
			}).then(res => {
				context?.$toolUtil.message('删除成功', 'success', ()=>{
					getList()
				})
			})
		})
	}
	
	// 选择变化
	const handleSelectionChange = (e) => {
		selRows.value = e
	}
	
	// 分页控制
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
		listQuery.value.page--
		getList()
	}
	const nextClick = () => {
		listQuery.value.page++
		getList()
	}
	
	// 权限验证
	const btnAuth = (e,a)=>{
		return context?.$toolUtil.isAuth(e,a)
	}
	const setGradeFilter = (grade) => {
		searchQuery.value.grade = grade
		searchClick()
	}
	
	// 搜索
	const searchClick = () => {
		listQuery.value.page = 1
		getList()
	}
	const resetSearch = () => {
		searchQuery.value = {
			courseno: '',
			coursetitle: '',
			coursetype: '',
			grade: ''
		}
		searchClick()
	}
	
	// 表单相关
	const formRef = ref(null)
	const formModelChange=()=>{
		searchClick()
	}
	const addClick = ()=>{
		formRef.value.init()
	}
	const editClick = ()=>{
		if(selRows.value.length) {
			formRef.value.init(selRows.value[0].id, 'edit')
		}
	}
	const infoClick = (id=null)=>{
		if(id) {
			formRef.value.init(id, 'info')
		}else if(selRows.value.length) {
			formRef.value.init(selRows.value[0].id, 'info')
		}
	}
	
	
	// 初始化
	onMounted(() => {
		getList()
	})
</script>
<style lang="scss" scoped>
	.course_hero {
		display: flex;
		justify-content: space-between;
		gap: 18px;
		padding: 24px 28px;
		border-radius: 28px;
		margin-bottom: 18px;
		background:
			radial-gradient(circle at top right, rgba(255, 215, 150, 0.35), transparent 22%),
			linear-gradient(135deg, #fff8ea 0%, #f4fbf5 46%, #eef7ff 100%);
		box-shadow: 0 18px 45px rgba(114, 88, 35, 0.12);
	}
	.hero_badge {
		display: inline-flex;
		padding: 6px 14px;
		border-radius: 999px;
		background: #fff;
		color: #8a5a11;
		font-size: 12px;
		font-weight: 700;
		letter-spacing: 1px;
	}
	.hero_title {
		margin-top: 14px;
		color: #2f2a1f;
		font-size: 28px;
		font-weight: 700;
		line-height: 1.4;
	}
	.hero_desc {
		margin-top: 10px;
		max-width: 720px;
		color: #72654a;
		font-size: 14px;
		line-height: 1.8;
	}
	.hero_stats {
		display: flex;
		gap: 14px;
		align-items: stretch;
	}
	.stat_card {
		min-width: 150px;
		padding: 18px 20px;
		border-radius: 22px;
		background: rgba(255,255,255,0.86);
		border: 1px solid rgba(231, 220, 194, 0.95);
	}
	.stat_warm {
		background: linear-gradient(180deg, rgba(255,248,226,0.95) 0%, rgba(255,255,255,0.95) 100%);
	}
	.stat_label {
		color: #7b6e56;
		font-size: 13px;
	}
	.stat_value {
		margin-top: 10px;
		color: #2f2a1f;
		font-size: 28px;
		font-weight: 700;
	}
	.grade_quick_filter {
		display: flex;
		flex-wrap: wrap;
		gap: 10px;
		margin-bottom: 18px;
	}
	.grade_chip {
		border-radius: 999px;
		padding: 0 18px;
		height: 38px;
		border: 1px solid #e0d3b8;
		background: #fffdf8;
		color: #6c5d44;
	}
	.grade_chip.active {
		color: #fff;
		border-color: transparent;
		background: linear-gradient(135deg, #e6a53f 0%, #6dbf73 100%);
	}
	.list_search_view {
		margin: 20px auto;
		display: flex;
		width: 100%;
		flex-wrap: wrap;
		.search_form {
			display: flex;
			align-items: center;
			order: 2;
			.search_view {
				margin: 0 10px 0 0;
				display: flex;
				align-items: center;
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
				.search_box {
					display: inline-block;
					width: auto;
					:deep(.search_inp) {
						border: 1px solid #999;
						border-radius: 0px;
						padding: 0 10px;
						background: #fff;
						width: auto;
						line-height: 34px;
						box-sizing: border-box;
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
			.search_btn_view {
				width: auto;
				display: flex;
				padding: 0 20px;
				gap: 10px;
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
				.search_btn:hover {
					background: linear-gradient(30deg, rgba(130,196,209,1) 0%, rgba(115,186,200,1) 24%, rgba(174,210,217,1) 100%);
				}
				.reset_btn {
					border: 1px solid #d7c9ae;
					background: #fff;
					color: #7c6d54;
					height: 36px;
				}
			}
		}
		.btn_view {
			margin: 0;
			display: flex;
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
			:deep(.el-button--default:hover){
				background: linear-gradient(30deg, rgba(51,51,51,1) 0%, rgba(102,102,102,1) 50%, rgba(51,51,51,1) 100%);
			}
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
			:deep(.el-button--success:hover){
				background: linear-gradient(30deg, rgba(246,154,40,1) 0%, rgba(255,186,101,1) 50%, rgba(246,154,40,1) 100%);
			}
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
			:deep(.el-button--primary:hover){
				background: linear-gradient(30deg, rgba(25,169,123,1) 0%, rgba(58,214,164,1) 50%, rgba(25,169,123,1) 100%),#19a97b;
			}
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
			:deep(.el-button--info:hover){
				background: linear-gradient(30deg, rgba(40,172,246,1) 0%, rgba(111,203,255,1) 50%, rgba(40,172,246,1) 100%);
			}
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
			:deep(.el-button--danger:hover){
				background: linear-gradient(30deg, rgba(246,40,40,1) 0%, rgba(255,107,107,1) 50%, rgba(246,40,40,1) 100%);
			}
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
			:deep(.el-button--warning:hover){
				background: linear-gradient(30deg, rgba(0,205,236,1) 0%, rgba(66,230,255,1) 50%, rgba(0,205,236,1) 100%);
			}
		}
	}
	.title_cell {
		padding: 4px 0;
	}
	.title_text {
		color: #2f2a1f;
		font-weight: 700;
	}
	.intro_text {
		margin-top: 4px;
		color: #7b6e56;
		font-size: 12px;
		line-height: 1.7;
	}
	.grade_tag {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		padding: 4px 12px;
		border-radius: 999px;
		background: rgba(95, 185, 89, 0.12);
		color: #2f8d3e;
		font-size: 12px;
		font-weight: 700;
	}
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
	.el-pagination {
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
		:deep(.btn-prev:disabled), :deep(.btn-next:disabled) {
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
		:deep(.el-pager) {
			padding: 0;
			margin: 0;
			display: flex;
			vertical-align: top;
			align-items: center;
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
		:deep(.el-pagination__jump) {
			margin: 0 0 0 24px;
			color: #606266;
			display: inline-block;
			vertical-align: top;
			font-size: 13px;
			line-height: 28px;
			height: 28px;
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
	@media (max-width: 1200px) {
		.course_hero {
			flex-direction: column;
		}
		.hero_stats {
			flex-wrap: wrap;
		}
	}
</style>
