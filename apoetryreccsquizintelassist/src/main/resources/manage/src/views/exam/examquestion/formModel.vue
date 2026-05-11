
<template>
	<div>
		<el-dialog v-model="formVisible" :title="formTitle" width="70%" destroy-on-close :fullscreen='false'>
			<el-form class="formModel_form" ref="formRef" :model="form" label-width="$template2.back.add.form.base.labelWidth" :rules="rules">
				<el-row>
					<el-col :span="12">
						<el-form-item label="古诗词测试" prop="paperid">
							<el-select class="list_sel" :disabled="!isAdd" v-model="form.paperid" placeholder="古诗词测试名称" style="width:100%"
								@change="paperChange">
								<el-option v-for="(item,index) in paperList" :key="index" :label="item.name"
									:value="item.id"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="题目类型" prop="type">
							<el-select class="list_sel" :disabled="!isAdd" v-model="form.type" placeholder="题目类型" style="width:100%" @change="typeChange">
								<el-option v-for="(item,index) in typeList" :key="index" :label="item"
									:value="index"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="题目题目" prop="questionname">
							<el-input :readonly="!isAdd" v-model="form.questionname" placeholder="题目题目" type="textarea"></el-input>
						</el-form-item>
					</el-col>
					<!-- 单选题 -->
					<el-col :span="24" v-if="form.type==0">
						<el-form-item label="选项" prop="optionList">
							<el-input class="list_inpGroup" :readonly="!isAdd" v-for="(item,index) in form.optionList" :key="index" v-model="item.text"
								style="margin-bottom: 6px;" :placeholder="`选项${codeChange(index)}`">
								<template #prepend>{{codeChange(index)}}</template>
								<template #append v-if="isAdd">
									<el-button :icon="Delete" @click.native="delOption(index)" />
								</template>
							</el-input>
							<div style="width: 100%;text-align: center;padding-top: 10px;"
								v-if="form.optionList.length<4&&isAdd"><el-button circle :icon="Plus" type="success"
									@click.native="addOption"></el-button></div>
						</el-form-item>
					</el-col>
					<el-col :span="12" v-if="form.type==0">
						<el-form-item label="答案" prop="answer">
							<el-select class="list_sel" :disabled="!isAdd" v-model="form.answer" placeholder="答案">
								<el-option v-for="(item,index) in form.optionList" :label="codeChange(index)"
									:value="codeChange(index)"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<!-- 单选题 -->
					<!-- 多选题 -->
					<el-col :span="12" v-if="form.type==1">
						<el-form-item label="选项" prop="optionList">
							<el-input class="list_inpGroup" :readonly="!isAdd" v-for="(item,index) in form.optionList" :key="index" v-model="item.text"
								style="margin-bottom: 6px;" :placeholder="`选项${codeChange(index)}`">
								<template #prepend>{{codeChange(index)}}</template>
								<template #append v-if="isAdd">
									<el-button :icon="Delete" @click.native="delOption(index)" />
								</template>
							</el-input>
							<div style="width: 100%;text-align: center;padding-top: 10px;"
								v-if="form.optionList.length<4&&isAdd"><el-button circle :icon="Plus" type="success"
									@click.native="addOption"></el-button></div>
						</el-form-item>
					</el-col>
					<el-col :span="12" v-if="form.type==1">
						<el-form-item label="答案" prop="answers">
							<el-select class="list_sel" :disabled="!isAdd" v-model="form.answers" placeholder="答案" multiple @change="answerChange">
								<el-option v-for="(item,index) in form.optionList" :label="codeChange(index)"
									:value="codeChange(index)"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<!-- 多选题 -->
					<!-- 判断题 -->
					<el-col :span="12" v-if="form.type==2">
						<el-form-item label="答案" prop="answers">
							<el-select class="list_sel" :disabled="!isAdd" v-model="form.answer" placeholder="答案">
								<el-option v-for="(item,index) in form.optionList" :label="codeChange1(index)"
									:value="codeChange1(index)"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<!-- 判断题 -->
					<!-- 填空题 -->
					<el-col :span="12" v-if="form.type==3">
						<el-form-item label="答案" prop="answers">
							<el-input :readonly="!isAdd" v-model="form.answer" type="textarea" placeholder="答案"></el-input>
						</el-form-item>
					</el-col>
					<!-- 填空题 -->
					<el-col :span="24">
						<el-form-item label="解析" prop="analysis">
							<el-input :readonly="!isAdd" v-model="form.analysis" placeholder="解析" type="textarea"></el-input>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="分值" prop="score">
							<el-input class="list_inp" :readonly="!isAdd" v-model="form.score" placeholder="分值" type="number"></el-input>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="排序" prop="sequence">
							<el-input class="list_inp" :readonly="!isAdd" v-model="form.sequence" placeholder="排序" type="number" :min="1"
								:max="20"></el-input>
						</el-form-item>
					</el-col>
				</el-row>
			</el-form>
			<template #footer v-if="isAdd">
				<span class="formModel_btn_box">
					<el-button class="formModel_cancel" @click="closeClick">取消</el-button>
					<el-button class="formModel_confirm" type="primary" @click="save">
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
	import {
		Delete,
		Plus
	} from '@element-plus/icons-vue'
	const context = getCurrentInstance()?.appContext.config.globalProperties;
	const emit = defineEmits(['formModelChange'])
	//基础信息
	const tableName = 'examquestion'
	const formName = '问答题库'
	//基础信息
	//form表单
	const form = ref({
		papername: '',
		paperid: '',
		questionname: '',
		answer: '',
		analysis: '',
		score: '',
		sequence: '',
		type: '',
		optionList: [{
			text: '',
		},{
			text: '',
		}],
	})
	const disabledForm = ref({
		name: false,
		time: false,
		status: false,
	})
	const formVisible = ref(false)
	const formTitle = ref('')
	//表单验证
	//匹配数字
	const validateNumber = (rule, value, callback) => {
		if (!value) {
			callback();
		} else if (!context?.$toolUtil.isNumber(value)) {
			callback(new Error("请输入数字"));
		} else {
			callback();
		}
	}
	const rules = ref({
		paperid: [{
			required: true,
			message: '请选择古诗词测试题目',
			trigger: 'blur'
		}, ],
		type: [{
			required: true,
			message: '请选择题目类型',
			trigger: 'blur'
		}, ],
		score: [{
			required: true,
			message: '请输入分值',
			trigger: 'blur'
		}, ],
		questionname: [{
			required: true,
			message: '请输入题目题目',
			trigger: 'blur'
		}, ],
		analysis: [{
			required: true,
			message: '请输入解析',
			trigger: 'blur'
		}, ],
		sequence: [{
			required: true,
			message: '请输入排序',
			trigger: 'blur'
		}, ],
	})
	//表单验证
	const formRef = ref(null)
	const id = ref(0)
	//获取唯一标识
	const getUUID = () => {
		return new Date().getTime();
	}
	//重置
	const resetForm = () => {
		form.value = {
			papername: '',
			paperid: '',
			questionname: '',
			answer: '',
			analysis: '',
			score: '',
			sequence: '',
			type: '',
			optionList: [{
				text: '',
			},{
				text: '',
			}],
		}
	}
	//获取info
	const getInfo = () => {
		context?.$http({
			url: `${tableName}/info/${id.value}`,
			method: 'get'
		}).then(res => {
			if (res.data.data.options) {
				res.data.data.optionList = JSON.parse(res.data.data.options)
			} else {
				res.data.data.optionList = []
			}
			if (res.data.data.type == 1) {
				res.data.data.answers = res.data.data.answer.split(',')
			}
			form.value = res.data.data
			formVisible.value = true
		})
	}
	//初始化
	const isAdd = ref(true)
	const init = (refid = null, text = null) => {
		formTitle.value = text + formName
		resetForm()
		getPaperList()
		if (refid) {
			id.value = refid
			getInfo()
		}
		if(text=='查看'){
			isAdd.value = false
		}else{
			isAdd.value = true
		}
		formVisible.value = true
	}
	//初始化
	//获取古诗词测试列表
	const paperList = ref([])
	const getPaperList = () => {
		context?.$http({
			url: 'exampaper/page',
			method: 'get',
			params: {
				page: 1,
				limit: 1000
			}
		}).then(res => {
			paperList.value = res.data.data.list
		})
	}
	const paperChange = (e) => {
		for (let x in paperList.value) {
			if (paperList.value[x].id == e) {
				form.value.papername = paperList.value[x].name
			}
		}
	}
	//题目类型
	const typeList = ref(['单选题', '多选题', '判断题', '填空题'])
	const typeChange = (e) => {
		if (e == 3) {
			form.value.optionList = []
		} else if (e == 2) {
			form.value.optionList = [{
				text: '对'
			}, {
				text: '错'
			}]
		} else {
			form.value.optionList = [{
				text: ''
			}, {
				text: ''
			}]
		}
		nextTick(() => {
			form.value.answer = ''
			form.value.answers = []

		})
	}
	const codeChange1 = (type) => {
		let arr = ['对', '错']
		return arr[type]
	}
	//多选答案处理
	const answerChange = () => {
		form.value.answer = form.value.answers.join(',')
	}
	const codeChange = (type) => {
		let arr = ['A', 'B', 'C', 'D']
		return arr[type]
	}
	//删除选项
	const delOption = (index) => {
		form.value.optionList.splice(index, 1)
	}
	//新增选项
	const addOption = () => {
		form.value.optionList.push({
			text: '',
		})
	}
	//声明父级调用
	defineExpose({
		init
	})
	//关闭
	const closeClick = () => {
		formVisible.value = false
	}
	//提交
	const save = () => {
		formRef.value.validate((valid) => {
			if (valid) {
				if (form.value.type == 0 || form.value.type == 1 || form.value.type == 2) {
					if (form.value.optionList.length > 1) {
						for(let x in form.value.optionList){
							form.value.optionList[x].code = codeChange(x)
						}
						form.value.options = JSON.stringify(form.value.optionList)
					} else {
						context?.$toolUtil.message('至少输入一个选项', 'error')
						return false
					}
				} else {
					form.value.options = ''
				}
				if (form.value.answer == '') {
					context?.$toolUtil.message('请输入答案', 'error')
					return false
				}
				context?.$http({
					url: `${tableName}/${!form.value.id ? "save" : "update"}`,
					method: 'post',
					data: form.value
				}).then(res => {
					context?.$toolUtil.message(`操作成功`, 'success', () => {
						formVisible.value = false
						emit('formModelChange')
					})
				})
			}
		})
	}
