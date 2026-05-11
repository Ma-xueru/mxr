const {
  deleteData,
  page,
  list
} = require("../../api/index.js")
const utils = require("../../utils/index.js")
Page({
  /**
   * 页面的初始数据
   */
  data: {
    refid: "",
    questList: [],
    pageNum: 1,
    pageSize: 10,
    total: 0,
    flag: true,
    goodsListData: [],
    baseURL: wx.getStorageSync('baseURL') + "/",
    isLogin: wx.getStorageSync('isLogin')
  },

  /**
   * 生命周期函数--监听页面加载
   */
  async onLoad(options) {
    this.setData({
      refid: options.refid || ""
    })
    this.getData()
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    this.setData({
      isLogin: wx.getStorageSync('isLogin')
    })
    this.getData()
  },

  /**
   * 获取评论列表
   */
  async getData() {
    const searchForm = {}
    if (this.data.refid) {
      searchForm.refid = this.data.refid
    }
    const res = await page({
      pageNum: this.data.pageNum,
      pageSize: this.data.pageSize,
      tableName: 'discusscourse',
      searchForm: searchForm,
      order: 'desc'
    })
    this.setData({
      goodsListData: res.data,
      total: res.total
    })
  },

  /**
   * 删除评论
   */
  async deleteBtn(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '提示',
      content: '确定要删除这条评论吗？',
      success: async (res) => {
        if (res.confirm) {
          const result = await deleteData({ id, tableName: 'discusscourse' })
          if (result.code === 200) {
            wx.showToast({ title: '删除成功' })
            this.getData()
          }
        }
      }
    })
  },

  /**
   * 触底加载更多
   */
  onReachBottom() {
    if (this.data.flag && this.data.goodsListData.length < this.data.total) {
      this.setData({ flag: false })
      this.data.pageNum++
      this.loadMore()
    }
  },

  /**
   * 加载更多
   */
  async loadMore() {
    const searchForm = { refid: this.data.refid }
    const res = await page({
      pageNum: this.data.pageNum,
      pageSize: this.data.pageSize,
      tableName: 'discusscourse',
      searchForm: searchForm,
      order: 'desc'
    })
    this.setData({
      goodsListData: [...this.data.goodsListData, ...res.data],
      flag: true
    })
  },

  /**
   * 下拉刷新
   */
  onPullDownRefresh() {
    this.setData({ pageNum: 1 })
    this.getData().then(() => {
      wx.stopPullDownRefresh()
    })
  }
})