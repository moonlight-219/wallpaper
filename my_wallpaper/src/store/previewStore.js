import { reactive } from 'vue'

const PREVIEW_STORAGE_KEY = 'wallpaper_preview_list'

const previewState = reactive({
	list: [],
	hasData: false
})

const saveToStorage = (list) => {
	try {
		if (list && list.length > 0) {
			uni.setStorageSync(PREVIEW_STORAGE_KEY, JSON.stringify(list))
		} else {
			uni.removeStorageSync(PREVIEW_STORAGE_KEY)
		}
	} catch (e) {
		console.error('保存预览列表失败:', e)
	}
}

const loadFromStorage = () => {
	try {
		const data = uni.getStorageSync(PREVIEW_STORAGE_KEY)
		if (data) {
			return JSON.parse(data)
		}
	} catch (e) {
		console.error('读取预览列表失败:', e)
	}
	return null
}

const clearStorage = () => {
	try {
		uni.removeStorageSync(PREVIEW_STORAGE_KEY)
	} catch (e) {
		console.error('清除预览列表失败:', e)
	}
}

export const setPreviewList = (list) => {
	if (list && list.length > 0) {
		previewState.list = list
		previewState.hasData = true
		saveToStorage(list)
	} else {
		previewState.list = []
		previewState.hasData = false
		clearStorage()
	}
}

export const getPreviewList = () => {
	if (previewState.hasData && previewState.list.length > 0) {
		return {
			list: previewState.list,
			hasData: true
		}
	}

	const storedList = loadFromStorage()
	if (storedList && storedList.length > 0) {
		previewState.list = storedList
		previewState.hasData = true
		return {
			list: storedList,
			hasData: true
		}
	}

	return {
		list: [],
		hasData: false
	}
}

export const clearPreviewList = () => {
	previewState.list = []
	previewState.hasData = false
	clearStorage()
}

export default previewState
