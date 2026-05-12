const { detail, page } = require("../../api/index.js")
const utils = require("../../utils/index.js")

function splitPinyin(text) {
  if (!text) return []
  // split on spaces, punctuation, and non-letter chars
  const parts = text.replace(/[，。！？；：、\n]/g, ' ').split(/\s+/).filter(Boolean)
  return parts
}

Page({
  data: {
    id: '', token: '', baseURL: '',
    detailList: {},
    poem: { title: '', dynasty: '', author: '', lines: [], annotations: [], translation: '' },
    annoOpen: false, transOpen: false, introOpen: false,
    lastFollowRead: null
  },

  async onLoad(options) {
    const id = options?.id || getApp().globalData.detailId
    this.setData({
      id, token: wx.getStorageSync('token'),
      baseURL: wx.getStorageSync('baseURL') + '/'
    })
    await this.loadPoem()
  },

  async onShow() {
    this.setData({ token: wx.getStorageSync('token') })
  },

  async loadPoem() {
    if (!this.data.id) return
    const res = await detail("course", this.data.id)
    const data = res.data || {}
    this.setData({ detailList: data })

    // Parse poem content
    const content = data.content || ''
    const contentPinyin = data.contentpinyin || ''
    const lines = content.split(/[\n。，,]/).filter(l => l.trim())

    // Get author from intro: "王维所作..." -> pick out name
    let author = '', dynasty = ''
    const intro = data.intro || ''
    const authorMatch = intro.match(/([（(]([^）)]+)[）)])?\s*(\S+)所/)
    if (authorMatch) {
      const raw = authorMatch[0].replace(/所作.*/, '')
      const dm = raw.match(/[（(]([^）)]+)[）)]/)
      if (dm) dynasty = dm[1]
      const am = raw.replace(/[（(][^）)]+[）)]/, '').replace(/所作/, '').trim()
      if (am) author = am
    }

    // Split pinyin into per-line arrays
    const pinyinLines = contentPinyin ? contentPinyin.split('\n').filter(l => l.trim()) : []

    const poemLines = lines.map((line, i) => {
      const clean = line.replace(/[，。！？；：、\s]/g, '')
      const chars = clean.split('')
      let pinyins = []
      if (pinyinLines[i]) {
        pinyins = splitPinyin(pinyinLines[i])
        if (pinyins.length < chars.length) {
          // pad with empty
          while (pinyins.length < chars.length) pinyins.push('')
        }
        if (pinyins.length > chars.length) pinyins = pinyins.slice(0, chars.length)
      } else {
        pinyins = chars.map(() => '')
      }
      return { chars, pinyins }
    })

    // Parse annotations from content
    const annotations = []
    const fullText = data.intro || ''
    // Simple: use the content above the translation as annotations
    // For now extract key phrases
    if (fullText) {
      const parts = fullText.split(/注释[：:]|注解[：:]/)
      if (parts.length > 1) {
        const anotext = parts[1].split(/译文[：:]|翻译[：:]/)[0]
        anotext.split(/[；;]/).filter(s => s.trim()).forEach(s => annotations.push(s.trim()))
      }
    }

    // Parse translation
    let translation = ''
    if (fullText) {
      const tparts = fullText.split(/译文[：:]|翻译[：:]/)
      if (tparts.length > 1) translation = tparts[1].trim()
    }

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
      const baseURL = wx.getStorageSync('baseURL') || ''
      const frRes = await new Promise((resolve, reject) => {
        wx.request({
          url: baseURL + '/followread/records?courseid=' + this.data.id + '&page=1&limit=1',
          method: 'GET', header: { Token: wx.getStorageSync('token') },
          success: resolve, fail: reject
        })
      })
      const list = frRes?.data?.data?.list || []
      if (list.length > 0) {
        const r = list[0]
        let rd = null
        try { rd = JSON.parse(r.reportjson || '{}') } catch(e) {}
        this.setData({ lastFollowRead: { score: r.totalscore, report: rd, time: r.addtime } })
      }
    } catch(e) {}
  },

  toggleSection(e) {
    const key = e.currentTarget.dataset.key
    this.setData({ [key]: !this.data[key] })
  },

  startFollowRead() {
    const id = this.data.id
    if (!id) return wx.showToast({ title: '请先加载古诗', icon: 'none' })
    wx.navigateTo({ url: '/pages/followread/practice?id=' + id })
  }
})
