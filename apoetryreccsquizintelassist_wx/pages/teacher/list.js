Page({
  data: {
    teachers: [], selectedId: '', currentTeacher: null
  },

  onLoad() { this.loadTeachers() },
  onShow() { this.loadTeachers() },

  loadTeachers() {
    var that = this
    var selectedId = wx.getStorageSync('selectedTeacherId') || ''
    if (!wx.cloud) {
      // 兜底：本地数据
      this.setData({ teachers: this._getDefaultTeachers(), selectedId: selectedId })
      return
    }
    wx.cloud.database().collection('ai_teachers').get().then(function(res) {
      var list = (res.data || []).map(function(t) {
        t.tags = that._getTags(t.modelKey)
        // 云存储URL转换
        if (t.avatarUrl && t.avatarUrl.indexOf('cloud://') === 0) {
          wx.cloud.getTempFileURL({ fileList: [t.avatarUrl] }).then(function(urlRes) {
            if (urlRes.fileList && urlRes.fileList[0]) t.avatarUrl = urlRes.fileList[0].tempFileURL
            that.setData({ teachers: list })
          })
        }
        return t
      })
      that.setData({ teachers: list, selectedId: selectedId })
      if (selectedId) {
        var found = list.find(function(t) { return t._id === selectedId }) || list[0]
        that._applyTeacher(found)
      }
    }).catch(function() {
      that.setData({ teachers: that._getDefaultTeachers(), selectedId: selectedId })
    })
  },

  _getDefaultTeachers() {
    var baseURL = wx.getStorageSync('baseURL') || ''
    return [
      { _id:'1', name:'墨墨老师', personality:'优雅、感性、擅长留白', modelKey:'deepseek', avatarUrl: baseURL + '/file/poem_img_1778767526732.png', tags:['意境推敲','唯美意象','诗意生活'] },
      { _id:'2', name:'豆豆助教', personality:'活泼、急性子、爱用Emoji', modelKey:'doubao', avatarUrl: baseURL + '/file/poem_img_1778767555610.png', tags:['快问快答','打油诗','趣味互动'] },
      { _id:'3', name:'元气夫子', personality:'慈祥、博学、爱讲故事', modelKey:'hunyuan', avatarUrl: baseURL + '/file/poem_img_1778767583895.png', tags:['历史典故','睡前故事','稳重工整'] },
      { _id:'4', name:'智多星老师', personality:'严谨、好奇、爱提问', modelKey:'zhipu', avatarUrl: baseURL + '/file/poem_img_1778767610463.png', tags:['逻辑推理','字词解谜','分步解析'] },
      { _id:'5', name:'海螺姐姐', personality:'温柔、治愈、擅长朗诵', modelKey:'mimo', avatarUrl: baseURL + '/file/poem_img_1778767641804.png', tags:['韵律朗读','儿歌创作','温柔鼓励'] },
      { _id:'6', name:'问问博士', personality:'专业、全能、爱用对比', modelKey:'qwen', avatarUrl: baseURL + '/file/poem_img_1778767668564.png', tags:['科学关联','百科知识','对比学习'] }
    ]
  },

  _getTags(modelKey) {
    var map = {
      deepseek: ['意境推敲','唯美意象','诗意生活'],
      doubao: ['快问快答','打油诗','趣味互动'],
      hunyuan: ['历史典故','睡前故事','稳重工整'],
      zhipu: ['逻辑推理','字词解谜','分步解析'],
      mimo: ['韵律朗读','儿歌创作','温柔鼓励'],
      qwen: ['科学关联','百科知识','对比学习']
    }
    return map[modelKey] || ['AI教学']
  },

  selectTeacher(e) {
    var id = e.currentTarget.dataset.id
    var idx = e.currentTarget.dataset.index
    var teacher = this.data.teachers[idx]
    if (!teacher) return
    this.setData({ selectedId: id, currentTeacher: teacher })
    wx.setStorageSync('selectedTeacherId', id)
    wx.setStorageSync('selectedTeacher', teacher)
    var baseURL = wx.getStorageSync('baseURL') || ''
    wx.request({
      url: baseURL + '/voice/teacher/select', method: 'POST',
      header: { 'Content-Type': 'application/json', Token: wx.getStorageSync('token') },
      data: JSON.stringify({ modelKey: teacher.modelKey, systemPrompt: teacher.systemPrompt }),
      success: function() { wx.showToast({ title: teacher.name + '：你好呀！接下来的学习由我来陪你哦！', icon: 'none', duration: 2000 }) },
      fail: function() { wx.showToast({ title: '切换失败', icon: 'none' }) }
    })
  }
})
