import request from '@/utils/request.js'

/**
 * 获取壁纸列表
 * @param {Object} params - 查询参数
 * @param {Number} params.page - 页码，默认1
 * @param {Number} params.pageSize - 每页数量，默认10
 * @param {Number} params.type - 壁纸类型（1手机壁纸、2平板壁纸、3头像）
 * @param {Number} params.categoryId - 分类ID
 * @param {Number} params.userId - 用户ID
 * @param {String} params.keyword - 关键词搜索
 */
export const getWallpaperList = (params = {}) => {
  const requestParams = {
    page: params.page || 1,
    pageSize: params.pageSize || 10
  }

  if (params.type !== undefined && params.type !== null) {
    requestParams.type = params.type
  }
  if (params.categoryId !== undefined && params.categoryId !== null) {
    requestParams.categoryId = params.categoryId
  }
  if (params.userId !== undefined && params.userId !== null && params.userId !== '') {
    requestParams.userId = params.userId
  }
  if (params.keyword !== undefined && params.keyword !== null && params.keyword !== '') {
    requestParams.keyword = params.keyword
  }

  return request({
    url: '/wallpaper/list',
    method: 'GET',
    params: requestParams
  })
}

/**
 * 获取壁纸详情
 * @param {Number} id - 壁纸ID【必填】
 * @param {Number} userId - 当前用户ID（用于判断是否喜欢/收藏）【可选】
 */
export const getWallpaperDetail = (id, userId) => {
  return request({
    url: `/wallpaper/detail/${id}`,
    method: 'GET',
    ...(userId !== undefined && userId !== null ? { params: { userId } } : {})
  })
}

/**
 * 点赞壁纸
 * @param {Number} id - 壁纸ID【必填】
 */
export const likeWallpaper = (id) => {
  return request({
    url: `/wallpaper/like/${id}`,
    method: 'POST'
  })
}

/**
 * 收藏壁纸
 * @param {Number} id - 壁纸ID【必填】
 */
export const collectWallpaper = (id) => {
  return request({
    url: `/wallpaper/collect/${id}`,
    method: 'POST'
  })
}

/**
 * 举报壁纸
 * @param {Number} id - 壁纸ID【必填】
 * @param {String} reason - 举报原因【必填】
 */
export const reportWallpaper = (id, reason) => {
  return request({
    url: `/wallpaper/report/${id}`,
    method: 'POST',
    params: {
      reason: reason
    }
  })
}

/**
 * 下载壁纸
 * @param {Number} id - 壁纸ID【必填】
 */
export const downloadWallpaper = (id) => {
  return request({
    url: `/wallpaper/download/${id}`,
    method: 'POST'
  })
}

/**
 * 获取作品集的壁纸列表
 * @param {Number} workId - 作品集ID【必填】
 * @param {Number} userId - 当前用户ID（用于判断是否喜欢/收藏）【可选】
 */
export const getWorkWallpapers = (workId, userId) => {
  return request({
    url: `/works/${workId}/wallpapers`,
    method: 'GET',
    ...(userId !== undefined && userId !== null ? { params: { userId } } : {})
  })
}