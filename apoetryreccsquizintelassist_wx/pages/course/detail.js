const {
  deleteData,
  update,
  add,
  page,
  list,
  detail,
  save,
  session,
  exampaperlist,
  deleteRecords
} = require("../../api/index.js")
const utils = require("../../utils/index.js")
Page({
  data: {
    token: '',
    baseURL: '',
    id: "",
    userId: '',
    userInfo: {},
    detailList: {},
    payAuth: "",
    picture: "",
    priceVisible: false,
    goodname: "",
    storeupShow: false,
    thumbsupnumShow: false,
    crazilynumShow: false,
    predetailList: "",
    commmentList: [],
    commentContent: "",
    tableName: "course",
    relatedPracticeList: []
  },
  /**
   * 生命周期函数--监听页面加载
   */
  async onLoad(options) {
    let authobj = {}
    if (options?.isAuth == true) {
      wx.setStorageSync('isAuth', true)
    }
    this.setData(authobj)
    let myid = options?.id ? options.id : getApp().globalData.detailId
    this.setData({
      id: myid,
      tableName: options.tableName || "course",
      token: wx.getStorageSync('token'),
      baseURL: wx.getStorageSync('baseURL') + '/'
    })
    this.handleUpdateData()
  },
  authTap() {
    if (!this.data.token) {
      wx.showToast({
        title: '请先登陆',
        icon: 'none'
      })
      return
    }
  },
  async getUserInfoFromSession() {
    try {
      const sessionRes = await session('student')
      if (sessionRes.code == 0 && sessionRes.data) {
        const studentInfo = sessionRes.data
        this.setData({
          userId: studentInfo.id,
          userInfo: studentInfo
        })
        return studentInfo.id
      } else {
        wx.showToast({
          title: '获取用户信息失败',
          icon: 'none'
        })
        return ''
      }
    } catch (error) {
      console.log('获取session信息失败', error)
      wx.showToast({
        title: '获取用户信息失败',
        icon: 'none'
      })
      return ''
    }
  },
  async handleUpdateData() {
    // 更新当前页面的数据
    var that = this
    const id = this.data.id
    
    // 页面加载时必须通过session('student')获取用户信息
    await this.getUserInfoFromSession()
    
    if (id) {
      const {
        data
      } = await detail("course", id)
      this.setData({
        payAuth: utils.isAuthFront('course', '支付')
      })

      data.thumbsupnum == null ? data.thumbsupnum = 0 : ''
      data.crazilynum == null ? data.crazilynum = 0 : ''

      const predetailList = Object.assign({}, data)
      this.setData({
        predetailList
      })

      const detailList = data
      this.setData({
        detailList,
        picture: detailList.picture ? detailList.picture.split(',') : [],
      })
      await this.loadRelatedPractices(detailList)

      if (!this.data.token) {
        return
      }

      // 有token的情况下，确保userId存在才进行后续操作
      if (this.data.userId) {
        await this.searchList("isthumbsupb")
        await this.searchList("isCrazily")
        await this.searchList("isStoreup")
      } else {
        wx.showToast({
          title: '用户信息不完整',
          icon: 'none'
        })
      }
    }
    const commentData = {
      page: 1,
      limit: 10,
      refid: this.data.id
    }
    const commentRes = await list("discusscourse", commentData)
    this.setData({
      commmentList: commentRes.data?.list
    })
  },

  normalizeText(value) {
    return String(value || '').replace(/\s+/g, '').toLowerCase()
  },

  buildPracticeKeywords(detailList) {
    const title = detailList.coursetitle || ''
    const type = detailList.coursetype || ''
    const grade = detailList.grade || ''
    const titleKeywords = String(title).split(/[《》、，,\s]+/).filter(item => item && item.length >= 2)
    return Array.from(new Set([title, type, grade, ...titleKeywords].filter(Boolean)))
  },

  calculatePracticeScore(item, keywords, detailList) {
    const source = this.normalizeText(item.name)
    const title = this.normalizeText(detailList.coursetitle)
    const courseType = this.normalizeText(detailList.coursetype)
    let score = 0

    if (title && source.includes(title)) {
      score += 10
    }
    if (courseType && source.includes(courseType)) {
      score += 4
    }

    keywords.forEach(keyword => {
      const normalizedKeyword = this.normalizeText(keyword)
      if (normalizedKeyword && source.includes(normalizedKeyword)) {
        score += normalizedKeyword.length >= 4 ? 3 : 1
      }
    })

    return score
  },

  async loadRelatedPractices(detailList) {
    try {
      const res = await exampaperlist("exampaper", {
        order: 'desc',
        page: 1,
        limit: 100,
        status: 1
      })
      const practiceList = res?.data?.list || []
      const keywords = this.buildPracticeKeywords(detailList)
      const matchedList = practiceList
        .map(item => ({
          ...item,
          matchScore: this.calculatePracticeScore(item, keywords, detailList)
        }))
        .sort((a, b) => {
          if (b.matchScore !== a.matchScore) {
            return b.matchScore - a.matchScore
          }
          return Number(b.id || 0) - Number(a.id || 0)
        })

      const prioritizedList = matchedList.filter(item => item.matchScore > 0)
      this.setData({
        relatedPracticeList: (prioritizedList.length ? prioritizedList : matchedList).slice(0, 3)
      })
    } catch (error) {
      console.log('获取相关练习失败', error)
      this.setData({
        relatedPracticeList: []
      })
    }
  },

  async startPractice(e) {
    const id = e.currentTarget.dataset.id
    if (!id) {
      return
    }
    if (!this.data.userId) {
      await this.getUserInfoFromSession()
    }
    if (!this.data.userId) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }
    await deleteRecords(this.data.userId, id)
    wx.navigateTo({
      url: `/pages/exampaper/exam?id=${id}`,
    })
  },

  onUnload: function () {
    getApp().globalData.detailList = {}
    console.log('页面被卸载，执行销毁操作');
  },
  async addCommentap() {
    if (!this.data.token) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }
    
    // 确保用户信息存在
    if (!this.data.userId) {
      await this.getUserInfoFromSession()
      if (!this.data.userId) {
        wx.showToast({
          title: '用户信息不完整',
          icon: 'none'
        })
        return
      }
    }

    getApp().globalData.detailId = this.data.detailList.id
    wx.setStorageSync('tableName', "course")
    wx.navigateTo({
      url: `/pages/discusscourse/update-and-add`,
    })
  },
  async listAdd(anyType) {
    // 检测userId是否为空
    if (!this.data.userId) {
      wx.showToast({
        title: '用户信息不完整',
        icon: 'none'
      })
      throw new Error('用户信息不完整')
    }
    
    const data = {
      picture: this.data.detailList.picture,
      name: this.data.detailList.coursetitle,
      tablename: `course`,
      // type 收藏是1 关注是41
      refid: this.data.id,
      userid: this.data.userId,
      type: anyType
    }
    await add("storeup", data)
  },
  async searchList(name) {
    const searchData = {
      page: 1,
      limit: 1,
      refid: this.data.id,
      tablename: "course",
      userid: this.data.userId,
      // 1收藏 %2%点赞
      type: 1
    }
    if (name == "isthumbsupb") {
      searchData.type = "%2%"
      const isthumbRes = this.data.userId ? await list("storeup", searchData) : ''
      if (this.data.userId && isthumbRes?.data?.list?.length > 0) {
        this.setData({
          // 点赞
          thumbsupnumShow: true,
          crazilynumShow: false,
        })
        const id = [isthumbRes.data.list[0].id]
        return id
      } else {
        this.setData({
          thumbsupnumShow: false
        })
      }
    }
    if (name == "isCrazily") {
      searchData.type = "22"
      const crazilyRes = this.data.userId ? await list("storeup", searchData) : ''
      if (crazilyRes?.data?.list?.length > 0) {
        // 踩
        this.setData({
          thumbsupnumShow: false,
          crazilynumShow: true
        })
        const id = [crazilyRes.data?.list[0].id]
        return id
      } else {
        this.setData({
          crazilynumShow: false,
        })
      }
    }
    if (name == "isStoreup") {
      searchData.type = "1"
      const storeupRes = this.data.userId ? await list("storeup", searchData) : ''
      if (storeupRes?.data?.list?.length > 0) {
        // 已收藏
        this.setData({
          storeupShow: true
        })
        const id = [storeupRes.data?.list[0].id]
        return id
      } else {
        this.setData({
          storeupShow: false
        })
      }
    }
  },

  // 收藏
  async storeupTap() {
    if (!this.data.token) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }
    
    // 确保用户信息存在
    if (!this.data.userId) {
      await this.getUserInfoFromSession()
      if (!this.data.userId) {
        wx.showToast({
          title: '用户信息不完整',
          icon: 'none'
        })
        return
      }
    }
    
    wx.showModal({
      title: '提示',
      content: '是否收藏',
      complete: async (res) => {
        if (res.confirm) {
          try {
            await this.listAdd("1")
            await this.searchList("isStoreup")
            wx.showToast({
              title: '收藏成功',
              icon: 'success'
            })
          } catch (error) {
            console.error('收藏失败', error)
          }
        }
      }
    })
  },

  // 取消收藏
  async cancelStoreupTap() {
    if (!this.data.token) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }
    
    // 确保用户信息存在
    if (!this.data.userId) {
      await this.getUserInfoFromSession()
      if (!this.data.userId) {
        wx.showToast({
          title: '用户信息不完整',
          icon: 'none'
        })
        return
      }
    }
    
    wx.showModal({
      title: '提示',
      content: '是否取消收藏',
      complete: async (res) => {
        if (res.confirm) {
          const id = await this.searchList("isStoreup")
          if (id && id.length > 0) {
            await deleteData("storeup", id)
          }
          await this.searchList("isStoreup")
          wx.showToast({
            title: '取消收藏成功',
            icon: 'success'
          })
        }
      }
    })
  },

  // 点赞
  async thumbsupnumTap() {
    if (!this.data.token) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }
    
    // 确保用户信息存在
    if (!this.data.userId) {
      await this.getUserInfoFromSession()
      if (!this.data.userId) {
        wx.showToast({
          title: '用户信息不完整',
          icon: 'none'
        })
        return
      }
    }
    
    wx.showModal({
      title: '提示',
      content: '是否点赞',
      complete: async (res) => {
        if (res.confirm) {
          try {
            await this.listAdd("21")
            await this.listUpdate("thumbsupnum")
            await this.searchList("isthumbsupb")
          } catch (error) {
            console.error('点赞失败', error)
          }
        }
      }
    })
  },
  // 取消点赞
  async canceThumbsupnumTap() {
    if (!this.data.token) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }
    
    // 确保用户信息存在
    if (!this.data.userId) {
      await this.getUserInfoFromSession()
      if (!this.data.userId) {
        wx.showToast({
          title: '用户信息不完整',
          icon: 'none'
        })
        return
      }
    }
    
    wx.showModal({
      title: '提示',
      content: '是否取消点赞',
      complete: async (res) => {
        if (res.confirm) {
          const id = await this.searchList("isthumbsupb")
          // 取消点赞
          if (id && id.length > 0) {
            await deleteData('storeup', id)
            await this.listUpdate("cancelthumb")
            await this.searchList("isthumbsupb")
          }
        }
      }
    })
  },
  // 点踩
  async crazilynumTap() {
    if (!this.data.token) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }
    
    // 确保用户信息存在
    if (!this.data.userId) {
      await this.getUserInfoFromSession()
      if (!this.data.userId) {
        wx.showToast({
          title: '用户信息不完整',
          icon: 'none'
        })
        return
      }
    }
    
    wx.showModal({
      title: '提示',
      content: '是否踩点',
      complete: async (res) => {
        if (res.confirm) {
          try {
            await this.listAdd("22")
            await this.listUpdate("crazilynum")
            await this.searchList("isCrazily")
          } catch (error) {
            console.error('点踩失败', error)
          }
        }
      }
    })
  },
  // 取消点踩
  async cancelCrazilynumTap() {
    if (!this.data.token) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }
    
    // 确保用户信息存在
    if (!this.data.userId) {
      await this.getUserInfoFromSession()
      if (!this.data.userId) {
        wx.showToast({
          title: '用户信息不完整',
          icon: 'none'
        })
        return
      }
    }
    
    wx.showModal({
      title: '提示',
      content: '是否取消点踩',
      complete: async (res) => {
        if (res.confirm) {
          const id = await this.searchList("isCrazily")
          if (id && id.length > 0) {
            await deleteData('storeup', id)
            await this.listUpdate("cancelCrazily")
            await this.searchList("isCrazily")
          }
        }
      }
    })
  },
  async listUpdate(name) {
    const predetailList = this.data.predetailList
    const detailList = this.data.detailList
    if (predetailList && detailList) {
      if (name == "thumbsupnum") {
        // 点赞
        predetailList.thumbsupnum = (predetailList.thumbsupnum || 0) + 1
        detailList.thumbsupnum = (detailList.thumbsupnum || 0) + 1
      }
      if (name == "cancelthumb") {
        // 取消点赞
        predetailList.thumbsupnum = Math.max(0, (predetailList.thumbsupnum || 0) - 1)
        detailList.thumbsupnum = Math.max(0, (detailList.thumbsupnum || 0) - 1)
      }
      if (name == "crazilynum") {
        predetailList.crazilynum = (predetailList.crazilynum || 0) + 1
        detailList.crazilynum = (detailList.crazilynum || 0) + 1
      }
      if (name == "cancelCrazily") {
        predetailList.crazilynum = Math.max(0, (predetailList.crazilynum || 0) - 1)
        detailList.crazilynum = Math.max(0, (detailList.crazilynum || 0) - 1)
      }
      if (name == 'cancelislike') {
        predetailList.storeupnum = Math.max(0, (predetailList.storeupnum || 0) - 1)
        detailList.storeupnum = Math.max(0, (detailList.storeupnum || 0) - 1)
      }
      if (name == "islike") {
        predetailList.storeupnum = (predetailList.storeupnum || 0) + 1
        detailList.storeupnum = (detailList.storeupnum || 0) + 1
      }
      this.setData({
        detailList
      })
      const resUpdate = await update('course', predetailList)
      if (resUpdate.code == 0) {
        this.setData({
          predetailList,
          "detailList.crazilynum": predetailList.crazilynum
        })
      }
    }
  },
  // 收藏
  async storeUp() {
    if (!this.data.token) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }
    
    // 确保用户信息存在
    if (!this.data.userId) {
      await this.getUserInfoFromSession()
      if (!this.data.userId) {
        wx.showToast({
          title: '用户信息不完整',
          icon: 'none'
        })
        return
      }
    }
    
    try {
      await this.listAdd("1")
      await this.listUpdate("islike")
      wx.showToast({
        title: '收藏成功'
      })
    } catch (error) {
      console.error('收藏失败', error)
    }
  },
  // 评论输入
  commentInput(e) {
    this.setData({
      commentContent: e.detail.value
    })
  },
  // 提交评论
  async submitComment() {
    if (!this.data.token) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }
    
    // 确保用户信息存在
    if (!this.data.userId) {
      await this.getUserInfoFromSession()
      if (!this.data.userId) {
        wx.showToast({
          title: '用户信息不完整',
          icon: 'none'
        })
        return
      }
    }
    
    if (!this.data.commentContent.trim()) {
      wx.showToast({
        title: '评论内容不能为空',
        icon: 'none'
      })
      return
    }
    const data = {
      refid: this.data.id,
      content: this.data.commentContent,
      userid: getApp().globalData.userInfo?.id,
      nickname: wx.getStorageSync('nickname')
    }
    const res = await add("discusscourse", data)
    if (res.code == 0) {
      wx.showToast({
        title: '评论成功'
      })
      this.setData({
        commentContent: ''
      })
      this.handleUpdateData()
    }
  },
  // 查看评论列表
  goCommentList() {
    wx.navigateTo({
      url: `/pages/discusscourse/list?refid=${this.data.id}`,
    })
  },
  // 编辑
  edit() {
    if (!this.data.token) {
      wx.showToast({
        title: '请先登陆',
        icon: 'none'
      })
      return
    }
    getApp().globalData.detailId = this.data.id
    wx.navigateTo({
      url: `/pages/course/update-and-add?id=${this.data.id}`,
    })
  },
  // 分享
  onShareAppMessage() {
    return {
      title: this.data.detailList.coursetitle || '古诗词学习详情',
      path: `/pages/course/detail?id=${this.data.id}&tableName=course`
    }
  },
  // 生命周期函数--监听页面显示
  async onShow() {
    this.setData({
      token: wx.getStorageSync('token')
    })
    if (this.data.id) {
      this.handleUpdateData()
    }
  }
})
