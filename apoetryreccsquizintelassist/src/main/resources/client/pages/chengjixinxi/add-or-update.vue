<template>
<view class="content">
	<view :style='{"width":"100%","padding":"0","position":"relative","background":"#ffffff","height":"100%"}'>
		<form :style='{"width":"100%","padding":"24rpx","background":"#ffffff","display":"block","height":"auto"}' class="app-update-pv">
			<view :style='fieldStyle' class="select">
				<view :style='titleStyle' class="title">用户账号</view>
				<picker :style='{"width":"100%","flex":"1","height":"auto"}' @change="studentaccountChange" :value="studentaccountIndex" :range="studentaccountOptions">
					<view :style='pickerTextStyle' class="uni-input">{{studentaccountOptions[studentaccountIndex]}}</view>
				</picker>
			</view>
			<view :style='fieldStyle'><view :style='titleStyle' class="title">用户姓名</view><input :style='inputStyle' :disabled="ro.studentname" v-model="ruleForm.studentname" placeholder="用户姓名"></input></view>
			<view :style='fieldStyle'><view :style='titleStyle' class="title">练习成绩</view><input :style='inputStyle' :disabled="ro.kaoshichengji" v-model="ruleForm.kaoshichengji" placeholder="练习成绩"></input></view>
			<view :style='fieldStyle'><view :style='titleStyle' class="title">教师账号</view><input :style='inputStyle' :disabled="ro.teacheraccount" v-model="ruleForm.teacheraccount" placeholder="教师账号"></input></view>
			<view :style='fieldStyle'><view :style='titleStyle' class="title">教师姓名</view><input :style='inputStyle' :disabled="ro.teachername" v-model="ruleForm.teachername" placeholder="教师姓名"></input></view>
			<view :style='fieldStyle'><view :style='titleStyle' class="title">发布日期</view><input :style='inputStyle' v-model="ruleForm.releasetime" placeholder="发布日期" @tap="toggleTab('releasetime')"></input></view>
			<view :style='{"width":"100%","justifyContent":"center","display":"flex","height":"auto"}' class="btn">
				<button :style='{"border":"0px solid #eeeeee","padding":"0","margin":"0 4% 0 0","color":"#fff","borderRadius":"60rpx","background":"#d84fa9","width":"30%","lineHeight":"80rpx","fontSize":"28rpx","height":"80rpx"}' @tap="onSubmitTap" class="bg-red">提交</button>
			</view>
		</form>
		<w-picker mode="dateTime" step="1" :current="false" :hasSecond="false" @confirm="releasetimeConfirm" ref="releasetime" themeColor="#333333"></w-picker>
	</view>
</view>
</template>

<script>
import wPicker from "@/components/w-picker/w-picker.vue";

