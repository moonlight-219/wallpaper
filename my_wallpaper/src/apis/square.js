import request from '@/utils/request.js'

/**
 * 获取广场作品集列表
 * @param {Object} params - 查询参数
 * @param {Number} params.page - 页码，默认1
 * @param {Number} params.pageSize - 每页数量，默认10
 * @param {Number} params.sortType - 排序类型：0-时间，1-点赞，2-收藏，默认0
 * @param {Number} params.categoryId - 分类ID（可选）
 * @param {String} params.keyword - 关键词（可选）
 * @param {Number} params.status - 状态（可选）
 * @param {Number} params.userId - 用户ID（可选）
 */
export const getSquareWorks = (params = {}) => {
  const requestParams = {
    page: params.page || 1,
    pageSize: params.pageSize || 10,
    sortType: params.sortType !== undefined ? params.sortType : 0
  }

  if (params.categoryId !== undefined && params.categoryId !== null) {
    requestParams.categoryId = params.categoryId
  }
  if (params.keyword !== undefined && params.keyword !== null && params.keyword !== '') {
    requestParams.keyword = params.keyword
  }
  if (params.status !== undefined && params.status !== null) {
    requestParams.status = params.status
  }
  if (params.userId !== undefined && params.userId !== null && params.userId !== '') {
    requestParams.userId = params.userId
  }

  return request({
    url: '/square/works',
    method: 'GET',
    params: requestParams
  })
}

/**
 * 点赞/取消点赞作品集
 * @param {Number} workId - 作品集ID
 */
export const toggleWorkLike = (workId) => {
  return request({
    url: `/works/${workId}/like`,
    method: 'POST'
  })
}

/**
 * 收藏/取消收藏作品集
 * @param {Number} workId - 作品集ID
 */
export const toggleWorkCollect = (workId) => {
  return request({
    url: `/works/${workId}/collect`,
    method: 'POST'
  })
}