const {
  list,
  session
} = require("../../api/index.js")

Page({
  data: {
    paperid: "",
    papername: "",
    list: [],
    loading: false
  },

  onLoad(options) {
    this.setData({
      paperid: options.id || "",
      papername: decodeURIComponent(options.papername || "")
    })
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
        order: 'asc'
      })
      const allRecords = res?.data?.list || []
      const currentRecords = allRecords.filter(item => {
        if (this.data.paperid) {
          return String(item.paperid || '') === String(this.data.paperid)
        }
        return (item.papername || '') === this.data.papername
      })
      const papername = this.data.papername || (currentRecords[0] && currentRecords[0].papername) || ''
      this.setData({
        list: currentRecords,
        papername
      })
    } catch (error) {
      console.log('获取测验详情失败', error)
      this.setData({
        list: []
      })
    } finally {
      this.setData({
        loading: false
      })
    }
  },

  onPullDownRefresh() {
    this.getData().finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  onShareAppMessage() {}
})
