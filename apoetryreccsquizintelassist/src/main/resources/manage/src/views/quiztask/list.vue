<template>
	<div>
		<div class="app-contain">
			<div class="overview_panel" v-if="btnAuth('quiztask','查看')">
				<div class="overview_header">
					<div class="overview_title">{{ sessionTable === 'teacher' ? '我所带班级完成情况' : '班级完成情况' }}</div>
					<div class="overview_desc">按老师当前可查看的AI测验任务自动汇总，方便快速了解各班完成进度。</div>
				</div>
				<div class="overview_cards">
					<div class="overview_card">
						<div class="overview_value">{{ classOverview.length }}</div>
						<div class="overview_label">班级数</div>
					</div>
					<div class="overview_card warm">
						<div class="overview_value">{{ overviewStats.total }}</div>
						<div class="overview_label">总任务数</div>
					</div>
					<div class="overview_card green">
						<div class="overview_value">{{ overviewStats.completed }}</div>
						<div class="overview_label">已完成任务</div>
					</div>
					<div class="overview_card blue">
						<div class="overview_value">{{ overviewStats.uploaded }}</div>
						<div class="overview_label">已完成测验</div>
					</div>
				</div>
				<div class="overview_table_wrap" v-if="classOverview.length">
					<el-table :data="classOverview" border :stripe="false" size="small" class="overview_table">
						<el-table-column prop="classname" label="班级" min-width="140"></el-table-column>
						<el-table-column prop="studentCount" label="涉及学生" width="100"></el-table-column>
						<el-table-column prop="total" label="任务数" width="90"></el-table-column>
						<el-table-column prop="completed" label="已完成" width="90"></el-table-column>
						<el-table-column prop="pending" label="待完成" width="90"></el-table-column>
						<el-table-column prop="uploaded" label="已交作答" width="100"></el-table-column>
						<el-table-column label="完成率" width="140">
							<template #default="scope">
								<div class="progress_cell">
									<div class="progress_bar">
										<div class="progress_inner" :style="{ width: `${scope.row.rate}%` }"></div>
									</div>
									<span class="progress_text">{{ scope.row.rate }}%</span>
								</div>
							</template>
						</el-table-column>
					</el-table>
				</div>
				<div class="overview_empty" v-else>
					当前还没有可统计的班级任务，老师创建测验作业后这里会自动显示完成情况。
				</div>
				<div class="student_progress_block">
					<div class="subsection_title">每个学生的任务进度</div>
					<div class="subsection_desc">老师可以直接查看每位学生的任务总量、完成数量、作答提交情况和当前完成率。</div>
					<div class="chart_grid" v-if="studentOverview.length || classOverview.length">
						<div class="chart_card">
							<div class="chart_title">学生完成率柱状图</div>
							<div ref="studentBarChartRef" class="chart_canvas"></div>
						</div>
						<div class="chart_card">
							<div class="chart_title">任务状态饼状图</div>
							<div ref="taskPieChartRef" class="chart_canvas"></div>
						</div>
						<div class="chart_card chart_card_full">
							<div class="chart_title">班级完成率折线图</div>
							<div ref="classLineChartRef" class="chart_canvas chart_canvas_wide"></div>
						</div>
					</div>
					<div class="overview_table_wrap" v-if="studentOverview.length">
						<el-table :data="studentOverview" border :stripe="false" size="small" class="overview_table">
							<el-table-column prop="classname" label="班级" min-width="120"></el-table-column>
							<el-table-column prop="studentname" label="学生姓名" min-width="120"></el-table-column>
							<el-table-column prop="studentaccount" label="学生账号" min-width="120"></el-table-column>
							<el-table-column prop="total" label="总任务数" width="90"></el-table-column>
							<el-table-column prop="completed" label="已完成" width="90"></el-table-column>
							<el-table-column prop="pending" label="待完成" width="90"></el-table-column>
							<el-table-column prop="uploaded" label="已交作答" width="100"></el-table-column>
							<el-table-column prop="scored" label="已评分" width="90"></el-table-column>
							<el-table-column label="进度" min-width="180">
								<template #default="scope">
									<div class="progress_cell">
										<div class="progress_bar">
											<div class="progress_inner" :style="{ width: `${scope.row.rate}%` }"></div>
										</div>
										<span class="progress_text">{{ scope.row.rate }}%</span>
									</div>
								</template>
							</el-table-column>
						</el-table>
					</div>
					<div class="overview_empty" v-else>
						当前还没有学生任务进度数据。
					</div>
				</div>
			</div>
			<div class="list_search_view">
				<el-form :model="searchQuery" class="search_form">
					<div class="search_view">
						<div class="search_label">学生账号：</div>
						<div class="search_box">
							<el-select class="search_sel" clearable v-model="searchQuery.studentaccount" placeholder="学生账号">
								<el-option v-for="item in studentaccountLists" :label="item" :value="item"></el-option>
							</el-select>
						</div>
					</div>
					<div class="search_view">
						<div class="search_label">任务标题：</div>
						<div class="search_box">
							<el-input class="search_inp" v-model="searchQuery.tasktitle" placeholder="任务标题" clearable></el-input>
						</div>
					</div>
					<div class="search_btn_view">
						<el-button class="search_btn" type="primary" @click="searchClick()" size="small">搜索</el-button>
					</div>
				</el-form>
				<br>
				<div class="btn_view">
					<el-button type="success" @click="addClick" v-if="btnAuth('quiztask','新增')">创建作业 / 批量发送</el-button>
					<el-button type="info" v-if="btnAuth('quiztask','查看')" :disabled="selRows.length==1?false:true" @click="infoClick(null)">详情</el-button>
					<el-button type="primary" :disabled="selRows.length==1?false:true" @click="editClick" v-if="btnAuth('quiztask','修改')">修改</el-button>
					<el-button type="danger" :disabled="selRows.length?false:true" @click="delClick(null)" v-if="btnAuth('quiztask','删除')">删除</el-button>
				</div>
			</div>
			<br>
			<el-table v-loading="listLoading" border :stripe='false' @selection-change="handleSelectionChange" ref="table" v-if="btnAuth('quiztask','查看')" :data="list" @row-click="listChange">
				<el-table-column :resizable='true' align="left" header-align="left" type="selection" width="55" />
				<el-table-column label="序号" width="70" :resizable='true' :sortable='true' align="left" header-align="left"><template #default="scope">{{ scope.$index + 1}}</template></el-table-column>
				<el-table-column :resizable='true' align="left" header-align="left" label="班级" width="140"><template #default="scope">{{ getClassname(scope.row.studentaccount) }}</template></el-table-column>
				<el-table-column :resizable='true' :sortable='true' align="left" header-align="left" prop="studentaccount" label="学生账号"><template #default="scope">{{scope.row.studentaccount}}</template></el-table-column>
				<el-table-column :resizable='true' :sortable='true' align="left" header-align="left" prop="studentname" label="学生姓名"><template #default="scope">{{scope.row.studentname}}</template></el-table-column>
				<el-table-column :resizable='true' align="left" header-align="left" prop="coursetitles" label="指定古诗" min-width="220"><template #default="scope">{{scope.row.coursetitles || '未指定'}}</template></el-table-column>
				<el-table-column :resizable='true' :sortable='true' align="left" header-align="left" prop="tasktitle" label="任务标题"><template #default="scope">{{scope.row.tasktitle}}</template></el-table-column>
				<el-table-column :resizable='true' align="left" header-align="left" prop="completionremark" label="测验作答"><template #default="scope">{{scope.row.completionremark ? '已上传' : '未完成'}}</template></el-table-column>
				<el-table-column :resizable='true' :sortable='true' align="left" header-align="left" prop="completionstatus" label="完成状态"><template #default="scope">{{scope.row.completionstatus}}</template></el-table-column>
				<el-table-column :resizable='true' :sortable='true' align="left" header-align="left" prop="kaoshichengji" label="测验得分"><template #default="scope">{{scope.row.kaoshichengji || '待评分'}}<el-tag v-if="scope.row.aiscorecomment && scope.row.aiscorecomment.startsWith('{')" size="small" type="success" effect="plain" style="margin-left:6px">AI</el-tag></template></el-table-column>
				<el-table-column :resizable='true' align="left" header-align="left" prop="recognizedtext" label="识别文本" min-width="220"><template #default="scope">{{scope.row.recognizedtext || '未识别'}}</template></el-table-column>
				<el-table-column :resizable='true' :sortable='true' align="left" header-align="left" prop="deadline" label="截止日期"><template #default="scope">{{scope.row.deadline}}</template></el-table-column>
				<el-table-column label="操作" width="320" :resizable='true' :sortable='true' align="left" header-align="left">
					<template #default="scope">
						<el-button type="info" v-if="btnAuth('quiztask','查看')" @click="infoClick(scope.row.id)">详情</el-button>
						<el-button
							type="success"
							v-if="sessionTable === 'teacher' && btnAuth('quiztask','修改') && scope.row.completionremark"
							@click="reviewClick(scope.row.id)"
						>
							批改
						</el-button>
					</template>
				</el-table-column>
			</el-table>
			<el-pagination background :layout="layouts.join(',')" :total="total" :page-size="listQuery.limit" prev-text="上一页" next-text="下一页" :hide-on-single-page="false" :style='{"padding":"0","margin":"20px auto","whiteSpace":"nowrap","color":"#333","alignItems":"center","textAlign":"center","display":"flex","width":"100%","fontWeight":"500","justifyContent":"center"}' @size-change="sizeChange" @current-change="currentChange" @prev-click="prevClick" @next-click="nextClick" />
		</div>
		<formModel ref="formRef" @formModelChange="formModelChange"></formModel>
	</div>
