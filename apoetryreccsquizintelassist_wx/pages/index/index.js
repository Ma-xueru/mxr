const {
  newsData,
  list,
  page,
  autoSort,
  swiperData,
  info,
  exampaperlist
  } = require("../../api/index.js")
  const utils = require('../../utils/index.js')
  const menu = require('../../utils/menu.js')
  Page({
  data: {
  value: null,
       baseURL: wx.getStorageSync('baseURL') + "/",
      
  isIndex: false,
  forumArr: [],
  newsList:[],
  swiperList:[],
  frontMenuList: [],
  courseList: [],
    showRecite: true,
    isStudent: true,
    pendingCount: 0,
    pendingTasks: [],
    taskStats: { total: 0, completed: 0, pending: 0 }
  },
  
  onReady() {
  },
  onSwiperTap(e){
  
  },
  checkMoreTap(e) {
  const tableName = e.currentTarget.dataset.tablename;
  const tabar = getApp().globalData.tabarList
  if(tabar.includes(tableName)){
   wx.switchTab({
       url: `/pages/${tableName}/list`,
   })
  }else{
   wx.navigateTo({
       url: `/pages/${tableName}/list`,
   })
  }
  
  
  },
  async onLoad(options) {
      this.getData()
  },
  async onShow() {
  this.onLoad()
  },
  
  selectTap(e) {
      const item = e.currentTarget.dataset.item;
      this.selectComponent('#bottomFrame').showFrame();
      this.setData({
          childItem: item.child
      })
  },
  tomenuTap(e) {
      const myitem = e.currentTarget.dataset.myitem;
      console.log('tableName', myitem);
      utils.menuTap(myitem.tableName)
  },
  cancelShow() {
      this.selectComponent('#bottomFrame').hideFrame();
  },
  menuTap(e) {
      const item = e.currentTarget.dataset.item;
      console.log('item', item);
      const tableName = item.child[0].tableName;
      console.log('tableName', tableName);
      utils.menuTap(tableName)
  
  },
  toNewsDetail(e) {
      const id = e.currentTarget.dataset.id;
      console.log("id", id);
      wx.navigateTo({
          url: `/pages/news/detail?id=${id}`,
      })
  },
  toNewsList() {
      const tabar = getApp().globalData.tabarList
      if(tabar.includes('news')){
          wx.switchTab({
              url: `/pages/news/list`,
          })
      }else{
          wx.navigateTo({
              url: `/pages/news/list`,
          })
      }
  },
  
  // 跳转到古诗词学习详情
  toCourseDetail(e) {
      const id = e.currentTarget.dataset.id;
      wx.navigateTo({
          url: `/pages/course/detail?id=${id}`,
      })
  },
  
  // 跳转到古诗词学习列表
  toCourseList() {
      const tabar = getApp().globalData.tabarList
      if(tabar.includes('exampaper')){
          wx.switchTab({
              url: `/pages/exampaper/list`,
          })
      }else{
          wx.navigateTo({
              url: `/pages/exampaper/list`,
          })
      }
  },
  toAichat() {
      wx.navigateTo({ url: '/pages/chat/chat' })
  },
  goRecite() {
      wx.navigateTo({ url: '/pages/recitationtask/list' })
  },
  
	goPoemCreator() {
		wx.navigateTo({ url: '/pages/poem-creator/index' })
	},
	goTaskQuiz() {
		wx.navigateTo({ url: '/pages/quiz/task-list' })
	},

	goWrongbook() {
		wx.navigateTo({ url: '/pages/quiz/wrongbook' })
	},

	goReciteTask(e) {
      const id = e.currentTarget.dataset.id
      getApp().globalData.detailId = id
      wx.navigateTo({ url: '/pages/recitationtask/update-and-add?id=' + id })
  },
      
  
  onSearch() {},
  
  onHide() {
  
  },
  
  onUnload() {
  
  },
  imageErrorHandler(e) {
      console.log('图片加载失败:', e)
      // 在这里可以进行错误处理，例如显示加载失败的提示文字或设置默认图片
  },
  onPullDownRefresh() {
  
  },
  
  
  onReachBottom() {
  
  },
  
  
  onShareAppMessage() {
  
  },
  
  async getData() {
   let baseURL=wx.getStorageSync("baseURL")+"/"
      this.setData({
          baseURL
      })
  const role=  wx.getStorageSync("role");
  const menus = menu.default.list()
  const frontMenuList=[]
  menus.forEach((item,key) => {
      if(role==item.roleName) {
          item.frontMenu.forEach((item2,key2) => {
              if(item2.child[0].buttons.indexOf("查看")>-1) {
                  frontMenuList.push(item2);
              }
          })
      }
  })
  this.setData({
      frontMenuList
  })
  
  const swiperRes = await swiperData(1, 5)
  const swiperList = swiperRes?.data?.list.filter(item=>(item.value)).map(item => {
          return {
              img: baseURL + item.value,
              title: item.name,
              id: item.id
          };
  })
  this.setData({
      swiperList
  })
  
  // 获取古诗词学习推荐数据：按点赞人数倒序取前四首
  const courseRes = await list('course', {
      page: 1,
      limit: 50,
      sort: 'thumbsupnum',
      order: 'desc'
  })
  const courseList = (courseRes.data.list || [])
      .sort((a, b) => Number(b.thumbsupnum || 0) - Number(a.thumbsupnum || 0))
      .slice(0, 4)
  this.setData({
      courseList
  })
  
  const newsRes = await newsData(1, 6)
  const newsList = newsRes.data.list.map(item => {
  if (item) {
      item.addtime = item?.addtime.substring(0, 10);
  }
  return item;
  });
  this.setData({
  newsList
  })
  }
  })
  
