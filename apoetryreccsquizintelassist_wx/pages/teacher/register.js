
const dateUtils = require('../../utils/defautils')
const utils = require("../../utils/index.js")
const {
SendverificationCode,
register,
option,
    smscode,
follow
} = require('../../api/login.js')
const {
    levelOption,
    sheng,
} = require('../../api/index.js')
Page({
data: {
     teacheraccount:'',
     teacherpassword:'',
     teachername:'',
    zhaopian:'',
    tempPathzhaopian:'../../static/upload.png',
genderList:"男,女".split(','),
genderIndex:0,
     lianxidianhua:'',

    registerContainerClass: "",

},

async onLoad() {










},
onUnload() {
},
async onShow() {





    this.setData({
            genderList:  "男,女".split(',')
    })





},







async  register(){
if (this.data.teacheraccount == "") {
wx.showToast({
title: '请输入教师账号',
icon: "none"
})
return;
}
if (this.data.teacherpassword == "") {
wx.showToast({
title: '请输入教师密码',
icon: "none"
})
return;
}
if (this.data.teacherpassword2=="") {
wx.showToast({
title: '请输入确认教师密码',
icon: "none"
})
return;
}
if (this.data.teacherpassword !== this.data.teacherpassword2) {
wx.showToast({
title: '教师密码与确认教师密码不一致!!',
icon: "none"
})
return;
}
if (this.data.lianxidianhua == "") {
wx.showToast({
title: '请输入联系电话',
icon: "none"
})
return;
}
        if (!utils.validata("lianxidianhua",this.data.lianxidianhua)) {
        wx.showToast({
            title: '请输入有效联系电话',
            icon: 'none'
        })
        return;
    }











    const regex = new RegExp(wx.getStorageSync("baseURL"), "g");
  const resultObj={
        teacheraccount:this.data.teacheraccount,
        teacherpassword:this.data.teacherpassword,
        teachername:this.data.teachername,
        zhaopian:this.data.zhaopian.replace(regex, ""),
        gender: this.data.genderList?.length ? this.data.genderList[this.data.genderIndex] : "",
        lianxidianhua:this.data.lianxidianhua,
  }
    const name="teacheraccount"
    const password="teacherpassword"
    const res = await register("teacher", name, this.data[name],password , this.data[password], resultObj)
if (res.code == 0) {
wx.navigateTo({
url: '../login/login',
})
} else {
wx.showToast({
title: res.msg,
icon: "none"
})
}

}



});