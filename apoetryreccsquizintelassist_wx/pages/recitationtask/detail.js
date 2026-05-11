const { detail } = require("../../api/index.js")

Page({
  data: {
    id: '',
    detailList: {},
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
      this.setData({
        detailList: res.data
      })
    }
  }
})
