<template>
	<div>
		<div class="exam_top">
			<div class="examTopLeft">
				{{examDetail.name}} (共{{questionList.length}}题)
			</div>
			<div class="examTopCenter">
				剩余时间：{{times}}
			</div>
			<div class="examTopRight">
				<el-button type="danger" @click="leaveExam">退出练习</el-button>
			</div>
		</div>
		<div class="exam_top_kong"></div>
		<el-card v-if="endType" class="score_view">
			<div class="score_item">
				练习成绩：<span class="score_num">{{scoreChange()}}</span>
			</div>
			<div class="btn_view">
				<el-button type="danger" @click="endExam">结束练习</el-button>
			</div>
		</el-card>
		<el-card class="question_list" v-if="questionList.length&&!endType">
			<div class="question">
				<div class="questionTitle">
					{{currentIndex + 1}}、{{questionList[currentIndex].questionname}}
					<el-tag type="success" v-if="questionList[currentIndex].type==0">单选题</el-tag>
					<el-tag type="warning" v-if="questionList[currentIndex].type==1">多选题</el-tag>
					<el-tag type="danger" v-if="questionList[currentIndex].type==2">判断题</el-tag>
					<el-tag type="info" v-if="questionList[currentIndex].type==3">填空题</el-tag>
				</div>
				<div class="optionList"
					v-if="questionList[currentIndex].type==0||questionList[currentIndex].type==1">
					<div class="option" v-for="(items,indexs) in questionList[currentIndex].optionList"
						:key="index">
						{{codeChange(indexs)}}：{{items.text}}
					</div>
				</div>
				<div class="answer" v-if="!submitType">
					<el-select
						v-if="questionList[currentIndex].type==2||questionList[currentIndex].type==0"
						v-model="questionList[currentIndex].myanswer" placeholder="输入答案">
						<el-option v-if="questionList[currentIndex].type==0"
							v-for="(items,indexs) in questionList[currentIndex].optionList"
							:label="codeChange(indexs)" :value="codeChange(indexs)"></el-option>
						<el-option v-if="questionList[currentIndex].type==2"
							v-for="(items,indexs) in questionList[currentIndex].optionList"
							:label="codeChange1(indexs)" :value="codeChange(indexs)"></el-option>
					</el-select>
					<el-select v-else-if="questionList[currentIndex].type==1"
						v-model="questionList[currentIndex].myanswers" placeholder="输入答案" multiple
						@change="(e)=>type1Change(e)">
						<el-option v-for="(items,indexs) in questionList[currentIndex].optionList"
							:label="codeChange(indexs)" :value="codeChange(indexs)"></el-option>
					</el-select>
					<el-input class="answer_inp" v-model="questionList[currentIndex].myanswer" placeholder="输入答案" v-else></el-input>
				</div>
				<div class="answer" v-if="submitType">
					<el-tag type="success"
						v-if="questionList[currentIndex].answer==questionList[currentIndex].myanswer"
						size="large">答案正确</el-tag>
					<el-tag type="danger"
						v-if="questionList[currentIndex].answer!=questionList[currentIndex].myanswer"
						size="large">答案错误</el-tag>
					我的答案：{{questionList[currentIndex].myanswer}}
				</div>
				<el-collapse v-if="submitType" model-value="1" class="analysis_view">
					<el-collapse-item title="查看解析" name="1">
						<div class="right_view">正确答案：{{questionList[currentIndex].answer}}</div>
						<div class="analysis">解析：{{questionList[currentIndex].analysis}}</div>
					</el-collapse-item>
				</el-collapse>
			</div>
			<div class="btn_view">
				<el-button type="success" v-if="!submitType" @click="saveClick">提交</el-button>
				<el-button type="warning" v-if="submitType" @click="nextClick">下一题</el-button>
			</div>
		</el-card>
	</div>
</template>

