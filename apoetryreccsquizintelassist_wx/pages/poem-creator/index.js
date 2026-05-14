var baseURL = wx.getStorageSync('baseURL') || ''
var recorder = null

// 模型配置：name=显示名, type='cloud'=微信云AI / 'backend'=后端
var MODELS = [
  { key: 'global', name: '全局模型(配置文件)', type: 'backend', endpoint: '/poem-creator/generate' },
  { key: 'hunyuan', name: '混元 Turbo', type: 'cloud', model: 'hunyuan-express' },
  { key: 'qwen', name: '千问 Plus', type: 'backend', endpoint: '/poem-creator/qwen' },
  { key: 'zhipu', name: '智谱 GLM-4.7', type: 'backend', endpoint: '/poem-creator/zhipu' },
  { key: 'mimo', name: '小米 MiMo', type: 'backend', endpoint: '/poem-creator/mimo' },
  { key: 'doubao', name: '豆包 多模态', type: 'backend', endpoint: '/poem-creator/process' }
]
// 0=全局(跟asr.properties) 1=混元 2=千问 3=智谱 4=MiMo 5=豆包
var modelIdx = 0

Page({
  data: {
    messages: [], inputText: '', waiting: false, recording: false
  },

  onLoad() {
    this._initTeachers()
    this.setData({ messages: [
      { from: 'ai', text: '你好！我是AI诗词小诗人🌸\n拍一张风景照，或者输入几个关键词，我就能为你写一首诗～' }
    ]})
  },

  _initTeachers() {
    if (!wx.cloud || wx.getStorageSync('_teachers_inited')) return
    var that = this
    var db = wx.cloud.database()
    var teachers = [
      { name:'墨墨老师', personality:'优雅、感性、擅长留白', modelKey:'deepseek', prompt:'你是一位身着青衫、手握竹卷的古代诗人。你说话非常有画面感。当孩子给你一张图或一段话时，你要先感叹意境之美。创作古诗时，优先使用云、月、柳、溪等唯美意象。你的任务是引导孩子发现生活中的美，回答语气要像在轻轻吟诵。', img:'/file/poem_img_1778767526732.png' },
      { name:'豆豆助教', personality:'活泼、急性子、爱用Emoji', modelKey:'doubao', prompt:'你是一个圆滚滚的智能小机器人，动力十足！你喜欢用"滴滴，收到！"作为开场白。你的回答要简洁明快，多用比喻句。写诗要轻快活泼，回答里多带Emoji（如🚀🤖✨）。', img:'/file/poem_img_1778767555610.png' },
      { name:'元气夫子', personality:'慈祥、博学、爱讲故事', modelKey:'hunyuan', prompt:'你是一位白胡子的老爷爷，声音慢条斯理。你不仅写诗，更喜欢给孩子讲诗词背后的历史小知识。你的开场白通常是"好孩子，来，听爷爷给你讲..."。用词极其稳重工整，解释诗意时像讲睡前故事一样温柔且条理清晰。', img:'/file/poem_img_1778767583895.png' },
      { name:'智多星老师', personality:'严谨、好奇、爱提问', modelKey:'zhipu', prompt:'你是一位戴着黑框眼镜、拿着放大镜的侦探老师。你认为写诗就像解谜。你不会直接告诉孩子答案，而是会问："你看，这句诗里为什么要用这个动词呢？"。创作风格简练，逻辑性强。解释诗词时分条列点（1.意境；2.用词；3.哲理）。', img:'/file/poem_img_1778767610463.png' },
      { name:'海螺姐姐', personality:'温柔、治愈、擅长朗诵', modelKey:'mimo', prompt:'你是一位声音像海风一样温柔的邻家姐姐。你的文字非常有亲和力，多用"亲爱的、好宝贝"等称呼。诗词要像儿歌一样朗朗上口，特别注重韵律感，仿佛每一行字都是为了被优美地读出来而设计的。', img:'/file/poem_img_1778767641804.png' },
      { name:'问问博士', personality:'专业、全能、爱用对比', modelKey:'qwen', prompt:'你是一位戴着博士帽、博古通今的科学家。你喜欢把古诗和现代科学联系起来。你的知识储备极广，解释诗词时会自动关联相关的成语和同类古诗。语气专业但不严肃，充满探索欲望。', img:'/file/poem_img_1778767668564.png' }
    ]
    var baseURL = wx.getStorageSync('baseURL') || ''
    db.collection('ai_teachers').count().then(function(res) {
      if (res.total >= 6) { wx.setStorageSync('_teachers_inited', '1'); return }
      teachers.forEach(function(t) {
        wx.downloadFile({
          url: baseURL + t.img,
          success: function(df) {
            if (df.statusCode !== 200) return
            wx.cloud.uploadFile({
              cloudPath: 'teacher_avatars/' + t.modelKey + '.png',
              filePath: df.tempFilePath,
              success: function(up) {
                db.collection('ai_teachers').add({
                  data: { name:t.name, personality:t.personality, modelKey:t.modelKey, systemPrompt:t.prompt, avatarUrl:up.fileID, createTime:new Date() }
                }).then(function() { wx.setStorageSync('_teachers_inited', '1') })
              }
            })
          }
        })
      })
    })
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
        msgs.push({ from: 'ai', text: '小诗人正在研墨构思...', loading: true })
        that.setData({ messages: msgs, waiting: true })
        that._uploadAndCompose('image', path)
      }
    })
  },

  startVoice() {
    var that = this
    recorder = wx.getRecorderManager()
    recorder.onStart(() => { that.setData({ recording: true }) })
    recorder.onStop((res) => {
      that.setData({ recording: false })
      if (!res.tempFilePath) return
      that.setData({ waiting: true })
      var msgs = that.data.messages
      msgs.push({ from: 'ai', text: '🎧 正在识别语音...', loading: true })
      that.setData({ messages: msgs })
      wx.uploadFile({
        url: baseURL + '/voice/chat', filePath: res.tempFilePath, name: 'audio',
        header: { Token: wx.getStorageSync('token') },
        success(upRes) {
          that.data.messages.pop()
          that.setData({ messages: that.data.messages, waiting: false })
          var data = JSON.parse(upRes.data)
          var text = (data.data && data.data.recognized) || ''
          if (text) {
            that.setData({ inputText: text })
            wx.showToast({ title: '识别成功', icon: 'success', duration: 1000 })
          } else { wx.showToast({ title: '没听清，请重试', icon: 'none' }) }
        },
        fail() { that.data.messages.pop(); that.setData({ messages: that.data.messages, waiting: false }); wx.showToast({ title: '网络错误', icon: 'none' }) }
      })
    })
    recorder.onError(() => { that.setData({ recording: false }) })
    recorder.start({ duration: 15000, sampleRate: 16000, numberOfChannels: 1, encodeBitRate: 48000, format: 'aac' })
  },

  _uploadAndCompose(type, filePath) {
    var that = this
    var formKey = type === 'image' ? 'image' : 'audio'
    wx.uploadFile({
      url: baseURL + '/poem-creator/process', filePath: filePath, name: formKey,
      timeout: 90000,
      success(res) {
        that.data.messages.pop()
        var data = JSON.parse(res.data)
        var poem = (data.data) || ''
        if (poem) that._showPoem(poem)
        else { that.setData({ waiting: false }); wx.showToast({ title: data.msg || '生成失败', icon: 'none' }) }
      },
      fail() { that._showError() }
    })
  },

  stopVoice() { if (recorder) { recorder.stop(); recorder = null } },

  // ===== 核心作诗 =====
  _startCompose(scene, type) {
    var that = this
    var msgs = this.data.messages
    if (type === 'text') msgs.push({ from: 'user', text: scene, type: 'text' })
    msgs.push({ from: 'ai', text: '小诗人正在研墨构思...', loading: true })
    this.setData({ messages: msgs, inputText: '', waiting: true })

    var m = MODELS[modelIdx]
    if (m.type === 'cloud' && wx.cloud && wx.cloud.extend && wx.cloud.extend.AI) {
      wx.showToast({ title: m.name + '作诗中...', icon: 'none', duration: 1000 })
      this._composeWithCloudAI(scene)
    } else {
      wx.showToast({ title: m.name + '作诗中...', icon: 'none', duration: 1000 })
      var ep = m.endpoint || '/poem-creator/process'
      wx.request({
        url: baseURL + ep + '?text=' + encodeURIComponent(scene),
        method: 'GET', timeout: 60000,
        success(res) {
          that.data.messages.pop()
          var poem = (res.data && res.data.data) || '作诗失败，请重试'
          that.setData({ messages: that.data.messages, waiting: false })
          that._showPoem(poem)
        },
        fail() { that._showError() }
      })
    }
  },

  async _composeWithCloudAI(scene) {
    var that = this
    try {
      var m = MODELS[modelIdx]
      console.log("[诗词小诗人] 🚀 云AI模型: " + m.name)
      var model = wx.cloud.extend.AI.createModel("cloudbase")
      var systemPrompt = "你是充满童趣的AI小诗人，专门为小学生创作古诗。请用通俗优美的语言创作五言或七言诗，必须包含诗名、作者（AI小诗人）、正文和'诗人老师说'（大白话解释）。格式：[诗名]...\n[作者]AI小诗人\n[正文]...\n[诗人老师说]..."
      var res = await model.streamText({
        data: {
          model: m.model,
          messages: [
            { role: "system", content: systemPrompt },
            { role: "user", content: "请根据以下内容创作诗：" + scene }
          ]
        }
      })
      var poem = ''
      for await (var str of res.textStream) { poem += str }
      that.data.messages.pop()
      if (poem) {
        that.setData({ messages: that.data.messages, waiting: false })
        that._showPoem(poem)
      } else {
        that._fallbackCompose(scene)
      }
    } catch(e) {
      console.log("[诗词小诗人] 云AI失败:", e)
      that._fallbackCompose(scene)
    }
  },

  _fallbackCompose(scene) {
    var that = this
    wx.request({
      url: baseURL + '/poem-creator/process?text=' + encodeURIComponent(scene),
      method: 'GET', timeout: 60000,
      success(res) {
        that.data.messages.pop()
        var poem = (res.data && res.data.data) || '作诗失败，请重试'
        that.setData({ messages: that.data.messages, waiting: false })
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
    if (this._ttsAudio) { try { this._ttsAudio.destroy() } catch(ex) {} }
    var audio = wx.createInnerAudioContext()
    this._ttsAudio = audio
    audio.src = url; audio.autoplay = true
    audio.onError(() => { wx.showToast({ title: '播放失败', icon: 'none' }) })
  },

  _showError() {
    this.data.messages.pop()
    this.data.messages.push({ from: 'ai', text: '小诗人打了个盹，请重试～', loading: false })
    this.setData({ messages: this.data.messages, waiting: false })
  }
})
