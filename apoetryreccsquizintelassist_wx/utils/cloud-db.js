const db = wx.cloud ? wx.cloud.database() : null
const _ = db ? db.command : null
const ASSETS_COLL = 'poem_assets'

/**
 * 获取单首古诗的图片资源
 * @param {number} courseId - 古诗ID
 * @returns {Promise<Object|null>} { courseId, imgUrl, ... }
 */
function getPoemAsset(courseId) {
  if (!db) return Promise.resolve(null)
  return db.collection(ASSETS_COLL)
    .where({ courseId: Number(courseId) })
    .limit(1)
    .get()
    .then(res => (res.data && res.data.length > 0) ? res.data[0] : null)
    .catch(err => {
      console.error('[poem_assets] 查询失败:', err)
      return null
    })
}

/**
 * 批量获取古诗图片
 * @param {number[]} courseIds
 * @returns {Promise<Object>} { [courseId]: imgUrl, ... }
 */
function getPoemAssetsBatch(courseIds) {
  if (!db || !courseIds.length) return Promise.resolve({})
  return db.collection(ASSETS_COLL)
    .where({ courseId: _.in(courseIds) })
    .get()
    .then(res => {
      const map = {}
      if (res.data) {
        res.data.forEach(item => { map[item.courseId] = item.imgUrl || item.image || '' })
      }
      return map
    })
    .catch(err => {
      console.error('[poem_assets] 批量查询失败:', err)
      return {}
    })
}

/**
 * 插入或更新古诗图片
 * @param {number} courseId
 * @param {string} imgUrl
 */
function upsertPoemAsset(courseId, imgUrl) {
  if (!db) return Promise.resolve(null)
  return db.collection(ASSETS_COLL)
    .where({ courseId: Number(courseId) })
    .limit(1)
    .get()
    .then(res => {
      if (res.data && res.data.length > 0) {
        return db.collection(ASSETS_COLL).doc(res.data[0]._id).update({ data: { imgUrl, updateTime: new Date() } })
      } else {
        return db.collection(ASSETS_COLL).add({ data: { courseId: Number(courseId), imgUrl, createTime: new Date() } })
      }
    })
    .catch(err => { console.error('[poem_assets] upsert失败:', err) })
}

module.exports = { getPoemAsset, getPoemAssetsBatch, upsertPoemAsset }
