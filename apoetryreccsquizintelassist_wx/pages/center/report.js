Page({
  data: {
    loading: true, stats: {}, radar: {}, trend: [], aiMentor: '',
    history: [], totalCount: 0, avgScore: 0,
    showDetail: false, selectedRecord: null, parsedReport: null
  },

  onLoad() { this.loadData() },

  loadData() {
    var that = this
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({
      url: baseURL + '/followread/comprehensiveReport',
      method: 'GET', header: { Token: wx.getStorageSync('token') },
      success: function(res) {
        if (res.data.code !== 0) { that.setData({ loading: false }); return }
        var d = res.data.data
        that.setData({
          stats: d.stats || {}, radar: d.radar || {}, trend: d.trend || [],
          aiMentor: d.aiMentor || '', history: d.history || [],
          totalCount: d.totalCount || 0, avgScore: d.avgScore || 0, loading: false
        }, function() {
          if (d.radar && d.radar.knowledgeScore !== undefined) that.drawRadar()
          if (d.trend && d.trend.length) setTimeout(function() { that.drawChart() }, 300)
        })
      },
      fail: function() { that.setData({ loading: false }) }
    })
  },

  drawRadar() {
    var r = this.data.radar
    var dims = [
      { name: '知识掌握度', score: r.knowledgeScore || 0 },
      { name: '答题准确率', score: r.accuracyScore || 0 },
      { name: '理解深度', score: r.depthScore || 0 }
    ]
    var that = this
    var query = wx.createSelectorQuery().in(this)
    query.select('#reportRadarCanvas').fields({ node: true, size: true }).exec(function(res) {
      if (!res || !res[0] || !res[0].node) return
      var canvas = res[0].node, ctx = canvas.getContext('2d')
      var w = res[0].width, h = res[0].height
      var dpr = wx.getSystemInfoSync().pixelRatio
      canvas.width = w * dpr; canvas.height = h * dpr; ctx.scale(dpr, dpr)
      var cx = w/2, cy = h/2, r = Math.min(w,h)/2 - 28
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
        ctx.fillText(dims[i].name + ' ' + (dims[i].score||0), cx + (r+24)*Math.cos(a), cy + (r+24)*Math.sin(a)+4)
      }
    })
  },

  drawChart(data) {
    var data = this.data.trend
    var that = this
    var query = wx.createSelectorQuery().in(this)
    query.select('#trendChart').fields({ node: true, size: true }).exec(function(res) {
      if (!res || !res[0] || !res[0].node || !data.length) return
      var canvas = res[0].node, ctx = canvas.getContext('2d')
      var w = res[0].width, h = res[0].height
      var dpr = wx.getSystemInfoSync().pixelRatio
      canvas.width = w * dpr; canvas.height = h * dpr
      ctx.scale(dpr, dpr)
      var pad = { top: 12, right: 12, bottom: 24, left: 34 }
      var pw = w - pad.left - pad.right, ph = h - pad.top - pad.bottom
      var stepX = data.length > 1 ? pw / (data.length - 1) : pw

      // 网格
      ctx.strokeStyle = '#e8e0d0'; ctx.lineWidth = 0.5
      for (var i = 0; i <= 4; i++) {
        var gy = pad.top + ph * i / 4
        ctx.beginPath(); ctx.moveTo(pad.left, gy); ctx.lineTo(w - pad.right, gy); ctx.stroke()
      }

      var types = [{ key: 's4', color: '#4CAF50' }, { key: 's6', color: '#FF9800' }, { key: 's7', color: '#9C27B0' }, { key: 's8', color: '#2196F3' }]
      types.forEach(function(t) {
        var pts = []
        data.forEach(function(d, i) {
          if (d[t.key] > 0) pts.push({ x: pad.left + stepX * i, y: pad.top + ph - (d[t.key] / 100) * ph })
        })
        if (pts.length < 2) return
        ctx.strokeStyle = t.color; ctx.lineWidth = 2
        ctx.beginPath()
        pts.forEach(function(p, i) { i === 0 ? ctx.moveTo(p.x, p.y) : ctx.lineTo(p.x, p.y) })
        ctx.stroke()
        pts.forEach(function(p) { ctx.fillStyle = t.color; ctx.beginPath(); ctx.arc(p.x, p.y, 2.5, 0, 2*Math.PI); ctx.fill() })
      })
    })
  },

  tapHistory(e) {
    var idx = e.currentTarget.dataset.index
    var record = this.data.history[idx]
    if (!record) return
    var report = null
    if (record.reportJson) {
      try { report = JSON.parse(record.reportJson) } catch(e) {}
    }
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
    this.setData({ selectedRecord: record, parsedReport: report, showDetail: true }, function() {
      var that = this
      setTimeout(function() {
        var query = wx.createSelectorQuery().in(that)
        query.select('#detailRadarCanvas').fields({ node: true, size: true }).exec(function(res) {
          if (!res || !res[0] || !res[0].node) return
          var canvas = res[0].node, ctx = canvas.getContext('2d')
          var w = res[0].width, h = res[0].height
          var dpr = wx.getSystemInfoSync().pixelRatio
          canvas.width = w * dpr; canvas.height = h * dpr; ctx.scale(dpr, dpr)
          var cx = w/2, cy = h/2, r = Math.min(w,h)/2 - 26
          var dims = report.dimensions
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
            ctx.beginPath(); ctx.arc(cx + r*val*Math.cos(a), cy + r*val*Math.sin(a), 3.5, 0, 2*Math.PI); ctx.fill()
            ctx.fillStyle = '#555'; ctx.font = '11px sans-serif'; ctx.textAlign = 'center'
            ctx.fillText(dims[i].name + ' ' + (dims[i].score||0), cx + (r+22)*Math.cos(a), cy + (r+22)*Math.sin(a)+4)
          }
        })
      }, 300)
    })
  },

  closeDetail() { this.setData({ showDetail: false, selectedRecord: null, parsedReport: null }) },

  fmtTime(v) {
    if (!v) return ''
    var d = new Date(v), pad = function(n) { return String(n).padStart(2, '0') }
    return d.getFullYear() + '-' + pad(d.getMonth()+1) + '-' + pad(d.getDate())
  },

  fmtFullTime(v) {
    if (!v) return ''
    var d = new Date(v), pad = function(n) { return String(n).padStart(2, '0') }
    return (d.getMonth()+1) + '/' + d.getDate() + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes())
  },

  sourceLabel(t) {
    var m = { 4: '🗣️ 跟读', 6: '✍️ 测验', 7: '🔄 举一反三', 8: '📚 温故知新' }
    return m[t] || '学习'
  }
})
