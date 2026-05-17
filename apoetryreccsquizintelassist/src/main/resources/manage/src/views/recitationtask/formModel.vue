<template>
	<div>
		<el-dialog v-model="formVisible" :title="formTitle" width="80%" destroy-on-close :fullscreen='false' class="task_dialog">
			<div class="dialog_intro">
				<div class="intro_badge">{{ type === 'info' ? '任务详情' : (type === 'review' ? '作业批改' : (type === 'edit' ? '任务编辑' : '任务发布')) }}</div>
				<div class="intro_title">{{ form.tasktitle || '按班级一键发布，覆盖全班学生' }}</div>
				<div class="intro_desc">{{ isReviewMode ? '老师批改背诵作业，只保留音频、得分和评语。' : '选择管辖班级，一键级联分发给所有学生，告别逐个添加。' }}</div>
			</div>
			<el-form class="formModel_form" ref="formRef" :model="form" label-width="$template2.back.add.form.base.labelWidth" :rules="rules">

				<!-- 批改/详情模式 -->
				<div class="form_section review_section" v-if="isReviewMode || isDetailMode">
					<div class="section_title">批改信息</div>
					<el-row :gutter="24">
						<el-col :span="12"><el-form-item label="学生账号"><el-input class="list_inp" v-model="form.studentaccount" readonly /></el-form-item></el-col>
						<el-col :span="12"><el-form-item label="学生姓名"><el-input class="list_inp" v-model="form.studentname" readonly /></el-form-item></el-col>
						<el-col :span="12"><el-form-item label="任务标题"><el-input class="list_inp" v-model="form.tasktitle" readonly /></el-form-item></el-col>
						<el-col :span="12"><el-form-item label="指定古诗"><el-input class="list_inp" v-model="form.coursetitles" readonly /></el-form-item></el-col>
						<el-col :span="12"><el-form-item label="截止日期"><el-input class="list_inp" v-model="form.deadline" readonly /></el-form-item></el-col>
						<el-col :span="12"><el-form-item label="完成时间"><el-input class="list_inp" v-model="form.completiontime" readonly /></el-form-item></el-col>
						<el-col :span="24"><el-form-item label="完成说明"><el-input class="list_inp" v-model="form.completionremark" type="textarea" readonly /></el-form-item></el-col>
						<el-col :span="24">
							<el-form-item label="背诵音频">
								<div class="audio_panel" v-if="form.recitationaudio">
									<audio :src="getAudioSrc(form.recitationaudio)" controls preload="metadata" class="audio_player"></audio>
									<div class="audio_name">{{ getFileName(form.recitationaudio) }}</div>
								</div>
								<div v-else class="audio_empty">暂未上传</div>
							</el-form-item>
						</el-col>
						<el-col :span="24"><el-form-item label="识别文本"><el-input class="list_inp" v-model="form.recognizedtext" type="textarea" readonly /></el-form-item></el-col>
						<el-col :span="24" v-if="parsedAiReview">
							<el-form-item label="AI多维度评审">
								<div class="ai_review_panel">
									<div class="radar_section"><div ref="radarChartRef" class="radar_chart"></div></div>
									<div class="detail_section">
										<div class="dimension_card" v-for="(dim, idx) in parsedAiReview.dimensions" :key="idx">
											<div class="dim_header"><span class="dim_name">{{ dim.name }}</span><span class="dim_weight">权重 {{ dim.weight }}%</span><span class="dim_score">{{ dim.score }}分</span></div>
											<el-progress :percentage="dim.score" :color="progressColor(dim.score)" :stroke-width="8" />
											<div class="dim_comment">{{ dim.comment }}</div><div class="dim_encourage">{{ dim.encourage }}</div>
										</div>
										<div class="overall_comment" v-if="parsedAiReview.overallComment"><div class="overall_label">总体评价</div><div class="overall_text">{{ parsedAiReview.overallComment }}</div></div>
									</div>
								</div>
							</el-form-item>
						</el-col>
						<el-col :span="24" v-else>
							<el-form-item label="AI初评"><el-input class="list_inp" v-model="form.aiscorecomment" type="textarea" readonly /></el-form-item>
						</el-col>
						<el-col :span="12"><el-form-item label="背诵得分" prop="kaoshichengji"><el-input class="list_inp" v-model.number="form.kaoshichengji" placeholder="请输入评分" :readonly="isDetailMode" /></el-form-item></el-col>
						<el-col :span="24"><el-form-item label="教师评语" prop="teachercomment"><el-input class="list_inp" v-model="form.teachercomment" placeholder="请输入批改意见" type="textarea" :readonly="isDetailMode" /></el-form-item></el-col>
					</el-row>
				</div>

				<!-- 发布/编辑模式 -->
				<div class="form_section" v-else>
					<div class="section_title">任务信息</div>
					<el-row :gutter="24">
						<el-col :span="24" v-if="!form.id">
							<el-form-item label="发布班级" prop="classnames">
								<el-select class="list_sel" v-model="selectedClassnames" placeholder="请选择管辖班级（可多选）" multiple collapse-tags collapse-tags-tooltip>
									<el-option v-for="item in classOptions" :key="item" :label="item" :value="item"></el-option>
								</el-select>
							</el-form-item>
						</el-col>
						<el-col :span="12">
							<el-form-item label="任务标题" prop="tasktitle">
								<el-input class="list_inp" v-model="form.tasktitle" placeholder="例如：背诵《静夜思》" type="text" :readonly="disabledForm.tasktitle?true:false" />
							</el-form-item>
						</el-col>
						<el-col :span="12">
							<el-form-item label="指定古诗" prop="courseids">
								<el-select class="list_sel" v-model="selectedGrade" placeholder="先选年级" @change="gradeChange" style="margin-bottom:8px">
									<el-option v-for="g in gradeOptions" :key="g" :label="g" :value="g"></el-option>
								</el-select>
								<el-select class="list_sel" v-model="selectedCourseIds" placeholder="选择古诗（可多选）" multiple collapse-tags :disabled="!selectedGrade" @change="courseSelectChange">
									<el-option v-for="item in filteredCourseOptions" :key="item.id" :label="item.coursetitle" :value="item.id"></el-option>
								</el-select>
							</el-form-item>
						</el-col>
						<el-col :span="12">
							<el-form-item label="完成状态" prop="completionstatus">
								<el-select class="list_sel" v-model="form.completionstatus" :disabled="disabledForm.completionstatus?true:false">
									<el-option label="待完成" value="待完成"></el-option>
									<el-option label="已完成" value="已完成"></el-option>
								</el-select>
							</el-form-item>
						</el-col>
						<el-col :span="24">
							<el-form-item label="任务要求" prop="taskcontent">
								<el-input class="list_inp" v-model="form.taskcontent" placeholder="例如：请完成以下古诗背诵..." type="textarea" :readonly="disabledForm.taskcontent?true:false" />
							</el-form-item>
							<div v-if="selectedCourseTitles.length" class="selected_course_hint">
								<span class="selected_course_label">当前古诗：</span>{{ selectedCourseTitles.join('、') }}
							</div>
							<div v-if="!form.id && selectedClassnames.length" class="selected_course_hint">
								<span class="selected_course_label">发布对象：</span>{{ selectedClassnames.join('、') }}（共 {{ selectedClassnames.length }} 个班）
							</div>
						</el-col>
					</el-row>
				</div>

				<!-- 进度与评分 -->
				<div class="form_section" v-if="!isReviewMode">
					<div class="section_title">进度与评分</div>
					<el-row :gutter="24">
						<el-col :span="12">
							<el-form-item label="截止日期" prop="deadline">
								<el-date-picker class="list_date" v-model="form.deadline" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" type="datetime" :readonly="disabledForm.deadline?true:false" placeholder="请选择截止日期" />
							</el-form-item>
						</el-col>
						<el-col :span="12">
							<el-form-item label="教师账号" prop="teacheraccount">
								<el-input class="list_inp" v-model="form.teacheraccount" placeholder="自动填入" type="text" :readonly="!isAdd||disabledForm.teacheraccount?true:false" />
							</el-form-item>
						</el-col>
						<el-col :span="12">
							<el-form-item label="教师姓名" prop="teachername">
								<el-input class="list_inp" v-model="form.teachername" placeholder="自动填入" type="text" :readonly="!isAdd||disabledForm.teachername?true:false" />
							</el-form-item>
						</el-col>
						<el-col :span="12">
							<el-form-item label="发布日期" prop="releasetime">
								<el-date-picker class="list_date" v-model="form.releasetime" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" type="datetime" :readonly="disabledForm.releasetime?true:false" placeholder="请选择发布日期" />
							</el-form-item>
						</el-col>
					</el-row>
				</div>
			</el-form>
			<template #footer v-if="isAdd">
				<span class="formModel_btn_box">
					<el-button class="formModel_cancel" @click="closeClick">取消</el-button>
					<el-button class="formModel_confirm" type="primary" @click="save">{{ isReviewMode ? '提交批改' : '发布任务' }}</el-button>
				</span>
			</template>
		</el-dialog>
	</div>
