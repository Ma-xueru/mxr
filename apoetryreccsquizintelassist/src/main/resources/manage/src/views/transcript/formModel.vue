<template>
	<div>
		<el-dialog v-model="formVisible" :title="formTitle" width="80%" destroy-on-close :fullscreen="false">
			<el-form class="formModel_form" ref="formRef" :model="form" label-width="$template2.back.add.form.base.labelWidth" :rules="rules">
				<el-row>
					<el-col :span="12">
						<el-form-item label="学生账号" prop="studentaccount">
							<el-select class="list_sel" :disabled="!isAdd||disabledForm.studentaccount" v-model="form.studentaccount" placeholder="请选择学生账号" @change="studentaccountChange">
								<el-option v-for="item in studentaccountLists" :key="item" :label="item" :value="item"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="学生姓名" prop="studentname">
							<el-input class="list_inp" v-model="form.studentname" placeholder="学生姓名" :readonly="!isAdd||disabledForm.studentname" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="练习成绩" prop="kaoshichengji">
							<el-input class="list_inp" v-model.number="form.kaoshichengji" placeholder="练习成绩" :readonly="disabledForm.kaoshichengji" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="教师账号" prop="teacheraccount">
							<el-input class="list_inp" v-model="form.teacheraccount" placeholder="教师账号" :readonly="!isAdd||disabledForm.teacheraccount" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="教师姓名" prop="teachername">
							<el-input class="list_inp" v-model="form.teachername" placeholder="教师姓名" :readonly="!isAdd||disabledForm.teachername" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="发布日期" prop="releasetime">
							<el-date-picker class="list_date" v-model="form.releasetime" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" type="datetime" :readonly="!isAdd||disabledForm.releasetime" placeholder="请选择发布日期" />
						</el-form-item>
					</el-col>
				</el-row>
			</el-form>
			<template #footer v-if="isAdd||type=='logistics'||type=='reply'">
				<span class="formModel_btn_box">
					<el-button class="formModel_cancel" @click="closeClick">取消</el-button>
					<el-button class="formModel_confirm" type="primary" @click="save">提交</el-button>
				</span>
			</template>
		</el-dialog>
	</div>
</template>
<script setup>
import { ref, getCurrentInstance, defineEmits, defineExpose } from 'vue'

const context = getCurrentInstance()?.appContext.config.globalProperties
const emit = defineEmits(['formModelChange'])
const tableName = 'transcript'
const formName = '成绩信息'
const form = ref({})
const disabledForm = ref({ studentaccount: false, studentname: false, kaoshichengji: false, teacheraccount: false, teachername: false, releasetime: false })
const formVisible = ref(false)
const isAdd = ref(false)
const formTitle = ref('')
const formRef = ref(null)
const id = ref(0)
const type = ref('')
const studentaccountLists = ref([])
const crossRow = ref('')
const crossTable = ref('')
const crossTips = ref('')
const crossColumnName = ref('')
const crossColumnValue = ref('')

const validateIntNumber = (rule, value, callback) => {
	if (!value) callback()
	else if (!context?.$toolUtil.isIntNumer(value)) callback(new Error("请输入整数"))
	else callback()
}
const rules = ref({
	studentaccount: [{ required: true, message: '请输入', trigger: 'blur' }],
	studentname: [{ required: true, message: '请输入', trigger: 'blur' }],
	kaoshichengji: [{ validator: validateIntNumber, trigger: 'blur' }],
})

const resetForm = () => {
	form.value = { studentaccount: '', studentname: '', kaoshichengji: '', teacheraccount: '', teachername: '', releasetime: '' }
	disabledForm.value = { studentaccount: false, studentname: false, kaoshichengji: false, teacheraccount: false, teachername: false, releasetime: false }
}
const getInfo = () => {
	context?.$http({ url: `${tableName}/info/${id.value}`, method: 'get' }).then(res => {
		form.value = res.data.data
		formVisible.value = true
	})
}

