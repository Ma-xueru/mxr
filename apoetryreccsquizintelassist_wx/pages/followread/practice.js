const baseURL = wx.getStorageSync('baseURL') || ''

Page({
  data: {
    poemTitle: '', courseId: 0,
    lines: [], currentLine: 1, totalLines: 0,
    currentText: '', currentTtsUrl: '',
    state: 'idle', playing: false,
    recordTime: 0, recordTimeDisplay: '00:00',
    records: [],
    scoring: false, scoringProgress: 10, scoringHint: '正在上传录音...',
    totalScore: 0, showReport: false,
    report: null, fullText: ''
  },
  recorder: null, recordTimer: null,
  _audio: null, _audioTimer: null,

  async onLoad(options) {
    const id = options.id
    if (!id) return
    this.setData({ courseId: id })
    await this.loadLines()
  },

  async loadLines() {
    wx.showLoading({ title: '加载中...' })
    try {
      const res = await new Promise((resolve, reject) => {
        wx.request({
          url: baseURL + '/followread/lines?courseId=' + this.data.courseId,
          method: 'GET', header: { Token: wx.getStorageSync('token') },
          success: resolve, fail: reject
        })
      })
      const data = res.data.data
      const lines = (data.lines || []).filter(l => l.ttsUrl).map(l => ({
        ...l, ttsUrl: baseURL + l.ttsUrl
      }))
      this.setData({
        poemTitle: data.courseTitle, lines, totalLines: lines.length,
        currentLine: 1, currentText: lines[0]?.text || '',
        currentTtsUrl: lines[0]?.ttsUrl || '',
        state: 'idle', records: [],
        fullText: lines.map(l => l.text).join('')
      })
      this.autoPlay()
    } catch(e) { wx.showToast({ title: '加载失败', icon: 'none' }) }
    wx.hideLoading()
  },

  autoPlay() {
    clearTimeout(this._autoPlayTimer)
    this._autoPlayTimer = setTimeout(() => {
      if (this.data.state === 'idle' && this.data.currentTtsUrl) this.playTTS()
    }, 600)
  },

  playTTS() {
    if (!this.data.currentTtsUrl) return
    // 如果正在播放，点击按钮 = 停止播放
    if (this.data.playing) { this._stopAudio(); return }
    // 销毁旧音频，创建新实例 — 防止旧状态卡死
    this._stopAudio()
    var audio = wx.createInnerAudioContext()
    this._audio = audio
    audio.src = this.data.currentTtsUrl
    // 先绑回调，再播放
    audio.onPlay(() => {
      this.setData({ playing: true })
      // 安全超时：30秒后强制重置（防止网络慢等因素导致回调永不触发）
      clearTimeout(this._audioTimer)
      this._audioTimer = setTimeout(() => { this._onAudioEnd() }, 30000)
    })
    audio.onEnded(() => { this._onAudioEnd() })
    audio.onStop(() => { this._onAudioEnd() })
    audio.onError(() => { this._onAudioEnd() })
    audio.play()
  },

  _onAudioEnd() {
    clearTimeout(this._audioTimer)
    this.setData({ playing: false, state: 'played' })
    this._stopAudio()
  },

  _stopAudio() {
    clearTimeout(this._audioTimer)
    if (this._audio) {
      try { this._audio.destroy() } catch(e) {}
      this._audio = null
    }
  },

  startRecord() {
    this._stopAudio()
    this._cleanup()
    const rec = wx.getRecorderManager()
    this.recorder = rec
    rec.onStart(() => {
      this.setData({ state: 'recording', recordTime: 0, recordTimeDisplay: '00:00' })
      this.recordTimer = setInterval(() => {
        const t = this.data.recordTime + 1
        this.setData({ recordTime: t, recordTimeDisplay: fmtTime(t) })
      }, 1000)
    })
    rec.onStop((res) => {
      clearInterval(this.recordTimer)
      if (res.tempFilePath) {
        const records = [...this.data.records]
        records.push({ text: this.data.currentText, filePath: res.tempFilePath })
        this.setData({ records, state: 'done' })
        if (this.data.currentLine < this.data.totalLines) {
          setTimeout(() => this.nextLine(), 800)
        }
      } else {
        this.setData({ state: 'played' })
      }
    })
    rec.onError(() => { clearInterval(this.recordTimer); this.setData({ state: 'played' }) })
    rec.start({ duration: 60000, sampleRate: 16000, numberOfChannels: 1, encodeBitRate: 48000, format: 'aac' })
  },

  stopRecord() { if (this.recorder) try { this.recorder.stop() } catch(e) {} },

  nextLine() {
    const next = this.data.currentLine + 1
    const line = this.data.lines[next - 1]
    if (!line) return
    this.setData({ currentLine: next, currentText: line.text, currentTtsUrl: line.ttsUrl, state: 'idle' })
    this.autoPlay()
  },

  // ===== 提交评分 =====
  async submitAll() {
    if (!this.data.records.length) return wx.showToast({ title: '请先跟读', icon: 'none' })

    this.setData({ scoring: true, scoringProgress: 10, scoringHint: '正在上传录音...' })
    this._simulateProgress()

    const tasks = this.data.records.map((rec, idx) => new Promise((resolve) => {
      wx.uploadFile({
        url: baseURL + '/followread/score',
        filePath: rec.filePath, name: 'audio',
        formData: { expectedText: rec.text, lineIndex: String(idx + 1) },
        header: { Token: wx.getStorageSync('token') },
        success: (res) => { try { resolve(JSON.parse(res.data).data || {}) } catch(e) { resolve({}) } },
        fail: () => resolve({})
      })
    }))

    const results = await Promise.all(tasks)

    this.setData({ scoringProgress: 70, scoringHint: 'AI 正在综合评分...' })

    const linesParam = this.data.records.map((r, i) => {
      const rs = results[i] || {}
      return r.text + '|' + (rs.recognized || '') + '|' + (rs.score || 0)
    }).join('\n')

    const recText = this.data.records.map((r, i) => {
      const rs = results[i] || {}
      return (rs.recognized || '')
    }).join('\n')

    wx.request({
      url: baseURL + '/followread/report',
      data: JSON.stringify({
        poemTitle: this.data.poemTitle,
        linesData: linesParam,
        fullText: this.data.fullText
      }),
      method: 'POST',
      header: { 'Content-Type': 'application/json', Token: wx.getStorageSync('token') },
      success: (res) => {
        if (res.data.code === 0) {
          const report = res.data.data
          this.setData({
            scoringProgress: 100, scoringHint: '评分完成！',
            totalScore: report.overallScore, report, showReport: true
          })
          const ui = getApp().globalData.userInfo || {}
          wx.request({
            url: baseURL + '/followread/saveRecord',
            method: 'POST', header: { 'Content-Type': 'application/json', Token: wx.getStorageSync('token') },
            data: JSON.stringify({
              studentaccount: ui.studentaccount || wx.getStorageSync('nickname'),
              studentname: ui.studentname || wx.getStorageSync('nickname') || ui.studentaccount || '',
              courseid: this.data.courseId, coursetitle: this.data.poemTitle,
              totalscore: report.overallScore,
              reportjson: JSON.stringify(report),
              recognizedtext: recText
            })
          })
        } else {
          this.setData({ scoring: false })
          wx.showToast({ title: '评分失败，请重试', icon: 'none' })
        }
      },
      fail: () => { this.setData({ scoring: false }); wx.showToast({ title: '网络错误', icon: 'none' }) }
    })
  },

  _simulateProgress() {
    const steps = [
      { pct: 25, hint: '正在转写录音...', delay: 1200 },
      { pct: 45, hint: '正在分析发音...', delay: 2500 },
      { pct: 60, hint: 'AI 正在评估...', delay: 4000 },
    ]
    steps.forEach(s => setTimeout(() => {
      if (this.data.scoring) this.setData({ scoringProgress: s.pct, scoringHint: s.hint })
    }, s.delay))
  },

  closeReport() {
    const courseId = this.data.courseId
    // 回到古诗详情页
    wx.redirectTo({ url: '/pages/course/detail?id=' + courseId })
  },

  _cleanup() { clearInterval(this.recordTimer); clearTimeout(this._autoPlayTimer) },

  onUnload() { this._cleanup(); this._stopAudio(); if (this.recorder) try { this.recorder.stop() } catch(e) {} }
})

function fmtTime(sec) {
  const m = Math.floor(sec / 60), s = Math.floor(sec % 60)
  return (m<10?'0'+m:m) + ':' + (s<10?'0'+s:s)
}
