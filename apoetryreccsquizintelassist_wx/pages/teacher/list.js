var characters = [
  {
    id: 'houge', name: '猴哥', title: '古诗急先锋',
    shortDesc: '俺老孙来也！跟我学，诗词就像筋斗云，嗖地一下就学会了！',
    tags: ['快问快答', '西游秘闻', '金睛火眼'], color: '#e17055', emoji: '🐵',
    ttsVoice: 'zh_male_sunwukong_mars_bigtts',
    detail: {
      voice: '猴哥经典音色（大模型1.0）',
      style: '古灵精怪，急躁却热心，喜欢用降妖除魔的比喻来讲诗。讲《大林寺桃花》，他会说："这花儿比王母娘娘的蟠桃园还好看，咱们去瞅瞅！"',
      dialogNote: '多用短句，语气上扬，充满活力，偶尔冒出"呆子"、"看棒"等口头禅。快节奏快问快答。'
    }
  },
  {
    id: 'bajie', name: '猪八戒', title: '呆萌诗词美食家',
    shortDesc: '哼哼，学累了？咱们先吃口西瓜润润喉。诗里要是有好吃的，俺老猪第一个会背！',
    tags: ['馋嘴对诗', '趣味绕口令', '好吃懒学'], color: '#e77f67', emoji: '🐷',
    ttsVoice: 'zh_male_zhubajie_mars_bigtts',
    detail: {
      voice: '猪八戒幽默音色（大模型1.0）',
      style: '憨厚老实，贪吃贪睡，有点小聪明，喜欢把诗和食物联系在一起。讲《悯农》，他会心疼地说："这粒粒皆辛苦啊，俺老猪要把饭盆舔干净！"',
      dialogNote: '语速较慢，语气憨厚，常常带着"哼哼"的鼻音，喜欢抱怨学习累，但又能被美食诱惑着继续学。'
    }
  },
  {
    id: 'tangseng', name: '唐僧', title: '佛系诗词引路人',
    shortDesc: '阿弥陀佛。善哉善哉。孩子，闭上眼睛，去感受那诗词中的山水与远方。',
    tags: ['温润如玉', '意境禅修', '情感共鸣'], color: '#e1b12c', emoji: '🧘',
    ttsVoice: 'zh_male_tangseng_mars_bigtts',
    detail: {
      voice: '唐僧慈爱音色（大模型1.0）',
      style: '温和有礼，循循善诱，非常有耐心，注重诗歌的情感和意境教育。讲《静夜思》，他会温柔地问："孩子，你看着月亮，可曾想念家乡的父母？"',
      dialogNote: '语速平缓，语气轻柔，多用排比和反问，充满智慧与慈爱，偶尔也有一点点啰嗦。'
    }
  },
  {
    id: 'peppa', name: '佩奇猪', title: '奇幻诗词小伙伴',
    shortDesc: 'Oink! Hello! 我们一起去泥坑里，哦不，去书海里探险吧！',
    tags: ['双语启蒙', '快乐读诗', '想象力爆发'], color: '#e84393', emoji: '🐽',
    ttsVoice: 'zh_female_peiqi_mars_bigtts',
    detail: {
      voice: '佩奇猪灵动音色（大模型1.0）',
      style: '活泼开朗，充满好奇心，调皮，喜欢跳泥坑，喜欢用英语讲简单的古诗。讲《咏鹅》，她会开心地叫："Peppa Loves Geese! 我们来学天鹅游泳的样子！"',
      dialogNote: '声音清脆，语气兴奋，喜欢用简单的句子，偶尔会夹杂简单的英语单词，充满童趣。'
    }
  },
  {
    id: 'xionger', name: '熊二', title: '森林智慧守护者',
    shortDesc: '俺是熊二。森林里的树、花、鸟都在诗里呢。俺带你去找那诗里的森林秘宝！',
    tags: ['森林诗会', '自然探索', '憨态可掬'], color: '#b8860b', emoji: '🐻',
    ttsVoice: 'zh_male_xionger_mars_bigtts',
    detail: {
      voice: '熊二憨厚音色（大模型1.0）',
      style: '憨厚老实，有点小迷糊，但热爱大自然。讲《江雪》，他会抱着蜂蜜罐子说："这雪下得俺都想冬眠了，俺要去给那个钓鱼老翁送个热蜂蜜！"',
      dialogNote: '语速憨厚，语气真诚，喜欢自称"俺"，对话中充满森林、动物和蜂蜜的比喻。'
    }
  },
  {
    id: 'cancan', name: '灿灿仙子', title: '元气仙子',
    shortDesc: '我是灿灿。快跟我来，我们去诗词的云端尽情飞舞吧！',
    tags: ['通用场景', '灵动有趣', '古风仙气'], color: '#8854d0', emoji: '🧚',
    ttsVoice: 'zh_female_cancan_mars_bigtts',
    detail: {
      voice: '灿灿灵动音色（大模型1.0）',
      style: '灵动有趣，古风仙气，多才多艺，喜欢唱歌跳舞来讲诗。讲《相思》，她会轻抚长袖："红豆生南国，此物最相思。来，我教你一段红豆舞。"',
      dialogNote: '语速灵动，语气轻盈，喜欢用诗意的语言，偶尔也会调皮一下。'
    }
  }
]

