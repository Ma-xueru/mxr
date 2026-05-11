const utils = require('../../utils/index.js')
Component({
  properties: {
    listData: {
      type: Array | Object,
      value: {}
    },
    step: {
      type: Number,
      value: 1
    },
  },
  data: {
    isShowChildren: false,
  },
  methods: {
    toggleShowChildren() {
      this.setData({
        isShowChildren: !this.data.isShowChildren
      })
    },
    menuTap(e) {
      console.log("点击")
      const item = e.currentTarget.dataset.item;
      const tableName = item.tableName
      const tabar = getApp().globalData.tabarList;
      console.log(item)
      wx.setStorageSync('isAuth', true)
      if (tableName == 'forum') {
        if (item.menu == '学习社区') {
          wx.switchTab({
            url: `/pages/forum/list`,
          })
        }
        // 固定模板
        if (item.menu.includes('论坛')) {
          console.log("==1==")
          if (tabar.includes(tableName)) {
            console.log("==2==")
            wx.switchTab({
              url: `/pages/${tableName}/${tableName}-list`,
            })
          } else {
            console.log('23222');
            wx.navigateTo({
              url: `/pages/${tableName}/${tableName}-list`,
            })
          }

        }
        console.log("==3==")
        if (item.menu == '我的发布') {
          console.log("我的发布")
          wx.navigateTo({
            url: `/pages/${tableName}/${tableName}-my`,
          })
        }
        if (item.menu.includes('详情')) {
          wx.navigateTo({
            url: `/pages/${tableName}/${tableName}-detail`,
          })
        }
        if (item.menu.includes('回复')) {
          wx.navigateTo({
            url: `/pages/${tableName}/${tableName}-reply`,
          })
        }
      } else if (item.menu == "错题本") {
        wx.navigateTo({
          url: '/pages/examrecord/detail',
        })
      } else if (tableName == 'orders') {

        if (tabar.includes(tableName)) {
          wx.switchTab({
            url: '/pages/shop-orders/orders-list',
          })
        } else {
          wx.navigateTo({
            url: '/pages/shop-orders/orders-list',
          })
        }
      } else if (tableName == 'cart') {
        const tabar = getApp().globalData.tabarList;
        if (tabar.includes(tableName)) {
          wx.switchTab({
            url: '/pages/shop-cart/shop-cart',
          })
        } else {
          wx.navigateTo({
            url: '/pages/shop-cart/shop-cart',
          })
        }
      } else {
        let id = getApp().globalData.userInfo.id
        if (tableName) {
          utils.menuTap(tableName, id)
        }
      }




    },
  }
})