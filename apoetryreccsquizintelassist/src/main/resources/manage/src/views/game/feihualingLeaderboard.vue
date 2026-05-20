<template>
  <div class="app-contain">
    <div class="overview_panel">
      <div class="overview_header">
        <div>
          <div class="overview_badge">飞花令管理</div>
          <div class="overview_title">排行榜管理</div>
          <div class="overview_desc">查看飞花令对战排行榜与历史对战记录</div>
        </div>
      </div>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="🏆 排行榜" name="leaderboard">
        <el-table v-loading="loading" :data="leaderboardList" border stripe>
          <el-table-column label="排名" width="70" align="center">
            <template #default="s">{{ (pageNum-1)*pageSize + s.$index + 1 }}</template>
          </el-table-column>
          <el-table-column prop="username" label="用户" min-width="120"></el-table-column>
          <el-table-column prop="maxScore" label="最高分" width="100" align="center">
            <template #default="s"><b :style="{color:s.row.maxScore>=80?'#4CAF50':s.row.maxScore>=50?'#FF9800':'#999'}">{{ s.row.maxScore }}</b></template>
          </el-table-column>
          <el-table-column prop="title" label="称号" width="120"></el-table-column>
          <el-table-column prop="totalGames" label="总局数" width="80" align="center"></el-table-column>
          <el-table-column prop="totalWins" label="胜局" width="70" align="center"></el-table-column>
          <el-table-column prop="totalRounds" label="回合" width="70" align="center"></el-table-column>
          <el-table-column prop="updateTime" label="更新时间" width="160"></el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="s">
              <el-button type="danger" size="small" @click="delLeaderboard(s.row.userId)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pager_view" style="margin-top:16px">
          <el-pagination background @size-change="sizeChange" @current-change="pageChange" :page-sizes="[10,20,50]" :page-size="pageSize" layout="total,sizes,prev,pager,next" :total="lbTotal"></el-pagination>
        </div>
      </el-tab-pane>

      <el-tab-pane label="📋 对战记录" name="records">
        <el-table v-loading="rLoading" :data="recordList" border stripe>
          <el-table-column label="序号" width="60" align="center">
            <template #default="s">{{ (rPage-1)*rSize + s.$index + 1 }}</template>
          </el-table-column>
          <el-table-column prop="username" label="用户" width="120"></el-table-column>
          <el-table-column prop="score" label="得分" width="80" align="center"></el-table-column>
          <el-table-column prop="rounds" label="回合" width="70" align="center"></el-table-column>
          <el-table-column prop="maxCombo" label="连击" width="70" align="center"></el-table-column>
          <el-table-column prop="keyword" label="关键字" width="80"></el-table-column>
          <el-table-column prop="addtime" label="时间" width="160"></el-table-column>
        </el-table>
        <div class="pager_view" style="margin-top:16px">
          <el-pagination background @size-change="rSizeChange" @current-change="rPageChange" :page-sizes="[10,20,50]" :page-size="rSize" layout="total,sizes,prev,pager,next" :total="rTotal"></el-pagination>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance } from 'vue'
const context = getCurrentInstance()?.appContext.config.globalProperties

const activeTab = ref('leaderboard')
const loading = ref(false); const leaderboardList = ref([]); const lbTotal = ref(0)
const pageNum = ref(1); const pageSize = ref(10)
const rLoading = ref(false); const recordList = ref([]); const rTotal = ref(0)
const rPage = ref(1); const rSize = ref(10)

const loadLeaderboard = () => {
  loading.value = true
  context?.$http({ url: 'game/fhlLeaderboard', method: 'get', params: { page: pageNum.value, limit: pageSize.value } }).then(res => {
    if (res.data.code === 0) { leaderboardList.value = res.data.data.list || []; lbTotal.value = res.data.data.total || 0 }
    loading.value = false
  })
}
const loadRecords = () => {
  rLoading.value = true
  context?.$http({ url: 'game/fhlRecords', method: 'get', params: { page: rPage.value, limit: rSize.value } }).then(res => {
    if (res.data.code === 0) { recordList.value = res.data.data.list || []; rTotal.value = res.data.data.total || 0 }
    rLoading.value = false
  })
}
const delLeaderboard = (uid) => {
  context?.$toolUtil.message('确定删除该用户排行榜？', '确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }).then(() => {
    context?.$http({ url: 'game/fhlLeaderboardDelete', method: 'post', data: [uid] }).then(() => loadLeaderboard())
  }).catch(() => {})
}
const sizeChange = (s) => { pageSize.value = s; loadLeaderboard() }
const pageChange = (p) => { pageNum.value = p; loadLeaderboard() }
const rSizeChange = (s) => { rSize.value = s; loadRecords() }
const rPageChange = (p) => { rPage.value = p; loadRecords() }

onMounted(() => { loadLeaderboard(); loadRecords() })
</script>
