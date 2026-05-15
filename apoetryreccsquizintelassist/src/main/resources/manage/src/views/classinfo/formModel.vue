<template>
	<div>
		<el-dialog v-model="formVisible" :title="formTitle" width="70%" destroy-on-close>
			<el-form class="formModel_form" ref="formRef" :model="form" label-width="120px" :rules="rules">
				<el-row :gutter="20">
					<el-col :span="12">
						<el-form-item label="年级" prop="grade">
							<el-select class="list_sel" v-model="form.grade" placeholder="请选择年级" :disabled="!isAdd||disabledForm.grade">
								<el-option v-for="item in gradeLists" :key="item" :label="item" :value="item"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="班级名称" prop="classname">
							<el-input class="list_inp" v-model="form.classname" placeholder="例如：三年级1班" :readonly="!isAdd||disabledForm.classname"></el-input>
						</el-form-item>
					</el-col>
					>
					>
					<el-col :span="24">
						<el-form-item label="班级说明" prop="classdesc">
							<el-input class="list_inp" type="textarea" :rows="4" v-model="form.classdesc" placeholder="请输入班级说明" :readonly="!isAdd||disabledForm.classdesc"></el-input>
						</el-form-item>
					</el-col>
					<template>
	<div>
		<el-dialog v-model="formVisible" :title="formTitle" width="70%" destroy-on-close>
			<el-form class="formModel_form" ref="formRef" :model="form" label-width="120px" :rules="rules">
				<el-row :gutter="20">
					<el-col :span="12">
						<el-form-item label="年级" prop="grade">
							<el-select class="list_sel" v-model="form.grade" placeholder="请选择年级" :disabled="!isAdd||disabledForm.grade">
								<el-option v-for="item in gradeLists" :key="item" :label="item" :value="item"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="班级名称" prop="classname">
							<el-input class="list_inp" v-model="form.classname" placeholder="例如：三年级1班" :readonly="!isAdd||disabledForm.classname"></el-input>
						</el-form-item>
					</el-col>
					>
					>
					<el-col :span="24">
						<el-form-item label="班级说明" prop="classdesc">
							<el-input class="list_inp" type="textarea" :rows="4" v-model="form.classdesc" placeholder="请输入班级说明" :readonly="!isAdd||disabledForm.classdesc"></el-input>
						</el-form-item>
					</el-col>
					>
					<template>
	<div>
		<el-dialog v-model="formVisible" :title="formTitle" width="70%" destroy-on-close>
			<el-form class="formModel_form" ref="formRef" :model="form" label-width="120px" :rules="rules">
				<el-row :gutter="20">
					<el-col :span="12">
						<el-form-item label="年级" prop="grade">
							<el-select class="list_sel" v-model="form.grade" placeholder="请选择年级" :disabled="!isAdd||disabledForm.grade">
								<el-option v-for="item in gradeLists" :key="item" :label="item" :value="item"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="班级名称" prop="classname">
							<el-input class="list_inp" v-model="form.classname" placeholder="例如：三年级1班" :readonly="!isAdd||disabledForm.classname"></el-input>
						</el-form-item>
					</el-col>
					>
					>
					<el-col :span="24">
						<el-form-item label="班级说明" prop="classdesc">
							<el-input class="list_inp" type="textarea" :rows="4" v-model="form.classdesc" placeholder="请输入班级说明" :readonly="!isAdd||disabledForm.classdesc"></el-input>
						</el-form-item>
					</el-col>
					<template>
	<div>
		<el-dialog v-model="formVisible" :title="formTitle" width="70%" destroy-on-close>
			<el-form class="formModel_form" ref="formRef" :model="form" label-width="120px" :rules="rules">
				<el-row :gutter="20">
					<el-col :span="12">
						<el-form-item label="年级" prop="grade">
							<el-select class="list_sel" v-model="form.grade" placeholder="请选择年级" :disabled="!isAdd||disabledForm.grade">
								<el-option v-for="item in gradeLists" :key="item" :label="item" :value="item"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="班级名称" prop="classname">
							<el-input class="list_inp" v-model="form.classname" placeholder="例如：三年级1班" :readonly="!isAdd||disabledForm.classname"></el-input>
						</el-form-item>
					</el-col>
					>
					>
					<el-col :span="24">
						<el-form-item label="班级说明" prop="classdesc">
							<el-input class="list_inp" type="textarea" :rows="4" v-model="form.classdesc" placeholder="请输入班级说明" :readonly="!isAdd||disabledForm.classdesc"></el-input>
						</el-form-item>
					</el-col>
					<el-col :span="24">
						<el-form-item label="班级学生" prop="studentIds">
							<el-select
								class="list_sel"
								v-model="selectedStudentIds"
								multiple
								filterable
								collapse-tags
								collapse-tags-tooltip
								:max-collapse-tags="3"
								placeholder="请选择需要分配到本班的学生"
								:disabled="!isAdd"
							>
								<el-option
									v-for="item in studentOptions"
									:key="item.id"
									:label="`${item.studentname}（${item.studentaccount}）${item.classname ? ' - 当前班级：' + item.classname : ''}`"
									:value="item.id"
								></el-option>
							</el-select>
							<div class="student_tip">保存后将自动为所选学生分配当前班级，未勾选的原班级学生会移出本班。</div>
						</el-form-item>
					</el-col>
					>
				</el-row>
			</el-form>
			<template #footer v-if="isAdd">
				<span class="formModel_btn_box">
					<el-button class="formModel_cancel" @click="closeClick">取消</el-button>
					<el-button class="formModel_confirm" type="primary" @click="save">提交</el-button>
				</span>
			</template>
		</el-dialog>
	</div>