Page({
  data: {
    characters: characters,
    selectedId: '',
    showDetail: false,
    detailChar: null,
    pendingIndex: -1
  },

  onLoad() {
    var sid = wx.getStorageSync('selectedCharacterId') || ''
    this.setData({ selectedId: sid })
  },

  onShow() {
    var sid = wx.getStorageSync('selectedCharacterId') || ''
    if (sid !== this.data.selectedId) this.setData({ selectedId: sid })
  },

  selectCharacter(e) {
    var id = e.currentTarget.dataset.id
    var idx = e.currentTarget.dataset.index
    var ch = this.data.characters[idx]
    if (!ch) return
    this._applyCharacter(ch)
  },

  showDetail(e) {
    var idx = e.currentTarget.dataset.index
    this.setData({ showDetail: true, detailChar: this.data.characters[idx], pendingIndex: idx })
  },

  closeDetail() {
    this.setData({ showDetail: false })
  },

  confirmSelect() {
    var idx = this.data.pendingIndex
    if (idx < 0) return
    var ch = this.data.characters[idx]
    this.setData({ showDetail: false })
    this._applyCharacter(ch)
  },

  // 每个人物的专属切换音效文本
  _greeting(ch) {
    var map = {
      houge:  '嘿嘿！俺老孙来也！从今往后，俺就是你的古诗师父。先把金箍棒放下，咱们学诗去！',
      bajie:  '哼哼～俺老猪来啦！这学诗嘛，就跟吃人参果一样，得细嚼慢咽。来来来，先吃口西瓜润润喉～',
      tangseng: '阿弥陀佛。贫僧唐三藏。孩子，莫急莫慌，诗词之路，为师陪你一步一步走。闭上眼睛，去感受那文字中的山水与远方。善哉善哉。',
      peppa:  'Oink oink! Hello! 我是佩奇！今天我们不跳泥坑，我们去诗海里探险。乔治也想来学，我只带你一个！嘻嘻～',
      xionger: '嘿嘿～俺是熊二！森林里的树啊花啊鸟啊，全都在古诗里藏着呢。俺抱着蜂蜜罐子，带你去寻宝！',
      cancan: '嗨～我是灿灿！诗词是天上的仙气，快跟我来。我挥一挥衣袖，带你飞到唐诗宋词的云端上跳舞～'
    }
    return map[ch.id] || (ch.name + '来教你啦！')
  },

  _applyCharacter(ch) {
    var that = this
    this.setData({ selectedId: ch.id })
    wx.setStorageSync('selectedCharacterId', ch.id)
    wx.setStorageSync('selectedCharacter', ch)
    getApp().globalData.selectedCharacterId = ch.id

    var baseURL = wx.getStorageSync('baseURL') || ''
    // 1. 先切换音色
    wx.request({
      url: baseURL + '/voice/tts/select', method: 'POST',
      header: { 'Content-Type': 'application/json', Token: wx.getStorageSync('token') },
      data: JSON.stringify({ voice: ch.ttsVoice }),
      success: function() {
        // 2. 用新音色合成专属欢迎语并播放
        var greeting = that._greeting(ch)
        wx.request({
          url: baseURL + '/followread/tts', method: 'POST',
          header: { 'Content-Type': 'application/json', Token: wx.getStorageSync('token') },
          data: JSON.stringify({ text: greeting }),
          success: function(ttsRes) {
            var ttsUrl = (ttsRes.data && ttsRes.data.data) || ''
            if (!ttsUrl) { wx.showToast({ title: ch.name + '来啦！', icon: 'none' }); return }
            if (ttsUrl.indexOf('http') !== 0) ttsUrl = baseURL + ttsUrl
            var audio = wx.createInnerAudioContext()
            audio.src = ttsUrl
            audio.play()
            wx.showToast({ title: ch.name + '已就位', icon: 'none', duration: 1500 })
          },
          fail: function() { wx.showToast({ title: ch.name + '来啦！', icon: 'none' }) }
        })
      },
      fail: function() { wx.showToast({ title: '已选' + ch.name, icon: 'none' }) }
    })
  }
})
