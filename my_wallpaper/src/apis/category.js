import request from '@/utils/request.js'

/**
 * 获取分类列表
 * @param {Object} params - 查询参数
 * @param {String} params.name - 分类名（支持模糊查询，不区分大小写）【可选】
 * @param {Number} params.page - 页码（从1开始）【可选，默认1】
 * @param {Number} params.pageSize - 每页数量【可选，默认10】
 */
export const getCategoryList = (params = {}) => {
	return request({
		url: '/category',
		method: 'GET',
		params
	})
}

/**
 * 获取精选分类（点赞量最高的分类）
 * @param {Number} limit - 返回数量，默认5个【可选】
 */
export const getHotCategories = (limit = 5) => {
	return request({
		url: '/category/hot',
		method: 'GET',
		params: { limit }
	})
}

/**
 * 根据分类ID获取分类基本信息
 * @param {Number} id - 分类ID【必填】
 */
export const getCategoryById = (id) => {
	return request({
		url: `/category/${id}`,
		method: 'GET'
	})
}

/**
 * 壁纸类型（与后端 wallpaper.type、分类详情接口 type 查询参数一致）
 * 1 手机 2 平板 3 头像
 */
export const WALLPAPER_TYPE = {
	PHONE: 1,
	TABLET: 2,
	AVATAR: 3
}

/**
 * 获取分类详情及该分类下壁纸分页列表
 * @param {Number|String} id - 分类ID【必填】
 * @param {Object} [params] - 查询参数
 * @param {Number|String} [params.type] - 壁纸类型：1 手机、2 平板、3 头像；不传则返回全部类型
 * @param {Number} [params.page=1] - 页码
 * @param {Number} [params.pageSize=10] - 每页数量
 */
export const getCategoryDetail = (id, params = {}) => {
	return request({
		url: `/category/${id}/detail`,
		method: 'GET',
		params: {
			page: params.page ?? 1,
			pageSize: params.pageSize ?? 20,
			...(params.type !== undefined && params.type !== null && params.type !== ''
				? { type: String(params.type) }
				: {})
		}
	})
}
