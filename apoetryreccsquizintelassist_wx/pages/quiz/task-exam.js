Page({
  data: {
    loading: true, finished: false,
    taskId: '', courseTitle: '', courseId: '',
    questions: [], currentIndex: 0,
    answers: [], score: 0, correctCount: 0, totalQuestions: 0,
    aiReport: null
  },

  onLoad(options) {
    var data = getApp().globalData._taskQuiz
    getApp().globalData._taskQuiz = null
    if (!data) { wx.showToast({ title: '数据丢失', icon: 'none' }); setTimeout(() => wx.navigateBack(), 1000); return }
    this.setData({
      taskId: data.taskId, courseTitle: data.courseTitle, courseId: data.courseId,
      questions: data.questions, totalQuestions: data.questions.length, loading: false
    })
  },

  selectOption(e) {
    var idx = e.currentTarget.dataset.index
    var answers = this.data.answers
    answers.push({ qId: this.data.questions[this.data.currentIndex].id, selected: idx })
    if (this.data.currentIndex < this.data.questions.length - 1) {
      this.setData({ currentIndex: this.data.currentIndex + 1, answers })
    } else {
      this.setData({ answers })
      this.submitAnswers()
    }
  },

  submitAnswers() {
    this.setData({ loading: true })
    var that = this
    var ui = getApp().globalData.userInfo || {}
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({
      url: baseURL + '/quiztask/submit', method: 'POST',
      header: { 'Content-Type': 'application/json', Token: wx.getStorageSync('token') },
      data: JSON.stringify({
        taskId: this.data.taskId, courseId: this.data.courseId,
        courseTitle: this.data.courseTitle,
        studentaccount: ui.studentaccount || wx.getStorageSync('nickname'),
        studentname: ui.studentname || wx.getStorageSync('nickname') || '',
        answers: this.data.answers
      }),
      success: function(res) {
        if (res.data.code === 0) {
          var data = res.data.data
          var report = null
          try { report = JSON.parse(data.aiReport) } catch(e) {}
          that.setData({
            loading: false, finished: true,
            score: data.score, correctCount: data.correctCount,
            totalQuestions: data.totalQuestions, aiReport: report || data.aiReport
          })
        } else {
          wx.showToast({ title: '提交失败', icon: 'none' })
          that.setData({ loading: false })
        }
      },
      fail: function() { that.setData({ loading: false }); wx.showToast({ title: '网络错误', icon: 'none' }) }
    })
  },

  viewWrongbook() { wx.navigateTo({ url: '/pages/quiz/wrongbook' }) },
  goBack() { wx.switchTab({ url: '/pages/index/index' }) }
})