</template>
<script setup>
import { ref, computed, watch, nextTick, onBeforeUnmount, inject, getCurrentInstance, defineEmits, defineExpose } from 'vue'
import axios from 'axios'
const context = getCurrentInstance()?.appContext.config.globalProperties;
const echarts = inject('echarts');
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
const isDetailMode = computed(() => type.value === 'info')
const showAiReview = computed(() => isReviewMode.value || isDetailMode.value)
const radarChartRef = ref(null)
const radarChart = ref(null)
const classOptions = ref([])
const selectedClassnames = ref([])
const gradeOptions = ref("一年级,二年级,三年级,四年级,五年级,六年级".split(','))
const validateIntNumber = (rule, value, callback) => { if (!value) callback(); else if (!context?.$toolUtil.isIntNumer(value)) callback(new Error("请输入整数")); else callback(); }
const selectedCourseTitles = ref([])
const selectedGrade = ref('')
const selectedCourseIds = ref([])
const allCourseOptions = ref([])
const filteredCourseOptions = computed(() => { if (!selectedGrade.value) return []; return allCourseOptions.value.filter(item => item.grade === selectedGrade.value) })
const rules = ref({ tasktitle:[{required:true,message:'请输入任务标题',trigger:'blur'}], taskcontent:[{required:true,message:'请输入任务要求',trigger:'blur'}], kaoshichengji:[{validator:validateIntNumber,trigger:'blur'}] })

