// pages/edit/edit.js
const {
detail,
option,
update,
add,
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
baseURL:'',
sessionReadArr:[],

detailList: null,
id: "",
cross:"",
ruleForm:{},
userid:getApp().globalData.userInfo.id,
userInfo:getApp().globalData.userInfo,
ro:{
},

studentaccountIndex: null,
studentaccountList: [],
studentname:"",
kaoshichengji:"",
teacheraccount:"",
teachername:"",
releasetime:"请选择时间",
showreleasetime:false,
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
if(options?.isAuth==true){
wx.setStorageSync('isAuth',true)
}
let nowTable = wx.getStorageSync("nowTable");
const res = await session(nowTable)
if(res.data.code==0){
getApp().globalData.userInfo=res?.data
userid = res?.data.id
this.setData({
userid
})

}

let baseURL =wx.getStorageSync('baseURL') + '/'
const id = getApp().globalData.detailId
this.setData({
refid:id,
baseURL
})
//人脸识别
const studentaccountres = await option('student/studentaccount')
studentaccountres.data.unshift('请选择用户账号')
this.setData({
studentaccountList: studentaccountres.data
})


let  ro=this.data.ro
if(options?.cross){
var obj = wx.getStorageSync('crossObj');
let refobjempty={}
for (var o in obj){

if(o=='studentaccount'){
refobjempty["studentaccount"]=obj[o]
ro.studentaccount = true;
continue;
}else{
}


if(o=='studentname'){
refobjempty["studentname"]=obj[o]
ro.studentname = true;
continue;
}else{
}


if(o=='kaoshichengji'){
refobjempty["kaoshichengji"]=obj[o]
ro.kaoshichengji = true;
continue;
}else{
}


if(o=='teacheraccount'){
refobjempty["teacheraccount"]=obj[o]
ro.teacheraccount = true;
continue;
}else{
}


if(o=='teachername'){
refobjempty["teachername"]=obj[o]
ro.teachername = true;
continue;
}else{
}


if(o=='releasetime'){
refobjempty["releasetime"]=obj[o]
ro.releasetime = true;
continue;
}else{
}

}

    let  statusColumnName=wx.getStorageSync('statusColumnName');
statusColumnName=statusColumnName.replace('[',"").replace(']',"");
this.setData({
ro,
cross:options?.cross,
statusColumnName
})
this.setData(refobjempty)
}

if(id){
// 如果上一级页面传递了id，获取改id数据信息
const   data=getApp().globalData.detailList
studentaccountres.data.map((item, index) => {
if (item == data.studentaccount) {
this.setData({
studentaccountIndex: index,
studentaccount: item
})
this.getfollow("student","studentaccount",item,"studentname")
}else if (this.data.studentaccountList.length == 1) {
this.setData({
studentaccountIndex: 0,
studentaccount: v
})
}
})




const url = wx.getStorageSync("baseURL") + "/"
const detailList = data
let  objtemp= {
detailList,
studentaccount: data.studentaccount,
kaoshichengji: data.kaoshichengji,
teacheraccount: data.teacheraccount,
teachername: data.teachername,
releasetime:utils.getCurrentDate("yMDhms"),
}
this.setData(objtemp);

//ss读取
let h = this.data
let c = this.data
this.setData({
});

}else {
this.setData({
})
}



// ss读取
let sessionReadArr=[]
let teacheraccount= getApp().globalData.userInfo.teacheraccount
ro.teacheraccount=true
this.setData({
teacheraccount,
})
sessionReadArr.push('teacheraccount')
let teachername= getApp().globalData.userInfo.teachername
ro.teachername=true
this.setData({
teachername,
})
sessionReadArr.push('teachername')

this.setData({
cross:options?.cross,
ro,
id,
sessionReadArr

})







this.setData({
releasetime:utils.getCurrentDate("yMDhms")
})

},
getUUID () {
return new Date().getTime();
},
onUnload: function () {
console.log('页面被卸载，执行销毁操作');
},
onShow() {

},





























async getfollow(refTable, refColumn, refColumnValue, suiColumnName) {
const {
data
} = await follow(`${refTable}/${refColumn}`, refColumnValue)
let tempObj = {};
tempObj[suiColumnName] = data[suiColumnName];
this.setData(tempObj);
},

async studentaccountChange(e) {
const selectedIndex = e.detail.value;
let  studentaccount=this.data.studentaccountList[selectedIndex]
this.setData({
studentaccountIndex: selectedIndex,
studentaccount
});
const {
data
} = await follow('student/studentaccount', studentaccount)
if(data.studentname){
this.setData({
studentname:data.studentname
})
}

},













































