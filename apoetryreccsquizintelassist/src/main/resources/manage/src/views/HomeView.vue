<template>
	<div class="home_view">
		<section class="hero_panel">
			<div>
				<div class="hero_badge">乡村儿童古诗文学习平台</div>
				<h1>首页数据总览</h1>
				<p>集中查看当前平台教师与学生数量，帮助管理员快速掌握师生规模。</p>
			</div>
			<div class="hero_total">
				<div class="total_label">师生总数</div>
				<div class="total_value">{{ totalCount }}</div>
				<div class="total_note">教师 {{ teacherCount }} 人 / 学生 {{ studentCount }} 人</div>
			</div>
		</section>

		<section class="stat_grid">
			<div class="stat_card teacher">
				<div class="stat_label">教师人数</div>
				<div class="stat_value">{{ teacherCount }}</div>
				<div class="stat_desc">当前已登记教师账号数量</div>
			</div>
			<div class="stat_card student">
				<div class="stat_label">学生人数</div>
				<div class="stat_value">{{ studentCount }}</div>
				<div class="stat_desc">当前已登记学生账号数量</div>
			</div>
		</section>

		<section class="panel_card grade_panel">
			<div class="section_head">
				<div>
					<div class="section_tag">Grade Chart</div>
					<h2>各年级学生人数</h2>
				</div>
			</div>
			<div class="grade_chart">
				<div class="grade_item" v-for="item in gradeStats" :key="item.grade">
					<div class="grade_name">{{ item.grade }}</div>
					<div class="grade_bar_track">
						<div class="grade_bar_fill" :style="{ height: item.percent + '%' }"></div>
					</div>
					<div class="grade_num">{{ item.count }}人</div>
				</div>
			</div>
		</section>

		<section class="chart_grid">
			<div class="panel_card">
				<div class="section_head">
					<div>
						<div class="section_tag">Bar Chart</div>
						<h2>师生数量柱状图</h2>
					</div>
				</div>
				<div class="bar_chart">
					<div class="bar_row">
						<div class="bar_name">教师</div>
						<div class="bar_track">
							<div class="bar_fill teacher_fill" :style="{ width: teacherPercent + '%' }"></div>
						</div>
						<div class="bar_num">{{ teacherCount }}</div>
					</div>
					<div class="bar_row">
						<div class="bar_name">学生</div>
						<div class="bar_track">
							<div class="bar_fill student_fill" :style="{ width: studentPercent + '%' }"></div>
						</div>
						<div class="bar_num">{{ studentCount }}</div>
					</div>
				</div>
			</div>

			<div class="panel_card ratio_panel">
				<div class="section_head">
					<div>
						<div class="section_tag">Ratio</div>
						<h2>师生占比</h2>
					</div>
				</div>
				<div class="donut_wrap">
					<div class="donut" :style="{ background: donutStyle }">
						<div class="donut_inner">
							<div class="donut_value">{{ totalCount }}</div>
							<div class="donut_label">总人数</div>
						</div>
					</div>
					<div class="legend">
						<div class="legend_item">
							<span class="dot teacher_dot"></span>
							教师 {{ teacherPercent }}%
						</div>
						<div class="legend_item">
							<span class="dot student_dot"></span>
							学生 {{ studentPercent }}%
						</div>
					</div>
				</div>
			</div>
		</section>
	</div>
</template>

<script setup>
import { computed, getCurrentInstance, onMounted, ref } from 'vue'

const context = getCurrentInstance()?.appContext.config.globalProperties
const teacherCount = ref(0)
const studentCount = ref(0)
const gradeCounts = ref({})
const grades = ['一年级', '二年级', '三年级', '四年级', '五年级', '六年级']

