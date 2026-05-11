
<template>
	<div>
		<div class="app-contain">
			<div class="overview_panel" v-if="btnAuth('coursereserve','鏌ョ湅')">
				<div class="overview_header">
					<div>
						<div class="overview_badge">预约请求</div>
						<div class="overview_title">预约请求总览</div>
						<div class="overview_desc">汇总预约请求数量、已接受数量和已取消数量，帮助教师处理学生预约。</div>
					</div>
					<div class="overview_stat">
						<div class="overview_stat_label">当前预约数</div>
						<div class="overview_stat_value">{{ total }}</div>
					</div>
				</div>
				<div class="overview_cards">
					<div class="overview_card">
						<div class="overview_value">{{ total }}</div>
						<div class="overview_label">棰勭害鎬绘暟</div>
					</div>
					<div class="overview_card green">
						<div class="overview_value">{{ reserveStats.approved }}</div>
						<div class="overview_label">瀹℃牳閫氳繃</div>
					</div>
					<div class="overview_card warm">
						<div class="overview_value">{{ reserveStats.pending }}</div>
						<div class="overview_label">待确认</div>
					</div>
					<div class="overview_card blue">
						<div class="overview_value">{{ reserveStats.cancelled }}</div>
						<div class="overview_label">已取消</div>
					</div>
				</div>
			</div>
			<div class="list_search_view">
				<el-form :model="searchQuery" class="search_form" >
					<div class="search_view">
						<div class="search_label">
							鐢ㄦ埛濮撳悕锛?
						</div>
						<div class="search_box">
							<el-input class="search_inp" v-model="searchQuery.studentname" placeholder="鐢ㄦ埛濮撳悕"
								clearable>
							</el-input>
						</div>
					</div>
					<div class="search_view">
						<div class="search_label">
							瀹℃牳鐘舵€侊細
						</div>
						<div class="search_box">
							<el-select
								class="search_sel"
								clearable
								v-model="searchQuery.sfsh" 
								placeholder="审核状态">
								<el-option v-for="item in approvalLists" :label="item" :value="item"></el-option>
							</el-select>
						</div>
					</div>
					<div class="search_btn_view">
						<el-button class="search_btn" type="primary" @click="searchClick()" size="small">鎼滅储</el-button>
					</div>
				</el-form>
				<br>
				<div class="btn_view">
					<el-button class="action_btn create_btn" type="success" @click="addClick" v-if="btnAuth('coursereserve','鏂板')">鏂板</el-button>
					<el-button class="action_btn" v-if=" btnAuth('coursereserve','鏌ョ湅')" type="info"  :disabled="selRows.length==1?false:true" @click="infoClick(null)">璇︽儏</el-button>
					<el-button class="action_btn" type="primary" :disabled="selRows.length==1?false:true" @click="editClick" v-if=" btnAuth('coursereserve','淇敼')">淇敼</el-button>
					<el-button class="action_btn" type="danger" :disabled="selRows.length?false:true" @click="delClick(null)"  v-if="btnAuth('coursereserve','鍒犻櫎')">鍒犻櫎</el-button>
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
				v-if="btnAuth('coursereserve','鏌ョ湅')"
				:data="list"
				@row-click="listChange">
				<el-table-column :resizable='true' align="left" header-align="left" type="selection" width="55" />
				<el-table-column label="搴忓彿" width="70" :resizable='true' :sortable='true' align="left" header-align="left">
					<template #default="scope">{{ scope.$index + 1}}</template>
				</el-table-column>
				<el-table-column
					 :resizable='true' 
					 :sortable='true' 
					 align="left" 
					 header-align="left"
					 prop="studentaccount"
					label="鐢ㄦ埛璐﹀彿">
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
					label="鐢ㄦ埛濮撳悕">
					<template #default="scope">
						{{scope.row.studentname}}
					</template>
				</el-table-column>
				<el-table-column
					 :resizable='true' 
					 :sortable='true' 
					 align="left" 
					 header-align="left"
					 prop="reservetime"
					label="棰勭害鏃堕棿">
					<template #default="scope">
						{{scope.row.reservetime}}
					</template>
				</el-table-column>
				<el-table-column
					 :resizable='true' 
					 :sortable='true' 
					 align="left" 
					 header-align="left"
					 prop="teacheraccount"
					label="鏁欏笀璐﹀彿">
					<template #default="scope">
						{{scope.row.teacheraccount}}
					</template>
				</el-table-column>
				<el-table-column
					 :resizable='true' 
					 :sortable='true' 
					 align="left" 
					 header-align="left"
					 prop="teachername"
					label="鏁欏笀濮撳悕">
					<template #default="scope">
						{{scope.row.teachername}}
					</template>
				</el-table-column>
				<el-table-column
					 :resizable='true' 
					 :sortable='true' 
					 align="left" 
					 header-align="left"
					 prop="reservestatus"
					label="预约状态">
					<template #default="scope">
						{{scope.row.reservestatus}}
					</template>
				</el-table-column>
				<el-table-column
					 :resizable='true' 
					 :sortable='true' 
					 align="left" 
					 header-align="left"
					 prop="reservecount"
					label="浜烘暟">
					<template #default="scope">
						{{scope.row.reservecount}}
					</template>
				</el-table-column>
				<el-table-column label="瀹℃牳鍥炲" :resizable='true' :sortable='true' align="left" header-align="left">
					<template #default="scope">
						{{scope.row.shhf}}
					</template>
				</el-table-column>
				<el-table-column prop="sfsh" label="审核状态" :resizable='true' :sortable='true' align="left" header-align="left">
					<template #default="scope">
						<el-tag type="success" v-if="scope.row.sfsh=='是'">通过</el-tag>
						<el-tag type="danger" v-else-if="scope.row.sfsh=='否'">未通过</el-tag>
						<el-tag type="warning" v-else>待审核</el-tag>
					</template>
				</el-table-column>
				<el-table-column label="瀹℃牳" v-if="btnAuth('coursereserve','瀹℃牳')" :resizable='true' :sortable='true' align="left" header-align="left">
					<template #default="scope">
						<el-button type="text" @click="approvalClick(scope.row)">瀹℃牳</el-button>
					</template>
				</el-table-column>
				<el-table-column label="鎿嶄綔" width="300" :resizable='true' :sortable='true' align="left" header-align="left">
					<template #default="scope">
						<el-button type="info" v-if=" btnAuth('coursereserve','鏌ョ湅')" @click="infoClick(scope.row.id)">璇︽儏</el-button>
                        <el-button type="primary" v-if="scope.row.reservestatus === '待确认'" @click="handleReserve(scope.row, 'accept')">接受预约</el-button>
                        <el-button type="danger" v-if="scope.row.reservestatus === '待确认'" @click="handleReserve(scope.row, 'reject')">拒绝预约</el-button>
						<el-button v-if="btnAuth('coursereserve','取消预约')" type="success" @click="reservecancelCrossAddOrUpdateHandler(scope.row,'cross','否','reservestatus','已取消','已取消,已预约'.split(',')[0])">取消预约</el-button>
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
		<Approval ref="approvalRef" :tableName="tableName" @shChange="searchClick()"></Approval>
		<reservecancelFormModel ref="reservecancelFormModelRef" @formModelChange="formModelChange"></reservecancelFormModel>
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
	
	//鍩虹淇℃伅
	const tableName = 'coursereserve'
	const formName = '棰勭害璇剧▼'
	const route = useRoute()
	//鍩虹淇℃伅
	onMounted(()=>{
	})
	//鍒楄〃鏁版嵁
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
	const reserveStats = ref({ approved: 0, pending: 0, cancelled: 0 })
	const listChange = (row) =>{
		nextTick(()=>{
			table.value.clearSelection()
			table.value.toggleRowSelection(row)
		})
	}
	//鍒楄〃
	const getList = () => {
		listLoading.value = true
		let params = JSON.parse(JSON.stringify(listQuery.value))
		params['sort'] = 'id'
		params['order'] = 'desc'
		if(searchQuery.value.studentname&&searchQuery.value.studentname!=''){
			params['studentname'] = '%' + searchQuery.value.studentname + '%'
		}
		if(searchQuery.value.sfsh && searchQuery.value.sfsh!=''){
			params['sfsh'] = searchQuery.value.sfsh
		}
		context?.$http({
			url: `${tableName}/page`,
			method: 'get',
			params: params
		}).then(res => {
			listLoading.value = false
			list.value = res.data.data.list
			total.value = Number(res.data.data.total)
			buildReserveStats(list.value || [])
		})
	}
	const buildReserveStats = (rows=[]) => {
		let approved = 0
		let pending = 0
		let cancelled = 0
		rows.forEach(item => {
			if (item.sfsh === '是') approved += 1
			else if (!item.sfsh || item.sfsh === '待审核' || item.reservestatus === '待确认') pending += 1
			if ((item.reservestatus || '').indexOf('取消') !== -1) cancelled += 1
		})
		reserveStats.value = { approved, pending, cancelled }
	}
	//鍒?
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
		ElMessageBox.confirm(`鏄惁鍒犻櫎閫変腑${formName}`, '鎻愮ず', {
			confirmButtonText: '是',
			cancelButtonText: '否',
			type: 'warning',
		}).then(() => {
			context?.$http({
				url: `${tableName}/delete`,
				method: 'post',
				data: ids.value
			}).then(res => {
				context?.$toolUtil.message('鍒犻櫎鎴愬姛', 'success',()=>{
					getList()
				})
			})
		})
	}
	//澶氶€?
	const handleSelectionChange = (e) => {
		selRows.value = e
	}
	//鍒楄〃鏁版嵁
	//鍒嗛〉
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
	//鍒嗛〉
	//鏉冮檺楠岃瘉
	const btnAuth = (e,a)=>{
		return context?.$toolUtil.isAuth(e,a)
	}
	//鎼滅储
	const searchClick = () => {
		listQuery.value.page = 1
		getList()
	}
	//琛ㄥ崟
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
	// 琛ㄥ崟
	// 棰勮鏂囦欢
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
	// 涓嬭浇鏂囦欢
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
			// 涓嬮潰杩欎釜鍐欐硶鍏煎鐏嫄
			a.dispatchEvent(new MouseEvent('click', {
				bubbles: true,
				cancelable: true,
				view: window
			}))
			window.URL.revokeObjectURL(data)
		})
	}

	//瀹℃牳
	import Approval from '@/components/common/approval.vue'
	const approvalRef = ref(null)
	const approvalClick = (row) => {
		let params = {
			id:row.id,
			studentaccount: row.studentaccount,
			studentname: row.studentname,
			reservetime: row.reservetime,
			teacheraccount: row.teacheraccount,
			teachername: row.teachername,
			reservestatus: row.reservestatus,
			sfsh: row.sfsh,
			shhf: row.shhf,
			reservecount: row.reservecount,
		}
		nextTick(() => {
			approvalRef.value.approvalClick(params )
		})
	}

	const handleReserve = (row, action) => {
		const accepted = action === 'accept'
		const payload = {
			...row,
			reservestatus: accepted ? '已预约' : '已拒绝',
			sfsh: accepted ? '是' : '否',
			shhf: accepted ? '教师已接受预约' : '教师已拒绝预约'
		}
		context?.$http({
			url: `${tableName}/update`,
			method: 'post',
			data: payload
		}).then(() => {
			context?.$toolUtil.message(accepted ? '已接受预约' : '已拒绝预约', 'success', () => {
				getList()
			})
		})
	}

	import reservecancelFormModel from '@/views/reservecancel/formModel'
	const reservecancelFormModelRef = ref(null)
    const reservecancelCrossAddOrUpdateHandler = (row,type,crossOptAudit,statusColumnName,tips,statusColumnValue) => {
		if(crossOptAudit=='是'&&row.sfsh!='是') {
			context?.$toolUtil.message('请审核通过后再操作','error')
			return
		}
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
			reservecancelFormModelRef.value.init(row.id,'cross','鍙栨秷棰勭害',row,'coursereserve',statusColumnName,tips,statusColumnValue)
		})
    }
	//鏌ヨ瀹℃牳鐘舵€佸垪琛?
	const approvalLists = ref([])
	//鍒濆鍖?
	const init = () => {
        approvalLists.value = "是,否,待审核".split(',');
		getList()
	}
	init()
