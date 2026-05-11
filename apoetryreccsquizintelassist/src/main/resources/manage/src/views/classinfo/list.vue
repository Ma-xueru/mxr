<template>
	<div>
		<div class="app-contain">
			<div class="class_hero">
				<div>
					<div class="hero_badge">班级管理</div>
					<div class="hero_title">管理员统一维护年级班级信息</div>
					<div class="hero_desc">班级名称、班主任、人数和说明都可以在这里统一维护，后续老师布置任务和学生归班会更清晰。</div>
				</div>
				<div class="hero_stat">
					<div class="stat_label">当前班级数</div>
					<div class="stat_value">{{ total }}</div>
				</div>
			</div>
			<div class="list_search_view">
				<el-form :model="searchQuery" class="search_form">
					<div class="search_view">
						<div class="search_label">年级：</div>
						<div class="search_box">
							<el-select class="search_inp" v-model="searchQuery.grade" placeholder="请选择年级" clearable>
								<el-option v-for="item in gradeLists" :key="item" :label="item" :value="item"></el-option>
							</el-select>
						</div>
					</div>
					<div class="search_view">
						<div class="search_label">班级名称：</div>
						<div class="search_box">
							<el-input class="search_inp" v-model="searchQuery.classname" placeholder="请输入班级名称" clearable></el-input>
						</div>
					</div>
					<div class="search_btn_view">
						<el-button class="search_btn" type="primary" @click="searchClick()" size="small">搜索</el-button>
					</div>
				</el-form>
				<br>
				<div class="btn_view">
					<el-button type="success" @click="addClick" v-if="btnAuth('classinfo','新增')">新增班级</el-button>
					<el-button type="info" v-if="btnAuth('classinfo','查看')" :disabled="selRows.length==1?false:true" @click="infoClick(null)">详情</el-button>
					<el-button type="primary" v-if="btnAuth('classinfo','修改')" :disabled="selRows.length==1?false:true" @click="editClick">修改</el-button>
					<el-button type="danger" v-if="btnAuth('classinfo','删除')" :disabled="selRows.length?false:true" @click="delClick(null)">删除</el-button>
				</div>
			</div>
			<br>
			<el-table v-loading="listLoading" border :stripe='false' @selection-change="handleSelectionChange" ref="table" v-if="btnAuth('classinfo','查看')" :data="list" @row-click="listChange">
				<el-table-column :resizable='true' align="left" header-align="left" type="selection" width="55" />
				<el-table-column label="序号" width="70" :resizable='true' :sortable='true' align="left" header-align="left"><template #default="scope">{{ scope.$index + 1}}</template></el-table-column>
				<el-table-column prop="grade" label="年级" width="120" :resizable='true' :sortable='true' align="left" header-align="left"></el-table-column>
				<el-table-column prop="classname" label="班级名称" min-width="160" :resizable='true' :sortable='true' align="left" header-align="left"></el-table-column>
				<el-table-column prop="headteacher" label="班主任" width="140" :resizable='true' align="left" header-align="left"></el-table-column>
				<el-table-column prop="studentcount" label="学生人数" width="100" :resizable='true' :sortable='true' align="left" header-align="left"></el-table-column>
				<el-table-column prop="classdesc" label="班级说明" min-width="220" :resizable='true' align="left" header-align="left"></el-table-column>
				<el-table-column label="操作" width="180" :resizable='true' align="left" header-align="left">
					<template #default="scope">
						<el-button type="info" v-if="btnAuth('classinfo','查看')" @click="infoClick(scope.row.id)">详情</el-button>
					</template>
				</el-table-column>
			</el-table>
			<el-pagination background :layout="layouts.join(',')" :total="total" :page-size="listQuery.limit" prev-text="上一页" next-text="下一页" :hide-on-single-page="false" :style='{"padding":"0","margin":"20px auto","whiteSpace":"nowrap","color":"#333","alignItems":"center","textAlign":"center","display":"flex","width":"100%","fontWeight":"500","justifyContent":"center"}' @size-change="sizeChange" @current-change="currentChange" @prev-click="prevClick" @next-click="nextClick" />
		</div>
		<formModel ref="formRef" @formModelChange="formModelChange"></formModel>
	</div>
