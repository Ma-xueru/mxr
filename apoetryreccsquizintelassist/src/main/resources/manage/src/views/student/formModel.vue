<template>
	<div>
		<el-dialog v-model="formVisible" :title="formTitle" width="80%" destroy-on-close :fullscreen='false'>
			<el-form class="formModel_form" ref="formRef" :model="form" label-width="$template2.back.add.form.base.labelWidth" :rules="rules">
				<el-row>
					<el-col :span="12">
						<el-form-item label="用户账号" prop="studentaccount">
							<el-input class="list_inp" v-model="form.studentaccount" placeholder="用户账号"
								type="text" :readonly="!isAdd||disabledForm.studentaccount?true:false" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="用户密码" prop="studentpassword">
							<el-input class="list_inp" v-model="form.studentpassword" placeholder="用户密码"
								type="password" :readonly="!isAdd||disabledForm.studentpassword?true:false" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="用户姓名" prop="studentname">
							<el-input class="list_inp" v-model="form.studentname" placeholder="用户姓名"
								 type="text" 								:readonly="!isAdd||disabledForm.studentname?true:false" />
						</el-form-item>
					</el-col>

					<el-col :span="12">
						<el-form-item label="性别" prop="gender">
							<el-select
								class="list_sel"
								:disabled="!isAdd||disabledForm.gender?true:false"
								v-model="form.gender"
								placeholder="请选择性别"
								>
								<el-option v-for="(item,index) in genderLists" :label="item"
									:value="item"
									>
								</el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="手机号码" prop="telephone">
							<el-input class="list_inp" v-model="form.telephone" placeholder="手机号码"
								 type="text" 								:readonly="!isAdd||disabledForm.telephone?true:false" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="年级" prop="grade">
							<el-select
								class="list_sel"
								:disabled="!isAdd||disabledForm.grade?true:false"
								v-model="form.grade"
								@change="gradeChange"
								placeholder="请选择年级">
								<el-option v-for="item in gradeOptions" :key="item" :label="item" :value="item"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="班级" prop="classname">
							<el-select
								class="list_sel"
								:disabled="!isAdd||disabledForm.classname?true:false"
								v-model="form.classname"
								placeholder="请选择班级">
								<el-option v-for="(item,index) in classOptions" :label="item" :value="item" :key="index"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="权限状态" prop="permissionstatus">
							<el-select
								class="list_sel"
								:disabled="!isAdd||disabledForm.permissionstatus?true:false"
								v-model="form.permissionstatus"
								placeholder="请选择权限状态">
								<el-option label="启用" value="启用"></el-option>
								<el-option label="禁用" value="禁用"></el-option>
							</el-select>
						</el-form-item>
					</el-col>

				</el-row>
			</el-form>
			<template #footer v-if="isAdd||type=='logistics'||type=='reply'">
				<span class="formModel_btn_box">
					<el-button class="formModel_cancel" @click="closeClick">取消</el-button>
					<el-button class="formModel_confirm" type="primary" @click="save"
						>
						提交
					</el-button>
				</span>
			</template>
		</el-dialog>
	</div>
