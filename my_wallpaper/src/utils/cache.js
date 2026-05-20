const CACHE_KEYS = {
	HOME_DATA: 'home_data',
	HOT_CATEGORIES: 'hot_categories',
	CATEGORY_LIST: 'category_list'
}

const CACHE_PREFIX = 'cache_'
const DEFAULT_EXPIRE = 5 * 60 * 1000

function setCache(key, data, expire = DEFAULT_EXPIRE) {
	try {
		const cacheData = {
			data,
			timestamp: Date.now(),
			expire
		}
		uni.setStorageSync(CACHE_PREFIX + key, JSON.stringify(cacheData))
		return true
	} catch (e) {
		console.error('缓存设置失败:', e)
		return false
	}
}

function getCache(key) {
	try {
		const cacheStr = uni.getStorageSync(CACHE_PREFIX + key)
		if (!cacheStr) return null

		const cacheData = JSON.parse(cacheStr)
		const now = Date.now()

		if (now - cacheData.timestamp > cacheData.expire) {
			removeCache(key)
			return null
		}

		return cacheData.data
	} catch (e) {
		console.error('缓存读取失败:', e)
		return null
	}
}

function removeCache(key) {
	try {
		uni.removeStorageSync(CACHE_PREFIX + key)
		return true
	} catch (e) {
		console.error('缓存删除失败:', e)
		return false
	}
}

function clearAllCache() {
	try {
		const keys = uni.getStorageInfoSync().keys
		keys.forEach(key => {
			if (key.startsWith(CACHE_PREFIX)) {
				uni.removeStorageSync(key)
			}
		})
		return true
	} catch (e) {
		console.error('清空缓存失败:', e)
		return false
	}
}

async function getOrFetch(key, fetchFn, expire = DEFAULT_EXPIRE) {
	const cached = getCache(key)
	if (cached !== null) {
		return { data: cached, fromCache: true }
	}

	const data = await fetchFn()
	setCache(key, data, expire)
	return { data, fromCache: false }
}

export { CACHE_KEYS }

export default {
	CACHE_KEYS,
	setCache,
	getCache,
	removeCache,
	clearAllCache,
	getOrFetch
}