</script>
<style lang="scss" scoped>
	// 表单
	.formModel_form{
		border: 0px solid #ddd;
		border-radius: 4px;
		padding: 30px;
		margin: 0;
		background: #fff;
		// form item
		:deep(.el-form-item) {
			margin: 0 150px 20px 0;
			background: none;
			display: flex;
			// 内容盒子
			.el-form-item__content{
				display: flex;
				width: calc(100% - 120px);
				justify-content: flex-start;
				align-items: center;
				flex-wrap: wrap;

				// 输入框
				.list_inp {
					border: 1px solid #ddd;
					border-radius: 0px;
					padding: 0 10px;
					width: auto;
					line-height: 36px;
					box-sizing: border-box;
					height: 36px;
					//去掉默认样式
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
				// 下拉框
				.list_sel {
					border: 1px solid #ddd;
					border-radius: 0px;
					padding: 0 10px;
					width: auto;
					line-height: 36px;
					box-sizing: border-box;
					min-width: 200px;
					//去掉默认样式
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
							}
							.is-focus {
								box-shadow: none !important;
							}
						}
					}
				}
				// 长文本
				.el-textarea__inner {
					border: 1px solid #ddd;
					border-radius: 0;
					padding: 12px;
					color: #333;
					width: auto;
					font-size: 14px;
					min-height: 180px;
					min-width: 800px;
				}
				// 选项输入框
				.list_inpGroup {
					border: 0px solid #ddd;
					padding: 0;
					width: auto;
					line-height: 36px;
					box-sizing: border-box;
					height: 36px;
				
					.el-input-group__prepend {
						border: 1px solid #ddd;
						border-radius: 0px 0 0 0px;
						padding: 0 20px;
						color: #f00;
						background: #f6f6f6;
						border-width: 1px 0 1px 1px;
					}
				
					.el-input__wrapper {
						border: 1px solid #ddd;
						padding: 0 10px;
					}
				
					.el-input-group__append {
						border: 1px solid #ddd;
						border-radius: 0 0px 0px 0;
						padding: 0 20px;
						color: #f00;
						background: #f6f6f6;
						border-width: 1px 1px 1px 0;
				
						.el-input {
							padding: 0 10px;
							background: #fff;
							width: 120px;
							//去掉默认样式
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
				
						.el-button {
							padding: 0;
							width: 70px;
							font-size: 18px;
						}
					}
				}
			}
		}
	}

</style>