const init = (formId = null, formType = 'add', formNames = '', row = null, table = null, statusColumnName = null, tips = null, statusColumnValue = null) => {
	resetForm()
	form.value.releasetime = context?.$toolUtil.getCurDateTime()
	if (formId) { id.value = formId; type.value = formType }
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
		for (let x in row) {
			if (Object.prototype.hasOwnProperty.call(disabledForm.value, x)) {
				form.value[x] = row[x]
				disabledForm.value[x] = true
			}
		}
		if (row) crossRow.value = row
		if (table) crossTable.value = table
		if (tips) crossTips.value = tips
		if (statusColumnName) crossColumnName.value = statusColumnName
		if (statusColumnValue) crossColumnValue.value = statusColumnValue
		formVisible.value = true
	}

	context?.$http({ url: `${context?.$toolUtil.storageGet('sessionTable')}/session`, method: 'get' }).then(res => {
		const json = res.data.data
		if (json.hasOwnProperty('teacheraccount') && context?.$toolUtil.storageGet("role") != "管理员") {
			form.value.teacheraccount = json.teacheraccount
			disabledForm.value.teacheraccount = true
		}
		if (json.hasOwnProperty('teachername') && context?.$toolUtil.storageGet("role") != "管理员") {
			form.value.teachername = json.teachername
			disabledForm.value.teachername = true
		}
	})
	context?.$http({ url: `option/student/studentaccount`, method: 'get' }).then(res => { studentaccountLists.value = res.data.data })
	disabledForm.value.studentname = true
}
defineExpose({ init })

const closeClick = () => { formVisible.value = false }
const studentaccountChange = () => {
	context?.$http({ url: `follow/student/studentaccount?columnValue=` + form.value.studentaccount, method: 'get' }).then(res => {
		if (res.data.data.studentname) form.value.studentname = res.data.data.studentname
	})
}
const changeCrossData = (row) => { context?.$http({ url: `${crossTable.value}/update`, method: 'post', data: row }).then(() => {}) }

const save = () => {
	const objcross = JSON.parse(JSON.stringify(crossRow.value))
	let crossUserId = ''
	let crossRefId = ''
	let crossOptNum = ''
	if (type.value == 'cross' && crossColumnName.value != '') {
		if (!crossColumnName.value.startsWith('[')) {
			for (let o in objcross) if (o == crossColumnName.value) objcross[o] = crossColumnValue.value
			changeCrossData(objcross)
		} else {
			crossUserId = context?.$toolUtil.storageGet('userid')
			crossRefId = objcross.id
			crossOptNum = crossColumnName.value.replace(/\[/, "").replace(/\]/, "")
		}
	}
	formRef.value.validate((valid) => {
		if (!valid) return
		if (crossUserId && crossRefId) {
			form.value.crossuserid = crossUserId
			form.value.crossrefid = crossRefId
			const params = { page: 1, limit: 1000, crossuserid: form.value.crossuserid, crossrefid: form.value.crossrefid }
			context?.$http({ url: `${tableName}/page`, method: 'get', params }).then(res => {
				if (res.data.data.total >= crossOptNum) return context?.$toolUtil.message(`${crossTips.value}`, 'error')
				context?.$http({ url: `${tableName}/${!form.value.id ? "save" : "update"}`, method: 'post', data: form.value }).then(() => {
					emit('formModelChange')
					context?.$toolUtil.message(`操作成功`, 'success', () => { formVisible.value = false })
				})
			})
		} else {
			context?.$http({ url: `${tableName}/${!form.value.id ? "save" : "update"}`, method: 'post', data: form.value }).then(() => {
				emit('formModelChange')
				context?.$toolUtil.message(`操作成功`, 'success', () => { formVisible.value = false })
			})
		}
	})
}
</script>
<style lang="scss" scoped>
	.formModel_form { padding: 30px; background: #fff; }
	.list_inp, .list_date, .list_sel { border: 1px solid #ddd; border-radius: 0; padding: 0 10px; min-width: 200px; }
	.list_inp { line-height: 36px; height: 36px; }
	.formModel_btn_box { display: flex; width: 100%; justify-content: center; }
	.formModel_cancel, .formModel_confirm { cursor: pointer; border-radius: 0; padding: 0 24px; margin: 0 20px 0 0; color: #fff; min-width: 100px; height: 36px; }
	.formModel_cancel { border: 0; background: #999; }
	.formModel_confirm { border: 1px solid #f69a28; background: linear-gradient(270deg, rgba(246,154,40,1) 0%, rgba(255,186,101,1) 50%, rgba(246,154,40,1) 100%); }
</style>
