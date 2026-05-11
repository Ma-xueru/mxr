const {
  deleteData,
  update,
  add,
  page,
  list,
  detail,
  session,
  save
} = require("../../api/index.js")
const utils = require("../../utils/index.js")
Page({
  data: {
    token: '',
    baseURL: '',
    id: getApp().globalData.detailId,
    userId: '',
    userInfo: {},
    detailList: {},
    payAuth: "",
    picture: "",
    priceVisible: false,
    goodname: "",

    thumbsupnumShow: false,
    crazilynumShow: false,
    storeupShow: false,
    predetailList: "",
    commmentList: [],

  },
  async onLoad(option) {
    let authobj = {}
    if (option?.isAuth == true) {
      wx.setStorageSync('isAuth', true)
    }
    this.setData(authobj)
    let myid = option?.id ? option.id : getApp().globalData.detailId
    this.setData({
      id: myid,
      token: wx.getStorageSync('token'),
      baseURL: wx.getStorageSync('baseURL') + '/'
    })
    this.handleUpdateData()
  },


  authTap() {
    if (!this.data.token) {
      wx.showToast({
        title: '请先登录',
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
        console
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
    const id = this.data.id

    // 页面加载时必须通过session('student')获取用户信息
    await this.getUserInfoFromSession()

    if (id) {
      const {
        data
      } = await detail("forum", id)
      this.setData({
        payAuth: utils.isAuthFront('forum', '支付')
      })

      data.thumbsupnum == null ? data.thumbsupnum = 0 : ''
      data.crazilynum == null ? data.crazilynum = 0 : ''
      data.storeupnum == null ? data.storeupnum = 0 : ''

      const predetailList = Object.assign({}, data)
      this.setData({
        predetailList
      })

      const detailList = data
      this.setData({
        detailList,
        picture: detailList.picture.split(','),
      })

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
    const commentRes = await list("discussforum", commentData)
    this.setData({
      commmentList: commentRes.data?.list
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
    wx.setStorageSync('tableName', "forum")
    wx.navigateTo({
      url: `/pages/discussforum/update-and-add`,
    })
    console.log("userInfo", this.data.userInfo);
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
      name: this.data.detailList.forumtitle,
      tablename: `forum`,
      // type 收藏是1 关注是41
      refid: this.data.id,
      userid: this.data.userId,
      type: anyType
    }
    await add("storeup", data)
  },

  // 收藏功能
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
            await this.listUpdate("islike")
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
          try {
            const id = await this.searchList("isStoreup")
            if (id && id.length > 0) {
              await deleteData('storeup', id)
              await this.listUpdate("cancelislike")
              await this.searchList("isStoreup")
              wx.showToast({
                title: '取消收藏成功',
                icon: 'success'
              })
            }
          } catch (error) {
            console.error('取消收藏失败', error)
          }
        }
      }
    })
  },
  async searchList(name) {
    // 检测userId是否为空
    if (!this.data.userId) {
      return []
    }

    const searchData = {
      page: 1,
      limit: 1,
      refid: this.data.id,
      tablename: "forum",
      userid: this.data.userId,
      // 1收藏 %2%点赞
      type: 1
    }
    if (name == "isthumbsupb") {
      searchData.type = "%2%"
      const isthumbRes = await list("storeup", searchData)
      console.log(isthumbRes)
      if (isthumbRes?.data?.list?.length > 0) {
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
      const crazilyRes = await list("storeup", searchData)
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
      const storeupRes = await list("storeup", searchData)
      if (storeupRes?.data?.list?.length > 0) {
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
          try {
            const id = await this.searchList("isthumbsupb")
            if (id && id.length > 0) {
              await deleteData('storeup', id)
              await this.listUpdate("cancelthumb")
              await this.searchList("isthumbsupb")
            }
          } catch (error) {
            console.error('取消点赞失败', error)
          }
        }
      }
    })
  },
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
          try {
            const id = await this.searchList("isCrazily")
            if (id && id.length > 0) {
              await deleteData('storeup', id)
              await this.listUpdate("cancelCrazily")
              await this.searchList("isCrazily")
            }
          } catch (error) {
            console.error('取消点踩失败', error)
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
      const resUpdate = await update('forum', predetailList)
      if (resUpdate.code == 0) {
        this.setData({
          predetailList,
          "detailList.crazilynum": predetailList.crazilynum
        })
      }
    }
  },







  onPayTap() {

    if (!this.data.token) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }

    if (!this.data.userId) {
      wx.showToast({
        title: '用户信息不完整',
        icon: 'none'
      })
      return
    }

    const baseURL = wx.getStorageSync('baseURL')
    let data = this.data.detailList
    data["picture"] = data.picture
    data['buynumber'] = 1
    wx.setStorageSync('payObject', data);
    wx.setStorageSync('paytable', 'forum');

    wx.navigateTo({
      url: "/pages/pay-confirm/pay-confirm?type=1"
    })
  },


  onSHTap() {
    this.selectComponent('#bottomFrame').showFrame();
  },
  canlreply() {
    this.selectComponent('#bottomFrame').hideFrame();
  },
  async reply() {
    const detailList = this.data.detailList
    const res = await update("forum", detailList)
    if (res.code == 0) {
      setTimeout(function () {
        wx.showToast({
          title: '回复成功',
          icon: "none"
        })
      }, 1000)

      this.handleUpdateData()
    }
    this.selectComponent('#bottomFrame').hideFrame();
  },


  async onShow() {},



  //免费试读

  // 下载
  download(e) {
    let url = e.currentTarget.dataset.url
    url = wx.getStorageSync('baseURL') + '/' + url;
    wx.downloadFile({
      url: url,
      success: (res) => {
        if (res.statusCode === 200) {
          wx.showToast({
            title: '下载成功',
            icon: "none"
          })

          const filePath = res.tempFilePath
          wx.openDocument({
            filePath: filePath,
            showMenu: true,
            success: function (res) {
              console.log('打开文档成功')
            }
          })
          console.log('点击查看文件', filePath);
        }
      }
    });
  },
  // 跨表



})