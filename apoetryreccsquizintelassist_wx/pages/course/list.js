const {
  deleteData,
  page,
  list,
  newsData,
  option,
  session,
} = require("../../api/index.js")
const utils = require("../../utils/index.js")
Page({
  /**
   * 页面的初始数据
   */
  data: {
    questList: [],
    pageNum: 1,
    pageSize: 10,
    total: 0,
    flag: true, // 防抖开关 防止用户不停的下拉

    showToTopButton: true,
    onPageScrollTop: 0, // 存储滚动距离的变量

    goodsListData: [],
    activeIndex: 0,
    allData: [],
    deleteShow: false,
    className: "",
    name: "",
    addAuth: true,
    delAuth: "",
    editAuth: "",
    userid: "",
    studentGrade: "",
    baseURL: wx.getStorageSync('baseURL') + "/",
    isAuthStatus: false
  },

  /**
   * 生命周期函数--监听页面加载
   */
  async onLoad(options) {
    this.setData({
      isAuthStatus: wx.getStorageSync('isAuth')
    })
    if (options?.userid) {
      this.setData({
        userid: options.userid
      })
    }
    const currentPageUrl = this.getCurrentPageUrl();
    if (getApp().globalData.name != null) {
      this.setData({
        name: getApp().globalData.name
      })
      getApp().globalData.name = null
      this.searhandler()
    } else {
      this.getData()
    }
  },
  onShow() {
    if (getApp().globalData.name == null) {
      this.setData({
        name: null
      })
      this.setData({
        isAuthStatus: wx.getStorageSync('isAuth')
      })
      this.getData()
    }

    const currentPageUrl = this.getCurrentPageUrl();
    if (getApp().globalData.name) {
      this.setData({
        name: getApp().globalData.name
      })
      getApp().globalData.name = null
      this.searhandler()
    } else {
      this.getData()
    }
  },
  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onPageShow() {
    // 页面显示时执行的操作
  },
  getCurrentPageUrl: function () {
    const pages = getCurrentPages();
    const currentPage = pages[pages.length - 1];
    return currentPage.route;
  },
  // 搜索
  search() {
    getApp().globalData.name = this.data.name
    this.searhandler()
  },
  async getStudentGrade() {
    const role = wx.getStorageSync('role')
    if (role !== 'student') {
      this.setData({
        studentGrade: ""
      })
      return ""
    }
    try {
      const res = await session('student')
      if (res.code === 0 && res.data) {
        const grade = res.data.grade || ''
        this.setData({
          studentGrade: grade
        })
        return grade
      }
    } catch (error) {
      console.log('获取学生年级失败', error)
    }
    this.setData({
      studentGrade: ""
    })
    return ""
  },
  buildPageParams(pageNum) {
    const params = {
      pageNum,
      pageSize: this.data.pageSize,
      order: 'desc'
    }
    if (this.data.studentGrade) {
      params.searchForm = {
        grade: this.data.studentGrade
      }
    }
    return params
  },
  // 搜索处理
  async searhandler() {
    const name = this.data.name;
    const searchForm = {};
    if (this.data.studentGrade) {
      searchForm.grade = this.data.studentGrade;
    }
    if (name) {
      searchForm.coursetitle = name;
    }
    const res = await page('course',{
      pageNum: 1,
      pageSize: this.data.pageSize,
      searchForm: searchForm,
      order: 'desc'
    })
    this.setData({
      goodsListData: res.data.list,
      total: res.total,
      pageNum: 1
    })
  },
  // 获取数据
  async getData() {
    await this.getStudentGrade()
    const res = await page('course', this.buildPageParams(this.data.pageNum))
    this.setData({
      goodsListData: res.data.list,
      total: res.total
    })
  },
  // 详情
  detailBtn(e) {
    const item = e.currentTarget.dataset.item || {};
    const id = e.currentTarget.dataset.id || item.id;
    wx.navigateTo({
      url: `/pages/course/detail?id=${id}&tableName=course`,
    })
  },
  // 添加
  addTap() {
    wx.navigateTo({
      url: '/pages/course/update-and-add',
    })
  },
  // 修改
  editBtn(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/course/update-and-add?id=${id}`,
    })
  },
  // 删除
  deleteBtn(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确定要删除这条记录吗？',
      success: async (res) => {
        if (res.confirm) {
          const result = await deleteData({
            id,
            tableName: 'course'
          })
          if (result.code === 200) {
            wx.showToast({
              title: '删除成功'
            })
            this.getData()
          }
        }
      }
    })
  },
  // 触底加载更多
  onReachBottom() {
    if (this.data.flag && this.data.goodsListData.length < this.data.total) {
      this.setData({
        flag: false
      })
      this.data.pageNum++
      this.loadMore()
    }
  },
  // 加载更多
  async loadMore() {
    const res = await page('course', this.buildPageParams(this.data.pageNum))
    this.setData({
      goodsListData: [...this.data.goodsListData, ...res.data.list],
      flag: true
    })
  },
  // 下拉刷新
  onPullDownRefresh() {
    this.setData({
      pageNum: 1
    })
    this.getData().then(() => {
      wx.stopPullDownRefresh()
    })
  }
})
