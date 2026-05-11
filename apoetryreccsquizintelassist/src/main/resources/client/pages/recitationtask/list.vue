<template>
<mescroll-uni @init="mescrollInit" :up="upOption" :down="downOption" @down="downCallback" @up="upCallback">
	<view class="content">
		<view :style='{"minHeight":"100vh","width":"100%","padding":"20rpx 0 0 0","position":"relative","background":"#ffffff","height":"100%"}'>
			<view :style='{"padding":"20rpx 0 0 0","alignItems":"center","flex":"1","display":"flex","width":"calc(98% - 120rpx)","position":"relative","justifyContent":"space-between"}'>
				<view :style='{"backgroundColor":"#f5f5f5","margin":"0 30rpx 0 30rpx","color":"#333333","alignItems":"center","borderRadius":"0px","flex":"1","display":"flex","lineHeight":"64rpx","fontSize":"24rpx","height":"64rpx"}'>
					<text class="iconfont icon-sousuo1" :style='{"margin":"0 16rpx 0 16rpx"}'></text>
					<input v-model="searchForm.tasktitle" type="text" placeholder="搜索背诵任务标题" :style='{"background":"transparent","height":"100%"}'></input>
				</view>
				<button @tap="search" :style='{"border":"0px","padding":"0 40rpx 0 40rpx","margin":"0 20rpx 0 0px","borderRadius":"0px","color":"#333","background":"#befaba","fontSize":"28rpx","lineHeight":"64rpx","height":"64rpx"}'>搜索</button>
			</view>
			<view :style='{"width":"100%","flexWrap":"wrap","background":"#ffffff","justifyContent":"space-between","display":"flex","height":"auto"}'>
				<view class="uni-product-list" :style='{"padding":"24rpx","margin":"40rpx 0 0 0","alignItems":"flex-start","flexWrap":"wrap","display":"flex","width":"100%","justifyContent":"space-between","height":"auto"}'>
					<view @tap="onDetailTap(product)" class="uni-product" :style='{"margin":"0 0 40rpx 0","backgroundColor":"#ffffff","borderRadius":"8rpx","flexWrap":"wrap","display":"flex","width":"48%","justifyContent":"center","height":"auto"}' v-for="(product,index) in list" :key="index">
						<view :style='{"padding":"16rpx 20rpx 0","color":"#333333","textAlign":"left","overflow":"hidden","width":"100%","lineHeight":"72rpx","fontSize":"30rpx","textOverflow":"ellipsis","height":"72rpx","fontWeight":"600"}'>{{product.tasktitle || '背诵任务'}}</view>
						<view :style='{"padding":"0 20rpx","color":"#666","textAlign":"left","overflow":"hidden","width":"100%","lineHeight":"44rpx","fontSize":"24rpx","minHeight":"88rpx"}'>{{product.taskcontent || '请按老师要求完成背诵任务'}}</view>
						<view :style='{"padding":"0 20rpx","color":"#666","textAlign":"left","overflow":"hidden","width":"100%","lineHeight":"44rpx","fontSize":"24rpx","minHeight":"44rpx"}'>指定古诗：{{product.coursetitles || '未指定'}}</view>
						<view :style='{"padding":"0 20rpx","color":"#333333","textAlign":"left","overflow":"hidden","width":"100%","lineHeight":"52rpx","fontSize":"24rpx","height":"52rpx"}'>完成状态：{{product.completionstatus || '待完成'}}</view>
						<view :style='{"padding":"0 20rpx","color":"#333333","textAlign":"left","overflow":"hidden","width":"100%","lineHeight":"52rpx","fontSize":"24rpx","height":"52rpx"}'>截止日期：{{formatDate(product.deadline)}}</view>
						<view :style='{"padding":"0","margin":"8rpx 0 0 0","display":"flex","width":"100%","justifyContent":"space-between","height":"auto"}'>
							<view :style='{"padding":"0 20rpx 20rpx","display":"flex"}' v-if="canComplete(product)" @click.stop="onUpdateTap(product.id)">
								<text :style='{"margin":"0 8rpx 0 0","fontSize":"28rpx","lineHeight":"1","color":"#5fb959","display":"inline-block"}' class="iconfont icon-xiugai3"></text>
								<text :style='{"fontSize":"28rpx","lineHeight":"1","color":"#5fb959","display":"inline-block"}'>完成任务</text>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>
		<button :style='{"margin":"40rpx 2% 0 0","borderColor":"#d84fa930","borderRadius":"0px","color":"#d84fa9","textAlign":"center","background":"none","borderWidth":"2rpx","width":"100rpx","lineHeight":"80rpx","fontSize":"28rpx","borderStyle":"solid","height":"80rpx"}' class="add-btn" @click="screenBoxShow=true">筛选</button>
		<button :style='{"border":"0","color":"#ffffff","bottom":"120rpx","right":"120rpx","borderRadius":"100%","background":"#3da742","width":"80rpx","lineHeight":"80rpx","fontSize":"26rpx","position":"fixed","height":"80rpx","zIndex":"999"}' v-if="tableName=='teacher'" class="add-btn" @click="onAddTap()">新增</button>
	</view>
	<view class="screenBoxBG" v-if="screenBoxShow" @click="screenBoxShow=false"></view>
	<view :style='{"width":"80%","padding":"40rpx 20rpx","background":"#fdfbd9"}' class="screenBox" :class="screenBoxShow?'screenBoxActive':''">
		<view :style='{"width":"100%","padding":"20rpx 20rpx 20rpx 0","alignItems":"center","justifyContent":"space-around","display":"flex"}'>
			<view :style='{"width":"18%","padding":"0 0 0 20rpx","fontSize":"24rpx"}'>状态</view>
			<input :style='{"border":"2rpx solid #dddddd","padding":"0 20rpx","borderRadius":"8rpx","color":"#666666","background":"#ffffff","width":"80%","height":"60rpx"}' placeholder="请输入完成状态，如待完成" v-model="searchForm.completionstatus">
		</view>
		<view :style='{"width":"100%","padding":"0 24rpx","margin":"110rpx 0 0 0","alignItems":"center","justifyContent":"flex-start","display":"flex"}'>
			<button :style='{"margin":"0 20rpx 0 0","color":"#888888","borderRadius":"8rpx","textAlign":"center","background":"#ffffff","width":"30%","lineHeight":"60rpx","height":"60rpx","order":"2"}' @click="screenReset">重置</button>
			<button :style='{"margin":"0 20rpx 0 0","color":"#333333","borderRadius":"8rpx","textAlign":"center","background":"#fff000","width":"30%","lineHeight":"60rpx","height":"60rpx","order":"1"}' @click="search">确定</button>
		</view>
	</view>
