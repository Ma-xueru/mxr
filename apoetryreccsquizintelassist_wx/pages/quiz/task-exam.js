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
    if (data.status === 'completed') {
      this.loadSavedResult(data.taskId)
    } else {
      this.setData({
        taskId: data.taskId, courseTitle: data.courseTitle, courseId: data.courseId,
        questions: data.questions, totalQuestions: data.questions.length, loading: false
      })
    }
  },

  loadSavedResult(taskId) {
    var that = this
    wx.request({
      url: (wx.getStorageSync('baseURL')||'') + '/quiztask/result?taskId=' + taskId,
      method: 'GET', header: { Token: wx.getStorageSync('token') },
      success: function(res) {
        if (res.data.code === 0) {
          var data = res.data.data
          var report = null
          try { report = JSON.parse(data.aiReport) } catch(e) {}
          that.setData({
            loading: false, finished: true,
            score: data.score, correctCount: data.correctCount,
            totalQuestions: data.totalQuestions, aiReport: report || data.aiReport
          }, function() { that.drawRadar() })
        } else { that.setData({ loading: false }) }
      },
      fail: function() { that.setData({ loading: false }) }
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
          }, function() { that.drawRadar() })
        } else {
          wx.showModal({ title: '提交失败', content: res.data.msg || '请重试', showCancel: false })
          that.setData({ loading: false })
        }
      },
      fail: function(e) { that.setData({ loading: false }); wx.showModal({ title: '网络错误', content: e.errMsg || '连接失败', showCancel: false }) }
    })
  },

  drawRadar() {
    var report = this.data.aiReport
    if (!report || !report.dimensions) return
    var dims = report.dimensions
    var that = this
    var query = wx.createSelectorQuery().in(this)
    query.select('#radarCanvas').fields({ node: true, size: true }).exec(function(res) {
      if (!res || !res[0] || !res[0].node) return
      var canvas = res[0].node, ctx = canvas.getContext('2d')
      var w = res[0].width, h = res[0].height
      var dpr = wx.getSystemInfoSync().pixelRatio
      canvas.width = w * dpr; canvas.height = h * dpr; ctx.scale(dpr, dpr)
      var cx = w/2, cy = h/2, r = Math.min(w,h)/2 - 30
      var n = dims.length, step = Math.PI*2/n
      var colors = ['#e57373','#64B5F6','#FFB74D','#81C784']

      // 网格
      for (var lv = 1; lv <= 5; lv++) {
        var lr = r * lv / 5
        ctx.beginPath()
        for (var i = 0; i <= n; i++) {
          var a = step*i - Math.PI/2, x = cx + lr*Math.cos(a), y = cy + lr*Math.sin(a)
          i === 0 ? ctx.moveTo(x,y) : ctx.lineTo(x,y)
        }
        ctx.closePath(); ctx.strokeStyle = '#e8e0d0'; ctx.lineWidth = 0.5; ctx.stroke()
      }
      // 轴线
      for (var i = 0; i < n; i++) {
        ctx.beginPath(); ctx.moveTo(cx,cy)
        ctx.lineTo(cx + r*Math.cos(step*i-Math.PI/2), cy + r*Math.sin(step*i-Math.PI/2))
        ctx.strokeStyle = '#e8e0d0'; ctx.stroke()
      }
      // 数据
      ctx.beginPath()
      for (var i = 0; i < n; i++) {
        var val = (dims[i].score || 50) / 100
        var a = step*i - Math.PI/2, x = cx + r*val*Math.cos(a), y = cy + r*val*Math.sin(a)
        i === 0 ? ctx.moveTo(x,y) : ctx.lineTo(x,y)
      }
      ctx.closePath(); ctx.fillStyle = 'rgba(129,199,132,0.2)'; ctx.fill()
      ctx.strokeStyle = '#4CAF50'; ctx.lineWidth = 2; ctx.stroke()
      // 标签
      for (var i = 0; i < n; i++) {
        var val = (dims[i].score || 50) / 100
        var a = step*i - Math.PI/2
        ctx.fillStyle = colors[i%4]
        ctx.beginPath(); ctx.arc(cx + r*val*Math.cos(a), cy + r*val*Math.sin(a), 4, 0, 2*Math.PI); ctx.fill()
        ctx.fillStyle = '#555'; ctx.font = '12px sans-serif'; ctx.textAlign = 'center'
        ctx.fillText(dims[i].name + ' ' + (dims[i].score||0), cx + (r+28)*Math.cos(a), cy + (r+28)*Math.sin(a)+4)
      }
    })
  },

  viewWrongbook() { wx.navigateTo({ url: '/pages/quiz/wrongbook' }) },
  goBack() { wx.navigateBack() }
})
