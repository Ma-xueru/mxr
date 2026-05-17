<template>
  <div class="portrait_page">
    <div class="page_header">
      <el-button class="back_btn" @click="$router.back()">← 返回</el-button>
      <span class="page_title">{{ portrait.studentname || '加载中...' }} 的综合素质画像</span>
    </div>

    <div class="portrait_body" v-if="portrait.studentaccount">
      <!-- 模块一 -->
      <div class="module_card">
        <div class="module_title"><span class="module_num">01</span> 微观基础 · 多维能力雷达</div>
        <el-row :gutter="20">
          <el-col :span="10">
            <div class="student_basic">
              <div class="avatar_circle">{{ portrait.studentname ? portrait.studentname.charAt(0) : '生' }}</div>
              <div class="basic_name">{{ portrait.studentname }}</div>
              <div class="basic_account">学号 {{ portrait.studentaccount }}</div>
              <div class="basic_class">{{ portrait.grade }} · {{ portrait.classname }}</div>
              <div class="basic_rank">班级综合排名 <b>#{{ portrait.overallRank }}</b> / {{ portrait.classTotal }} 人</div>
            </div>
          </el-col>
          <el-col :span="14">
            <div ref="radarChartRef" class="radar_chart"></div>
            <div class="weak_advice" v-if="portrait.weakAdvice">⚠️ {{ portrait.weakAdvice }}</div>
          </el-col>
        </el-row>
      </div>

      <!-- 模块二 -->
      <div class="module_card">
        <div class="module_title"><span class="module_num">02</span> 强制约束轨 · 班级作业与测验</div>
        <el-row :gutter="16">
          <el-col :span="8">
            <div class="ring_card">
              <div class="ring_label">背诵达标率</div>
              <el-progress type="dashboard" :percentage="portrait.recitationPassRate" :color="progressColor(portrait.recitationPassRate)">
                <span class="ring_value">{{ portrait.recitationDone }}/{{ portrait.recitationTotal }}</span>
              </el-progress>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="ring_card">
              <div class="ring_label">测验及格率</div>
              <el-progress type="dashboard" :percentage="portrait.quizPassRate" :color="progressColor(portrait.quizPassRate)">
                <span class="ring_value">{{ portrait.quizPassed }}/{{ portrait.quizTotal }}</span>
              </el-progress>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="ring_card">
              <div class="ring_label">跟读最高分</div>
              <el-progress type="dashboard" :percentage="Math.min(portrait.followReadMaxScore, 100)" color="#6fc47d">
                <span class="ring_value">{{ portrait.followReadMaxScore }}分</span>
              </el-progress>
            </div>
          </el-col>
        </el-row>
        <div class="recent_table" v-if="portrait.recentTasks && portrait.recentTasks.length">
          <div class="sub_title">最近 5 次班级任务</div>
          <el-table :data="portrait.recentTasks" border size="small">
            <el-table-column prop="type" label="类型" width="55">
              <template #default="s"><el-tag :type="s.row.type==='测验'?'danger':'warning'" size="small">{{ s.row.type }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="title" label="任务标题" min-width="120"></el-table-column>
            <el-table-column prop="poem" label="古诗" min-width="80"></el-table-column>
            <el-table-column prop="score" label="得分" width="55"></el-table-column>
            <el-table-column prop="status" label="状态" width="70"></el-table-column>
            <el-table-column prop="deadline" label="截止" width="95"></el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 模块三 -->
      <div class="module_card">
        <div class="module_title"><span class="module_num">03</span> 自主特训轨 · 自学与 AI 拓展</div>
        <el-row :gutter="12">
          <el-col :span="12">
            <div class="stat_item">
              <div class="stat_icon">📖</div>
              <div class="stat_info">
                <div class="stat_label">古诗自学进度</div>
                <div class="stat_num">{{ portrait.selfStudyPoems }} / {{ portrait.totalPoems }}</div>
                <el-progress :percentage="portrait.totalPoems ? Math.round(portrait.selfStudyPoems/portrait.totalPoems*100) : 0" :stroke-width="6" :show-text="false" />
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="stat_item"><div class="stat_icon">🎤</div><div class="stat_info"><div class="stat_label">AI 跟读统计</div><div class="stat_num">{{ portrait.followReadCount }} 次 · 最高 {{ portrait.followReadMaxScore }} 分</div></div></div>
          </el-col>
          <el-col :span="12">
            <div class="stat_item"><div class="stat_icon">🔄</div><div class="stat_info"><div class="stat_label">温故知新</div><div class="stat_num">重练 {{ portrait.reviewWrongCount }} 次 · 消灭 {{ portrait.wrongFixedCount }} 题</div></div></div>
          </el-col>
          <el-col :span="12">
            <div class="stat_item"><div class="stat_icon">🚀</div><div class="stat_info"><div class="stat_label">举一反三</div><div class="stat_num">突破 {{ portrait.deriveBreakCount }} 题</div><div class="stat_sub">弱项标签：{{ portrait.weakTags || '暂无' }}</div></div></div>
          </el-col>
        </el-row>
      </div>

      <!-- 模块四 -->
      <div class="module_card" v-if="portrait.topWrongItems && portrait.topWrongItems.length">
        <div class="module_title"><span class="module_num">04</span> 顽固盲区 · 未掌握错题摘录</div>
        <el-collapse>
          <el-collapse-item v-for="(item, idx) in portrait.topWrongItems" :key="idx" :title="'错题 ' + (idx + 1) + '：' + (item.question || '未知题目')">
            <div class="wrong_detail">
              <el-tag size="small" type="info">{{ item.poemTitle }}</el-tag>
              <el-tag size="small" type="danger" style="margin-left:8px">{{ item.errorType }}</el-tag>
              <div class="wrong_meta">错误 {{ item.wrongCount }} 次 · 最近：{{ item.lastWrongTime }}</div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>

    <div class="loading_hint" v-else-if="loading">加载中学情数据...</div>
    <div class="loading_hint" v-else>未找到该学生数据</div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onMounted, onBeforeUnmount, inject, getCurrentInstance } from 'vue'