const parsedAiReview = computed(() => {
	try { const raw = form.value.aiscorecomment; if (!raw || typeof raw !== 'string') return null
		const p = JSON.parse(raw); if (typeof p.totalScore === 'number' && Array.isArray(p.dimensions)) return p; return null
	} catch (e) { return null }
})
const progressColor = (score) => { if (score >= 85) return '#6fc47d'; if (score >= 70) return '#e7ba63'; return '#e88a6e' }
const renderRadarChart = () => {
	nextTick(() => {
		if (!radarChartRef.value || !parsedAiReview.value || !echarts) return
		if (radarChart.value) { radarChart.value.dispose(); radarChart.value = null }
		radarChart.value = echarts.init(radarChartRef.value)
		const dims = parsedAiReview.value.dimensions
		radarChart.value.setOption({
			radar: { center: ['50%','50%'], radius: '65%', indicator: dims.map(d => ({ name: `${d.name}\n${d.score}分`, max: 100 })), axisName: { color: '#5b503f', fontSize: 12 } },
			series: [{ type: 'radar', data: [{ value: dims.map(d => d.score), name: '背诵评分', areaStyle: { color: 'rgba(109,190,114,0.2)' }, lineStyle: { color: '#6fc47d', width: 2 }, itemStyle: { color: '#6fc47d' } }] }]
		})
	})
}
watch(parsedAiReview, (val) => { if (val) renderRadarChart() })
onBeforeUnmount(() => { radarChart.value && radarChart.value.dispose() })

