<template>
	<div>
		<div class="app-contain">
			<div class="overview_panel" v-if="btnAuth('examrecord','查看')">
				<div class="overview_header">
					<div>
						<div class="overview_badge">答题记录</div>
						<div class="overview_title">学习成绩总览看板</div>
						<div class="overview_desc">查看学生各专题练习成绩</div>
					</div>
					<div class="overview_stat">
						<div class="overview_stat_label">当前记录数</div>
						<div class="overview_stat_value">{{ total }}</div>
					</div>
				</div>
				<div class="overview_cards">
					<div class="overview_card">
						<div class="overview_value">{{ total }}</div>
						<div class="overview_label">成绩记录</div>
					</div>
					<div class="overview_card warm">
						<div class="overview_value">{{ scoreStats.average }}</div>
						<div class="overview_label">平均得分</div>
					</div>
					<div class="overview_card green">
						<div class="overview_value">{{ scoreStats.excellent }}</div>
						<div class="overview_label">优秀记录</div>
					</div>
					<div class="overview_card blue">
						<div class="overview_value">{{ scoreStats.paperCount }}</div>
						<div class="overview_label">涉及专题</div>
					</div>
				</div>
			</div>
			<div class="list_search_view">
				<el-form :model="searchQuery" class="search_form">
					<div class="search_view">
						<div class="search_label">学生账号：</div>
						<div class="search_box">
							<el-input class="search_inp" v-model="searchQuery.studentaccount" placeholder="学生账号" clearable></el-input>
						</div>
					</div>
					<div class="search_view">
						<div class="search_label">学生姓名：</div>
						<div class="search_box">
							<el-input class="search_inp" v-model="searchQuery.studentname" placeholder="学生姓名" clearable></el-input>
						</div>
					</div>
					<div class="search_view">
						<div class="search_label">练习专题：</div>
						<div class="search_box">
							<el-input class="search_inp" v-model="searchQuery.papername" placeholder="练习专题" clearable></el-input>
						</div>
					</div>
					<div class="search_btn_view">
						<el-button class="search_btn" type="primary" @click="searchClick()" size="small">搜索</el-button>
					</div>
				</el-form>
				<div class="btn_view">
					<el-button class="action_btn stat_btn" type="warning" @click="echartClick1">得分统计</el-button>
				</div>
			</div>
			<el-table class="beauty_table" v-loading="listLoading" border :stripe="false" ref="table" v-if="btnAuth('examrecord','查看')" :data="list" @row-click="listChange" @selection-change="handleSelectionChange">
				<el-table-column :resizable="true" align="left" header-align="left" type="selection" width="55" />
				<el-table-column label="序号" width="70" :resizable="true" align="left" header-align="left">
					<template #default="scope">{{ scope.$index + 1 }}</template>
				</el-table-column>
				<el-table-column :resizable="true" align="left" header-align="left" prop="studentaccount" label="学生账号"></el-table-column>
				<el-table-column :resizable="true" align="left" header-align="left" prop="studentname" label="学生姓名"></el-table-column>
				<el-table-column :resizable="true" align="left" header-align="left" prop="papername" label="练习专题" min-width="180"></el-table-column>
				<el-table-column :resizable="true" align="left" header-align="left" label="题目数" width="90">
					<template #default="scope">{{ scope.row.questionCount }}</template>
				</el-table-column>
				<el-table-column :resizable="true" align="left" header-align="left" label="练习成绩" width="140">
					<template #default="scope">
						<span class="score_badge" :class="scoreClass(scope.row.myscore, scope.row.totalScore)">{{ scope.row.myscore }} / {{ scope.row.totalScore }}</span>
					</template>
				</el-table-column>
				<el-table-column :resizable="true" align="left" header-align="left" prop="releasetime" label="提交时间" min-width="160"></el-table-column>
				<el-table-column label="操作" width="180" :resizable="true" align="left" header-align="left">
					<template #default="scope">
						<el-button type="info" v-if="btnAuth('examrecord','查看')" @click="infoClick(scope.row)">查看答卷</el-button>
					</template>
				</el-table-column>
			</el-table>
			<el-pagination background :layout="layouts.join(',')" :total="total" :page-size="listQuery.limit" prev-text="上一页" next-text="下一页" :hide-on-single-page="false" :style='{"padding":"0","margin":"20px auto","whiteSpace":"nowrap","color":"#333","alignItems":"center","textAlign":"center","display":"flex","width":"100%","fontWeight":"500","justifyContent":"center"}' @size-change="sizeChange" @current-change="currentChange" @prev-click="prevClick" @next-click="nextClick" />
		</div>
		<formModel ref="formRef"></formModel>
		<el-dialog v-model="echartVisible" title="练习成绩统计" width="70%">
			<div id="examrecordScoreChart" style="width:100%;height:600px;"></div>
			<template #footer>
				<span class="formModel_btn_box">
					<el-button class="formModel_cancel" @click="echartVisible=false">取消</el-button>
				</span>
			</template>
		</el-dialog>
	</div>
