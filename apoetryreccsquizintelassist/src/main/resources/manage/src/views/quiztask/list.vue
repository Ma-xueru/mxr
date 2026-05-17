<template>
  <div>
    <div class="app-contain">
      <div class="overview_panel">
        <div class="overview_header">
          <div>
            <div class="overview_badge">测验大盘</div>
            <div class="overview_title">AI 测验管理</div>
            <div class="overview_desc">按发布批次聚合展示，一次发布一行，点击详情查看学生明细。</div>
          </div>
        </div>
        <div class="overview_cards">
          <div class="overview_card"><div class="overview_value">{{ taskGroups.length }}</div><div class="overview_label">发布批次</div></div>
          <div class="overview_card warm"><div class="overview_value">{{ totalStudents }}</div><div class="overview_label">覆盖学生人次</div></div>
          <div class="overview_card green"><div class="overview_value">{{ totalDone }}</div><div class="overview_label">已完成人次</div></div>
        </div>
      </div>

      <div class="btn_view" style="margin-bottom:12px">
        <el-button type="success" @click="addClick" v-if="btnAuth('quiztask','新增')">新增测验</el-button>
      </div>

      <el-table v-loading="loading" :data="taskGroups" border stripe v-if="btnAuth('quiztask','查看')">
        <el-table-column label="序号" width="60" align="center">
          <template #default="scope">{{ scope.$index + 1 }}</template>
        </el-table-column>
        <el-table-column prop="tasktitle" label="测验标题" min-width="160"></el-table-column>
        <el-table-column prop="classes" label="发布班级" min-width="130"></el-table-column>
        <el-table-column prop="coursetitles" label="指定古诗" min-width="120"></el-table-column>
        <el-table-column label="截止日期" width="110">
          <template #default="scope">{{ scope.row.deadline ? scope.row.deadline.substring(0,10) : '-' }}</template>
        </el-table-column>
        <el-table-column label="完成进度" width="200">
          <template #default="scope">
            <div style="display:flex;align-items:center;gap:8px">
              <el-progress :percentage="scope.row.total ? Math.round(scope.row.done/scope.row.total*100) : 0" :stroke-width="10" :color="scope.row.done===scope.row.total ? '#6fc47d' : '#e7ba63'" />
              <span style="font-size:13px;white-space:nowrap;color:#5b503f">{{ scope.row.done }}/{{ scope.row.total }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="info" size="small" @click="viewDetails(scope.row)">详情</el-button>
            <el-button type="danger" size="small" @click="deleteGroup(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 学生明细弹窗 -->
    <el-dialog v-model="detailVisible" :title="'测验：' + detailTitle + ' — 学生作答明细'" width="90%" destroy-on-close top="3vh">
      <div class="detail_overview" v-if="detailList.length">
        <span>共 <b>{{ detailList.length }}</b> 名学生 · 已完成 <b style="color:#6fc47d">{{ detailDone }}</b> 人</span>
      </div>
      <el-table :data="detailList" border size="small" v-loading="detailLoading" max-height="65vh">
        <el-table-column prop="studentaccount" label="学生账号" width="120"></el-table-column>
        <el-table-column prop="studentname" label="学生姓名" width="100"></el-table-column>
        <el-table-column label="完成状态" width="90">
          <template #default="s"><el-tag :type="s.row.completionstatus==='已完成'?'success':'warning'" size="small">{{ s.row.completionstatus || '待完成' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="kaoshichengji" label="得分" width="70"></el-table-column>
        <el-table-column prop="completionremark" label="完成说明" min-width="140">
          <template #default="s"><span style="font-size:12px;color:#666">{{ s.row.completionremark || '-' }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="s">
            <el-button type="primary" size="small" @click="viewRadar(s.row)">雷达图</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 雷达图弹窗 -->
    <el-dialog v-model="radarVisible" title="AI 多维评估雷达图" width="720px" destroy-on-close top="2vh">
      <div class="total_score" v-if="radarTotalScore !== null">
        <span class="ts_label">总成绩</span>
        <span class="ts_value" :style="{color: radarTotalScore>=80?'#4CAF50':radarTotalScore>=60?'#FF9800':'#f44336'}">{{ radarTotalScore }}</span>
        <span class="ts_unit">分</span>
        <span class="ts_sub">正确 {{ radarCorrect }} / {{ radarTotalQ }} 题</span>
      </div>
      <div class="dim_scores" v-if="radarReport && radarReport.dimensions">
        <div class="dim_score_item" v-for="d in radarReport.dimensions" :key="d.name">
          <span class="ds_name">{{ d.name }}</span>
          <span class="ds_score" :style="{color: d.score>=80?'#4CAF50':d.score>=50?'#FF9800':'#f44336'}">{{ d.score }}分</span>
        </div>
      </div>
      <div ref="radarChartRef" style="width:100%;height:420px" v-if="radarVisible"></div>
      <div class="dim_cards" v-if="radarReport && radarReport.dimensions">
        <div class="dim_card" v-for="(d, idx) in radarReport.dimensions" :key="idx">
          <div class="dim_card_head">
            <span class="dim_card_name">{{ d.name }}</span>
            <span class="dim_card_score" :style="{color: d.score>=80?'#4CAF50':d.score>=50?'#FF9800':'#f44336'}">{{ d.score }}分</span>
          </div>
          <el-progress :percentage="d.score" :stroke-width="6" :color="d.score>=80?'#6fc47d':d.score>=50?'#e7ba63':'#e88a6e'" />
          <div class="dim_card_comment">{{ d.comment }}</div>
        </div>
      </div>
      <div class="suggestion_box" v-if="radarReport && radarReport.suggestion">
        <div class="sug_title">💡 智能自学建议</div>
        <div class="sug_content">{{ radarReport.suggestion }}</div>
      </div>
      <div class="overall_box" v-if="radarReport && radarReport.overallComment">
        <div class="ov_title">📝 测验整体总评</div>
        <div class="ov_content">{{ radarReport.overallComment }}</div>
      </div>
    </el-dialog>

    <formModel ref="formRef" @formModelChange="loadTaskGroups"></formModel>
  </div>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance, computed, nextTick, inject } from 'vue'
