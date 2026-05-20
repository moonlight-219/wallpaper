import request from '@/utils/request.js'

export const login = (data = {}) => {
	return request({
		url: '/user/login',
		method: 'POST',
		data
	})
}

export const register = (data = {}) => {
	return request({
		url: '/user/register',
		method: 'POST',
		data
	})
}

export const logout = () => {
	return request({
		url: '/user/logout',
		method: 'POST'
	})
}

export const getUserInfo = () => {
	return request({
		url: '/user/info',
		method: 'GET'
	})
}

export const updateUserInfo = (data) => {
	return request({
		url: '/user/update',
		method: 'PUT',
		data
	})
}

/**
 * 获取用户信息和统计
 * @param {Number} userId - 用户ID【必填】
 */
export const getUserStats = (userId) => {
	return request({
		url: `/user/${userId}`,
		method: 'GET'
	})
}

export const getUserDownloads = (userId, params = {}) => {
	return request({
		url: `/user/${userId}/downloads`,
		method: 'GET',
		params: {
			page: params.page || 1,
			pageSize: params.pageSize || 20
		}
	})
}

export const getUserUploads = (userId, params = {}) => {
	return request({
		url: `/user/${userId}/uploads`,
		method: 'GET',
		params: {
			page: params.page || 1,
			pageSize: params.pageSize || 20,
			...(params.status !== undefined && params.status !== null ? { status: params.status } : {})
		}
	})
}

export const getUserLikes = (userId, params = {}) => {
	return request({
		url: `/user/${userId}/likes`,
		method: 'GET',
		params: {
			page: params.page || 1,
			pageSize: params.pageSize || 20,
			type: params.type || 'wallpaper'
		}
	})
}

export const getUserCollects = (userId, params = {}) => {
	return request({
		url: `/user/${userId}/collects`,
		method: 'GET',
		params: {
			page: params.page || 1,
			pageSize: params.pageSize || 20,
			type: params.type || 'wallpaper'
		}
	})
}

/**
 * 删除作品集
 * @param {Number} workId - 作品集ID【必填】
 */
export const deleteWork = (workId) => {
	return request({
		url: `/works/${workId}`,
		method: 'DELETE'
	})
}

/**
 * 删除壁纸
 * @param {Number} wallpaperId - 壁纸ID【必填】
 */
export const deleteWallpaper = (wallpaperId) => {
	return request({
		url: `/wallpaper/${wallpaperId}`,
		method: 'DELETE'
	})
}

/**
 * 删除下载记录
 * @param {Number} actionId - 下载记录ID【必填】
 */
export const deleteDownloadRecord = (actionId) => {
	return request({
		url: `/user/download-record/${actionId}`,
		method: 'DELETE'
	})
}