</template>
<script setup>
import { ref, computed, watch, getCurrentInstance, defineEmits, defineExpose } from 'vue'
const context = getCurrentInstance()?.appContext.config.globalProperties
const emit = defineEmits(['formModelChange'])
const tableName = 'classinfo'
const formName = '班级'
const form = ref({})
const studentOptions = ref([])
const selectedStudentIds = ref([])
const originalStudentIds = ref([])
const disabledForm = ref({ grade:false, classname:false, classdesc:false })
const formVisible = ref(false)
const isAdd = ref(false)
const formTitle = ref('')
const formRef = ref(null)
const id = ref(0)
const type = ref('')
const gradeLists = ref("一年级,二年级,三年级,四年级,五年级,六年级".split(','))
const validateIntNumber = (rule, value, callback) => {
	if (value === '' || value === null || value === undefined) callback()
	else if (!context?.$toolUtil.isIntNumer(value)) callback(new Error("请输入整数"))
	else callback()
}
const rules = ref({
	grade:[{ required:true, message:'请选择年级', trigger:'change' }],
	classname:[{ required:true, message:'请输入班级名称', trigger:'blur' }],
	studentcount:[{ validator: validateIntNumber, trigger:'blur' }]
})
const selectedStudents = computed(() => studentOptions.value.filter(item => selectedStudentIds.value.includes(item.id)))
const resetForm = () => {
	form.value = { grade:'一年级', classname:'', classdesc:'' }
	disabledForm.value = { grade:false, classname:false,   classdesc:false }
	selectedStudentIds.value = []
	originalStudentIds.value = []
}
	context?.$http({ url: `${tableName}/info/${id.value}`, method: 'get' }).then(res => {
		form.value = res.data.data
		selectedStudentIds.value = studentOptions.value
			.filter(item => item.classname === form.value.classname)
			.map(item => item.id)
		originalStudentIds.value = [...selectedStudentIds.value]
		formVisible.value = true
	})
}
const init = (formId=null, formType='add') => {
	resetForm()
	if (formId) { id.value = formId; type.value = formType }
	loadStudents().then(() => {
		if (formType == 'add') { isAdd.value = true; formTitle.value = '新增' + formName; formVisible.value = true }
		else if (formType == 'info') { isAdd.value = false; formTitle.value = '查看' + formName; getInfo() }
		else if (formType == 'edit') { isAdd.value = true; formTitle.value = '修改' + formName; getInfo() }
	})
}
defineExpose({ init })
const closeClick = () => { formVisible.value = false }
watch(selectedStudentIds, (value) => {
	form.value.studentcount = value.length
}, { deep: true })
const updateStudentClassRelation = async () => {
	const currentClassname = form.value.classname || ''
	const currentGrade = form.value.grade || ''
	const selectedMap = new Map(studentOptions.value.map(item => [item.id, item]))
	const addTargets = selectedStudentIds.value
		.map(id => selectedMap.get(id))
		.filter(item => item)
	const removeTargets = originalStudentIds.value
		.filter(id => !selectedStudentIds.value.includes(id))
		.map(id => selectedMap.get(id))
		.filter(item => item)
	const requests = []
	addTargets.forEach(item => {
		requests.push(context?.$http({
			url: 'student/update',
			method: 'post',
			data: { ...item, grade: currentGrade, classname: currentClassname }
		}))
	})
	removeTargets.forEach(item => {
		requests.push(context?.$http({
			url: 'student/update',
			method: 'post',
			data: { ...item, classname: '' }
		}))
	})
	if (requests.length) {
		await Promise.all(requests)
	}
}
const save = () => {
	formRef.value.validate((valid)=>{
		if(valid){
			form.value.studentcount = selectedStudentIds.value.length
			context?.$http({ url: `${tableName}/${!form.value.id ? "save" : "update"}`, method: 'post', data: form.value }).then(async ()=>{
				await updateStudentClassRelation()
				emit('formModelChange')
				context?.$toolUtil.message(`操作成功`,'success',()=>{ formVisible.value = false 	})
		}
	})
}
</script>
<style lang="scss" scoped>
.formModel_form{
	border: 0;
	padding: 20px 10px 0;
	margin: 0;
	background: #fff;
	:deep(.el-form-item) {
		margin: 0 0 18px;
		.el-form-item__label {
			font-weight: 600;
			color: #5b503f;
		}
	}
	:deep(.list_inp), :deep(.list_sel) {
		width: 100%;
	}
	:deep(.el-input__wrapper), :deep(.el-textarea__inner), :deep(.el-select__wrapper) {
		border-radius: 12px;
		border: 1px solid #e4dac7;
		box-shadow: none;
	}
	:deep(.el-textarea__inner) {
		min-height: 110px !important;
	}
}
.student_tip {
	margin-top: 8px;
	color: #8c7b58;
	font-size: 13px;
	line-height: 1.6;
}
:deep(.auto_count_inp .el-input__wrapper) {
	background: #f8f4eb;
	color: #8a7444;
}
.student_panel {
	padding: 16px 18px;
	border-radius: 16px;
	background: linear-gradient(135deg, #fffdf7 0%, #f7f2e8 100%);
	border: 1px solid #eadfca;
}
.student_panel_title {
	margin-bottom: 12px;
	color: #5d4c2e;
	font-size: 14px;
	font-weight: 700;
}
.student_tags {
	display: flex;
	flex-wrap: wrap;
	gap: 10px;
}
.student_tag {
	display: flex;
	align-items: center;
	gap: 8px;
	padding: 8px 12px;
	border-radius: 999px;
	background: #fff;
	border: 1px solid #eadfca;
}
.student_name {
	color: #3f3628;
	font-weight: 600;
}
.student_meta {
	color: #8b7b60;
	font-size: 12px;
}
.formModel_btn_box {
	display:flex;
	justify-content:flex-end;
	gap:12px;
	width:100%;
}
.formModel_cancel,
.formModel_confirm {
	border-radius:999px;
	min-width:110px;
	height:40px;
}
</style>
