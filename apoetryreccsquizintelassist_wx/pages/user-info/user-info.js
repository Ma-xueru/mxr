const dateUtils = require('../../utils/defautils')
const menuData=require('../../utils/menu.js')
const {
SendverificationCode,
option,
follow,wxbind,wxunbind
} = require('../../api/login.js')
const {
levelOption,
sheng,
update,
session
} = require('../../api/index.js')
const utils = require("../../utils/index.js")
Page({
data: {
code:'',
openid:'',
tableName:"",
ro:{
},
studentaccount:'',
studentpassword:'',
studentname:'',
avatar:'../../static/upload.png',
tempPathavatar:'../../static/upload.png',
genderList:"男,女".split(','),
genderIndex:0,

telephone:'',
grade:'',
classname:'',
medalcount:0,
teacheraccount:'',
teacherpassword:'',
teachername:'',
zhaopian:'../../static/upload.png',
tempPathzhaopian:'../../static/upload.png',
genderList:"男,女".split(','),
genderIndex:0,

lianxidianhua:'',
reservecount:'',

registerContainerClass: "",
role:""

},

async onLoad() {

wx.login({
provider: 'weixin',
success: function (res) {
let code = res.code;
if (code) {
this.setData({
code
});
}
}.bind(this)
});
let tableName = wx.getStorageSync("nowTable");
const menu = menuData.default.list()
const name = wx.getStorageSync("role")
let role
menu.map(obj => {
if (name == obj.roleName) {
role = obj.tableName
}
})
this.setData({
role,
tableName
})
const  userInfo  = getApp().globalData.userInfo
const   baseURL= wx.getStorageSync('baseURL')+"/"
    let userInfoObj={}
    userInfoObj["studentaccount"]= userInfo.studentaccount==null?"":userInfo.studentaccount
    userInfoObj["studentpassword"]= userInfo.studentpassword==null?"":userInfo.studentpassword
    userInfoObj["studentname"]= userInfo.studentname==null?"":userInfo.studentname
userInfo['avatar']=userInfo?.avatar?.replace('upload','file')
this.setData({
avatar:baseURL+userInfo.avatar,
tempPathavatar:baseURL+userInfo.avatar,
})
this.data.genderList?.map((v, index) => {
    if (v ==  userInfo.gender) {
        this.setData({
                genderIndex: index
        })
    }
})
this.setData({
telephone: userInfo.telephone,
grade: userInfo.grade || '',
classname: userInfo.classname || '',
medalcount: userInfo.medalcount || 0,
})
    userInfoObj["teacheraccount"]= userInfo.teacheraccount==null?"":userInfo.teacheraccount
    userInfoObj["teacherpassword"]= userInfo.teacherpassword==null?"":userInfo.teacherpassword
    userInfoObj["teachername"]= userInfo.teachername==null?"":userInfo.teachername
userInfo['zhaopian']=userInfo?.zhaopian?.replace('upload','file')
this.setData({
zhaopian:baseURL+userInfo.zhaopian,
tempPathzhaopian:baseURL+userInfo.zhaopian,
})
this.data.genderList?.map((v, index) => {
    if (v ==  userInfo.gender) {
        this.setData({
                genderIndex: index
        })
    }
})
this.setData({
lianxidianhua: userInfo.lianxidianhua,
})
    userInfoObj["reservecount"]= userInfo.reservecount==null?"":userInfo.reservecount
    this.setData(
        userInfoObj
    )
//ss读取

},
async onShow() {
},
avatarTap() {
wx.chooseImage({
count: 1,
sizeType: ['compressed'],
sourceType: ['album', 'camera'],
success: async (res) => {
const tempFilePaths = res.tempFilePaths;
// 本地临时图片的路径
this.setData({
tempPathavatar: tempFilePaths[0]
})
// 上传网络图片
const  baseURL=  wx.getStorageSync("baseURL")
wx.uploadFile({
url: `${baseURL}/file/upload`,
filePath: res.tempFilePaths[0],
name: 'file',
header: {
'Token': wx.getStorageSync('token')
},
success: (uploadFileRes) => {
let result = JSON.parse(uploadFileRes.data);
// result.file是上传成功为网络图片的名称
if (result.code == 0) {
this.setData({
        avatar: 'file/' + result.file
})
} else {
wx.showToast({
    title: result.msg,
    icon: 'none',
    duration: 2000
});
}
}
})



}
})
},
genderChange(e) {
const selectedIndex = e.detail.value;
this.setData({
genderIndex: selectedIndex,
});
},
async sendCodeHandler() {
if (!this.data.canSendCode) {
return;
}
if (this.data.telephone == "") {
wx.showToast({
title: '请输入手机号码',
icon: 'none'
})
return;
} else {
if (validatePhoneNumber(this.data.telephone) == false) {
wx.showToast({
title: '请输入有效手机号码',
icon: 'none'
})
return;
} else {
const res = await SendverificationCode("${tableName}", 'sendsms','telephone',  this.data.telephone)
this.setData({
smscode: res.data
})
}
}
this.setData({
canSendCode: false,
});
let time = this.data.countdown;
let timer = setInterval(() => {
time--;
this.setData({
countdown: time, // 更新倒计时的时间
});
if (time <= 0) {
clearInterval(timer); // 倒计时结束，清除定时器
this.setData({
buttonText: '发送验证码',
canSendCode: true,
countdown: 60
});
}
}, 1000);

},
zhaopianTap() {
wx.chooseImage({
count: 1,
sizeType: ['compressed'],
sourceType: ['album', 'camera'],
success: async (res) => {
const tempFilePaths = res.tempFilePaths;
// 本地临时图片的路径
this.setData({
tempPathzhaopian: tempFilePaths[0]
})
// 上传网络图片
const  baseURL=  wx.getStorageSync("baseURL")
wx.uploadFile({
url: `${baseURL}/file/upload`,
filePath: res.tempFilePaths[0],
name: 'file',
header: {
'Token': wx.getStorageSync('token')
},
success: (uploadFileRes) => {
let result = JSON.parse(uploadFileRes.data);
// result.file是上传成功为网络图片的名称
if (result.code == 0) {
this.setData({
        zhaopian: 'file/' + result.file
})
} else {
wx.showToast({
    title: result.msg,
    icon: 'none',
    duration: 2000
});
}
}
})



}
})
},
genderChange(e) {
const selectedIndex = e.detail.value;
this.setData({
genderIndex: selectedIndex,
});
},
async sendCodeHandler() {
if (!this.data.canSendCode) {
return;
}
if (this.data.lianxidianhua == "") {
wx.showToast({
title: '请输入联系电话',
icon: 'none'
})
return;
} else {
if (validatePhoneNumber(this.data.lianxidianhua) == false) {
wx.showToast({
title: '请输入有效联系电话',
icon: 'none'
})
return;
} else {
const res = await SendverificationCode("${tableName}", 'sendsms','lianxidianhua',  this.data.lianxidianhua)
this.setData({
smscode: res.data
})
}
}
this.setData({
canSendCode: false,
});
let time = this.data.countdown;
let timer = setInterval(() => {
time--;
this.setData({
countdown: time, // 更新倒计时的时间
});
if (time <= 0) {
clearInterval(timer); // 倒计时结束，清除定时器
this.setData({
buttonText: '发送验证码',
canSendCode: true,
countdown: 60
});
}
}, 1000);

},
quitTap(){
let saveBaseURL = wx.getStorageSync('baseURL')

wx.clearStorageSync();
wx.setStorageSync('baseURL', saveBaseURL)
wx.reLaunch({
url: "/pages/login/login"
});
},
async  saveTap(){


const baseURL = wx.getStorageSync('baseURL') + "/"
const regex = new RegExp(baseURL, "g");
const resultObj={
studentaccount:this.data.studentaccount,
studentpassword:this.data.studentpassword,
studentname:this.data.studentname,
avatar:this.data.avatar.replace(regex, ""),
gender:this.data.genderIndex ? this.data.genderList[this.data.genderIndex] : "",
telephone:this.data.telephone,
grade:this.data.grade,
classname:this.data.classname,
medalcount:this.data.medalcount,
teacheraccount:this.data.teacheraccount,
teacherpassword:this.data.teacherpassword,
teachername:this.data.teachername,
zhaopian:this.data.zhaopian.replace(regex, ""),
gender:this.data.genderIndex ? this.data.genderList[this.data.genderIndex] : "",
lianxidianhua:this.data.lianxidianhua,
reservecount:this.data.reservecount,
id:getApp().globalData.userInfo.id

}
const res = await update(this.data.role, resultObj)
if (res.code == 0) {
const userInfoRes = await session(this.data.role)
if (userInfoRes.code == 0) {
getApp().globalData.userInfo = userInfoRes.data
wx.reLaunch({
url: '/pages/center/center',
})
}
} else {
wx.showToast({
title: res.msg,
icon: "none"
})
}

},
async   getSession(){
let res = await session(this.data.role)
getApp().globalData.userInfo=res.data
this.setData({
openid:res.data.openid
})
}
});
