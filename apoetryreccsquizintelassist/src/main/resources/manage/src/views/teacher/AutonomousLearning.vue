<template>
  <div>
    <div class="app-contain">
      <div class="overview_panel">
        <div class="overview_header">
          <div>
            <div class="overview_badge">自主学习大盘</div>
            <div class="overview_title">自主学习管理</div>
            <div class="overview_desc">按学生聚合展示：自主跟读 · 自主测验 · 举一反三 · 温故知新</div>
          </div>
        </div>
        <div class="overview_cards">
          <div class="overview_card"><div class="overview_value">{{ overview.totalAll || 0 }}</div><div class="overview_label">总自学人次</div></div>
          <div class="overview_card warm"><div class="overview_value">{{ overview.totalFollow || 0 }}</div><div class="overview_label">自主跟读</div></div>
          <div class="overview_card green"><div class="overview_value">{{ overview.totalQuiz || 0 }}</div><div class="overview_label">自主测验</div></div>
          <div class="overview_card purple"><div class="overview_value">{{ overview.totalAnalogy || 0 }}</div><div class="overview_label">举一反三</div></div>
          <div class="overview_card blue"><div class="overview_value">{{ overview.totalReview || 0 }}</div><div class="overview_label">温故知新</div></div>
        </div>
      </div>

      <el-table v-loading="loading" :data="studentList" border stripe>
        <el-table-column label="序号" width="60" align="center">
          <template #default="scope">{{ scope.$index + 1 }}</template>
        </el-table-column>
        <el-table-column prop="classname" label="班级" width="100"></el-table-column>
        <el-table-column prop="studentname" label="学生姓名" width="100"></el-table-column>
        <el-table-column prop="studentaccount" label="账号" min-width="110"></el-table-column>
        <el-table-column prop="followCount" label="跟读" width="70" align="center">
          <template #default="s"><el-tag type="info" size="small">{{ s.row.followCount || 0 }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="quizCount" label="测验" width="70" align="center">
          <template #default="s"><el-tag type="warning" size="small">{{ s.row.quizCount || 0 }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="analogyCount" label="举一反三" width="80" align="center">
          <template #default="s"><el-tag type="danger" size="small">{{ s.row.analogyCount || 0 }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="reviewCount" label="温故知新" width="80" align="center">
          <template #default="s"><el-tag type="success" size="small">{{ s.row.reviewCount || 0 }}</el-tag></template>
        </el-table-column>
        <el-table-column label="最近自学时间" width="160">
          <template #default="s">{{ fmtTime(s.row.lastActiveTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="s">
            <el-button type="primary" size="small" @click="openDrawer(s.row)">调阅自学明细</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-drawer v-model="drawerVisible" :title="drawerStudent + ' 的自主自学全量轨迹'" size="70%" destroy-on-close>
      <el-tabs v-model="activeTab" @tab-change="tabChange">
        <el-tab-pane label="🗣️ 自主跟读" name="follow"></el-tab-pane>
        <el-tab-pane label="✍️ 自主测验" name="quiz"></el-tab-pane>
        <el-tab-pane label="🔄 举一反三" name="analogy"></el-tab-pane>
        <el-tab-pane label="📚 温故知新" name="review"></el-tab-pane>
      </el-tabs>

      <el-row :gutter="16" v-loading="historyLoading">
        <el-col :span="10">
          <div class="history_list_title">{{ tabLabel }}记录（{{ historyList.length }} 条）</div>
          <el-table :data="historyList" highlight-current-row @row-click="selectRecord"
            :row-class-name="rowCls" max-height="55vh" size="small" v-if="historyList.length">
            <el-table-column prop="poetryTitle" label="古诗/模块" min-width="120"></el-table-column>
            <el-table-column prop="createTime" label="时间" width="140">
              <template #default="s">{{ fmtTime(s.row.createTime) }}</template>
            </el-table-column>
            <el-table-column prop="score" label="得分" width="70">
              <template #default="s">
                <span :style="{color:s.row.score>=85?'#4CAF50':s.row.score>=60?'#FF9800':'#F44336',fontWeight:700}">{{ s.row.score }}</span>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无记录" />
        </el-col>

        <el-col :span="14">
          <div class="diagnosis_panel" v-if="selectedRecord">
            <div class="radar_section">
              <div class="section_subtitle">📊 核心能力指标</div>
              <div ref="radarChartRef" class="radar_chart"></div>
            </div>

            <div class="audio_section" v-if="activeTab === 'follow' && selectedRecord.audioUrl">
              <div class="section_subtitle">🎙️ 跟读录音</div>
              <audio :src="selectedRecord.audioUrl" controls style="width:100%"></audio>
            </div>

            <div class="text_diagnosis">
              <div class="dim_cards">
                <div class="dim_card" v-for="(d, idx) in parsedDims" :key="idx">
                  <div class="dim_card_head">
                    <span class="dim_card_name">{{ d.name }}</span>
                    <span class="dim_card_score" :style="{color: colorByScore(d.score)}">{{ d.score }}分</span>
                  </div>
                  <el-progress :percentage="d.score" :stroke-width="6" :color="progressColor(d.score)" />
                  <div class="dim_comment" v-if="d.comment">{{ d.comment }}</div>
                </div>
              </div>

              <div class="suggestion_box" v-if="parsedReport && parsedReport.suggestion">
                <div class="sug_title">🤖 智能专家学习建议</div>
                <div class="sug_content">{{ parsedReport.suggestion }}</div>
              </div>

              <div class="overall_box" v-if="parsedReport && parsedReport.overallComment">
                <div class="ov_title">📝 整体总评</div>
                <div class="ov_content">{{ parsedReport.overallComment }}</div>
              </div>
            </div>
          </div>
          <el-empty v-else description="点击左侧记录查看诊断详情" />
        </el-col>
      </el-row>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, getCurrentInstance, nextTick, inject } from 'vue'
