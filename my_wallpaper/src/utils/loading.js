/**
 * 通用 Loading 工具
 */

let loadingCount = 0 // 记录 loading 调用次数，支持多个并发请求

/**
 * 显示加载提示
 * @param {String} title - 提示文字，默认"加载中..."
 * @param {Boolean} mask - 是否显示透明蒙层，默认 true
 */
export const showLoading = (title = '加载中...', mask = true) => {
	loadingCount++
	uni.showLoading({
		title,
		mask
	})
}

/**
 * 隐藏加载提示
 * @param {Boolean} force - 是否强制关闭，默认 false
 */
export const hideLoading = (force = false) => {
	if (force) {
		loadingCount = 0
		uni.hideLoading()
		return
	}
	
	loadingCount--
	if (loadingCount <= 0) {
		loadingCount = 0
		uni.hideLoading()
	}
}

/**
 * 显示成功提示
 * @param {String} title - 提示文字
 * @param {Number} duration - 显示时长，默认 1500ms
 */
export const showSuccess = (title = '操作成功', duration = 1500) => {
	uni.showToast({
		title,
		icon: 'success',
		duration
	})
}

/**
 * 显示失败提示
 * @param {String} title - 提示文字
 * @param {Number} duration - 显示时长，默认 2000ms
 */
export const showError = (title = '操作失败', duration = 2000) => {
	uni.showToast({
		title,
		icon: 'none',
		duration
	})
}

/**
 * 显示普通提示
 * @param {String} title - 提示文字
 * @param {Number} duration - 显示时长，默认 1500ms
 */
export const showToast = (title, duration = 1500) => {
	uni.showToast({
		title,
		icon: 'none',
		duration
	})
}

/**
 * 异步操作包装器，自动处理 loading
 * @param {Function} asyncFn - 异步函数
 * @param {Object} options - 配置项
 * @param {String} options.loadingText - loading 文字
 * @param {String} options.successText - 成功提示文字
 * @param {String} options.errorText - 失败提示文字
 * @param {Boolean} options.showSuccess - 是否显示成功提示，默认 false
 * @param {Boolean} options.showError - 是否显示失败提示，默认 true
 */
export const withLoading = async (asyncFn, options = {}) => {
	const {
		loadingText = '加载中...',
		successText = '操作成功',
		errorText = '操作失败',
		showSuccess: showSuccessToast = false,
		showError: showErrorToast = true
	} = options
	
	try {
		showLoading(loadingText)
		const result = await asyncFn()
		hideLoading()
		
		if (showSuccessToast) {
			showSuccess(successText)
		}
		
		return result
	} catch (error) {
		hideLoading(true) // 出错时强制关闭
		
		if (showErrorToast) {
			showError(errorText)
		}
		
		throw error
	}
}

/**
 * 页面跳转时的 loading（带延迟，确保 loading 先显示）
 * @param {Function} callback - 跳转前的回调函数
 * @param {String} loadingText - loading 文字
 * @param {Number} delay - 延迟时间，默认 50ms
 */
export const loadingBeforeNavigate = async (callback, loadingText = '加载中...', delay = 50) => {
	showLoading(loadingText)
	
	// 等待 loading 显示
	await new Promise(resolve => setTimeout(resolve, delay))
	
	try {
		await callback()
	} finally {
		hideLoading()
	}
}
