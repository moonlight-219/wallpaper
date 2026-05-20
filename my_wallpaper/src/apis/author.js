import request from '@/utils/request'

/**
 * 获取创作者列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码（从1开始，默认1）
 * @param {number} params.pageSize - 每页数量（默认10）
 * @returns {Promise} 返回创作者列表数据
 */
export function getAuthorList(params = {}) {
  return request({
    url: '/author/list',
    method: 'get',
    params
  })
}

/**
 * 获取创作者预览信息
 * @param {number} id - 创作者ID【必填】
 * @returns {Promise} 返回创作者信息、统计数据和最新作品（最多6个）
 */
export function getAuthorById(id) {
  return request({
    url: `/author/${id}`,
    method: 'get'
  })
}

/**
 * 获取创作者详情及作品列表
 * @param {number} id - 创作者ID【必填】
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码（默认1）
 * @param {number} params.pageSize - 每页数量（默认10）
 * @param {string} params.type - 类型筛选（phone/tablet/avatar）
 * @returns {Promise} 返回创作者详细信息和作品列表
 */
export function getAuthorDetail(id, params = {}) {
  return request({
    url: `/author/detail/${id}`,
    method: 'get',
    params
  })
}