const getFileName = (file) => file ? file.split('/').pop() : ''
const getDownloadUrl = (file) => {
	if (!file) return ''
	const fileName = getFileName(file)
	const basePrefix = location.href.split(context?.$config.name).length > 1 ? location.href.split(context?.$config.name)[0] : ''
	return `${basePrefix}${context?.$config.name}/file/download?fileName=${encodeURIComponent(fileName)}`
}
const getAudioSrc = (file) => { if (!file) return ''; if (file.indexOf('http') === 0) return file; return context?.$config.url + '/file/' + getFileName(file) }
const previewAudio = (file) => { if (!file) return; window.open(getDownloadUrl(file), '_blank') }
const downloadAudio = (file) => {
	if(!file){ context?.$toolUtil.message('文件不存在','error'); return }
	axios.get(getDownloadUrl(file), { headers: { token: context?.$toolUtil.storageGet('Token') }, responseType: 'blob' }).then(({ data }) => {
		const objectUrl = window.URL.createObjectURL(new Blob([data], { type: 'audio/mpeg' }))
		const a = document.createElement('a'); a.href = objectUrl; a.download = getFileName(file)
		a.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, view: window }))
		window.URL.revokeObjectURL(objectUrl)
	}).catch(() => { context?.$toolUtil.message('下载失败','error') })
}

const loadClassOptions = () => {
	context?.$http({ url: 'classinfo/list', method: 'get', params: { page: 1, limit: 999 } }).then(res => {
		const list = (res.data.data && res.data.data.list) || []
		classOptions.value = list.map(c => c.classname).filter(Boolean).sort()
	}).catch(() => {})
}

const getInfo = () => {
	context?.$http({ url: `${tableName}/info/${id.value}`, method: 'get' }).then(res => {
		form.value = res.data.data
		selectedCourseTitles.value = form.value.coursetitles ? form.value.coursetitles.split(/[,，、\n]+/).filter(Boolean) : []
		if (form.value.courseids) {
			const ids = form.value.courseids.split(",").map(Number)
			selectedCourseIds.value = ids
			if (ids.length > 0 && allCourseOptions.value.length > 0) {
				const firstCourse = allCourseOptions.value.find(c => c.id === ids[0])
				if (firstCourse) selectedGrade.value = firstCourse.grade
			}
		}
		formVisible.value = true
	})
}

const loadCourseOptions = () => {
	context.$http({ url: 'course/list', method: 'get', params: { page: 1, limit: 200 } }).then(res => {
		allCourseOptions.value = (res.data.data.list || []).map(item => ({ id: item.id, coursetitle: item.coursetitle, grade: item.grade }))
	})
}

const gradeChange = () => { selectedCourseIds.value = []; form.value.courseids = ''; form.value.coursetitles = ''; selectedCourseTitles.value = [] }
const courseSelectChange = (ids) => {
	if (!ids || !ids.length) { form.value.courseids = ''; form.value.coursetitles = ''; selectedCourseTitles.value = []; return }
	form.value.courseids = ids.join(',')
	const titles = ids.map(id => { const found = allCourseOptions.value.find(item => item.id === id); return found ? found.coursetitle : '' }).filter(Boolean)
	form.value.coursetitles = titles.join('、')
	selectedCourseTitles.value = titles
	if (!form.value.tasktitle || form.value.tasktitle.startsWith('背诵')) {
		if (titles.length === 1) form.value.tasktitle = '背诵《' + titles[0].replace(/[《》]/g, '') + '》'
		else if (titles.length > 1) form.value.tasktitle = '背诵' + titles.length + '首古诗'
	}
	if (titles.length && (!form.value.taskcontent || form.value.taskcontent.startsWith('请完成以下古诗背诵：'))) {
		form.value.taskcontent = '请完成以下古诗背诵：' + titles.join('、') + '。'
	}
}

const resetForm = () => {
	form.value = { studentaccount:'', studentname:'', courseids:'', coursetitles:'', tasktitle:'', taskcontent:'', deadline:'', completionstatus:'待完成', completionremark:'', recitationaudio:'', completiontime:'', kaoshichengji:'', recognizedtext:'', aiscorecomment:'', teachercomment:'', teacheraccount:'', teachername:'', releasetime:'' }
	selectedCourseTitles.value = []
	selectedClassnames.value = []
}

