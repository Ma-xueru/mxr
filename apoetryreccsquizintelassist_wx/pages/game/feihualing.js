var keywords = ['月','花','风','云','山','水','春','秋','日','雨','雪','夜','人','心','梦','红','白','金','玉','柳']
Page({
  data: {
    keyword: '', messages: [], inputText: '', waiting: false,
    roundCount: 1, scrollToId: '', score: 0, combo: 0, maxCombo: 0,
    showResult: false, resultRank: 0, resultTitle: ''
  },
  _scoreAnimTimer: null,

  onLoad() { this.resetGame() },

  resetGame() {
    var kw = keywords[Math.floor(Math.random() * keywords.length)]
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({ url: baseURL + '/game/reset?sessionId=default', method: 'GET', header: { Token: wx.getStorageSync('token') } })
    this.setData({
      keyword: kw, messages: [
        { from: 'ai', text: '飞花令开始！关键字是「' + kw + '」，请说出含此字的古诗词句～', source: '', comment: '', typing: false }
      ], waiting: false, inputText: '',
      score: 0, combo: 0, maxCombo: 0, showResult: false, roundCount: 1
    })
  },

  onInput(e) { this.setData({ inputText: e.detail.value }) },

  sendPoem() {
    var text = this.data.inputText.trim()
    if (!text || this.data.waiting) return
    var msgs = this.data.messages
    msgs.push({ from: 'user', text: text, valid: true, reason: '' })
    msgs.push({ from: 'ai', text: '', source: '', comment: '', typing: true })
    this.setData({ messages: msgs, inputText: '', waiting: true, scrollToId: 'chat-bottom' })

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
        if (!data.userValid) {
          msgs[msgs.length-1].valid = false
          msgs[msgs.length-1].reason = data.reason || '不含关键字'
        }
        msgs.push({
          from: 'ai', typing: false,
          text: data.aiPoem || 'AI休息中，经典名句顶上～',
          source: data.source || '', comment: data.aiComment || ''
        })
        var combo = data.combo || 0, maxCombo = Math.max(that.data.maxCombo, combo)
        that.setData({
          messages: msgs, waiting: false,
          roundCount: (data.roundCount || that.data.roundCount) + 1,
          score: data.score || that.data.score, combo: combo, maxCombo: maxCombo,
          scrollToId: 'chat-bottom'
        })
      },
      fail: function() {
        var msgs = that.data.messages; msgs.pop()
        msgs.push({ from: 'ai', text: '网络好像不太好，再试一次吧～', source: '', comment: '', typing: false })
        that.setData({ messages: msgs, waiting: false })
      }
    })
  },

  exitGame() {
    if (this.data.score > 0) {
      this._saveAndShowResult()
    } else {
      wx.navigateBack()
    }
  },

  _saveAndShowResult() {
    var that = this
    var baseURL = wx.getStorageSync('baseURL') || ''
    // 保存成绩
    wx.request({
      url: baseURL + '/game/saveRecord', method: 'GET',
      data: { sessionId: 'default', score: this.data.score, rounds: this.data.roundCount - 1, maxCombo: this.data.maxCombo, keyword: this.data.keyword },
      header: { Token: wx.getStorageSync('token') }
    })
    // 查询排名
    wx.request({
      url: baseURL + '/game/rank?score=' + this.data.score, method: 'GET',
      header: { Token: wx.getStorageSync('token') },
      success: function(res) {
        var pct = (res.data && res.data.data && res.data.data.percentage) || 50
        var title = that.data.score < 30 ? '诗词书童' : that.data.score < 80 ? '翰林学士' : '一代诗宗'
        that.setData({ showResult: true, resultRank: pct, resultTitle: title })
        that._animateScore(that.data.score)
      }
    })
  },

  _animateScore(target) {
    var that = this
    var current = 0, step = Math.max(1, Math.floor(target / 30))
    clearInterval(this._scoreAnimTimer)
    this._scoreAnimTimer = setInterval(function() {
      current += step
      if (current >= target) { current = target; clearInterval(that._scoreAnimTimer) }
      that.setData({ score: current })
    }, 50)
  },

  closeResult() { wx.navigateBack() },

  onUnload() { clearInterval(this._scoreAnimTimer) }
})
