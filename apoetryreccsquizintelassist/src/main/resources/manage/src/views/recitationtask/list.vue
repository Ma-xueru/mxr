<template>
  <div>
    <div class="app-contain">
      <!-- 任务大盘 -->
      <div class="overview_panel">
        <div class="overview_header">
          <div>
            <div class="overview_badge">任务大盘</div>
            <div class="overview_title">背诵任务管理</div>
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
        <el-button type="success" @click="addClick" v-if="btnAuth('recitationtask','新增')">新增任务</el-button>
      </div>

      <!-- 任务大盘表格 -->
      <el-table v-loading="loading" :data="taskGroups" border stripe v-if="btnAuth('recitationtask','查看')">
        <el-table-column label="序号" width="60" align="center">
          <template #default="scope">{{ scope.$index + 1 }}</template>
        </el-table-column>
        <el-table-column prop="tasktitle" label="任务标题" min-width="160"></el-table-column>
        <el-table-column prop="classes" label="发布班级" min-width="150"></el-table-column>
        <el-table-column prop="coursetitles" label="指定古诗" min-width="140"></el-table-column>
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
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button type="info" size="small" @click="viewDetails(scope.row)">详情</el-button>
            <el-button type="danger" size="small" @click="deleteGroup(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 学生明细弹窗 -->
    <el-dialog v-model="detailVisible" :title="'任务：' + detailTitle + ' — 学生作答明细'" width="90%" destroy-on-close top="3vh">
      <div class="detail_overview" v-if="detailList.length">
        <span>共 <b>{{ detailList.length }}</b> 名学生 · 已完成 <b style="color:#6fc47d">{{ detailDone }}</b> 人</span>
      </div>
      <el-table :data="detailList" border size="small" v-loading="detailLoading" max-height="65vh">
        <el-table-column prop="studentaccount" label="学生账号" width="120"></el-table-column>
        <el-table-column prop="studentname" label="学生姓名" width="100"></el-table-column>
        <el-table-column label="背诵音频" width="120">
          <template #default="s">
            <audio v-if="s.row.recitationaudio" :src="getAudioSrc(s.row.recitationaudio)" controls style="width:110px;height:28px"></audio>
            <span v-else style="color:#ccc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="完成状态" width="90">
          <template #default="s">
            <el-tag :type="s.row.completionstatus==='已完成'?'success':'warning'" size="small">{{ s.row.completionstatus || '待完成' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="kaoshichengji" label="得分" width="70"></el-table-column>
        <el-table-column prop="recognizedtext" label="识别文本" min-width="180">
          <template #default="s"><span style="font-size:12px;color:#666">{{ s.row.recognizedtext || '-' }}</span></template>
        </el-table-column>
        <el-table-column prop="completionremark" label="完成说明" min-width="140">
          <template #default="s"><span style="font-size:12px;color:#666">{{ s.row.completionremark || '-' }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="s">
            <el-button type="warning" size="small" @click="reviewClick(s.row.id)">评分详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <formModel ref="formRef" @formModelChange="loadTaskGroups"></formModel>
  </div>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance, computed } from 'vue'
const context = getCurrentInstance()?.appContext.config.globalProperties
import formModel from './formModel.vue'

const tableName = 'recitationtask'
const taskGroups = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const detailTitle = ref('')
const detailList = ref([])
const detailDone = ref(0)
const detailLoading = ref(false)
const formRef = ref(null)

const totalStudents = computed(() => taskGroups.value.reduce((s, g) => s + (g.total || 0), 0))
const totalDone = computed(() => taskGroups.value.reduce((s, g) => s + (g.done || 0), 0))

const getAudioSrc = (file) => {
  if (!file) return ''; if (file.indexOf('http')===0) return file
  const fn = file.replace(/\\/g,'/').split('/').pop()
  return (context?.$config.url || '') + '/file/' + fn
}

const loadTaskGroups = () => {
  loading.value = true
  context?.$http({ url: `${tableName}/taskGroups`, method: 'get' }).then(res => {
    taskGroups.value = res.data.data || []
  }).finally(() => loading.value = false)
}

const viewDetails = (row) => {
  detailTitle.value = row.tasktitle
  detailVisible.value = true
  detailLoading.value = true
  const params = {
    title: row.tasktitle, courses: row.coursetitles || '',
    releaseTime: row.releasetime, teacher: row.teacheraccount || ''
  }
  context?.$http({ url: `${tableName}/taskDetails`, method: 'get', params }).then(res => {
    detailList.value = res.data.data || []
    detailDone.value = detailList.value.filter(d => d.completionstatus === '已完成').length
  }).finally(() => detailLoading.value = false)
}

const deleteGroup = (row) => {
  if (!confirm('确定删除该批次所有任务？')) return
  const params = {
    title: row.tasktitle, courses: row.coursetitles || '',
    releaseTime: row.releasetime, teacher: row.teacheraccount || ''
  }
  context?.$http({ url: `${tableName}/taskDetails`, method: 'get', params }).then(res => {
    const ids = (res.data.data || []).map(d => d.id)
    if (!ids.length) return
    context?.$http({ url: `${tableName}/delete`, method: 'post', data: ids }).then(() => {
      context?.$toolUtil.message('已删除', 'success')
      loadTaskGroups()
    })
  })
}

const addClick = () => formRef.value.init()
const reviewClick = (id) => formRef.value.init(id, 'review')

const btnAuth = (e, a) => context?.$toolUtil.isAuth(e, a)

onMounted(() => loadTaskGroups())
</script>

<style lang="scss" scoped>
.overview_panel { padding: 20px 24px; margin-bottom: 16px; background: #fffdf7; border-radius: 16px; border: 1px solid #efe5cd; }
.overview_badge { display: inline-block; padding: 4px 12px; border-radius: 8px; background: #f9a82520; color: #e65100; font-size: 12px; font-weight: 700; margin-bottom: 8px; }
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
</style>