</template>
<script setup>
import { ref, getCurrentInstance, nextTick, inject, watch, onMounted, onBeforeUnmount } from 'vue'
import { ElMessageBox } from 'element-plus'
import formModel from './formModel.vue'
const context = getCurrentInstance()?.appContext.config.globalProperties;
const echarts = inject('echarts') || context?.$echarts
const tableName = 'quiztask'
const formName = 'AI测验任务'
const list = ref([])
const table = ref(null)
const listQuery = ref({ page: 1, limit: 20, sort: 'id', order: 'desc' })
const searchQuery = ref({})
const selRows = ref([])
const listLoading = ref(false)
const total = ref(0)
const layouts = ref(["total","prev","pager","next","sizes"])
const studentaccountLists = ref([])
const studentMap = ref({})
const classOverview = ref([])
const studentOverview = ref([])
const overviewStats = ref({
	total: 0,
	completed: 0,
	uploaded: 0
})
const studentBarChartRef = ref(null)
const taskPieChartRef = ref(null)
const classLineChartRef = ref(null)
const studentBarChart = ref(null)
const taskPieChart = ref(null)
const classLineChart = ref(null)
const sessionTable = ref(context?.$toolUtil.storageGet('sessionTable') || '')
const formRef = ref(null)
const listChange = (row)=>{ nextTick(()=>{ table.value.clearSelection(); table.value.toggleRowSelection(row) }) }
const btnAuth = (e,a)=> context?.$toolUtil.isAuth(e,a)
const getstudentaccountLists = () => { context?.$http({ url: 'option/student/studentaccount', method:'get' }).then(res=>{ studentaccountLists.value = res.data.data }) }
const getClassname = (studentaccount) => studentMap.value[studentaccount]?.classname || '未分班'
const buildClassOverview = (tasks) => {
	const classMap = {}
	tasks.forEach(item => {
		const studentInfo = studentMap.value[item.studentaccount] || {}
		const classname = studentInfo.classname || '未分班'
		if (!classMap[classname]) {
			classMap[classname] = {
				classname,
				total: 0,
				completed: 0,
				pending: 0,
				uploaded: 0,
				studentSet: {}
			}
		}
		classMap[classname].total += 1
		if (item.completionstatus === '已完成') classMap[classname].completed += 1
		else classMap[classname].pending += 1
		if (item.completionremark) classMap[classname].uploaded += 1
		if (item.studentaccount) classMap[classname].studentSet[item.studentaccount] = true
	})
	const rows = Object.values(classMap).map(item => ({
		classname: item.classname,
		total: item.total,
		completed: item.completed,
		pending: item.pending,
		uploaded: item.uploaded,
		studentCount: Object.keys(item.studentSet).length,
		rate: item.total ? Math.round((item.completed / item.total) * 100) : 0
	})).sort((a,b) => b.completed - a.completed || a.classname.localeCompare(b.classname))
	classOverview.value = rows
	overviewStats.value = rows.reduce((sum, item) => {
		sum.total += item.total
		sum.completed += item.completed
		sum.uploaded += item.uploaded
		return sum
	}, { total: 0, completed: 0, uploaded: 0 })
}
const buildStudentOverview = (tasks) => {
	const studentStats = {}
	tasks.forEach(item => {
		const key = item.studentaccount || `unknown-${item.id}`
		const studentInfo = studentMap.value[item.studentaccount] || {}
		if (!studentStats[key]) {
			studentStats[key] = {
				studentaccount: item.studentaccount || '',
				studentname: item.studentname || studentInfo.studentname || '未命名学生',
				classname: studentInfo.classname || '未分班',
				total: 0,
				completed: 0,
				pending: 0,
				uploaded: 0,
				scored: 0
			}
		}
		studentStats[key].total += 1
		if (item.completionstatus === '已完成') studentStats[key].completed += 1
		else studentStats[key].pending += 1
		if (item.completionremark) studentStats[key].uploaded += 1
		if (item.kaoshichengji !== null && item.kaoshichengji !== undefined && item.kaoshichengji !== '') {
			studentStats[key].scored += 1
		}
	})
	studentOverview.value = Object.values(studentStats).map(item => ({
		...item,
		rate: item.total ? Math.round((item.completed / item.total) * 100) : 0
	})).sort((a, b) => {
		if (a.classname === b.classname) {
			return (b.rate - a.rate) || a.studentname.localeCompare(b.studentname)
		}
		return a.classname.localeCompare(b.classname)
	})
}
const resizeCharts = () => {
	studentBarChart.value && studentBarChart.value.resize()
	taskPieChart.value && taskPieChart.value.resize()
	classLineChart.value && classLineChart.value.resize()
}
const renderCharts = () => {
	if (!echarts) return
	nextTick(() => {
		if (studentBarChartRef.value) {
			if (!studentBarChart.value) studentBarChart.value = echarts.init(studentBarChartRef.value)
			const topStudents = [...studentOverview.value]
				.sort((a, b) => b.rate - a.rate || b.completed - a.completed)
				.slice(0, 8)
			studentBarChart.value.setOption({
				grid: { left: 36, right: 20, top: 30, bottom: 50 },
				tooltip: { trigger: 'axis' },
				xAxis: {
					type: 'category',
					data: topStudents.map(item => item.studentname),
					axisLabel: { interval: 0, rotate: 20, color: '#7b6a4b' },
					axisLine: { lineStyle: { color: '#dfd3ba' } }
				},
				yAxis: {
					type: 'value',
					max: 100,
					axisLabel: { formatter: '{value}%', color: '#7b6a4b' },
					splitLine: { lineStyle: { color: 'rgba(185, 161, 116, 0.12)' } }
				},
				series: [{
					type: 'bar',
					barWidth: 26,
					data: topStudents.map(item => item.rate),
					itemStyle: {
						borderRadius: [8, 8, 0, 0],
						color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
							{ offset: 0, color: '#6fc47d' },
							{ offset: 1, color: '#e7ba63' }
						])
					}
				}]
			})
		}
		if (taskPieChartRef.value) {
			if (!taskPieChart.value) taskPieChart.value = echarts.init(taskPieChartRef.value)
			taskPieChart.value.setOption({
				tooltip: { trigger: 'item' },
				legend: { bottom: 0, icon: 'circle' },
				series: [{
					type: 'pie',
					radius: ['48%', '72%'],
					center: ['50%', '45%'],
					label: { formatter: '{b}\n{d}%' },
					data: [
						{ value: overviewStats.value.completed, name: '已完成', itemStyle: { color: '#71c179' } },
						{ value: Math.max(overviewStats.value.total - overviewStats.value.completed, 0), name: '待完成', itemStyle: { color: '#ebb45e' } },
						{ value: Math.max(overviewStats.value.uploaded - overviewStats.value.completed, 0), name: '待批改', itemStyle: { color: '#79aef2' } }
					]
				}]
			})
		}
		if (classLineChartRef.value) {
			if (!classLineChart.value) classLineChart.value = echarts.init(classLineChartRef.value)
			classLineChart.value.setOption({
				grid: { left: 36, right: 20, top: 30, bottom: 36 },
				tooltip: { trigger: 'axis' },
				xAxis: {
					type: 'category',
					data: classOverview.value.map(item => item.classname),
					axisLabel: { color: '#7b6a4b' },
					axisLine: { lineStyle: { color: '#dfd3ba' } }
				},
				yAxis: {
					type: 'value',
					max: 100,
					axisLabel: { formatter: '{value}%', color: '#7b6a4b' },
					splitLine: { lineStyle: { color: 'rgba(185, 161, 116, 0.12)' } }
				},
				series: [{
					type: 'line',
					smooth: true,
					symbol: 'circle',
					symbolSize: 10,
					data: classOverview.value.map(item => item.rate),
					lineStyle: { width: 3, color: '#6aa2e8' },
					itemStyle: { color: '#6aa2e8' },
					areaStyle: { color: 'rgba(106, 162, 232, 0.15)' }
				}]
			})
		}
		resizeCharts()
	})
}
const getOverviewData = async () => {
	try {
		const [studentRes, taskRes] = await Promise.all([
			context?.$http({
				url: 'student/page',
				method: 'get',
				params: { page: 1, limit: 1000, sort: 'id', order: 'desc' }
			}),
			context?.$http({
				url: `${tableName}/page`,
				method: 'get',
				params: { page: 1, limit: 1000, sort: 'id', order: 'desc' }
			})
		])
		const students = studentRes.data.data.list || []
		studentMap.value = students.reduce((acc, item) => {
			acc[item.studentaccount] = item
			return acc
		}, {})
		const taskList = taskRes.data.data.list || []
		buildClassOverview(taskList)
		buildStudentOverview(taskList)
	} catch (error) {
		classOverview.value = []
		studentOverview.value = []
		overviewStats.value = { total: 0, completed: 0, uploaded: 0 }
	}
}
const getList = () => {
	listLoading.value = true
	let params = JSON.parse(JSON.stringify(listQuery.value))
	params.sort = 'id'; params.order = 'desc'
	params.tasktitle = searchQuery.value.tasktitle ? searchQuery.value.tasktitle : '测验：'
	if(searchQuery.value.studentaccount) params.studentaccount = searchQuery.value.studentaccount
	context?.$http({ url: `${tableName}/page`, method: 'get', params }).then(res => {
		listLoading.value = false
		list.value = res.data.data.list
		total.value = Number(res.data.data.total)
	})
}
const searchClick = async () => { listQuery.value.page = 1; getList(); await getOverviewData() }
const formModelChange = async ()=> { await searchClick() }
const addClick = ()=> formRef.value.init()
const editClick = ()=> { if(selRows.value.length) formRef.value.init(selRows.value[0].id,'edit') }
const reviewClick = (id)=> { if(id) formRef.value.init(id,'review') }
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
watch([studentOverview, classOverview, overviewStats], () => {
	renderCharts()
}, { deep: true })
onMounted(() => {
	window.addEventListener('resize', resizeCharts)
})
onBeforeUnmount(() => {
	window.removeEventListener('resize', resizeCharts)
	studentBarChart.value && studentBarChart.value.dispose()
	taskPieChart.value && taskPieChart.value.dispose()
	classLineChart.value && classLineChart.value.dispose()
})
getstudentaccountLists()
getOverviewData()
getList()
</script>
<style lang="scss" scoped>
.overview_panel {
	margin: 0 0 24px;
	padding: 24px;
	border-radius: 24px;
	background:
		radial-gradient(circle at top right, rgba(255, 206, 129, 0.18), transparent 22%),
		linear-gradient(180deg, #fffdf8 0%, #ffffff 100%);
	box-shadow: 0 20px 48px rgba(102, 89, 57, 0.08);
	border: 1px solid rgba(235, 221, 191, 0.9);
}
.overview_header { margin-bottom: 18px; }
.overview_title { color:#2f2a1f; font-size:24px; font-weight:700; }
.overview_desc { margin-top:8px; color:#7b6a4b; font-size:14px; line-height:1.8; }
.overview_cards { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:16px; margin-bottom:18px; }
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
.overview_table_wrap { margin-top: 8px; }
.student_progress_block { margin-top: 26px; }
.chart_grid {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 16px;
	margin-bottom: 18px;
}
.chart_card {
	padding: 18px;
	border-radius: 20px;
	background: linear-gradient(180deg, #fffdfa 0%, #ffffff 100%);
	border: 1px solid rgba(236,227,211,.9);
	box-shadow: 0 10px 24px rgba(132,115,74,0.08);
}
.chart_card_full {
	grid-column: 1 / -1;
}
.chart_title {
	color: #5f5138;
	font-size: 15px;
	font-weight: 700;
	margin-bottom: 10px;
}
.chart_canvas {
	width: 100%;
	height: 280px;
}
.chart_canvas_wide {
	height: 320px;
}
.subsection_title { color:#2f2a1f; font-size:20px; font-weight:700; margin-bottom:6px; }
.subsection_desc { color:#7b6a4b; font-size:13px; line-height:1.8; margin-bottom:12px; }
.progress_cell { display:flex; align-items:center; gap:10px; }
.progress_bar { flex:1; height:8px; border-radius:999px; background:#efe7d7; overflow:hidden; }
.progress_inner { height:100%; border-radius:999px; background:linear-gradient(90deg,#f0ba5f 0%,#69ba73 100%); }
.progress_text { color:#6c7a89; font-size:12px; min-width:40px; }
.overview_empty {
	padding: 18px 20px;
	border-radius: 18px;
	background: rgba(248, 243, 232, 0.9);
	color: #87765d;
	font-size: 14px;
	line-height: 1.8;
}
.list_search_view { margin:20px auto; display:flex; width:100%; flex-wrap:wrap; }
.search_form { display:flex; align-items:center; order:2; }
.search_view { margin:0 10px 0 0; display:flex; align-items:center; }
.search_label { margin:0 10px 0 0; color:#666; font-weight:500; min-width:100px; line-height:40px; }
.search_box { display:inline-block; width:auto; }
:deep(.search_inp), :deep(.search_sel) { border:1px solid #999; border-radius:0; padding:0 10px; background:#fff; width:auto; line-height:34px; box-sizing:border-box; }
.search_btn_view { width:20%; display:flex; padding:0 20px; }
.search_btn { border:0; border-radius:0; padding:0 24px; color:#fff; background:linear-gradient(270deg, rgba(130,196,209,1) 0%, rgba(115,186,200,1) 24%, rgba(174,210,217,1) 100%); height:36px; }
@media (max-width: 1200px) {
	.overview_cards { grid-template-columns:repeat(2,minmax(0,1fr)); }
	.chart_grid { grid-template-columns: 1fr; }
	.chart_card_full { grid-column: auto; }
}
</style>
