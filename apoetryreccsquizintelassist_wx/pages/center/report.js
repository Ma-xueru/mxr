Page({
  data: {
    loading: true, chartData: [], historyList: [], aiComment: '',
    totalCount: 0, avgScore: 0, bestScore: 0
  },

  onLoad() { this.loadData() },

  loadData() {
    var that = this
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({
      url: baseURL + '/followread/all-learning-records',
      method: 'GET', header: { Token: wx.getStorageSync('token') },
      success: function(res) {
        if (res.data.code !== 0) { that.setData({ loading: false }); return }
        var desc = (res.data.data && res.data.data.desc) || []
        var asc = (res.data.data && res.data.data.asc) || []

        // 格式化列表
        var list = desc.map(function(r) {
          var d = new Date(r.timestamp)
          var timeStr = (d.getMonth()+1) + '/' + d.getDate() + ' ' +
            String(d.getHours()).padStart(2,'0') + ':' + String(d.getMinutes()).padStart(2,'0')
          return {
            type: r.type, score: r.score, courseTitle: r.courseTitle,
            timeStr: timeStr, detail: r.detail
          }
        })

        // 统计
        var total = desc.length
        var sum = 0, best = 0
        desc.forEach(function(r) { sum += r.score; if (r.score > best) best = r.score })
        var avg = total > 0 ? Math.round(sum / total) : 0

        // AI 点评
        var comment = ''
        if (asc.length >= 3) {
          var first = asc[0].score, last = asc[asc.length-1].score
          if (last > first + 5) comment = '进步明显！继续保持，你在稳步提升✨'
          else if (last < first - 5) comment = '最近有所下滑，多复习之前的错题哦💪'
          else comment = '成绩稳定，再接再厉向满分冲刺🚀'
        } else if (asc.length > 0) {
          comment = '刚开始积累数据，坚持练习就能看到成长曲线📈'
        }

        that.setData({
          historyList: list, chartData: asc, aiComment: comment,
          totalCount: total, avgScore: avg, bestScore: best, loading: false
        })
        that.drawChart(asc)
      },
      fail: function() { that.setData({ loading: false }) }
    })
  },

  drawChart(data) {
    var that = this
    var query = wx.createSelectorQuery().in(this)
    query.select('#trendChart').fields({ node: true, size: true }).exec(function(res) {
      if (!res || !res[0] || !res[0].node) return
      var canvas = res[0].node
      var ctx = canvas.getContext('2d')
      var w = res[0].width, h = res[0].height
      var dpr = wx.getSystemInfoSync().pixelRatio
      canvas.width = w * dpr; canvas.height = h * dpr
      ctx.scale(dpr, dpr)

      if (!data.length) return
      var pad = { top: 16, right: 16, bottom: 24, left: 36 }
      var pw = w - pad.left - pad.right, ph = h - pad.top - pad.bottom

      var minScore = Math.max(0, Math.min.apply(null, data.map(function(d) { return d.score })) - 5)
      var maxScore = 100, scoreRange = maxScore - minScore || 1
      var stepX = data.length > 1 ? pw / (data.length - 1) : pw

      // 背景网格
      ctx.strokeStyle = '#e8e0d0'; ctx.lineWidth = 0.5
      for (var i = 0; i <= 4; i++) {
        var gy = pad.top + ph * i / 4
        ctx.beginPath(); ctx.moveTo(pad.left, gy); ctx.lineTo(w - pad.right, gy); ctx.stroke()
      }

      var followPts = [], quizPts = []
      data.forEach(function(d, i) {
        var x = pad.left + stepX * i
        var y = pad.top + ph - (d.score - minScore) / scoreRange * ph
        if (d.type === 'follow') followPts.push({ x: x, y: y, score: d.score })
        else quizPts.push({ x: x, y: y, score: d.score })
      })

      // 跟读折线(绿)
      if (followPts.length > 1) {
        ctx.strokeStyle = '#4CAF50'; ctx.lineWidth = 2
        ctx.beginPath()
        followPts.forEach(function(p, i) { i === 0 ? ctx.moveTo(p.x, p.y) : ctx.lineTo(p.x, p.y) })
        ctx.stroke()
      }
      followPts.forEach(function(p) {
        ctx.fillStyle = '#4CAF50'; ctx.beginPath(); ctx.arc(p.x, p.y, 3.5, 0, 2*Math.PI); ctx.fill()
      })

      // 测验折线(橙)
      if (quizPts.length > 1) {
        ctx.strokeStyle = '#FF9800'; ctx.lineWidth = 2
        ctx.beginPath()
        quizPts.forEach(function(p, i) { i === 0 ? ctx.moveTo(p.x, p.y) : ctx.lineTo(p.x, p.y) })
        ctx.stroke()
      }
      quizPts.forEach(function(p) {
        ctx.fillStyle = '#FF9800'; ctx.beginPath(); ctx.arc(p.x, p.y, 3.5, 0, 2*Math.PI); ctx.fill()
      })
    })
  },

  viewDetail(e) {
    var idx = e.currentTarget.dataset.index
    var item = this.data.historyList[idx]
    if (!item || !item.detail) return
    var detail = item.detail
    try { detail = JSON.parse(detail) } catch(e) {}
    var content = item.type === 'follow'
      ? '综合评分: ' + (detail.overallScore || '') + '\n' + (detail.overallComment || '')
      : '错题数: ' + (Array.isArray(detail) ? detail.length : 0) + '题'
    wx.showModal({ title: item.courseTitle || '详情', content: content, showCancel: false })
  }
})
