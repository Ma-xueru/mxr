var baseURL = ''

Component({
  data: {
    state: 'idle',       // idle | recording | thinking | speaking
    bubbleShow: false,
    bubbleText: '',
  },

  lifetimes: {
    attached() {
      baseURL = wx.getStorageSync('baseURL') || ''
    }
  },

  methods: {
    onTouchStart(e) {
      this.data._tx = e.touches[0].clientX
      this.data._ty = e.touches[0].clientY
      this.data._moved = false
      var s = this.data.state
      if (s === 'idle') {
        this.startRecord()
      }
    },

    onTouchEnd() {
      if (this.data._moved) return // 拖动操作，不触发录音
      var s = this.data.state
      if (s === 'recording') {
        this.stopRecord()
      } else if (s === 'speaking') {
        this.stopSpeak()
      }
    },

    onMove(e) {
      if (!e || !e.detail) return
      // 检测移动距离，超过15px算拖动
      var dx = Math.abs((e.detail.x || 0) - (this.data._tx || 0))
      var dy = Math.abs((e.detail.y || 0) - (this.data._ty || 0))
      if (dx > 15 || dy > 15) this.data._moved = true
    },

    // ===== 录音 =====
    startRecord() {
      var that = this
      var rec = wx.getRecorderManager()
      this._recorder = rec

      rec.onStart(function () {
        that.setData({ state: 'recording', bubbleShow: true, bubbleText: '你说，我在听～' })
      })

      rec.onStop(function (res) {
        if (res.tempFilePath) {
          that.setData({ state: 'thinking', bubbleText: '这个问题有意思，让我翻翻知识宝库...' })
          that._sendToAI(res.tempFilePath)
        } else {
          that.setData({ bubbleShow: false, state: 'idle' })
        }
      })

      rec.onError(function () {
        that.setData({ bubbleShow: false, state: 'idle' })
      })

      rec.start({
        duration: 15000,
        sampleRate: 16000,
        numberOfChannels: 1,
        encodeBitRate: 48000,
        format: 'aac'
      })
    },

    stopRecord() {
      if (this._recorder) {
        try { this._recorder.stop() } catch (e) { }
        this._recorder = null
      }
    },

    // ===== 发送到后端 =====
    _sendToAI(audioPath) {
      var that = this
      wx.uploadFile({
        url: baseURL + '/voice/chat',
        filePath: audioPath,
        name: 'audio',
        header: { Token: wx.getStorageSync('token') },
        success: function (res) {
          try {
            var data = JSON.parse(res.data)
            if (data.code === 0 && data.data) {
              var reply = data.data.reply || '这个问题有点难，再问我一次吧～'
              that.setData({ state: 'speaking', bubbleText: reply })
              // 播报
              if (data.data.ttsUrl) {
                that._playTTS(baseURL + data.data.ttsUrl)
              } else {
                setTimeout(function () { that.setData({ bubbleShow: false, state: 'idle' }) }, 3500)
              }
            } else {
              that.setData({ bubbleText: '哎呀没听清，再说一遍吧～', state: 'idle' })
              setTimeout(function () { that.setData({ bubbleShow: false }) }, 2000)
            }
          } catch (e) {
            that._showError()
          }
        },
        fail: function () { that._showError() }
      })
    },

    _showError() {
      this.setData({ bubbleText: '网络好像不太好呢，再试试吧～' })
      var that = this
      setTimeout(function () { that.setData({ bubbleShow: false, state: 'idle' }) }, 2000)
    },

    // ===== TTS 播报 =====
    _playTTS(url) {
      var that = this
      this._stopAudio()
      var audio = wx.createInnerAudioContext()
      this._audio = audio
      audio.src = url
      audio.playbackRate = 1.0
      audio.autoplay = true
      audio.onEnded(function () {
        that.setData({ bubbleShow: false, state: 'idle' })
      })
      audio.onError(function () {
        setTimeout(function () { that.setData({ bubbleShow: false, state: 'idle' }) }, 3000)
      })
    },

    stopSpeak() {
      this._stopAudio()
      this.setData({ bubbleShow: false, state: 'idle' })
    },

    _stopAudio() {
      if (this._audio) {
        try { this._audio.destroy() } catch (e) { }
        this._audio = null
      }
    }
  }
})
