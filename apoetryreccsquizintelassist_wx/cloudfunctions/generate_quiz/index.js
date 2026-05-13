const cloud = require('wx-server-sdk')
cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })

const API_KEY = process.env.DEEPSEEK_API_KEY || ''
const MODEL = process.env.DEEPSEEK_MODEL || 'deepseek-v4-flash'
const API_URL = 'https://api.deepseek.com/chat/completions'

exports.main = async (event) => {
  const { poemTitle, poemContent } = event
  if (!poemTitle || !poemContent) {
    return { code: -1, msg: 'poemTitle 和 poemContent 必传' }
  }

  console.log('[generate_quiz] 出题 poemTitle=' + poemTitle)
  console.log('[generate_quiz] 原文长度=' + poemContent.length)

  // 截断过长内容避免 token 浪费
  const content = poemContent.length > 400 ? poemContent.substring(0, 397) + '...' : poemContent

  const systemPrompt = `你是一名资深小学语文老师，擅长根据古诗内容设计适合小学生的选择题。

【核心要求】
1. 请针对提供的古诗，生成 5 道单选题。
2. 考查维度分布：字词解释(2题)、诗句理解(2题)、情感/背景(1题)。
3. 每题4个选项(A/B/C/D)，只有1个正确答案。
4. 选项要有迷惑性但不能太难，适合小学生。

【输出格式】
必须严格只返回 JSON 数组，严禁任何说明文字、Markdown标记或代码块。
格式如下：
[{"question":"题?","options":["A选项","B选项","C选项","D选项"],"answer":0,"analysis":"解析"}]`

  const userPrompt = `古诗标题：《${poemTitle}》\n古诗原文：\n${content}`

  try {
    const resp = await callDeepSeek(systemPrompt, userPrompt)
    console.log('[generate_quiz] 原始响应长度=' + resp.length)

    // 清洗 JSON — 去除可能的 Markdown 包裹和乱码
    let json = cleanJSON(resp)
    console.log('[generate_quiz] 清洗后长度=' + json.length)

    // 解析验证
    const questions = JSON.parse(json)
    if (!Array.isArray(questions) || questions.length === 0) {
      return { code: -1, msg: 'AI 返回的题目数量为0' }
    }

    // 验证每道题结构
    for (let i = 0; i < questions.length; i++) {
      const q = questions[i]
      if (!q.question || !Array.isArray(q.options) || q.options.length < 4 ||
          typeof q.answer !== 'number' || q.answer < 0 || q.answer > 3) {
        return { code: -1, msg: '第' + (i + 1) + '题格式异常: ' + JSON.stringify(q).substring(0, 100) }
      }
      if (!q.analysis) q.analysis = ''
    }

    console.log('[generate_quiz] 成功 题目数=' + questions.length)
    return { code: 0, data: questions }
  } catch (e) {
    console.error('[generate_quiz] 失败:', e.message || e)
    return { code: -1, msg: '出题失败: ' + (e.message || e).substring(0, 200) }
  }
}

function cleanJSON(str) {
  let s = str.trim()
  // 去掉 ```json ... ``` 包裹
  const mdMatch = s.match(/```(?:json)?\s*([\s\S]*?)```/)
  if (mdMatch) s = mdMatch[1].trim()
  // 找到第一个 [ 和最后一个 ]
  const start = s.indexOf('[')
  const end = s.lastIndexOf(']')
  if (start >= 0 && end > start) s = s.substring(start, end + 1)
  return s
}

async function callDeepSeek(systemPrompt, userPrompt) {
  const https = require('https')
  const body = JSON.stringify({
    model: MODEL,
    messages: [
      { role: 'system', content: systemPrompt },
      { role: 'user', content: userPrompt }
    ],
    temperature: 0.7,
    max_tokens: 2000,
    stream: false
  })

  return new Promise((resolve, reject) => {
    const url = new (require('url')).URL(API_URL)
    const options = {
      hostname: url.hostname,
      port: 443,
      path: url.pathname,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + API_KEY,
        'Content-Length': Buffer.byteLength(body)
      },
      timeout: 90000
    }

    const req = https.request(options, (res) => {
      let data = ''
      res.on('data', chunk => data += chunk)
      res.on('end', () => {
        if (res.statusCode !== 200) {
          reject(new Error('HTTP ' + res.statusCode + ': ' + data.substring(0, 300)))
          return
        }
        try {
          const json = JSON.parse(data)
          const content = json.choices[0].message.content
          resolve(content)
        } catch (e) {
          reject(new Error('解析API响应失败: ' + e.message))
        }
      })
    })
    req.on('error', reject)
    req.on('timeout', () => { req.destroy(); reject(new Error('请求超时')) })
    req.write(body)
    req.end()
  })
}
