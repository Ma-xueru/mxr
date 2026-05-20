<template>
  <div class="dashboard">
    <div class="welcome_bar">
      <h2>欢迎，{{ dashboard.teachername }}老师</h2>
      <span class="grade_tag">管辖 {{ dashboard.classCount || 0 }} 个班级</span>
    </div>

    <!-- 三张统计卡 -->
    <div class="stat_cards">
      <div class="stat_card master_card">
        <div class="stat_header">管辖班级大盘</div>
        <div class="stat_big_value">{{ dashboard.classCount || 0 }} <small>个班</small></div>
        <div class="stat_sub">{{ dashboard.classnames || '未绑定班级' }}</div>
        <div class="stat_footer">覆盖学生 {{ dashboard.totalStudents || 0 }} 人</div>
      </div>
      <div class="stat_card ongoing_card">
        <div class="stat_header">进行中任务</div>
        <div class="stat_big_value">{{ dashboard.ongoingTasks || 0 }} <small>项</small></div>
        <div class="stat_sub">待完成背诵/测验</div>
        <div class="stat_footer">学生正在作答中</div>
      </div>
      <div class="stat_card submit_card">
        <div class="stat_header">今日提交率</div>
        <div class="stat_big_value">{{ dashboard.submitRate || 0 }}<small>%</small></div>
        <div class="stat_sub">今日截止 {{ dashboard.todayTotal || 0 }} 项，已交 {{ dashboard.todayDone || 0 }}</div>
        <div class="stat_footer">当日截止任务提交情况</div>
      </div>
    </div>

    <!-- 预警横幅 -->
    <div class="warn_bar" v-if="dashboard.warningStudents && dashboard.warningStudents.length">
      <span class="warn_icon">⚠️</span>
      <span>{{ dashboard.warningStudents.length }} 人 3 天未学习：</span>
      <span v-for="(w, i) in dashboard.warningStudents.slice(0,5)" :key="i" class="warn_name">{{ w.studentname }}({{ w.classname }}){{ i < Math.min(dashboard.warningStudents.length,5)-1 ? '、' : '' }}</span>
      <span v-if="dashboard.warningStudents.length > 5">等</span>
    </div>

    <!-- 本周趋势小条 -->
    <div class="week_bar" v-if="dashboard.thisWeekCount !== undefined">
      <span>本周自学 <b>{{ dashboard.thisWeekCount || 0 }}</b> 次</span>
      <span class="week_trend"> vs 上周 {{ dashboard.lastWeekCount || 0 }} <b :style="{color: (dashboard.weekTrend||'').startsWith('↑')?'#4CAF50':'#e88a6e'}">{{ dashboard.weekTrend }}</b></span>
    </div>

    <!-- 学生活跃度TOP10 -->
    <div class="chart_section" v-if="dashboard.topStudents && dashboard.topStudents.length">
      <h3>学生活跃度 TOP10</h3>
      <div ref="barChartRef" class="echart_box" style="height:320px"></div>
    </div>

    <!-- 今日学习动态 -->
    <div class="feed_section" v-if="dashboard.todayActivities && dashboard.todayActivities.length">
      <h3>今日学习动态</h3>
      <div class="feed_list">
        <div class="feed_item" v-for="(item, idx) in dashboard.todayActivities.slice(0,10)" :key="idx">
          <span class="feed_dot" :style="{background: item.type==='跟读'?'#4CAF50':item.type==='测验'?'#FF9800':item.type==='举一反三'?'#9C27B0':'#2196F3'}"></span>
          <span class="feed_name">{{ item.studentname }}</span>
          <span class="feed_action">{{ item.type }}《{{ item.poetryTitle }}》</span>
          <span class="feed_score">{{ item.score }}分</span>
          <span class="feed_time">{{ item.timeAgo }}</span>
        </div>
      </div>
    </div>

    <!-- 班级达标率排行 -->
    <div class="chart_section" v-if="dashboard.classStats && dashboard.classStats.length">
      <h3>所带班级背诵达标率排行</h3>
      <div class="bar_chart">
        <div class="bar_item" v-for="(item, index) in dashboard.classStats" :key="index">
          <div class="bar_label">{{ item.classname }}</div>
          <div class="bar_track">
            <div class="bar_fill" :style="{ width: item.passRate + '%', background: barColor(item.passRate) }"></div>
            <span class="bar_text">{{ item.passRate }}% ({{ item.done }}/{{ item.total }})</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick_actions">
      <h3>快捷操作</h3>
      <div class="action_btns">
        <el-button type="primary" @click="$router.push('/classinfo')">班级管理</el-button>
        <el-button type="success" @click="$router.push('/student')">学生管理</el-button>
        <el-button type="warning" @click="$router.push('/recitationtask')">发布背诵任务</el-button>
        <el-button type="danger" @click="$router.push('/quiztask')">发布测验任务</el-button>
        <el-button @click="$router.push('/course')">古诗词查看</el-button>
      </div>
    </div>

    <!-- 最近活动 -->
    <div class="recent_section" v-if="dashboard.recentActivities && dashboard.recentActivities.length">
      <h3>最近活动</h3>
      <el-table :data="dashboard.recentActivities" border size="small">
        <el-table-column prop="type" label="类型" width="70">
          <template #default="s"><el-tag :type="s.row.type==='测验'?'danger':'warning'" size="small">{{ s.row.type }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="student" label="学生" width="100"></el-table-column>
        <el-table-column prop="title" label="任务" min-width="160"></el-table-column>
        <el-table-column prop="status" label="状态" width="90"></el-table-column>
        <el-table-column prop="score" label="分数" width="70"></el-table-column>
        <el-table-column prop="time" label="时间" width="170"></el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance, nextTick } from 'vue'
