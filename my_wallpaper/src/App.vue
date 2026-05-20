<script>
	import { safeNavigateBack } from '@/utils/navigation.js'
	
	export default {
		onLaunch: function() {
			console.log('App Launch')
			
			// 全局错误处理
			this.setupGlobalErrorHandler()
			
			// 全局未捕获的 Promise 错误处理
			this.setupUnhandledRejection()
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		},
		methods: {
			setupGlobalErrorHandler() {
				// #ifdef H5
				window.onerror = (message, source, lineno, colno, error) => {
					console.error('[全局错误]', { message, source, lineno, colno, error })
					this.handlePageError(error)
					return true
				}
				// #endif
			},
			
			setupUnhandledRejection() {
				// #ifdef H5
				window.addEventListener('unhandledrejection', (event) => {
					console.error('[未处理的Promise拒绝]', event.reason)
					
					// 如果是导航相关的错误，尝试返回上一级
					if (this.isNavigationError(event.reason)) {
						safeNavigateBack()
					}
				})
				// #endif
				
				// uni-app 的全局错误处理
				const _this = this
				uni.onError((error) => {
					console.error('[uni-app错误]', error)
					_this.handlePageError(error)
				})
				
				// 页面找不到时的处理
				uni.onPageNotFound((res) => {
					console.error('[页面未找到]', res)
					uni.showToast({
						title: '页面不存在',
						icon: 'none'
					})
					setTimeout(() => {
						uni.switchTab({
							url: '/pages/index/index'
						})
					}, 1500)
				})
			},
			
			handlePageError(error) {
				// 记录错误信息
				console.error('[页面错误详情]', error)
				
				// 可以根据错误类型进行不同处理
				if (this.isNavigationError(error)) {
					// 导航相关错误，延迟后尝试返回
					setTimeout(() => {
						safeNavigateBack()
					}, 500)
				} else if (this.isNetworkError(error)) {
					// 网络错误提示
					uni.showToast({
						title: '网络异常，请检查网络连接',
						icon: 'none',
						duration: 2000
					})
				} else if (this.isAuthError(error)) {
					// 认证错误，跳转到登录页
					uni.showToast({
						title: '登录已过期，请重新登录',
						icon: 'none',
						duration: 1500
					})
					setTimeout(() => {
						uni.navigateTo({
							url: '/pages/login/index'
						})
					}, 1500)
				}
			},
			
			isNavigationError(error) {
				if (!error) return false
				const errorMsg = error.message || error.toString() || ''
				return errorMsg.includes('navigate') || 
					   errorMsg.includes('navigateBack') ||
					   errorMsg.includes('Failed to fetch dynamically imported module') ||
					   errorMsg.includes('Cannot find page')
			},
			
			isNetworkError(error) {
				if (!error) return false
				const errorMsg = error.message || error.toString() || ''
				return errorMsg.includes('network') || 
					   errorMsg.includes('timeout') ||
					   errorMsg.includes('Network Error') ||
					   errorMsg.includes('请求失败')
			},
			
			isAuthError(error) {
				if (!error) return false
				const errorMsg = error.message || error.toString() || ''
				return errorMsg.includes('401') || 
					   errorMsg.includes('unauthorized') ||
					   errorMsg.includes('未登录') ||
					   errorMsg.includes('登录已过期')
			}
		}
	}
</script>

<style lang="scss">
	/*每个页面公共css */
	@import '@/common/css/style.css';

	::-webkit-scrollbar {
		display: none;
		width: 0 !important;
		height: 0 !important;
		-webkit-appearance: none;
		background: transparent;
	}

	/* 隐藏原生 tabBar，使用自定义 tabBar */
	uni-tabbar {
		display: none !important;
	}
</style>
