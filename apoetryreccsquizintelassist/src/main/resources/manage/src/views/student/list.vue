
<template>
	<div>
		<div class="app-contain">
			<div class="overview_panel" v-if="btnAuth('student','查看')">
				<div class="overview_header">
					<div>
						<div class="overview_badge">学生管理</div>
						<div class="overview_title">学生账号与学习身份统一维护</div>
						<div class="overview_desc">集中查看年级、班级、勋章和权限状态，让班级分配与学生管理更清晰。</div>
					</div>
					<div class="overview_stat">
						<div class="overview_stat_label">当前学生数</div>
						<div class="overview_stat_value">{{ total }}</div>
					</div>
				</div>
				<div class="overview_cards">
					<div class="overview_card">
						<div class="overview_value">{{ total }}</div>
						<div class="overview_label">学生总数</div>
					</div>
					<div class="overview_card warm">
						<div class="overview_value">{{ studentStats.enabled }}</div>
						<div class="overview_label">启用账号</div>
					</div>
					<div class="overview_card green">
						<div class="overview_value">{{ studentStats.classCount }}</div>
						<div class="overview_label">涉及班级</div>
					</div>
					<div class="overview_card blue">
						<div class="overview_value">{{ studentStats.medalTotal }}</div>
						<div class="overview_label">勋章总数</div>
					</div>
				</div>
			</div>
			<div class="list_search_view">
				<el-form :model="searchQuery" class="search_form" >
					<div class="search_view">
						<div class="search_label">
							所在年级：
						</div>
						<div class="search_box">
							<el-select class="search_inp" v-model="searchQuery.grade" placeholder="所在年级" clearable>
								<el-option v-for="item in gradeLists" :key="item" :label="item" :value="item"></el-option>
							</el-select>
						</div>
					</div>
					<div class="search_view">
						<div class="search_label">
							所在班级：
						</div>
						<div class="search_box">
							<el-input class="search_inp" v-model="searchQuery.classname" placeholder="所在班级"
								clearable>
							</el-input>
						</div>
					</div>
					<div class="search_view">
						<div class="search_label">
							用户账号：
						</div>
						<div class="search_box">
							<el-input class="search_inp" v-model="searchQuery.studentaccount" placeholder="用户账号"
								clearable>
							</el-input>
						</div>
					</div>
					<div class="search_btn_view">
						<el-button class="search_btn" type="primary" @click="searchClick()" size="small">搜索</el-button>
					</div>
				</el-form>
				<div class="btn_view">
					<el-button class="action_btn create_btn" type="success" @click="addClick" v-if="btnAuth('student','新增')">新增</el-button>
					<el-button class="action_btn" v-if=" btnAuth('student','查看')" type="info"  :disabled="selRows.length==1?false:true" @click="infoClick(null)">详情</el-button>
					<el-button class="action_btn" type="primary" :disabled="selRows.length==1?false:true" @click="editClick" v-if=" btnAuth('student','修改')">修改</el-button>
					<el-button class="action_btn" type="danger" :disabled="selRows.length?false:true" @click="delClick(null)"  v-if="btnAuth('student','删除')">删除</el-button>
					<el-button class="action_btn stat_btn" type="warning" @click="echartClick1" v-if="btnAuth('student','用户人数统计')">用户人数统计</el-button>
				</div>
			</div>
			<el-table
				class="beauty_table"
				v-loading="listLoading"
				border 
				:stripe='false'
				@selection-change="handleSelectionChange" 
				ref="table"
				v-if="btnAuth('student','查看')"
				:data="list"
				@row-click="listChange">
				<el-table-column :resizable='true' align="left" header-align="left" type="selection" width="55" />
				<el-table-column label="序号" width="70" :resizable='true' :sortable='true' align="left" header-align="left">
					<template #default="scope">{{ scope.$index + 1}}</template>
				</el-table-column>
				<el-table-column
					 :resizable='true' 
					 :sortable='true' 
					 align="left" 
					 header-align="left"
					 prop="studentaccount"
					label="用户账号">
					<template #default="scope">
						{{scope.row.studentaccount}}
					</template>
				</el-table-column>
				<el-table-column
					 :resizable='true' 
					 :sortable='true' 
					 align="left" 
					 header-align="left"
					 prop="studentname"
					label="用户姓名">
					<template #default="scope">
						{{scope.row.studentname}}
					</template>
				</el-table-column>
				<el-table-column
					 :resizable='true' 
					 :sortable='true' 
					 align="left" 
					 header-align="left"
					 prop="grade"
					label="所在年级">
					<template #default="scope">
						{{scope.row.grade}}
					</template>
				</el-table-column>
				<el-table-column
					 :resizable='true' 
					 :sortable='true' 
					 align="left" 
					 header-align="left"
					 prop="classname"
					label="所在班级">
					<template #default="scope">
						{{scope.row.classname}}
					</template>
				</el-table-column>
				<el-table-column
					 :resizable='true' 
					 :sortable='true' 
					 align="left" 
					 header-align="left"
					 prop="medalcount"
					label="勋章数量">
					<template #default="scope">
						<span class="count_badge">{{scope.row.medalcount || 0}}</span>
					</template>
				</el-table-column>
				<el-table-column
					 :resizable='true'
					 align="left"
					 header-align="left"
					 prop="permissionstatus"
					label="权限状态">
					<template #default="scope">
						<span :style="{ color: scope.row.permissionstatus === '禁用' ? '#f56c6c' : '#19a97b', fontWeight: '700' }">
							{{scope.row.permissionstatus || '启用'}}
						</span>
					</template>
				</el-table-column>
				<el-table-column label="头像" width="120" :resizable='true' :sortable='true' align="left" header-align="left">
					<template #default="scope">
						<div v-if="scope.row.avatar">
							<el-image v-if="scope.row.avatar.substring(0,4)=='http'" preview-teleported
								:preview-src-list="[scope.row.avatar.split(',')[0]]"
								:src="scope.row.avatar.split(',')[0]" style="width:100px;height:100px"></el-image>
							<el-image v-else preview-teleported
								:preview-src-list="[$config.url+scope.row.avatar.split(',')[0]]"
								:src="$config.url+scope.row.avatar.split(',')[0]" style="width:100px;height:100px">
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
					 prop="gender"
					label="性别">
					<template #default="scope">
						{{scope.row.gender}}
					</template>
				</el-table-column>
				<el-table-column
					 :resizable='true' 
					 :sortable='true' 
					 align="left" 
					 header-align="left"
					 prop="telephone"
					label="手机号码">
					<template #default="scope">
						{{scope.row.telephone}}
					</template>
				</el-table-column>
				<el-table-column label="操作" width="300" :resizable='true' :sortable='true' align="left" header-align="left">
					<template #default="scope">
						<el-button type="info" v-if=" btnAuth('student','查看')" @click="infoClick(scope.row.id)">详情</el-button>
						<el-button type="warning" v-if="context?.$toolUtil.storageGet('role')=='管理员'" @click="togglePermission(scope.row)">
							{{ scope.row.permissionstatus === '禁用' ? '启用权限' : '禁用权限' }}
						</el-button>
						<el-button v-if="btnAuth('student','分配')" type="success" @click="mystudentCrossAddOrUpdateHandler(scope.row,'cross','','','')">分配</el-button>
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
		<!-- 统计图弹窗 -->
		<el-dialog v-model="echartVisible" :title="'用户人数统计'" width="70%">
			<div  id="studentnameEchart1" style="width:100%;height:600px;"></div>

			<template #footer>
				<span class="formModel_btn_box">
					<el-button class="formModel_cancel" @click="echartVisible=false">取消</el-button>
				</span>
			</template>
		</el-dialog>
		<mystudentFormModel ref="mystudentFormModelRef" @formModelChange="formModelChange"></mystudentFormModel>
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
		inject
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
	const tableName = 'student'
	const formName = '用户'
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
	const gradeLists = ref("一年级,二年级,三年级,四年级,五年级,六年级".split(','))
	const selRows = ref([])
	const listLoading = ref(false)
	const studentStats = ref({
		enabled: 0,
		classCount: 0,
		medalTotal: 0
	})
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
		if(searchQuery.value.studentaccount&&searchQuery.value.studentaccount!=''){
			params['studentaccount'] = '%' + searchQuery.value.studentaccount + '%'
		}
		if(searchQuery.value.grade&&searchQuery.value.grade!=''){
			params['grade'] = searchQuery.value.grade
		}
		if(searchQuery.value.classname&&searchQuery.value.classname!=''){
			params['classname'] = '%' + searchQuery.value.classname + '%'
		}
		context?.$http({
			url: `${tableName}/page`,
			method: 'get',
			params: params
		}).then(res => {
			listLoading.value = false
			list.value = res.data.data.list
			total.value = Number(res.data.data.total)
			buildStudentStats(list.value || [])
		})
	}
	const buildStudentStats = (rows=[]) => {
		const classSet = new Set()
		let enabled = 0
		let medalTotal = 0
		rows.forEach(item => {
			if ((item.permissionstatus || '启用') !== '禁用') enabled += 1
			if (item.classname) classSet.add(item.classname)
			medalTotal += Number(item.medalcount || 0)
		})
		studentStats.value = {
			enabled,
			classCount: classSet.size,
			medalTotal
		}
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
	const togglePermission = (row) => {
		const nextStatus = row.permissionstatus === '禁用' ? '启用' : '禁用'
		ElMessageBox.confirm(`确定将该学生权限设置为“${nextStatus}”吗？`, '权限控制', {
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'warning',
		}).then(() => {
			context?.$http({
				url: `${tableName}/update`,
				method: 'post',
				data: {
					...row,
					permissionstatus: nextStatus
				}
			}).then(() => {
				context?.$toolUtil.message(`学生权限已${nextStatus === '启用' ? '启用' : '禁用'}`, 'success',()=>{
					getList()
				})
			})
		})
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
	// 统计图1
	const echarts = inject("echarts")
	const echartVisible = ref(false)
	const echartClick1 = ()=>{
		echartVisible.value = true
		nextTick(()=>{
			var studentnameEchart1 = echarts.init(document.getElementById("studentnameEchart1"),'macarons');
			context?.$http({
				url: `student/group/studentname`,
				method: 'get'
			}).then(res=>{
				let obj = res.data.data
				let xAxis = [];
				let yAxis = [];
				let pArray = []
				for(let i=0;i<obj.length;i++){
				    xAxis.push(obj[i].studentname);
				    yAxis.push(parseFloat((obj[i].total)));
				    pArray.push({
				        value: parseFloat((obj[i].total)),
				        name: obj[i].studentname
				    })
				}
				var option = {};
                option = {
                    title: {
                        text: '用户人数统计',
                        left: 'center'
                    },
                    tooltip: {
                      trigger: 'item',
                      formatter: '{b} : {c}'
                    },
                    xAxis: {
                        type: 'category',
                        data: xAxis,
                        axisLabel : {
                            rotate:40
                        }
                    },
                    yAxis: {
                        type: 'value'
                    },
                    series: [{
                        data: yAxis,
                        type: 'bar'
                    }]
                };
				// 使用刚指定的配置项和数据显示图表。
				studentnameEchart1.setOption(option);
				  //根据窗口的大小变动图表
				window.onresize = function() {
				    studentnameEchart1.resize();
				};
			})
		})
	}


	import mystudentFormModel from '@/views/mystudent/formModel'
	const mystudentFormModelRef = ref(null)
    const mystudentCrossAddOrUpdateHandler = (row,type,crossOptAudit,statusColumnName,tips,statusColumnValue) => {
		if(statusColumnName!=''&&!statusColumnName.startsWith("[")) {
			var obj = row
			for (var o in obj){
				if(o==statusColumnName && obj[o]==statusColumnValue){
					context?.$toolUtil.message(tips,'error')
					return;
				}
			}
		}
		nextTick(()=>{
			mystudentFormModelRef.value.init(row.id,'cross','分配',row,'student',statusColumnName,tips,statusColumnValue)
		})
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
.overview_panel {
	margin: 0 0 24px;
	padding: 24px 26px;
	border-radius: 24px;
	background:
		radial-gradient(circle at top right, rgba(255, 205, 120, 0.18), transparent 26%),
		linear-gradient(180deg, #fffdf8 0%, #ffffff 100%);
	box-shadow: 0 20px 48px rgba(102, 89, 57, 0.08);
	border: 1px solid rgba(235, 221, 191, 0.9);
}
.overview_header { display:flex; justify-content:space-between; align-items:flex-start; gap:20px; margin-bottom:18px; }
.overview_badge { display:inline-flex; padding:6px 14px; border-radius:999px; background:#fff; color:#8d6c2f; font-size:12px; font-weight:700; }
.overview_title { margin-top:12px; color:#2f2a1f; font-size:26px; font-weight:700; }
.overview_desc { margin-top:8px; color:#7b6a4b; font-size:14px; line-height:1.8; }
.overview_stat { min-width:180px; padding:18px 20px; border-radius:20px; background:linear-gradient(135deg,#f2f7ff 0%,#ffffff 100%); }
.overview_stat_label { color:#73829a; font-size:14px; }
.overview_stat_value { margin-top:10px; color:#2f3640; font-size:34px; font-weight:700; }
.overview_cards { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:16px; }
.overview_card { padding:18px 20px; border-radius:20px; background:#fff; box-shadow: inset 0 1px 0 rgba(255,255,255,.9), 0 10px 24px rgba(132,115,74,0.08); border:1px solid rgba(236,227,211,.9); }
.overview_card.warm { background:linear-gradient(135deg,#fff7e7 0%,#ffffff 100%); }
.overview_card.green { background:linear-gradient(135deg,#eefaf0 0%,#ffffff 100%); }
.overview_card.blue { background:linear-gradient(135deg,#eef5ff 0%,#ffffff 100%); }
.overview_value { color:#2f2a1f; font-size:30px; font-weight:700; }
.overview_label { margin-top:6px; color:#86755b; font-size:14px; }
.list_search_view { display:flex !important; justify-content:space-between; align-items:center; gap:18px; padding:20px 22px; margin:0 0 18px; border-radius:22px; background:linear-gradient(180deg,#fffdfa 0%,#ffffff 100%); box-shadow:0 14px 34px rgba(121, 104, 67, 0.08); border:1px solid rgba(235, 221, 191, 0.9); flex-wrap:wrap; }
.search_form { display:flex; align-items:center; gap:14px; flex-wrap:wrap; flex:1; }
.search_view { display:flex; align-items:center; }
.search_label { color:#665942 !important; font-weight:700 !important; min-width:84px !important; }
:deep(.search_inp) { border:1px solid #e6dcc8 !important; border-radius:14px !important; padding:0 12px !important; min-width:220px; box-shadow:none !important; }
.search_btn { border-radius:999px !important; height:40px !important; box-shadow:0 12px 24px rgba(92, 168, 195, 0.22); }
.btn_view { display:flex; gap:12px; flex-wrap:wrap; }
.action_btn { border-radius:999px !important; min-width:100px; height:40px !important; font-weight:700; }
.beauty_table { border-radius:24px; overflow:hidden; box-shadow:0 18px 40px rgba(119, 102, 64, 0.08); }
:deep(.beauty_table th.el-table__cell) { background:#fbf7ef !important; color:#766445; font-weight:700; }
:deep(.beauty_table td.el-table__cell) { padding:18px 0; }
.count_badge { display:inline-flex; align-items:center; justify-content:center; min-width:40px; padding:6px 12px; border-radius:999px; background:#eef5ff; color:#4a86d9; font-weight:700; }
@media (max-width: 1200px) {
	.overview_cards { grid-template-columns:repeat(2,minmax(0,1fr)); }
}
</style>
