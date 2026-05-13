const cloud = require('wx-server-sdk')
cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })
const db = cloud.database()

const ARK_API_KEY = process.env.ARK_API_KEY || ''
const ARK_MODEL = process.env.ARK_MODEL || 'doubao-seedream-2-0-t2i-250112'
const ARK_URL = 'https://ark.cn-beijing.volces.com/api/v3/images/generations'

exports.main = async (event) => {
  const { poemId, poemTitle, translation } = event
  if (!poemId || !translation) {
    return { code: -1, msg: 'poemId 和 translation 必传' }
  }

  console.log('[generatePoemImage] 开始生图 poemId=' + poemId)
  console.log('[generatePoemImage] 标题=' + poemTitle)

  // 1. 构建 prompt
  const prompt = buildPrompt(poemTitle, translation)

  // 2. 调用豆包文生图 API
  let imageBuffer
  try {
    imageBuffer = await callArkAPI(prompt)
    console.log('[generatePoemImage] 图片下载成功 size=' + imageBuffer.length)
  } catch (e) {
    console.error('[generatePoemImage] API调用失败:', e.message)
    return { code: -1, msg: 'AI绘图失败: ' + e.message }
  }

  // 3. 上传到云存储
  let fileID
  try {
    const cloudPath = 'poem_images/' + poemId + '.png'
    const uploadRes = await cloud.uploadFile({
      cloudPath: cloudPath,
      fileContent: imageBuffer
    })
    fileID = uploadRes.fileID
    console.log('[generatePoemImage] 上传成功 fileID=' + fileID)
  } catch (e) {
    console.error('[generatePoemImage] 上传云存储失败:', e.message)
    return { code: -1, msg: '上传失败: ' + e.message }
  }

  // 4. 更新数据库 poem_assets
  try {
    await upsertAsset(poemId, fileID)
    console.log('[generatePoemImage] 数据库更新成功')
  } catch (e) {
    console.error('[generatePoemImage] 数据库更新失败:', e.message)
  }

  return { code: 0, data: { fileID, poemId } }
}

function buildPrompt(title, translation) {
  const text = translation.length > 200 ? translation.substring(0, 197) + '...' : translation
  return '中国风水墨画风格，根据古诗《' + title + '》的意境创作。' +
    '译文参考：' + text + '。' +
    '要求：画面唯美、留白、古风意境。' +
    'no text, no words, no letters, clean background, no watermark'
}

async function callArkAPI(prompt) {
  const https = require('https')
  const http = require('http')

  const body = JSON.stringify({
    model: ARK_MODEL,
    prompt: prompt,
    n: 1,
    size: '1024x1024'
  })

  return new Promise((resolve, reject) => {
    const url = new (require('url')).URL(ARK_URL)
    const options = {
      hostname: url.hostname,
      port: 443,
      path: url.pathname,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + ARK_API_KEY,
        'Content-Length': Buffer.byteLength(body)
      },
      timeout: 90000
    }

    const req = https.request(options, (res) => {
      let data = ''
      res.on('data', chunk => data += chunk)
      res.on('end', () => {
        console.log('[generatePoemImage] API HTTP ' + res.statusCode)
        if (res.statusCode !== 200) {
          reject(new Error('HTTP ' + res.statusCode + ': ' + data.substring(0, 300)))
          return
        }
        try {
          const json = JSON.parse(data)
          const imgUrl = json.data && json.data[0] && json.data[0].url
          if (!imgUrl) {
            reject(new Error('响应中无图片URL: ' + data.substring(0, 300)))
            return
          }
          // 下载图片
          downloadImage(imgUrl).then(resolve).catch(reject)
        } catch (e) {
          reject(new Error('解析响应失败: ' + e.message))
        }
      })
    })

    req.on('error', reject)
    req.on('timeout', () => { req.destroy(); reject(new Error('请求超时')) })
    req.write(body)
    req.end()
  })
}

function downloadImage(imgUrl) {
  const https = require('https')
  const http = require('http')
  const urlObj = new (require('url')).URL(imgUrl)
  const mod = urlObj.protocol === 'https:' ? https : http

  return new Promise((resolve, reject) => {
    mod.get(imgUrl, { timeout: 60000 }, (res) => {
      if (res.statusCode >= 300 && res.statusCode < 400) {
        // 重定向
        downloadImage(res.headers.location).then(resolve).catch(reject)
        return
      }
      if (res.statusCode !== 200) {
        reject(new Error('下载图片HTTP ' + res.statusCode))
        return
      }
      const chunks = []
      res.on('data', chunk => chunks.push(chunk))
      res.on('end', () => resolve(Buffer.concat(chunks)))
    }).on('error', reject)
  })
}

async function upsertAsset(poemId, fileID) {
  const coll = db.collection('poem_assets')
  const existing = await coll.where({ courseId: Number(poemId) }).limit(1).get()
  if (existing.data && existing.data.length > 0) {
    await coll.doc(existing.data[0]._id).update({
      data: { imageUrl: fileID, updateTime: new Date() }
    })
  } else {
    await coll.add({
      data: { courseId: Number(poemId), imageUrl: fileID, createTime: new Date() }
    })
  }
}
