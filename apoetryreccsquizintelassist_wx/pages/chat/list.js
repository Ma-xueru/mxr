const {
  deleteData,
  page,
  list,
  session // 导入session方法
} = require("../../api/index.js")
const utils = require("../../utils/index.js")
Page({
  data: {
    messages: [],
    baseUrl: wx.getStorageSync('baseURL') + '/',
    inputText: '',
    currentPage: 1,
    totalPages: 1, // 改为从接口动态获取，默认1页
    scrollTop: 0,
    userid: "", // 初始化为空，由getUserInfo设置
    loading: false,
    userInfo: null,
    hasMore: true,
    zhanghao: "",
    isSending: false, // 控制发送按钮状态
    lastMsgId: "" // 记录最后一条消息的ID，用于判断新消息
  },

  onLoad() {
    this.getUserInfo(); // 先获取用户信息，再加载消息
    // 启动5秒自动刷新定时器
    this.startRefreshTimer();
  },

  onShow() {
    // 页面从后台返回时，重启定时器（防止后台时定时器暂停）
    if (!this.refreshTimer) {
      this.startRefreshTimer();
    }
  },

  onHide() {
    // 页面隐藏时，清除定时器
    this.clearRefreshTimer();
  },

  onUnload() {
    // 页面销毁时，彻底清除定时器
    this.clearRefreshTimer();
  },

  // 启动自动刷新定时器（5秒一次）
  startRefreshTimer() {
    this.clearRefreshTimer(); // 先清除旧定时器，避免重复创建
    this.refreshTimer = setInterval(() => {
      // 发送中或未登录时，不执行刷新
      if (!this.data.isSending && this.data.userid) {
        this.refreshNewMessages();
      }
    }, 5000); // 5000毫秒 = 5秒
  },

  // 清除自动刷新定时器
  clearRefreshTimer() {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer);
      this.refreshTimer = null;
    }
  },

  // 优先获取用户信息，成功后再加载消息
  async getUserInfo() {
    try {
      const res = await session('yonghu');
      if (res.code === 0 && res.data) {
        this.setData({
          userInfo: res.data,
          userid: res.data.id,
          zhanghao: res.data.nickname || res.data.username
        }, () => {
          // 用户信息获取成功后，加载历史消息
          this.loadMessages(true);
        });
      } else {
        this.setData({
          userInfo: null,
          userid: "",
          zhanghao: ""
        });
        wx.showToast({
          title: '未登录，无法查看对话',
          icon: 'none',
          duration: 1500
        });
        this.clearRefreshTimer(); // 未登录时，停止刷新
      }
    } catch (error) {
      console.error("获取用户信息失败", error);
      this.setData({
        userInfo: null,
        userid: "",
        zhanghao: ""
      });
      wx.showToast({
        title: '登录状态失效',
        icon: 'none',
        duration: 1500
      });
      this.clearRefreshTimer(); // 登录失效时，停止刷新
    }
  },

  // 加载消息列表（初始加载/下拉加载历史消息）
  loadMessages(initialLoad = false) {
    const { loading, hasMore, userid } = this.data;
    // 若未登录、正在加载或无更多数据，直接返回
    if (!userid || loading || !hasMore) return;

    this.setData({ loading: true });

    wx.request({
      url: `${this.data.baseUrl}chat/page2`,
      method: 'GET',
      data: {
        page: initialLoad ? 1 : this.data.currentPage,
        limit: 30,
        userid
      },
      header: { 'Token': wx.getStorageSync("token") },
      success: res => {
        if (res.statusCode === 200 && res.data.code === 0) {
          const newMessages = res.data.data.list || [];
          // 从接口获取总页数，替换硬编码的5
          const totalPages = res.data.data.totalPages || 1;
          const hasMore = initialLoad 
            ? newMessages.length > 0 && 1 < totalPages 
            : newMessages.length > 0 && this.data.currentPage < totalPages;

          // 记录最后一条消息的ID（用于后续刷新判断）
          const lastMsgId = newMessages.length > 0 ? newMessages[newMessages.length - 1].id : "";

          this.setData({
            messages: initialLoad ? newMessages : [...newMessages, ...this.data.messages], // 下拉加载追加到前面
            currentPage: initialLoad ? 2 : this.data.currentPage + 1, // 初始加载后，下一页为2
            totalPages,
            hasMore,
            lastMsgId: initialLoad ? lastMsgId : this.data.lastMsgId // 仅初始加载时更新lastMsgId
          }, () => {
            // 初始加载完成后滚动到底部
            initialLoad && this.scrollToBottom();
          });
        } else {
          console.error('消息加载失败', res.data.msg || '接口返回异常');
        }
      },
      fail: err => {
        console.error('消息请求出错', err);
        wx.showToast({ title: '消息加载失败', icon: 'none' });
      },
      complete: () => {
        this.setData({ loading: false });
      }
    });
  },

  // 自动刷新：仅加载最新的新消息（核心新增方法）
  refreshNewMessages() {
    const { userid, lastMsgId, loading } = this.data;
    // 未登录、正在加载或无历史消息时，不执行刷新
    if (!userid || loading || !lastMsgId) return;

    wx.request({
      url: `${this.data.baseUrl}chat/page2`,
      method: 'GET',
      data: {
        page: 1, // 最新消息在第1页
        limit: 30,
        userid
      },
      header: { 'Token': wx.getStorageSync("token") },
      success: res => {
        if (res.statusCode === 200 && res.data.code === 0) {
          const latestMessages = res.data.data.list || [];
          if (latestMessages.length === 0) return;

          // 找到本地未有的新消息（对比lastMsgId）
          const lastLocalMsgIndex = latestMessages.findIndex(msg => msg.id === lastMsgId);
          const newMessages = lastLocalMsgIndex > -1 
            ? latestMessages.slice(lastLocalMsgIndex + 1) // 只取lastMsgId之后的新消息
            : latestMessages; // 若本地消息已不存在（如删除），则全量更新

          if (newMessages.length > 0) {
            this.setData({
              messages: [...this.data.messages, ...newMessages], // 追加新消息到末尾
              lastMsgId: newMessages[newMessages.length - 1].id // 更新最后一条消息ID
            }, () => {
              // 有新消息时，滚动到底部
              this.scrollToBottom();
            });
          }
        } else {
          console.error('自动刷新失败', res.data.msg || '接口返回异常');
        }
      },
      fail: err => {
        console.error('自动刷新请求出错', err);
      }
    });
  },

  // 输入框内容变化
  onInput(e) {
    this.setData({ inputText: e.detail.value.trim() });
  },

  // 发送消息（核心优化：本地先显示，接口返回后更新）
  sendMessage() {
    const { inputText, userid, userInfo, isSending } = this.data;
    if (!inputText || isSending || !userid) return;

    // 1. 本地临时生成消息（立即显示，提升体验）
    const tempMsg = {
      id: `temp-${Date.now()}`, // 临时ID
      ask: inputText,
      isreply: 0, // 0=用户消息
      addtime: this.formatTime(new Date()), // 本地时间
      zhaopian: userInfo.zhaopian || '' // 用户头像
    };

    this.setData({
      inputText: '',
      isSending: true,
      messages: [...this.data.messages, tempMsg] // 追加到消息列表
    }, () => {
      this.scrollToBottom(); // 滚动到底部

      // 2. 调用接口保存消息
      wx.request({
        url: `${this.data.baseUrl}chat/save`,
        method: 'POST',
        data: { ask: inputText, userid },
        header: { 'Token': wx.getStorageSync("token") },
        success: res => {
          if (res.statusCode === 200 && res.data.code === 0) {
            // 接口返回后，触发一次刷新（立即获取AI回复，无需等5秒）
            this.refreshNewMessages();
          } else {
            console.error('消息发送失败', res.data.msg);
            wx.showToast({ title: '消息发送失败', icon: 'none' });
            // 失败时，移除本地临时消息
            this.setData({
              messages: this.data.messages.filter(msg => msg.id !== tempMsg.id)
            });
          }
        },
        fail: err => {
          console.error('消息发送请求出错', err);
          wx.showToast({ title: '网络异常，发送失败', icon: 'none' });
          // 失败时，移除本地临时消息
          this.setData({
            messages: this.data.messages.filter(msg => msg.id !== tempMsg.id)
          });
        },
        complete: () => {
          this.setData({ isSending: false });
        }
      });
    });
  },

  // 滚动到底部（修复原代码失效问题）
  scrollToBottom() {
    wx.nextTick(() => {
      const query = wx.createSelectorQuery().in(this);
      query.select('.chat-list').boundingClientRect(rect => {
        if (rect) {
          this.setData({ scrollTop: rect.scrollHeight });
        }
      }).exec();
    });
  },

  // 下拉加载更多（修复原事件绑定错误）
  loadPreviousPage() {
    this.loadMessages(false);
  },

  // 格式化时间（yyyy-MM-dd HH:mm:ss）
  formatTime(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hour = String(date.getHours()).padStart(2, '0');
    const minute = String(date.getMinutes()).padStart(2, '0');
    const second = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
  }
});