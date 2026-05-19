const { deleteData, page, option } = require("../../api/index.js")
const utils = require("../../utils/index.js")

Page({
  data: {
    goodsListData: [],
    questList: [],
    allData: [],
    pageNum: 1,
    pageSize: 10,
    total: 0,
    flag: true,
    popopShow: false,
    studentaccountOptions: [],
    studentaccount: "",
    tasktitle: "",
    completionstatus: "",
    addAuth: "",
    delAuth: "",
    editAuth: "",
    showUploadButton: false,
    userid: "",
    isAuthStatus: false
  },

  async onLoad(options) {
    this.setData({
      isAuthStatus: wx.getStorageSync('isAuth')
    })
    if (options?.userid) {
      this.setData({ userid: options.userid })
    }
    const studentRes = await option('student/studentaccount')
    this.setData({
      studentaccountOptions: studentRes.data || []
    })
    this.getData()
  },

  onShow() {
    this.setData({
      isAuthStatus: wx.getStorageSync('isAuth')
    })
    this.getData()
  },

  studentaccountChange(e) {
    this.setData({
      studentaccount: e.currentTarget.dataset.item
    })
  },

  screenBoxShow() {
    this.setData({ popopShow: true })
  },

  screenReset() {
    this.setData({
      studentaccount: "",
      tasktitle: "",
      completionstatus: ""
    })
  },

  addTap() {
    getApp().globalData.detailId = null
    wx.navigateTo({
      url: `/pages/recitationtask/update-and-add`
    })
  },

  editBtn(e) {
    const id = e.currentTarget.dataset.id
    getApp().globalData.detailId = id
    wx.navigateTo({
      url: `/pages/recitationtask/update-and-add?id=${id}&isAuth=${this.data.isAuthStatus}`
    })
  },

  detailBtn(e) {
    const item = e.currentTarget.dataset.item
    getApp().globalData.detailId = item?.id
    getApp().globalData.detailList = item
    wx.navigateTo({
      url: `/pages/recitationtask/detail?isAuth=${this.data.isAuthStatus}`
    })
  },

  uploadAudioBtn(e) {
    const id = e.currentTarget.dataset.id
    getApp().globalData.detailId = id
    wx.navigateTo({
      url: `/pages/recitationtask/update-and-add?id=${id}&isAuth=${this.data.isAuthStatus}&mode=upload`
    })
  },

  deleteBtn(e) {
    wx.showModal({
      title: '提示',
      content: '确认删除？',
      complete: async (res) => {
        if (res.confirm) {
          const id = e.currentTarget.dataset.id
          const delRes = await deleteData("recitationtask", [id])
          if (delRes.code == 0) {
            this.getData()
          }
        }
      }
    })
  },

  async search() {
    let params = {
      page: 1,
      limit: this.data.pageSize
    }
    const nowTable = wx.getStorageSync('nowTable')
    if (this.data.isAuthStatus) {
      const account = getApp().globalData.userInfo[nowTable + 'account']
      if (account) {
        params[nowTable + 'account'] = wx.getStorageSync('nickname')
      }
    }
    if (this.data.studentaccount) params.studentaccount = this.data.studentaccount
    if (this.data.tasktitle) params.tasktitle = '%' + this.data.tasktitle + '%'
    if (this.data.completionstatus) params.completionstatus = '%' + this.data.completionstatus + '%'
    const res = await page("recitationtask", params)
    var listData2 = (res.data.list || []).filter(function(item) { return item.taskType !== 2 })
    this.setData({
      goodsListData: listData2,
      questList: listData2,
      allData: listData2,
      total: res.total || 0,
      pageNum: 2,
      flag: (res.data.list || []).length < (res.total || 0),
      popopShow: false
    })
  },

  async getData() {
    const obj = {
      page: 1,
      limit: this.data.pageSize
    }
    const nowTable = wx.getStorageSync('nowTable')
    const isAuthObj = {}
    if (this.data.isAuthStatus) {
      isAuthObj.addAuth = utils.isAuth("recitationtask", "新增")
      isAuthObj.delAuth = utils.isAuth("recitationtask", "删除")
      isAuthObj.editAuth = utils.isAuth("recitationtask", "修改")
      isAuthObj.showUploadButton = nowTable === 'student'
      const account = getApp().globalData.userInfo[nowTable + 'account']
      if (account) obj[nowTable + 'account'] = wx.getStorageSync('nickname')
    } else {
      isAuthObj.addAuth = utils.isAuthFront("recitationtask", "新增")
      isAuthObj.delAuth = utils.isAuthFront("recitationtask", "删除")
      isAuthObj.editAuth = utils.isAuthFront("recitationtask", "修改")
      isAuthObj.showUploadButton = wx.getStorageSync('nowTable') === 'student'
    }
    this.setData(isAuthObj)
    const res = await page("recitationtask", obj)
    if (res.code == 0) {
      var listData = (res.data.list || []).filter(function(item) { return item.taskType !== 2 })
      this.setData({
        goodsListData: listData,
        questList: listData,
        allData: listData,
        total: res.total,
        pageNum: 2,
        flag: listData.length < res.total
      })
    }
  },

  onReachBottom() {
    if (!this.data.flag) return
  }
})
