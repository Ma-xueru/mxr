<template>
<view class="content">
	<view :style='{"padding":"24rpx","background":"#ffffff"}'>
		<view :style='{"padding":"24rpx","borderRadius":"24rpx","background":"#fff7fb","margin":"0 0 24rpx 0"}'>
			<view :style='{"fontSize":"34rpx","fontWeight":"600","color":"#333"}'>{{ isTeacher ? '布置背诵任务' : '完成背诵任务' }}</view>
			<view :style='{"fontSize":"24rpx","color":"#666","margin":"12rpx 0 0 0"}'>{{ isTeacher ? '填写任务内容后发布给学生。' : '提交录音后，系统会自动转文字并给出初步评分。' }}</view>
		</view>
		<view class="field" v-if="isTeacher"><view class="title">学生账号</view><picker @change="studentaccountChange" :value="studentaccountIndex" :range="studentaccountOptions"><view class="picker">{{ studentaccountOptions[studentaccountIndex] || '请选择学生账号' }}</view></picker></view>
		<view class="field"><view class="title">学生姓名</view><input class="input" :disabled="true" v-model="ruleForm.studentname" placeholder="学生姓名"></input></view>
		<view class="field" v-if="isTeacher"><view class="title">任务标题</view><input class="input" v-model="ruleForm.tasktitle" placeholder="例如：背诵《静夜思》"></input></view>
		<view class="field" v-if="isTeacher"><view class="title">任务要求</view><textarea class="textarea" v-model="ruleForm.taskcontent" placeholder="请输入老师布置的背诵要求"></textarea></view>
		<view class="field" v-if="isTeacher"><view class="title">截止日期</view><input class="input" v-model="ruleForm.deadline" placeholder="请选择截止日期" @tap="toggleTab('deadline')"></input></view>
		<view class="field" v-if="!isTeacher"><view class="title">任务标题</view><input class="input" :disabled="true" v-model="ruleForm.tasktitle"></input></view>
		<view class="field" v-if="!isTeacher"><view class="title">任务要求</view><textarea class="textarea" :disabled="true" v-model="ruleForm.taskcontent"></textarea></view>
		<view class="field" v-if="!isTeacher"><view class="title">完成状态</view><input class="input" :disabled="true" v-model="ruleForm.completionstatus"></input></view>
		<view class="field" v-if="!isTeacher"><view class="title">完成说明</view><textarea class="textarea" v-model="ruleForm.completionremark" placeholder="说说你是怎么完成背诵的"></textarea></view>
		<view class="field" v-if="!isTeacher">
			<view class="title">背诵录音</view>
			<view class="record_card">
				<view class="record_status">{{ recordStatusText }}</view>
				<view class="record_actions">
					<button class="mini_btn start" @tap="startRecord" :disabled="recording">开始录音</button>
					<button class="mini_btn stop" @tap="stopRecord" :disabled="!recording">停止录音</button>
					<button class="mini_btn upload" @tap="uploadRecord" :disabled="!tempAudioPath || audioUploading">上传录音</button>
				</view>
				<view class="record_tip" v-if="tempAudioPath">本地录音：{{ getFileName(tempAudioPath) }}</view>
				<view class="record_tip" v-if="ruleForm.recitationaudio">已上传：{{ getFileName(ruleForm.recitationaudio) }}</view>
			</view>
		</view>
		<view class="field" v-if="isTeacher"><view class="title">背诵得分</view><input class="input" v-model="ruleForm.kaoshichengji" placeholder="选填，整数"></input></view>
		<view class="field"><view class="title">教师姓名</view><input class="input" :disabled="true" v-model="ruleForm.teachername"></input></view>
		<view class="field" v-if="!isTeacher && ruleForm.recognizedtext"><view class="title">识别文本</view><textarea class="textarea" :disabled="true" v-model="ruleForm.recognizedtext"></textarea></view>
		<view class="field" v-if="!isTeacher && ruleForm.aiscorecomment"><view class="title">AI初评</view><textarea class="textarea" :disabled="true" v-model="ruleForm.aiscorecomment"></textarea></view>
		<button @tap="onSubmitTap" :style='{"margin":"40rpx 0 0 0","border":"0","color":"#fff","borderRadius":"60rpx","background":"#d84fa9","lineHeight":"84rpx","fontSize":"30rpx","height":"84rpx"}'>提交</button>
	</view>
	<w-picker mode="dateTime" step="1" :current="false" :hasSecond="false" @confirm="deadlineConfirm" ref="deadline" themeColor="#333333"></w-picker>
