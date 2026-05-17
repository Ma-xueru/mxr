// pages/forumCenter/forumCenter.js
const {
    exampaperlist,deleteData,
    deleteRecords,
    session
} = require("../../api/index")
Page({

    data: {
        userid:"",
        list: [],
        currentList: [],
        name: "",
        showRank: false,
        rankClassName: '',
        rankList: []
    },
    async onLoad(options) {
        let nowTable = wx.getStorageSync("nowTable");
        const res = await session(nowTable)
        if(res.code==0){
            this.setData({
                userid:res.data.id
            })
        }
    },

    /**
     * 生命周期函数--监听页面初次渲染完成
     */
    async onShow() {
        this.getData()
        this.loadSmartCenter()
        this.loadFeihualingProfile()
    },

    loadSmartCenter() {
        var that = this
        var baseURL = wx.getStorageSync('baseURL') || ''
        wx.request({
            url: baseURL + '/quiz/smart-center',
            method: 'GET', header: { Token: wx.getStorageSync('token') },
            success: function(res) {
                if (res.data.code !== 0) return
                var data = res.data.data
                that.setData({ ability: data.ability, dimensions: data.dimensions })
                that.drawRadar(data.ability, data.dimensions)
                // 把W值映射到列表
                var weights = {}
                ;(data.recommended || []).forEach(function(r) { weights[r.courseId] = r })
                var list = that.data.currentList.map(function(item) {
                    var w = weights[item.id]
                    return w ? Object.assign({}, item, { weight: w.weight, latestScore: w.latestScore }) : item
                })
                that.setData({ currentList: list })
            }
        })
    },

    drawRadar(ability, dims) {
        var that = this
        var query = wx.createSelectorQuery().in(this)
        query.select('#radarCanvas').fields({ node: true, size: true }).exec(function(res) {
            if (!res || !res[0] || !res[0].node) return
            var canvas = res[0].node, ctx = canvas.getContext('2d')
            var w = res[0].width, h = res[0].height
            var dpr = wx.getSystemInfoSync().pixelRatio
            canvas.width = w * dpr; canvas.height = h * dpr; ctx.scale(dpr, dpr)

            var cx = w / 2, cy = h / 2, r = Math.min(w, h) / 2 - 30
            var n = dims.length, step = Math.PI * 2 / n
            var colors = ['#e57373','#64B5F6','#FFB74D','#81C784']

            // 网格
            for (var level = 1; level <= 5; level++) {
                var lr = r * level / 5
                ctx.beginPath()
                for (var i = 0; i <= n; i++) {
                    var angle = step * i - Math.PI / 2
                    var x = cx + lr * Math.cos(angle), y = cy + lr * Math.sin(angle)
                    i === 0 ? ctx.moveTo(x, y) : ctx.lineTo(x, y)
                }
                ctx.closePath(); ctx.strokeStyle = '#e8e0d0'; ctx.lineWidth = 0.5; ctx.stroke()
            }

            // 轴线
            for (var i = 0; i < n; i++) {
                ctx.beginPath(); ctx.moveTo(cx, cy)
                ctx.lineTo(cx + r * Math.cos(step * i - Math.PI/2), cy + r * Math.sin(step * i - Math.PI/2))
                ctx.strokeStyle = '#e8e0d0'; ctx.stroke()
            }

            // 数据区
            ctx.beginPath()
            for (var i = 0; i < n; i++) {
                var val = (ability[dims[i]] || 50) / 100
                var angle = step * i - Math.PI / 2
                var x = cx + r * val * Math.cos(angle), y = cy + r * val * Math.sin(angle)
                i === 0 ? ctx.moveTo(x, y) : ctx.lineTo(x, y)
            }
            ctx.closePath()
            ctx.fillStyle = 'rgba(129,199,132,0.2)'; ctx.fill()
            ctx.strokeStyle = '#4CAF50'; ctx.lineWidth = 2; ctx.stroke()

            // 数据点+标签
            for (var i = 0; i < n; i++) {
                var val = (ability[dims[i]] || 50) / 100
                var angle = step * i - Math.PI / 2
                var x = cx + r * val * Math.cos(angle), y = cy + r * val * Math.sin(angle)
                ctx.fillStyle = colors[i]; ctx.beginPath(); ctx.arc(x, y, 5, 0, 2*Math.PI); ctx.fill()
                // 标签
                var lx = cx + (r + 28) * Math.cos(angle), ly = cy + (r + 28) * Math.sin(angle)
                ctx.fillStyle = '#555'; ctx.font = '12px sans-serif'; ctx.textAlign = 'center'
                ctx.fillText(dims[i] + ' ' + (ability[dims[i]]||0), lx, ly + 4)
            }
        })
    },

    loadFeihualingProfile() {
        var that = this
        var baseURL = wx.getStorageSync('baseURL') || ''
        wx.request({
            url: baseURL + '/game/profile', method: 'GET',
            header: { Token: wx.getStorageSync('token') },
            success: function(res) {
                if (res.data.code === 0) that.setData({ fhlProfile: res.data.data })
            }
        })
    },

    startHistoryReview() {
        var that = this
        this.setData({ aiLoading: true, aiLoadingHint: '正在分析近14天学习足迹...' })
        wx.request({
            url: (wx.getStorageSync('baseURL') || '') + '/game/history-review', method: 'GET',
            header: { Token: wx.getStorageSync('token') },
            timeout: 45000,
            success: function(res) { that._onQuizReady(res) },
            fail: function() { that.setData({ aiLoading: false }); wx.showToast({ title: '出题超时，请重试', icon: 'none' }) }
        })
    },

    startAnalogy() {
        var that = this
        this.setData({ aiLoading: true, aiLoadingHint: '正在提取薄弱标签并匹配同类题目...' })
        wx.request({
            url: (wx.getStorageSync('baseURL') || '') + '/game/analogy-training', method: 'GET',
            header: { Token: wx.getStorageSync('token') },
            timeout: 45000,
            success: function(res) { that._onQuizReady(res) },
            fail: function() { that.setData({ aiLoading: false }); wx.showToast({ title: '出题超时，请重试', icon: 'none' }) }
        })
    },

    _onQuizReady(res) {
        this.setData({ aiLoading: false })
        if (res.data.code === 0 && res.data.data) {
            var questions = JSON.parse(res.data.data)
            getApp().globalData._aiQuestions = questions
            wx.navigateTo({ url: '/pages/quiz/practice?id=ai&title=' + encodeURIComponent('AI智能出题') + '&content=ai' })
        } else {
            wx.showToast({ title: res.data.msg || '出题失败', icon: 'none' })
        }
    },

    toFeiHuaLing() { wx.navigateTo({ url: '/pages/game/feihualing' }) },

    showRankPopup() {
      this.setData({ showRank: true })
      var ui = getApp().globalData.userInfo || {}
      var cn = ui.classname || '我的班级'
      this.setData({ rankClassName: cn })
      var that = this
      var baseURL = wx.getStorageSync('baseURL') || ''
      wx.request({
        url: baseURL + '/game/leaderboard', method: 'GET',
        header: { Token: wx.getStorageSync('token') },
        success: function(res) {
          if (res.data && res.data.code === 0) {
            that.setData({ rankList: (res.data.data || []).slice(0, 10) })
          }
        }
      })
    },

    hideRankPopup() { this.setData({ showRank: false }) },

    searhandler() {
        const result = this.data.list.filter((item, index) => {
            if (item.title.includes(this.data.name)) {
                return item
            }

        })
        this.setData({
            currentList: result
        })
    },

    async examTap(e) {
        const id = e.currentTarget.dataset.id;
                await deleteRecords(this.data.userid,id)
        // wx.setStorageSync('parentId', id)
        // // 将数组转换为字符串
        wx.navigateTo({
            url: `/pages/exampaper/exam?id=${id}`,
        })
    },

    /**
     * 生命周期函数--监听页面显示
     */

    async getData() {
        const obj = {
            order: 'desc',
            page: 1,
            limit: 20,
            status: 1
        }
        const {
            data
        } = await exampaperlist("exampaper", obj)
        this.setData({
            list: data.list,
            currentList: data.list,
            name: ""
        })
    },
    /**
     * 生命周期函数--监听页面卸载
     */
    onUnload() {
    },

    /**
     * 页面相关事件处理函数--监听用户下拉动作
     */
    onPullDownRefresh() {

    },

    /**
     * 页面上拉触底事件的处理函数
     */
    onReachBottom() {

    },
    backToTop() {
      wx.pageScrollTo({
      scrollTop: 0, // 返回顶部的位置
      duration: 1000, // 滚动动画的时长，单位为 ms
      });
      // 返回顶部时隐藏按钮
      
      },
    /**
     * 用户点击右上角分享
     */
    onShareAppMessage() {

    }
})