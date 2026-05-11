const {
  list,
  session
} = require("../../api/index.js")

Page({
  data: {
    list: [],
    loading: false
  },

  onLoad() {
    this.getData()
  },

  onShow() {
    this.getData()
  },

  async getCurrentUserId() {
    const globalUser = getApp().globalData.userInfo || {}
    if (globalUser.id) {
      return globalUser.id
    }
    try {
      const res = await session('student')
      if (res.code === 0 && res.data) {
        getApp().globalData.userInfo = res.data
        return res.data.id
      }
    } catch (error) {
      console.log('获取学生信息失败', error)
    }
    return ''
  },

  buildRecordGroups(records) {
    const groupedMap = {}
    records.forEach(item => {
      const paperId = item.paperid || ''
      const paperName = item.papername || item.questionname || '未命名专题'
      const key = `${paperId}__${paperName}`
      if (!groupedMap[key]) {
        groupedMap[key] = {
          paperid: paperId,
          papername: paperName,
          username: item.username || '',
          questionCount: 0,
          totalScore: 0,
          myscore: 0,
          addtime: item.addtime || ''
        }
      }
      groupedMap[key].questionCount += 1
      groupedMap[key].totalScore += Number(item.score || 0)
      groupedMap[key].myscore += Number(item.myscore || 0)
      if (item.addtime && (!groupedMap[key].addtime || item.addtime > groupedMap[key].addtime)) {
        groupedMap[key].addtime = item.addtime
      }
    })

    return Object.values(groupedMap).sort((a, b) => {
      const timeA = a.addtime ? new Date(a.addtime).getTime() : 0
      const timeB = b.addtime ? new Date(b.addtime).getTime() : 0
      return timeB - timeA
    })
  },

  async getData() {
    const userId = await this.getCurrentUserId()
    if (!userId) {
      this.setData({
        list: []
      })
      return
    }

    this.setData({
      loading: true
    })

    try {
      const res = await list('examrecord', {
        page: 1,
        limit: 100000,
        userid: userId,
        sort: 'addtime',
        order: 'desc'
      })
      const records = res?.data?.list || []
      this.setData({
        list: this.buildRecordGroups(records)
      })
    } catch (error) {
      console.log('获取测验记录失败', error)
      this.setData({
        list: []
      })
    } finally {
      this.setData({
        loading: false
      })
    }
  },

  onDetailTap(e) {
    const item = e.currentTarget.dataset.item || {}
    const paperId = item.paperid || ''
    const paperName = encodeURIComponent(item.papername || '')
    wx.navigateTo({
      url: `/pages/examrecord/detail?id=${paperId}&papername=${paperName}`,
    })
  },

  onPullDownRefresh() {
    this.getData().finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  onShareAppMessage() {}
})
