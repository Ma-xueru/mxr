<template>
	<div>
		<div class="app-contain">
			<div class="overview_panel" v-if="btnAuth('teacher','查看')">
				<div class="overview_header">
					<div>
						<div class="overview_badge">教师预约</div>
						<div class="overview_title">教师信息与预约处理</div>
						<div class="overview_desc">在教师预约页面查看教师资料，并处理学生提交的预约信息。</div>
					</div>
					<div class="overview_stat">
						<div class="overview_stat_label">当前教师数</div>
						<div class="overview_stat_value">{{ total }}</div>
					</div>
				</div>
			</div>

			<div class="list_search_view">
				<el-form :model="searchQuery" class="search_form">
					<div class="search_view">
						<div class="search_label">教师账号：</div>
						<div class="search_box">
							<el-input class="search_inp" v-model="searchQuery.teacheraccount" placeholder="教师账号" clearable />
						</div>
					</div>
					<div class="search_btn_view">
						<el-button class="search_btn" type="primary" @click="searchClick()" size="small">搜索</el-button>
					</div>
				</el-form>
				<div class="btn_view">
					<el-button class="action_btn create_btn" type="success" @click="addClick" v-if="btnAuth('teacher','新增')">新增</el-button>
					<el-button class="action_btn" v-if="btnAuth('teacher','查看')" type="info" :disabled="selRows.length!==1" @click="infoClick(null)">详情</el-button>
					<el-button class="action_btn" type="primary" :disabled="selRows.length!==1" @click="editClick" v-if="btnAuth('teacher','修改')">修改</el-button>
					<el-button class="action_btn" type="danger" :disabled="!selRows.length" @click="delClick(null)" v-if="btnAuth('teacher','删除')">删除</el-button>
				</div>
			</div>

			<el-table
				class="beauty_table"
				v-loading="listLoading"
				border
				@selection-change="handleSelectionChange"
				ref="table"
				v-if="btnAuth('teacher','查看')"
				:data="list"
				@row-click="listChange">
				<el-table-column align="left" header-align="left" type="selection" width="55" />
				<el-table-column label="序号" width="70" align="left" header-align="left">
					<template #default="scope">{{ scope.$index + 1 }}</template>
				</el-table-column>
				<el-table-column prop="teacheraccount" label="教师账号" align="left" header-align="left" />
				<el-table-column prop="teachername" label="教师姓名" align="left" header-align="left" />
				<el-table-column label="照片" width="120" align="left" header-align="left">
					<template #default="scope">
						<el-image
							v-if="scope.row.zhaopian"
							preview-teleported
							:preview-src-list="[imageUrl(scope.row.zhaopian)]"
							:src="imageUrl(scope.row.zhaopian)"
							style="width:72px;height:72px;border-radius:10px;object-fit:cover" />
						<span v-else>无图片</span>
					</template>
				</el-table-column>
				<el-table-column prop="gender" label="性别" align="left" header-align="left" />
				<el-table-column prop="lianxidianhua" label="联系电话" align="left" header-align="left" />
				<el-table-column prop="reservecount" label="可约人数" align="left" header-align="left" />
				<el-table-column prop="permissionstatus" label="权限状态" align="left" header-align="left">
					<template #default="scope">
						<el-tag :type="scope.row.permissionstatus === '禁用' ? 'danger' : 'success'">
							{{ scope.row.permissionstatus || '启用' }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column label="操作" width="340" align="left" header-align="left">
					<template #default="scope">
						<el-button type="info" v-if="btnAuth('teacher','查看')" @click.stop="infoClick(scope.row.id)">详情</el-button>
						<el-button type="primary" v-if="btnAuth('teacher','查看')" @click.stop="viewReserveClick(scope.row)">查看预约</el-button>
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
				:style='{"padding":"0","margin":"20px auto","whiteSpace":"nowrap","display":"flex","width":"100%","justifyContent":"center"}'
				@size-change="sizeChange"
				@current-change="currentChange"
				@prev-click="prevClick"
				@next-click="nextClick" />
		</div>

		<formModel ref="formRef" @formModelChange="formModelChange"></formModel>

		<el-dialog v-model="requestVisible" :title="`${currentTeacher.teachername || ''} 的预约信息`" width="900px" destroy-on-close>
			<el-table v-loading="requestLoading" :data="reserveRequests" border>
				<el-table-column prop="studentaccount" label="学生账号" min-width="110" />
				<el-table-column prop="studentname" label="学生姓名" min-width="110" />
				<el-table-column prop="reservetime" label="预约时间" min-width="160" />
				<el-table-column prop="reservecount" label="人数" width="80" />
				<el-table-column prop="reservestatus" label="预约状态" min-width="110">
					<template #default="scope">
						<el-tag :type="reserveTagType(scope.row.reservestatus)">{{ scope.row.reservestatus || '待确认' }}</el-tag>
					</template>
				</el-table-column>
				<el-table-column prop="sfsh" label="审核状态" min-width="100">
					<template #default="scope">{{ scope.row.sfsh || '待审核' }}</template>
				</el-table-column>
				<el-table-column prop="shhf" label="回复" min-width="150" />
				<el-table-column label="操作" width="180">
					<template #default="scope">
						<el-button type="success" size="small" v-if="canHandleReserve(scope.row)" @click="handleReserve(scope.row, 'accept')">接受</el-button>
						<el-button type="danger" size="small" v-if="canHandleReserve(scope.row)" @click="handleReserve(scope.row, 'reject')">拒绝</el-button>
					</template>
				</el-table-column>
			</el-table>
			<el-empty v-if="!requestLoading && reserveRequests.length === 0" description="暂无学生预约" />
		</el-dialog>
	</div>
</template>

<script setup>
import { ref, getCurrentInstance, nextTick } from 'vue'
import { ElMessageBox } from 'element-plus'
import formModel from './formModel.vue'

const context = getCurrentInstance()?.appContext.config.globalProperties
const tableName = 'teacher'
const formName = '教师'

const list = ref([])
const table = ref(null)
const listQuery = ref({ page: 1, limit: 20, sort: 'id', order: 'desc' })
const searchQuery = ref({})
const selRows = ref([])
const listLoading = ref(false)
const total = ref(0)
const layouts = ref(['total', 'prev', 'pager', 'next', 'sizes'])

const requestVisible = ref(false)
const requestLoading = ref(false)
const reserveRequests = ref([])
const currentTeacher = ref({})

const btnAuth = (e, a) => context?.$toolUtil.isAuth(e, a)
const imageUrl = (file) => file && file.substring(0, 4) === 'http' ? file.split(',')[0] : context?.$config.url + file.split(',')[0]
const reserveTagType = (status) => status === '已预约' ? 'success' : status === '已拒绝' || status === '已取消' ? 'danger' : 'warning'
const canHandleReserve = (row) => {
	const status = row.reservestatus || ''
	if (status === '已预约' || status === '已拒绝') return false
	return true
}

const getList = () => {
	listLoading.value = true
	const params = { ...listQuery.value, sort: 'id', order: 'desc' }
	if (searchQuery.value.teacheraccount) params.teacheraccount = `%${searchQuery.value.teacheraccount}%`
	context?.$http({ url: `${tableName}/page`, method: 'get', params }).then(res => {
		list.value = res.data.data.list || []
		total.value = Number(res.data.data.total || 0)
	}).finally(() => {
		listLoading.value = false
	})
}

const listChange = (row) => {
	nextTick(() => {
		table.value?.clearSelection()
		table.value?.toggleRowSelection(row)
	})
}
const handleSelectionChange = (e) => { selRows.value = e }
const sizeChange = (size) => { listQuery.value.limit = size; getList() }
const currentChange = (page) => { listQuery.value.page = page; getList() }
const prevClick = () => { listQuery.value.page -= 1; getList() }
const nextClick = () => { listQuery.value.page += 1; getList() }
const searchClick = () => { listQuery.value.page = 1; getList() }

const formRef = ref(null)
const formModelChange = () => searchClick()
const addClick = () => formRef.value.init()
const editClick = () => { if (selRows.value.length) formRef.value.init(selRows.value[0].id, 'edit') }
const infoClick = (id = null) => {
	if (id) formRef.value.init(id, 'info')
	else if (selRows.value.length) formRef.value.init(selRows.value[0].id, 'info')
}
const delClick = (id) => {
	const ids = id ? [id] : selRows.value.map(item => item.id)
	if (!ids.length) return
	ElMessageBox.confirm(`是否删除选中的${formName}`, '提示', {
		confirmButtonText: '是',
		cancelButtonText: '否',
		type: 'warning'
	}).then(() => {
		context?.$http({ url: `${tableName}/delete`, method: 'post', data: ids }).then(() => {
			context?.$toolUtil.message('删除成功', 'success', () => getList())
		})
	})
}

const viewReserveClick = (row) => {
	currentTeacher.value = row
	requestVisible.value = true
	loadReserveRequests()
}

const loadReserveRequests = () => {
	requestLoading.value = true
	context?.$http({
		url: 'coursereserve/page',
		method: 'get',
		params: {
			page: 1,
			limit: 100,
			sort: 'id',
			order: 'desc',
			teacheraccount: currentTeacher.value.teacheraccount
		}
	}).then(res => {
		reserveRequests.value = res.data.data.list || []
	}).finally(() => {
		requestLoading.value = false
	})
}

const handleReserve = (row, action) => {
	const accepted = action === 'accept'
	context?.$http({
		url: 'coursereserve/update',
		method: 'post',
		data: {
			...row,
			reservestatus: accepted ? '已预约' : '已拒绝',
			sfsh: accepted ? '是' : '否',
			shhf: accepted ? '教师已接受预约' : '教师已拒绝预约'
		}
	}).then(() => {
		context?.$toolUtil.message(accepted ? '已接受预约' : '已拒绝预约', 'success', () => {
			loadReserveRequests()
		})
	})
}

getList()
</script>

<style lang="scss" scoped>
.overview_panel {
	margin: 0 0 24px;
	padding: 24px 26px;
	border-radius: 18px;
	background:
		radial-gradient(circle at top right, rgba(179, 80, 59, 0.08), transparent 30%),
		linear-gradient(180deg, #fffdf4 0%, #fbf7e9 100%);
	box-shadow: 0 18px 40px rgba(86, 72, 45, 0.1);
	border: 1px solid rgba(190, 165, 111, 0.36);
}
.overview_header { display:flex; justify-content:space-between; align-items:flex-start; gap:20px; }
.overview_badge { display:inline-flex; padding:6px 14px; border-radius:999px; background:#f7ead1; color:#9b533f; border:1px solid rgba(155,83,63,.12); font-size:12px; font-weight:700; }
.overview_title { margin-top:12px; color:#3d3221; font-size:28px; font-weight:700; font-family:"STKaiti","KaiTi","Microsoft YaHei",sans-serif; }
.overview_desc { margin-top:8px; color:#715f3e; font-size:14px; line-height:1.8; }
.overview_stat { min-width:180px; padding:18px 20px; border-radius:16px; background:linear-gradient(135deg,#eef6e8 0%,#fffdf5 100%); border:1px solid rgba(139,166,113,.18); }
.overview_stat_label { color:#6f765b; font-size:14px; }
.overview_stat_value { margin-top:10px; color:#9b533f; font-size:34px; font-weight:800; }
.list_search_view { display:flex; justify-content:space-between; align-items:center; gap:18px; padding:20px 22px; margin:0 0 18px; border-radius:18px; background:linear-gradient(180deg,#fffdf6 0%,#fbf8ee 100%); box-shadow:0 14px 32px rgba(103, 86, 54, 0.08); border:1px solid rgba(190, 165, 111, 0.28); flex-wrap:wrap; }
.search_form { display:flex; align-items:center; gap:14px; flex-wrap:wrap; flex:1; }
.search_view { display:flex; align-items:center; }
.search_label { color:#66583b; font-weight:700; min-width:84px; }
:deep(.search_inp) { border:1px solid #dfcfaa; border-radius:12px; padding:0 12px; min-width:220px; box-shadow:none; background:#fffefa; }
.search_btn, .action_btn { border-radius:999px !important; min-width:90px; height:40px !important; font-weight:700; }
.btn_view { display:flex; gap:12px; flex-wrap:wrap; }
.beauty_table { border-radius:18px; overflow:hidden; box-shadow:0 18px 36px rgba(102, 83, 50, 0.1); border:1px solid rgba(190, 165, 111, 0.26); }
:deep(.beauty_table th.el-table__cell) { background:#f5edd9 !important; color:#6b5634; font-weight:700; }
:deep(.beauty_table td.el-table__cell) { padding:14px 0; }
:deep(.el-dialog) { border-radius:18px; background:#fffdf5; }
:deep(.el-dialog__title) { color:#3d3221; font-family:"STKaiti","KaiTi","Microsoft YaHei",sans-serif; font-size:22px; font-weight:700; }
:deep(.el-button--primary) { background:#4f7e5d; border-color:#4f7e5d; }
:deep(.el-button--success) { background:#719b63; border-color:#719b63; }
:deep(.el-button--danger) { background:#b65a46; border-color:#b65a46; }
</style>
