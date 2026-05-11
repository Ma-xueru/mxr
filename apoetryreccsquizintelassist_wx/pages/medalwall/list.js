const {
  session,
  list
} = require("../../api/index.js")

Page({
  data: {
    userInfo: {},
    topThree: [],
    chartList: [],
    myRank: 0,
    myMedalCount: 0,
    className: '',
    totalStudents: 0,
    baseURL: wx.getStorageSync('baseURL') + '/',
    loading: false
  },

  onLoad() {
    this.loadData()
  },

  onShow() {
    this.loadData()
  },

  onPullDownRefresh() {
    this.loadData().finally(() => wx.stopPullDownRefresh())
  },

  async loadData() {
    if (this.data.loading) return
    this.setData({ loading: true })
    try {
      const sessionRes = await session('student')
      const userInfo = sessionRes.data || {}
      if (!userInfo.id) {
        wx.showToast({
          title: '请先登录学生账号',
          icon: 'none'
        })
        this.setData({ loading: false })
        return
      }
      if (!userInfo.classname) {
        this.setData({
          userInfo,
          topThree: [],
          chartList: [],
          myRank: 0,
          myMedalCount: Number(userInfo.medalcount || 0),
          className: '',
          totalStudents: 0,
          loading: false
        })
        return
      }

      const classmatesRes = await list('student', {
        page: 1,
        limit: 1000,
        classname: userInfo.classname
      })
      const students = (classmatesRes.data.list || [])
        .map(item => ({
          ...item,
          avatarUrl: item.avatar ? wx.getStorageSync('baseURL') + '/' + String(item.avatar).replace('upload', 'file') : '/static/login.png',
          medalcount: Number(item.medalcount || 0)
        }))
        .sort((a, b) => {
          const medalDiff = Number(b.medalcount || 0) - Number(a.medalcount || 0)
          if (medalDiff !== 0) return medalDiff
          return String(a.studentname || '').localeCompare(String(b.studentname || ''), 'zh-Hans-CN')
        })

      const myIndex = students.findIndex(item => item.studentaccount === userInfo.studentaccount)
      const currentStudent = myIndex > -1 ? students[myIndex] : userInfo
      const maxMedal = Math.max(1, ...students.map(item => Number(item.medalcount || 0)))
      const chartList = students.slice(0, 3).map((item, index) => ({
        ...item,
        rank: index + 1,
        barWidth: Math.max(8, Math.round((Number(item.medalcount || 0) / maxMedal) * 100)),
        isMe: item.studentaccount === userInfo.studentaccount
      }))

      this.setData({
        userInfo,
        topThree: students.slice(0, 3),
        chartList,
        myRank: myIndex > -1 ? myIndex + 1 : 0,
        myMedalCount: Number(currentStudent.medalcount || 0),
        className: userInfo.classname,
        totalStudents: students.length,
        loading: false
      })
    } catch (error) {
      this.setData({ loading: false })
      wx.showToast({
        title: '勋章墙加载失败',
        icon: 'none'
      })
    }
  }
})