</script>
<style lang="scss" scoped>
	
	// 鎿嶄綔鐩掑瓙
	.list_search_view {
		margin: 20px auto;
		display: flex;
		width: 100%;
		flex-wrap: wrap;
		// 鎼滅储鐩掑瓙
		.search_form {
			display: flex;
			align-items: center;
			order: 2;
			// 瀛愮洅瀛?
			.search_view {
				margin: 0 10px 0 0;
				display: flex;
				align-items: center;
				// 鎼滅储label
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
				// 鎼滅储item
				.search_box {
					display: inline-block;
					width: auto;
					// 杈撳叆妗?
					:deep(.search_inp) {
						border: 1px solid #999;
						border-radius: 0px;
						padding: 0 10px;
						background: #fff;
						width: auto;
						line-height: 34px;
						box-sizing: border-box;
						//鍘绘帀榛樿鏍峰紡
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
					// 涓嬫媺妗?
					:deep(.search_sel) {
						border: 1px solid #999;
						border-radius: 0px;
						padding: 0 10px;
						background: #fff;
						width: auto;
						line-height: 34px;
						box-sizing: border-box;
						//鍘绘帀榛樿鏍峰紡
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
			}
			// 鎼滅储鎸夐挳鐩掑瓙
			.search_btn_view {
				width: 20%;
				display: flex;
				padding: 0 20px;
				// 鎼滅储鎸夐挳
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
				// 鎼滅储鎸夐挳-鎮诞
				.search_btn:hover {
					background: linear-gradient(30deg, rgba(130,196,209,1) 0%, rgba(115,186,200,1) 24%, rgba(174,210,217,1) 100%);
				}
			}
		}
		//澶撮儴鎸夐挳鐩掑瓙
		.btn_view {
			margin: 0;
			display: flex;
			// 鍏朵粬
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
			// 鍏朵粬-鎮诞
			:deep(.el-button--default:hover){
				background: linear-gradient(30deg, rgba(51,51,51,1) 0%, rgba(102,102,102,1) 50%, rgba(51,51,51,1) 100%);
			}
			// 鏂板
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
			// 鏂板-鎮诞
			:deep(.el-button--success:hover){
				background: linear-gradient(30deg, rgba(246,154,40,1) 0%, rgba(255,186,101,1) 50%, rgba(246,154,40,1) 100%);
			}
			// 淇敼
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
			// 淇敼-鎮诞
			:deep(.el-button--primary:hover){
				background: linear-gradient(30deg, rgba(25,169,123,1) 0%, rgba(58,214,164,1) 50%, rgba(25,169,123,1) 100%),#19a97b;
			}
			// 璇︽儏
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
			// 璇︽儏-鎮诞
			:deep(.el-button--info:hover){
				background: linear-gradient(30deg, rgba(40,172,246,1) 0%, rgba(111,203,255,1) 50%, rgba(40,172,246,1) 100%);
			}
			// 鍒犻櫎
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
			// 鍒犻櫎-鎮诞
			:deep(.el-button--danger:hover){
				background: linear-gradient(30deg, rgba(246,40,40,1) 0%, rgba(255,107,107,1) 50%, rgba(246,40,40,1) 100%);
			}
			// 缁熻
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
			// 缁熻-鎮诞
			:deep(.el-button--warning:hover){
				background: linear-gradient(30deg, rgba(0,205,236,1) 0%, rgba(66,230,255,1) 50%, rgba(0,205,236,1) 100%);
			}
		}
	}
	// 琛ㄦ牸鏍峰紡
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
							// 缂栬緫
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
							// 缂栬緫-鎮诞
							.el-button--primary:hover {
							}
							// 璇︽儏
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
							// 璇︽儏-鎮诞
							.el-button--info:hover {
							}
							// 鍒犻櫎
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
							// 鍒犻櫎-鎮诞
							.el-button--danger:hover {
							}
							// 璺ㄨ〃
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
							// 璺ㄨ〃-鎮诞
							.el-button--success:hover {
							}
							// 鎿嶄綔
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
							// 鎿嶄綔-鎮诞
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
	// 鍒嗛〉鍣?
	.el-pagination {
		// 鎬婚〉鐮?
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
		// 涓婁竴椤?
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
		// 涓嬩竴椤?
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
		// 涓婁竴椤电鐢?
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
		// 涓嬩竴椤电鐢?
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
		// 椤电爜
		:deep(.el-pager) {
			padding: 0;
			margin: 0;
			display: flex;
			vertical-align: top;
			align-items: center;
			// 鏁板瓧
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
			// 鏁板瓧鎮诞
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
			// 閫変腑
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
		// 璺抽〉
		:deep(.el-pagination__jump) {
			margin: 0 0 0 24px;
			color: #606266;
			display: inline-block;
			vertical-align: top;
			font-size: 13px;
			line-height: 28px;
			height: 28px;
			// 杈撳叆妗?
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
				//鍘绘帀榛樿鏍峰紡
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
.overview_card.green { background: linear-gradient(135deg, #eefbf4 0%, #dcf5e7 100%); }
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
