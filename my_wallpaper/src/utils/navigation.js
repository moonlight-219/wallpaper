/**
 * 导航工具函数
 * 提供统一的页面跳转和返回功能，包含错误处理
 */

/**
 * 安全返回上一级页面
 * 当 navigateBack 失败时（如页面栈为空），自动跳转到首页
 */
export function safeNavigateBack(options = {}) {
	const defaultOptions = {
		delta: 1,
		fail: () => {
			// 页面栈为空时，跳转到首页
			uni.switchTab({
				url: '/pages/index/index'
			})
		}
	}
	
	uni.navigateBack({
		...defaultOptions,
		...options,
		fail: (err) => {
			console.warn('[导航] 返回失败:', err)
			if (options.fail) {
				options.fail(err)
			} else {
				defaultOptions.fail()
			}
		}
	})
}

/**
 * 安全跳转到指定页面
 * 包含错误处理和日志记录
 */
export function safeNavigateTo(url, options = {}) {
	return new Promise((resolve, reject) => {
		uni.navigateTo({
			url,
			...options,
			success: (res) => {
				console.log('[导航] 跳转成功:', url)
				resolve(res)
			},
			fail: (err) => {
				console.error('[导航] 跳转失败:', url, err)
				uni.showToast({
					title: '页面跳转失败',
					icon: 'none'
				})
				reject(err)
			}
		})
	})
}

/**
 * 安全重定向到指定页面
 */
export function safeRedirectTo(url, options = {}) {
	return new Promise((resolve, reject) => {
		uni.redirectTo({
			url,
			...options,
			success: (res) => {
				console.log('[导航] 重定向成功:', url)
				resolve(res)
			},
			fail: (err) => {
				console.error('[导航] 重定向失败:', url, err)
				uni.showToast({
					title: '页面跳转失败',
					icon: 'none'
				})
				reject(err)
			}
		})
	})
}

/**
 * 安全切换到 TabBar 页面
 */
export function safeSwitchTab(url, options = {}) {
	return new Promise((resolve, reject) => {
		uni.switchTab({
			url,
			...options,
			success: (res) => {
				console.log('[导航] 切换Tab成功:', url)
				resolve(res)
			},
			fail: (err) => {
				console.error('[导航] 切换Tab失败:', url, err)
				// 如果 switchTab 失败，尝试使用 reLaunch
				uni.reLaunch({
					url,
					success: (res2) => {
						console.log('[导航] 使用reLaunch成功:', url)
						resolve(res2)
					},
					fail: (err2) => {
						console.error('[导航] reLaunch也失败:', url, err2)
						reject(err2)
					}
				})
			}
		})
	})
}

/**
 * 获取当前页面栈信息
 */
export function getCurrentPagesInfo() {
	const pages = getCurrentPages()
	const currentPage = pages[pages.length - 1]
	const route = currentPage ? currentPage.route : ''
	
	return {
		pages,
		currentPage,
		route,
		length: pages.length
	}
}

/**
 * 检查是否可以返回
 */
export function canGoBack(delta = 1) {
	const pages = getCurrentPages()
	return pages.length > delta
}
