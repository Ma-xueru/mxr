const { detail } = require("../../api/index.js")

Page({
  data: {
    id: '',
    detailList: {},
    aiReport: null,
    baseURL: wx.getStorageSync('baseURL') + '/'
  },

  async onLoad(option) {
    const myid = option?.id ? option.id : getApp().globalData.detailId
    this.setData({ id: myid })
    await this.handleUpdateData()
  },

  async handleUpdateData() {
    const id = this.data.id || getApp().globalData.detailId
    if (!id) return
    const res = await detail("recitationtask", id)
    if (res.code == 0) {
      const data = res.data
      // 解析 AI 测评报告 JSON
      let report = null
      if (data.aiscorecomment) {
        try {
          const parsed = JSON.parse(data.aiscorecomment)
          if (parsed.totalScore && parsed.dimensions) {
            report = parsed
          }
        } catch (e) {}
      }
      this.setData({ detailList: data, aiReport: report })
    }
  }
})