const init = (formId=null, formType='add') => {
	resetForm()
	id.value = formId || 0
	type.value = formType
	form.value.releasetime = context?.$toolUtil.getCurDateTime()
	loadCourseOptions()
	loadClassOptions()
	if (formType == 'add') { isAdd.value = true; formTitle.value = '新增' + formName; formVisible.value = true }
	else if (formType == 'info') { isAdd.value = false; formTitle.value = '查看' + formName; getInfo() }
	else if (formType == 'edit') { isAdd.value = true; formTitle.value = '修改' + formName; getInfo() }
	else if (formType == 'review') { isAdd.value = true; formTitle.value = '批改' + formName; getInfo() }
	context?.$http({ url: `${context?.$toolUtil.storageGet('sessionTable')}/session`, method: 'get' }).then(res => {
		const json = res.data.data
		if(json.hasOwnProperty('teacheraccount')&& context?.$toolUtil.storageGet("role")!="管理员"){ form.value.teacheraccount = json.teacheraccount; disabledForm.value.teacheraccount = true }
		if(json.hasOwnProperty('teachername')&& context?.$toolUtil.storageGet("role")!="管理员"){ form.value.teachername = json.teachername; disabledForm.value.teachername = true }
	})
}

defineExpose({ init })
const closeClick = () => { formVisible.value = false }

