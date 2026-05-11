const { option, follow, session, info, update, add } = require("../../api/index.js")
const utils = require("../../utils/index.js")

Page({
  data: {
    editStatus: false,
    studentaccountList: [],
    studentaccountIndex: null,
    studentaccount: '',
    studentname: '',
    tasktitle: '',
    taskcontent: '',
    deadline: '请选择时间',
    showdeadline: false,
    completionstatus: '待完成',
    completionremark: '',
    recitationaudio: '',
    completiontime: '请选择时间',
    showcompletiontime: false,
    kaoshichengji: '',
    teachercomment: '',
    teacheraccount: '',
    teachername: '',
    releasetime: '',
    ro: {},
    tableName: '',
    id: '',
    baseURL: wx.getStorageSync('baseURL'),
    uploadAudioName: '',
    audioLocked: false
  },

  async onLoad(options) {
    if (options?.id) {
      this.setData({ editStatus: true, id: options.id })
    }
    const nowTable = wx.getStorageSync("nowTable")
    const sessionRes = await session(nowTable)
    const studentRes = await option('student/studentaccount')
    const studentList = ['请选择学生账号'].concat(studentRes.data || [])
    const baseData = {
      tableName: nowTable,
      studentaccountList: studentList,
      teacheraccount: sessionRes.data.teacheraccount || '',
      teachername: sessionRes.data.teachername || '',
      releasetime: utils.getCurrentDate("yMDhms")
    }
    if (nowTable === 'student') {
      baseData.studentaccount = sessionRes.data.studentaccount || ''
      baseData.studentname = sessionRes.data.studentname || ''
      baseData.ro = { studentaccount: true, studentname: true, teacheraccount: true, teachername: true, tasktitle: true, taskcontent: true, deadline: true, completionstatus: true, kaoshichengji: true, teachercomment: true }
    } else {
      baseData.ro = { teacheraccount: true, teachername: true }
    }
    this.setData(baseData)

    if (options?.id) {
      const detailRes = await info('recitationtask', options.id)
      const detailData = detailRes.data
      const idx = studentList.findIndex(item => item === detailData.studentaccount)
      this.setData({
        studentaccountIndex: idx > -1 ? idx : null,
        studentaccount: detailData.studentaccount || '',
        studentname: detailData.studentname || '',
        tasktitle: detailData.tasktitle || '',
        taskcontent: detailData.taskcontent || '',
        deadline: detailData.deadline || '请选择时间',
        completionstatus: detailData.completionstatus || '待完成',
        completionremark: detailData.completionremark || '',
        recitationaudio: detailData.recitationaudio || '',
        completiontime: detailData.completiontime || '请选择时间',
        kaoshichengji: detailData.kaoshichengji || '',
        teachercomment: detailData.teachercomment || '',
        teacheraccount: detailData.teacheraccount || this.data.teacheraccount,
        teachername: detailData.teachername || this.data.teachername,
        releasetime: detailData.releasetime || this.data.releasetime,
        uploadAudioName: detailData.recitationaudio ? detailData.recitationaudio.split('/').pop() : '',
        audioLocked: nowTable === 'student' && !!detailData.kaoshichengji
      })
    }
  },

  async studentaccountChange(e) {
    const selectedIndex = e.detail.value
    const studentaccount = this.data.studentaccountList[selectedIndex]
    this.setData({
      studentaccountIndex: selectedIndex,
      studentaccount
    })
    const res = await follow('student/studentaccount', studentaccount)
    if (res.data.studentname) {
      this.setData({ studentname: res.data.studentname })
    }
  },

  tasktitleInput(e) { this.setData({ tasktitle: e.detail.value }) },
  taskcontentInput(e) { this.setData({ taskcontent: e.detail.value }) },
  completionremarkInput(e) { this.setData({ completionremark: e.detail.value }) },
  kaoshichengjiInput(e) { this.setData({ kaoshichengji: e.detail.value }) },
  teachercommentInput(e) { this.setData({ teachercomment: e.detail.value }) },

  completionstatusChange(e) {
    this.setData({ completionstatus: e.detail.value })
  },

  ondeadlineTap() { this.setData({ showdeadline: true }) },
  deadlineTap(e) { this.setData({ deadline: e.detail.data }) },
  oncompletiontimeTap() { this.setData({ showcompletiontime: true }) },
  completiontimeTap(e) { this.setData({ completiontime: e.detail.data }) },
  uploadAudio() {
    if (this.data.audioLocked) {
      wx.showToast({ title: '该任务已评分，不能重新上传音频', icon: 'none' })
      return
    }
    wx.chooseMessageFile({
      count: 1,
      type: 'file',
      extension: ['mp3', 'wav', 'm4a', 'aac'],
      success: (res) => {
        const file = res.tempFiles[0]
        wx.showLoading({ title: '上传中...' })
        wx.uploadFile({
          url: `${this.data.baseURL}/file/upload`,
          filePath: file.path,
          name: 'file',
          header: {
            Token: wx.getStorageSync('token')
          },
          success: (uploadRes) => {
            const result = JSON.parse(uploadRes.data || '{}')
            if (result.code === 0 || result.code === 200) {
              this.setData({
                recitationaudio: result.file || result.data?.filePath || result.data,
                uploadAudioName: file.name || (result.file || result.data?.filePath || '').split('/').pop()
              })
              wx.showToast({ title: '音频上传成功', icon: 'none' })
            } else {
              wx.showToast({ title: result.msg || '上传失败', icon: 'none' })
            }
          },
          fail: () => {
            wx.showToast({ title: '上传失败', icon: 'none' })
          },
          complete: () => {
            wx.hideLoading()
          }
        })
      }
    })
  },

  async submit() {
    if (!this.data.studentaccount || this.data.studentaccount === '请选择学生账号') return wx.showToast({ icon: 'none', title: '学生账号不能为空' })
    if (!this.data.studentname) return wx.showToast({ icon: 'none', title: '学生姓名不能为空' })

    const obj = {
      studentaccount: this.data.studentaccount,
      studentname: this.data.studentname,
      tasktitle: this.data.tasktitle,
      taskcontent: this.data.taskcontent,
      deadline: this.data.deadline.includes('请选择') ? '' : this.data.deadline,
      completionstatus: this.data.completionstatus,
      completionremark: this.data.completionremark,
      recitationaudio: this.data.recitationaudio,
      completiontime: this.data.completiontime.includes('请选择') ? '' : this.data.completiontime,
      kaoshichengji: this.data.kaoshichengji,
      teachercomment: this.data.teachercomment,
      teacheraccount: this.data.teacheraccount,
      teachername: this.data.teachername,
      releasetime: this.data.releasetime
    }

    if (this.data.tableName === 'teacher') {
      if (!obj.tasktitle) return wx.showToast({ icon: 'none', title: '任务标题不能为空' })
      if (!obj.taskcontent) return wx.showToast({ icon: 'none', title: '任务要求不能为空' })
      if (!obj.deadline) return wx.showToast({ icon: 'none', title: '截止日期不能为空' })
    } else {
      if (this.data.audioLocked) return wx.showToast({ icon: 'none', title: '该任务已评分，不能重新提交音频' })
      obj.completionstatus = '已完成'
      obj.completiontime = utils.getCurrentDate("yMDhms")
      if (!obj.completionremark) return wx.showToast({ icon: 'none', title: '完成说明不能为空' })
      if (!obj.recitationaudio) return wx.showToast({ icon: 'none', title: '请先上传背诵音频' })
    }

    if (this.data.editStatus) {
      obj.id = this.data.id || getApp().globalData.detailId
      await update('recitationtask', obj)
    } else {
      await add('recitationtask', obj)
    }

    wx.showToast({ title: '提交成功', icon: 'none' })
    wx.navigateBack({ delta: 1 })
  }
})