</template>

<script setup>
import { ref, getCurrentInstance, nextTick, inject } from 'vue'
import formModel from '../exam/examrecord/formModel.vue'

const context = getCurrentInstance()?.appContext.config.globalProperties
const table = ref(null)
const formRef = ref(null)
const echarts = inject("echarts")
const echartVisible = ref(false)
const listLoading = ref(false)
const allRows = ref([])
const list = ref([])
const total = ref(0)
const listQuery = ref({ page: 1, limit: 20 })
const layouts = ref(["total","prev","pager","next","sizes"])
const searchQuery = ref({ studentaccount: '', studentname: '', papername: '' })
const selRows = ref([])
const studentMap = ref({})
const scoreStats = ref({
	average: 0,
	excellent: 0,
	paperCount: 0
})

const btnAuth = (e, a) => context?.$toolUtil.isAuth(e, a)
const listChange = (row) => nextTick(() => { table.value.clearSelection(); table.value.toggleRowSelection(row) })
const handleSelectionChange = (e) => { selRows.value = e }
const sizeChange = (size) => { listQuery.value.limit = size; applyFilters() }
const currentChange = (page) => { listQuery.value.page = page; applyFilters() }
const prevClick = () => { listQuery.value.page--; applyFilters() }
const nextClick = () => { listQuery.value.page++; applyFilters() }

const buildStudentMap = async () => {
	const pageSize = 1000
	let page = 1
	const map = {}
	while (true) {
		const res = await context?.$http({
			url: `student/page`,
			method: 'get',
			params: { page, limit: pageSize, sort: 'id', order: 'asc' }
		})
		const rows = res?.data?.data?.list || []
		rows.forEach(item => {
			map[item.id] = {
				studentaccount: item.studentaccount || '',
				studentname: item.studentname || ''
			}
		})
		if (rows.length < pageSize) {
			break
		}
		page += 1
	}
	studentMap.value = map
}

const normalizeRows = (rows = []) => rows.map(item => {
	const studentInfo = studentMap.value[item.userid] || {}
	const totalScore = Number(item.totalScore || item.score || 0)
	const myscore = Number(item.myscore || 0)
	return {
		...item,
		studentaccount: studentInfo.studentaccount || '',
		studentname: studentInfo.studentname || item.username || '',
		questionCount: Number(item.questionCount || 0),
		totalScore,
		myscore,
		releasetime: item.addtime || item.releasetime || ''
	}
})

const buildScoreStats = (rows = []) => {
	const paperSet = new Set(rows.map(item => item.papername).filter(Boolean))
	const avg = rows.length ? rows.reduce((sum, item) => sum + Number(item.myscore || 0), 0) / rows.length : 0
	scoreStats.value = {
		average: rows.length ? Math.round(avg * 10) / 10 : 0,
		excellent: rows.filter(item => Number(item.totalScore || 0) > 0 && Number(item.myscore || 0) / Number(item.totalScore || 1) >= 0.85).length,
		paperCount: paperSet.size
	}
}

