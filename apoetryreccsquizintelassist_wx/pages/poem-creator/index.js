var baseURL = wx.getStorageSync('baseURL') || ''
var recorder = null

Page({
  data: {
    messages: [], inputText: '', waiting: false, recording: false
  },

  onLoad() {
    this.setData({ messages: [
      { from: 'ai', text: '你好！我是AI诗词小诗人🌸\n拍一张风景照，或者输入几个关键词，我就能为你写一首诗～' }
    ]})
  },

  onInput(e) { this.setData({ inputText: e.detail.value }) },

  // ===== 文本输入 =====
  sendText() {
    var text = this.data.inputText.trim()
    if (!text || this.data.waiting) return
    this._startCompose(text, 'text')
  },

  // ===== 拍照 =====
  takePhoto() {
    var that = this
    wx.chooseMedia({ count: 1, mediaType: ['image'], sourceType: ['camera','album'],
      success(res) {
        var path = res.tempFiles[0].tempFilePath
        var msgs = that.data.messages
        msgs.push({ from: 'user', text: path, type: 'image' })
        msgs.push({ from: 'ai', text: '小诗人正在观察画面...', loading: true })
        that.setData({ messages: msgs, waiting: true })
        wx.uploadFile({
          url: baseURL + '/poem-creator/vision', filePath: path, name: 'image',
          success(uploadRes) {
            var data = JSON.parse(uploadRes.data)
            var desc = (data.data) || '美丽风景'
            that.data.messages.pop()
            that._startCompose(desc, 'text')
          },
          fail() { that._showError() }
        })
      }
    })
  },

  // ===== 语音输入 =====
  startVoice() {
    var that = this
    recorder = wx.getRecorderManager()
    recorder.onStart(() => { that.setData({ recording: true }) })
    recorder.onStop((res) => {
      that.setData({ recording: false })
      if (!res.tempFilePath) return
      that.setData({ waiting: true })
      // 先用 ASR 转文字（简化：传音频到后端做语音识别）
      wx.uploadFile({
        url: baseURL + '/voice/chat', filePath: res.tempFilePath, name: 'audio',
        success(upRes) {
          var data = JSON.parse(upRes.data)
          var recognized = (data.data && data.data.recognized) || ''
          if (recognized) that._startCompose(recognized, 'text')
          else { that.setData({ waiting: false }); wx.showToast({ title: '没听清', icon: 'none' }) }
        },
        fail() { that._showError() }
      })
    })
    recorder.onError(() => { that.setData({ recording: false }) })
    recorder.start({ duration: 15000, sampleRate: 16000, numberOfChannels: 1, encodeBitRate: 48000, format: 'aac' })
  },

  stopVoice() { if (recorder) { recorder.stop(); recorder = null } },

  // ===== 核心作诗 =====
  _startCompose(scene, type) {
    var that = this
    var msgs = this.data.messages
    if (type === 'text') msgs.push({ from: 'user', text: scene, type: 'text' })
    msgs.push({ from: 'ai', text: '小诗人正在研墨构思...', loading: true })
    this.setData({ messages: msgs, inputText: '', waiting: true })

    wx.request({
      url: baseURL + '/poem-creator/compose?scene=' + encodeURIComponent(scene),
      method: 'GET', timeout: 60000,
      success(res) {
        that.data.messages.pop()
        var poem = (res.data && res.data.data) || '作诗失败，请重试'
        that.setData({ messages: that.data.messages, waiting: false })
        // 显示诗 + 自动请求 TTS
        that._showPoem(poem)
      },
      fail() { that._showError() }
    })
  },

  _showPoem(poem) {
    var that = this
    var msgs = this.data.messages
    // 提取正文用于 TTS
    var bodyMatch = poem.match(/\[正文\]([\s\S]*?)\[/) || poem.match(/正文[：:]([\s\S]*)/)
    var body = bodyMatch ? bodyMatch[1].trim().replace(/[\[\]]/g, '') : ''
    msgs.push({ from: 'ai', text: poem, loading: false, ttsUrl: '' })
    that.setData({ messages: msgs })

    if (body) {
      wx.request({
        url: baseURL + '/poem-creator/tts?text=' + encodeURIComponent(body),
        method: 'GET', timeout: 30000,
        success(res) {
          if (res.data && res.data.data) {
            var msgs2 = that.data.messages
            msgs2[msgs2.length - 1].ttsUrl = baseURL + res.data.data
            that.setData({ messages: msgs2 })
          }
        }
      })
    }
  },

  playTTS(e) {
    var url = e.currentTarget.dataset.url
    if (!url) return
    var audio = wx.createInnerAudioContext()
    audio.src = url; audio.autoplay = true
    audio.onError(() => { wx.showToast({ title: '播放失败', icon: 'none' }) })
  },

  _showError() {
    this.data.messages.pop()
    this.data.messages.push({ from: 'ai', text: '小诗人打了个盹，请重试～', loading: false })
    this.setData({ messages: this.data.messages, waiting: false })
  }
})