const save = () => {
	formRef.value.validate((valid)=>{
		if(valid){
			if (isReviewMode.value) {
				context?.$http({ url: `${tableName}/update`, method: 'post', data: { id: form.value.id, kaoshichengji: form.value.kaoshichengji, teachercomment: form.value.teachercomment } }).then(()=>{
					emit('formModelChange'); context?.$toolUtil.message('批改完成','success',()=>{ formVisible.value = false })
				})
				return
			}
			if(!form.value.id && !selectedClassnames.value.length) {
				context?.$toolUtil.message('请至少选择一个管辖班级', 'error'); return
			}
			if(!form.value.id){
				// 按班级批量发布
				const batchPayload = { ...form.value, classnames: selectedClassnames.value }
				context?.$http({ url: `${tableName}/batchAssign`, method: 'post', data: batchPayload }).then((res)=>{
					const count = res.data.data || 0
					emit('formModelChange'); context?.$toolUtil.message(`已成功发布 ${count} 条背诵作业`,'success',()=>{ formVisible.value = false })
				})
				return
			}
			context?.$http({ url: `${tableName}/${!form.value.id ? "save" : "update"}`, method: 'post', data: form.value }).then(()=>{ emit('formModelChange'); context?.$toolUtil.message(`操作成功`,'success',()=>{ formVisible.value = false }) })
		}
	})
}
</script>
<style lang="scss" scoped>
:deep(.task_dialog .el-dialog) {
	border-radius: 28px; overflow: hidden;
	background: radial-gradient(circle at top right, rgba(255, 211, 146, 0.28), transparent 24%), linear-gradient(180deg, #fffdf8 0%, #ffffff 24%, #fffefb 100%);
	box-shadow: 0 28px 80px rgba(100, 78, 41, 0.18);
}
:deep(.task_dialog .el-dialog__header) { padding: 24px 30px 0; margin-right: 0; }
:deep(.task_dialog .el-dialog__title) { color: #2f2a1f; font-size: 28px; font-weight: 700; letter-spacing: 1px; }
:deep(.task_dialog .el-dialog__body) { padding: 10px 30px 24px; }
:deep(.task_dialog .el-dialog__footer) { padding: 0 30px 30px; }
.dialog_intro { border: 1px solid rgba(225, 196, 129, 0.45); border-radius: 24px; padding: 22px 24px; margin: 8px 0 20px; background: linear-gradient(135deg, rgba(255, 248, 226, 0.95) 0%, rgba(245, 252, 247, 0.96) 100%); }
.intro_badge { display: inline-flex; align-items: center; justify-content: center; padding: 6px 14px; border-radius: 999px; background: #fff; color: #7b5b17; font-size: 12px; font-weight: 700; letter-spacing: 1px; box-shadow: 0 8px 18px rgba(139, 115, 52, 0.12); }
.intro_title { margin-top: 14px; color: #2d2417; font-size: 24px; font-weight: 700; line-height: 1.4; }
.intro_desc { margin-top: 8px; color: #72654a; font-size: 14px; line-height: 1.8; }
.formModel_form { border: 0; padding: 0; margin: 0; background: transparent; }
.form_section { border: 1px solid rgba(231, 220, 194, 0.9); border-radius: 24px; padding: 24px 24px 6px; margin-bottom: 18px; background: rgba(255, 255, 255, 0.88); box-shadow: inset 0 1px 0 rgba(255,255,255,0.9); }
.section_title { margin: 0 0 22px; color: #3f3424; font-size: 17px; font-weight: 700; position: relative; padding-left: 14px; }
.section_title::before { content: ''; position: absolute; left: 0; top: 4px; width: 4px; height: 18px; border-radius: 999px; background: linear-gradient(180deg, #f3b74b 0%, #6abf73 100%); }
.formModel_form :deep(.el-form-item) { margin: 0 0 18px; display: flex; align-items: flex-start; }
.formModel_form :deep(.el-form-item__label) { color: #5b503f; font-weight: 700; padding-right: 14px; line-height: 46px; }
.formModel_form :deep(.el-form-item__content) { min-width: 0; }
.formModel_form :deep(.list_inp), .formModel_form :deep(.list_sel), .formModel_form :deep(.list_date) { width: 100%; min-width: 100%; }
.formModel_form :deep(.el-input__wrapper), .formModel_form :deep(.el-textarea__inner), .formModel_form :deep(.el-select__wrapper) { border-radius: 16px; border: 1px solid #e7dcc2; background: linear-gradient(180deg, #fffdfa 0%, #ffffff 100%); box-shadow: none; padding: 0 14px; transition: border-color .2s ease, box-shadow .2s ease, transform .2s ease; }
.formModel_form :deep(.el-input__wrapper), .formModel_form :deep(.el-select__wrapper) { min-height: 46px; }
.formModel_form :deep(.el-textarea__inner) { min-height: 112px !important; padding: 12px 14px; line-height: 1.8; }
.formModel_form :deep(.el-input__wrapper:hover), .formModel_form :deep(.el-textarea__inner:hover), .formModel_form :deep(.el-select__wrapper:hover) { border-color: #d7ba7c; }
.formModel_form :deep(.is-focus), .formModel_form :deep(.el-textarea__inner:focus) { border-color: #d8a13a !important; box-shadow: 0 0 0 4px rgba(240, 190, 89, 0.14) !important; transform: translateY(-1px); }
.formModel_form :deep(.el-input.is-disabled .el-input__wrapper), .formModel_form :deep(.el-textarea.is-disabled .el-textarea__inner) { background: linear-gradient(180deg, #f8f5ec 0%, #f4efe3 100%); color: #82745b; }
.selected_course_hint { margin-top: -6px; padding-left: 122px; color: #7c6a47; font-size: 13px; line-height: 1.8; }
.selected_course_label { color: #5fb959; font-weight: 700; margin-right: 6px; }
.audio_panel { display: flex; flex-direction: column; gap: 6px; padding: 6px 0; }
.audio_player { width: 100%; max-width: 420px; height: 44px; border-radius: 12px; }
.audio_name { color: #7c6a47; font-size: 13px; word-break: break-all; }
.audio_empty { color: #9a8d73; font-size: 13px; padding: 8px 0; }
.formModel_btn_box { display:flex; width:100%; justify-content:flex-end; align-items:center; gap: 14px; }
.formModel_cancel, .formModel_confirm { border-radius: 999px; padding: 0 28px; min-width: 120px; height: 44px; font-weight: 700; letter-spacing: 1px; }
.formModel_cancel { border: 1px solid #ddd1b5; color: #7a6d55; background: #fff; }
.formModel_confirm { border: none; color: #fff; background: linear-gradient(135deg, #e3a73c 0%, #f0bf5e 45%, #6dbe72 100%); box-shadow: 0 12px 24px rgba(109, 190, 114, 0.24); }
.formModel_confirm:hover { transform: translateY(-1px); }
@media (max-width: 1200px) { .formModel_form :deep(.el-col) { max-width: 100%; flex: 0 0 100%; } }
</style>
