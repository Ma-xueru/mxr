var keywords = ['月','花','风','云','山','水','春','秋','日','雨','雪','夜','人','心','梦','红','白','金','玉','柳']
Page({
  data: {
    keyword: '', messages: [], inputText: '', waiting: false,
    roundCount: 1, scrollToId: '', score: 0, combo: 0,
    lives: 3, timerSeconds: 30, timerDisplay: '30',
    showResult: false, resultData: null,
    inputWarning: ''
  },
  _timer: null, _animTimer: null, _recorder: null, recording: false,

  onLoad() { this.resetGame() },

  resetGame() {
    var kw = keywords[Math.floor(Math.random() * keywords.length)]
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({ url: baseURL + '/game/reset?sessionId=default', method: 'GET', header: { Token: wx.getStorageSync('token') } })
    wx.request({ url: baseURL + '/game/init?keyword=' + kw + '&sessionId=default', method: 'GET', header: { Token: wx.getStorageSync('token') } })
    this._stopTimer()
    var welcome = '飞花令开始！关键字是「' + kw + '」，请说出含此字的古诗词句，你有三次机会～'
    this.setData({
      keyword: kw, messages: [
        { from: 'ai', text: welcome, source: '', comment: '', typing: false }
      ], waiting: false, inputText: '', score: 0, combo: 0,
      lives: 3, showResult: false, resultData: null, roundCount: 1,
      timerSeconds: 30, timerDisplay: '30'
    })
    this._ttsPlay(welcome)
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
        if (data.aiPoem) { msgs.push({ from: 'ai', text: data.aiPoem, source: data.source || '', comment: data.aiComment || '', typing: false }); that._ttsPlay(data.aiPoem + (data.aiComment ? '。' + data.aiComment : '')) }
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

  // 录音：按下开始
  startRecord() {
    var that = this
    var rec = wx.getRecorderManager()
    this._recorder = rec
    rec.onStart(function () { that.setData({ recording: true }) })
    rec.onStop(function (res) {
      that.setData({ recording: false })
      if (!res.tempFilePath) return
      wx.showLoading({ title: '识别中...' })
      wx.uploadFile({
        url: (wx.getStorageSync('baseURL') || '') + '/voice/chat',
        filePath: res.tempFilePath, name: 'audio',
        header: { Token: wx.getStorageSync('token') },
        success: function(upRes) {
          wx.hideLoading()
          try {
            var data = JSON.parse(upRes.data)
            var text = (data.data && data.data.recognized) || ''
            if (text) {
              that.setData({ inputText: text, inputWarning: '' })
              wx.showToast({ title: '识别成功', icon: 'success', duration: 800 })
              // 自动发送
              setTimeout(function () { if (that.data.inputText === text) that.sendPoem() }, 300)
            } else { wx.showToast({ title: '没听清，请重试', icon: 'none' }) }
          } catch (e) { wx.showToast({ title: '识别失败', icon: 'none' }) }
        },
        fail: function () { wx.hideLoading(); wx.showToast({ title: '网络错误', icon: 'none' }) }
      })
    })
    rec.onError(function () { that.setData({ recording: false }) })
    rec.start({ duration: 10000, sampleRate: 16000, numberOfChannels: 1, encodeBitRate: 48000, format: 'aac' })
  },

  // 录音：松开停止
  stopRecord() {
    this.setData({ recording: false })
    if (this._recorder) { try { this._recorder.stop() } catch (e) {}; this._recorder = null }
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
      data: { keyword: this.data.keyword, userPoem: text, sessionId: 'default', characterId: (getApp().globalData.selectedCharacterId) || '' },
      header: { Token: wx.getStorageSync('token') },
      success: function(res) {
        var data = (res.data && res.data.data) || {}
        var msgs = that.data.messages; msgs.pop()
        if (!data.isValid) {
          msgs[msgs.length-1].valid = false
          msgs[msgs.length-1].reason = data.reason || '无效输入'
        }
        var aiText = data.aiPoem || ''
        msgs.push({ from: 'ai', typing: false, text: aiText || 'AI休息中～', source: data.source || '', comment: data.aiComment || '' })
        that.setData({
          messages: msgs, waiting: false, lives: data.lives || 0,
          roundCount: (data.roundCount || 0) + 1, score: data.score || 0,
          combo: data.combo || 0, scrollToId: 'chat-bottom'
        })
        // 自动朗读AI对句 + 趣味点评
        var ttsFull = aiText
        if (data.aiComment) ttsFull += '。' + data.aiComment
        if (ttsFull) that._ttsPlay(ttsFull)
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
    var finalScore = this.data.score  // 用前端已累计的分数兜底
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({
      url: baseURL + '/game/settlement?sessionId=default', method: 'GET',
      header: { Token: wx.getStorageSync('token') },
      success: function(res) {
        var data = (res.data && res.data.data) || {}
        var score = data.score || finalScore || 0
        data.score = score
        that.setData({ showResult: true, resultData: data })
        that._animateScore(score)
      },
      fail: function() {
        // 结算接口挂了也不影响展示
        that.setData({ showResult: true, resultData: { score: finalScore, rounds: that.data.roundCount, maxCombo: that.data.combo, rankPct: 50, title: finalScore >= 60 ? '翰林学士' : '诗词书童', keyword: that.data.keyword } })
        that._animateScore(finalScore)
      }
    })
  },

  _animateScore(target) {
    if (!target || target <= 0) return
    var that = this
    var current = 0, step = Math.max(1, Math.floor(target / 25))
    clearInterval(this._animTimer)
    var d = that.data.resultData || {}
    d.score = 0; that.setData({ resultData: d })
    this._animTimer = setInterval(function() {
      current += step
      if (current >= target) { current = target; clearInterval(that._animTimer) }
      var dd = that.data.resultData || {}
      dd.score = current; that.setData({ resultData: dd })
    }, 60)
  },

  closeResult() { wx.navigateBack() },

  // 手动点击小喇叭重播
  playAudio(e) {
    var text = e.currentTarget.dataset.text
    if (text) this._ttsPlay(text)
  },

  // 自动/手动 TTS 播放
  _ttsPlay(text) {
    if (!text) return
    var that = this
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({
      url: baseURL + '/followread/tts',
      method: 'POST',
      data: { text: text },
      header: { Token: wx.getStorageSync('token'), 'Content-Type': 'application/json' },
      success: function(res) {
        var ttsUrl = (res.data && res.data.data) || ''
        if (!ttsUrl) return
        if (ttsUrl.indexOf('http') !== 0) ttsUrl = baseURL + ttsUrl
        var audio = wx.createInnerAudioContext()
        audio.src = ttsUrl
        audio.play()
      }
    })
  },

  exitGame() {
    if (this.data.score > 0 && !this.data.showResult) { this._showSettlement() }
    else { wx.navigateBack() }
  },

  onUnload() { this._stopTimer(); clearInterval(this._animTimer) }
})
