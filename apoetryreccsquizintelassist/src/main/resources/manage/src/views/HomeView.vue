<template>
	<div class="home_view">
		<section class="hero_panel">
			<div class="hero_content">
				<div class="hero_badge">Admin Dashboard</div>
				<h1 class="hero_title">欢迎回来，{{ roleName }}</h1>
				<p class="hero_desc">
					这里是 {{ projectName }} 的管理工作台，您可以快速查看核心数据、进入高频模块并掌握当前系统运行概况。
				</p>
				<div class="hero_meta">
					<div class="meta_chip">
						<el-icon><Calendar /></el-icon>
						<span>{{ todayText }}</span>
					</div>
					<div class="meta_chip">
						<el-icon><Bell /></el-icon>
						<span>已开放 {{ quickEntryList.length }} 个快捷入口</span>
					</div>
				</div>
				<div class="hero_summary">
					<div
						v-for="item in heroSummaryList"
						:key="item.label"
						class="hero_summary_item"
					>
						<div class="hero_summary_label">{{ item.label }}</div>
						<div class="hero_summary_value">{{ item.value }}</div>
					</div>
				</div>
			</div>
			<div class="hero_side">
				<div class="hero_stat">
					<div class="hero_stat_label">当前可见数据总量</div>
					<div class="hero_stat_value">{{ totalVisibleCount }}</div>
					<div class="hero_stat_tip">按当前账号权限自动展示</div>
				</div>
				<div class="hero_side_list">
					<div
						v-for="item in heroSideList"
						:key="item.label"
						class="hero_side_item"
					>
						<span>{{ item.label }}</span>
						<strong>{{ item.value }}</strong>
					</div>
				</div>
				<div class="hero_glow hero_glow_one"></div>
				<div class="hero_glow hero_glow_two"></div>
			</div>
		</section>

		<section class="status_strip">
			<div
				v-for="item in statusCards"
				:key="item.label"
				class="status_card"
			>
				<div class="status_icon">
					<el-icon>
						<component :is="item.icon" />
					</el-icon>
				</div>
				<div class="status_info">
					<div class="status_label">{{ item.label }}</div>
					<div class="status_value">{{ item.value }}</div>
					<div class="status_desc">{{ item.desc }}</div>
				</div>
			</div>
		</section>

		<section class="overview_section">
			<div class="section_head">
				<div>
					<div class="section_tag">Overview</div>
					<h2>核心数据概览</h2>
				</div>
				<p>以更清晰的方式呈现首页统计信息，便于管理员快速掌握整体情况。</p>
			</div>
			<div class="count_grid">
				<div
					v-for="item in statCards"
					:key="item.key"
					class="stat_card"
					:class="item.theme"
				>
					<div class="stat_icon">
						<el-icon>
							<component :is="item.icon" />
						</el-icon>
					</div>
					<div class="stat_info">
						<div class="stat_label">{{ item.label }}</div>
						<div class="stat_value">{{ item.value }}</div>
						<div class="stat_note">{{ item.note }}</div>
					</div>
				</div>
			</div>
		</section>

		<section class="content_grid">
			<div class="panel_card quick_panel">
				<div class="section_head compact">
					<div>
						<div class="section_tag">Navigate</div>
						<h2>常用快捷入口</h2>
					</div>
					<p>一键进入高频操作模块。</p>
				</div>
				<div class="quick_grid">
					<div
						v-for="item in quickEntryList"
						:key="item.path"
						class="quick_item"
						@click="goPage(item.path)"
					>
						<div class="quick_icon">
							<el-icon>
								<component :is="item.icon" />
							</el-icon>
						</div>
						<div class="quick_title">{{ item.title }}</div>
						<div class="quick_desc">{{ item.desc }}</div>
						<div class="quick_link">
							立即进入
							<el-icon><Right /></el-icon>
						</div>
					</div>
				</div>
			</div>

			<div class="panel_card insight_panel">
				<div class="section_head compact">
					<div>
						<div class="section_tag">Insight</div>
						<h2>今日看板</h2>
					</div>
					<p>简洁展示当前后台的管理重点。</p>
				</div>
				<div class="insight_list">
					<div class="insight_item">
						<span>账号角色</span>
						<strong>{{ roleName }}</strong>
					</div>
					<div class="insight_item">
						<span>项目名称</span>
						<strong>{{ projectName }}</strong>
					</div>
					<div class="insight_item">
						<span>开放模块</span>
						<strong>{{ quickEntryList.length }} 个</strong>
					</div>
					<div class="insight_item">
						<span>成绩数据</span>
						<strong>{{ transcriptCount }} 条</strong>
					</div>
				</div>
				<div class="insight_footer">
					<div class="footer_card">
						<div class="footer_label">用户与教师总量</div>
						<div class="footer_value">{{ studentCount + teacherCount }}</div>
					</div>
					<div class="footer_card">
						<div class="footer_label">首页更新时间</div>
						<div class="footer_value small">{{ currentTime }}</div>
					</div>
				</div>
			</div>
		</section>

		<section class="support_grid">
			<div class="panel_card flow_panel">
				<div class="section_head compact">
					<div>
						<div class="section_tag">Workflow</div>
						<h2>管理重点</h2>
					</div>
					<p>把首页数据转成更顺手的日常操作视角。</p>
				</div>
				<div class="flow_list">
					<div
						v-for="item in managementFocusList"
						:key="item.title"
						class="flow_item"
					>
						<div class="flow_top">
							<div class="flow_icon">
								<el-icon>
									<component :is="item.icon" />
								</el-icon>
							</div>
							<div class="flow_text">
								<div class="flow_title">{{ item.title }}</div>
								<div class="flow_desc">{{ item.desc }}</div>
							</div>
							<div class="flow_value">{{ item.value }}</div>
						</div>
					</div>
				</div>
			</div>

			<div class="panel_card focus_panel">
				<div class="section_head compact">
					<div>
						<div class="section_tag">Focus</div>
						<h2>推荐关注模块</h2>
					</div>
					<p>优先展示当前账号最常用的入口和用途。</p>
				</div>
				<div class="focus_grid">
					<div
						v-for="item in recommendedList"
						:key="item.path"
						class="focus_item"
						@click="goPage(item.path)"
					>
						<div class="focus_icon">
							<el-icon>
								<component :is="item.icon" />
							</el-icon>
						</div>
						<div class="focus_title">{{ item.title }}</div>
						<div class="focus_desc">{{ item.desc }}</div>
					</div>
				</div>
			</div>
		</section>

		<section v-if="btnAuth('transcript','首页统计')" class="panel_card chart_panel">
			<div class="section_head compact">
				<div>
					<div class="section_tag">Chart</div>
					<h2>成绩分布统计</h2>
				</div>
				<p>根据成绩区间统计数量，帮助快速识别整体分布情况。</p>
			</div>
			<div class="chart_wrap">
				<div id="transcriptkaoshichengjiEchart1" class="chart_canvas"></div>
			</div>
		</section>
	</div>
