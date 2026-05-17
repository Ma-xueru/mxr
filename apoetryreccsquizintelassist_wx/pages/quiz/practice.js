Page({
  data: {
    poemId: '', poemTitle: '', poemContent: '',
    questions: [], currentIndex: 0,
    answered: false, showAnalysis: false, selectedIndex: -1,
    correctCount: 0, wrongList: [],
    score: 0, loading: true, finished: false,
    timerDisplay: '00:00', startTime: 0, totalDuration: 0,
    aiReport: null, aiLoading: false
  },
  timer: null,

  async onLoad(options) {
    const id = options.id
    const title = decodeURIComponent(options.title || '')
    const content = decodeURIComponent(options.content || '')
    const source = options.source || 'quiz'
    // source → sourceType: quiz=6, analogy=7, review=8
    var sourceType = source === 'analogy' ? 7 : source === 'review' ? 8 : 6
    this._sourceType = sourceType
    if (id === 'ai') {
      // AI 预生成题目，直接从 globalData 取
      var qs = getApp().globalData._aiQuestions
      getApp().globalData._aiQuestions = null
      if (!qs || !qs.length) { wx.showToast({ title: '题目丢失', icon: 'none' }); return }
      var questions = qs.map(function(q) { q.status = ''; return q })
      this.setData({ poemId: 'ai', poemTitle: title, poemContent: content, questions: questions, loading: false })
      this.startTimer()
      return
    }
    if (!id || !content) {
      wx.showToast({ title: '参数缺失', icon: 'none' })
      return
    }
    this.setData({ poemId: id, poemTitle: title, poemContent: content })
    await this.generateQuiz()
  },

  async generateQuiz() {
    var that = this
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({
      url: baseURL + '/voice/generateQuiz',
      method: 'GET',
      data: { poemTitle: this.data.poemTitle, poemContent: this.data.poemContent },
      header: { Token: wx.getStorageSync('token') },
      success: function(res) {
        if (res.data && res.data.code === 0 && res.data.data) {
          var questions = JSON.parse(res.data.data)
          questions = questions.map(function(q) { q.status = ''; return q })
          that.setData({ questions: questions, loading: false })
          that.startTimer()
        } else {
          wx.showModal({ title: '出题失败', content: res.data?.msg || '请重试', showCancel: false,
            success: function() { wx.navigateBack() } })
        }
      },
      fail: function(e) {
        wx.showModal({ title: '调用失败', content: e.errMsg || JSON.stringify(e), showCancel: false,
          success: function() { wx.navigateBack() } })
      }
    })
  },

  startTimer() {
    this.data.startTime = Date.now()
    this.timer = setInterval(() => {
      const elapsed = Math.floor((Date.now() - this.data.startTime) / 1000)
      this.setData({ timerDisplay: fmtTime(elapsed) })
    }, 1000)
  },

  selectOption(e) {
    if (this.data.answered) return
    const idx = e.currentTarget.dataset.index
    const q = this.data.questions[this.data.currentIndex]
    const isCorrect = idx === q.answer
    const questions = [...this.data.questions]
    const qCopy = { ...questions[this.data.currentIndex] }
    const options = qCopy.options.map((opt, i) => {
      if (i === q.answer) return 'correct'      // 正确答案标绿
      if (i === idx && !isCorrect) return 'wrong' // 选错标红
      return 'dimmed'                             // 其余变灰
    })
    qCopy.status = options
    questions[this.data.currentIndex] = qCopy
    const wrongList = [...this.data.wrongList]
    if (!isCorrect) wrongList.push({ question: q.question, options: q.options, answer: q.answer, analysis: q.analysis, selected: idx })
    this.setData({
      questions, answered: true, showAnalysis: !isCorrect,
      selectedIndex: idx, correctCount: this.data.correctCount + (isCorrect ? 1 : 0),
      wrongList
    })
  },

  nextQuestion() {
    if (this.data.currentIndex < this.data.questions.length - 1) {
      this.setData({ currentIndex: this.data.currentIndex + 1, answered: false, showAnalysis: false, selectedIndex: -1 })
      wx.pageScrollTo({ scrollTop: 0, duration: 300 })
    } else {
      this.finishQuiz()
    }
  },

  finishQuiz() {
    clearInterval(this.timer)
    const totalDuration = Math.floor((Date.now() - this.data.startTime) / 1000)
    const questions = this.data.questions
    const correctCount = this.data.correctCount
    const score = Math.round((correctCount / questions.length) * 100)
    this.setData({ finished: true, totalDuration, timerDisplay: fmtTime(totalDuration), score, correctCount, aiLoading: true })

    var ui = getApp().globalData.userInfo || {}
    var baseURL = wx.getStorageSync('baseURL') || ''
    var that = this

    // 保存记录 + AI评估
    wx.request({
      url: baseURL + '/quiz/evaluate',
      method: 'POST',
      header: { 'Content-Type': 'application/json', Token: wx.getStorageSync('token') },
      data: JSON.stringify({
        poemTitle: this.data.poemTitle,
        score: score,
        correctCount: correctCount,
        totalQuestions: questions.length,
        studentaccount: ui.studentaccount || wx.getStorageSync('nickname'),
        studentname: ui.studentname || wx.getStorageSync('nickname') || '',
        courseid: this.data.poemId === 'ai' ? (this.data.poemId === 'ai' ? 0 : parseInt(this.data.poemId) || 0) : parseInt(this.data.poemId) || 0,
        wrongList: this.data.wrongList,
        sourceType: this._sourceType || 6
      }),
      success: function(res) {
        if (res.data && res.data.code === 0) {
          var data = res.data.data
          var report = null
          try { report = typeof data.aiReport === 'string' ? JSON.parse(data.aiReport) : data.aiReport } catch(e) {}
          that.setData({ aiReport: report || data.aiReport, aiLoading: false }, function() {
            if (report && report.dimensions) that.drawRadar()
          })
        } else {
          that.setData({ aiLoading: false })
        }
      },
      fail: function() { that.setData({ aiLoading: false }) }
    })
    this._migrateCloudData()
  },

  _migrateCloudData() {
    if (!wx.cloud || !wx.getStorageSync('_quiz_migrated')) return
    var that = this
    var baseURL = wx.getStorageSync('baseURL') || ''
    var ui = getApp().globalData.userInfo || {}
    wx.cloud.database().collection('quiz_records').limit(50).get()
      .then(function(res) {
        if (!res.data || !res.data.length) return
        var remain = []
        res.data.forEach(function(r) {
          remain.push({
            studentaccount: ui.studentaccount || wx.getStorageSync('nickname'),
            studentname: ui.studentname || wx.getStorageSync('nickname') || '',
            courseid: r.poem_id, coursetitle: r.poem_title,
            score: r.score, duration: r.duration,
            questionsCount: r.questions_count, correctCount: Math.round(r.score / 100 * (r.questions_count || 5)),
            wrongListJson: JSON.stringify(r.wrong_list || [])
          })
        })
        if (!remain.length) return
        wx.request({
          url: baseURL + '/quiz/migrate',
          method: 'POST', header: { 'Content-Type': 'application/json', Token: wx.getStorageSync('token') },
          data: JSON.stringify(remain),
          success: function() { wx.setStorageSync('_quiz_migrated', '1') }
        })
      }).catch(function() {})
  },

  drawRadar() {
    var report = this.data.aiReport
    if (!report || !report.dimensions) return
    var dims = report.dimensions
    var that = this
    var query = wx.createSelectorQuery().in(this)
    query.select('#practiceRadarCanvas').fields({ node: true, size: true }).exec(function(res) {
      if (!res || !res[0] || !res[0].node) return
      var canvas = res[0].node, ctx = canvas.getContext('2d')
      var w = res[0].width, h = res[0].height
      var dpr = wx.getSystemInfoSync().pixelRatio
      canvas.width = w * dpr; canvas.height = h * dpr; ctx.scale(dpr, dpr)
      var cx = w/2, cy = h/2, r = Math.min(w,h)/2 - 30
      var n = dims.length, step = Math.PI*2/n
      for (var lv = 1; lv <= 5; lv++) {
        var lr = r * lv / 5
        ctx.beginPath()
        for (var i = 0; i <= n; i++) {
          var a = step*i - Math.PI/2, x = cx + lr*Math.cos(a), y = cy + lr*Math.sin(a)
          i === 0 ? ctx.moveTo(x,y) : ctx.lineTo(x,y)
        }
        ctx.closePath(); ctx.strokeStyle = '#e8e0d0'; ctx.lineWidth = 0.5; ctx.stroke()
      }
      for (var i = 0; i < n; i++) {
        ctx.beginPath(); ctx.moveTo(cx,cy)
        ctx.lineTo(cx + r*Math.cos(step*i-Math.PI/2), cy + r*Math.sin(step*i-Math.PI/2))
        ctx.strokeStyle = '#e8e0d0'; ctx.stroke()
      }
      ctx.beginPath()
      for (var i = 0; i < n; i++) {
        var val = (dims[i].score || 50) / 100
        var a = step*i - Math.PI/2, x = cx + r*val*Math.cos(a), y = cy + r*val*Math.sin(a)
        i === 0 ? ctx.moveTo(x,y) : ctx.lineTo(x,y)
      }
      ctx.closePath(); ctx.fillStyle = 'rgba(129,199,132,0.2)'; ctx.fill()
      ctx.strokeStyle = '#4CAF50'; ctx.lineWidth = 2; ctx.stroke()
      var colors = ['#e57373','#64B5F6','#FFB74D']
      for (var i = 0; i < n; i++) {
        var val = (dims[i].score || 50) / 100
        var a = step*i - Math.PI/2
        ctx.fillStyle = colors[i%3]
        ctx.beginPath(); ctx.arc(cx + r*val*Math.cos(a), cy + r*val*Math.sin(a), 4, 0, 2*Math.PI); ctx.fill()
        ctx.fillStyle = '#555'; ctx.font = '12px sans-serif'; ctx.textAlign = 'center'
        ctx.fillText(dims[i].name + ' ' + (dims[i].score||0), cx + (r+28)*Math.cos(a), cy + (r+28)*Math.sin(a)+4)
      }
    })
  },

  retryQuiz() {
    clearInterval(this.timer)
    wx.redirectTo({ url: '/pages/quiz/practice?id=' + this.data.poemId +
      '&title=' + encodeURIComponent(this.data.poemTitle) +
      '&content=' + encodeURIComponent(this.data.poemContent) })
  },

  goBack() {
    clearInterval(this.timer)
    wx.navigateBack()
  },

  onUnload() { clearInterval(this.timer) }
})

function fmtTime(sec) {
  const m = Math.floor(sec / 60), s = Math.floor(sec % 60)
  return (m < 10 ? '0' + m : m) + ':' + (s < 10 ? '0' + s : s)
}
