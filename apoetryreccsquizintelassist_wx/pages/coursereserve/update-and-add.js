// pages/edit/edit.js
const {
  detail,
  option,
  update,
  add,
  book,
  list,
  follow,
  faceMatch,
  session,
  rubbish,
  levelOption,
  baiduIdentify
} = require("../../api/index.js")

const des = require('../../utils/des.js')
const utils = require("../../utils/index.js")

Page({

  /**
   * 页面的初始数据
   */
  data: {
    imgIcon: "../../static/upload.png",
    editStatus: false,
    baseURL: '',
    sessionReadArr: [],

    detailList: null,
    id: "",
    cross: "",
    ruleForm: {},
    userid: getApp().globalData.userInfo.id,
    userInfo: getApp().globalData.userInfo,
    ro: {},

    studentaccount: "",
    studentname: "",
    reservetime: "请选择时间",
    showreservetime: false,
    teacheraccount: "",
    teachername: "",
    teacherList: [],
    teacherIndex: -1,
    reservestatus: '已预约',
    sfsh: '待审核',
    reservecount: "",
  },


  /**
   * 生命周期函数--监听页面加载
   */
  async onLoad(options) {
    let userid
    if (options?.id) {
      this.setData({
        editStatus: true
      })

    }
    if (options?.isAuth == true) {
      wx.setStorageSync('isAuth', true)
    }
    let nowTable = wx.getStorageSync("nowTable");
    const res = await session(nowTable)
    if (res.data.code == 0) {
      getApp().globalData.userInfo = res?.data
      userid = res?.data.id
      this.setData({
        userid
      })

    }

    let baseURL = wx.getStorageSync('baseURL') + '/'
    const id = getApp().globalData.detailId
    this.setData({
      refid: id,
      baseURL
    })
    await this.loadTeacherOptions()
    //人脸识别
    this.setData({
      reservestatusList: "已取消,已预约".split(',')
    })



    let ro = this.data.ro
    if (options?.cross) {
      var obj = wx.getStorageSync('crossObj');
      let refobjempty = {}
      for (var o in obj) {

        if (o == 'studentaccount') {
          refobjempty["studentaccount"] = obj[o]
          ro.studentaccount = true;
          continue;
        } else {}


        if (o == 'studentname') {
          refobjempty["studentname"] = obj[o]
          ro.studentname = true;
          continue;
        } else {}


        if (o == 'reservetime') {
          refobjempty["reservetime"] = obj[o]
          ro.reservetime = true;
          continue;
        } else {}


        if (o == 'teacheraccount') {
          refobjempty["teacheraccount"] = obj[o]
          ro.teacheraccount = true;
          continue;
        } else {}


        if (o == 'teachername') {
          refobjempty["teachername"] = obj[o]
          ro.teachername = true;
          continue;
        } else {}


        if (o == 'reservestatus') {
          refobjempty["reservestatus"] = obj[o]
          ro.reservestatus = true;
          continue;
        } else {}




        if (o == 'reservecount') {
          refobjempty["reservecount"] = obj[o]
          ro.reservecount = true;
          continue;
        } else {}

      }
      refobjempty['reservecount'] = 0;
      ro['reservecount'] = false;

      let statusColumnName = wx.getStorageSync('statusColumnName');
      statusColumnName = statusColumnName.replace('[', "").replace(']', "");
      this.setData({
        ro,
        cross: options?.cross,
        statusColumnName
      })
      this.setData(refobjempty)
    }

    if (id) {
      // 如果上一级页面传递了id，获取改id数据信息
      const data = getApp().globalData.detailList
      const def_6 = "已预约";
      this.data.reservestatusList.map((v, index) => {
        if (v == data.reservestatus || (v == def_6 && def_6 != null)) {
          this.setData({
            reservestatusIndex: index,
            reservestatus: v
          })
        } else if (this.data.reservestatusList.length == 1) {
          this.setData({
            reservestatusIndex: 0,
            reservestatus: v,
            reservestatusList: [def_6]
          })
        }
      })




      const url = wx.getStorageSync("baseURL") + "/"
      const detailList = data
      let objtemp = {
        detailList,
        studentaccount: data.studentaccount,
        studentname: data.studentname,
        reservetime: utils.getCurrentDate("yMDhms"),
        teacheraccount: data.teacheraccount,
        teachername: data.teachername,
        sfsh: '待审核',

        shhf: data.shhf,
        reservecount: data.reservecount,
      }
      this.setData(objtemp);
      this.syncTeacherPicker(data.teacheraccount)

      //ss读取
      let h = this.data
      let c = this.data
      this.setData({});

    } else {
      this.setData({})
    }



    // ss读取
    let sessionReadArr = []
    let studentaccount = getApp().globalData.userInfo.studentaccount
    ro.studentaccount = true
    this.setData({
      studentaccount,
    })
    sessionReadArr.push('studentaccount')
    let studentname = getApp().globalData.userInfo.studentname
    ro.studentname = true
    this.setData({
      studentname,
    })
    sessionReadArr.push('studentname')

    this.setData({
      cross: options?.cross,
      ro,
      id,
      sessionReadArr

    })










  },
  reservecountInput(e) {
    this.setData({
      reservecount: e.detail.value
    })
  },
  async loadTeacherOptions() {
    const res = await list('teacher', {
      page: 1,
      limit: 500,
      permissionstatus: '启用'
    })
    if (res && res.code === 0) {
      const teacherList = (res.data.list || []).map(item => ({
        teacheraccount: item.teacheraccount,
        teachername: item.teachername,
        reservecount: item.reservecount
      }))
      this.setData({ teacherList })
      this.syncTeacherPicker(this.data.teacheraccount)
    }
  },
  syncTeacherPicker(teacheraccount) {
    if (!teacheraccount || !Array.isArray(this.data.teacherList)) return
    const idx = this.data.teacherList.findIndex(item => item.teacheraccount === teacheraccount)
    if (idx > -1) {
      this.setData({
        teacherIndex: idx,
        teacheraccount: this.data.teacherList[idx].teacheraccount,
        teachername: this.data.teacherList[idx].teachername
      })
    }
  },
  teacherChange(e) {
    const idx = Number(e.detail.value)
    const teacher = this.data.teacherList[idx]
    if (!teacher) return
    this.setData({
      teacherIndex: idx,
      teacheraccount: teacher.teacheraccount,
      teachername: teacher.teachername
    })
  },
  getUUID() {
    return new Date().getTime();
  },
  onUnload: function () {
    console.log('页面被卸载，执行销毁操作');
  },
  onShow() {

  },







  //reservestatus, 0
  // 下拉变化
  reservestatusChange(e) {
    this.setData({
      reservestatusIndex: e.detail.value,
      reservestatus: this.data.reservestatusList[e.detail.value]
    })
  },








































  onreservetimeTap() {
    this.setData({
      showreservetime: true,
    })

  },
  reservetimeTap(e) {
    this.setData({
      reservetime: e.detail.data
    })

    let c = this.data;
    let h = this.data;
  },






































  shhfInput(e) {
    this.setData({
      shhf: e.detail.value // 每次输入变化时更新文本框的值
    });
  },















  async submit() {
    let that = this
    var query = wx.createSelectorQuery();



    if (this.data.reservetime?.includes("请选择") || this.data.reservetime == "") {
      wx.showToast({
        icon: "none",
        title: `预约时间不能为空`,
      })
      return
    }
    if (this.data.teacherIndex < 0 || !this.data.teacheraccount) {
      wx.showToast({
        icon: "none",
        title: `请选择预约教师`,
      })
      return
    }













    const baseURL = wx.getStorageSync('baseURL') + "/"
    const regex = new RegExp(baseURL, "g");
    const obj = {
      studentaccount: this.data.studentaccount,
      studentname: this.data.studentname,
      reservetime: this.data.reservetime,
      teacheraccount: this.data.teacheraccount,
      teachername: this.data.teachername,
      reservestatus: this.data.reservestatus,
      sfsh: this.data.sfsh,
      reservecount: this.data.reservecount,
    }
    const detailId = getApp().globalData.detailId
    const tableName = `coursereserve`
    const saveReserve = async (data) => {
      const nowTable = wx.getStorageSync("nowTable")
      const isCreate = !(data.id || this.data.editStatus)
      if (nowTable === 'student' && isCreate) {
        return await book('coursereserve', data)
      }
      if (data.id || this.data.editStatus) {
        return await update('coursereserve', data)
      }
      return await add('coursereserve', data)
    }

    //跨表计算判断
    var obj2;
    var ruleForm = obj
    obj2 = ruleForm
    this.data.refid == "" ? ruleForm['refid'] = getApp().globalData.detailId : ""
    ruleForm['userid'] = getApp().globalData.userInfo.id
    var userInfo = getApp().globalData.userInfo
    obj2 = wx.getStorageSync('crossObj');
    var table = wx.getStorageSync('crossTable');
    const targetreservecount_9 = ruleForm.reservecount ? ruleForm.reservecount : this.data.reservecount;
    if ((parseFloat(obj2[targetreservecount_9]) - parseFloat(ruleForm[targetreservecount_9])) < 0) {
      wx.showToast({
        icon: "none",
        title: '人数不足',
      });
      return;
    }


    const sessionReadArr = this.data.sessionReadArr
    const phonePattern = /^\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}$/;
    const mobilePattern = /^(?:\+?86)?1[3-9]\d{9}$/;
    const emailPattern = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
    const idPattern = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[1-2]\d|3[0-1])\d{3}[\dxX]$/;
    const urlPattern = /^(http|https):\/\/[\w\-]+(\.[\w\-]+)+[/#?]?.*$/;



















































    if (this.data.reservestatusList[this.data.reservestatusIndex] == undefined) {
      wx.showToast({
        icon: "none",
        title: `预约状态不能为空`,
      })
      return
    }


































    //更新跨表属性
    var crossuserid;
    var crossrefid;
    var crossoptnum;

    if (this.data.cross) {
      wx.setStorageSync('crossCleanType', true);
      var statusColumnName = wx.getStorageSync('statusColumnName');
      var statusColumnValue = wx.getStorageSync('statusColumnValue');
      if (statusColumnName != '') {
        obj2 = wx.getStorageSync('crossObj');
        if (!statusColumnName.startsWith("[")) {
          for (var o in obj2) {
            if (statusColumnName.includes(o)) {
              obj2[o] = statusColumnValue;
            }

          }
          var table = wx.getStorageSync('crossTable');
          obj2.reservecount
          obj2.reservecount - this.data.reservecount
          await update(table, obj2)
        } else {

          crossuserid = getApp().globalData.userInfo.id
          crossrefid = this.data.id
          crossoptnum = wx.getStorageSync('statusColumnName');
          crossoptnum = crossoptnum.replace(/\[/, "").replace(/\]/, "");
        }
      }
    }
    this.data.cross ? (crossrefid = obj2.id, crossuserid = getApp().globalData.userInfo.id) : ""
    ruleForm?.crossrefid == undefined ? (ruleForm["crossrefid"] = obj2.id, ruleForm["crossuserid"] = getApp().globalData.userInfo.id) : "";
    ruleForm?.shhf ? ruleForm.shhf = this.data.shhf : ''
    if (crossrefid && crossuserid) {
      ruleForm['crossuserid'] = obj2.id
      ruleForm['crossrefid'] = getApp().globalData.userInfo.id

      this.setData({
        ruleForm
      })
      let params = {
        page: 1,
        limit: 10,
        crossuserid: crossuserid,
        crossrefid: crossrefid,
      }
      const tips = wx.getStorageSync('tips')
      let corssRes = await list(`coursereserve`, params)
      crossoptnum = wx.getStorageSync('statusColumnName');
      crossoptnum = crossoptnum.match(/\d+/g);
      if (corssRes.data.total >= parseInt(crossoptnum)) {
        wx.showToast({
          icon: "none",
          title: tips,
        })
        wx.removeStorageSync('crossCleanType');
        return;
      } else {


        //跨表计算









        obj2 = wx.getStorageSync('crossObj');
        var table = wx.getStorageSync('crossTable');
        obj2.reservecount
        obj2.reservecount - this.data.reservecount
        await update(table, obj2)

        await saveReserve(ruleForm)
      }


    } else {


      //跨表计算
      obj2 = wx.getStorageSync('crossObj');
      var table = wx.getStorageSync('crossTable');
      obj2.reservecount
      obj2.reservecount - this.data.reservecount
      await update(table, obj2)
      if (ruleForm.id || this.data.editStatus) {
        this.data.editStatus ? ruleForm['id'] = getApp().globalData.detailId : ""
      }
      await saveReserve(ruleForm)
    }
    getApp().globalData.editorContent = ''
    wx.showToast({
      title: '提交成功',
      icon: "none"
    })
    const preId = getApp().globalData.detailId

    if (table) {
      let res = await detail(table, preId)
      if (res.code == 0) {
        getApp().globalData.detailList = res.data
      }

    }



    wx.navigateBack({
      delta: 1,
      complete: () => {
        // 触发事件通知，传递需要更新的数据
        const pages = getCurrentPages();
        if (pages.length >= 1) {
          const prePage = pages[pages.length - 1];
          prePage.onLoad(); //
        }
      }
    })













  },
  onHide() {

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

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  }
})
