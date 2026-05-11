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
        name: ""
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

    },

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