</mescroll-uni>
</template>

<script>
export default {
	data() {
		return { list: [], tableName: '', mescroll: null, downOption: { auto: false }, upOption: { noMoreSize: 5, textNoMore: '~ 没有更多了 ~' }, hasNext: true, searchForm: {}, screenBoxShow: false };
	},
	async onShow() {
		this.tableName = uni.getStorageSync("nowTable") || '';
		this.hasNext = true;
		if (this.mescroll) this.mescroll.resetUpScroll();
	},
	methods: {
		screenReset() { this.searchForm = {}; this.$forceUpdate(); },
		formatDate(value) { return value ? value.split(' ')[0] : '未设置'; },
		canComplete(product) { return this.tableName === 'student' && product.completionstatus !== '已完成'; },
		mescrollInit(mescroll) { this.mescroll = mescroll; },
		downCallback(mescroll) { this.hasNext = true; mescroll.resetUpScroll(); },
		async upCallback(mescroll) {
			let params = { page: mescroll.num, limit: mescroll.size };
			if (this.searchForm.tasktitle) params.tasktitle = '%' + this.searchForm.tasktitle + '%';
			if (this.searchForm.completionstatus) params.completionstatus = '%' + this.searchForm.completionstatus + '%';
			const res = await this.$api.page('recitationtask', params);
			if (mescroll.num == 1) this.list = [];
			this.list = this.list.concat(res.data.list);
			if (res.data.list.length == 0) this.hasNext = false;
			mescroll.endSuccess(mescroll.size, this.hasNext);
		},
		onDetailTap(item) { this.$utils.jump(`./detail?id=${item.id}`); },
		onUpdateTap(id) { this.$utils.jump(`./add-or-update?id=${id}`); },
		onAddTap() { this.$utils.jump(`./add-or-update`); },
		search() { this.mescroll.num = 1; this.hasNext = true; this.mescroll.resetUpScroll(); this.screenBoxShow = false; }
	}
};
</script>

<style lang="scss" scoped>
.content { min-height: calc(100vh - 44px); box-sizing: border-box; }
.screenBoxBG { position: fixed; width: 100%; height: 100%; z-index: 665; top: 0; left: 0; background: rgba(0, 0, 0, .3); }
.screenBox { width: 80%; position: fixed; height: 100%; right: 0; top: 0; z-index: 666; transform: translate3d(100%, 0, 0); transition: transform .3s; overflow-y: scroll; }
.screenBoxActive { transform: translate3d(0%, 0, 0); }
</style>
