<template>
<view>
<mescroll-uni @init="mescrollInit" :up="upOption" :down="downOption" @down="downCallback" @up="upCallback">
	<view class="content">
		<view class="container" :style='{"width":"100%","padding":"0px","position":"relative","background":"#ffffff","height":"auto"}'>
			<view :style='{"padding":"24rpx","flexWrap":"wrap","background":"#ffffff","display":"flex","width":"100%","justifyContent":"space-between","height":"auto"}' class="detail-content">
				<view :style='itemStyle'><view :style='labelStyle'>用户账号：</view><view :style='textStyle'>{{detail.studentaccount}}</view></view>
				<view :style='itemStyle'><view :style='labelStyle'>用户姓名：</view><view :style='textStyle'>{{detail.studentname}}</view></view>
				<view :style='itemStyle'><view :style='labelStyle'>练习成绩：</view><view :style='textStyle'>{{detail.kaoshichengji}}</view></view>
				<view :style='itemStyle'><view :style='labelStyle'>教师账号：</view><view :style='textStyle'>{{detail.teacheraccount}}</view></view>
				<view :style='itemStyle'><view :style='labelStyle'>教师姓名：</view><view :style='textStyle'>{{detail.teachername}}</view></view>
				<view :style='itemStyle'><view :style='labelStyle'>发布日期：</view><view :style='textStyle'>{{detail.releasetime}}</view></view>
			</view>
		</view>
	</view>
</mescroll-uni>
</view>
</template>

<script>
export default {
	data() {
		return {
			id: '',
			detail: {},
			mescroll: null,
			downOption: { auto: false },
			upOption: { noMoreSize: 3, textNoMore: '~ 没有更多了 ~' },
			hasNext: true,
			itemStyle: {"margin":"16rpx 0","borderColor":"#f0b8dd","flexWrap":"wrap","borderWidth":"0 0 2rpx 0","background":"#ffffff","display":"flex","width":"100%","borderStyle":"dotted","height":"auto"},
			labelStyle: {"width":"auto","padding":"0 20rpx 0 0","lineHeight":"60rpx","fontSize":"28rpx","color":"#5fb959","textAlign":"right"},
			textStyle: {"padding":"0","margin":"0","lineHeight":"60rpx","fontSize":"28rpx","color":"#666666","flex":"1"}
		}
	},
	async onLoad(options) {
		this.id = options.id;
		await this.init();
	},
	methods: {
		async init() {
			let res = await this.$api.info('transcript', this.id);
			this.detail = res.data;
		},
		mescrollInit(mescroll) { this.mescroll = mescroll; },
		downCallback(mescroll) { this.hasNext = true; mescroll.resetUpScroll(); },
		async upCallback(mescroll) { mescroll.endSuccess(mescroll.size, this.hasNext); }
	}
}
</script>

<style lang="scss" scoped>
	.content { min-height: calc(100vh - 44px); box-sizing: border-box; }
</style>
