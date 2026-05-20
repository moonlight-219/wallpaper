import request from '@/utils/request.js'
import cache, { CACHE_KEYS } from '@/utils/cache.js'

const HOME_CACHE_TIME = 5 * 60 * 1000

export const getHomeData = (forceRefresh = false) => {
	if (!forceRefresh) {
		const cached = cache.getCache(CACHE_KEYS.HOME_DATA)
		if (cached) {
			return Promise.resolve({ code: 200, data: cached })
		}
	}

	return request({
		url: '/home/data',
		method: 'GET'
	}).then(res => {
		if (res.code === 200) {
			cache.setCache(CACHE_KEYS.HOME_DATA, res.data, HOME_CACHE_TIME)
		}
		return res
	})
}

export const getDailyRecommend = (page = 1, pageSize = 10) => {
	return request({
		url: '/home/daily-recommend',
		method: 'GET',
		params: {
			page,
			pageSize
		}
	})
}

export const clearHomeCache = () => {
	cache.removeCache(CACHE_KEYS.HOME_DATA)
}
