// pages/exampaper/exam.js
const {
  info,
  exampaperlist,
  add,
  save,
  session,
  list
} = require("../../api/index.js")
const utils = require("../../utils/index.js")
const menuData = require('../../utils/menu.js');

// 计算和判断逻辑的工具类
const ExamCalculator = {
  // 计算单题分数
  calculateQuestionScore(totalScore, questionCount) {
    return questionCount > 0 ? totalScore / questionCount : 0;
  },

  // 判断答案是否正确
  isAnswerCorrect(question, userAnswer) {
    if (!question || !userAnswer) return false;
    
    // 处理多选题答案比较（排序后比较）
    if (question.type === 1) {
      const correctAnswer = Array.isArray(question.answer) 
        ? question.answer 
        : question.answer.split(',').sort();
      const normalizedUserAnswer = Array.isArray(userAnswer)
        ? userAnswer.sort()
        : userAnswer.split(',').sort();
      
      return JSON.stringify(correctAnswer) === JSON.stringify(normalizedUserAnswer);
    }
    
    // 其他题型直接比较
    return question.answer === userAnswer;
  },

  // 格式化用户答案（确保多选题按字母排序）
  formatUserAnswer(question, answer) {
    if (!question || !answer) return '';
    
    // 处理多选题答案 - 排序后用逗号分隔
    if (question.type === 1) {
      const sortedAnswer = Array.isArray(answer) ? answer.sort() : answer.split(',').sort();
      return sortedAnswer.join(',');
    }
    
    // 处理判断题答案
    if (question.type === 2 && question.options) {
      const option = question.options.find(opt => opt.code === answer);
      return option ? option.text : answer;
    }
    
    return answer;
  },

  // 判断是否是最后一题
  isLastQuestion(currentIndex, totalCount) {
    return currentIndex === totalCount - 1;
  },

  // 准备下一题的数据
  prepareNextQuestionData(currentData) {
    return {
      subShow: true,
      nextShow: false,
      analysis: false,
      error: false,
      correct: false,
      myanswer: "",
      checkedOptions: [],
      num: currentData.num + 1,
      isdisabled: false,
      showValue: false
    };
  },

  // 验证答案是否已填写
  isAnswerProvided(question, answer, checkedOptions) {
    if (question.type === 1) {
      return checkedOptions && checkedOptions.length > 0;
    }
    return !!answer;
  },

  // 准备保存的答题记录
  prepareSaveData(question, userData, userAnswer, score, paperInfo) {
    const formattedAnswer = this.formatUserAnswer(question, userAnswer);
    
    return {
      userid: userData.userid,
      username: userData.username,
      paperid: question.paperid,
      papername: (paperInfo && paperInfo.name) || question.papername || '',
      questionid: question.id,
      questionname: question.originalQuestionname || question.questionname,
      options: JSON.stringify(question.options || {}),
      score: question.score || 0,
      answer: question.answer,
      analysis: question.analysis || '',
      myscore: score,
      myanswer: formattedAnswer
    };
  }
};

