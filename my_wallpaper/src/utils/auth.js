/**
 * 登录态与登录页跳转（需与 stores/user、request 中 token 存储一致）
 */

import { useUserStore } from '@/stores/user.js'

const TAB_BAR_PATHS = new Set([
	'/pages/index/index',
	'/pages/category/index',
	'/pages/author/index',
	'/pages/square/index',
	'/pages/my/index'
])

/** 登录回跳：H5 等环境下 URL 参数可能丢失，必须用存储兜底 */
const STORAGE_REDIRECT = 'authRedirect'
/** push = navigateTo 打开登录页；replace = redirectTo 替换为登录页 */
const STORAGE_NAV_MODE = 'authNavigateMode'

export function isLoggedIn() {
	const store = useUserStore()
	return store.isLoggedIn.value
}

/**
 * 打开登录页（保留当前页栈，适合从业务页 navigateTo 登录后 navigateBack）
 * @param {string} [redirectPath] 登录成功后的目标页，需以 / 开头，可带 query
 */
export function navigateToLogin(redirectPath) {
	const path = redirectPath && String(redirectPath).trim()
	uni.setStorageSync(STORAGE_NAV_MODE, 'push')
	if (path) {
		uni.setStorageSync(STORAGE_REDIRECT, path)
	} else {
		uni.removeStorageSync(STORAGE_REDIRECT)
	}
	const q = path ? `?redirect=${encodeURIComponent(path)}` : ''
	uni.navigateTo({ url: `/pages/login/index${q}` })
}

/**
 * 用登录页替换当前页（未登录进入「必须登录」子页时使用，避免空白闪一下）
 */
export function redirectToLoginReplace(redirectPath) {
	const path = (redirectPath && String(redirectPath).trim()) || '/pages/index/index'
	uni.setStorageSync(STORAGE_NAV_MODE, 'replace')
	uni.setStorageSync(STORAGE_REDIRECT, path)
	const q = `?redirect=${encodeURIComponent(path)}`
	uni.redirectTo({ url: `/pages/login/index${q}` })
}

/**
 * 登录成功后的跳转（在登录页调用）
 * @param {string} redirectPath encode 前的路径
 */
export function afterLoginNavigate(redirectPath) {
	const r = (redirectPath && String(redirectPath).trim()) || ''
	if (!r) {
		uni.reLaunch({ url: '/pages/index/index' })
		return
	}
	const pathOnly = r.split('?')[0]
	if (TAB_BAR_PATHS.has(pathOnly)) {
		uni.switchTab({
			url: pathOnly,
			fail: () => {
				uni.reLaunch({ url: '/pages/index/index' })
			}
		})
	} else {
		const url = r.startsWith('/') ? r : `/${r}`
		uni.redirectTo({
			url,
			fail: () => {
				uni.reLaunch({ url })
			}
		})
	}
}

/**
 * 登录成功后的路由（在登录页调用）
 * 1. 有存储或入参的 redirect → 跳转到该页（解决 H5 丢 query、及「我的→登录→要去子页」）
 * 2. 无 redirect 且是 push 打开的登录 → navigateBack 回到上一级
 * 3. 否则回首页
 * @param {string} [pageFallback] 登录页 onLoad 解析到的 redirect（双重兜底）
 */
export function finishLoginNavigation(pageFallback = '') {
	const storedPath = (uni.getStorageSync(STORAGE_REDIRECT) || '').trim()
	const mode = uni.getStorageSync(STORAGE_NAV_MODE) || ''
	const path = storedPath || (pageFallback && String(pageFallback).trim()) || ''

	uni.removeStorageSync(STORAGE_REDIRECT)
	uni.removeStorageSync(STORAGE_NAV_MODE)

	if (path) {
		afterLoginNavigate(path)
		return
	}
	if (mode === 'push') {
		const pages = getCurrentPages()
		if (pages.length > 1) {
			uni.navigateBack({ delta: 1 })
			return
		}
	}
	afterLoginNavigate('')
}

/**
 * 上传页：去登录 / 仅浏览
 * @returns {Promise<'login'|'browse'>}
 */
export function promptUploadAuth(redirectPath) {
	return new Promise((resolve) => {
		uni.showModal({
			title: '需要登录',
			content: '上传作品需先登录。是否前往登录？也可选择「仅浏览」本页（无法上传、提交）。',
			confirmText: '去登录',
			cancelText: '仅浏览',
			confirmColor: '#7a544d',
			success: (res) => {
				if (res.confirm) {
					navigateToLogin(redirectPath)
					resolve('login')
				} else {
					resolve('browse')
				}
			}
		})
	})
}

/**
 * 需要登录才能继续的操作（点赞、收藏、下载等）
 * 已登录返回 true；未登录弹窗，点「去登录」则跳转登录页并返回 false
 * @param {Object} [options]
 * @param {string} [options.redirectPath] 登录成功后回跳的完整路径
 * @param {string} [options.title]
 * @param {string} [options.content]
 * @param {string} [options.cancelText]
 */
export function ensureLoggedInForAction(options = {}) {
	if (isLoggedIn()) {
		return Promise.resolve(true)
	}
	const {
		redirectPath = '',
		title = '需要登录',
		content = '该功能需要先登录，是否前往登录？',
		cancelText = '取消'
	} = options
	return new Promise((resolve) => {
		uni.showModal({
			title,
			content,
			confirmText: '去登录',
			cancelText,
			confirmColor: '#7a544d',
			success: (res) => {
				if (res.confirm) {
					navigateToLogin(redirectPath || undefined)
				}
				resolve(false)
			}
		})
	})
}