const context = getCurrentInstance()?.appContext.config.globalProperties
const echarts = inject('echarts')

const loading = ref(false)
const studentList = ref([])
const overview = ref({ totalAll: 0, totalFollow: 0, totalQuiz: 0, totalAnalogy: 0, totalReview: 0 })

const drawerVisible = ref(false); const drawerStudent = ref('')
const activeTab = ref('follow'); const historyLoading = ref(false)
const historyList = ref([]); const selectedRecord = ref(null)
const radarChartRef = ref(null); let radarChart = null

const tabLabel = computed(() => {
  const m = { follow: '跟读', quiz: '测验', analogy: '举一反三', review: '温故知新' }
  return m[activeTab.value] || ''
})

const fmtTime = (v) => {
  if (!v) return '-'
  const d = new Date(v); if (isNaN(d.getTime())) return '-'
  const pad = (n) => String(n).padStart(2, '0')
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes())
}
const parsedReport = computed(() => {
  const r = selectedRecord.value
  if (!r) return null
  if (r.reportJson) {
    try { const p = JSON.parse(r.reportJson); if (p.dimensions) return p } catch(e) {}
  }
  return null
})
const parsedDims = computed(() => {
  if (parsedReport.value) return parsedReport.value.dimensions
  // 兜底：用列存分数构建3维
  const r = selectedRecord.value
  if (!r) return []
  return [
    { name: '知识掌握度', score: r.knowledgeScore || 0, comment: '' },
    { name: '答题准确率', score: r.accuracyScore || 0, comment: '' },
    { name: '理解深度', score: r.depthScore || 0, comment: '' }
  ]
})
const colorByScore = (s) => s >= 80 ? '#4CAF50' : s >= 60 ? '#FF9800' : '#F44336'
const progressColor = (s) => s >= 80 ? '#6fc47d' : s >= 60 ? '#e7ba63' : '#e88a6e'

const loadStudents = () => {
  loading.value = true
  context?.$http({ url: 'teacher/autonomousStudents', method: 'get' }).then(res => {
    if (res.data && res.data.code === 0) {
      studentList.value = res.data.data.list || []
      overview.value = {
        totalAll: res.data.data.totalAll || 0,
        totalFollow: res.data.data.totalFollow || 0,
        totalQuiz: res.data.data.totalQuiz || 0,
        totalAnalogy: res.data.data.totalAnalogy || 0,
        totalReview: res.data.data.totalReview || 0
      }
    }
  }).finally(() => loading.value = false)
}

const openDrawer = (row) => {
  drawerStudent.value = row.studentname || row.studentaccount
  drawerVisible.value = true
  activeTab.value = 'follow'
  loadHistory(row.studentaccount, 4)
}

let selectedAccount = ''
const tabChange = (name) => {
  if (!selectedAccount) return
  const typeMap = { follow: 4, quiz: 6, analogy: 7, review: 8 }
  loadHistory(selectedAccount, typeMap[name] || 4)
}
const loadHistory = (account, sourceType) => {
  selectedAccount = account
  historyLoading.value = true; historyList.value = []; selectedRecord.value = null
  context?.$http({ url: 'teacher/autonomousHistory', method: 'get', params: { studentaccount: account, sourceType } }).then(res => {
    if (res.data && res.data.code === 0) {
      historyList.value = res.data.data || []
      if (historyList.value.length) selectedRecord.value = historyList.value[0]
      nextTick(() => { setTimeout(() => renderRadar(), 200) })
    }
  }).finally(() => historyLoading.value = false)
}

