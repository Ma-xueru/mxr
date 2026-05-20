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
    poemImages: {},
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
    var nowTable = wx.getStorageSync('nowTable')
    if (nowTable !== 'student') return ''
    try {
      var res = await session('student')
      if (res.code === 0 && res.data && res.data.grade) {
        this.setData({ studentGrade: res.data.grade })
        return res.data.grade
      }
    } catch(e) {}
    return ''
  },
  buildPageParams(pageNum) {
    var params = { page: pageNum, limit: 100, order: 'desc' }
    if (this.data.studentGrade) params.grade = this.data.studentGrade
    return params
  },
  loadPoemImages(list) {
    if (!list.length) return
    var that = this
    var poemImages = Object.assign({}, this.data.poemImages)
    var needCloud = []

    // 1. 优先从后端 picture 字段直接加载（已同步的 cloud:// ID）
    list.forEach(function(item) {
      if (item.picture && !poemImages[item.id]) {
        if (item.picture.indexOf('cloud://') === 0) {
          needCloud.push({ id: item.id, fid: item.picture })
        } else if (item.picture.indexOf('http') === 0) {
          poemImages[item.id] = item.picture
        } else if (item.picture.indexOf('/') === 0) {
          poemImages[item.id] = (wx.getStorageSync('baseURL') || '') + item.picture
        }
      }
    })

    // 2. 转换后端 picture 中的 cloud:// ID
    function convertNext(idx) {
      if (idx >= needCloud.length) {
        // 3. 兜底：查云数据库 poem_assets
        if (!wx.cloud) { that.setData({ poemImages: poemImages }); return }
        var missing = list.filter(function(item) { return !poemImages[item.id] }).map(function(item) { return Number(item.id) })
        if (!missing.length) { that.setData({ poemImages: poemImages }); return }
        var db = wx.cloud.database()
        db.collection('poem_assets').where({ courseId: db.command.in(missing) }).limit(500).get()
          .then(function(res) {
            if (!res.data || !res.data.length) { that.setData({ poemImages: poemImages }); return }
            var cids = []
            res.data.forEach(function(row) {
              if (row.imageUrl && !poemImages[row.courseId]) {
                if (row.imageUrl.indexOf('cloud://') === 0) cids.push({ id: row.courseId, fid: row.imageUrl })
                else poemImages[row.courseId] = row.imageUrl
              }
            })
            if (!cids.length) { that.setData({ poemImages: poemImages }); return }
            var ci = 0
            function convCloud() {
              if (ci >= cids.length) { that.setData({ poemImages: poemImages }); return }
              var c = cids[ci]
              wx.cloud.getTempFileURL({ fileList: [c.fid] }).then(function(r) {
                poemImages[c.id] = (r.fileList && r.fileList[0] && r.fileList[0].tempFileURL) || ''
                ci++; convCloud()
              }).catch(function() { ci++; convCloud() })
            }
            convCloud()
          }).catch(function() { that.setData({ poemImages: poemImages }) })
        return
      }
      var item = needCloud[idx]
      wx.cloud.getTempFileURL({ fileList: [item.fid] }).then(function(r) {
        poemImages[item.id] = (r.fileList && r.fileList[0] && r.fileList[0].tempFileURL) || ''
        convertNext(idx + 1)
      }).catch(function() { convertNext(idx + 1) })
    }
    convertNext(0)
  },

  // 搜索处理
  async searhandler() {
    const name = this.data.name;
    const searchForm = {};
    if (name) {
      searchForm.coursetitle = name;
    }
    const res = await page('course',{
      page: 1,
      limit: 100,
      searchForm: searchForm,
      order: 'desc'
    })
    this.setData({
      goodsListData: res.data.list,
      total: res.total,
      pageNum: 1
    })
    this.loadPoemImages(res.data.list)
  },
  // 获取数据
  async getData() {
    await this.getStudentGrade()
    const res = await page('course', this.buildPageParams(this.data.pageNum))
    this.setData({
      goodsListData: res.data.list,
      total: res.total
    })
    this.loadPoemImages(res.data.list)
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
