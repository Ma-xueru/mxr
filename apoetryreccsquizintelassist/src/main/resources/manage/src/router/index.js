	import {
		createRouter,
		createWebHashHistory
	} from 'vue-router'
	import news from '@/views/news/list'
	import discussforum from '@/views/discussforum/list'
	import examquestion from '@/views/exam/examquestion/list'
	import teacher from '@/views/teacher/list'
	import student from '@/views/student/list'
	import exampaper from '@/views/exampaper/list'
	import mystudent from '@/views/mystudent/list'
	import admin from '@/views/admin/list'
	import forum from '@/views/forum/list'
	import transcript from '@/views/transcript/list'
	import config from '@/views/config/list'
	import feedback from '@/views/feedback/list'
	import exampaperlist from '@/views/exam/exampaperlist/list'
	import examination from '@/views/exam/exampaperlist/examination'
	import examrecord from '@/views/exam/examrecord/list'
	import examfailrecord from '@/views/exam/examfailrecord/list'
	import teacherRegister from '@/views/teacher/register'
	import teacherCenter from '@/views/teacher/center'
	import course from '@/views/course/list'
	import recitationtask from '@/views/recitationtask/list'
	import classinfo from '@/views/classinfo/list'

export const routes = [{
		path: '/login',
		name: 'login',
		component: () => import('../views/login.vue')
	},{
		path: '/',
		name: '首页',
		component: () => import('../views/index'),
		children: [{
			path: '/',
			name: '首页',
			component: () => import('../views/HomeView.vue'),
			meta: {
				affix: true
			}
		}, {
			path: '/updatepassword',
			name: '修改密码',
			component: () => import('../views/updatepassword.vue')
		}
		
		,{
			path: '/teacherCenter',
			name: '教师个人中心',
			component: teacherCenter
		}
		,{
			path: '/news',
			name: '公告信息',
			component: news
		}
		,{
			path: '/discussforum',
			name: '学习社区评论评论',
			component: discussforum
		}
		,{
			path: '/examquestion',
			name: '问答题库管理',
			component: examquestion
		}
		,{
			path: '/teacher',
			name: '教师',
			component: teacher
		}
		,{
			path: '/student',
			name: '用户',
			component: student
		}
		,{
			path: '/exampaper',
			name: '古诗词测试列表',
			component: exampaper
		}
		,{
			path: '/mystudent',
			name: '师生绑定管理',
			component: mystudent
		}
		,{
			path: '/admin',
			name: '管理员',
			component: admin
		}
		,{
			path: '/forum',
			name: '学习社区',
			component: forum
		}
		,{
			path: '/transcript',
			name: '成绩信息',
			component: transcript
		}
		,{
			path: '/config',
			name: '轮播图',
			component: config
		}
		,{
			path: '/feedback',
			name: '意见反馈',
			component: feedback
		}
		, {
			path: '/exampaperlist',
			name: '题库列表',
			component: exampaperlist
		}, {
			path: '/examrecord',
			name: '测试记录',
			component: examrecord
		}, {
			path: '/examfailrecord',
			name: '错题本',
			component: examfailrecord
		}
		,{
			path: '/course',
			name: '古诗词管理',
			component: course
		}
		,{
			path: '/recitationtask',
			name: '背诵任务',
			component: recitationtask
		}
		,{
			path: '/classinfo',
			name: '班级管理',
			component: classinfo
		}
		]
	},
	{
		path: '/teacherRegister',
		name: '教师注册',
		component: teacherRegister
	},
	{
		path: '/examination',
		name: '练习',
		component: examination
	},
]

const router = createRouter({
	history: createWebHashHistory(process.env.BASE_URL),
	routes
})

export default router