const selectRecord = (row) => {
  selectedRecord.value = row
  nextTick(() => { setTimeout(() => renderRadar(), 200) })
}

const rowCls = ({ row }) => selectedRecord.value && row.id === selectedRecord.value.id ? 'current-row' : ''

const renderRadar = () => {
  if (!radarChartRef.value || !echarts) return
  if (radarChart) { radarChart.dispose(); radarChart = null }
  radarChart = echarts.init(radarChartRef.value)
  const dims = parsedDims.value
  if (!dims.length) return
  radarChart.setOption({
    radar: {
      center: ['50%', '55%'], radius: '62%',
      indicator: dims.map(d => ({ name: d.name + '\n' + d.score + '分', max: 100 })),
      axisName: { color: '#3f3424', fontSize: 13, fontWeight: 'bold', borderRadius: 3, padding: [3, 6] }
    },
    series: [{
      type: 'radar',
      data: [{ value: dims.map(d => d.score), name: '',
        areaStyle: { color: 'rgba(109,190,114,.22)' }, lineStyle: { color: '#6fc47d', width: 3 },
        itemStyle: { color: '#6fc47d' }, symbolSize: 8 }]
    }]
  })
}

onMounted(() => loadStudents())
</script>

<style lang="scss" scoped>
.overview_panel { padding: 20px 24px; margin-bottom: 16px; background: #fffdf7; border-radius: 16px; border: 1px solid #efe5cd; }
.overview_badge { display: inline-block; padding: 4px 12px; border-radius: 8px; background: #7B1FA220; color: #7B1FA2; font-size: 12px; font-weight: 700; margin-bottom: 8px; }
.overview_title { font-size: 22px; font-weight: 700; color: #3f3424; }
.overview_desc { font-size: 13px; color: #9a8d73; margin-top: 4px; }
.overview_cards { display: flex; gap: 12px; margin-top: 16px; flex-wrap: wrap; }
.overview_card { flex: 1; min-width: 100px; padding: 12px 14px; border-radius: 12px; background: #faf7f0; text-align: center; }
.overview_card.warm { background: #fff8e1; }
.overview_card.green { background: #e8f5e9; }
.overview_card.purple { background: #f3e5f5; }
.overview_card.blue { background: #e3f2fd; }
.overview_value { font-size: 24px; font-weight: 700; color: #3f3424; }
.overview_label { font-size: 11px; color: #9a8d73; margin-top: 2px; }

.history_list_title { font-size: 14px; font-weight: 700; color: #3f3424; margin-bottom: 10px; padding: 8px 0; border-bottom: 1px solid #efe5cd; }

.diagnosis_panel { display: flex; flex-direction: column; gap: 14px; }
.radar_section { background: #fffdf7; border-radius: 16px; padding: 16px; border: 1px solid #efe5cd; text-align: center; }
.section_subtitle { font-size: 15px; font-weight: 700; color: #3f3424; margin-bottom: 8px; }
.radar_chart { width: 100%; height: 320px; }

.audio_section { background: #faf7f0; border-radius: 14px; padding: 12px 16px; border: 1px solid #efe5cd; }

.text_diagnosis { display: flex; flex-direction: column; gap: 10px; }
.dim_cards { display: flex; flex-direction: column; gap: 8px; }
.dim_card { padding: 12px 16px; border-radius: 14px; background: #fdfbf6; border: 1px solid #efe5cd; }
.dim_card_head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.dim_card_name { font-size: 14px; font-weight: 700; color: #3f3424; }
.dim_card_score { font-size: 22px; font-weight: 800; }
.dim_comment { margin-top: 8px; font-size: 12px; color: #6d6050; line-height: 1.7; }

.suggestion_box { padding: 14px 18px; background: linear-gradient(135deg,#f0fbf1,#e8f5e9); border-radius: 12px; border: 1px solid #c8e6c9; }
.sug_title { font-size: 15px; font-weight: 700; color: #2e7d32; margin-bottom: 8px; }
.sug_content { font-size: 13px; color: #33691e; line-height: 1.8; }

.overall_box { padding: 14px 18px; background: #f5f6fa; border-radius: 12px; border: 1px solid #dcdde4; }
.ov_title { font-size: 14px; font-weight: 700; color: #5b503f; margin-bottom: 6px; }
.ov_content { font-size: 13px; color: #6d6050; line-height: 1.8; }
</style>
