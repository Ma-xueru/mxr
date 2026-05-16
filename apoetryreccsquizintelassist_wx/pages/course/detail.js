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

  async onShow() {
    this.setData({ token: wx.getStorageSync('token') })
    if (this.data.id) { this.loadFollowRead(); this.loadQuizHistory() }
  },

  async loadPoem() {
    if (!this.data.id) return
    var res = await detail("course", this.data.id)
    var data = res.data || {}
    this.setData({ detailList: data })

    var content = data.content || ''
    var contentPinyin = data.contentpinyin || ''
    var author = data.authorName || data.author_name || ''
    var dynasty = data.grade || ''

    // Split pinyin by verse lines (||| separator)
    var pinyinVerseLines = contentPinyin ? contentPinyin.split('|||').filter(function(l) { return l.trim() }) : []

    // Split content by newline to get verse lines
    var verseLines = content.split(/\n/).filter(function(l) { return l.trim() })
    var poemLines = []

    // For each verse line, split by punctuation for display
    verseLines.forEach(function(verseLine, vi) {
      var subLines = verseLine.split(/[，。，。！？；：、]/).filter(function(s) { return s.trim() })
      var versePinyins = pinyinVerseLines[vi] ? splitPinyin(pinyinVerseLines[vi]) : []
      var pinyinIdx = 0

      subLines.forEach(function(sub) {
        var clean = sub.replace(/[，。！？；：、\s]/g, '')
        var chars = clean.split('')
        var subPinyins = versePinyins.slice(pinyinIdx, pinyinIdx + chars.length)
        while (subPinyins.length < chars.length) subPinyins.push('')
        pinyinIdx += chars.length
        poemLines.push({ chars: chars, pinyins: subPinyins })
      })
    })

    // Parse annotations and translation
    var annText = data.annotations || ''
    var annotations = annText.split(/[；;\n]/).filter(function(s) { return s.trim() })
    var translation = data.translation || ''

    this.setData({
      poem: {
        title: data.coursetitle || '',
        dynasty: dynasty || data.grade || '',
        author: author,
        lines: poemLines,
        annotations: annotations,
        translation: translation
      }
    })

    // Load last follow-read
    this.loadFollowRead()
    this.loadQuizHistory()
    // Load poem image from cloud DB
    this.checkAndLoadImage()
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
            // 云存储fileID → 临时URL显示 + 同步到后端MySQL
            wx.cloud.getTempFileURL({ fileList: [stored] }).then(function(urlRes) {
              var tmpUrl = (urlRes.fileList && urlRes.fileList[0] && urlRes.fileList[0].tempFileURL) || stored
              that.setData({ imageUrl: tmpUrl, imageLoading: false })
              // 同步 cloud:// fileID 到后端
              that._syncImageToBackend(stored)
            }).catch(function() { that.setData({ imageUrl: stored, imageLoading: false }) })
          } else if (stored.indexOf('/file/') !== -1 || stored.indexOf('http') === 0) {
            that._uploadToCloudStorage(stored)
          } else {
            that.setData({ imageUrl: stored, imageLoading: false })
          }
        } else {
          that.generateImage()
        }
      })
      .catch(function() { that.generateImage() })
  },

  // 把 cloud:// fileID 同步到后端 MySQL
  _syncImageToBackend(fileID) {
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({ url: baseURL + '/course/update', method: 'POST',
      header: { Token: wx.getStorageSync('token'), 'Content-Type': 'application/json' },
      data: JSON.stringify({ id: Number(this.data.id), picture: fileID }),
      success: function() { console.log('[封面] 已同步 cloud:// fileID') }
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
          // 下载 → 上传云存储 → 写云数据库
          that._uploadToCloudStorage(backendUrl)
        } else {
          that.setData({ imageLoading: false })
        }
      },
      fail: function() { that.setData({ imageLoading: false }) }
    })
  },

  _uploadToCloudStorage(backendUrl) {
    var that = this
    var pid = Number(this.data.id)
    // 1. 从后端下载图片到本地临时文件
    wx.downloadFile({
      url: backendUrl,
      success: function(dfRes) {
        if (dfRes.statusCode !== 200) { that.generateImage(); return }
        // 2. 上传到微信云存储
        wx.cloud.uploadFile({
          cloudPath: 'poem_images/' + pid + '.png',
          filePath: dfRes.tempFilePath,
          success: function(upRes) {
            var fileID = upRes.fileID
            that.setData({ imageUrl: fileID, imageLoading: false })
            // 3. 同步 cloud:// fileID 到后端 MySQL（后端代理转 HTTPS）
            var baseURL = wx.getStorageSync('baseURL') || ''
            wx.request({ url: baseURL + '/course/update', method: 'POST',
              header: { Token: wx.getStorageSync('token'), 'Content-Type': 'application/json' },
              data: JSON.stringify({ id: pid, picture: fileID }),
              success: function() { console.log('[封面] 已同步 course.picture='+fileID) }
            })
            // 4. 写云数据库（小程序端展示用）
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
          fail: function() { that.generateImage() }
        })
      },
      fail: function() { that.generateImage() }
    })
  },

  loadFollowRead() {
    var that = this
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({
      url: baseURL + '/followread/records?courseid=' + this.data.id + '&page=1&limit=1',
      method: 'GET', header: { Token: wx.getStorageSync('token') },
      success: function(res) {
        var list = (res.data && res.data.data && res.data.data.list) || []
        var history = []
        list.forEach(function(r) {
          var rd = null
          try { rd = JSON.parse(r.reportjson || '{}') } catch(e) {}
          var dims = (rd && rd.dimensions) ? rd.dimensions.map(function(d) {
            return { name: d.name, score: d.score }
          }) : []
          var t = r.addtime
          if (t && t.length > 10) t = t.substring(0, 16)
          history.push({ score: r.totalscore, dims: dims, time: t || '', fullReport: rd })
        })
        that.setData({ followHistory: history })
      }
    })
  },

  viewFollowReport(e) {
    var idx = e.currentTarget.dataset.index
    var item = this.data.followHistory[idx]
    if (item && item.fullReport) {
      this.setData({ followReport: item.fullReport, showFollowReport: true })
    }
  },

  closeFollowReport() {
    this.setData({ showFollowReport: false })
  },

  loadQuizHistory() {
    var that = this
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({
      url: baseURL + '/quiz/records?courseid=' + this.data.id + '&limit=1',
      method: 'GET', header: { Token: wx.getStorageSync('token') },
      success: function(res) {
        var list = (res.data && res.data.data && res.data.data.list) || []
        var history = []
        list.forEach(function(r) {
          var t = r.addtime
          if (t && t.length > 10) t = t.substring(0, 16)
          history.push({ score: r.score, correct: r.correctCount, total: r.questionsCount, duration: r.duration, time: t || '' })
        })
        that.setData({ quizHistory: history })
      }
    })
  },

  startQuiz() {
    var id = this.data.id
    var title = this.data.detailList.coursetitle || this.data.poem.title || ''
    var content = this.data.detailList.content || ''
    if (!content) { wx.showToast({ title: '缺少古诗内容', icon: 'none' }); return }
    wx.navigateTo({ url: '/pages/quiz/practice?id=' + id + '&title=' + encodeURIComponent(title) + '&content=' + encodeURIComponent(content) })
  },

  goToCenter() {
    wx.switchTab({ url: '/pages/center/center' })
  },

  startFollowRead() {
    var id = this.data.id
    if (!id) return wx.showToast({ title: '请先加载古诗', icon: 'none' })
    wx.navigateTo({ url: '/pages/followread/practice?id=' + id })
  }
})
