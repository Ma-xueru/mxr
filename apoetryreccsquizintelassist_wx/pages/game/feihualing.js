var keywords = ['月','花','风','云','山','水','春','秋','日','雨','雪','夜','人','心','梦','红','白','金','玉','柳']
Page({
  data: {
    keyword: '', messages: [], inputText: '', waiting: false,
    roundCount: 1, scrollToId: '', score: 0, combo: 0,
    lives: 3, timerSeconds: 30, timerDisplay: '30',
    showResult: false, resultData: null,
    inputWarning: ''
  },
  _timer: null, _animTimer: null,

  onLoad() { this.resetGame() },

  resetGame() {
    var kw = keywords[Math.floor(Math.random() * keywords.length)]
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({ url: baseURL + '/game/reset?sessionId=default', method: 'GET', header: { Token: wx.getStorageSync('token') } })
    wx.request({ url: baseURL + '/game/init?keyword=' + kw + '&sessionId=default', method: 'GET', header: { Token: wx.getStorageSync('token') } })
    this._stopTimer()
    this.setData({
      keyword: kw, messages: [
        { from: 'ai', text: '飞花令开始！关键字是「' + kw + '」，请说出含此字的古诗词句～\n你有 ❤️❤️❤️ 三次机会', source: '', comment: '', typing: false }
      ], waiting: false, inputText: '', score: 0, combo: 0,
      lives: 3, showResult: false, resultData: null, roundCount: 1,
      timerSeconds: 30, timerDisplay: '30'
    })
    this._startTimer()
  },

  _startTimer() {
    var that = this
    this._stopTimer()
    this.setData({ timerSeconds: 30, timerDisplay: '30' })
    this._timer = setInterval(function() {
      var t = that.data.timerSeconds - 1
      if (t <= 0) { that._onTimeout(); return }
      that.setData({ timerSeconds: t, timerDisplay: t < 10 ? '0' + t : String(t) })
    }, 1000)
  },

  _stopTimer() { if (this._timer) { clearInterval(this._timer); this._timer = null } },

  _onTimeout() {
    this._stopTimer()
    var that = this
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({ url: baseURL + '/game/timeout?sessionId=default', method: 'GET', header: { Token: wx.getStorageSync('token') },
      success: function(res) {
        var data = (res.data && res.data.data) || {}
        var msgs = that.data.messages
        msgs.push({ from: 'ai', text: '⏰ 超时！' + (data.reason || ''), source: '', comment: '', typing: false })
        if (data.aiPoem) msgs.push({ from: 'ai', text: data.aiPoem, source: data.source || '', comment: '', typing: false })
        that.setData({ messages: msgs, lives: data.lives || 0, score: data.score || 0 })
        if (data.gameOver) { that._showSettlement() }
        else { that.setData({ combo: 0 }); that._startTimer() }
      },
      fail: function() { that._startTimer() }
    })
  },

  onInput(e) {
    var val = e.detail.value
    this.setData({ inputText: val, inputWarning: '' })
  },

  sendPoem() {
    var text = this.data.inputText.trim()
    if (!text || this.data.waiting) return

    // 非中文拦截
    if (!/[一-龥]/.test(text)) {
      this.setData({ inputWarning: '请输入中文古诗词句', inputText: '' })
      wx.showToast({ title: '请输入中文诗句', icon: 'none', duration: 1500 })
      return
    }

    this._stopTimer()
    var msgs = this.data.messages
    msgs.push({ from: 'user', text: text, valid: true, reason: '' })
    msgs.push({ from: 'ai', text: '', source: '', comment: '', typing: true })
    this.setData({ messages: msgs, inputText: '', inputWarning: '', waiting: true, scrollToId: 'chat-bottom' })

    var that = this
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({
      url: baseURL + '/game/fei-hua-ling',
      method: 'GET',
      data: { keyword: this.data.keyword, userPoem: text, sessionId: 'default' },
      header: { Token: wx.getStorageSync('token') },
      success: function(res) {
        var data = (res.data && res.data.data) || {}
        var msgs = that.data.messages; msgs.pop()
        if (!data.isValid) {
          msgs[msgs.length-1].valid = false
          msgs[msgs.length-1].reason = data.reason || '无效输入'
        }
        msgs.push({ from: 'ai', typing: false, text: data.aiPoem || 'AI休息中～', source: data.source || '', comment: data.aiComment || '' })
        that.setData({
          messages: msgs, waiting: false, lives: data.lives || 0,
          roundCount: (data.roundCount || 0) + 1, score: data.score || 0,
          combo: data.combo || 0, scrollToId: 'chat-bottom'
        })
        if (data.gameOver) { that._showSettlement() }
        else { that._startTimer() }
      },
      fail: function() {
        var msgs = that.data.messages; msgs.pop()
        msgs.push({ from: 'ai', text: '网络好像不太好，再试一次吧～', source: '', comment: '', typing: false })
        that.setData({ messages: msgs, waiting: false }); that._startTimer()
      }
    })
  },

  _showSettlement() {
    this._stopTimer()
    var that = this
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({
      url: baseURL + '/game/settlement?sessionId=default', method: 'GET',
      header: { Token: wx.getStorageSync('token') },
      success: function(res) {
        var data = (res.data && res.data.data) || {}
        that.setData({ showResult: true, resultData: data })
        that._animateScore(data.score || 0)
      }
    })
  },

  _animateScore(target) {
    var that = this
    var current = 0, step = Math.max(1, Math.floor(target / 30))
    clearInterval(this._animTimer)
    this._animTimer = setInterval(function() {
      current += step
      if (current >= target) { current = target; clearInterval(that._animTimer) }
      var d = that.data.resultData || {}
      d.score = current; that.setData({ resultData: d })
    }, 50)
  },

  closeResult() { wx.navigateBack() },

  exitGame() {
    if (this.data.score > 0 && !this.data.showResult) { this._showSettlement() }
    else { wx.navigateBack() }
  },

  onUnload() { this._stopTimer(); clearInterval(this._animTimer) }
})