</template>
<script setup>
	import {
		reactive,
		ref,
		getCurrentInstance,
		nextTick,
		computed,
		defineEmits
	} from 'vue'
	const context = getCurrentInstance()?.appContext.config.globalProperties;
	const emit = defineEmits(['formModelChange'])
	const tableName = 'student'
	const formName = '用户'
	const form = ref({})
	const disabledForm = ref({
		studentaccount : false,
		studentpassword : false,
		studentname : false,
		gender : false,
		telephone : false,
		grade : false,
		classname : false,
		permissionstatus : false,
	})
	const formVisible = ref(false)
	const isAdd = ref(false)
	const formTitle = ref('')

	const validateMobile = (rule, value, callback) => {
		if(!value){
			callback();
		} else if (!context?.$toolUtil.isMobile(value)) {
			callback(new Error("请输入正确的手机号码"));
		} else {
			callback();
		}
	}
	const rules = ref({
		studentaccount: [
			{required: true,message: '请输入',trigger: 'blur'},
		],
		studentpassword: [
			{required: true,message: '请输入',trigger: 'blur'},
		],
		studentname: [
			{required: true,message: '请输入',trigger: 'blur'},
		],
		gender: [
		],
		telephone: [
			{ validator: validateMobile, trigger: 'blur' },
		],
		grade: [
		],
		classname: [
		],
		permissionstatus: [
		],
	})

	const formRef = ref(null)
	const id = ref(0)
	const type = ref('')
	const genderLists = ref([])
	const gradeOptions = ref([])
	const classOptions = ref([])
	const allTeacherClasses = ref([])

	const getUUID =()=> {
      return new Date().getTime();
    }

	const resetForm = () => {
		form.value = {
			studentaccount: '',
			studentpassword: '',
			studentname: '',
			gender: '男',
			telephone: '',
			grade: '',
			classname: '',
			permissionstatus: '启用',
		}
	}

	const gradeChange = (value) => {
		form.value.classname = ''
		if (value) {
			classOptions.value = allTeacherClasses.value.filter(c => c.startsWith(value))
		}
	}

	const getInfo = ()=>{
		context?.$http({
			url: `${tableName}/info/${id.value}`,
			method: 'get'
		}).then(res => {
			form.value = res.data.data
			formVisible.value = true
		})
	}
	const crossRow = ref('')
	const crossTable = ref('')
	const crossTips = ref('')
	const crossColumnName = ref('')
	const crossColumnValue = ref('')

	const loadTeacherClasses = () => {
		context?.$http({
			url: 'classinfo/list',
			method: 'get',
			params: { page: 1, limit: 999 }
		}).then(res => {
			const list = (res.data.data && res.data.data.list) || []
			const classes = list.map(c => c.classname).filter(Boolean).sort()
			allTeacherClasses.value = classes
			classOptions.value = classes
			const grades = [...new Set(classes.map(c => c.replace(/\d+班$/, '')))]
			gradeOptions.value = grades
			if (grades.length === 1 && isAdd.value) {
				form.value.grade = grades[0]
				gradeChange(grades[0])
			}
		}).catch(() => {
			gradeOptions.value = "一年级,二年级,三年级,四年级,五年级,六年级".split(',')
		})
	}

	const init=(formId=null,formType='add',formNames='',row=null,table=null,statusColumnName=null,tips=null,statusColumnValue=null)=>{
		resetForm()
		if(formId){
			id.value = formId
			type.value = formType
		}
		if(formType == 'add'){
			isAdd.value = true
			formTitle.value = '新增' + formName
			loadTeacherClasses()
			formVisible.value = true
		}else if(formType == 'info'){
			isAdd.value = false
			formTitle.value = '查看' + formName
			getInfo()
		}else if(formType == 'edit'){
			isAdd.value = true
			formTitle.value = '修改' + formName
			loadTeacherClasses()
			getInfo()
		}
		else if(formType == 'cross'){
			isAdd.value = true
			formTitle.value = formNames
			for(let x in row){
				if(x=='studentaccount'){
					form.value.studentaccount = row[x];
					disabledForm.value.studentaccount = true;
					continue;
				}
				if(x=='studentpassword'){
					form.value.studentpassword = row[x];
					disabledForm.value.studentpassword = true;
					continue;
				}
				if(x=='studentname'){
					form.value.studentname = row[x];
					disabledForm.value.studentname = true;
					continue;
				}
				if(x=='gender'){
					form.value.gender = row[x];
					disabledForm.value.gender = true;
					continue;
				}
				if(x=='telephone'){
					form.value.telephone = row[x];
					disabledForm.value.telephone = true;
					continue;
				}
				if(x=='grade'){
					form.value.grade = row[x];
					disabledForm.value.grade = true;
					continue;
				}
				if(x=='classname'){
					form.value.classname = row[x];
					disabledForm.value.classname = true;
					continue;
				}
				if(x=='permissionstatus'){
					form.value.permissionstatus = row[x];
					disabledForm.value.permissionstatus = true;
					continue;
				}
			}
			if(row){
				crossRow.value = row
			}
			if(table){
				crossTable.value = table
			}
			if(tips){
				crossTips.value = tips
			}
			if(statusColumnName){
				crossColumnName.value = statusColumnName
			}
			if(statusColumnValue){
				crossColumnValue.value = statusColumnValue
			}
			form.value.gender='男'
			formVisible.value = true
		}

		context?.$http({
			url: `${context?.$toolUtil.storageGet('sessionTable')}/session`,
			method: 'get'
		}).then(res => {
			var json = res.data.data
		})
		genderLists.value = "男,女".split(',')
	}
	defineExpose({
		init
	})
	const closeClick = () => {
		formVisible.value = false
	}
	const editorChange = (e,name) =>{
		form.value[name] = e
	}
	const save=()=>{
		var table = crossTable.value
		var objcross = JSON.parse(JSON.stringify(crossRow.value))
		let crossUserId = ''
		let crossRefId = ''
		let crossOptNum = ''
		if(type.value == 'cross'){
			if(crossColumnName.value!=''){
				if(!crossColumnName.value.startsWith('[')){
					for(let o in objcross){
						if(o == crossColumnName.value){
							objcross[o] = crossColumnValue.value
						}
					}
					changeCrossData(objcross)
				}else{
					crossUserId = context?.$toolUtil.storageGet('userid')
					crossRefId = objcross['id']
					crossOptNum = crossColumnName.value.replace(/\[/,"").replace(/\]/,"")
				}
			}
		}
		formRef.value.validate((valid)=>{
			if(valid){
				if(crossUserId&&crossRefId){
					form.value.crossuserid = crossUserId
					form.value.crossrefid = crossRefId
					let params = {
						page: 1,
						limit: 1000,
						crossuserid:form.value.crossuserid,
						crossrefid:form.value.crossrefid,
					}
					context?.$http({
						url: `${tableName}/page`,
						method: 'get',
						params: params
					}).then(res=>{
						if(res.data.data.total>=crossOptNum){
							context?.$toolUtil.message(`${crossTips.value}`,'error')
							return false
						}else{
							context?.$http({
								url: `${tableName}/${!form.value.id ? "save" : "update"}`,
								method: 'post',
								data: form.value
							}).then(res=>{
								emit('formModelChange')
								context?.$toolUtil.message(`操作成功`,'success',()=>{
									formVisible.value = false
								})
							})
						}
					})
				}else{
					context?.$http({
						url: `${tableName}/${!form.value.id ? "save" : "update"}`,
						method: 'post',
						data: form.value
					}).then(res=>{
						emit('formModelChange')
						context?.$toolUtil.message(`操作成功`,'success',()=>{
							formVisible.value = false
						})
					})
				}
			}
		})
	}
	const changeCrossData=(row)=>{
		context?.$http({
			url: `${crossTable.value}/update`,
			method: 'post',
			data: row
		}).then(res=>{})
	}
</script>
<style lang="scss" scoped>
	.formModel_form{
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
					.el-input__wrapper{
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
				.list_sel {
					border: 1px solid #ddd;
					border-radius: 0px;
					padding: 0 10px;
					width: auto;
					line-height: 36px;
					box-sizing: border-box;
					min-width: 200px;
					.select-trigger{
						height: 100%;
						.el-input{
							height: 100%;
							.el-input__wrapper{
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
					}
				}
				.el-upload-list  {
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
						.el-icon{
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
		.formModel_cancel:hover {
		}

		.formModel_confirm {
			border: 1px solid #f69a28;
			cursor: pointer;
			border-radius: 0px;
			padding: 0 24px;
			margin: 0 20px 0 0;
			outline: none;
			color: #fff;
			background: linear-gradient(270deg, rgba(246,154,40,1) 0%, rgba(255,186,101,1) 50%, rgba(246,154,40,1) 100%);
			width: auto;
			font-size: 14px;
			min-width: 100px;
			height: 36px;
		}
		.formModel_confirm:hover {
		}
	}
</style>