import * as echarts from 'echarts'
const context = getCurrentInstance()?.appContext.config.globalProperties
const dashboard = ref({ teachername:'', classCount:0, classnames:'', totalStudents:0, ongoingTasks:0, submitRate:0, todayTotal:0, todayDone:0, classStats:[], recentActivities:[], modulePie:[], trend7:[], topStudents:[], todayActivities:[] })

const barChartRef = ref(null)
let barChart

const barColor = (rate) => {
  if (rate >= 80) return 'linear-gradient(90deg, #58B86F, #82D68F)'
  if (rate >= 60) return 'linear-gradient(90deg, #F6A63A, #FFD36A)'
  return 'linear-gradient(90deg, #E85D5D, #FF8A72)'
}

const renderCharts = () => {
  nextTick(() => {
    const d = dashboard.value
    if (!echarts) return

    // 条形图 — TOP10
    if (barChartRef.value && d.topStudents && d.topStudents.length) {
      if (barChart) barChart.dispose()
      barChart = echarts.init(barChartRef.value)
      const names = d.topStudents.map(i => i.name).reverse()
      const counts = d.topStudents.map(i => i.count).reverse()
      barChart.setOption({
        grid: { top:4, right:20, bottom:20, left:80 },
        xAxis: { type:'value', axisLabel:{fontSize:10} },
        yAxis: { type:'category', data: names, axisLabel:{fontSize:11} },
        series: [{ type:'bar', data: counts, barWidth:16,
          itemStyle: { borderRadius:[0,6,6,0], color: { type:'linear',x:0,y:0,x2:1,y2:0,
            colorStops: [{offset:0,color:'#81C784'},{offset:1,color:'#4CAF50'}] } } }]
      })
    }
  })
}

onMounted(() => {
  context?.$http({ url: 'teacher/dashboard', method: 'get' }).then(res => {
    if (res.data.code === 0) {
      dashboard.value = res.data.data
      setTimeout(() => renderCharts(), 300)
    }
  })
})
</script>

