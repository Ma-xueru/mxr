const {
  getDetailData,
  addData,
  updateData
} = require("../../api/index.js")
const utils = require("../../utils/index.js")
Page({
  /**
   * 页面的初始数据
   */
  data: {
    id: "",
    courseno: "",
    coursetitle: "",
    coursetype: "",
    picture: "",
    intro: "",
    content: "",
    video: "",
    isEdit: false,
    baseURL: wx.getStorageSync('baseURL') + "/"
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    if (options.id) {
      this.setData({ id: options.id, isEdit: true })
      this.getDetail()
    }
  },

  /**
   * 获取详情
   */
  async getDetail() {
    const res = await getDetailData({
      id: this.data.id,
      tableName: 'course'
    })
    if (res.code === 200) {
      const data = res.data || {}
      this.setData({
        courseno: data.courseno || "",
        coursetitle: data.coursetitle || "",
        coursetype: data.coursetype || "",
        picture: data.picture || "",
        intro: data.intro || "",
        content: data.content || "",
        video: data.video || ""
      })
    }
  },

  /**
   * 表单输入
   */
  inputChange(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail
    this.setData({ [field]: value })
  },

  /**
   * 上传图片
   */
  async uploadImage() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: async (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath
        wx.showLoading({ title: '上传中...' })
        try {
          const uploadRes = await utils.uploadFile(tempFilePath, 'image')
          if (uploadRes.code === 200) {
            this.setData({ picture: uploadRes.data.filePath })
            wx.showToast({ title: '上传成功' })
          }
        } catch (error) {
          wx.showToast({ title: '上传失败', icon: 'none' })
        } finally {
          wx.hideLoading()
        }
      }
    })
  },

  /**
   * 上传视频
   */
  async uploadVideo() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['video'],
      sourceType: ['album', 'camera'],
      success: async (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath
        wx.showLoading({ title: '上传中...' })
        try {
          const uploadRes = await utils.uploadFile(tempFilePath, 'video')
          if (uploadRes.code === 200) {
            this.setData({ video: uploadRes.data.filePath })
            wx.showToast({ title: '上传成功' })
          }
        } catch (error) {
          wx.showToast({ title: '上传失败', icon: 'none' })
        } finally {
          wx.hideLoading()
        }
      }
    })
  },

  /**
   * 保存数据
   */
  async saveData() {
    // 表单验证
    if (!this.data.courseno.trim()) {
      wx.showToast({ title: '请输入古诗词学习号', icon: 'none' })
      return
    }
    if (!this.data.coursetitle.trim()) {
      wx.showToast({ title: '请输入古诗词学习标题', icon: 'none' })
      return
    }
    if (!this.data.coursetype.trim()) {
      wx.showToast({ title: '请输入古诗词学习类型', icon: 'none' })
      return
    }
    if (!this.data.intro.trim()) {
      wx.showToast({ title: '请输入古诗词学习简介', icon: 'none' })
      return
    }

    const formData = {
      courseno: this.data.courseno,
      coursetitle: this.data.coursetitle,
      coursetype: this.data.coursetype,
      picture: this.data.picture,
      intro: this.data.intro,
      content: this.data.content,
      video: this.data.video
    }

    wx.showLoading({ title: '保存中...' })
    try {
      let res
      if (this.data.isEdit) {
        res = await updateData({
          id: this.data.id,
          tableName: 'course',
          data: formData
        })
      } else {
        res = await addData({
          tableName: 'course',
          data: formData
        })
      }
      
      if (res.code === 200) {
        wx.showToast({ title: '保存成功' })
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      }
    } catch (error) {
      wx.showToast({ title: '保存失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  }
})