<script setup>
	import {
		ref,
		nextTick,
		getCurrentInstance,
		computed
	} from 'vue';
	import {
		useRoute,
		useRouter
	} from 'vue-router'
	import {
		ElMessageBox
	} from 'element-plus'
	const route = useRoute()
	const context = getCurrentInstance()?.appContext.config.globalProperties;
	//初始化
	const id = ref(0)
	const init = () => {
		if (route.query.id) {
			id.value = route.query.id
			getSession()
			getExamination()
			getQuestionList()
		}
	}
	//获取用户信息
	const user = ref({})
	const getSession = () => {
		context?.$http({
			url: `${context?.$toolUtil.storageGet('sessionTable')}/session`,
			method: 'get'
		}).then(res => {
			user.value = res.data.data
		})
	}
	//获取古诗词测试详情
	const examDetail = ref({})
	const examTime = ref(0)
	const timeInter = ref(null)
	const getExamination = () => {
		context?.$http({
			url: `exampaper/info/${id.value}`,
			method: 'get'
		}).then(res => {
			examDetail.value = res.data.data
			examTime.value = Number(res.data.data.time * 60)
			timeInter.value = setInterval(() => {
				examTime.value--
			}, 1000)
		})
	}
	const times = computed(() => {
		var time = examTime.value
		if (null != time && "" != time) {
			if (time > 60 && time < 60 * 60) {
				time =
					parseInt(time / 60.0) +
					"分钟" +
					parseInt((parseFloat(time / 60.0) - parseInt(time / 60.0)) * 60) +
					"秒";
			} else if (time >= 60 * 60 && time < 60 * 60 * 24) {
				time =
					parseInt(time / 3600.0) +
					"小时" +
					parseInt(
						(parseFloat(time / 3600.0) - parseInt(time / 3600.0)) * 60
					) +
					"分钟" +
					parseInt(
						(parseFloat(
								(parseFloat(time / 3600.0) - parseInt(time / 3600.0)) * 60
							) -
							parseInt(
								(parseFloat(time / 3600.0) - parseInt(time / 3600.0)) * 60
							)) *
						60
					) +
					"秒";
			} else if (time >= 60 * 60 * 24) {
				time =
					parseInt(time / 3600.0 / 24) +
					"天" +
					parseInt(
						(parseFloat(time / 3600.0 / 24) - parseInt(time / 3600.0 / 24)) *
						24
					) +
					"小时" +
					parseInt(
						(parseFloat(time / 3600.0) - parseInt(time / 3600.0)) * 60
					) +
					"分钟" +
					parseInt(
						(parseFloat(
								(parseFloat(time / 3600.0) - parseInt(time / 3600.0)) * 60
							) -
							parseInt(
								(parseFloat(time / 3600.0) - parseInt(time / 3600.0)) * 60
							)) *
						60
					) +
					"秒";
			} else {
				if (parseInt(time) <= 0) {
					time = "0秒";
				} else {
					time = parseInt(time) + "秒";
				}
			}
		}
		return time;
	})
	//交卷状态
	const submitType = ref(false)
	const endType = ref(false)
	//题目
	const questionList = ref([])
	const getQuestionList = () => {
		context?.$http({
			url: 'examquestion/page',
			method: 'get',
			params: {
				page: 1,
				limit: 1000,
				sort: 'sequence',
				paperid: id.value
			}
		}).then(res => {
			for (let x in res.data.data.list) {
				if (res.data.data.list[x].options) {
					res.data.data.list[x].optionList = JSON.parse(res.data.data.list[x].options)
				} else {
					res.data.data.list[x].optionList = []
				}
			}
			questionList.value = res.data.data.list
		})
	}
	//获取选项
	const codeChange = (type) => {
		let arr = ['A', 'B', 'C', 'D']
		return arr[type]
	}
	const codeChange1 = (type) => {
		let arr = ['对', '错']
		return arr[type]
	}
	//多选题
	const type1Change = (e) => {
		questionList.value[currentIndex.value].myanswers = questionList.value[currentIndex.value].myanswers.sort()
		questionList.value[currentIndex.value].myanswer = e.sort().join(',')
	}
	//退出练习
	const leaveExam = () => {
		ElMessageBox.confirm(`是否退出练习？`, '提示', {
			confirmButtonText: '是',
			cancelButtonText: '否',
			type: 'warning',
		}).then(() => {
			history.back()
		})
	}
	//结束练习
	const endExam = () => {
		history.back()
	}
	//分数统计
	const scoreChange = () => {
		let score = 0
		for (let x in questionList.value) {
			if (questionList.value[x].myanswer == questionList.value[x].answer) {
				questionList.value[x].myscore = questionList.value[x].score
			} else {
				questionList.value[x].myscore = 0
			}
			score += questionList.value[x].myscore
		}
		return score
	}
	//提交测试记录
	const saverecord = (row) => {
		let arr = {
			userid: user.value.id,
			username: user.value.studentname,
			paperid: examDetail.value.id,
			papername: examDetail.value.name,
			questionid: parseInt(row.id),
			questionname: row.questionname,
			options: row.options ? row.options : '[]',
			score: row.score,
			answer: row.answer,
			analysis: row.analysis,
			myanswer: row.myanswer,
			myscore: row.myanswer == row
				.answer ? row.score : 0,
		}
		context?.$http({
			url: 'examrecord/save',
			method: 'POST',
			data: arr
		}).then(res => {})
	}
	const currentIndex = ref(0)
	//提交
	const saveClick = () => {
		if (!questionList.value[currentIndex.value].myanswer) {
			context?.$toolUtil.message('请输入答案', 'error')
			return false
		}
		saverecord(questionList.value[currentIndex.value])
		submitType.value = true
	}
	const nextClick = () => {
		if (currentIndex.value == questionList.value.length - 1) {
			clearInterval(timeInter.value)
			endType.value = true
		} else {
			submitType.value = false
			currentIndex.value++
		}
	}
	init()
