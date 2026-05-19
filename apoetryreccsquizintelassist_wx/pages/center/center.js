const utils = require('../../utils/index.js')
const {
  session,
  encrypt,
  update,
  groupby,
  list
} = require("../../api/index")
Page({
  /**
   * 页面的初始数据
   */
  data: {
    nickname: '',
    token: '',
    userInfo: {},
    menuOpen: [false, false, false, false, false, false, false],
    baseURL: wx.getStorageSync('baseURL') + '/',
    studyCount: 0,     // 学习次数
    correctRate: 0,    // 正确率
    wordCount: 0,      // 正确次数数
    isStudent: false,
    showAvatarPicker: false,
    selectedAvatarIndex: -1,
    avatarPool: [
      { src: 'file/儒雅小书生.png', name: '儒雅小书生', bg: 'linear-gradient(135deg,#B3E5FC,#81D4FA)' },
      { src: 'file/活泼小侠客.png', name: '活泼小侠客', bg: 'linear-gradient(135deg,#FFCDD2,#EF9A9A)' },
      { src: 'file/机灵小书童.png', name: '机灵小书童', bg: 'linear-gradient(135deg,#FFE0B2,#FFCC80)' },
      { src: 'file/优雅小才女.png', name: '优雅小才女', bg: 'linear-gradient(135deg,#F3E5F5,#CE93D8)' },
      { src: 'file/福气小福娃.png', name: '福气小福娃', bg: 'linear-gradient(135deg,#FFE0B2,#FFB74D)' },
      { src: 'file/自然小仙子.png', name: '自然小仙子', bg: 'linear-gradient(135deg,#C8E6C9,#A5D6A7)' },
      { src: 'file/抱书萌萌兔.png', name: '抱书萌萌兔', bg: 'linear-gradient(135deg,#BBDEFB,#90CAF9)' },
      { src: 'file/祥云福运龙.png', name: '祥云福运龙', bg: 'linear-gradient(135deg,#FFF9C4,#FFF176)' },
      { src: 'file/绿野小仙子.png', name: '绿野小仙子', bg: 'linear-gradient(135deg,#C8E6C9,#81C784)' }
    ],
    ownMedalCount: 0,
    classMedalCount: 0,
    weeklyHeat: [],
    trend7: [],
    bestScores: { follow: 0, quiz: 0, analogy: 0, review: 0 },
    totalDays: 0,
    weeklyStudyData: [],
    reviewRecommendations: [],
    weakPointSummary: '',
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    this.setData({
      nickname: wx.getStorageSync('nickname')
    })
    this.getData()
  },

  onShow() {
    this.setData({
      nickname: wx.getStorageSync('nickname')
    })
    this.getData()
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {},

  onAvatarTap() {
    this.setData({ showAvatarPicker: true, selectedAvatarIndex: -1 })
  },
  closeAvatarPicker() {
    this.setData({ showAvatarPicker: false })
  },
  noop() {},
  selectAvatar(e) {
    this.setData({ selectedAvatarIndex: e.currentTarget.dataset.idx })
  },
  confirmAvatar() {
    var that = this
    var idx = this.data.selectedAvatarIndex
    if (idx < 0) { wx.showToast({ title: '请先选择一个头像', icon: 'none' }); return }
    var avatar = this.data.avatarPool[idx]
    var baseURL = wx.getStorageSync('baseURL') || ''
    // 更新 student 表的 avatar 字段
    var updatedInfo = Object.assign({}, this.data.userInfo, { avatar: avatar.src })
    wx.request({
      url: baseURL + '/student/update', method: 'POST',
      header: { 'Content-Type': 'application/json', Token: wx.getStorageSync('token') },
      data: JSON.stringify(updatedInfo),
      success: function(res) {
        if (res.data && res.data.code === 0) {
          getApp().globalData.userInfo = updatedInfo
          that.setData({ userInfo: updatedInfo, showAvatarPicker: false })
          wx.showToast({ title: '头像设置成功！', icon: 'success' })
        } else {
          wx.showToast({ title: '保存失败，请重试', icon: 'none' })
        }
      },
      fail: function() { wx.showToast({ title: '网络错误', icon: 'none' }) }
    })
  },

  toDetail() {
    wx.navigateTo({
      url: '/pages/user-info/user-info',
    })
  },

  async getData() {
    this.setData({
      token: wx.getStorageSync('token'),
    })
    if (!this.data.token) {
      return
    }
    let table = wx.getStorageSync("nowTable");
    const res = await session(table)

    if (res.data && res.data.avatar) {
      res.data["avatar"] = res.data.avatar.replace('upload', 'file')
    }
    if (res.data && !res.data.avatar && res.data.zhaopian) {
      res.data["avatar"] = res.data.zhaopian.replace('upload', 'file')
    }
    this.setData({
      userInfo: res.data || {},
      isStudent: table === 'student'
    })
    getApp().globalData.userInfo = res.data || {}

    // 获取学习统计数据
    if (this.data.userInfo.id) {
      await this.getStudyStats();
      this.loadHomeStats();
    }
  },

  /**
   * 获取学习统计数据
   */
  async getStudyStats() {
    try {
      const userId = this.data.userInfo.id
      const studentaccount = this.data.userInfo.studentaccount
      const classname = this.data.userInfo.classname
      const grade = this.data.userInfo.grade
      const ownMedalCount = this.data.userInfo.medalcount || 0

      const examRes = await list('examrecord', {
        page: 1,
        limit: 100000,
        userid: userId,
      })
      const records = examRes.data.list || [];
      const recitationRes = studentaccount ? await list('recitationtask', {
        page: 1,
        limit: 1000,
        studentaccount
      }) : { data: { list: [] } }
      const recitationTasks = recitationRes.data.list || []
      const transcriptRes = studentaccount ? await list('transcript', {
        page: 1,
        limit: 1000,
        studentaccount
      }) : { data: { list: [] } }
      const transcriptList = transcriptRes.data.list || []

      const totalScore = records.reduce((sum, item) => sum + Number(item.score || 0), 0)
      const myScore = records.reduce((sum, item) => sum + Number(item.myscore || 0), 0)
      const correctRate = totalScore > 0 ? Math.round((myScore / totalScore) * 100) : 0
      const correctRecords = records.filter(record => Number(record.myscore || 0) > 0)
      const uniqueWords = new Set()
      correctRecords.forEach(record => {
        const word = record.papername || record.questionname
        if (word) uniqueWords.add(word)
      })

      const weeklyStudyData = this.buildWeeklyStudyData(records, recitationTasks, transcriptList)
      const studyCount = weeklyStudyData.reduce((sum, item) => sum + item.count, 0)
      const classMedalCount = await this.getClassMedalCount(classname)
      const reviewRecommendations = await this.buildReviewRecommendations(records, recitationTasks, grade)
      const weakPointSummary = reviewRecommendations.length
        ? `建议优先复习：${reviewRecommendations.map(item => item.title).join('、')}`
        : '本周学习比较稳定，继续保持背诵和练习节奏。'

      this.setData({
        studyCount,
        correctRate,
        wordCount: uniqueWords.size,
        ownMedalCount,
        classMedalCount,
        weeklyStudyData,
        reviewRecommendations,
        weakPointSummary
      })
    } catch (error) {
      console.error('获取学习统计数据失败:', error)
      this.setData({
        studyCount: 0,
        correctRate: 0,
        wordCount: 0,
        ownMedalCount: this.data.userInfo.medalcount || 0,
        classMedalCount: 0,
        weeklyStudyData: this.buildWeeklyStudyData([], [], []),
        reviewRecommendations: [],
        weakPointSummary: '暂时还没有足够的数据，先去完成一次背诵任务或测验吧。'
      })
    }
  },
  loadHomeStats() {
    var that = this
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({
      url: baseURL + '/followread/homeStats', method: 'GET',
      header: { Token: wx.getStorageSync('token') },
      success: function(res) {
        if (res.data && res.data.code === 0) {
          var d = res.data.data
          that.setData({
            weeklyHeat: d.weeklyHeat || [],
            trend7: d.trend7 || [],
            bestScores: d.bestScores || { follow: 0, quiz: 0, analogy: 0, review: 0 },
            totalDays: d.totalDays || 0
          })
        }
      }
    })
  },

  async getClassMedalCount(classname) {
    if (!classname) return 0
    try {
      const classmateRes = await list('student', {
        page: 1,
        limit: 1000,
        classname
      })
      const classmates = classmateRes.data.list || []
      return classmates.reduce((sum, item) => sum + Number(item.medalcount || 0), 0)
    } catch (error) {
      return 0
    }
  },
  buildWeeklyStudyData(records, recitationTasks, transcriptList) {
    const days = []
    const counter = {}
    for (let i = 6; i >= 0; i--) {
      const date = new Date()
      date.setDate(date.getDate() - i)
      const key = this.formatDateKey(date)
      days.push({
        key,
        label: `${date.getMonth() + 1}.${date.getDate()}`,
      })
      counter[key] = 0
    }

    const increaseCounter = (dateStr) => {
      if (!dateStr) return
      const key = String(dateStr).slice(0, 10)
      if (Object.prototype.hasOwnProperty.call(counter, key)) {
        counter[key] += 1
      }
    }

    records.forEach(item => increaseCounter(item.addtime))
    recitationTasks.forEach(item => increaseCounter(item.completiontime || item.addtime))
    transcriptList.forEach(item => increaseCounter(item.releasetime || item.addtime))

    const maxCount = Math.max(1, ...Object.values(counter))
    return days.map(item => ({
      label: item.label,
      count: counter[item.key],
      height: Math.max(18, Math.round((counter[item.key] / maxCount) * 120))
    }))
  },
  async buildReviewRecommendations(records, recitationTasks, grade) {
    const weakMap = {}
    records.forEach(item => {
      const total = Number(item.score || 0)
      const mine = Number(item.myscore || 0)
      if (total > mine) {
        const key = item.papername || item.questionname || '古诗词综合练习'
        weakMap[key] = (weakMap[key] || 0) + (total - mine || 1)
      }
    })
    recitationTasks
      .filter(item => item.completionstatus !== '已完成')
      .forEach(item => {
        const key = item.coursetitles || item.tasktitle || '背诵任务'
        weakMap[key] = (weakMap[key] || 0) + 3
      })

    const sortedWeak = Object.keys(weakMap)
      .map(key => ({ title: key, score: weakMap[key] }))
      .sort((a, b) => b.score - a.score)

    if (sortedWeak.length) {
      return sortedWeak.slice(0, 3).map((item, index) => ({
        title: item.title,
        reason: index === 0 ? '近期错误较多或任务未完成，建议优先复习。' : '可以作为本周重点巩固内容。'
      }))
    }

    try {
      const courseRes = await list('course', {
        page: 1,
        limit: 3,
        grade
      })
      return (courseRes.data.list || []).slice(0, 3).map(item => ({
        title: item.coursetitle,
        reason: '结合你当前年级，推荐进行系统复习。'
      }))
    } catch (error) {
      return []
    }
  },
  formatDateKey(date) {
    const year = date.getFullYear()
    const month = `${date.getMonth() + 1}`.padStart(2, '0')
    const day = `${date.getDate()}`.padStart(2, '0')
    return `${year}-${month}-${day}`
  },

  /**
   * 切换菜单展开/折叠状态
   */
  toggleMenu(e) {
    const index = Number(e.currentTarget.dataset.index);
    const menuOpen = [...this.data.menuOpen];
    if (Number.isNaN(index)) {
      return;
    }
    while (menuOpen.length <= index) {
      menuOpen.push(false);
    }
    menuOpen[index] = !menuOpen[index];
    this.setData({
      menuOpen
    });
  },
  
  /**
   * 跳转到测验记录页面
   */
  toExamRecord() {
    wx.navigateTo({
      url: '/pages/examrecord/list',
    });
  },

  /**
   * 跳转到错题本页面
   */
  toErrorBook() {
    wx.navigateTo({
      url: '/pages/examrecord/detail',
    });
  },

  /**
   * 跳转到意见反馈页面
   */
  toFeedback() {
    wx.navigateTo({
      url: '/pages/feedback/list',
    });
  },

  /**
   * 跳转到学习社区页面
   */
  toForum() {
    wx.switchTab({
      url: '/pages/forum/list',
    });
  },
  toMyforum() {
    wx.navigateTo({
      url: '/pages/forum/forum-my',
    });
  },
  toTeacher() {
    wx.switchTab({
      url: '/pages/teacher/list',
    });
  },
  toCoursereserve() {
    wx.navigateTo({
      url: '/pages/coursereserve/list',
    });
  },
  toReservecancel() {
    wx.navigateTo({
      url: '/pages/reservecancel/list',
    });
  },
  toStoreup() {
    wx.navigateTo({
      url: '/pages/storeup/list',
    })
  },
  toAichat() {
    wx.switchTab({
      url: '/pages/chat/chat',
    })
  },
  toClassPk() {
    wx.navigateTo({
      url: '/pages/classpk/list',
    })
  },
  toAutonomousHistory() {
    wx.navigateTo({
      url: '/pages/autonomous/history',
    })
  },
  toMedalWall() {
    wx.navigateTo({
      url: '/pages/medalwall/list',
    })
  },
  toRecitationTask() {
    wx.navigateTo({
      url: '/pages/recitationtask/list',
    })
  },

  toWrongBook() {
    wx.navigateTo({ url: '/pages/quiz/wrongbook' })
  },

  toReport() {
    wx.navigateTo({ url: '/pages/center/report' })
  },

  tologin() {
    wx.navigateTo({
      url: '/pages/login/login',
    })
  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {},

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {},

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {},

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {},

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {}
});
