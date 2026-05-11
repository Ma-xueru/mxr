const {
  session,
  list,
  add,
  update
} = require("../../api/index.js")
const utils = require("../../utils/index.js")

Page({
  data: {
    userInfo: {},
    classmates: [],
    pkRecords: [],
    classMedalCount: 0,
    baseURL: wx.getStorageSync('baseURL') + '/',
    loading: false,
    activeOpponent: null,
    quizQuestions: [],
    quizReady: false,
    submitting: false,
    answeredCount: 0,
    progressWidth: 0
  },

  onLoad() {
    this.loadData()
  },

  onShow() {
    this.loadData()
  },

  async loadData() {
    try {
      const sessionRes = await session('student')
      const userInfo = sessionRes.data || {}
      this.setData({ userInfo })
      if (!userInfo.id) return
      if (!userInfo.classname) {
        this.setData({
          classmates: [],
          pkRecords: [],
          classMedalCount: Number(userInfo.medalcount || 0)
        })
        return
      }

      const classmatesRes = await list('student', {
        page: 1,
        limit: 1000,
        classname: userInfo.classname
      })
      const classmates = (classmatesRes.data.list || []).filter(item => item.id !== userInfo.id)

      const pkRes = await list('classpk', {
        page: 1,
        limit: 100,
        classname: userInfo.classname
      })
      const pkRecords = (pkRes.data.list || []).filter(item =>
        item.studentaccount === userInfo.studentaccount || item.opponentaccount === userInfo.studentaccount
      ).sort((a, b) => new Date(b.pktime || b.addtime || 0) - new Date(a.pktime || a.addtime || 0))

      const classMedalCount = (classmatesRes.data.list || []).reduce((sum, item) => sum + Number(item.medalcount || 0), 0)

      this.setData({
        classmates,
        pkRecords,
        classMedalCount
      })
    } catch (error) {
      wx.showToast({
        title: '加载PK数据失败',
        icon: 'none'
      })
    }
  },

  async startPk(e) {
    const opponent = e.currentTarget.dataset.item
    const userInfo = this.data.userInfo
    if (!opponent || !userInfo.id) return
    if (!userInfo.classname) {
      wx.showToast({
        title: '请先完善班级信息',
        icon: 'none'
      })
      return
    }

    this.setData({ loading: true })
    wx.showLoading({ title: '抽题中...' })
    try {
      const quizQuestions = await this.fetchQuizQuestions()
      this.setData({
        activeOpponent: opponent,
        quizQuestions,
        quizReady: true,
        answeredCount: 0,
        progressWidth: 0
      })
      wx.pageScrollTo({
        scrollTop: 280,
        duration: 300
      })
    } catch (error) {
      wx.showToast({
        title: error && error.message ? error.message : '生成题目失败',
        icon: 'none'
      })
    } finally {
      wx.hideLoading()
      this.setData({ loading: false })
    }
  },

  async fetchQuizQuestions() {
    const questionRes = await list('examquestion', {
      page: 1,
      limit: 1000
    })
    let questions = (questionRes.data.list || []).map(item => this.normalizeQuestion(item))
      .filter(item => item.questionname && item.answer)

    if (questions.length < 5) {
      throw new Error('题库题目不足5道')
    }

    questions = this.shuffleList(questions)

    return questions.slice(0, 5).map((item, index) => ({
      ...item,
      order: index + 1
    }))
  },

  normalizeQuestion(item) {
    let options = []
    try {
      options = item.options ? JSON.parse(item.options) : []
    } catch (error) {
      options = []
    }
    if (!Array.isArray(options)) {
      options = []
    }
    options = options.sort((a, b) => String(a.code || '').localeCompare(String(b.code || '')))
    if (Number(item.type) === 2 && !options.length) {
      options = [
        { text: '对', code: 'A' },
        { text: '错', code: 'B' }
      ]
    }
    return {
      id: item.id,
      questionname: item.questionname,
      answer: String(item.answer || ''),
      analysis: item.analysis || '',
      type: Number(item.type),
      options,
      selectedAnswer: Number(item.type) === 1 ? [] : '',
      inputAnswer: '',
      answered: false
    }
  },

  shuffleList(listData) {
    const arr = [...listData]
    for (let i = arr.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1))
      const temp = arr[i]
      arr[i] = arr[j]
      arr[j] = temp
    }
    return arr
  },

  chooseAnswer(e) {
    const index = Number(e.currentTarget.dataset.index)
    const code = e.currentTarget.dataset.code
    const quizQuestions = [...this.data.quizQuestions]
    const currentQuestion = quizQuestions[index]
    if (!currentQuestion) return

    if (currentQuestion.type === 1) {
      const selectedList = Array.isArray(currentQuestion.selectedAnswer) ? [...currentQuestion.selectedAnswer] : []
      const selectedIndex = selectedList.indexOf(code)
      if (selectedIndex > -1) {
        selectedList.splice(selectedIndex, 1)
      } else {
        selectedList.push(code)
      }
      selectedList.sort()
      currentQuestion.selectedAnswer = selectedList
    } else {
      currentQuestion.selectedAnswer = code
    }
    currentQuestion.answered = this.isQuestionAnswered(currentQuestion)

    this.updateQuizProgress(quizQuestions)
  },

  inputAnswer(e) {
    const index = Number(e.currentTarget.dataset.index)
    const value = String(e.detail.value || '').trim()
    const quizQuestions = [...this.data.quizQuestions]
    if (!quizQuestions[index]) return
    quizQuestions[index].inputAnswer = value
    quizQuestions[index].selectedAnswer = value
    quizQuestions[index].answered = this.isQuestionAnswered(quizQuestions[index])
    this.updateQuizProgress(quizQuestions)
  },

  updateQuizProgress(quizQuestions) {
    const answeredCount = quizQuestions.filter(item => this.isQuestionAnswered(item)).length
    this.setData({
      quizQuestions,
      answeredCount,
      progressWidth: answeredCount * 20
    })
  },

  isQuestionAnswered(question) {
    if (question.type === 1) {
      return Array.isArray(question.selectedAnswer) && question.selectedAnswer.length > 0
    }
    return !!String(question.selectedAnswer || '').trim()
  },

  cancelQuiz() {
    this.setData({
      activeOpponent: null,
      quizQuestions: [],
      quizReady: false,
      submitting: false,
      answeredCount: 0,
      progressWidth: 0
    })
  },

  async submitQuiz() {
    const { quizQuestions, activeOpponent, userInfo } = this.data
    if (!activeOpponent || !quizQuestions.length) {
      return
    }
    const unanswered = quizQuestions.some(item => !this.isQuestionAnswered(item))
    if (unanswered) {
      wx.showToast({
        title: '请先完成 5 道题目',
        icon: 'none'
      })
      return
    }

    this.setData({ submitting: true })
    wx.showLoading({ title: '结算中...' })
    try {
      const myScore = quizQuestions.reduce((sum, item) => sum + (this.isAnswerCorrect(item) ? 1 : 0), 0)
      const opponentScore = this.simulateOpponentScore(activeOpponent, quizQuestions)
      const meWin = myScore > opponentScore
      const isDraw = myScore === opponentScore
      const winner = meWin ? userInfo : activeOpponent

      if (meWin) {
        const winnerStudent = Object.assign({}, userInfo, {
          medalcount: Number(userInfo.medalcount || 0) + 1
        })
        await update('student', winnerStudent)
      }

      await add('classpk', {
        studentaccount: userInfo.studentaccount,
        studentname: userInfo.studentname,
        classname: userInfo.classname,
        opponentaccount: activeOpponent.studentaccount,
        opponentname: activeOpponent.studentname,
        myscore: myScore,
        opponentscore: opponentScore,
        winneraccount: isDraw ? '' : winner.studentaccount,
        winnername: isDraw ? '平局' : winner.studentname,
        medalreward: meWin ? 1 : 0,
        pkstatus: isDraw ? '平局' : (meWin ? '胜利' : '惜败'),
        pktime: utils.getCurrentDate("yMDhms")
      })

      wx.showModal({
        title: isDraw ? 'PK平局' : (meWin ? 'PK胜利' : 'PK结果'),
        content: isDraw
          ? `你和 ${activeOpponent.studentname} 都答对了 ${myScore} 题，本场平局。`
          : (meWin
            ? `你答对 ${myScore} 题，${activeOpponent.studentname} 答对 ${opponentScore} 题，获得 1 枚勋章。`
            : `你答对 ${myScore} 题，${activeOpponent.studentname} 答对 ${opponentScore} 题，再接再厉。`),
        showCancel: false
      })

      this.cancelQuiz()
      await this.loadData()
    } catch (error) {
      wx.showToast({
        title: 'PK提交失败，请稍后重试',
        icon: 'none'
      })
    } finally {
      wx.hideLoading()
      this.setData({ submitting: false })
    }
  },

  isAnswerCorrect(question) {
    const answer = String(question.answer || '').trim()
    if (question.type === 1) {
      const correctAnswer = answer.split(',').map(item => item.trim()).filter(Boolean).sort()
      const selectedAnswer = (Array.isArray(question.selectedAnswer) ? question.selectedAnswer : [])
        .map(item => String(item).trim()).filter(Boolean).sort()
      return JSON.stringify(correctAnswer) === JSON.stringify(selectedAnswer)
    }
    if (question.type === 3) {
      return String(question.selectedAnswer || '').trim() === answer
    }
    return String(question.selectedAnswer || '').trim() === answer
  },

  simulateOpponentScore(opponent, quizQuestions) {
    const medalFactor = Math.min(Number(opponent.medalcount || 0), 8) * 0.04
    const baseRate = Math.min(0.82, 0.42 + medalFactor)
    let score = 0
    quizQuestions.forEach(() => {
      if (Math.random() < baseRate) {
        score += 1
      }
    })
    return score
  },

  getQuestionTypeText(type) {
    const map = {
      0: '单选题',
      1: '多选题',
      2: '判断题',
      3: '填空题'
    }
    return map[type] || '题目'
  }
})