const totalCount = computed(() => teacherCount.value + studentCount.value)
const maxGradeCount = computed(() => Math.max(...grades.map(grade => gradeCounts.value[grade] || 0), 1))
const gradeStats = computed(() => grades.map(grade => {
	const count = gradeCounts.value[grade] || 0
	return {
		grade,
		count,
		percent: Math.max(Math.round((count / maxGradeCount.value) * 100), count ? 8 : 0)
	}
}))
const teacherPercent = computed(() => {
	if (!totalCount.value) return 0
	return Math.round((teacherCount.value / totalCount.value) * 100)
})
const studentPercent = computed(() => {
	if (!totalCount.value) return 0
	return 100 - teacherPercent.value
})
const donutStyle = computed(() => {
	const end = teacherPercent.value
	return `conic-gradient(#9b533f 0 ${end}%, #4f7e5d ${end}% 100%)`
})

const fetchCount = async (tableName) => {
	const res = await context?.$http({
		url: `${tableName}/count`,
		method: 'get'
	})
	return Number(res?.data?.data || 0)
}

const loadStats = async () => {
	const [teachers, students, studentList] = await Promise.all([
		fetchCount('teacher'),
		fetchCount('student'),
		context?.$http({
			url: 'student/page',
			method: 'get',
			params: { page: 1, limit: 10000 }
		})
	])
	teacherCount.value = teachers
	studentCount.value = students
	const countMap = {}
	;(studentList?.data?.data?.list || []).forEach(item => {
		const grade = item.grade || '未分年级'
		countMap[grade] = (countMap[grade] || 0) + 1
	})
	gradeCounts.value = countMap
}

onMounted(() => {
	loadStats()
})
</script>

<style lang="scss" scoped>
.home_view {
	min-height: 100vh;
	padding: 24px;
	box-sizing: border-box;
	color: #4f3925;
}

