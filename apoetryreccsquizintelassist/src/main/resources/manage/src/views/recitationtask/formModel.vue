<template>
	<div>
		<el-dialog v-model="formVisible" :title="formTitle" width="80%" destroy-on-close :fullscreen='false' class="task_dialog">
			<div class="dialog_intro">
				<div class="intro_badge">{{ type === 'info' ? '任务详情' : (type === 'review' ? '作业批改' : (type === 'edit' ? '任务编辑' : '任务发布')) }}</div>
				<div class="intro_title">{{ form.tasktitle || '让背诵任务更清晰、更好完成' }}</div>
				<div class="intro_desc">{{ isReviewMode ? '这里专门用于老师批改背诵作业，只保留音频、得分和评语等必要信息。' : '把学生、任务要求、时间节点和完成情况放在同一张卡片里，老师查看和维护都会更顺手。' }}</div>
			</div>
			<el-form class="formModel_form" ref="formRef" :model="form" label-width="$template2.back.add.form.base.labelWidth" :rules="rules">
				<div class="form_section review_section" v-if="isReviewMode">
					<div class="section_title">批改信息</div>
					<el-row :gutter="24">
						<el-col :span="12">
							<el-form-item label="学生账号">
								<el-input class="list_inp" v-model="form.studentaccount" readonly />
							</el-form-item>
						</el-col>
						<el-col :span="12">
							<el-form-item label="学生姓名">
								<el-input class="list_inp" v-model="form.studentname" readonly />
							</el-form-item>
						</el-col>
						<el-col :span="12">
							<el-form-item label="任务标题">
								<el-input class="list_inp" v-model="form.tasktitle" readonly />
							</el-form-item>
						</el-col>
						<el-col :span="12">
							<el-form-item label="指定古诗">
								<el-input class="list_inp" v-model="form.coursetitles" readonly />
							</el-form-item>
						</el-col>
						<el-col :span="12">
							<el-form-item label="截止日期">
								<el-input class="list_inp" v-model="form.deadline" readonly />
							</el-form-item>
						</el-col>
						<el-col :span="12">
							<el-form-item label="完成时间">
								<el-input class="list_inp" v-model="form.completiontime" readonly />
							</el-form-item>
						</el-col>
						<el-col :span="24">
							<el-form-item label="完成说明">
								<el-input class="list_inp" v-model="form.completionremark" type="textarea" readonly />
							</el-form-item>
						</el-col>
						<el-col :span="24">
							<el-form-item label="背诵音频">
								<div class="audio_panel" v-if="form.recitationaudio">
									<el-button class="audio_btn" type="primary" plain @click="previewAudio(form.recitationaudio)">试听音频</el-button>
									<el-button class="audio_btn" type="success" plain @click="downloadAudio(form.recitationaudio)">下载音频</el-button>
									<div class="audio_name">{{ getFileName(form.recitationaudio) }}</div>
								</div>
								<div v-else class="audio_empty">学生暂未上传背诵音频</div>
							</el-form-item>
						</el-col>
						<el-col :span="24">
							<el-form-item label="识别文本">
								<el-input class="list_inp" v-model="form.recognizedtext" type="textarea" readonly />
							</el-form-item>
						</el-col>
						<el-col :span="24">
							<el-form-item label="AI初评">
								<el-input class="list_inp" v-model="form.aiscorecomment" type="textarea" readonly />
							</el-form-item>
						</el-col>
						<el-col :span="12">
							<el-form-item label="背诵得分" prop="kaoshichengji">
								<el-input class="list_inp" v-model.number="form.kaoshichengji" placeholder="请输入评分" />
							</el-form-item>
						</el-col>
						<el-col :span="24">
							<el-form-item label="教师评语" prop="teachercomment">
								<el-input class="list_inp" v-model="form.teachercomment" placeholder="请输入批改意见" type="textarea" />
							</el-form-item>
						</el-col>
					</el-row>
				</div>
				<div class="form_section" v-else>
					<div class="section_title">学生与任务</div>
					<el-row :gutter="24">
					<el-col :span="24" v-if="!form.id && type !== 'info'">
						<el-form-item label="发布方式" prop="assignmode">
							<el-radio-group v-model="assignMode" @change="assignModeChange">
								<el-radio-button label="single">单个学生</el-radio-button>
								<el-radio-button label="multiple">多个学生</el-radio-button>
								<el-radio-button label="class">按班级</el-radio-button>
							</el-radio-group>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="学生账号" prop="studentaccount">
							<el-select class="list_sel" :disabled="!isAdd||disabledForm.studentaccount||assignMode!=='single'?true:false" v-model="form.studentaccount" placeholder="请选择学生账号" @change="studentaccountChange">
								<el-option v-for="item in studentaccountLists" :label="item" :value="item"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="12" v-if="!form.id && type !== 'info'">
						<el-form-item label="多个学生" prop="studentaccounts">
							<el-select
								class="list_sel"
								v-model="selectedStudentAccounts"
								placeholder="请选择多个学生"
								multiple
								collapse-tags
								collapse-tags-tooltip
								@change="syncAssignStudentName"
								:disabled="assignMode!=='multiple'">
								<el-option
									v-for="item in studentOptions"
									:key="item.studentaccount"
									:label="`${item.studentname}（${item.studentaccount}）`"
									:value="item.studentaccount"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="12" v-if="!form.id && type !== 'info'">
						<el-form-item label="班级作业" prop="classname">
							<el-select
								class="list_sel"
								v-model="selectedClassname"
								placeholder="请选择班级"
								filterable
								allow-create
								default-first-option
								@change="syncAssignStudentName"
								:disabled="assignMode!=='class'">
								<el-option v-for="item in classOptions" :key="item" :label="item" :value="item"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="学生姓名" prop="studentname">
							<el-input class="list_inp" v-model="form.studentname" :placeholder="assignMode==='single' ? '学生姓名' : '批量发布时自动带入'" type="text" :readonly="!isAdd||disabledForm.studentname?true:false" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="任务标题" prop="tasktitle">
							<el-input class="list_inp" v-model="form.tasktitle" placeholder="任务标题" type="text" :readonly="disabledForm.tasktitle?true:false" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="指定古诗" prop="courseids">
							<el-input
								class="list_inp"
								v-model="form.coursetitles"
								placeholder="请输入古诗名称，可输入一首或多首，用顿号、逗号或换行隔开"
								type="textarea"
								:rows="3"
								:readonly="disabledForm.tasktitle?true:false"
								@input="coursetitlesInput" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="完成状态" prop="completionstatus">
							<el-select class="list_sel" v-model="form.completionstatus" placeholder="请选择完成状态" :disabled="disabledForm.completionstatus?true:false">
								<el-option label="待完成" value="待完成"></el-option>
								<el-option label="已完成" value="已完成"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="24">
						<el-form-item label="任务要求" prop="taskcontent">
							<el-input class="list_inp" v-model="form.taskcontent" placeholder="任务要求" type="textarea" :readonly="disabledForm.taskcontent?true:false" />
						</el-form-item>
						<div v-if="selectedCourseTitles.length" class="selected_course_hint">
							<span class="selected_course_label">当前古诗：</span>{{ selectedCourseTitles.join('、') }}
						</div>
						<div v-if="!form.id && type !== 'info' && assignTargetSummary" class="selected_course_hint">
							<span class="selected_course_label">发布对象：</span>{{ assignTargetSummary }}
						</div>
					</el-col>
					</el-row>
				</div>
				<div class="form_section" v-if="!isReviewMode">
					<div class="section_title">进度与评分</div>
					<el-row :gutter="24">
					<el-col :span="12">
						<el-form-item label="截止日期" prop="deadline">
							<el-date-picker class="list_date" v-model="form.deadline" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" type="datetime" :readonly="disabledForm.deadline?true:false" placeholder="请选择截止日期" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="背诵得分" prop="kaoshichengji">
							<el-input class="list_inp" v-model.number="form.kaoshichengji" placeholder="背诵得分" type="text" :readonly="disabledForm.kaoshichengji?true:false" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="教师账号" prop="teacheraccount">
							<el-input class="list_inp" v-model="form.teacheraccount" placeholder="教师账号" type="text" :readonly="!isAdd||disabledForm.teacheraccount?true:false" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="教师姓名" prop="teachername">
							<el-input class="list_inp" v-model="form.teachername" placeholder="教师姓名" type="text" :readonly="!isAdd||disabledForm.teachername?true:false" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="发布日期" prop="releasetime">
							<el-date-picker class="list_date" v-model="form.releasetime" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" type="datetime" :readonly="disabledForm.releasetime?true:false" placeholder="请选择发布日期" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="完成时间" prop="completiontime">
							<el-date-picker class="list_date" v-model="form.completiontime" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" type="datetime" :readonly="disabledForm.completiontime?true:false" placeholder="请选择完成时间" />
						</el-form-item>
					</el-col>
					<el-col :span="24">
						<el-form-item label="完成说明" prop="completionremark">
							<el-input class="list_inp" v-model="form.completionremark" placeholder="完成说明" type="textarea" :readonly="disabledForm.completionremark?true:false" />
						</el-form-item>
					</el-col>
					<el-col :span="24">
						<el-form-item label="背诵音频">
							<div class="audio_panel" v-if="form.recitationaudio">
								<el-button class="audio_btn" type="primary" plain @click="previewAudio(form.recitationaudio)">试听音频</el-button>
								<el-button class="audio_btn" type="success" plain @click="downloadAudio(form.recitationaudio)">下载音频</el-button>
								<div class="audio_name">{{ getFileName(form.recitationaudio) }}</div>
							</div>
							<div v-else class="audio_empty">学生暂未上传背诵音频</div>
						</el-form-item>
					</el-col>
					<el-col :span="24">
						<el-form-item label="识别文本">
							<el-input class="list_inp" v-model="form.recognizedtext" type="textarea" :readonly="true" />
						</el-form-item>
					</el-col>
					<el-col :span="24">
						<el-form-item label="AI初评">
							<el-input class="list_inp" v-model="form.aiscorecomment" type="textarea" :readonly="true" />
						</el-form-item>
					</el-col>
					<el-col :span="24">
						<el-form-item label="教师评语" prop="teachercomment">
							<el-input class="list_inp" v-model="form.teachercomment" placeholder="老师可填写点评意见" type="textarea" :readonly="disabledForm.teachercomment?true:false" />
						</el-form-item>
					</el-col>
					</el-row>
				</div>
			</el-form>
			<template #footer v-if="isAdd">
				<span class="formModel_btn_box">
					<el-button class="formModel_cancel" @click="closeClick">取消</el-button>
					<el-button class="formModel_confirm" type="primary" @click="save">{{ isReviewMode ? '提交批改' : '提交' }}</el-button>
				</span>
			</template>
		</el-dialog>
	</div>
