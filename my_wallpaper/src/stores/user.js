import { ref, computed } from 'vue'

const token = ref(uni.getStorageSync('token') || '')
const userInfo = ref(uni.getStorageSync('userInfo') || null)

const isLoggedIn = computed(() => !!token.value && !!userInfo.value)

const setToken = (newToken) => {
	token.value = newToken
	uni.setStorageSync('token', newToken)
}

const setUserInfo = (info) => {
	userInfo.value = info
	uni.setStorageSync('userInfo', info)
}

const clearAuth = () => {
	token.value = ''
	userInfo.value = null
	uni.removeStorageSync('token')
	uni.removeStorageSync('userInfo')
	uni.removeStorageSync('userId')
}

export const useUserStore = () => {
	return {
		token,
		userInfo,
		isLoggedIn,
		setToken,
		setUserInfo,
		clearAuth
	}
}