.hero_panel {
	display: grid;
	grid-template-columns: minmax(0, 1fr) 260px;
	gap: 24px;
	align-items: center;
	padding: 30px 34px;
	border-radius: 22px;
	background:
		radial-gradient(circle at 8% 10%, rgba(169, 78, 61, 0.08), transparent 24%),
		linear-gradient(135deg, #fffaf0 0%, #eff7e9 100%);
	border: 1px solid rgba(181, 149, 95, 0.26);
	box-shadow: 0 18px 38px rgba(99, 86, 58, 0.1);
}

.hero_badge {
	display: inline-flex;
	padding: 7px 14px;
	border-radius: 999px;
	background: rgba(169, 78, 61, 0.1);
	color: #9b533f;
	font-size: 13px;
	font-weight: 700;
}

h1 {
	margin: 14px 0 10px;
	font-size: 34px;
	font-family: "STKaiti", "KaiTi", "Microsoft YaHei", sans-serif;
	line-height: 1.2;
}

p {
	margin: 0;
	color: #725f3d;
	line-height: 1.8;
}

.hero_total {
	padding: 24px;
	border-radius: 20px;
	background: rgba(255, 255, 250, 0.72);
	border: 1px solid rgba(185, 154, 103, 0.22);
	text-align: center;
}

.total_label,
.stat_label {
	color: #715f3e;
	font-weight: 700;
}

.total_value {
	margin: 8px 0;
	font-size: 48px;
	font-weight: 800;
	color: #9b533f;
}

.total_note,
.stat_desc {
	color: #7b704f;
	font-size: 14px;
}

.stat_grid {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 20px;
	margin: 22px 0;
}

.stat_card,
.panel_card {
	border-radius: 20px;
	background: #fffdf5;
	border: 1px solid rgba(190, 165, 111, 0.28);
	box-shadow: 0 14px 32px rgba(103, 86, 54, 0.08);
}

.stat_card {
	padding: 26px;
}

.stat_card.teacher {
	background: linear-gradient(135deg, #fff7eb 0%, #f8ead6 100%);
}

.stat_card.student {
	background: linear-gradient(135deg, #f4fbef 0%, #e8f4df 100%);
}

.stat_value {
	margin: 10px 0 8px;
	font-size: 42px;
	font-weight: 800;
}

.teacher .stat_value {
	color: #9b533f;
}

.student .stat_value {
	color: #4f7e5d;
}

.chart_grid {
	display: grid;
	grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.65fr);
	gap: 20px;
}

.grade_panel {
	margin-bottom: 22px;
}

.grade_chart {
	display: grid;
	grid-template-columns: repeat(6, minmax(0, 1fr));
	gap: 18px;
	align-items: end;
	min-height: 250px;
	padding: 12px 6px 4px;
}

.grade_item {
	display: grid;
	grid-template-rows: auto 180px auto;
	gap: 10px;
	justify-items: center;
	min-width: 0;
}

.grade_name {
	color: #5b442d;
	font-weight: 800;
	font-size: 14px;
}

.grade_bar_track {
	width: 42px;
	height: 180px;
	border-radius: 999px;
	background: #f0e7d4;
	display: flex;
	align-items: flex-end;
	overflow: hidden;
	box-shadow: inset 0 0 0 1px rgba(91, 68, 45, 0.06);
}

.grade_bar_fill {
	width: 100%;
	border-radius: 999px 999px 0 0;
	background: linear-gradient(180deg, #b65a46 0%, #d8a05b 100%);
	transition: height 0.3s ease;
}

.grade_num {
	color: #4f7e5d;
	font-weight: 800;
	font-size: 15px;
}

.panel_card {
	padding: 26px;
}

.section_head {
	margin-bottom: 24px;
}

.section_tag {
	color: #9b533f;
	font-size: 12px;
	font-weight: 800;
	letter-spacing: 0.6px;
}

h2 {
	margin: 6px 0 0;
	font-size: 24px;
	font-family: "STKaiti", "KaiTi", "Microsoft YaHei", sans-serif;
	color: #3d3221;
}

.bar_chart {
	display: grid;
	gap: 26px;
	padding: 16px 0 10px;
}

.bar_row {
	display: grid;
	grid-template-columns: 64px minmax(0, 1fr) 54px;
	gap: 14px;
	align-items: center;
}

.bar_name {
	font-weight: 800;
	color: #5b442d;
}

.bar_track {
	height: 26px;
	border-radius: 999px;
	background: #f0e7d4;
	overflow: hidden;
}

.bar_fill {
	height: 100%;
	min-width: 4%;
	border-radius: 999px;
	transition: width 0.3s ease;
}

.teacher_fill {
	background: linear-gradient(90deg, #b65a46, #d8a05b);
}

.student_fill {
	background: linear-gradient(90deg, #4f7e5d, #89b76e);
}

.bar_num {
	font-size: 18px;
	font-weight: 800;
	color: #3d3221;
	text-align: right;
}

.donut_wrap {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 22px;
}

.donut {
	width: 210px;
	height: 210px;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: inset 0 0 0 1px rgba(91, 68, 45, 0.08);
}

.donut_inner {
	width: 128px;
	height: 128px;
	border-radius: 50%;
	background: #fffdf5;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
}

.donut_value {
	font-size: 34px;
	font-weight: 800;
	color: #3d3221;
}

.donut_label {
	color: #715f3e;
	font-size: 13px;
}

.legend {
	display: flex;
	gap: 18px;
	flex-wrap: wrap;
	justify-content: center;
	color: #5b442d;
	font-weight: 700;
}

.legend_item {
	display: inline-flex;
	align-items: center;
	gap: 8px;
}

.dot {
	width: 10px;
	height: 10px;
	border-radius: 50%;
}

.teacher_dot {
	background: #9b533f;
}

.student_dot {
	background: #4f7e5d;
}

@media (max-width: 980px) {
	.hero_panel,
	.chart_grid,
	.stat_grid {
		grid-template-columns: 1fr;
	}

	.grade_chart {
		grid-template-columns: repeat(3, minmax(0, 1fr));
	}
}
</style>