import { useRoute } from 'vue-router'
const route = useRoute()
const context = getCurrentInstance()?.appContext.config.globalProperties
const echarts = inject('echarts')
const portrait = ref({})
const loading = ref(true)
const radarChartRef = ref(null)
let radarChart = null

const progressColor = (rate) => {
  if (rate >= 80) return '#6fc47d'
  if (rate >= 60) return '#e7ba63'
  return '#e88a6e'
}

const renderRadar = () => {
  nextTick(() => {
    if (!radarChartRef.value || !echarts) return
    if (!portrait.value || !portrait.value.radarScores) return
    if (radarChart) { radarChart.dispose(); radarChart = null }
    radarChart = echarts.init(radarChartRef.value)
    const scores = portrait.value.radarScores
    const dims = Object.keys(scores).map(k => ({ name: k, max: 100 }))
    const vals = Object.values(scores)
    radarChart.setOption({
      radar: { center: ['50%', '52%'], radius: '62%', indicator: dims, axisName: { color: '#5b503f', fontSize: 11 } },
      series: [{ type: 'radar', data: [{ value: vals, name: '能力值', areaStyle: { color: 'rgba(109,190,114,0.18)' }, lineStyle: { color: '#6fc47d', width: 2 }, itemStyle: { color: '#6fc47d' } }] }]
    })
  })
}

watch(portrait, (val) => { if (val && val.radarScores) renderRadar() }, { deep: true })

onMounted(() => {
  const account = route.query.studentaccount
  if (!account) { loading.value = false; return }
  context?.$http({ url: 'teacher/studentPortrait', method: 'get', params: { studentaccount: account } }).then(res => {
    loading.value = false
    if (res.data.code === 0) { portrait.value = res.data.data; renderRadar() }
  }).catch(() => { loading.value = false })
})

onBeforeUnmount(() => { radarChart && radarChart.dispose() })
</script>

<style lang="scss" scoped>
.portrait_page { padding: 20px; max-width: 900px; margin: 0 auto; }
.page_header { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; }
.page_title { font-size: 20px; font-weight: 700; color: #3f3424; }
.back_btn { border-radius: 8px; }
.loading_hint { text-align: center; padding: 60px; color: #9a8d73; font-size: 14px; }

.module_card { background: #fff; border-radius: 16px; padding: 20px 20px 16px; margin-bottom: 16px; border: 1px solid #efe5cd; box-shadow: 0 4px 16px rgba(100,78,41,.04); }
.module_title { font-size: 16px; font-weight: 700; color: #3f3424; margin-bottom: 16px; display: flex; align-items: center; gap: 8px; }
.module_num { display: inline-flex; align-items: center; justify-content: center; width: 28px; height: 28px; border-radius: 8px; background: linear-gradient(135deg, #e3a73c, #f0bf5e); color: #fff; font-size: 13px; font-weight: 700; }
.sub_title { font-size: 14px; font-weight: 600; color: #5b503f; margin: 14px 0 8px; }

.student_basic { text-align: center; padding: 10px 0; }
.avatar_circle { width: 72px; height: 72px; border-radius: 50%; background: linear-gradient(135deg, #a8c080, #8b9d6f); color: #fff; font-size: 30px; font-weight: 700; line-height: 72px; margin: 0 auto 10px; }
.basic_name { font-size: 18px; font-weight: 700; color: #3f3424; }
.basic_account { font-size: 12px; color: #9a8d73; margin-top: 2px; }
.basic_class { font-size: 13px; color: #6d8056; margin-top: 4px; font-weight: 500; }
.basic_rank { margin-top: 8px; padding: 6px 14px; border-radius: 20px; background: #fff8e1; color: #7b5b17; font-size: 12px; font-weight: 600; }

.radar_chart { width: 100%; height: 240px; }
.weak_advice { display: flex; align-items: flex-start; gap: 6px; padding: 10px 14px; margin-top: 4px; border-radius: 10px; background: #fff3e0; color: #bf6d1a; font-size: 12px; line-height: 1.6; }

.ring_card { text-align: center; padding: 8px 0; }
.ring_label { font-size: 13px; color: #7c6a47; margin-bottom: 4px; font-weight: 500; }
.ring_value { font-size: 16px; font-weight: 700; color: #3f3424; }
.recent_table { margin-top: 4px; }

.stat_item { display: flex; align-items: flex-start; gap: 12px; padding: 12px 14px; border-radius: 12px; background: linear-gradient(135deg, #fdfbf7, #f8f5ec); margin-bottom: 10px; }
.stat_icon { font-size: 32px; line-height: 1.2; }
.stat_info { flex: 1; }
.stat_label { font-size: 13px; font-weight: 600; color: #4a4030; }
.stat_num { font-size: 15px; font-weight: 700; color: #3f3424; margin-top: 2px; }
.stat_sub { font-size: 11px; color: #9a8d73; margin-top: 2px; }

.wrong_detail { padding: 8px 4px; }
.wrong_meta { margin-top: 8px; font-size: 12px; color: #9a8d73; }
</style>