const context = getCurrentInstance()?.appContext.config.globalProperties
const echarts = inject('echarts')
import formModel from './formModel.vue'

const tableName = 'quiztask'
const taskGroups = ref([]); const loading = ref(false)
const detailVisible = ref(false); const detailTitle = ref('')
const detailList = ref([]); const detailDone = ref(0); const detailLoading = ref(false)
const formRef = ref(null)

const totalStudents = computed(() => taskGroups.value.reduce((s, g) => s + (g.total || 0), 0))
const totalDone = computed(() => taskGroups.value.reduce((s, g) => s + (g.done || 0), 0))

const loadTaskGroups = () => {
  loading.value = true
  context?.$http({ url: `${tableName}/taskGroups`, method: 'get' }).then(res => {
    taskGroups.value = res.data.data || []
  }).finally(() => loading.value = false)
}

const viewDetails = (row) => {
  detailTitle.value = row.tasktitle; detailVisible.value = true; detailLoading.value = true
  context?.$http({ url: `${tableName}/page`, method: 'get', params: { page:1, limit:999, tasktitle: row.tasktitle, teacheraccount: row.teacheraccount || '' } }).then(res => {
    detailList.value = res.data.data.list || []
    detailDone.value = detailList.value.filter(d => d.completionstatus === '已完成').length
  }).finally(() => detailLoading.value = false)
}

const deleteGroup = (row) => {
  if (!confirm('确定删除该批次所有测验任务？')) return
  context?.$http({ url: `${tableName}/page`, method: 'get', params: { page:1, limit:999, tasktitle: row.tasktitle, teacheraccount: row.teacheraccount || '' } }).then(res => {
    const ids = (res.data.data.list || []).map(d => d.id)
    if (!ids.length) return
    context?.$http({ url: `${tableName}/delete`, method: 'post', data: ids }).then(() => { context?.$toolUtil.message('已删除', 'success'); loadTaskGroups() })
  })
}

const addClick = () => formRef.value.init()
const radarVisible = ref(false); const radarReport = ref(null)
const radarTotalScore = ref(null); const radarCorrect = ref(0); const radarTotalQ = ref(0)
const radarChartRef = ref(null); let radarChart = null

const viewRadar = (row) => {
  radarVisible.value = true; radarReport.value = null
  radarTotalScore.value = null; radarCorrect.value = 0; radarTotalQ.value = 0
  context?.$http({ url: 'quiztask/result', method: 'get', params: { taskId: row.id, student: row.studentaccount || '' } }).then(res => {
    if (res.data && res.data.code === 0 && res.data.data) {
      const data = res.data.data
      radarTotalScore.value = data.score || 0
      radarCorrect.value = data.correctCount || 0
      radarTotalQ.value = data.totalQuestions || 0
      let report = null
      try { report = (typeof data.aiReport === 'string') ? JSON.parse(data.aiReport) : data.aiReport } catch(e) {}
      if (report && report.dimensions) {
        radarReport.value = report
        nextTick(() => { setTimeout(() => renderRadar(), 200) })
      } else if (data.score !== undefined) {
        radarReport.value = { overallComment: '得分 ' + data.score + ' 分 · 对 ' + data.correctCount + ' / ' + data.totalQuestions + ' 题（AI报告生成中）' }
      } else {
        radarReport.value = { overallComment: '该生暂未完成测验，无评估数据' }
      }
    } else {
      radarReport.value = { overallComment: '该生暂未完成测验，无评估数据' }
    }
  }).catch(() => {
    radarReport.value = { overallComment: '请求失败，请稍后重试' }
  })
}

