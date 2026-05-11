
<template>
	<div>
		<div class="app-contain">
			<el-form class="userinfo_form" ref="userinfoFormRef" :model="form" label-width="$template2.back.add.form.base.labelWidth">
				<el-row>
					<el-col :span="12">
						<el-form-item label="教师账号" prop="teacheraccount">
							<el-input class="list_inp" v-model="user.teacheraccount" readonly placeholder="教师账号" clearable />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="教师密码" prop="teacherpassword">
							<el-input class="list_inp" v-model="user.teacherpassword" readonly placeholder="教师密码" clearable />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="教师姓名" prop="teachername">
							<el-input class="list_inp" v-model="user.teachername"  placeholder="教师姓名" clearable />
						</el-form-item>
					</el-col>
					<el-col :span="24">
						<el-form-item label="照片" prop="zhaopian">
							<uploads
								action="file/upload" 
								tip="请上传照片" 
								:limit="3" 
								style="width: 100%;text-align: left;"
								:fileUrls="user.zhaopian?user.zhaopian:''" 
								@change="teacherzhaopianUploadSuccess">
							</uploads>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="性别" prop="gender">
							<el-select 
								class="list_sel" 
								v-model="user.gender"
								placeholder="请选择性别"
								>
								<el-option v-for="item in teachergenderLists" :label="item" :value="item"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="联系电话" prop="lianxidianhua">
							<el-input class="list_inp" v-model="user.lianxidianhua"  placeholder="联系电话" clearable />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="可约人数" prop="reservecount">
							<el-input class="list_inp" v-model="user.reservecount"  placeholder="可约人数" clearable />
						</el-form-item>
					</el-col>
					<span class="userinfo_form_btn_box">
						<el-button class='userinfo_confirm' type="primary" @click="onSubmit">保存</el-button>
					</span>
				</el-row>
			</el-form>
		</div>
	</div>
</template>

<script setup>
	import { isNumber,isIntNumer,isEmail,isMobile,isPhone,isURL,checkIdCard } from "@/utils/toolUtil";
	import {
		reactive,
		ref,
		getCurrentInstance
	} from 'vue'
	const context = getCurrentInstance()?.appContext.config.globalProperties;
	const tableName = ref('teacher')
	const user = ref({})
	const teachergenderLists = ref([])
	const init = () => {
		teachergenderLists.value = "男,女".split(',')
	}
	const teacherzhaopianUploadSuccess=(fileUrls)=> {
	    user.value.zhaopian = fileUrls;
	}
	const onSubmit = () => {
		if((!user.value.teacheraccount)){
			context?.$toolUtil.message(`教师账号不能为空`,'error')
			return false
		}
		if((!user.value.teacherpassword)){
			context?.$toolUtil.message(`教师密码不能为空`,'error')
			return false
		}
		if((!user.value.teachername)){
			context?.$toolUtil.message(`教师姓名不能为空`,'error')
			return false
		}
		if(user.value.zhaopian!=null){
			user.value.zhaopian = user.value.zhaopian.replace(new RegExp(context?.$config.url,"g"),"");
		}
		if((user.value.lianxidianhua)&&(!context?.$toolUtil.isMobile(user.value.lianxidianhua))){
			context?.$toolUtil.message(`联系电话应输入手机格式`,'error')
			return false
		}
		if((user.value.reservecount)&&(!context?.$toolUtil.isIntNumer(user.value.reservecount))){
			context?.$toolUtil.message(`可约人数应输入整数`,'error')
			return false
		}
		context?.$http({
			url: `${tableName.value}/update`,
			method: 'post',
			data: user.value
		}).then(res => {
			context?.$toolUtil.message('修改成功','success')
		})

	}
	const getInfo = () => {
		context?.$http({
			url: `${tableName.value}/session`,
			method: 'get'
		}).then(res => {
			user.value = res.data.data
			init()
		})
	}
	getInfo()
</script>

<style lang="scss" scoped>
	// 表单
	.userinfo_form {
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
			}
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
			//下拉框样式
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
			//图片上传样式
			.el-upload-list  {
				//提示语
				.el-upload__tip {
					margin: 7px 0 0;
					color: #999;
					display: flex;
					font-size: 14px;
					justify-content: flex-start;
					align-items: center;
				}
				//外部盒子
				.el-upload--picture-card {
					border: 1px solid #ddd;
					cursor: pointer;
					background-color: #fff;
					border-radius: 0px;
					width: 120px;
					line-height: 70px;
					text-align: center;
					height: 60px;
					//图标
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
		// 按钮盒子
		.userinfo_form_btn_box {
			display: flex;
			width: 100%;
			justify-content: center;
			align-items: center;
			// 确定按钮
			.userinfo_confirm {
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
			// 确定按钮-悬浮
			.userinfo_confirm:hover {
			}
		}
	}
</style>
