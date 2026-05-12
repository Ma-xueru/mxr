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
    lastFollowRead: null
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
    try {
      var baseURL = wx.getStorageSync('baseURL') || ''
      var that = this
      wx.request({
        url: baseURL + '/followread/records?courseid=' + this.data.id + '&page=1&limit=1',
        method: 'GET', header: { Token: wx.getStorageSync('token') },
        success: function(res) {
          var list = (res.data && res.data.data && res.data.data.list) || []
          if (list.length > 0) {
            var r = list[0]
            var rd = null
            try { rd = JSON.parse(r.reportjson || '{}') } catch(e) {}
            that.setData({ lastFollowRead: { score: r.totalscore, report: rd, time: r.addtime } })
          }
        }
      })
    } catch(e) {}
  },

  startFollowRead() {
    var id = this.data.id
    if (!id) return wx.showToast({ title: '请先加载古诗', icon: 'none' })
    wx.navigateTo({ url: '/pages/followread/practice?id=' + id })
  }
})
