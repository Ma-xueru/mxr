<template>
<mescroll-uni @init="mescrollInit" :up="upOption" :down="downOption" @down="downCallback" @up="upCallback">
	<view class="content">
		<view :style='{"minHeight":"100vh","width":"100%","padding":"20rpx 0 0 0","position":"relative","background":"#ffffff","height":"100%"}'>
			<view :style='{"padding":"20rpx 0 0 0","alignItems":"center","flex":"1","display":"flex","width":"calc(98% - 120rpx)","position":"relative","justifyContent":"space-between"}'>
				<view :style='{"backgroundColor":"#f5f5f5","margin":"0 30rpx 0 30rpx","color":"#333333","alignItems":"center","borderRadius":"0px","flex":"1","display":"flex","lineHeight":"64rpx","fontSize":"24rpx","height":"64rpx"}'>
					<text class="iconfont icon-sousuo1" :style='{"margin":"0 16rpx 0 16rpx"}'></text>
					<input v-model="searchForm.studentaccount" type="text" placeholder="用户账号" :style='{"background":"transparent","height":"100%"}'></input>
				</view>
				<button @tap="search" :style='{"border":"0px","padding":"0 40rpx 0 40rpx","margin":"0 20rpx 0 0px","borderRadius":"0px","color":"#333","background":"#befaba","fontSize":"28rpx","lineHeight":"64rpx","height":"64rpx"}'>搜索</button>
			</view>
			<view :style='{"width":"100%","flexWrap":"wrap","background":"#ffffff","justifyContent":"space-between","display":"flex","height":"auto"}'>
				<view class="uni-product-list" :style='{"padding":"24rpx","margin":"40rpx 0 0 0","alignItems":"flex-start","flexWrap":"wrap","display":"flex","width":"100%","justifyContent":"space-between","height":"auto"}'>
					<view @tap="onDetailTap(product)" class="uni-product" :style='{"boxShadow":"0 0px 0px #eeeeee","margin":"0 0 40rpx 0","backgroundColor":"#ffffff","borderRadius":"8rpx","flexWrap":"wrap","textAlign":"center","display":"flex","width":"48%","justifyContent":"center","height":"auto"}' v-for="(product,index) in list" :key="index">
						<view class="uni-product-title" :style='{"padding":"0px 20rpx","whiteSpace":"nowrap","color":"#333333","textAlign":"left","overflow":"hidden","borderRadius":"8rpx","width":"100%","lineHeight":"72rpx","fontSize":"28rpx","textOverflow":"ellipsis","order":"2","height":"72rpx"}'>用户账号:{{product.studentaccount}}</view>
						<view class="uni-product-title" :style='{"padding":"0px 20rpx","whiteSpace":"nowrap","color":"#333333","textAlign":"left","overflow":"hidden","borderRadius":"8rpx","width":"100%","lineHeight":"72rpx","fontSize":"28rpx","textOverflow":"ellipsis","order":"2","height":"72rpx"}'>用户姓名:{{product.studentname}}</view>
						<view class="uni-product-title" :style='{"padding":"0px 20rpx","color":"#333333","textAlign":"left","width":"100%","lineHeight":"52rpx","fontSize":"24rpx","order":"2","height":"52rpx"}'>练习成绩:{{product.kaoshichengji}}</view>
						<view :style='{"padding":"0","margin":"8rpx 0 0 0","display":"flex","width":"100%","justifyContent":"space-between","height":"auto","order":"3"}'>
							<view :style='{"border":"0px solid #dcf8f5","padding":"0px 20rpx 20rpx 20rpx","borderRadius":"60rpx","display":"flex"}' v-if="(userid && isAuth('transcript','修改')) || (!userid && isAuthFront('transcript','修改'))" @click.stop="onUpdateTap(product.id)">
								<text :style='{"margin":"0 8rpx 0 0","fontSize":"28rpx","lineHeight":"1","color":"#5fb959","display":"inline-block"}' class="iconfont icon-xiugai3"></text>
								<text :style='{"fontSize":"28rpx","lineHeight":"1","color":"#5fb959","display":"inline-block"}'>修改</text>
							</view>
							<view :style='{"border":"0px solid #f9dede","padding":"0px 20rpx 20rpx 20rpx","borderRadius":"60rpx","display":"flex"}' v-if="(userid && isAuth('transcript','删除')) || (!userid && isAuthFront('transcript','删除'))" @click.stop="onDeleteTap(product.id)">
								<text :style='{"margin":"0 8rpx 0 0","fontSize":"28rpx","lineHeight":"1","color":"#cc0000","display":"inline-block"}' class="iconfont icon-shanchu4"></text>
								<text :style='{"fontSize":"28rpx","lineHeight":"1","color":"#cc0000","display":"inline-block"}'>删除</text>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>
		<button :style='{"margin":"40rpx 2% 0 0","borderColor":"#d84fa930","borderRadius":"0px","color":"#d84fa9","textAlign":"center","background":"none","borderWidth":"2rpx","width":"100rpx","lineHeight":"80rpx","fontSize":"28rpx","borderStyle":"solid","height":"80rpx"}' class="add-btn" @click="screenBoxShow=true">筛选</button>
		<button :style='{"border":"0","boxShadow":"0 0px 0px #cccccc","color":"#ffffff","bottom":"120rpx","right":"120rpx","outline":"none","borderRadius":"100%","background":"#3da742","width":"80rpx","lineHeight":"80rpx","fontSize":"26rpx","position":"fixed","height":"80rpx","zIndex":"999"}' v-if="userid && isAuth('transcript','新增')" class="add-btn" @click="onAddTap()">新增</button>
		<button :style='{"border":"0","boxShadow":"0 0px 0px #cccccc","color":"#ffffff","bottom":"120rpx","right":"120rpx","outline":"none","borderRadius":"100%","background":"#3da742","width":"80rpx","lineHeight":"80rpx","fontSize":"26rpx","position":"fixed","height":"80rpx","zIndex":"999"}' v-if="!userid && isAuthFront('transcript','新增')" class="add-btn" @click="onAddTap()">新增</button>
	</view>
	<view class="screenBoxBG" v-if="screenBoxShow" @click="screenBoxShow=false"></view>
	<view :style='{"width":"80%","padding":"40rpx 20rpx","background":"#fdfbd9"}' class="screenBox" :class="screenBoxShow?'screenBoxActive':''">
		<view :style='{"width":"100%","padding":"20rpx 20rpx 20rpx 0","alignItems":"center","justifyContent":"space-around","display":"flex"}'>
			<view :style='{"width":"18%","padding":"0 0 0 20rpx","fontSize":"24rpx"}'>用户姓名</view>
			<input :style='{"border":"2rpx solid #dddddd","padding":"0 20rpx","borderRadius":"8rpx","color":"#666666","background":"#ffffff","width":"80%","height":"60rpx"}' placeholder="请输入用户姓名" v-model="searchForm.studentname">
		</view>
		<view :style='{"width":"100%","padding":"0 24rpx 0 24rpx","margin":"110rpx 0 0 0","alignItems":"center","justifyContent":"flex-start","display":"flex"}'>
			<button :style='{"margin":"0 20rpx 0 0","color":"#888888","borderRadius":"8rpx","textAlign":"center","background":"#ffffff","width":"30%","lineHeight":"60rpx","height":"60rpx","order":"2"}' @click="screenReset">重置</button>
			<button :style='{"margin":"0 20rpx 0 0","color":"#333333","borderRadius":"8rpx","textAlign":"center","background":"#fff000","width":"30%","lineHeight":"60rpx","height":"60rpx","order":"1"}' @click="search">确定</button>
		</view>
	</view>
