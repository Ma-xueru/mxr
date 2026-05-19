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
    const nowTable = wx.getStorageSync('nowTable')
    if (nowTable !== 'student') {
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
    var params = {
      page: pageNum,
      limit: 100,
      order: 'desc'
    }
    if (this.data.studentGrade) {
      params.grade = this.data.studentGrade
    }
    return params
  },
  loadPoemImages(list) {
    if (!list.length) return
    var that = this
    var poemImages = Object.assign({}, this.data.poemImages)
    var cloudIds = []
    var cloudMap = {}

    // 1. 后端 picture 字段（HTTP公开可访问，内网穿透可用）
    list.forEach(function(item) {
      var pic = item.picture
      if (pic && !poemImages[item.id]) {
        if (pic.indexOf('cloud://') === 0) {
          cloudIds.push(pic)
          cloudMap[pic] = item.id
        } else if (pic.indexOf('http') === 0) {
          poemImages[item.id] = pic
        } else if (pic.indexOf('/file/') === 0) {
          poemImages[item.id] = baseURL + pic
        }
      }
    })
    // 转换 cloud:// → 临时 URL
    if (cloudIds.length > 0) {
      wx.cloud.getTempFileURL({ fileList: cloudIds }).then(function(urlRes) {
        (urlRes.fileList || []).forEach(function(f) {
          if (f.tempFileURL && cloudMap[f.fileID]) poemImages[cloudMap[f.fileID]] = f.tempFileURL
        })
        that.setData({ poemImages: poemImages })
      }).catch(function() { that.setData({ poemImages: poemImages }) })
    } else {
      that.setData({ poemImages: poemImages })
    }

    // 2. 兜底：云数据库 poem_assets
    if (!wx.cloud) return
    var remaining = list.filter(function(item) { return !poemImages[item.id] }).map(function(item) { return Number(item.id) })
    if (!remaining.length) return
    var db = wx.cloud.database()
    db.collection('poem_assets').where({ courseId: db.command.in(remaining) }).get()
      .then(function(res) {
        if (!res.data || !res.data.length) return
        var cids = []
        var cidMap = {}
        res.data.forEach(function(row) {
          var cid = row.courseId
          if (row.imageUrl && !poemImages[cid]) {
            if (row.imageUrl.indexOf('cloud://') === 0) {
              cids.push(row.imageUrl)
              cidMap[row.imageUrl] = cid
            } else {
              poemImages[cid] = row.imageUrl
            }
          }
        })
        if (cids.length > 0) {
          wx.cloud.getTempFileURL({ fileList: cids }).then(function(urlRes) {
            (urlRes.fileList || []).forEach(function(f) {
              if (f.tempFileURL && cidMap[f.fileID] !== undefined) poemImages[cidMap[f.fileID]] = f.tempFileURL
            })
            that.setData({ poemImages: poemImages })
          }).catch(function() { that.setData({ poemImages: poemImages }) })
        } else {
          that.setData({ poemImages: poemImages })
        }
      })
  },

  // 搜索处理
  async searhandler() {
    const name = this.data.name;
    const searchForm = {};
    if (name) {
      searchForm.coursetitle = name;
    }
    if (this.data.studentGrade) {
      searchForm.grade = this.data.studentGrade
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
