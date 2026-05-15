<template>
  <div class="dashboard">
    <div class="welcome_bar">
      <h2>欢迎，{{ dashboard.teachername }}老师</h2>
      <span class="grade_tag">{{ dashboard.grade }} · {{ dashboard.classname }}</span>
    </div>

    <div class="stat_cards">
      <div class="stat_card students">
        <div class="stat_icon">&#x1f393;</div>
        <div class="stat_value">{{ dashboard.studentCount }}</div>
        <div class="stat_label">本班学生</div>
      </div>
      <div class="stat_card recitation">
        <div class="stat_icon">&#x1f4d6;</div>
        <div class="stat_value">{{ dashboard.recitationDone }}/{{ dashboard.recitationTotal }}</div>
        <div class="stat_label">背诵任务(已完成/总数)</div>
      </div>
      <div class="stat_card quiz">
        <div class="stat_icon">&#x270f;</div>
        <div class="stat_value">{{ dashboard.quizDone }}/{{ dashboard.quizTotal }}</div>
        <div class="stat_label">测验任务(已完成/总数)</div>
      </div>
      <div class="stat_card follow">
        <div class="stat_icon">&#x1f3a4;</div>
        <div class="stat_value">{{ dashboard.followTotal }}</div>
        <div class="stat_label">跟读记录 · 均分 {{ dashboard.followAvgScore }}</div>
      </div>
    </div>

    <div class="quick_actions">
      <h3>快捷操作</h3>
      <div class="action_btns">
        <el-button type="primary" @click="$router.push('/classinfo')">班级管理</el-button>
        <el-button type="success" @click="$router.push('/student')">学生管理</el-button>
        <el-button type="warning" @click="$router.push('/recitationtask')">发布背诵任务</el-button>
        <el-button type="danger" @click="$router.push('/quiztask')">发布测验任务</el-button>
        <el-button @click="$router.push('/course')">古诗文库</el-button>
      </div>
    </div>

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
import { ref, onMounted, getCurrentInstance } from 'vue'
const context = getCurrentInstance()?.appContext.config.globalProperties
const dashboard = ref({ teachername:'', grade:'', classname:'', studentCount:0, recitationTotal:0, recitationDone:0, quizTotal:0, quizDone:0, followTotal:0, followAvgScore:0, recentActivities:[] })

onMounted(() => {
  context?.$http({ url: 'teacher/dashboard', method: 'get' }).then(res => {
    if (res.data.code === 0) dashboard.value = res.data.data
  })
})
</script>

<style lang="scss" scoped>
.dashboard { padding: 20px; }
.welcome_bar { display:flex; align-items:center; gap:16px; margin-bottom:24px; }
.welcome_bar h2 { margin:0; color:#3f3424; }
.grade_tag { padding:4px 14px; border-radius:20px; background:linear-gradient(135deg,#fef3c7,#fde68a); color:#92400e; font-size:13px; font-weight:600; }
.stat_cards { display:grid; grid-template-columns:repeat(4,1fr); gap:16px; margin-bottom:28px; }
.stat_card { padding:20px; border-radius:16px; background:#fff; border:1px solid #efe5cd; text-align:center; }
.stat_icon { font-size:28px; margin-bottom:8px; }
.stat_value { font-size:28px; font-weight:700; color:#3f3424; }
.stat_label { font-size:13px; color:#9a8d73; margin-top:4px; }
.quick_actions { margin-bottom:28px; }
.quick_actions h3, .recent_section h3 { color:#5b503f; margin:0 0 12px; }
.action_btns { display:flex; gap:12px; flex-wrap:wrap; }
.recent_section { background:#fff; border-radius:16px; padding:20px; border:1px solid #efe5cd; }
</style>
