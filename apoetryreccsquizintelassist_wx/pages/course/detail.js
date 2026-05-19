const { detail } = require("../../api/index.js")
const utils = require("../../utils/index.js")

function splitPinyin(text) {
  if (!text) return []
  return text.replace(/[，。！？；：、\n]/g, ' ').split(/\s+/).filter(Boolean)
}

Page({
  data: {
    id: '', token: '', baseURL: '',
    detailList: {},
    poem: { title: '', dynasty: '', author: '', lines: [], annotations: [], translation: '' },
    followHistory: [],
    showFollowReport: false,
    followReport: null,
    imageUrl: '', imageLoading: false, imageError: false,
    quizHistory: []
  },

  async onLoad(options) {
    var id = options ? options.id : getApp().globalData.detailId
    this.setData({
      id: id, token: wx.getStorageSync('token'),
      baseURL: wx.getStorageSync('baseURL') + '/'
    })
    await this.loadPoem()
  },

  async onShow() {},

  async loadPoem() {
    if (!this.data.token) return
    var that = this
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({
      url: baseURL + '/course/detail/' + this.data.id,
      method: 'GET', header: { Token: this.data.token },
      success: function(res) {
        if (res.data && res.data.code === 0) {
          var detail = res.data.data || {}
          that.setData({ detailList: detail })
          that.parsePoem(detail)
          that.checkAndLoadImage()
        }
      }
    })
  },

  parsePoem(detail) {
    var poem = {
      title: detail.coursetitle || '',
      dynasty: detail.dynasty || '',
      author: detail.author || '',
      lines: [],
      annotations: [],
      translation: detail.translation || ''
    }
    if (detail.content) {
      var rawLines = detail.content.split(/[\n。？?！!，,；;]/).map(function(l) { return l.trim() }).filter(function(l) { return l })
      var pinyinText = detail.contentpinyin || detail.pinyin || ''
      var allPinyins = splitPinyin(pinyinText)
      var pinyinIdx = 0
      poem.lines = rawLines.map(function(line) {
        var chars = line.replace(/[\s，。！？；：、""''《》（）\(\)\[\]【】\d]/g, '').split('')
        var pinyins = []
        for (var i = 0; i < chars.length; i++) {
          if (chars[i].match(/[一-龥]/)) {
            pinyins.push(allPinyins[pinyinIdx] || '')
            pinyinIdx++
          } else {
            pinyins.push('')
          }
        }
        return { chars: chars, pinyins: pinyins }
      })
    }
    this.setData({ poem: poem })
  },

  checkAndLoadImage() {
    var that = this
    if (!wx.cloud) { this.setData({ imageUrl: '' }); return }
    var db = wx.cloud.database()
    db.collection('poem_assets').where({ courseId: Number(this.data.id) }).limit(1).get()
      .then(function(res) {
        if (res.data && res.data.length > 0 && res.data[0].imageUrl) {
          var stored = res.data[0].imageUrl
          if (stored.indexOf('cloud://') === 0) {
            wx.cloud.getTempFileURL({ fileList: [stored] }).then(function(urlRes) {
              var tmp = (urlRes.fileList && urlRes.fileList[0] && urlRes.fileList[0].tempFileURL) || stored
              that.setData({ imageUrl: tmp, imageLoading: false })
            }).catch(function() { that.setData({ imageUrl: stored, imageLoading: false }) })
          } else if (stored.indexOf('/file/') !== -1 || stored.indexOf('http') === 0) {
            that._uploadToCloudStorage(stored.indexOf('http') === 0 ? stored : (wx.getStorageSync('baseURL') || '') + stored)
          } else {
            that.setData({ imageUrl: stored, imageLoading: false })
          }
        } else { that.generateImage() }
      }).catch(function() { that.generateImage() })
  },

  _syncImageToBackend(imagePath) {
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({ url: baseURL + '/course/update', method: 'POST',
      header: { Token: wx.getStorageSync('token'), 'Content-Type': 'application/json' },
      data: JSON.stringify({ id: Number(this.data.id), picture: imagePath }),
      success: function() {}
    })
  },

  generateImage() {
    var that = this
    var translation = this.data.detailList.translation || this.data.detailList.intro || ''
    if (!translation) { this.setData({ imageLoading: false }); return }
    this.setData({ imageLoading: true, imageError: false })
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({
      url: baseURL + '/voice/generateImage',
      method: 'GET',
      data: { poemTitle: this.data.detailList.coursetitle || '', translation: translation },
      header: { Token: wx.getStorageSync('token') },
      success: function(res) {
        if (res.data && res.data.code === 0 && res.data.data && res.data.data.imageUrl) {
          var backendUrl = baseURL + res.data.data.imageUrl
          // 下载后端图片 → 上传微信云存储 → 写云数据库
          that._uploadToCloudStorage(backendUrl)
        } else { that.setData({ imageLoading: false }) }
      },
      fail: function() { that.setData({ imageLoading: false }) }
    })
  },

  _uploadToCloudStorage(backendUrl) {
    var that = this
    var pid = Number(this.data.id)
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.downloadFile({
      url: backendUrl,
      success: function(dfRes) {
        if (dfRes.statusCode !== 200) { that.setData({ imageLoading: false }); return }
        if (!wx.cloud) { that.setData({ imageUrl: backendUrl, imageLoading: false }); return }
        wx.cloud.uploadFile({
          cloudPath: 'poem_images/' + pid + '.png',
          filePath: dfRes.tempFilePath,
          success: function(upRes) {
            var fileID = upRes.fileID
            that.setData({ imageUrl: fileID, imageLoading: false })
            // 同步到后端 MySQL（存 /file/ 路径，内网穿透也能看到）
            var imagePath = res.data.data.imageUrl  // /file/xxx.png
            wx.request({ url: baseURL + '/course/update', method: 'POST',
              header: { Token: wx.getStorageSync('token'), 'Content-Type': 'application/json' },
              data: JSON.stringify({ id: pid, picture: imagePath })
            })
            // 写云数据库
            var db = wx.cloud.database()
            db.collection('poem_assets').where({ courseId: pid }).limit(1).get()
              .then(function(qRes) {
                if (qRes.data && qRes.data.length > 0) {
                  db.collection('poem_assets').doc(qRes.data[0]._id).update({ data: { imageUrl: fileID } })
                } else {
                  db.collection('poem_assets').add({ data: { courseId: pid, imageUrl: fileID } })
                }
              }).catch(function() {})
          },
          fail: function() { that.setData({ imageUrl: backendUrl, imageLoading: false }) }
        })
      },
      fail: function() { that.setData({ imageLoading: false }) }
    })
  },
})
