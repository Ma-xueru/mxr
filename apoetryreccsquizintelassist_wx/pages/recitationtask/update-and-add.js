const { option, follow, session, info, update, add } = require("../../api/index.js")
const utils = require("../../utils/index.js")

Page({
  data: {
    id: '', tableName: '', editStatus: false,
    studentaccount: '', studentname: '', tasktitle: '', taskcontent: '',
    deadline: '请选择时间', showdeadline: false,
    completionstatus: '待完成', completionremark: '',
    recitationaudio: '', completiontime: '', kaoshichengji: '',
    teachercomment: '', teacheraccount: '', teachername: '', releasetime: '',
    studentaccountList: [], studentaccountIndex: null,
    coursetitles: '', courseids: '',
    baseURL: wx.getStorageSync('baseURL'), audioLocked: false,
    // state machine: idle | recording | preview | uploading | done
    state: 'idle', recordTime: 0, recordTimeDisplay: '00:00', recordTimer: null,
    localAudioPath: '',
    // AI评分进度
    submitting: false, aiProgress: 10, aiStepText: '正在上传...', aiScore: 0,
    isPlaying: false, playProgress: 0, currentTimeDisplay: '00:00', durationDisplay: '00:00', playTimer: null
  },

  recorder: null, audioCtx: null,

  async onLoad(options) {
    const nowTable = wx.getStorageSync("nowTable")
    const [sessRes, stuRes] = await Promise.all([session(nowTable), option('student/studentaccount')])
    const stuList = ['请选择学生账号'].concat(stuRes.data || [])

    const d = {
      tableName: nowTable,
      studentaccountList: stuList,
      teacheraccount: sessRes.data.teacheraccount || '',
      teachername: sessRes.data.teachername || '',
      releasetime: utils.getCurrentDate("yMDhms")
    }
    if (nowTable === 'student') {
      d.studentaccount = sessRes.data.studentaccount || ''
      d.studentname = sessRes.data.studentname || ''
    }
    if (options?.id) {
      d.editStatus = true; d.id = options.id
      const res = await info('recitationtask', options.id)
      const dt = res.data
      Object.assign(d, {
        studentaccount: dt.studentaccount || d.studentaccount,
        studentname: dt.studentname || d.studentname,
        tasktitle: dt.tasktitle || '',
        taskcontent: dt.taskcontent || '',
        deadline: dt.deadline || '请选择时间',
        completionstatus: dt.completionstatus || '待完成',
        completionremark: dt.completionremark || '',
        recitationaudio: dt.recitationaudio || '',
        completiontime: dt.completiontime || '',
        kaoshichengji: dt.kaoshichengji || '',
        teachercomment: dt.teachercomment || '',
        teacheraccount: dt.teacheraccount || d.teacheraccount,
        teachername: dt.teachername || d.teachername,
        coursetitles: dt.coursetitles || '',
        courseids: dt.courseids || '',
        releasetime: dt.releasetime || d.releasetime,
        audioLocked: nowTable === 'student' && !!dt.kaoshichengji,
        state: dt.recitationaudio ? 'done' : 'idle'
      })
      const idx = stuList.findIndex(item => item === dt.studentaccount)
      if (idx > -1) d.studentaccountIndex = idx
    }
    this.setData(d)
  },


  simulateProgress() {
    const steps = [
      { pct: 30, text: '正在分析音频...', delay: 800 },
      { pct: 45, text: '正在转写文字...', delay: 1600 },
      { pct: 55, text: '正在匹配古诗...', delay: 2400 },
    ]
    steps.forEach(s => setTimeout(() => {
      if (this.data.submitting) this.setData({ aiProgress: s.pct, aiStepText: s.text })
    }, s.delay))
  },
  goDetail() {
    const id = this.data.id
    wx.navigateTo({ url: '/pages/recitationtask/detail?id=' + id })
  },  onUnload() { this.cleanup() },

  cleanup() {
    clearInterval(this.data.recordTimer); clearInterval(this.data.playTimer)
    if (this.audioCtx) { try { this.audioCtx.destroy() } catch(e) {}; this.audioCtx = null }
    if (this.recorder) { try { this.recorder.stop() } catch(e) {}; this.recorder = null }
  },

  // ====== 录音 ======
  startRecord() {
    if (this.data.audioLocked) return wx.showToast({ title: '已评分不可重录', icon: 'none' })
    this.cleanup()
    const rec = wx.getRecorderManager(); this.recorder = rec
    rec.onStart(() => {
      this.setData({ state: 'recording', recordTime: 0, recordTimeDisplay: '00:00' })
      this.data.recordTimer = setInterval(() => {
        const t = this.data.recordTime + 1
        this.setData({ recordTime: t, recordTimeDisplay: fmtTime(t) })
      }, 1000)
    })
    rec.onStop((res) => {
      clearInterval(this.data.recordTimer)
      if (res.tempFilePath) {
        this.setData({ localAudioPath: res.tempFilePath, state: 'preview' })
        this.upload(res.tempFilePath)
      } else {
        this.setData({ state: 'idle' })
      }
    })
    rec.onError(() => { clearInterval(this.data.recordTimer); this.setData({ state: 'idle' }) })
    rec.start({ duration: 180000, sampleRate: 16000, numberOfChannels: 1, encodeBitRate: 48000, format: 'aac' })
  },

  stopRecord() { if (this.recorder) { try { this.recorder.stop() } catch(e) {} } },

  upload(filePath) {
    this.setData({ state: 'uploading' })
    const prefix = this.data.studentname || this.data.studentaccount || 'unknown'
    wx.uploadFile({
      url: `${this.data.baseURL}/file/upload`,
      filePath, name: 'file',
      formData: { prefix },
      header: { Token: wx.getStorageSync('token') },
      success: (res) => {
        try {
          const r = JSON.parse(res.data || '{}')
          if (r.code === 0 || r.code === 200) {
            this.setData({ recitationaudio: r.file || r.data?.filePath || r.data, state: 'done' })
            return
          }
        } catch(e) {}
        this.setData({ state: 'preview' })
      },
      fail: () => { this.setData({ state: 'preview' }) }
    })
  },

  // ====== 播放 ======
  togglePlay() { this.data.isPlaying ? this.pauseAudio() : this.playAudio() },

  playAudio() {
    const src = this.data.localAudioPath || (this.data.recitationaudio ? `${this.data.baseURL}/${this.data.recitationaudio}` : '')
    if (!src) return
    this.stopAudio()
    const ctx = wx.createInnerAudioContext(); this.audioCtx = ctx
    ctx.src = src; ctx.autoplay = true
    ctx.onPlay(() => {
      this.setData({ isPlaying: true })
      this.data.playTimer = setInterval(() => {
        const cur = ctx.currentTime || 0, dur = ctx.duration || 0.01
        this.setData({
          playProgress: Math.min(100, (cur / dur) * 100),
          currentTimeDisplay: fmtTime(cur),
          durationDisplay: fmtTime(dur)
        })
      }, 200)
    })
    ctx.onEnded(() => { this.stopAudio(); this.setData({ playProgress: 0, currentTimeDisplay: '00:00' }) })
    ctx.onStop(() => { this.stopAudio() })
    ctx.onError(() => { this.stopAudio() })
  },

  pauseAudio() { if (this.audioCtx) { try { this.audioCtx.pause() } catch(e) {} } clearInterval(this.data.playTimer); this.setData({ isPlaying: false }) },

  stopAudio() { clearInterval(this.data.playTimer); this.setData({ isPlaying: false }); if (this.audioCtx) { try { this.audioCtx.destroy() } catch(e) {}; this.audioCtx = null } },

  // ====== 表单 ======
  studentaccountChange(e) {
    const idx = e.detail.value; const acc = this.data.studentaccountList[idx]
    this.setData({ studentaccountIndex: idx, studentaccount: acc })
    follow('student/studentaccount', acc).then(r => { if (r.data?.studentname) this.setData({ studentname: r.data.studentname }) })
  },
  tasktitleInput(e) { this.setData({ tasktitle: e.detail.value }) },
  taskcontentInput(e) { this.setData({ taskcontent: e.detail.value }) },
  completionremarkInput(e) { this.setData({ completionremark: e.detail.value }) },
  completionstatusChange(e) { this.setData({ completionstatus: e.detail.value }) },
  ondeadlineTap() { this.setData({ showdeadline: true }) },
  deadlineTap(e) { this.setData({ deadline: e.detail.data }) },

  async submit() {
    if (!this.data.studentaccount) return wx.showToast({ icon: 'none', title: '学生不能为空' })
    const isTeacher = this.data.tableName === 'teacher'
    const obj = {
      studentaccount: this.data.studentaccount, studentname: this.data.studentname,
      tasktitle: this.data.tasktitle, taskcontent: this.data.taskcontent,
      deadline: this.data.deadline.includes('请选择') ? '' : this.data.deadline,
      completionstatus: this.data.completionstatus,
      completionremark: this.data.completionremark,
      recitationaudio: this.data.recitationaudio,
      completiontime: this.data.completiontime.includes('请选择') ? '' : this.data.completiontime,
      teacheraccount: this.data.teacheraccount, teachername: this.data.teachername,
      releasetime: this.data.releasetime
    }
    if (isTeacher) {
      if (!obj.tasktitle) return wx.showToast({ icon: 'none', title: '标题不能为空' })
      if (!obj.deadline) return wx.showToast({ icon: 'none', title: '截止日期不能为空' })
    } else {
      if (!obj.recitationaudio) return wx.showToast({ icon: 'none', title: '请先录音' })
      obj.completionstatus = '已完成'
      obj.completiontime = utils.getCurrentDate("yMDhms")
    }
    obj.id = this.data.id || undefined
    
    // 学生提交时显示AI评分进度
    if (!isTeacher) {
      this.setData({ submitting: true, aiProgress: 10, aiStepText: '正在上传录音...', aiScore: 0 })
      this.simulateProgress()
    }
    
    try {
      if (this.data.editStatus && obj.id) { await update('recitationtask', obj) }
      else { await add('recitationtask', obj) }
    } catch(e) {}
    
    if (isTeacher) {
      wx.showToast({ title: '发布成功', icon: 'success' })
      setTimeout(() => wx.navigateBack({ delta: 1 }), 1000)
      return
    }
    
    // 轮询获取AI评分结果
    this.setData({ aiStepText: 'AI 正在评分...', aiProgress: 60 })
    let retry = 0
    const pollScore = async () => {
      if (retry >= 15) {
        this.setData({ submitting: false })
        wx.showToast({ title: '提交成功', icon: 'success' })
        setTimeout(() => wx.navigateBack({ delta: 1 }), 1000)
        return
      }
      retry++
      try {
        const res = await info('recitationtask', obj.id)
        const data = res.data
        if (data && data.kaoshichengji && data.kaoshichengji > 0) {
          this.setData({ aiProgress: 100, aiStepText: '评分完成！', aiScore: data.kaoshichengji })
          return
        }
      } catch(e) {}
      const p = 60 + Math.min(retry * 3, 35)
      this.setData({ aiProgress: p })
      setTimeout(pollScore, 1500)
    }
    pollScore()
  }
})

function fmtTime(sec) {
  const m = Math.floor(sec / 60), s = Math.floor(sec % 60)
  return `${m<10?'0'+m:m}:${s<10?'0'+s:s}`
}