</mescroll-uni>
</template>

<script>
export default {
	data() {
		return {
			list: [],
			userid: '',
			mescroll: null,
			downOption: { auto: false },
			upOption: { noMoreSize: 5, textNoMore: '~ 没有更多了 ~' },
			hasNext: true,
			searchForm: {},
			screenBoxShow: false
		};
	},
	async onShow() {
		this.hasNext = true;
		if (this.mescroll) this.mescroll.resetUpScroll();
	},
	async onLoad(options) {
		this.userid = options.userid || "";
	},
	methods: {
		isAuth(tableName, key) { return this.$utils.isAuth(tableName, key); },
		isAuthFront(tableName, key) { return this.$utils.isAuthFront(tableName, key); },
		screenReset() { this.searchForm = {}; this.$forceUpdate(); },
		mescrollInit(mescroll) { this.mescroll = mescroll; },
		downCallback(mescroll) { this.hasNext = true; mescroll.resetUpScroll(); },
		async upCallback(mescroll) {
			let params = { page: mescroll.num, limit: mescroll.size };
			if (this.searchForm.studentaccount) params.studentaccount = '%' + this.searchForm.studentaccount + '%';
			if (this.searchForm.studentname) params.studentname = '%' + this.searchForm.studentname + '%';
			let res = this.userid ? await this.$api.page(`transcript`, params) : await this.$api.list(`transcript`, params);
			if (mescroll.num == 1) this.list = [];
			this.list = this.list.concat(res.data.list);
			if (res.data.list.length == 0) this.hasNext = false;
			mescroll.endSuccess(mescroll.size, this.hasNext);
		},
		onDetailTap(item) { uni.setStorageSync("useridTag", this.userid); this.$utils.jump(`./detail?id=${item.id}&userid=` + this.userid); },
		onUpdateTap(id) { uni.setStorageSync("useridTag", this.userid); this.$utils.jump(`./add-or-update?id=${id}`); },
		onAddTap() { uni.setStorageSync("useridTag", this.userid); this.$utils.jump(`./add-or-update`); },
		onDeleteTap(id) {
			const _this = this;
			uni.showModal({
				title: '提示',
				content: '是否确认删除',
				success: async function(res) {
					if (res.confirm) {
						await _this.$api.del('transcript', JSON.stringify([id]));
						_this.hasNext = true;
						_this.mescroll.resetUpScroll();
					}
				}
			});
		},
		async search() {
			this.mescroll.num = 1;
			let params = { page: this.mescroll.num, limit: this.mescroll.size };
			if (this.searchForm.studentaccount) params.studentaccount = this.searchForm.studentaccount;
			if (this.searchForm.studentname) params.studentname = '%' + this.searchForm.studentname + '%';
			let res = this.userid ? await this.$api.page(`transcript`, params) : await this.$api.list(`transcript`, params);
			if (this.mescroll.num == 1) this.list = [];
			this.list = this.list.concat(res.data.list);
			if (res.data.list.length == 0) this.hasNext = false;
			this.mescroll.endSuccess(this.mescroll.size, this.hasNext);
			this.screenBoxShow = false;
		}
	}
};
</script>

<style lang="scss" scoped>
	.content { min-height: calc(100vh - 44px); box-sizing: border-box; }
	.screenBoxBG { position: fixed; width: 100%; height: 100%; z-index: 665; top: 0; left: 0; background: rgba(0, 0, 0, .3); }
	.screenBox { width: 80%; position: fixed; height: 100%; right: 0; top: 0; z-index: 666; -webkit-transform: translate3d(100%, 0, 0); transform: translate3d(100%, 0, 0); transition: transform .3s; overflow-y: scroll; }
	.screenBoxActive { -webkit-transform: translate3d(0%, 0, 0); transform: translate3d(0%, 0, 0); }
</style>
