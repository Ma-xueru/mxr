const menuData = require('../../utils/menu.js')
const { update, session } = require('../../api/index.js')

Page({
  data: {
    tableName: '', role: '',
    studentaccount: '', studentname: '',
    genderList: "男,女".split(','), genderIndex: 0,
    telephone: '', grade: '', classname: '',
    teacheraccount: '', teachername: '', lianxidianhua: '',
    showPassword: false, oldPassword: '', newPassword: '', confirmPassword: ''
  },

  onLoad() {
    let tableName = wx.getStorageSync("nowTable")
    const name = wx.getStorageSync("role")
    let role = ''
    menuData.default.list().map(obj => { if (name == obj.roleName) role = obj.tableName })
    this.setData({ role, tableName })

    const userInfo = getApp().globalData.userInfo || {}
    if (tableName === 'student') {
      this.setData({
        studentaccount: userInfo.studentaccount || '',
        studentname: userInfo.studentname || '',
        telephone: userInfo.telephone || '',
        grade: userInfo.grade || '',
        classname: userInfo.classname || ''
      })
    } else {
      this.setData({
        teacheraccount: userInfo.teacheraccount || '',
        teachername: userInfo.teachername || '',
        lianxidianhua: userInfo.lianxidianhua || ''
      })
    }
    if (userInfo.gender) {
      this.data.genderList.map((v, i) => { if (v == userInfo.gender) this.setData({ genderIndex: i }) })
    }
  },

  showPasswordPopup() { this.setData({ showPassword: true, oldPassword: '', newPassword: '', confirmPassword: '' }) },
  hidePasswordPopup() { this.setData({ showPassword: false }) },

  async doChangePassword() {
    if (!this.data.oldPassword) { wx.showToast({ title: '请输入原密码', icon: 'none' }); return }
    if (!this.data.newPassword) { wx.showToast({ title: '请输入新密码', icon: 'none' }); return }
    if (!this.data.confirmPassword) { wx.showToast({ title: '请确认新密码', icon: 'none' }); return }

    const userInfo = getApp().globalData.userInfo || {}
    const table = this.data.role
    const passwordKey = table === 'student' ? 'studentpassword' : 'teacherpassword'
    const currentPassword = userInfo[passwordKey]
    if (this.data.oldPassword !== currentPassword) { wx.showToast({ title: '原密码不正确', icon: 'none' }); return }
    if (this.data.newPassword !== this.data.confirmPassword) { wx.showToast({ title: '两次密码不一致', icon: 'none' }); return }

    userInfo[passwordKey] = this.data.newPassword
    const res = await update(table, userInfo)
    if (res.code === 0) {
      wx.showToast({ title: '密码修改成功', icon: 'success' })
      this.setData({ showPassword: false })
      const userInfoRes = await session(table)
      if (userInfoRes.code === 0) getApp().globalData.userInfo = userInfoRes.data
    } else {
      wx.showToast({ title: res.msg || '修改失败', icon: 'none' })
    }
  },

  quitTap() {
    let saveBaseURL = wx.getStorageSync('baseURL')
    wx.clearStorageSync()
    wx.setStorageSync('baseURL', saveBaseURL)
    wx.reLaunch({ url: "/pages/login/login" })
  }
})