</view>
</template>

<script>
import wPicker from "@/components/w-picker/w-picker.vue";
import base from "@/api/base";
export default {
	components: { wPicker },
	data() {
		return {
			ruleForm: { studentaccount: '', studentname: '', tasktitle: '', taskcontent: '', deadline: '', completionstatus: '待完成', completionremark: '', recitationaudio: '', completiontime: '', kaoshichengji: '', recognizedtext: '', aiscorecomment: '', teachercomment: '', teacheraccount: '', teachername: '', releasetime: '' },
			studentaccountOptions: [],
			studentaccountIndex: 0,
			user: {},
			tableName: 'student',
			recordManager: null,
			recording: false,
			tempAudioPath: '',
			audioUploading: false
		}
	},
	computed: {
		isTeacher() { return this.tableName === 'teacher'; },
		recordStatusText() {
			if (this.recording) return '正在录音中...';
			if (this.audioUploading) return '录音上传中...';
			if (this.ruleForm.recitationaudio) return '录音已上传，可直接提交';
			if (this.tempAudioPath) return '录音已保存，请点击上传录音';
			return '请先录制一段背诵音频';
		}
	},
	async onLoad(options) {
		this.tableName = uni.getStorageSync('nowTable') || 'student';
		this.recordManager = uni.getRecorderManager ? uni.getRecorderManager() : null;
		this.bindRecorderEvents();
		this.ruleForm.releasetime = this.$utils.getCurDateTime();
		const session = await this.$api.session(this.tableName);
		this.user = session.data;
		this.ruleForm.teacheraccount = this.user.teacheraccount || '';
		this.ruleForm.teachername = this.user.teachername || '';
		if (this.isTeacher) {
			const res = await this.$api.option('student', 'studentaccount', {});
			this.studentaccountOptions = res.data || [];
		} else {
			this.ruleForm.studentaccount = this.user.studentaccount;
			this.ruleForm.studentname = this.user.studentname;
		}
		if (options.id) {
			const res = await this.$api.info('recitationtask', options.id);
			this.ruleForm = res.data;
		}
	},
	methods: {
		bindRecorderEvents() {
			if (!this.recordManager) return;
			this.recordManager.onStop((res) => {
				this.recording = false;
				this.tempAudioPath = res.tempFilePath || '';
				this.$utils.msg('录音已完成，请上传后再提交', 1500, false, 'none');
			});
			this.recordManager.onError(() => {
				this.recording = false;
				this.$utils.msg('录音失败，请检查麦克风权限');
			});
		},
		getFileName(filePath) {
			if (!filePath) return '';
			const parts = filePath.split('/');
			return parts[parts.length - 1];
		},
		startRecord() {
			if (!this.recordManager) {
				this.$utils.msg('当前环境暂不支持录音');
				return;
			}
			this.tempAudioPath = '';
			this.recordManager.start({
				duration: 180000,
				sampleRate: 16000,
				numberOfChannels: 1,
				encodeBitRate: 96000,
				format: 'wav'
			});
			this.recording = true;
		},
		stopRecord() {
			if (this.recordManager && this.recording) {
				this.recordManager.stop();
			}
		},
		async uploadRecord() {
			if (!this.tempAudioPath) {
				return this.$utils.msg('请先完成录音');
			}
			this.audioUploading = true;
			try {
				const uploadRes = await new Promise((resolve, reject) => {
					uni.uploadFile({
						url: `${base.url}file/upload`,
						filePath: this.tempAudioPath,
						name: 'file',
						header: {
							Token: uni.getStorageSync("token")
						},
						success: resolve,
						fail: reject
					});
				});
				const result = JSON.parse(uploadRes.data || '{}');
				if (result.code === 0) {
					this.ruleForm.recitationaudio = result.file;
					this.$utils.msg('录音上传成功');
				} else {
					this.$utils.msg(result.msg || '录音上传失败');
				}
			} catch (e) {
				this.$utils.msg('录音上传失败');
			}
			this.audioUploading = false;
		},
		async studentaccountChange(e) { this.studentaccountIndex = e.target.value; this.ruleForm.studentaccount = this.studentaccountOptions[this.studentaccountIndex]; const res = await this.$api.follow('student', 'studentaccount', { columnValue: this.ruleForm.studentaccount }); this.ruleForm.studentname = res.data.studentname || ''; },
		deadlineConfirm(val) { this.ruleForm.deadline = val.result; this.$forceUpdate(); },
		toggleTab(name) { this.$refs[name].show(); },
		async onSubmitTap() {
			if (!this.ruleForm.studentaccount) return this.$utils.msg('请选择学生账号');
			if (!this.ruleForm.studentname) return this.$utils.msg('学生姓名不能为空');
			if (this.isTeacher) {
				if (!this.ruleForm.tasktitle) return this.$utils.msg('任务标题不能为空');
				if (!this.ruleForm.taskcontent) return this.$utils.msg('任务要求不能为空');
				if (!this.ruleForm.deadline) return this.$utils.msg('请选择截止日期');
			} else {
				this.ruleForm.completionstatus = '已完成';
				this.ruleForm.completiontime = this.$utils.getCurDateTime();
				if (!this.ruleForm.completionremark) return this.$utils.msg('请填写完成说明');
				if (!this.ruleForm.recitationaudio) return this.$utils.msg('请先上传背诵录音');
			}
			if (this.ruleForm.kaoshichengji && !this.$validate.isIntNumer(this.ruleForm.kaoshichengji)) return this.$utils.msg('背诵得分应输入整数');
			if (this.ruleForm.id) await this.$api.update('recitationtask', this.ruleForm); else await this.$api.add('recitationtask', this.ruleForm);
			this.$utils.msgBack('提交成功');
		}
	},
	onUnload() {
		if (this.recordManager && this.recording) {
			this.recordManager.stop();
		}
	}
}
</script>