const applyFilters = () => {
	const accountKeyword = (searchQuery.value.studentaccount || '').trim()
	const nameKeyword = (searchQuery.value.studentname || '').trim()
	const paperKeyword = (searchQuery.value.papername || '').trim()
	const filteredRows = allRows.value.filter(item => {
		if (accountKeyword && !String(item.studentaccount || '').includes(accountKeyword)) return false
		if (nameKeyword && !String(item.studentname || '').includes(nameKeyword)) return false
		if (paperKeyword && !String(item.papername || '').includes(paperKeyword)) return false
		return true
	})
	total.value = filteredRows.length
	buildScoreStats(filteredRows)
	const start = (listQuery.value.page - 1) * listQuery.value.limit
	const end = start + listQuery.value.limit
	list.value = filteredRows.slice(start, end)
}

const getList = async () => {
	listLoading.value = true
	try {
		await buildStudentMap()
		const params = {
			page: 1,
			limit: 100000,
			sort: 'id',
			order: 'desc'
		}
		const res = await context?.$http({
			url: `examrecord/groupby`,
			method: 'get',
			params
		})
		allRows.value = normalizeRows(res?.data?.data?.list || [])
		listQuery.value.page = 1
		applyFilters()
	} finally {
		listLoading.value = false
	}
}

const scoreClass = (score, totalScore) => {
	const ratio = Number(totalScore || 0) > 0 ? Number(score || 0) / Number(totalScore || 1) : 0
	if (ratio >= 0.85) return 'excellent'
	if (ratio >= 0.6) return 'good'
	return 'normal'
}

const searchClick = () => {
	listQuery.value.page = 1
	applyFilters()
}

const infoClick = (row) => {
	if (!row) return
	formRef.value.init(row)
}

const echartClick1 = () => {
	echartVisible.value = true
	nextTick(() => {
		const chart = echarts.init(document.getElementById("examrecordScoreChart"), 'macarons')
		const sourceRows = allRows.value.filter(item => {
			if (searchQuery.value.studentaccount && !String(item.studentaccount || '').includes(searchQuery.value.studentaccount)) return false
			if (searchQuery.value.studentname && !String(item.studentname || '').includes(searchQuery.value.studentname)) return false
			if (searchQuery.value.papername && !String(item.papername || '').includes(searchQuery.value.papername)) return false
			return true
		})
		const xAxis = sourceRows.map(item => `${item.studentname}-${item.papername}`)
		const yAxis = sourceRows.map(item => Number(item.myscore || 0))
		chart.setOption({
			title: { text: '练习成绩统计', left: 'center' },
			tooltip: { trigger: 'axis' },
			xAxis: { type: 'category', data: xAxis, axisLabel: { rotate: 35 } },
			yAxis: { type: 'value' },
			series: [{ data: yAxis, type: 'bar', itemStyle: { color: '#5ca8c3' } }]
		})
		window.onresize = function() { chart.resize() }
	})
}