</template>

<script setup>
	import { computed, inject, nextTick, onBeforeUnmount, onMounted, ref, getCurrentInstance } from 'vue';
	import { useRouter } from 'vue-router';
	import {
		Calendar,
		Bell,
		User,
		UserFilled,
		Histogram,
		Reading,
		Document,
		EditPen,
		Right
	} from '@element-plus/icons-vue';

	const context = getCurrentInstance()?.appContext.config.globalProperties;
	const router = useRouter();
	const echarts = inject('echarts') || context?.$echarts;
	const projectName = context.$project.projectName;
	const roleName = context?.$toolUtil.storageGet('role') || '管理员';
	const chartInstance = ref(null);

	const studentCount = ref(0);
	const teacherCount = ref(0);
	const transcriptCount = ref(0);
	const currentTime = ref(context?.$toolUtil.getCurDateTime());

	const todayText = computed(() => {
		const date = new Date();
		const weekList = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
		return `${context?.$toolUtil.getCurDate()} ${weekList[date.getDay()]}`;
	});

	const totalVisibleCount = computed(() => studentCount.value + teacherCount.value + transcriptCount.value);

	const btnAuth = (tableName, key) => context?.$toolUtil.isAuth(tableName, key);

	const statCards = computed(() => {
		const list = [
			{
				key: 'student',
				label: '用户总数',
				value: studentCount.value,
				note: '覆盖平台学生端核心用户数据',
				icon: User,
				theme: 'theme_blue',
				show: btnAuth('student', '首页总数')
			},
			{
				key: 'teacher',
				label: '教师总数',
				value: teacherCount.value,
				note: '实时反映教师端账号规模',
				icon: UserFilled,
				theme: 'theme_orange',
				show: btnAuth('teacher', '首页总数')
			},
			{
				key: 'transcript',
				label: '成绩信息总数',
				value: transcriptCount.value,
				note: '便于追踪成绩沉淀与统计规模',
				icon: Histogram,
				theme: 'theme_green',
				show: btnAuth('transcript', '首页总数')
			}
		];
		return list.filter(item => item.show);
	});

	const quickEntryList = computed(() => {
		const list = [
			{
				title: '用户信息',
				desc: '查看学生账户资料与基础信息',
				path: '/student',
				icon: User,
				show: btnAuth('student', '查看')
			},
			{
				title: '教师管理',
				desc: '维护教师资料与教学账号',
				path: '/teacher',
				icon: UserFilled,
				show: btnAuth('teacher', '查看')
			},
			{
				title: '成绩信息',
				desc: '查看成绩记录与统计情况',
				path: '/transcript',
				icon: Histogram,
				show: btnAuth('transcript', '查看')
			},
			{
				title: '古诗词管理',
				desc: '维护诗词内容与资源信息',
				path: '/course',
				icon: Reading,
				show: btnAuth('course', '查看')
			},
			{
				title: '背诵任务',
				desc: '管理背诵安排与任务内容',
				path: '/recitationtask',
				icon: EditPen,
				show: btnAuth('recitationtask', '查看')
			},
			{
				title: '公告信息',
				desc: '发布和维护平台公告内容',
				path: '/news',
				icon: Document,
				show: btnAuth('news', '查看')
			}
		];
		return list.filter(item => item.show);
	});

	const heroSummaryList = computed(() => [
		{
			label: '当前身份',
			value: roleName
		},
		{
			label: '可见模块',
			value: `${quickEntryList.value.length} 个`
		},
		{
			label: '统计类型',
			value: `${statCards.value.length} 类`
		}
	]);

	const heroSideList = computed(() => [
		{
			label: '用户与教师',
			value: `${studentCount.value + teacherCount.value} 个账号`
		},
		{
			label: '成绩沉淀',
			value: `${transcriptCount.value} 条记录`
		}
	]);

	const statusCards = computed(() => [
		{
			label: '首页概览',
			value: `${statCards.value.length} 项`,
			desc: '自动按权限展示核心统计卡片',
			icon: Bell
		},
		{
			label: '快捷入口',
			value: `${quickEntryList.value.length} 个`,
			desc: '高频模块集中呈现，减少跳转成本',
			icon: Reading
		},
		{
			label: '当前日期',
			value: todayText.value,
			desc: '工作台信息与时间状态同步更新',
			icon: Calendar
		}
	]);

	const managementFocusList = computed(() => [
		{
			title: '账号管理',
			desc: '集中查看用户与教师账号规模，便于日常维护。',
			value: `${studentCount.value + teacherCount.value} 个`,
			icon: User
		},
		{
			title: '内容维护',
			desc: '关注古诗词与背诵任务模块，保持学习内容持续更新。',
			value: btnAuth('course', '查看') || btnAuth('recitationtask', '查看') ? '已开放' : '待授权',
			icon: Reading
		},
		{
			title: '成绩跟踪',
			desc: '通过成绩记录与图表分布快速了解近期学习情况。',
			value: `${transcriptCount.value} 条`,
			icon: Histogram
		}
	]);

	const recommendedList = computed(() => quickEntryList.value.slice(0, 4));

	const goPage = (path) => {
		router.push(path);
	};

	const getstudentCount = () => {
		context?.$http({
			url: 'student/count',
			method: 'get'
		}).then(res => {
			studentCount.value = Number(res.data.data || 0);
		});
	};

	const getteacherCount = () => {
		context?.$http({
			url: 'teacher/count',
			method: 'get'
		}).then(res => {
			teacherCount.value = Number(res.data.data || 0);
		});
	};

	const gettranscriptCount = () => {
		context?.$http({
			url: 'transcript/count',
			method: 'get'
		}).then(res => {
			transcriptCount.value = Number(res.data.data || 0);
		});
	};

	const resizeChart = () => {
		if (chartInstance.value) {
			chartInstance.value.resize();
		}
	};

	const gettranscriptChart1 = () => {
		nextTick(() => {
			const chartDom = document.getElementById('transcriptkaoshichengjiEchart1');
			if (!chartDom || !echarts) {
				return;
			}
			if (!chartInstance.value) {
				chartInstance.value = echarts.init(chartDom, 'macarons');
			}
			context?.$http({
				url: 'transcript/sectionStat/kaoshichengji',
				method: 'get'
			}).then(obj => {
				const res = obj.data.data || [];
				const xAxis = [];
				const yAxis = [];
				for (let i = 0; i < res.length; i++) {
					xAxis.push(res[i].kaoshichengji);
					yAxis.push(parseFloat(res[i].total));
				}
				chartInstance.value.setOption({
					grid: {
						left: '4%',
						right: '4%',
						bottom: '8%',
						top: '18%',
						containLabel: true
					},
					tooltip: {
						trigger: 'axis',
						axisPointer: {
							type: 'shadow'
						}
					},
					xAxis: {
						type: 'category',
						data: xAxis,
						axisLabel: {
							color: '#5b6475',
							rotate: 28
						},
						axisLine: {
							lineStyle: {
								color: '#cfd7e6'
							}
						}
					},
					yAxis: {
						type: 'value',
						axisLabel: {
							color: '#5b6475'
						},
						splitLine: {
							lineStyle: {
								color: 'rgba(90, 105, 130, 0.12)'
							}
						}
					},
					series: [{
						name: '成绩数量',
						data: yAxis,
						type: 'bar',
						barWidth: 34,
						itemStyle: {
							borderRadius: [12, 12, 0, 0],
							color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
								{ offset: 0, color: '#4f8cff' },
								{ offset: 1, color: '#67d4ff' }
							])
						}
					}]
				});
				resizeChart();
			});
		});
	};

	const init = () => {
		if (btnAuth('student', '首页总数')) {
			getstudentCount();
		}
		if (btnAuth('teacher', '首页总数')) {
			getteacherCount();
		}
		if (btnAuth('transcript', '首页总数')) {
			gettranscriptCount();
		}
		if (btnAuth('transcript', '首页统计')) {
			gettranscriptChart1();
		}
	};

	let timeTimer = null;

	onMounted(() => {
		init();
		window.addEventListener('resize', resizeChart);
		timeTimer = setInterval(() => {
			currentTime.value = context?.$toolUtil.getCurDateTime();
		}, 1000);
	});

	onBeforeUnmount(() => {
		window.removeEventListener('resize', resizeChart);
		if (timeTimer) {
			clearInterval(timeTimer);
		}
		if (chartInstance.value) {
			chartInstance.value.dispose();
			chartInstance.value = null;
		}
	});