const renderRadar = () => {
  if (!radarChartRef.value || !echarts) return
  if (!radarReport.value || !radarReport.value.dimensions) return
  if (radarChart) { radarChart.dispose(); radarChart = null }
  radarChart = echarts.init(radarChartRef.value)
  const dims = radarReport.value.dimensions
  radarChart.setOption({
    radar: { center: ['50%','55%'], radius:'58%', indicator: dims.map(d => ({name: d.name + '\n' + d.score + '分', max:100})), axisName: {color:'#3f3424',fontSize:13,fontWeight:'bold',borderRadius:3,padding:[3,6]} },
    series: [{ type:'radar', data:[{ value: dims.map(d => d.score), name:'', areaStyle:{color:'rgba(109,190,114,.22)'}, lineStyle:{color:'#6fc47d',width:3}, itemStyle:{color:'#6fc47d'}, symbolSize:8 }] }]
  })
}

const reviewClick = (id) => formRef.value.init(id, 'review')

const btnAuth = (e, a) => context?.$toolUtil.isAuth(e, a)

onMounted(() => loadTaskGroups())
</script>

<style lang="scss" scoped>
.overview_panel { padding: 20px 24px; margin-bottom: 16px; background: #fffdf7; border-radius: 16px; border: 1px solid #efe5cd; }
.overview_badge { display: inline-block; padding: 4px 12px; border-radius: 8px; background: #7B1FA220; color: #7B1FA2; font-size: 12px; font-weight: 700; margin-bottom: 8px; }
.overview_title { font-size: 22px; font-weight: 700; color: #3f3424; }
.overview_desc { font-size: 13px; color: #9a8d73; margin-top: 4px; }
.overview_cards { display: flex; gap: 16px; margin-top: 16px; }
.overview_card { flex: 1; padding: 14px 18px; border-radius: 12px; background: #faf7f0; text-align: center; }
.overview_card.warm { background: #fff8e1; }
.overview_card.green { background: #e8f5e9; }
.overview_value { font-size: 28px; font-weight: 700; color: #3f3424; }
.overview_label { font-size: 12px; color: #9a8d73; margin-top: 4px; }
.detail_overview { padding: 6px 0 12px; font-size: 14px; color: #5b503f; }
.btn_view { display: flex; gap: 8px; }
.total_score { display: flex; align-items: baseline; justify-content: center; gap: 8px; padding: 16px; margin-bottom: 16px; background: linear-gradient(135deg,#fffdf7,#f8f5ec); border-radius: 16px; border: 1px solid #efe5cd; }
.ts_label { font-size: 14px; color: #9a8d73; }
.ts_value { font-size: 56px; font-weight: 900; line-height: 1; }
.ts_unit { font-size: 18px; color: #9a8d73; }
.ts_sub { font-size: 12px; color: #9a8d73; margin-left: 8px; }
.dim_scores { display: flex; gap: 12px; justify-content: center; flex-wrap: wrap; margin-bottom: 10px; }
.dim_score_item { padding: 8px 16px; background: #fffdf7; border-radius: 12px; border: 1px solid #efe5cd; text-align: center; min-width: 80px; }
.ds_name { display: block; font-size: 11px; color: #9a8d73; }
.ds_score { display: block; font-size: 22px; font-weight: 800; }
.dim_cards { display: flex; flex-direction: column; gap: 10px; margin-top: 12px; }
.dim_card { padding: 12px 16px; border-radius: 14px; background: #fdfbf6; border: 1px solid #efe5cd; }
.dim_card_head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.dim_card_name { font-size: 14px; font-weight: 700; color: #3f3424; }
.dim_card_score { font-size: 22px; font-weight: 800; }
.dim_card_comment { margin-top: 8px; font-size: 12px; color: #6d6050; line-height: 1.7; }
.suggestion_box { margin-top: 14px; padding: 14px 18px; background: linear-gradient(135deg,#f0fbf1,#e8f5e9); border-radius: 12px; border: 1px solid #c8e6c9; }
.sug_title { font-size: 15px; font-weight: 700; color: #2e7d32; margin-bottom: 8px; }
.sug_content { font-size: 13px; color: #33691e; line-height: 1.8; }
.overall_box { margin-top: 12px; padding: 14px 18px; background: #f5f6fa; border-radius: 12px; border: 1px solid #dcdde4; }
.ov_title { font-size: 14px; font-weight: 700; color: #5b503f; margin-bottom: 6px; }
.ov_content { font-size: 13px; color: #6d6050; line-height: 1.8; }
</style>
