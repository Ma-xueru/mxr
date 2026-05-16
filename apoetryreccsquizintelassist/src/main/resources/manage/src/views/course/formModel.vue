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
						<el-form-item label="古诗词封面">
							<div v-if="form.picture&&form.picture.substring(0,6)=='cloud:'" style="display:flex;align-items:center;gap:12px">
								<el-image :src="$config.url+'/course/coverProxy?fileId='+encodeURIComponent(form.picture)" style="width:120px;height:120px;border-radius:8px"></el-image>
								<span style="color:#909399;font-size:12px">来自微信云存储</span>
							</div>
							<div v-else style="color:#909399;font-size:13px">封面由小程序端AI自动生成，无需手动上传</div>
						</el-form-item>
					</el-col>
					<el-col :span="24">
						<el-form-item label="古诗词简介" prop="intro">
							<el-input class="textarea" v-model="form.intro" placeholder="请输入古诗词简介" type="textarea"
								:rows="3" :readonly="!isAdd || disabledForm.intro ? true : false" />
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

const tableName = 'course'
const formName = '古诗词'

const form = ref({})
const disabledForm = ref({
	courseno: false,
	coursetitle: false,
	coursetype: false,
	grade: false,
	picture: true,
	intro: false,
	content: false,
	addtime: false,
	clicktime: false,
})
const formVisible = ref(false)
const isAdd = ref(false)
const formTitle = ref('')

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
})

const formRef = ref(null)
const id = ref(0)
const type = ref('')
const gradeLists = ref("一年级,二年级,三年级,四年级,五年级,六年级".split(','))

const resetForm = () => {
	form.value = {
		courseno: '',
		coursetitle: '',
		coursetype: '',
		grade: '一年级',
		picture: '',
		intro: '',
		content: '',
		addtime: new Date(),
		clicktime: null,
	}
}

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

		formVisible.value = true
	}

	context?.$http({
		url: `${context?.$toolUtil.storageGet('sessionTable')}/session`,
		method: 'get'
	}).then(res => {})
}

defineExpose({ init })

const closeClick = () => {
	formVisible.value = false
}

const editorChange = (e, name) => {
	form.value[name] = e
}

const save = () => {
	if (form.value.picture) {
		form.value.picture = form.value.picture.replace(new RegExp(context?.$config.url, "g"), "");
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

const changeCrossData = (row) => {
	context?.$http({
		url: `${crossTable.value}/update`,
		method: 'post',
		data: row
	}).then(res => {})
}
</script>
<style lang="scss" scoped>
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
</style>
