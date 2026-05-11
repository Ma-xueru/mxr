<template>
	<div>
		<el-dialog v-model="formVisible" :title="formTitle" width="80%" destroy-on-close :fullscreen='false'>
			<el-form class="formModel_form" ref="formRef" :model="form"
				label-width="$template2.back.add.form.base.labelWidth" :rules="rules">
				<el-row>
					<el-col :span="12">
						<el-form-item label="古诗词号" prop="courseno">
							<el-input class="list_inp" v-model="form.courseno" placeholder="请输入古诗词号" type="text"
								:readonly="!isAdd || disabledForm.courseno ? true : false" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="古诗词标题" prop="coursetitle">
							<el-input class="list_inp" v-model="form.coursetitle" placeholder="请输入古诗词标题" type="text"
								:readonly="!isAdd || disabledForm.coursetitle ? true : false" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="古诗词类型" prop="coursetype">
							<el-input class="list_inp" v-model="form.coursetype" placeholder="请输入古诗词类型" type="text"
								:readonly="!isAdd || disabledForm.coursetype ? true : false" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="适用年级" prop="grade">
							<el-select class="list_sel" v-model="form.grade" placeholder="请选择适用年级"
								:disabled="!isAdd || disabledForm.grade ? true : false">
								<el-option v-for="item in gradeLists" :key="item" :label="item" :value="item"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="点击数" prop="clicknum">
							<el-input class="list_inp" v-model="form.clicknum" placeholder="请输入点击数" type="number"
								:readonly="!isAdd || disabledForm.clicknum ? true : false" />
						</el-form-item>
					</el-col>
					<el-col :span="24">
						<el-form-item prop="picture" label="古诗词封面">
							<uploads :disabled="!isAdd || disabledForm.picture ? true : false" action="file/upload"
								tip="请上传图片" :limit="3" style="width: 100%;text-align: left;"
								:fileUrls="form.picture ? form.picture : ''" @change="pictureUploadSuccess">
							</uploads>
						</el-form-item>
					</el-col>
					<el-col :span="24">
						<el-form-item label="古诗词简介" prop="intro">
							<el-input class="textarea" v-model="form.intro" placeholder="请输入古诗词简介" type="textarea"
								:rows="3" :readonly="!isAdd || disabledForm.intro ? true : false" />
						</el-form-item>
					</el-col>
					<el-col :span="24">
						<el-form-item prop="video" label="视频">
							<div class="video_field">
								<uploads
									:disabled="!isAdd || disabledForm.video ? true : false"
									action="file/upload"
									tip="请上传视频"
									:limit="1"
									class="video_upload"
									:fileUrls="form.video ? form.video : ''"
									@change="videoUploadSuccess">
								</uploads>
								<div class="video_preview_box">
									<video
										v-if="form.video"
										class="video_preview"
										:src="$config.url + form.video"
										controls
										preload="metadata">
										无视频
									</video>
									<div v-else class="video_preview_empty">上传后在这里预览</div>
								</div>
							</div>
						</el-form-item>
					</el-col>
					<el-col :span="24">
						<el-form-item label="古诗词详情" prop="content">
							<editor :value="form.content" placeholder="请输入古诗词详情"
								:readonly="!isAdd || disabledForm.content ? true : false" class="list_editor"
								@change="(e) => editorChange(e, 'content')"></editor>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="最后更新时间" prop="addtime">
							<el-date-picker class="list_date" v-model="form.addtime" format="YYYY 年 MM 月 DD 日 HH:mm:ss"
								value-format="YYYY-MM-DD HH:mm:ss" type="datetime"
								:readonly="!isAdd || disabledForm.addtime ? true : false" placeholder="请选择最后更新时间" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="最后点击时间" prop="clicktime">
							<el-date-picker class="list_date" v-model="form.clicktime"
								format="YYYY 年 MM 月 DD 日 HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" type="datetime"
								:readonly="!isAdd || disabledForm.clicktime ? true : false" placeholder="请选择最后点击时间" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="点赞数" prop="thumbsupnum">
							<el-input class="list_inp" v-model="form.thumbsupnum" placeholder="请输入点赞数" type="number"
								:readonly="!isAdd || disabledForm.thumbsupnum ? true : false" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="点踩数" prop="crazilynum">
							<el-input class="list_inp" v-model="form.crazilynum" placeholder="请输入点踩数" type="number"
								:readonly="!isAdd || disabledForm.crazilynum ? true : false" />
						</el-form-item>
					</el-col>
				</el-row>
			</el-form>
			<template #footer v-if="isAdd || type == 'logistics' || type == 'reply'">
				<span class="formModel_btn_box">
					<el-button class="formModel_cancel" @click="closeClick">取消</el-button>
					<el-button class="formModel_confirm" type="primary" @click="save">提交</el-button>
				</span>
			</template>
		</el-dialog>
	</div>