<style lang="scss" scoped>
.content { min-height: calc(100vh - 44px); box-sizing: border-box; background: #ffffff; }
.field { margin: 0 0 24rpx 0; }
.title { color: #333; font-size: 28rpx; margin: 0 0 12rpx 0; }
.input,.picker,.textarea { width: 100%; box-sizing: border-box; border: 2rpx solid #eeeeee; border-radius: 16rpx; padding: 0 24rpx; background: #fff; color: #666; font-size: 28rpx; }
.input,.picker { line-height: 88rpx; height: 88rpx; }
.textarea { padding-top: 20rpx; min-height: 220rpx; }
.record_card { border: 2rpx solid #eeeeee; border-radius: 16rpx; background: #fff; padding: 24rpx; }
.record_status { color: #333; font-size: 28rpx; font-weight: 600; }
.record_actions { display: flex; gap: 16rpx; margin: 20rpx 0 0; flex-wrap: wrap; }
.mini_btn { margin: 0; border-radius: 40rpx; line-height: 72rpx; height: 72rpx; font-size: 26rpx; color: #fff; padding: 0 26rpx; }
.mini_btn::after { border: 0; }
.mini_btn.start { background: #5fb959; }
.mini_btn.stop { background: #f6a623; }
.mini_btn.upload { background: #4c8bf5; }
.record_tip { margin-top: 16rpx; color: #666; font-size: 24rpx; word-break: break-all; }
</style>
