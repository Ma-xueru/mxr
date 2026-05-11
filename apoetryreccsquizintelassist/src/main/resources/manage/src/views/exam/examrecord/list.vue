<template>
	<div>
		<div class="app-contain">
			<div>
				<div class="overview_panel">
					<div class="overview_header">
						<div>
							<div class="overview_badge">测试记录</div>
							<div class="overview_title">练习成绩概览</div>
							<div class="overview_desc">按当前筛选结果统计测试记录数量、及格情况和平均分，帮助老师快速掌握学习成效。</div>
						</div>
						<div class="overview_stat">
							<div class="overview_stat_label">当前记录数</div>
							<div class="overview_stat_value">{{ total }}</div>
						</div>
					</div>
					<div class="overview_cards">
						<div class="overview_card">
							<div class="overview_value">{{ total }}</div>
							<div class="overview_label">记录总数</div>
						</div>
						<div class="overview_card warm">
							<div class="overview_value">{{ recordStats.passCount }}</div>
							<div class="overview_label">及格记录</div>
						</div>
						<div class="overview_card blue">
							<div class="overview_value">{{ recordStats.avgScore }}</div>
							<div class="overview_label">平均分</div>
						</div>
					</div>
				</div>
				<div class="list_search_view">
					<el-form :model="searchQuery" class="search_form">
						<div class="search_view">
							<div class="search_label">
								古诗词测试：
							</div>
							<div class="search_box">
								<el-input class="search_inp" v-model="searchQuery.papername" placeholder="古诗词测试名称"
								 clearable>
								</el-input>
							</div>
						</div>
						<div class="search_btn_view">
							<el-button class="search_btn" type="primary" @click="searchClick()">搜索</el-button>
						</div>
					</el-form>
					<br>
					<div class="btn_view">
						<el-button class="action_btn" type="warning" @click="exportClick" v-if="btnAuth('examrecord','导出')">导出</el-button>
						<el-button class="action_btn" type="primary" @click="printClick" v-if="btnAuth('examrecord','打印')">打印</el-button>
					</div>
				</div>
			</div>
			<br>
			<el-table class="beauty_table" v-loading="listLoading" border :stripe='false' ref="table" :data="list">
				<el-table-column label="序号" width="120" :resizable='true' :sortable='true' align="left" header-align="left">
					<template #default="scope">{{ scope.$index + 1}}</template>
				</el-table-column>
				<el-table-column label="考生姓名" :resizable='true' :sortable='true' align="left" header-align="left">
					<template #default="scope">
						{{scope.row.username}}
					</template>
				</el-table-column>
				<el-table-column label="古诗词测试名称" :resizable='true' :sortable='true' align="left" header-align="left">
					<template #default="scope">
						{{scope.row.papername}}
					</template>
				</el-table-column>
				<el-table-column label="练习得分" :resizable='true' :sortable='true' align="left" header-align="left">
					<template #default="scope">
						<el-tag type="success" v-if="scope.row.myscore>0">{{scope.row.myscore}}</el-tag>
						<el-tag type="danger" v-else>{{scope.row.myscore}}</el-tag>
					</template>
				</el-table-column>
				<el-table-column label="操作" :resizable='true' :sortable='true' align="left" header-align="left">
					<template #default="scope">
						<el-button :type="'info'" @click="infoClick(scope.row)" size="small">查看答卷</el-button>
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
	import {
		ref,
		nextTick,
		getCurrentInstance
	} from 'vue';
	import {
		useRoute,
		useRouter
	} from 'vue-router'
	import {
		ElMessageBox
	} from 'element-plus'
	import formModel from './formModel.vue'
	const context = getCurrentInstance()?.appContext.config.globalProperties;
	//基础信息
	const tableName = 'examrecord'
	const formName = '测试记录'
	const route = useRoute()
	//基础信息
	const listLoading = ref(false)
	const list = ref([])
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
	const btnAuth = (e, a) => {
		return context?.$toolUtil.isAuth(e, a)
	}
	//搜索
	const searchClick = () => {
		listQuery.value.page = 1
		getList()
	}
	const table = ref(null)
	const listChange = (row) => {
		nextTick(() => {
			table.value.clearSelection()
			table.value.toggleRowSelection(row)
		})
	}
	const listQuery = ref({
		page: 1,
		limit: 20,
	})
	const searchQuery = ref({})
	const selRows = ref([])
	const recordStats = ref({ passCount: 0, avgScore: 0 })
	//多选
	const handleSelectionChange = (e) => {
		selRows.value = e
	}
	//导出选中数据
	const exportClick = () => {
		import('@/utils/Export2Excel').then(excel => {
			const tHeader = [
				"考生姓名",
				"古诗词测试名称",
				"总分",
			]
			const filterVal = [
				"username",
				"papername",
				"myscore",
			]
			excel.export_json_to_excel2(tHeader, list.value, filterVal, formName)
		})
	}
	//打印
	const printClick = () => {
		let data = []
		for (let x in list.value) {
			data.push({
				username: list.value[x].username,
				papername: list.value[x].papername,
				myscore: list.value[x].myscore,
			})
		}
		printJS({
			printable: data,
			properties: [{
					field: 'username',
					displayName: '考生姓名',
					columnSize: 1
				},
				{
					field: 'papername',
					displayName: '古诗词测试名称',
					columnSize: 1
				},
				{
					field: 'myscore',
					displayName: '总分',
					columnSize: 1
				},
			],
			type: 'json',
			header: formName,
			gridStyle: 'border: 1px solid #3971A5; text-align: center; padding: 6px 10px',
			gridHeaderStyle: 'color: #000; border: 1px solid #3971A5; padding: 6px 10px'
		})
	}
	//列表
	const getList = () => {
		listLoading.value = true
		let params = JSON.parse(JSON.stringify(listQuery.value))
		if (searchQuery.value.papername && searchQuery.value.papername != '') {
			params['papername'] = '%' + searchQuery.value.papername + '%'
		}
		context?.$http({
			url: `examrecord/groupby`,
			method: 'get',
			params: params
		}).then(res => {
			listLoading.value = false
			list.value = res.data.data.list
			total.value = Number(res.data.data.total)
			buildRecordStats(list.value || [])
		})
	}
	const buildRecordStats = (rows=[]) => {
		let passCount = 0
		let scoreSum = 0
		rows.forEach(item => {
			const score = Number(item.myscore || 0)
			scoreSum += score
			if (score >= 60) passCount += 1
		})
		recordStats.value = {
			passCount,
			avgScore: rows.length ? Math.round((scoreSum / rows.length) * 10) / 10 : 0
		}
	}
	const formRef = ref(null)
	const formModelChange = () => {
		searchClick()
	}
	const infoClick = (row) => {
		nextTick(()=>{
			formRef.value.init(row)
		})
	}
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
			// 打印
			:deep(.el-button--primary) {
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
			// 打印-悬浮
			:deep(.el-button--primary:hover) {
				background: linear-gradient(30deg, rgba(25,169,123,1) 0%, rgba(58,214,164,1) 50%, rgba(25,169,123,1) 100%),#19a97b;
			}
			// 导出
			:deep(.el-button--warning) {
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
			// 导出-悬浮
			:deep(.el-button--warning:hover) {
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
			.number.active {
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
.overview_panel {
	background: linear-gradient(135deg, #eff8ff 0%, #fff8ef 100%);
	border: 1px solid rgba(157, 184, 205, 0.35);
	border-radius: 26px;
	padding: 24px;
	margin-bottom: 20px;
	box-shadow: 0 18px 40px rgba(61, 95, 122, 0.1);
}
.overview_header { display: flex; justify-content: space-between; align-items: flex-start; gap: 20px; margin-bottom: 18px; }
.overview_badge { display: inline-flex; align-items: center; padding: 6px 12px; border-radius: 999px; background: rgba(39, 123, 192, 0.12); color: #1c6ca8; font-size: 13px; font-weight: 700; margin-bottom: 10px; }
.overview_title { font-size: 24px; line-height: 1.3; font-weight: 700; color: #264257; }
.overview_desc { margin-top: 8px; color: #61798c; line-height: 1.7; }
.overview_stat { min-width: 140px; padding: 16px 18px; border-radius: 20px; background: rgba(255, 255, 255, 0.88); box-shadow: inset 0 0 0 1px rgba(210, 225, 236, 0.85); }
.overview_stat_label { color: #7a8e9e; font-size: 13px; }
.overview_stat_value { margin-top: 8px; font-size: 30px; font-weight: 800; color: #204966; }
.overview_cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 14px; }
.overview_card { background: rgba(255, 255, 255, 0.92); border-radius: 20px; padding: 18px 20px; box-shadow: inset 0 0 0 1px rgba(221, 232, 241, 0.95); }
.overview_card.warm { background: linear-gradient(135deg, #fff7eb 0%, #fff1d9 100%); }
.overview_card.blue { background: linear-gradient(135deg, #eef6ff 0%, #deedff 100%); }
.overview_value { font-size: 28px; font-weight: 800; color: #21455d; line-height: 1.1; }
.overview_label { margin-top: 8px; color: #6c8293; font-size: 14px; }

.list_search_view { background: rgba(255, 255, 255, 0.94); border: 1px solid rgba(157, 184, 205, 0.3); border-radius: 22px; padding: 18px 20px !important; box-shadow: 0 12px 30px rgba(79, 112, 138, 0.08); }
.btn_view { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 14px; }
.action_btn, .search_btn_view :deep(.el-button) { border-radius: 999px !important; padding: 10px 22px !important; border: none !important; box-shadow: 0 10px 22px rgba(72, 114, 146, 0.12); font-weight: 600; }

.beauty_table {
	border-radius: 22px;
	overflow: hidden;
	border: 1px solid #e5eef5;
	box-shadow: 0 14px 34px rgba(63, 92, 117, 0.08);
	:deep(th.el-table__cell) { background: linear-gradient(180deg, #f4fbff 0%, #eaf5fb 100%) !important; color: #375063 !important; font-weight: 700; }
	:deep(.el-table__row td.el-table__cell) { padding-top: 14px; padding-bottom: 14px; }
	:deep(.el-button), :deep(.el-tag) { border-radius: 999px !important; }
}
</style>