</script>

<style lang="scss" scoped>
	.exam_top {
		padding: 0 7%;
		top: 0;
		color: #333;
		left: 0;
		background: url(http://clfile.zggen.cn/20240302/7c4edb5c8eac4baea020b6d3e851a33e.jpg) no-repeat center top / 100% 100%;
		display: flex;
		width: 100%;
		justify-content: space-between;
		align-items: center;
		position: fixed;
		box-sizing: border-box;
		height: 100px;
		
		// 练习名称
		.examTopLeft {
			width: calc(100% / 3);
			font-size: 16px;
			text-align: left;
		}
		//时间
		.examTopCenter {
			width: calc(100% / 3);
			font-size: 16px;
			text-align: center;
		}
		// 退出练习盒子
		.examTopRight {
			width: calc(100% / 3);
			text-align: right;
			// 按钮
			:deep(.el-button--danger) {
				border: 0;
				cursor: pointer;
				border-radius: 20px;
				padding: 0 10px;
				margin: 0 10px 0 0;
				color: #333;
				background: rgba(255,255,255,.6);
				width: auto;
				font-size: 14px;
				height: 32px;
			}
			// 按钮悬浮
			:deep(.el-button--danger:hover) {
			}
		}
	}

	// 答题区域
	.question_list {
		border: 0px solid #eee;
		border-radius: 0px;
		box-shadow: none;
		padding: 0;
		margin: 120px auto 20px;
		background: #f6f6f6;
		width: 86%;
		text-align: left;
		// 答题盒子
		.question {
			border-radius: 0px;
			padding: 20px;
			margin: 0 0 20px;
			background: #fff;
			width: 100%;
			border-color: #eee;
			border-width: 1px;
			border-style: solid;
			// 题目
			.questionTitle {
				width: 100%;
				// 单选题
				:deep(.el-tag--success) {
					background-color: #f0f9eb;
					color: #67c23a;
					border-color: #e1f3d8;
				}
				// 判断题
				:deep(.el-tag--danger) {
					background-color: #fef0f0;
					color: #f56c6c;
					border-color: #fde2e2;
				}
				// 填空题
				:deep(.el-tag--info) {
					background-color: #f4f4f5;
					color: #909399;
					border-color: #e9e9eb;
				}
				// 多选题
				:deep(.el-tag--warning) {
					background-color: #fdf6ec;
					color: #e6a23c;
					border-color: #faecd8;
				}
			}
			// 选项列表
			.optionList {
				padding: 10px 0;
				text-indent: 2rem;

				.option {
					padding: 0 0 10px;
				}
			}
			// 答案盒子
			.answer {
				padding: 10px 0;
				// 输入框
				:deep(.answer_inp) {
					border: 1px solid #eee;
					border-radius: 0px;
					padding: 0 10px;
					background: #fff;
					width: calc(100% - 120px);
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
				:deep(.el-select) {
					border: 1px solid #eee;
					border-radius: 0px;
					padding: 0 10px;
					background: #fff;
					width: calc(100% - 120px);
					line-height: 36px;
					box-sizing: border-box;
					//去掉默认样式
					.select-trigger {
						height: 100%;
						.el-input {
							height: 100%;
							.el-input__wrapper {
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
				// 答案正确
				:deep(.el-tag--success) {
					background-color: #f0f9eb;
					color: #67c23a;
					border-color: #e1f3d8;
				}
				// 答案错误
				:deep(.el-tag--danger) {
					background-color: #fef0f0;
					color: #f56c6c;
					border-color: #fde2e2;
				}
			}
			// 解析盒子
			:deep(.analysis_view) {
				border: 1px solid #eee;
				border-radius: 0px;
				background: #f6f6f6;
				height: auto;
				// 标题
				.el-collapse-item__header {
					border: 0;
					padding: 0 10px;
					background: none;
					height: 40px;
					// 图标
					.el-icon {
						color: #999;
					}
				}
				.el-collapse-item__wrap {
					border-bottom: none;
				}
				// 内容区
				.el-collapse-item__content {
					padding: 0 0 10px;
					background: #fff;
					border-color: #eee;
					border-width: 0 0 1px;
					border-style: solid;
					// 正确答案
					.right_view {
						padding: 5px 0 0;
						color: #000;
						text-indent: 2rem;
						font-size: 14px;
					}
					// 解析
					.analysis {
						padding: 5px 0 0;
						color: #000;
						text-indent: 2rem;
						font-size: 14px;
					}
				}
			}
		}
		// 按钮盒子
		.btn_view{
			text-align: center;
			// 提交
			:deep(.el-button--success) {
				border: 0;
				cursor: pointer;
				border-radius: 0px;
				padding: 0 24px;
				margin: 0 10px 0 0;
				color: #fff;
				background: linear-gradient(270deg, rgba(246,154,40,1) 0%, rgba(255,186,101,1) 50%, rgba(246,154,40,1) 100%);
				width: auto;
				font-size: 14px;
				height: 36px;
			}
			// 提交-悬浮
			:deep(.el-button--success:hover) {
				background: linear-gradient(30deg, rgba(246,154,40,1) 0%, rgba(255,186,101,1) 50%, rgba(246,154,40,1) 100%);
			}
			// 下一题
			:deep(.el-button--warning) {
				border: 0;
				cursor: pointer;
				border-radius: 0px;
				padding: 0 24px;
				margin: 0 10px 0 0;
				color: #fff;
				background: linear-gradient(270deg, rgba(40,172,246,1) 0%, rgba(111,203,255,1) 50%, rgba(40,172,246,1) 100%);
				width: auto;
				font-size: 14px;
				height: 36px;
			}
			// 下一题-悬浮
			:deep(.el-button--warning:hover) {
				background: linear-gradient(30deg, rgba(40,172,246,1) 0%, rgba(111,203,255,1) 50%, rgba(40,172,246,1) 100%);
			}
			// 查看解析
			:deep(.el-button--primary) {
				border: 0;
				cursor: pointer;
				border-radius: 0px;
				padding: 0 24px;
				margin: 0 10px 0 0;
				color: #fff;
				background: linear-gradient(270deg, rgba(25,169,123,1) 0%, rgba(58,214,164,1) 50%, rgba(25,169,123,1) 100%),#19a97b;
				width: auto;
				font-size: 14px;
				height: 36px;
			}
			// 查看解析-悬浮
			:deep(.el-button--primary:hover) {
				background: linear-gradient(30deg, rgba(25,169,123,1) 0%, rgba(58,214,164,1) 50%, rgba(25,169,123,1) 100%),#19a97b;
			}
			// 结束练习
			:deep(.el-button--danger) {
				border: 0;
				cursor: pointer;
				border-radius: 0px;
				padding: 0 24px;
				margin: 0 10px 0 0;
				color: #fff;
				background: linear-gradient(270deg, rgba(246,40,40,1) 0%, rgba(255,107,107,1) 50%, rgba(246,40,40,1) 100%);
				width: auto;
				font-size: 14px;
				height: 36px;
			}
			// 结束练习-悬浮
			:deep(.el-button--danger:hover) {
				background: linear-gradient(30deg, rgba(246,40,40,1) 0%, rgba(255,107,107,1) 50%, rgba(246,40,40,1) 100%);
			}
		}
	}
	// 成绩盒子
	.score_view {
		border: 1px solid #eee;
		border-radius: 0;
		box-shadow: none;
		margin: 80px auto 30px;
		background: #fff;
		width: 86%;
		.score_item {
			padding: 0px 0;
			color: #19a97b;
			font-size: 22px;
			text-align: center;
		}
		.score_num {
			color: #f00;
			font-size: 22px;
		}
	}
</style>