</script>

<style lang="scss">
	.home_view {
		min-height: 100vh;
		padding: 24px;
		box-sizing: border-box;
		background:
			radial-gradient(circle at top left, rgba(90, 160, 255, 0.18), transparent 28%),
			radial-gradient(circle at right top, rgba(35, 211, 193, 0.18), transparent 24%),
			linear-gradient(180deg, #f4f8ff 0%, #edf3fb 100%);
	}

	.hero_panel,
	.panel_card {
		position: relative;
		overflow: hidden;
		border: 1px solid rgba(116, 143, 181, 0.16);
		border-radius: 24px;
		box-shadow: 0 18px 40px rgba(31, 52, 88, 0.08);
		background: rgba(255, 255, 255, 0.92);
		backdrop-filter: blur(12px);
	}

	.hero_panel {
		padding: 34px 36px;
		margin-bottom: 24px;
		display: grid;
		grid-template-columns: minmax(0, 2fr) minmax(280px, 1fr);
		gap: 24px;
		background:
			linear-gradient(135deg, rgba(32, 84, 201, 0.96), rgba(55, 164, 255, 0.90)),
			linear-gradient(180deg, #ffffff, #ffffff);
		color: #fff;
	}

	.hero_badge {
		display: inline-flex;
		align-items: center;
		padding: 7px 14px;
		border-radius: 999px;
		background: rgba(255, 255, 255, 0.16);
		font-size: 13px;
		letter-spacing: 1px;
		margin-bottom: 18px;
	}

	.hero_title {
		margin: 0 0 12px;
		font-size: 34px;
		line-height: 1.2;
		font-weight: 700;
	}

	.hero_desc {
		margin: 0;
		max-width: 760px;
		font-size: 15px;
		line-height: 1.8;
		color: rgba(255, 255, 255, 0.88);
	}

	.hero_meta {
		margin-top: 24px;
		display: flex;
		flex-wrap: wrap;
		gap: 12px;
	}

	.meta_chip {
		display: inline-flex;
		align-items: center;
		gap: 8px;
		padding: 10px 14px;
		border-radius: 14px;
		background: rgba(255, 255, 255, 0.14);
		font-size: 14px;
	}

	.hero_summary {
		margin-top: 24px;
		display: grid;
		grid-template-columns: repeat(3, minmax(0, 1fr));
		gap: 14px;
	}

	.hero_summary_item {
		padding: 16px 18px;
		border-radius: 18px;
		background: rgba(255, 255, 255, 0.12);
		border: 1px solid rgba(255, 255, 255, 0.14);
	}

	.hero_summary_label {
		font-size: 13px;
		color: rgba(255, 255, 255, 0.76);
	}

	.hero_summary_value {
		margin-top: 8px;
		font-size: 22px;
		font-weight: 700;
	}

	.hero_side {
		position: relative;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		min-height: 220px;
		gap: 16px;
	}

	.hero_stat {
		position: relative;
		z-index: 2;
		width: 100%;
		max-width: 280px;
		padding: 28px 24px;
		border-radius: 24px;
		background: rgba(255, 255, 255, 0.14);
		border: 1px solid rgba(255, 255, 255, 0.18);
		box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.2);
	}

	.hero_stat_label {
		font-size: 14px;
		color: rgba(255, 255, 255, 0.82);
	}

	.hero_stat_value {
		margin: 14px 0 10px;
		font-size: 48px;
		font-weight: 700;
		line-height: 1;
	}

	.hero_stat_tip {
		font-size: 13px;
		color: rgba(255, 255, 255, 0.78);
	}

	.hero_side_list {
		position: relative;
		z-index: 2;
		width: 100%;
		max-width: 280px;
		display: grid;
		gap: 10px;
	}

	.hero_side_item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16px;
		padding: 13px 16px;
		border-radius: 16px;
		background: rgba(8, 31, 86, 0.16);
		border: 1px solid rgba(255, 255, 255, 0.12);
		font-size: 13px;
		color: rgba(255, 255, 255, 0.82);
	}

	.hero_side_item strong {
		font-size: 14px;
		color: #fff;
	}

	.hero_glow {
		position: absolute;
		border-radius: 50%;
		filter: blur(8px);
	}

	.hero_glow_one {
		width: 140px;
		height: 140px;
		right: 20px;
		top: 12px;
		background: rgba(255, 255, 255, 0.18);
	}

	.hero_glow_two {
		width: 92px;
		height: 92px;
		left: 12px;
		bottom: 18px;
		background: rgba(111, 232, 255, 0.34);
	}

	.section_head {
		display: flex;
		align-items: flex-end;
		justify-content: space-between;
		gap: 20px;
		margin-bottom: 18px;
	}

	.section_head.compact {
		align-items: center;
		margin-bottom: 20px;
	}

	.section_head h2 {
		margin: 6px 0 0;
		font-size: 24px;
		color: #21304a;
	}

	.section_head p {
		margin: 0;
		max-width: 460px;
		font-size: 14px;
		line-height: 1.7;
		color: #6b768a;
		text-align: right;
	}

	.section_tag {
		font-size: 12px;
		font-weight: 700;
		color: #4f8cff;
		letter-spacing: 0.8px;
		text-transform: uppercase;
	}

	.overview_section {
		margin-bottom: 24px;
	}

	.status_strip {
		display: grid;
		grid-template-columns: repeat(3, minmax(0, 1fr));
		gap: 18px;
		margin-bottom: 24px;
	}

	.status_card {
		display: flex;
		align-items: center;
		gap: 16px;
		padding: 22px 24px;
		border-radius: 22px;
		background: rgba(255, 255, 255, 0.9);
		border: 1px solid rgba(116, 143, 181, 0.14);
		box-shadow: 0 12px 30px rgba(31, 52, 88, 0.06);
	}

	.status_icon {
		width: 52px;
		height: 52px;
		border-radius: 16px;
		display: flex;
		align-items: center;
		justify-content: center;
		background: linear-gradient(135deg, #4f8cff 0%, #70d4ff 100%);
		color: #fff;
		font-size: 24px;
		flex-shrink: 0;
	}

	.status_info {
		min-width: 0;
	}

	.status_label {
		font-size: 13px;
		color: #6d7a8f;
	}

	.status_value {
		margin: 6px 0 4px;
		font-size: 20px;
		font-weight: 700;
		color: #22314d;
	}

	.status_desc {
		font-size: 13px;
		line-height: 1.6;
		color: #7b879a;
	}

	.count_grid {
		display: grid;
		grid-template-columns: repeat(3, minmax(0, 1fr));
		gap: 18px;
	}

	.stat_card {
		position: relative;
		display: flex;
		align-items: center;
		gap: 18px;
		padding: 24px;
		border-radius: 22px;
		overflow: hidden;
		box-shadow: 0 12px 30px rgba(29, 53, 87, 0.08);
	}

	.stat_card::after {
		content: '';
		position: absolute;
		right: -22px;
		top: -22px;
		width: 96px;
		height: 96px;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.18);
	}

	.theme_blue {
		background: linear-gradient(135deg, #4d8dff 0%, #79b8ff 100%);
	}

	.theme_orange {
		background: linear-gradient(135deg, #ff8a47 0%, #ffbf66 100%);
	}

	.theme_green {
		background: linear-gradient(135deg, #20b783 0%, #5dd39e 100%);
	}

	.stat_icon {
		position: relative;
		z-index: 1;
		width: 62px;
		height: 62px;
		border-radius: 18px;
		display: flex;
		align-items: center;
		justify-content: center;
		background: rgba(255, 255, 255, 0.18);
		font-size: 28px;
		color: #fff;
	}

	.stat_info {
		position: relative;
		z-index: 1;
		color: #fff;
	}

	.stat_label {
		font-size: 15px;
		opacity: 0.92;
	}

	.stat_value {
		margin: 8px 0 6px;
		font-size: 34px;
		line-height: 1;
		font-weight: 700;
	}

	.stat_note {
		font-size: 13px;
		opacity: 0.84;
	}

	.content_grid {
		display: grid;
		grid-template-columns: minmax(0, 1.5fr) minmax(320px, 1fr);
		gap: 24px;
		margin-bottom: 24px;
	}

	.support_grid {
		display: grid;
		grid-template-columns: minmax(0, 1.2fr) minmax(0, 1fr);
		gap: 24px;
		margin-bottom: 24px;
	}

	.panel_card {
		padding: 24px;
	}

	.quick_grid {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 16px;
	}

	.quick_item {
		padding: 20px;
		border-radius: 20px;
		background: linear-gradient(180deg, #f8fbff 0%, #eef5ff 100%);
		border: 1px solid rgba(79, 140, 255, 0.12);
		cursor: pointer;
		transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
	}

	.quick_item:hover {
		transform: translateY(-4px);
		box-shadow: 0 14px 28px rgba(66, 109, 177, 0.12);
		border-color: rgba(79, 140, 255, 0.28);
	}

	.quick_icon {
		width: 48px;
		height: 48px;
		border-radius: 15px;
		display: flex;
		align-items: center;
		justify-content: center;
		background: linear-gradient(135deg, #4f8cff, #70d4ff);
		color: #fff;
		font-size: 22px;
		margin-bottom: 16px;
	}

	.quick_title {
		font-size: 18px;
		font-weight: 700;
		color: #25314b;
		margin-bottom: 8px;
	}

	.quick_desc {
		min-height: 44px;
		font-size: 14px;
		line-height: 1.6;
		color: #667287;
	}

	.quick_link {
		margin-top: 14px;
		display: inline-flex;
		align-items: center;
		gap: 6px;
		color: #3e7bf6;
		font-weight: 600;
	}

	.insight_list {
		display: flex;
		flex-direction: column;
		gap: 14px;
	}

	.insight_item {
		display: flex;
		justify-content: space-between;
		gap: 20px;
		padding: 14px 16px;
		border-radius: 16px;
		background: #f6f9fd;
		color: #5f6b80;
	}

	.insight_item strong {
		color: #25314b;
		text-align: right;
	}

	.insight_footer {
		margin-top: 18px;
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 14px;
	}

	.footer_card {
		padding: 18px 16px;
		border-radius: 18px;
		background: linear-gradient(180deg, #f7fbff 0%, #edf4ff 100%);
		border: 1px solid rgba(79, 140, 255, 0.12);
	}

	.footer_label {
		font-size: 13px;
		color: #708097;
	}

	.footer_value {
		margin-top: 8px;
		font-size: 24px;
		font-weight: 700;
		color: #22314d;
		word-break: break-word;
	}

	.footer_value.small {
		font-size: 18px;
	}

	.flow_list {
		display: grid;
		gap: 16px;
	}

	.flow_item {
		padding: 18px;
		border-radius: 20px;
		background: linear-gradient(180deg, #fbfdff 0%, #f1f6fe 100%);
		border: 1px solid rgba(116, 143, 181, 0.12);
	}

	.flow_top {
		display: grid;
		grid-template-columns: 52px minmax(0, 1fr) auto;
		align-items: center;
		gap: 14px;
	}

	.flow_icon {
		width: 52px;
		height: 52px;
		border-radius: 16px;
		display: flex;
		align-items: center;
		justify-content: center;
		background: linear-gradient(135deg, #e9f2ff 0%, #dcecff 100%);
		color: #3e7bf6;
		font-size: 24px;
	}

	.flow_text {
		min-width: 0;
	}

	.flow_title {
		font-size: 17px;
		font-weight: 700;
		color: #24314a;
	}

	.flow_desc {
		margin-top: 6px;
		font-size: 13px;
		line-height: 1.7;
		color: #6d7990;
	}

	.flow_value {
		padding: 8px 12px;
		border-radius: 999px;
		background: #edf4ff;
		color: #2e67d1;
		font-size: 13px;
		font-weight: 700;
		white-space: nowrap;
	}

	.focus_grid {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 16px;
	}

	.focus_item {
		padding: 18px;
		border-radius: 20px;
		background: linear-gradient(180deg, #ffffff 0%, #f5f9ff 100%);
		border: 1px solid rgba(116, 143, 181, 0.12);
		cursor: pointer;
		transition: transform 0.2s ease, box-shadow 0.2s ease;
	}

	.focus_item:hover {
		transform: translateY(-4px);
		box-shadow: 0 14px 28px rgba(66, 109, 177, 0.12);
	}

	.focus_icon {
		width: 46px;
		height: 46px;
		border-radius: 14px;
		display: flex;
		align-items: center;
		justify-content: center;
		background: linear-gradient(135deg, #4f8cff 0%, #6bc8ff 100%);
		color: #fff;
		font-size: 22px;
		margin-bottom: 14px;
	}

	.focus_title {
		font-size: 16px;
		font-weight: 700;
		color: #25314b;
	}

	.focus_desc {
		margin-top: 8px;
		font-size: 13px;
		line-height: 1.7;
		color: #6a768b;
	}

	.chart_panel {
		padding-bottom: 12px;
	}

	.chart_wrap {
		padding: 10px 0 0;
	}

	.chart_canvas {
		width: 100%;
		height: 420px;
	}

	@media (max-width: 1200px) {
		.hero_summary,
		.status_strip,
		.count_grid,
		.focus_grid,
		.quick_grid,
		.insight_footer {
			grid-template-columns: repeat(2, minmax(0, 1fr));
		}

		.content_grid,
		.support_grid {
			grid-template-columns: 1fr;
		}
	}

	@media (max-width: 768px) {
		.home_view {
			padding: 16px;
		}

		.hero_panel {
			padding: 24px 18px;
			grid-template-columns: 1fr;
		}

		.section_head,
		.section_head.compact {
			flex-direction: column;
			align-items: flex-start;
		}

		.section_head p {
			text-align: left;
		}

		.hero_summary,
		.status_strip,
		.count_grid,
		.focus_grid,
		.quick_grid,
		.insight_footer {
			grid-template-columns: 1fr;
		}

		.flow_top {
			grid-template-columns: 52px minmax(0, 1fr);
		}

		.flow_value {
			grid-column: 1 / -1;
			justify-self: flex-start;
		}

		.stat_card {
			padding: 20px;
		}

		.hero_title {
			font-size: 28px;
		}

		.chart_canvas {
			height: 340px;
		}
	}
</style>