export default {
	components: { wPicker },
	data() {
		return {
			cross: '',
			ruleForm: { studentaccount: '', studentname: '', kaoshichengji: '', teacheraccount: '', teachername: '', releasetime: '' },
			studentaccountOptions: [],
			studentaccountIndex: 0,
			user: {},
			ro: { studentaccount: false, studentname: false, kaoshichengji: false, teacheraccount: false, teachername: false, releasetime: false },
			fieldStyle: {"padding":"4rpx 0","margin":"0 0 24rpx 0","borderColor":"#eeeeee","alignItems":"center","borderWidth":"0px 0px 2rpx 0px","background":"none","display":"flex","width":"100%","borderStyle":"solid","height":"auto"},
			titleStyle: {"width":"160rpx","padding":"0 20rpx 0 0","lineHeight":"80rpx","fontSize":"28rpx","color":"#333333","textAlign":"right"},
			inputStyle: {"border":"0px solid #eeeeee","padding":"0px 24rpx","margin":"0","color":"#666666","borderRadius":"8rpx","flex":"1","background":"#ffffff00","fontSize":"28rpx","lineHeight":"80rpx","height":"80rpx"},
			pickerTextStyle: {"width":"100%","lineHeight":"80rpx","fontSize":"28rpx","color":"#666666"}
		}
	},
	async onLoad(options) {
		this.ruleForm.releasetime = this.$utils.getCurDateTime();
		let table = uni.getStorageSync("nowTable");
		let res = await this.$api.session(table);
		this.user = res.data;
		this.ruleForm.teacheraccount = this.user.teacheraccount;
		this.ro.teacheraccount = true;
		this.ruleForm.teachername = this.user.teachername;
		this.ro.teachername = true;
		res = await this.$api.option(`student`, `studentaccount`, {});
		this.studentaccountOptions = res.data;
		this.ruleForm.userid = uni.getStorageSync("userid");
		if (options.id) {
			this.ruleForm.id = options.id;
			res = await this.$api.info(`transcript`, this.ruleForm.id);
			this.ruleForm = res.data;
		}
		this.cross = options.cross;
		if (options.cross) {
			const obj = uni.getStorageSync('crossObj');
			for (const o in obj) {
				if (Object.prototype.hasOwnProperty.call(this.ro, o)) {
					this.ruleForm[o] = obj[o];
					this.ro[o] = true;
				}
			}
		}
		this.$forceUpdate();
	},
	methods: {
		async studentaccountChange(e) {
			this.studentaccountIndex = e.target.value;
			this.ruleForm.studentaccount = this.studentaccountOptions[this.studentaccountIndex];
			const res = await this.$api.follow(`student`, `studentaccount`, { columnValue: this.ruleForm.studentaccount });
			if (res.data.studentname) this.ruleForm.studentname = res.data.studentname;
		},
		releasetimeConfirm(val) { this.ruleForm.releasetime = val.result; this.$forceUpdate(); },
		toggleTab(str) { this.$refs[str].show(); },
		async onSubmitTap() {
			let obj;
			if (!this.ruleForm.studentaccount) return this.$utils.msg(`用户账号不能为空`);
			if (!this.ruleForm.studentname) return this.$utils.msg(`用户姓名不能为空`);
			if (this.ruleForm.kaoshichengji && !this.$validate.isIntNumer(this.ruleForm.kaoshichengji)) return this.$utils.msg(`练习成绩应输入整数`);
			let crossuserid;
			let crossrefid;
			let crossoptnum;
			if (this.cross) {
				uni.setStorageSync('crossCleanType', true);
				const statusColumnName = uni.getStorageSync('statusColumnName');
				const statusColumnValue = uni.getStorageSync('statusColumnValue');
				if (statusColumnName != '') {
					if (!obj) obj = uni.getStorageSync('crossObj');
					if (!statusColumnName.startsWith("[")) {
						for (const o in obj) if (o == statusColumnName) obj[o] = statusColumnValue;
						const table = uni.getStorageSync('crossTable');
						await this.$api.update(`${table}`, obj);
					} else {
						crossuserid = Number(uni.getStorageSync('userid'));
						crossrefid = obj.id;
						crossoptnum = uni.getStorageSync('statusColumnName').replace(/\[/, "").replace(/\]/, "");
					}
				}
			}
			if (crossrefid && crossuserid) {
				this.ruleForm.crossuserid = crossuserid;
				this.ruleForm.crossrefid = crossrefid;
				const res = await this.$api.list(`transcript`, { page: 1, limit: 10, crossuserid, crossrefid });
				if (res.data.total >= crossoptnum) {
					this.$utils.msg(uni.getStorageSync('tips'));
					uni.removeStorageSync('crossCleanType');
					return false;
				}
			}
			if (this.ruleForm.id) await this.$api.update(`transcript`, this.ruleForm);
			else await this.$api.add(`transcript`, this.ruleForm);
			this.$utils.msgBack('提交成功');
		}
	}
}
</script>

<style lang="scss" scoped>
	.content { min-height: calc(100vh - 44px); box-sizing: border-box; }
</style>