</template>
<script setup>
import { ref, computed, getCurrentInstance, defineEmits, defineExpose } from 'vue'
import axios from 'axios'
const context = getCurrentInstance()?.appContext.config.globalProperties;
const emit = defineEmits(['formModelChange'])
const tableName = 'recitationtask'
const formName = '背诵任务'
const form = ref({})
const disabledForm = ref({ studentaccount:false, studentname:false, tasktitle:false, taskcontent:false, deadline:false, completionstatus:false, completionremark:false, completiontime:false, kaoshichengji:false, recognizedtext:true, aiscorecomment:true, teachercomment:false, teacheraccount:false, teachername:false, releasetime:false })
const formVisible = ref(false)
const isAdd = ref(false)
const formTitle = ref('')
const formRef = ref(null)
const id = ref(0)
const type = ref('')
const isReviewMode = computed(() => type.value === 'review')
const studentaccountLists = ref([])
const studentOptions = ref([])
const classOptions = ref([])
const selectedStudentAccounts = ref([])
const selectedClassname = ref('')
const assignMode = ref('single')
const currentStudentGrade = ref('')
const validateIntNumber = (rule, value, callback) => {
	if (!value) callback(); else if (!context?.$toolUtil.isIntNumer(value)) callback(new Error("请输入整数")); else callback();
}
const selectedCourseTitles = ref([])
const assignTargetSummary = computed(() => {
	if (assignMode.value === 'multiple') {
		return selectedStudentAccounts.value.length ? `已选择 ${selectedStudentAccounts.value.length} 位学生` : '尚未选择学生'
	}
	if (assignMode.value === 'class') {
		return selectedClassname.value ? `班级：${selectedClassname.value}` : '尚未选择班级'
	}
	return form.value.studentaccount ? `${form.value.studentname || ''}（${form.value.studentaccount}）` : '尚未选择学生'
})
const rules = ref({
	studentname:[{required:true,message:'请输入',trigger:'blur'}],
	tasktitle:[{required:true,message:'请输入',trigger:'blur'}],
	taskcontent:[{required:true,message:'请输入',trigger:'blur'}],
	kaoshichengji:[{validator:validateIntNumber,trigger:'blur'}]
})
const resetForm = () => {
	form.value = { studentaccount:'', studentname:'', courseids:'', coursetitles:'', tasktitle:'', taskcontent:'', deadline:'', completionstatus:'待完成', completionremark:'', recitationaudio:'', completiontime:'', kaoshichengji:'', recognizedtext:'', aiscorecomment:'', teachercomment:'', teacheraccount:'', teachername:'', releasetime:'' }
	disabledForm.value = { studentaccount:false, studentname:false, tasktitle:false, taskcontent:false, deadline:false, completionstatus:false, completionremark:false, completiontime:false, kaoshichengji:false, recognizedtext:true, aiscorecomment:true, teachercomment:false, teacheraccount:false, teachername:false, releasetime:false }
	selectedCourseTitles.value = []
	currentStudentGrade.value = ''
	selectedStudentAccounts.value = []
	selectedClassname.value = ''
	assignMode.value = 'single'
}
const getFileName = (file) => file ? file.split('/').pop() : ''
const getDownloadUrl = (file) => {
	if (!file) return ''
	const fileName = getFileName(file)
	const basePrefix = location.href.split(context?.$config.name).length > 1
		? location.href.split(context?.$config.name)[0]
		: ''
	return `${basePrefix}${context?.$config.name}/file/download?fileName=${encodeURIComponent(fileName)}`
}
const previewAudio = (file) => {
	if (!file) return
	window.open(getDownloadUrl(file), '_blank')
}
const downloadAudio = (file) => {
	if(!file){
		context?.$toolUtil.message('文件不存在','error')
		return
	}
	const fileName = getFileName(file)
	axios.get(getDownloadUrl(file), {
		headers: {
			token: context?.$toolUtil.storageGet('Token')
		},
		responseType: 'blob'
	}).then(({ data }) => {
		const objectUrl = window.URL.createObjectURL(new Blob([data], {
			type: 'audio/mpeg'
		}))
		const a = document.createElement('a')
		a.href = objectUrl
		a.download = fileName
		a.dispatchEvent(new MouseEvent('click', {
			bubbles: true,
			cancelable: true,
			view: window
		}))
		window.URL.revokeObjectURL(objectUrl)
	}).catch(() => {
		context?.$toolUtil.message('下载失败，请稍后重试','error')
	})
}
const loadStudentInfo = (studentaccount) => {
	if (!studentaccount) {
		form.value.studentname = ''
		currentStudentGrade.value = ''
		selectedCourseTitles.value = []
		form.value.courseids = ''
		form.value.coursetitles = ''
		return Promise.resolve()
	}
	return context?.$http({ url: `follow/student/studentaccount?columnValue=` + studentaccount, method: 'get' }).then(res => {
		const studentInfo = res.data.data || {}
		if(studentInfo.studentname){ form.value.studentname = studentInfo.studentname }
		currentStudentGrade.value = studentInfo.grade || ''
	})
}
const loadStudentOptions = () => {
	context?.$http({
		url: 'student/page',
		method: 'get',
		params: {
			page: 1,
			limit: 1000,
			sort: 'id',
			order: 'desc'
		}
	}).then(res => {
		const list = res.data.data.list || []
		studentOptions.value = list
		studentaccountLists.value = list.map(item => item.studentaccount).filter(Boolean)
		classOptions.value = Array.from(new Set(list.map(item => item.classname).filter(Boolean)))
	})
}
const getInfo = () => {
	context?.$http({ url: `${tableName}/info/${id.value}`, method: 'get' }).then(res => {
		form.value = res.data.data
		selectedCourseTitles.value = form.value.coursetitles ? form.value.coursetitles.split(/[,，、\n]+/).filter(Boolean) : []
		loadStudentInfo(form.value.studentaccount).finally(() => {
			formVisible.value = true
		})
	})
}
const coursetitlesInput = (value) => {
	const titles = String(value || '')
		.split(/[,，、\n]+/)
		.map(item => item.trim())
		.filter(Boolean)
	selectedCourseTitles.value = titles
	form.value.courseids = ''
	form.value.coursetitles = titles.join('、')
	if (!form.value.tasktitle || form.value.tasktitle.startsWith('背诵《') || form.value.tasktitle.includes('等')) {
		if (titles.length === 1) form.value.tasktitle = `背诵《${titles[0].replace(/[《》]/g, '')}》`
		else if (titles.length > 1) form.value.tasktitle = `背诵${titles.length}首古诗`
	}
	if (titles.length) {
		const selectedText = `请完成以下古诗背诵：${titles.join('、')}。`
		if (!form.value.taskcontent || form.value.taskcontent.startsWith('请完成以下古诗背诵：')) form.value.taskcontent = selectedText
	}
}
const init = (formId=null, formType='add') => {
	resetForm()
	id.value = formId || 0
	type.value = formType
	form.value.releasetime = context?.$toolUtil.getCurDateTime()
	if (formType == 'add') { isAdd.value = true; formTitle.value = '新增' + formName; formVisible.value = true }
	else if (formType == 'info') { isAdd.value = false; formTitle.value = '查看' + formName; getInfo() }
	else if (formType == 'edit') { isAdd.value = true; formTitle.value = '修改' + formName; getInfo() }
	else if (formType == 'review') { isAdd.value = true; formTitle.value = '批改' + formName; getInfo() }
	context?.$http({ url: `${context?.$toolUtil.storageGet('sessionTable')}/session`, method: 'get' }).then(res => {
		const json = res.data.data
		if(json.hasOwnProperty('teacheraccount')&& context?.$toolUtil.storageGet("role")!="管理员"){ form.value.teacheraccount = json.teacheraccount; disabledForm.value.teacheraccount = true; }
		if(json.hasOwnProperty('teachername')&& context?.$toolUtil.storageGet("role")!="管理员"){ form.value.teachername = json.teachername; disabledForm.value.teachername = true; }
	})
	loadStudentOptions()
	disabledForm.value.studentname = true;
}
defineExpose({ init })
const closeClick = () => { formVisible.value = false }
const studentaccountChange = () => {
	assignMode.value = 'single'
	selectedCourseTitles.value = []
	form.value.courseids = ''
	form.value.coursetitles = ''
	loadStudentInfo(form.value.studentaccount)
}
const assignModeChange = () => {
	if (assignMode.value !== 'single') {
		form.value.studentaccount = ''
		syncAssignStudentName()
	} else {
		selectedStudentAccounts.value = []
		selectedClassname.value = ''
		form.value.studentname = ''
	}
}
const syncAssignStudentName = () => {
	if (assignMode.value === 'multiple') {
		form.value.studentname = selectedStudentAccounts.value.length ? `已选${selectedStudentAccounts.value.length}位学生` : '批量发布'
		return
	}
	if (assignMode.value === 'class') {
		form.value.studentname = selectedClassname.value ? `${selectedClassname.value}学生` : '班级作业'
	}
}
const save = () => {
	formRef.value.validate((valid)=>{
		if(valid){
			if (isReviewMode.value) {
				context?.$http({
					url: `${tableName}/update`,
					method: 'post',
					data: {
						id: form.value.id,
						kaoshichengji: form.value.kaoshichengji,
						teachercomment: form.value.teachercomment
					}
				}).then(()=>{
					emit('formModelChange')
					context?.$toolUtil.message('批改完成','success',()=>{ formVisible.value = false })
				})
				return
			}
			if(type.value !== 'info' && !form.value.id && assignMode.value !== 'single') {
				if (assignMode.value === 'multiple' && !selectedStudentAccounts.value.length) {
					context?.$toolUtil.message('请先选择要发布的学生', 'error')
					return
				}
				if (assignMode.value === 'class' && !selectedClassname.value) {
					context?.$toolUtil.message('请先选择班级', 'error')
					return
				}
				const batchPayload = {
					...form.value,
					studentaccounts: selectedStudentAccounts.value,
					classname: selectedClassname.value
				}
				context?.$http({ url: `${tableName}/batchAssign`, method: 'post', data: batchPayload }).then((res)=>{
					const count = res.data.data || 0
					emit('formModelChange')
					context?.$toolUtil.message(`已成功发布 ${count} 条背诵作业`,'success',()=>{ formVisible.value = false })
				})
				return
			}
			if (assignMode.value === 'single' && !form.value.studentaccount) {
				context?.$toolUtil.message('请选择学生账号', 'error')
				return
			}
			context?.$http({ url: `${tableName}/${!form.value.id ? "save" : "update"}`, method: 'post', data: form.value }).then(()=>{ emit('formModelChange'); context?.$toolUtil.message(`操作成功`,'success',()=>{ formVisible.value = false }) })
		}
	})
}
</script>
<style lang="scss" scoped>
:deep(.task_dialog .el-dialog) {
	border-radius: 28px;
	overflow: hidden;
	background:
		radial-gradient(circle at top right, rgba(255, 211, 146, 0.28), transparent 24%),
		linear-gradient(180deg, #fffdf8 0%, #ffffff 24%, #fffefb 100%);
	box-shadow: 0 28px 80px rgba(100, 78, 41, 0.18);
}

:deep(.task_dialog .el-dialog__header) {
	padding: 24px 30px 0;
	margin-right: 0;
}

:deep(.task_dialog .el-dialog__title) {
	color: #2f2a1f;
	font-size: 28px;
	font-weight: 700;
	letter-spacing: 1px;
}

:deep(.task_dialog .el-dialog__body) {
	padding: 10px 30px 24px;
}

:deep(.task_dialog .el-dialog__footer) {
	padding: 0 30px 30px;
}

.dialog_intro {
	border: 1px solid rgba(225, 196, 129, 0.45);
	border-radius: 24px;
	padding: 22px 24px;
	margin: 8px 0 20px;
	background: linear-gradient(135deg, rgba(255, 248, 226, 0.95) 0%, rgba(245, 252, 247, 0.96) 100%);
}

.intro_badge {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	padding: 6px 14px;
	border-radius: 999px;
	background: #fff;
	color: #7b5b17;
	font-size: 12px;
	font-weight: 700;
	letter-spacing: 1px;
	box-shadow: 0 8px 18px rgba(139, 115, 52, 0.12);
}

.intro_title {
	margin-top: 14px;
	color: #2d2417;
	font-size: 24px;
	font-weight: 700;
	line-height: 1.4;
}

.intro_desc {
	margin-top: 8px;
	color: #72654a;
	font-size: 14px;
	line-height: 1.8;
}

.formModel_form {
	border: 0;
	padding: 0;
	margin: 0;
	background: transparent;
}

.form_section {
	border: 1px solid rgba(231, 220, 194, 0.9);
	border-radius: 24px;
	padding: 24px 24px 6px;
	margin-bottom: 18px;
	background: rgba(255, 255, 255, 0.88);
	box-shadow: inset 0 1px 0 rgba(255,255,255,0.9);
}

.section_title {
	margin: 0 0 22px;
	color: #3f3424;
	font-size: 17px;
	font-weight: 700;
	position: relative;
	padding-left: 14px;
}

.section_title::before {
	content: '';
	position: absolute;
	left: 0;
	top: 4px;
	width: 4px;
	height: 18px;
	border-radius: 999px;
	background: linear-gradient(180deg, #f3b74b 0%, #6abf73 100%);
}

.formModel_form :deep(.el-form-item) {
	margin: 0 0 18px;
	display: flex;
	align-items: flex-start;
}

.formModel_form :deep(.el-form-item__label) {
	color: #5b503f;
	font-weight: 700;
	padding-right: 14px;
	line-height: 46px;
}

.formModel_form :deep(.el-form-item__content) {
	min-width: 0;
}

.formModel_form :deep(.list_inp),
.formModel_form :deep(.list_sel),
.formModel_form :deep(.list_date) {
	width: 100%;
	min-width: 100%;
}

.formModel_form :deep(.el-input__wrapper),
.formModel_form :deep(.el-textarea__inner),
.formModel_form :deep(.el-select__wrapper) {
	border-radius: 16px;
	border: 1px solid #e7dcc2;
	background: linear-gradient(180deg, #fffdfa 0%, #ffffff 100%);
	box-shadow: none;
	padding: 0 14px;
	transition: border-color .2s ease, box-shadow .2s ease, transform .2s ease;
}

.formModel_form :deep(.el-input__wrapper),
.formModel_form :deep(.el-select__wrapper) {
	min-height: 46px;
}

.formModel_form :deep(.el-textarea__inner) {
	min-height: 112px !important;
	padding: 12px 14px;
	line-height: 1.8;
}

.formModel_form :deep(.el-input__wrapper:hover),
.formModel_form :deep(.el-textarea__inner:hover),
.formModel_form :deep(.el-select__wrapper:hover) {
	border-color: #d7ba7c;
}

.formModel_form :deep(.is-focus),
.formModel_form :deep(.el-textarea__inner:focus) {
	border-color: #d8a13a !important;
	box-shadow: 0 0 0 4px rgba(240, 190, 89, 0.14) !important;
	transform: translateY(-1px);
}

.formModel_form :deep(.el-input.is-disabled .el-input__wrapper),
.formModel_form :deep(.el-textarea.is-disabled .el-textarea__inner) {
	background: linear-gradient(180deg, #f8f5ec 0%, #f4efe3 100%);
	color: #82745b;
}

.selected_course_hint {
	margin-top: -6px;
	padding-left: 122px;
	color: #7c6a47;
	font-size: 13px;
	line-height: 1.8;
}

.selected_course_label {
	color: #5fb959;
	font-weight: 700;
	margin-right: 6px;
}

.audio_panel {
	display: flex;
	flex-wrap: wrap;
	align-items: center;
	gap: 12px;
	padding: 6px 0;
}

.audio_btn {
	border-radius: 999px;
}

.audio_name {
	color: #7c6a47;
	font-size: 13px;
	word-break: break-all;
}

.audio_empty {
	color: #9a8d73;
	font-size: 13px;
	padding: 8px 0;
}

.formModel_btn_box {
	display:flex;
	width:100%;
	justify-content:flex-end;
	align-items:center;
	gap: 14px;
}

.formModel_cancel,
.formModel_confirm {
	border-radius: 999px;
	padding: 0 28px;
	min-width: 120px;
	height: 44px;
	font-weight: 700;
	letter-spacing: 1px;
}

.formModel_cancel {
	border: 1px solid #ddd1b5;
	color: #7a6d55;
	background: #fff;
}

.formModel_confirm {
	border: none;
	color: #fff;
	background: linear-gradient(135deg, #e3a73c 0%, #f0bf5e 45%, #6dbe72 100%);
	box-shadow: 0 12px 24px rgba(109, 190, 114, 0.24);
}

.formModel_confirm:hover {
	transform: translateY(-1px);
}

@media (max-width: 1200px) {
	.formModel_form :deep(.el-col) {
		max-width: 100%;
		flex: 0 0 100%;
	}
}
</style>
