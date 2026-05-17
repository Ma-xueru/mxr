Page({
  data: {
    activeTab: 'follow',
    counts: { followCount: 0, quizCount: 0, analogyCount: 0, reviewCount: 0, totalCount: 0 },
    records: [],
    selectedRecord: null,
    loading: false,
    showDetail: false
  },

  onLoad() {
    this.loadCounts()
    this.loadRecords(4)
  },

  loadCounts() {
    var that = this
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({
      url: baseURL + '/followread/myAutonomousHistory?sourceType=4', method: 'GET',
      header: { Token: wx.getStorageSync('token') },
      success: function(res) {
        if (res.data && res.data.code === 0) {
          that.setData({ counts: {
            followCount: res.data.data.followCount || 0,
            quizCount: res.data.data.quizCount || 0,
            analogyCount: res.data.data.analogyCount || 0,
            reviewCount: res.data.data.reviewCount || 0,
            totalCount: res.data.data.totalCount || 0
          }})
        }
      }
    })
  },

  switchTab(e) {
    var tab = e.currentTarget.dataset.tab
    var typeMap = { follow: 4, quiz: 6, analogy: 7, review: 8 }
    this.setData({ activeTab: tab, selectedRecord: null, showDetail: false })
    this.loadRecords(typeMap[tab])
  },

  loadRecords(sourceType) {
    var that = this
    this.setData({ loading: true, records: [] })
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({
      url: baseURL + '/followread/myAutonomousHistory?sourceType=' + sourceType, method: 'GET',
      header: { Token: wx.getStorageSync('token') },
      success: function(res) {
        if (res.data && res.data.code === 0) {
          that.setData({ records: res.data.data.list || [], loading: false })
        } else {
          that.setData({ loading: false })
        }
      },
      fail: function() { that.setData({ loading: false }) }
    })
  },

  selectRecord(e) {
    var idx = e.currentTarget.dataset.index
    var record = this.data.records[idx]
    this.setData({ selectedRecord: record, showDetail: true }, function() {
      setTimeout(function() { this.drawRadar() }.bind(this), 300)
    }.bind(this))
  },

  closeDetail() {
    this.setData({ showDetail: false, selectedRecord: null })
  },

  drawRadar() {
    var record = this.data.selectedRecord
    if (!record) return
    var report = null
    if (record.reportJson) {
      try { report = JSON.parse(record.reportJson) } catch(e) {}
    }
    // 兜底：用列存分数
    if (!report || !report.dimensions) {
      report = {
        dimensions: [
          { name: '知识掌握度', score: record.knowledgeScore || 0, comment: '' },
          { name: '答题准确率', score: record.accuracyScore || 0, comment: '' },
          { name: '理解深度', score: record.depthScore || 0, comment: '' }
        ],
        suggestion: record.learningSuggestion || '',
        overallComment: record.overallSummary || ''
      }
    }
    this._parsedReport = report
    this.setData({ parsedReport: report })

    var dims = report.dimensions
    var that = this
    var query = wx.createSelectorQuery().in(this)
    query.select('#historyRadarCanvas').fields({ node: true, size: true }).exec(function(res) {
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
      var colors = ['#e57373','#64B5F6','#FFB74D','#81C784']
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

  fmtTime(v) {
    if (!v) return ''
    var d = new Date(v)
    var pad = function(n) { return String(n).padStart(2, '0') }
    return d.getFullYear() + '-' + pad(d.getMonth()+1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes())
  }
})