</template>
<script setup>
import { ref, getCurrentInstance, nextTick } from 'vue'
import { ElMessageBox } from 'element-plus'
import formModel from './formModel.vue'
const context = getCurrentInstance()?.appContext.config.globalProperties
const tableName = 'classinfo'
const formName = '班级'
const list = ref(null)
const table = ref(null)
const listQuery = ref({ page: 1, limit: 20, sort: 'id', order: 'desc' })
const searchQuery = ref({})
const gradeLists = ref("一年级,二年级,三年级,四年级,五年级,六年级".split(','))
const selRows = ref([])
const listLoading = ref(false)
const total = ref(0)
const layouts = ref(["total","prev","pager","next","sizes"])
const formRef = ref(null)
const listChange = (row)=>{ nextTick(()=>{ table.value.clearSelection(); table.value.toggleRowSelection(row) }) }
const btnAuth = (e,a)=> context?.$toolUtil.isAuth(e,a)
const loadStudentCounts = () => {
	return context?.$http({
		url: 'student/page',
		method: 'get',
		params: { page: 1, limit: 1000, sort: 'id', order: 'asc' }
	}).then(res => {
		const countMap = {}
		;(res.data.data.list || []).forEach(item => {
			if(item.classname){
				countMap[item.classname] = (countMap[item.classname] || 0) + 1
			}
		})
		return countMap
	}).catch(() => ({}))
}
const getList = () => {
	listLoading.value = true
	let params = JSON.parse(JSON.stringify(listQuery.value))
	params.sort = 'id'; params.order = 'desc'
	if(searchQuery.value.grade) params.grade = searchQuery.value.grade
	if(searchQuery.value.classname) params.classname = '%' + searchQuery.value.classname + '%'
	Promise.all([
		context?.$http({ url: `${tableName}/page`, method: 'get', params }),
		loadStudentCounts()
	]).then(([res, countMap]) => {
		listLoading.value = false
		list.value = (res.data.data.list || []).map(item => ({
			...item,
			studentcount: countMap[item.classname] || 0
		}))
		total.value = Number(res.data.data.total)
	}).catch(() => {
		listLoading.value = false
	})
}
const searchClick = () => { listQuery.value.page = 1; getList() }
const formModelChange = ()=> searchClick()
const addClick = ()=> formRef.value.init()
const editClick = ()=> { if(selRows.value.length) formRef.value.init(selRows.value[0].id,'edit') }
const infoClick = (id=null)=> { if(id) formRef.value.init(id,'info'); else if(selRows.value.length) formRef.value.init(selRows.value[0].id,'info') }
const delClick = (id) => {
	let ids = ref([])
	if (id) ids.value = [id]
	else { if (selRows.value.length) { for (let x in selRows.value) ids.value.push(selRows.value[x].id) } else return false }
	ElMessageBox.confirm(`是否删除选中${formName}`, '提示', { confirmButtonText: '是', cancelButtonText: '否', type: 'warning' }).then(() => {
		context?.$http({ url: `${tableName}/delete`, method: 'post', data: ids.value }).then(() => { context?.$toolUtil.message('删除成功', 'success',()=>{ getList() }) })
	})
}
const handleSelectionChange = (e) => { selRows.value = e }
const sizeChange = (size) => { listQuery.value.limit = size; getList() }
const currentChange = (page) => { listQuery.value.page = page; getList() }
const prevClick = () => { listQuery.value.page = listQuery.value.page - 1; getList() }
const nextClick = () => { listQuery.value.page = listQuery.value.page + 1; getList() }
getList()
</script>
<style lang="scss" scoped>
.class_hero {
	display:flex;
	justify-content:space-between;
	align-items:flex-start;
	gap:24px;
	padding:24px 26px;
	border-radius:24px;
	background: linear-gradient(135deg, #fffaf1 0%, #ffffff 100%);
	box-shadow: 0 16px 38px rgba(129, 111, 72, 0.08);
	margin-bottom: 22px;
}
.hero_badge {
	display:inline-flex;
	padding:6px 14px;
	border-radius:999px;
	background:#fff;
	color:#8d6c2f;
	font-size:12px;
	font-weight:700;
}
.hero_title {
	margin-top:12px;
	font-size:26px;
	font-weight:700;
	color:#2f2a1f;
}
.hero_desc {
	margin-top:8px;
	font-size:14px;
	line-height:1.8;
	color:#7b6a4b;
}
.hero_stat {
	min-width: 180px;
	padding: 18px 20px;
	border-radius: 20px;
	background: linear-gradient(135deg, #f2f7ff 0%, #ffffff 100%);
}
.stat_label { color:#73829a; font-size:14px; }
.stat_value { margin-top:10px; color:#2f3640; font-size:34px; font-weight:700; }
.list_search_view { margin:20px auto; display:flex; width:100%; flex-wrap:wrap; }
.search_form { display:flex; align-items:center; order:2; }
.search_view { margin:0 10px 0 0; display:flex; align-items:center; }
.search_label { margin:0 10px 0 0; color:#666; font-weight:500; min-width:100px; line-height:40px; }
.search_box { display:inline-block; width:auto; }
:deep(.search_inp) { border:1px solid #999; border-radius:0; padding:0 10px; background:#fff; width:auto; line-height:34px; box-sizing:border-box; }
.search_btn_view { width:20%; display:flex; padding:0 20px; }
.search_btn { border:0; border-radius:0; padding:0 24px; color:#fff; background:linear-gradient(270deg, rgba(130,196,209,1) 0%, rgba(115,186,200,1) 24%, rgba(174,210,217,1) 100%); height:36px; }
</style>
