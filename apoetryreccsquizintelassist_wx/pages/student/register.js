
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
     studentaccount:'',
     studentpassword:'',
     studentname:'',
    avatar:'',
    tempPathavatar:'../../static/upload.png',
     telephone:'',

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







studentaccountInput(e) {
this.setData({
    studentaccount: e.detail.value
})
},

studentpasswordInput(e) {
this.setData({
    studentpassword: e.detail.value
})
},

studentnameInput(e) {
this.setData({
    studentname: e.detail.value
})
},


avatarTap() {
wx.chooseImage({
count: 1,
sizeType: ['compressed'],
sourceType: ['album', 'camera'],
success: async (res) => {
const tempFilePaths = res.tempFilePaths;
let tempPathavatar= tempFilePaths[0]
// 本地临时图片的路径
this.setData({
    tempPathavatar,
})
// 上传网络图片
const  baseURL= wx.getStorageSync('baseURL')
    if(baseURL){
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
if (!utils.validata("telephone",this.data.telephone)) {
    wx.showToast({
        title: '请输入有效手机号码',
        icon: 'none'
    })
    return;
} else {
    const res = await SendverificationCode("student", 'sendsms','telephone',  this.data.telephone)
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

async  register(){
if (this.data.studentaccount == "") {
wx.showToast({
title: '请输入用户账号',
icon: "none"
})
return;
}
if (this.data.studentpassword == "") {
wx.showToast({
title: '请输入用户密码',
icon: "none"
})
return;
}
if (this.data.studentpassword2=="") {
wx.showToast({
title: '请输入确认用户密码',
icon: "none"
})
return;
}
if (this.data.studentpassword !== this.data.studentpassword2) {
wx.showToast({
title: '用户密码与确认用户密码不一致!!',
icon: "none"
})
return;
}
if (this.data.telephone == "") {
wx.showToast({
title: '请输入手机号码',
icon: "none"
})
return;
}
        if (!utils.validata("telephone",this.data.telephone)) {
        wx.showToast({
            title: '请输入有效手机号码',
            icon: 'none'
        })
        return;
    }










    const regex = new RegExp(wx.getStorageSync("baseURL"), "g");
  const resultObj={
        studentaccount:this.data.studentaccount,
        studentpassword:this.data.studentpassword,
        studentname:this.data.studentname,
        avatar:this.data.avatar.replace(regex, ""),
        telephone:this.data.telephone,
  }
    const name="studentaccount"
    const password="studentpassword"
    const res = await register("student", name, this.data[name],password , this.data[password], resultObj)
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