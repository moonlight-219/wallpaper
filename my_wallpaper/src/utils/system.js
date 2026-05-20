import {
	ref
} from "vue"

// 获取状态栏高度
export const getStatusBarHeight = () => {
	let WEIXIN_INFO = wx.getWindowInfo()
	return WEIXIN_INFO.statusBarHeight || 15
}

// 获取标题栏高度
export const getTitleBarHeight = () => {
	// #ifndef H5
	let MENU_INFO = uni.getMenuButtonBoundingClientRect()

	let {
		top,
		height
	} = MENU_INFO
	return (top - getStatusBarHeight()) * 2 + height || 0

	// #endif

	// #ifdef H5
	return 60
	// #endif

}

// 获取状态栏的高度
export const getNaviBarHeight = () => {
	return getTitleBarHeight() + getStatusBarHeight()
}