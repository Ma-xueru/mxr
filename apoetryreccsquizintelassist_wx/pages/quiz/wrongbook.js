Page({
  data: { list: [], loading: true },

  onLoad() { this.loadWrongList() },
  onShow() { this.loadWrongList() },

  loadWrongList() {
    var that = this
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({
      url: baseURL + '/quiz/wrongbook',
      method: 'GET', header: { Token: wx.getStorageSync('token') },
      success: function(res) {
        that.setData({ list: (res.data && res.data.data) || [], loading: false })
      },
      fail: function() { that.setData({ loading: false }) }
    })
  },

  goQuiz() {
    wx.switchTab({ url: '/pages/course/list' })
  }
})
