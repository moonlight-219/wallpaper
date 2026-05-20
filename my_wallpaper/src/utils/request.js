import config from '@/config/index.js'

const isDev = process.env.NODE_ENV === 'development'

const log = (...args) => {
	if (isDev) {
		console.log(...args)
	}
}

const logError = (...args) => {
	console.error(...args)
}

const PUBLIC_API_PREFIXES = [
	'/wallpaper/list',
	'/wallpaper/hot',
	'/wallpaper/popular',
	'/wallpaper/most-downloaded',
	'/wallpaper/most-collected',
	'/wallpaper/search',
	'/wallpaper/detail',
	'/home',
	'/square',
	'/category',
	'/author/list',
	'/author/',
	'/works',
	'/health',
	'/user/login',
	'/user/register'
]

const isPublicApi = (url) => {
	return PUBLIC_API_PREFIXES.some(prefix => url.startsWith(prefix))
}

const isAuthRequired = (url, method) => {
	const upperMethod = (method || 'GET').toUpperCase()
	if (['POST', 'PUT', 'DELETE'].includes(upperMethod)) {
		return true
	}
	const authPaths = [
		'/user/', '/upload', '/admin/'
	]
	return authPaths.some(path => url.includes(path)) && !isPublicApi(url)
}

const filterParams = (obj) => {
	if (!obj || typeof obj !== 'object') return {}
	const filtered = {}
	Object.keys(obj).forEach(key => {
		const value = obj[key]
		if (value !== undefined && value !== null && value !== '') {
			filtered[key] = value
		}
	})
	return filtered
}

const request = (options) => {
	return new Promise((resolve, reject) => {
		const token = uni.getStorageSync('token')
		const url = options.url
		const method = (options.method || 'GET').toUpperCase()

		log('[请求拦截器] 请求信息:', {
			url,
			method,
			hasToken: !!token,
			tokenPreview: token ? token.substring(0, 20) + '...' : 'null'
		})

		const requestData = options.data || {}
		const requestParams = options.params || {}

		let data = { ...requestData }
		if (Object.keys(requestParams).length > 0) {
			data = { ...data, ...requestParams }
		}

		data = filterParams(data)

		const filteredParams = filterParams(requestParams)

		const headers = {
			'Content-Type': 'application/json',
			...(options.header || {})
		}

		if (token) {
			headers['Authorization'] = `Bearer ${token}`
		}

		const handleResponse = (res) => { // 去掉多余的 requestUrl 参数
			if (res.statusCode === 200 && res.data.code === 200) {
				resolve(res.data)
			} else {
				uni.showToast({ title: '请求失败', icon: 'none' })
				reject(res)
			}
		}

		// ===================== 统一控制 /api 前缀 =====================
		// H5 dev：baseURL 为空，走 Vite proxy → /api/xxx
		// 小程序 dev：baseURL 为完整地址（后端 context-path 已含 /api）→ 直接拼
		// 生产环境：baseURL 为服务器地址，Nginx 代理 → 加 /api 前缀
		let fullUrl = config.baseURL || ''

		// #ifdef H5
		if (process.env.NODE_ENV === 'development') {
			// H5 开发：走 Vite proxy，需要 /api 前缀
			fullUrl += '/api' + url
		} else {
			// H5 生产：Nginx 代理，需要 /api 前缀
			fullUrl += '/api' + url
		}
		// #endif

		// #ifndef H5
		// 小程序：后端 context-path 已是 /api，URL 本身不含 /api 前缀
		if (process.env.NODE_ENV === 'production') {
			fullUrl += '/api' + url
		} else {
			fullUrl += '/api' + url
		}
		// #endif
		// ============================================================

		// 手动拼接 URL 参数（兼容小程序环境）- 使用过滤后的参数
		if (Object.keys(filteredParams).length > 0) {
			const queryString = Object.keys(filteredParams)
				.map(key => `${encodeURIComponent(key)}=${encodeURIComponent(filteredParams[key])}`)
				.join('&')
			fullUrl += '?' + queryString
		}

		uni.request({
			url: fullUrl,
			method: method,
			data: data,
			header: headers,
			success: handleResponse,
			fail: (err) => {
				logError(err)
				uni.showToast({ title: '网络失败', icon: 'none' })
				reject(err)
			}
		})
	})
}

export default request