</template>
<script setup>
import { reactive, ref, getCurrentInstance, nextTick, computed, defineEmits } from 'vue'
const context = getCurrentInstance()?.appContext.config.globalProperties;
const emit = defineEmits(['formModelChange'])

// 基础信息 - 改为古诗词
const tableName = 'course'
const formName = '古诗词'

// 表单数据
const form = ref({})
const disabledForm = ref({
	courseno: false,
	coursetitle: false,
	coursetype: false,
	grade: false,
	picture: false,
	intro: false,
	content: false,
	video: false,
	thumbsupnum: false,
	crazilynum: false,
	clicknum: false,
	addtime: false,
	clicktime: false,
})
const formVisible = ref(false)
const isAdd = ref(false)
const formTitle = ref('')

// 表单验证
const validateIntNumber = (rule, value, callback) => {
	if (!value) {
		callback();
	} else if (!context?.$toolUtil.isIntNumer(value)) {
		callback(new Error("请输入整数"));
	} else {
		callback();
	}
}
const rules = ref({
	courseno: [
		{ required: true, message: '请输入古诗词号', trigger: 'blur' }
	],
	coursetitle: [
		{ required: true, message: '请输入古诗词标题', trigger: 'blur' }
	],
	coursetype: [
		{ required: true, message: '请输入古诗词类型', trigger: 'blur' }
	],
	grade: [
		{ required: true, message: '请选择适用年级', trigger: 'change' }
	],
	clicknum: [
		{ validator: validateIntNumber, trigger: 'blur' }
	],
	thumbsupnum: [
		{ validator: validateIntNumber, trigger: 'blur' }
	],
	crazilynum: [
		{ validator: validateIntNumber, trigger: 'blur' }
	],
})

const formRef = ref(null)
const id = ref(0)
const type = ref('')
const gradeLists = ref("一年级,二年级,三年级,四年级,五年级,六年级".split(','))

// 图片上传回调
const pictureUploadSuccess = (e) => {
	form.value.picture = e
}

// 视频上传回调
const videoUploadSuccess = (e) => {
	form.value.video = e
}

// 重置表单
const resetForm = () => {
	form.value = {
		courseno: '',
		coursetitle: '',
		coursetype: '',
		grade: '一年级',
		picture: '',
		intro: '',
		content: '',
		video: '',
		thumbsupnum: 0,
		crazilynum: 0,
		clicknum: 0,
		addtime: new Date(),
		clicktime: null,
	}
}

// 获取详情
const getInfo = () => {
	context?.$http({
		url: `${tableName}/info/${id.value}`,
		method: 'get'
	}).then(res => {
		let reg = new RegExp('../../../file', 'g')
		res.data.data.content = res.data.data.content
			? res.data.data.content.replace(reg, '../../../apoetryreccsquizintelassist/file')
			: '';
		form.value = res.data.data
		formVisible.value = true
	})
}

const crossRow = ref('')
const crossTable = ref('')
const crossTips = ref('')
const crossColumnName = ref('')
const crossColumnValue = ref('')