Page({

  /**
   * 页面的初始数据
   */
  data: {
    curscore: 0,
    username: '',
    studentaccount: '',
    studentname: '',
    showValue: false,         // 控制多选题弹窗显示
    examList: [],
    num: 0,
    optionsCode: [],
    myanswer: "",             // 用于显示的答案
    checkedOptions: [],       // 存储多选题选中的选项（原始数据）
    info: {},
    error: false,
    correct: false,
    subShow: true,
    nextShow: false,
    reversetime: null,
    isdisabled: false,        // 是否禁用选项（提交后禁用）
    manyOptions: [],          // 多选题选项数据
    analysis: false,
    end: false,
    score: 0,
    userid: "",
    totalScore: 100           // 总分固定为100分
  },

  /**
   * 生命周期函数--监听页面加载
   */
  async onLoad(options) {
    // 获取用户信息
    let nowTable = wx.getStorageSync('nowTable');
    let sessionres = await session(nowTable);
    if (sessionres.code === 0) {
      this.setData({
        userid: sessionres.data.id,
        username: sessionres.data.studentname || '',
        studentaccount: sessionres.data.studentaccount || '',
        studentname: sessionres.data.studentname || ''
      });
    }

    // 获取试卷信息
    const id = options.id;
    const res = await info("exampaper", id);
    if (res?.data?.addtime) {
      // 启动倒计时
      utils.countdown(res?.data?.addtime, (text) => {
        this.setData({ reversetime: text });
      }, false, "hms");
    }

    // 获取题目列表
    const data = {
      page: 1,
      limit: 1000,
      sort: "sequence",
      paperid: id
    };
    const examListRes = await exampaperlist("examquestion", data);

    // 处理题目数据
    const examList = examListRes.data.list.map((v, index) => {
      // 保存原始题目名称，用于显示
      v.originalQuestionname = v.questionname || '';
      // 添加题号
      v.questionname = `${index + 1}. ${v.questionname || ''}`;
      // 解析选项并按字母排序
      if (v?.options) {
        try {
          v.options = JSON.parse(decodeURIComponent(v.options));
          // 按选项字母排序（A,B,C,D...）
          v.options.sort((a, b) => a.code.localeCompare(b.code));
        } catch (e) {
          console.error('解析选项失败:', e);
          v.options = [];
        }
      }
      return v;
    });

    if (!examList.length) {
      wx.showToast({
        title: '该专题暂未配置题目',
        icon: 'none'
      });
      setTimeout(() => {
        wx.navigateBack({
          delta: 1
        });
      }, 1200);
      return;
    }

    // 初始化第一题的选项数据
    const firstQuestionOptions = examList[0]?.options || [];
    const optionsCode = firstQuestionOptions.map(option => option.code);
    const manyOptions = optionsCode.map(v => ({ name: v, checked: false }));

    this.setData({
      info: res.data || {},
      examList,
      optionsCode,
      manyOptions
    });
  },

  /**
   * 提交答案处理
   */
  async submit() {
    const currentQuestion = this.data.examList[this.data.num];
    if (!currentQuestion) return;

    // 获取用户答案（根据题型）
    let userAnswer = this.data.myanswer;
    if (currentQuestion.type === 1) {
      userAnswer = this.data.checkedOptions;
    }

    // 验证答案是否已填写
    if (!ExamCalculator.isAnswerProvided(currentQuestion, userAnswer, this.data.checkedOptions)) {
      wx.showToast({
        title: '请选择或输入答案',
        icon: "none"
      });
      return;
    }

    // 计算题目分数
    const questionCount = this.data.examList.length;
    const questionScore = ExamCalculator.calculateQuestionScore(this.data.totalScore, questionCount);

    // 判断答案是否正确
    const isCorrect = ExamCalculator.isAnswerCorrect(currentQuestion, userAnswer);
    
    // 更新分数和答题状态
    const newScore = isCorrect ? this.data.score + questionScore : this.data.score;
    this.setData({
      correct: isCorrect,
      error: !isCorrect,
      score: newScore,
      curscore: isCorrect ? questionScore : 0
    });

    // 准备保存的答题记录
    const saveData = ExamCalculator.prepareSaveData(
      currentQuestion,
      { userid: this.data.userid, username: this.data.username },
      userAnswer,
      isCorrect ? questionScore : 0,
      this.data.info
    );

    // 保存答题记录
    await save("examrecord", saveData);

    // 判断是否是最后一题
    const isLast = ExamCalculator.isLastQuestion(this.data.num, this.data.examList.length);

    if (isLast) {
      await this.saveTranscriptRecord(Math.round(newScore))
    }
    
    // 更新按钮状态
    this.setData({
      subShow: isLast,
      nextShow: !isLast,
      isdisabled: true,
      end: isLast
    });
  },

  async saveTranscriptRecord(totalScore) {
    try {
      let teacheraccount = ''
      let teachername = ''
      if (this.data.studentaccount) {
        const relationRes = await list('mystudent', {
          page: 1,
          limit: 1,
          studentaccount: this.data.studentaccount
        })
        const relation = relationRes?.data?.list?.[0] || {}
        teacheraccount = relation.teacheraccount || ''
        teachername = relation.teachername || ''
      }

      await add('transcript', {
        studentaccount: this.data.studentaccount || '',
        studentname: this.data.studentname || this.data.username || '',
        kaoshichengji: Number(totalScore || 0),
        teacheraccount,
        teachername,
        releasetime: utils.getCurrentDate("yMDhms")
      })
    } catch (error) {
      console.error('保存成绩信息失败:', error)
    }
  },

  /**
   * 查看解析
   */
  analysisTap() {
    this.setData({
      analysis: !this.data.analysis
    });
  },

  /**
   * 处理多选题子组件返回的结果（弹窗选择后）
   */
  acceptChild(e) {
    let selectedOptions = e.detail.data || [];
    // 按字母正序排序
    selectedOptions.sort((a, b) => a.localeCompare(b));
    
    this.setData({
      checkedOptions: selectedOptions,
      myanswer: selectedOptions.join(','),
      showValue: false  // 关闭弹窗
    });
  },

  /**
   * 切换到下一题
   */
  nextTap() {
    const nextData = ExamCalculator.prepareNextQuestionData(this.data);
    
    // 更新下一题的选项数据
    const nextQuestion = this.data.examList[nextData.num];
    if (nextQuestion?.options) {
      nextData.optionsCode = nextQuestion.options.map(option => option.code);
      nextData.manyOptions = nextData.optionsCode.map(v => ({ name: v, checked: false }));
    }
    
    this.setData(nextData);
  },

  /**
   * 退出练习
   */
  quitTap() {
    wx.navigateBack({
      detail: 1
    });
  },

  /**
   * 单选题和判断题答案变更（下拉选择器）
   */
  answerChange(e) {
    const currentQuestion = this.data.examList[this.data.num];
    if (!currentQuestion || !(currentQuestion.type === 0 || currentQuestion.type === 2)) return;
    
    const selectedIndex = e.detail.value;
    const selectedAnswer = this.data.optionsCode[selectedIndex];
    this.setData({
      myanswer: selectedAnswer
    });
  },

  /**
   * 单选题和判断题选项点击
   */
  selectOption(e) {
    const currentQuestion = this.data.examList[this.data.num];
    if (this.data.isdisabled || !(currentQuestion.type === 0 || currentQuestion.type === 2)) return;
    
    const selectedCode = e.currentTarget.dataset.code;
    this.setData({
      myanswer: selectedCode
    });
  },

  /**
   * 多选题选项点击 - 直接修改选中状态，不弹窗
   */
  toggleMultipleOption(e) {
    const currentQuestion = this.data.examList[this.data.num];
    if (this.data.isdisabled || currentQuestion.type !== 1) return;
    
    const selectedCode = e.currentTarget.dataset.code;
    let currentOptions = [...this.data.checkedOptions];
    const index = currentOptions.indexOf(selectedCode);
    
    // 切换选中状态
    if (index === -1) {
      currentOptions.push(selectedCode);  // 选中
    } else {
      currentOptions.splice(index, 1);   // 取消选中
    }
    
    // 按字母正序排序
    currentOptions.sort((a, b) => a.localeCompare(b));
    
    this.setData({
      checkedOptions: currentOptions,
      myanswer: currentOptions.join(',')  // 用逗号分隔显示
    });
  },
  
  /**
   * 显示多选题弹窗选择器（仅点击输入框时触发）
   */
  manyTap() {
    const currentQuestion = this.data.examList[this.data.num];
    if (this.data.isdisabled || currentQuestion.type !== 1) return;
    
    // 更新选项的选中状态
    const updatedManyOptions = this.data.manyOptions.map(option => ({
      ...option,
      checked: this.data.checkedOptions.includes(option.name)
    }));
    
    this.setData({
      manyOptions: updatedManyOptions,
      showValue: true  // 显示弹窗
    });
  },
  
  /**
   * 获取题型名称
   */
  getQuestionTypeName(type) {
    switch(type) {
      case 0: return '单选题';
      case 1: return '多选题';
      case 2: return '判断题';
      case 3: return '填空题';
      default: return '未知题型';
    }
  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {
    utils.countdown("", "", true);
  }

  // 其他生命周期函数保持默认实现
})