getList()
</script>

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
.overview_header {
	display:flex;
	justify-content:space-between;
	align-items:flex-start;
	gap:20px;
	margin-bottom:18px;
}
.overview_badge {
	display:inline-flex;
	padding:6px 14px;
	border-radius:999px;
	background:#fff;
	color:#8d6c2f;
	font-size:12px;
	font-weight:700;
}
.overview_title { margin-top:12px; color:#2f2a1f; font-size:26px; font-weight:700; }
.overview_desc { margin-top:8px; color:#7b6a4b; font-size:14px; line-height:1.6; }
.overview_stat {
	min-width:180px;
	padding:18px 20px;
	border-radius:20px;
	background:linear-gradient(135deg,#f2f7ff 0%,#ffffff 100%);
}
.overview_stat_label { color:#73829a; font-size:14px; }
.overview_stat_value { margin-top:10px; color:#2f3640; font-size:34px; font-weight:700; }
.overview_cards { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:16px; }
.overview_card {
	padding:18px 20px;
	border-radius:20px;
	background:#fff;
	box-shadow: inset 0 1px 0 rgba(255,255,255,.9), 0 10px 24px rgba(132,115,74,0.08);
	border:1px solid rgba(236,227,211,.9);
}
.overview_card.warm { background:linear-gradient(135deg,#fff7e7 0%,#ffffff 100%); }
.overview_card.green { background:linear-gradient(135deg,#eefaf0 0%,#ffffff 100%); }
.overview_card.blue { background:linear-gradient(135deg,#eef5ff 0%,#ffffff 100%); }
.overview_value { color:#2f2a1f; font-size:30px; font-weight:700; }
.overview_label { margin-top:6px; color:#86755b; font-size:14px; }
.list_search_view {
	display:flex;
	justify-content:space-between;
	align-items:center;
	gap:18px;
	padding:20px 22px;
	margin: 0 0 18px;
	border-radius:22px;
	background:linear-gradient(180deg,#fffdfa 0%,#ffffff 100%);
	box-shadow:0 14px 34px rgba(121, 104, 67, 0.08);
	border:1px solid rgba(235, 221, 191, 0.9);
	flex-wrap:wrap;
}
.search_form { display:flex; align-items:center; gap:14px; flex-wrap:wrap; flex:1; }
.search_view { display:flex; align-items:center; }
.search_label { margin:0 10px 0 0; color:#665942; font-weight:700; min-width:84px; line-height:40px; text-align:right; }
.search_box { display:inline-block; width:auto; }
:deep(.search_inp) {
	border:1px solid #e6dcc8;
	border-radius:14px;
	padding:0 12px;
	background:#fff;
	line-height:38px;
	box-sizing:border-box;
	min-width:220px;
	box-shadow:none;
}
.search_btn_view { display:flex; }
.search_btn {
	border:0;
	border-radius:999px;
	padding:0 26px;
	color:#fff;
	background:linear-gradient(135deg,#73c4cf 0%,#5ca8c3 100%);
	height:40px;
	box-shadow:0 12px 24px rgba(92, 168, 195, 0.22);
}
.btn_view { display:flex; gap:12px; flex-wrap:wrap; }
.action_btn {
	border-radius:999px;
	min-width:100px;
	height:40px;
	font-weight:700;
}
.stat_btn { box-shadow:0 12px 24px rgba(235, 174, 66, 0.22); }
.beauty_table {
	border-radius:24px;
	overflow:hidden;
	box-shadow:0 18px 40px rgba(119, 102, 64, 0.08);
}
:deep(.beauty_table th.el-table__cell) {
	background:#fbf7ef;
	color:#766445;
	font-weight:700;
}
:deep(.beauty_table td.el-table__cell) {
	padding:18px 0;
}
.score_badge {
	display:inline-flex;
	align-items:center;
	justify-content:center;
	min-width:72px;
	padding:6px 12px;
	border-radius:999px;
	font-weight:700;
}
.score_badge.excellent {
	background:#eefaf0;
	color:#46a35b;
}
.score_badge.good {
	background:#eef5ff;
	color:#4a86d9;
}
.score_badge.normal {
	background:#fff7e8;
	color:#c88a2a;
}
.formModel_btn_box { display: flex; width: 100%; justify-content: center; }
.formModel_cancel { border: 0; cursor: pointer; border-radius: 999px; padding: 0 24px; color: #fff; background: #999; min-width: 100px; height: 36px; }
@media (max-width: 1200px) {
	.overview_cards { grid-template-columns:repeat(2,minmax(0,1fr)); }
}
</style>
