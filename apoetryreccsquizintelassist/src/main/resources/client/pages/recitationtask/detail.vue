<template>
<view class="content">
	<view :style='{"padding":"24rpx","background":"#ffffff"}'>
		<view :style='{"padding":"28rpx","borderRadius":"24rpx","background":"linear-gradient(135deg,#fff8e8 0%,#eef7ff 100%)","margin":"0 0 24rpx 0"}'>
			<view :style='{"fontSize":"36rpx","fontWeight":"600","color":"#333","lineHeight":"1.5"}'>{{ detail.tasktitle || '背诵任务' }}</view>
			<view :style='{"padding":"8rpx 20rpx","margin":"20rpx 0 0 0","display":"inline-block","borderRadius":"40rpx","fontSize":"24rpx","color":"#fff","background":detail.completionstatus==='已完成'?'#5fb959':'#f6a623'}'>{{ detail.completionstatus || '待完成' }}</view>
		</view>
		<view class="item"><text class="label">任务要求：</text><text class="value multiline">{{ detail.taskcontent || '请按老师要求完成背诵任务。' }}</text></view>
		<view class="item"><text class="label">指定古诗：</text><text class="value multiline">{{ detail.coursetitles || '老师暂未指定具体古诗' }}</text></view>
		<view class="item"><text class="label">学生：</text><text class="value">{{ detail.studentname }}（{{ detail.studentaccount }}）</text></view>
		<view class="item"><text class="label">老师：</text><text class="value">{{ detail.teachername || '未分配' }}</text></view>
		<view class="item"><text class="label">截止日期：</text><text class="value">{{ formatValue(detail.deadline) }}</text></view>
		<view class="item"><text class="label">发布日期：</text><text class="value">{{ formatValue(detail.releasetime) }}</text></view>
		<view class="item"><text class="label">完成说明：</text><text class="value multiline">{{ detail.completionremark || '还没有填写完成说明' }}</text></view>
		<view class="item"><text class="label">背诵录音：</text><text class="value">{{ detail.recitationaudio ? getFileName(detail.recitationaudio) : '未上传录音' }}</text></view>
		<view class="item"><text class="label">完成时间：</text><text class="value">{{ formatValue(detail.completiontime, '未完成') }}</text></view>
		<view class="item"><text class="label">背诵得分：</text><text class="value">{{ detail.kaoshichengji || '待老师评分' }}</text></view>
		<view class="item"><text class="label">识别文本：</text><text class="value multiline">{{ detail.recognizedtext || '系统暂未识别出文本' }}</text></view>
		<view class="item"><text class="label">AI初评：</text><text class="value multiline">{{ aiCommentDisplay || '系统暂未生成初评' }}</text></view>
		<view class="item"><text class="label">教师评语：</text><text class="value multiline">{{ detail.teachercomment || '老师暂未填写评语' }}</text></view>
		<button v-if="canComplete" @tap="goComplete" :style='{"margin":"40rpx 0 0 0","border":"0","color":"#fff","borderRadius":"60rpx","background":"#d84fa9","lineHeight":"84rpx","fontSize":"30rpx","height":"84rpx"}'>完成这项任务</button>
	</view>
</view>
</template>

<script>
export default {
	data() { return { id: '', detail: {}, user: null } },
	computed: {
		canComplete() { return this.user && this.user.studentaccount && this.user.studentaccount === this.detail.studentaccount && this.detail.completionstatus !== '已完成'; },
			aiCommentDisplay() { try { const p = JSON.parse(this.detail.aiscorecomment); return p.overallComment || this.detail.aiscorecomment; } catch(e) { return this.detail.aiscorecomment; } }
	},
	async onLoad(options) { this.id = options.id; await this.init(); },
	async onShow() { const table = uni.getStorageSync('nowTable'); if (table) { const res = await this.$api.session(table); this.user = res.data; } },
	methods: {
		async init() { const res = await this.$api.info('recitationtask', this.id); this.detail = res.data; },
		formatValue(value, fallback = '未设置') { return value || fallback; },
		getFileName(filePath) {
			if (!filePath) return '';
			const parts = filePath.split('/');
			return parts[parts.length - 1];
		},
		goComplete() { this.$utils.jump(`./add-or-update?id=${this.id}`); }
	}
}
</script>

<style lang="scss" scoped>
.content { min-height: calc(100vh - 44px); background: #ffffff; box-sizing: border-box; }
.item { padding: 24rpx 0; border-bottom: 2rpx dotted #f0b8dd; }
.label { color: #5fb959; font-size: 28rpx; }
.value { color: #666; font-size: 28rpx; line-height: 1.8; }
.multiline { display: block; margin-top: 12rpx; }
</style>