<style lang="scss" scoped>
.dashboard { padding: 20px; }
.welcome_bar { display:flex; align-items:center; gap:16px; margin-bottom:24px; }
.welcome_bar h2 { margin:0; color:#3f3424; }
.grade_tag { padding:4px 14px; border-radius:20px; background:linear-gradient(135deg,#fef3c7,#fde68a); color:#92400e; font-size:13px; font-weight:600; }

.stat_cards { display:grid; grid-template-columns:repeat(3,1fr); gap:16px; margin-bottom:16px; }

.warn_bar { background:linear-gradient(135deg,#FFF3E0,#FFE0B2); border:1px solid #FFB74D; border-radius:12px; padding:12px 18px; margin-bottom:14px; font-size:13px; color:#e65100; display:flex; align-items:center; flex-wrap:wrap; gap:4px; }
.warn_icon { font-size:18px; }
.warn_name { font-weight:600; }

.week_bar { display:flex; justify-content:space-between; align-items:center; padding:8px 18px; margin-bottom:14px; background:#fff; border-radius:12px; border:1px solid #efe5cd; font-size:13px; color:#5b503f; }
.week_trend { font-size:13px; }
.stat_card { padding:24px; border-radius:16px; border:1px solid #efe5cd; }
.stat_header { font-size:13px; color:#9a8d73; margin-bottom:8px; font-weight:500; }
.stat_big_value { font-size:36px; font-weight:700; color:#3f3424; }
.stat_big_value small { font-size:16px; font-weight:400; color:#9a8d73; }
.stat_sub { font-size:13px; color:#6b5f4a; margin-top:6px; line-height:1.6; }
.stat_footer { font-size:12px; color:#b0a58a; margin-top:8px; padding-top:8px; border-top:1px solid #f0ead6; }

.master_card { background:linear-gradient(135deg,#f0f7ff,#e8f0fe); }
.ongoing_card { background:linear-gradient(135deg,#fff8e7,#fef3c7); }
.submit_card { background:linear-gradient(135deg,#f0fbf1,#e6f6e8); }

.chart_section { background:#fff; border-radius:16px; padding:24px; margin-bottom:28px; border:1px solid #efe5cd; }
.chart_section h3 { margin:0 0 20px; color:#3f3424; }
.bar_chart { display:flex; flex-direction:column; gap:14px; }
.bar_item { display:flex; align-items:center; gap:12px; }
.bar_label { width:110px; font-size:14px; color:#5b503f; font-weight:500; text-align:right; }
.bar_track { flex:1; height:32px; background:#f5f0e0; border-radius:8px; position:relative; overflow:hidden; display:flex; align-items:center; }
.bar_fill { height:100%; border-radius:8px; transition:width .6s ease; min-width:2px; }
.bar_text { position:absolute; left:12px; font-size:12px; color:#3f3424; font-weight:600; }

.quick_actions { margin-bottom:28px; }
.quick_actions h3, .recent_section h3 { color:#5b503f; margin:0 0 12px; }
.action_btns { display:flex; gap:12px; flex-wrap:wrap; }
.recent_section { background:#fff; border-radius:16px; padding:20px; border:1px solid #efe5cd; }

.feed_section { background:#fff; border-radius:16px; padding:20px; margin-bottom:28px; border:1px solid #efe5cd; }
.feed_section h3 { margin:0 0 14px; color:#3f3424; }
.feed_list { display:flex; flex-direction:column; gap:10px; }
.feed_item { display:flex; align-items:center; gap:10px; padding:10px 0; border-bottom:1px solid #f5f0e0; font-size:13px; color:#5b503f; }
.feed_item:last-child { border-bottom:none; }
.feed_dot { width:8px; height:8px; border-radius:50%; flex-shrink:0; }
.feed_name { font-weight:600; color:#3f3424; min-width:50px; }
.feed_action { flex:1; color:#6d5d40; }
.feed_score { font-weight:700; }
.feed_time { color:#aaa; font-size:12px; min-width:60px; text-align:right; }
</style>