// 初始化表单
const init = (formId = null, formType = 'add', formNames = '', row = null, table = null, statusColumnName = null, tips = null, statusColumnValue = null) => {
	resetForm()
	if (formId) {
		id.value = formId
		type.value = formType
	}
	if (formType == 'add') {
		isAdd.value = true
		formTitle.value = '新增' + formName
		formVisible.value = true
	} else if (formType == 'info') {
		isAdd.value = false
		formTitle.value = '查看' + formName
		getInfo()
	} else if (formType == 'edit') {
		isAdd.value = true
		formTitle.value = '修改' + formName
		getInfo()
	} else if (formType == 'cross') {
		isAdd.value = true
		formTitle.value = formNames
		if (row) {
			// 跨表数据回显
			Object.keys(row).forEach(key => {
				if (disabledForm.value.hasOwnProperty(key)) {
					form.value[key] = row[key]
					disabledForm.value[key] = true
				}
			})
			crossRow.value = row
		}
		if (table) crossTable.value = table
		if (tips) crossTips.value = tips
		if (statusColumnName) crossColumnName.value = statusColumnName
		if (statusColumnValue) crossColumnValue.value = statusColumnValue

		form.value.thumbsupnum = 0
		form.value.crazilynum = 0
		formVisible.value = true
	}

	context?.$http({
		url: `${context?.$toolUtil.storageGet('sessionTable')}/session`,
		method: 'get'
	}).then(res => {
		// 处理会话数据
	})
}

defineExpose({ init })

// 关闭表单
const closeClick = () => {
	formVisible.value = false
}

// 富文本变化
const editorChange = (e, name) => {
	form.value[name] = e
}

// 提交表单
const save = () => {
	// 处理文件路径
	if (form.value.picture) {
		form.value.picture = form.value.picture.replace(new RegExp(context?.$config.url, "g"), "");
	}
	if (form.value.video) {
		form.value.video = form.value.video.replace(new RegExp(context?.$config.url, "g"), "");
	}

	var table = crossTable.value
	var objcross = JSON.parse(JSON.stringify(crossRow.value))
	let crossUserId = ''
	let crossRefId = ''
	let crossOptNum = ''

	if (type.value == 'cross') {
		if (crossColumnName.value && !crossColumnName.value.startsWith('[')) {
			Object.keys(objcross).forEach(o => {
				if (o == crossColumnName.value) objcross[o] = crossColumnValue.value
			})
			changeCrossData(objcross)
		} else {
			crossUserId = context?.$toolUtil.storageGet('userid')
			crossRefId = objcross['id']
			crossOptNum = crossColumnName.value.replace(/\[|\]/g, "")
		}
	}

	formRef.value.validate((valid) => {
		if (valid) {
			if (crossUserId && crossRefId) {
				form.value.crossuserid = crossUserId
				form.value.crossrefid = crossRefId
				context?.$http({
					url: `${tableName}/page`,
					method: 'get',
					params: {
						page: 1,
						limit: 1000,
						crossuserid: form.value.crossuserid,
						crossrefid: form.value.crossrefid,
					}
				}).then(res => {
					if (res.data.data.total >= crossOptNum) {
						context?.$toolUtil.message(`${crossTips.value}`, 'error')
						return
					}
					saveData()
				})
			} else {
				saveData()
			}
		}
	})
}

// 保存数据
const saveData = () => {
	context?.$http({
		url: `${tableName}/${!form.value.id ? "save" : "update"}`,
		method: 'post',
		data: form.value
	}).then(res => {
		emit('formModelChange')
		context?.$toolUtil.message(`操作成功`, 'success', () => {
			formVisible.value = false
		})
	})
}

