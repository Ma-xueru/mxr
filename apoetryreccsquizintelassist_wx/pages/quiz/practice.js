Page({
  data: {
    poemId: '', poemTitle: '', poemContent: '',
    questions: [], currentIndex: 0,
    answered: false, showAnalysis: false, selectedIndex: -1,
    correctCount: 0, wrongList: [],
    score: 0, loading: true, finished: false,
    timerDisplay: '00:00', startTime: 0, totalDuration: 0
  },
  timer: null,

  async onLoad(options) {
    const id = options.id
    const title = decodeURIComponent(options.title || '')
    const content = decodeURIComponent(options.content || '')
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
    this.setData({ finished: true, totalDuration, timerDisplay: fmtTime(totalDuration), score, correctCount })

    // 保存到云数据库
    if (wx.cloud) {
      wx.cloud.database().collection('quiz_records').add({
        data: {
          poem_id: this.data.poemId,
          poem_title: this.data.poemTitle,
          score: score,
          duration: totalDuration,
          questions_count: questions.length,
          wrong_list: this.data.wrongList,
          timestamp: Date.now(),
          create_time: new Date()
        }
      }).catch(() => {})
    }
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
