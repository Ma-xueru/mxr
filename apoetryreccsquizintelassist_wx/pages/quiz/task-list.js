Page({
  data: { list: [], loading: true },

  onShow() { this.loadTasks() },

  loadTasks() {
    var that = this
    wx.request({
      url: (wx.getStorageSync('baseURL')||'') + '/quiztask/student-pending', method: 'GET',
      header: { Token: wx.getStorageSync('token') },
      success: function(res) {
        if (res.data.code === 0) that.setData({ list: res.data.data || [], loading: false })
        else that.setData({ loading: false })
      },
      fail: function() { that.setData({ loading: false }) }
    })
  },

  startExam(e) {
    var item = this.data.list[e.currentTarget.dataset.index]
    getApp().globalData._taskQuiz = {
      taskId: item.taskId, courseTitle: item.courseTitle,
      courseId: item.courseId || '', questions: item.questions
    }
    wx.navigateTo({ url: '/pages/quiz/task-exam' })
  }
})