// 修改跨表数据
const changeCrossData = (row) => {
	context?.$http({
		url: `${crossTable.value}/update`,
		method: 'post',
		data: row
	}).then(res => { })
}
</script>
<style lang="scss" scoped>
// 保持原有样式不变
.formModel_form {
	border: 0px solid #ddd;
	border-radius: 4px;
	padding: 30px;
	margin: 0;
	background: #fff;

	:deep(.el-form-item) {
		margin: 0 150px 20px 0;
		background: none;
		display: flex;

		.el-form-item__label {
			background: none;
			font-weight: 500;
			display: block;
			width: 150px;
			min-width: 150px;
			text-align: right;
		}

		.el-form-item__content {
			display: flex;
			width: calc(100% - 120px);
			justify-content: flex-start;
			align-items: center;
			flex-wrap: wrap;

			.list_inp {
				border: 1px solid #ddd;
				border-radius: 0px;
				padding: 0 10px;
				width: auto;
				line-height: 36px;
				box-sizing: border-box;
				height: 36px;

				.el-input__wrapper {
					border: none;
					box-shadow: none;
					background: none;
					border-radius: 0;
					height: 100%;
					padding: 0;
				}

				.is-focus {
					box-shadow: none !important;
				}
			}

			.list_date {
				border: 1px solid #ddd;
				border-radius: 0px;
				width: auto;
				line-height: 36px;
				box-sizing: border-box;
				min-width: 200px;

				.el-input__wrapper {
					border: none;
					box-shadow: none;
					background: none;
					border-radius: 0;
					height: 100%;
				}
			}

			.list_editor {
				background-color: #fff;
				border-radius: 0;
				padding: 0;
				margin: 0;
				width: auto;
				border-color: #ccc;
				border-width: 0;
				border-style: solid;
				min-width: 600px;
				height: auto;
			}

			.el-upload-list {
				.el-upload__tip {
					margin: 7px 0 0;
					color: #999;
					display: flex;
					font-size: 14px;
					justify-content: flex-start;
					align-items: center;
				}

				.el-upload--picture-card {
					border: 1px solid #ddd;
					cursor: pointer;
					background-color: #fff;
					border-radius: 0px;
					width: 120px;
					line-height: 70px;
					text-align: center;
					height: 60px;

					.el-icon {
						color: #999;
						font-size: 32px;
					}
				}

				.el-upload-list__item {
					border: 1px solid #ddd;
					cursor: pointer;
					background-color: #fff;
					border-radius: 0px;
					width: 120px;
					line-height: 70px;
					text-align: center;
					height: 60px;
				}
			}
		}
	}
}

.formModel_btn_box {
	display: flex;
	width: 100%;
	justify-content: center;
	align-items: center;

	.formModel_cancel {
		border: 0;
		cursor: pointer;
		border-radius: 0px;
		padding: 0 24px;
		margin: 0 20px 0 0;
		outline: none;
		color: #fff;
		background: #999;
		width: auto;
		font-size: 14px;
		min-width: 100px;
		height: 36px;
	}

	.formModel_confirm {
		border: 1px solid #f69a28;
		cursor: pointer;
		border-radius: 0px;
		padding: 0 24px;
		margin: 0 20px 0 0;
		outline: none;
		color: #fff;
		background: linear-gradient(270deg, rgba(246, 154, 40, 1) 0%, rgba(255, 186, 101, 1) 50%, rgba(246, 154, 40, 1) 100%);
		width: auto;
		font-size: 14px;
		min-width: 100px;
		height: 36px;
	}
}

.video_field {
	display: flex;
	align-items: flex-start;
	gap: 20px;
	width: 100%;
	flex-wrap: wrap;
}

.video_upload {
	min-width: 220px;
	max-width: 320px;
}

.video_preview_box {
	width: 420px;
	max-width: 100%;
	min-height: 236px;
	border: 1px solid #dcdfe6;
	border-radius: 8px;
	background: #f8fafc;
	display: flex;
	align-items: center;
	justify-content: center;
	overflow: hidden;
}

.video_preview {
	width: 100%;
	height: 236px;
	object-fit: contain;
	background: #000;
}

.video_preview_empty {
	color: #909399;
	font-size: 14px;
}
</style>
