<template>
  <div>
    <div class="app-contain">
      <div class="btn_view">
        <el-button type="danger" :disabled="selRows.length?false:true" @click="delClick(null)">删除</el-button>
      </div>
      <el-table v-loading="listLoading" border :stripe='false' @selection-change="handleSelectionChange" ref="table" :data="list" @row-click="infoClick">
        <el-table-column type="selection" width="55" />
        <el-table-column label="学生" width="100"><template #default="scope">{{scope.row.studentname || scope.row.studentaccount || '-'}}</template></el-table-column>
        <el-table-column prop="coursetitle" label="古诗" width="140"></el-table-column>
        <el-table-column prop="totalscore" label="总分" width="80">
          <template #default="scope"><span :style="{color:scope.row.totalscore>=85?'#4CAF50':scope.row.totalscore>=60?'#FF9800':'#F44336',fontWeight:700}">{{scope.row.totalscore}}</span></template>
        </el-table-column>
        <el-table-column prop="addtime" label="时间" width="170"></el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button type="info" size="small" @click="infoClick(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager_view"><el-pagination background @size-change="sizeChange" @current-change="currentChange" :page-sizes="[10,20,50]" :page-size="pageSize" layout="total,sizes,prev,pager,next" :total="total"></el-pagination></div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="跟读报告详情" width="70%" destroy-on-close>
      <el-form v-if="detail" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="学生">{{detail.studentname}}</el-form-item></el-col>
          <el-col :span="8"><el-form-item label="古诗">{{detail.coursetitle}}</el-form-item></el-col>
          <el-col :span="8"><el-form-item label="总分"><b :style="{color:detail.totalscore>=85?'#4CAF50':detail.totalscore>=60?'#FF9800':'#F44336',fontSize:'24px'}">{{detail.totalscore}}</b></el-form-item></el-col>
        </el-row>
        <div v-if="parsedReport && parsedReport.dimensions" class="ai_review_panel">
          <div class="radar_section"><div ref="radarChartRef" class="radar_chart"></div></div>
          <div class="detail_section">
            <div class="dimension_card" v-for="(dim, idx) in parsedReport.dimensions" :key="idx">
              <div class="dim_header"><span class="dim_name">{{dim.name}}</span><span class="dim_weight">权重{{dim.weight}}%</span><span class="dim_score">{{dim.score}}分</span></div>
              <el-progress :percentage="dim.score" :color="dim.score>=85?'#6fc47d':dim.score>=60?'#e7ba63':'#e88a6e'" :stroke-width="8" />
              <div class="dim_comment">{{dim.comment}}</div>
              <div class="dim_encourage">{{dim.encourage}}</div>
            </div>
            <div class="overall_comment" v-if="parsedReport.overallComment">
              <div class="overall_label">总评</div><div class="overall_text">{{parsedReport.overallComment}}</div>
            </div>
          </div>
        </div>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, inject, getCurrentInstance } from 'vue'
const context = getCurrentInstance()?.appContext.config.globalProperties
const echarts = inject('echarts')
const list = ref([]); const listLoading = ref(false); const selRows = ref([]); const total = ref(0)
const pageNum = ref(1); const pageSize = ref(10)
const detailVisible = ref(false); const detail = ref(null)
const radarChartRef = ref(null); const radarChart = ref(null)

const parsedReport = computed(() => {
  try { const r = JSON.parse(detail.value?.reportjson || '{}'); return r.dimensions ? r : null }
  catch(e) { return null }
})

const getList = () => {
  listLoading.value = true
  context.$http({ url: 'followread/records', method: 'get', params: { page: pageNum.value, limit: pageSize.value } }).then(res => {
    list.value = res.data.data.list || []
    total.value = res.data.data.total || 0
    listLoading.value = false
  })
}
const infoClick = (row) => { detail.value = row; detailVisible.value = true }
const delClick = () => {
  context.$toolUtil.message('确认删除？', '确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }).then(() => {
    context.$http({ url: 'followread/deleteRecord', method: 'post', data: selRows.value.map(r => r.id) }).then(() => getList())
  }).catch(() => {})
}
const handleSelectionChange = (rows) => { selRows.value = rows }
const sizeChange = (s) => { pageSize.value = s; getList() }
const currentChange = (p) => { pageNum.value = p; getList() }

const renderRadar = () => {
  nextTick(() => {
    if (!radarChartRef.value || !parsedReport.value || !echarts) return
    if (!radarChart.value) radarChart.value = echarts.init(radarChartRef.value)
    const dims = parsedReport.value.dimensions
    radarChart.value.setOption({
      radar: { center: ['50%','50%'], radius:'65%', indicator: dims.map(d=>({name:`${d.name}\n${d.score}分`,max:100})), axisName:{color:'#5b503f',fontSize:12} },
      series: [{ type:'radar', data:[{value:dims.map(d=>d.score),name:'跟读评分',areaStyle:{color:'rgba(109,190,114,0.2)'},lineStyle:{color:'#6fc47d',width:2},itemStyle:{color:'#6fc47d'}}] }]
    })
  })
}
watch(detailVisible, v => { if(v) renderRadar() })
getList()
</script>

<style lang="scss" scoped>
.ai_review_panel { display:grid; grid-template-columns:280px 1fr; gap:20px; margin-top:16px; }
.radar_section { display:flex; align-items:center; justify-content:center; background:linear-gradient(180deg,#fffdfa,#fefefb); border-radius:20px; border:1px solid #efe5cd; padding:12px; }
.radar_chart { width:260px; height:260px; }
.detail_section { display:flex; flex-direction:column; gap:14px; }
.dimension_card { padding:14px 18px; border-radius:16px; background:linear-gradient(180deg,#fffdfa,#fff); border:1px solid #ece3d3; }
.dim_header { display:flex; align-items:center; gap:12px; margin-bottom:8px; }
.dim_name { font-weight:700; color:#3f3424; }
.dim_weight { font-size:12px; color:#9a8d73; }
.dim_score { margin-left:auto; font-weight:700; color:#6fc47d; font-size:18px; }
.dim_comment { margin-top:10px; color:#5b503f; font-size:13px; line-height:1.8; }
.dim_encourage { margin-top:6px; color:#e7a63c; font-size:13px; font-weight:600; }
.overall_comment { margin-top:4px; padding:14px 18px; border-radius:16px; background:linear-gradient(135deg,rgba(255,248,226,.95),rgba(245,252,247,.96)); border:1px solid rgba(225,196,129,.45); }
.overall_label { color:#7b5b17; font-size:12px; font-weight:700; margin-bottom:8px; }
.overall_text { color:#5b503f; font-size:14px; line-height:1.8; }
</style>