onreleasetimeTap(){
this.setData({
showreleasetime: true,
})

},
releasetimeTap(e) {
this.setData({
releasetime: e.detail.data
})

let c = this.data;
let h = this.data;
},





async submit() {
let that = this
var query = wx.createSelectorQuery();






if(this.data.releasetime?.includes("请选择") || this.data.releasetime==""){
wx.showToast({
icon: "none",
title: `发布日期不能为空`,
})
return
}







const baseURL = wx.getStorageSync('baseURL') + "/"
const regex = new RegExp(baseURL, "g");
const obj={
studentaccount: this.data. studentaccount,
studentname: this.data. studentname,
kaoshichengji: this.data. kaoshichengji,
teacheraccount: this.data. teacheraccount,
teachername: this.data. teachername,
releasetime: this.data. releasetime,
}
const detailId= getApp().globalData.detailId
const tableName= `transcript`

//跨表计算判断
var obj2;
var  ruleForm=obj
obj2 = ruleForm
this.data.refid==""? ruleForm['refid']= getApp().globalData.detailId:""
ruleForm['userid']=getApp().globalData.userInfo.id
var userInfo=getApp().globalData.userInfo


const sessionReadArr=this.data.sessionReadArr
const phonePattern = /^\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}$/;
const mobilePattern = /^(?:\+?86)?1[3-9]\d{9}$/;
const emailPattern = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
const idPattern = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[1-2]\d|3[0-1])\d{3}[\dxX]$/;
const urlPattern = /^(http|https):\/\/[\w\-]+(\.[\w\-]+)+[/#?]?.*$/;

if(!this.data.studentaccount ){
wx.showToast({
icon: "none",
title: `用户账号不能为空`,
})
return
}






if(this.data.studentaccountList[this.data.studentaccountIndex]==undefined ){
wx.showToast({
icon: "none",
title: `用户账号不能为空`,
})
return
}




if(!this.data.studentname ){
wx.showToast({
icon: "none",
title: `用户姓名不能为空`,
})
return
}

















































//更新跨表属性
var crossuserid;
var crossrefid;
var crossoptnum;

if(this.data.cross) {
wx.setStorageSync('crossCleanType', true);
var statusColumnName = wx.getStorageSync('statusColumnName');
var statusColumnValue = wx.getStorageSync('statusColumnValue');
if (statusColumnName != '') {
obj2 = wx.getStorageSync('crossObj');
if (!statusColumnName.startsWith("[")) {
for (var o in obj2) {
if (statusColumnName.includes(o)){
obj2[o] = statusColumnValue;
}

}
var table = wx.getStorageSync('crossTable');
await update(table, obj2)
} else {

crossuserid =getApp().globalData.userInfo.id
crossrefid =  this.data.id
crossoptnum = wx.getStorageSync('statusColumnName');
crossoptnum = crossoptnum.replace(/\[/, "").replace(/\]/, "");
}
}
}
this.data.cross ? (crossrefid = obj2.id, crossuserid =getApp().globalData.userInfo.id) : ""
ruleForm?.crossrefid==undefined? ( ruleForm["crossrefid"] = obj2.id, ruleForm["crossuserid"] =getApp().globalData.userInfo.id ): "";
ruleForm?.shhf?ruleForm.shhf=this.data.shhf:''
if(crossrefid && crossuserid) {
ruleForm['crossuserid'] =obj2.id
ruleForm['crossrefid'] =getApp().globalData.userInfo.id

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
let corssRes = await list(`transcript`, params)
crossoptnum = wx.getStorageSync('statusColumnName');
crossoptnum = crossoptnum.match(/\d+/g);
if (corssRes.data.total >= parseInt(crossoptnum)) {
wx.showToast({
icon: "none",
title: tips,
})
wx.removeStorageSync('crossCleanType');
return ;
}
else {


//跨表计算







if (ruleForm.id ) {
await update(`transcript`, ruleForm)
}
else {
await add(`transcript`, ruleForm)
}
}


}
else {


//跨表计算
if (ruleForm.id || this.data.editStatus) {
this.data.editStatus?ruleForm['id']= getApp().globalData.detailId:""
await update(`transcript`, ruleForm)
}
else {
await add(`transcript`, ruleForm)
}
}
getApp().globalData.editorContent=''
wx.showToast({
title: '提交成功',
icon: "none"
})
const preId = getApp().globalData.detailId

if(table){
let res = await detail(table, preId)
if(res.code==0){
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