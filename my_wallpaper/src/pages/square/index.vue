<template>
	<view class="square">
		<!-- 标题栏 -->
		<myTitleBar></myTitleBar>

		<!-- 排序筛选栏 -->
		<view class="filter-bar">
			<view class="filter-item" :class="{ active: sortType === 'time' }" @click="changeSortType('time')">
				<text class="filter-text">最新</text>
				<view class="filter-indicator" v-if="sortType === 'time'"></view>
			</view>
			<view class="filter-item" :class="{ active: sortType === 'likes' }" @click="changeSortType('likes')">
				<text class="filter-text">最热</text>
				<view class="filter-indicator" v-if="sortType === 'likes'"></view>
			</view>
			<view class="filter-item" :class="{ active: sortType === 'collects' }" @click="changeSortType('collects')">
				<text class="filter-text">收藏</text>
				<view class="filter-indicator" v-if="sortType === 'collects'"></view>
			</view>
		</view>

		<scroll-view class="square-box" scroll-y scroll-with-animation :style="{ height: scrollHeight, top: scrollTopValue }" :scroll-top="scrollPosition" @scroll="onScroll" @scrolltolower="loadMore" @refresherrefresh="onRefresh"
			:refresher-enabled="true" :refresher-triggered="refreshing" refresher-background="#fafafa" lower-threshold="100">
			<workList :worksList="sortedWorksList" :loading="loading" :loadStatus="loadStatus" @preview="goPreview"
				@like="toggleLike" @collect="toggleCollect" @author="goAuthorPage" />
		</scroll-view>

		<!-- 返回顶部按钮 -->
		<view class="back-top-btn" v-if="showBackTop" @click="handleBackTop">
			<text>↑</text>
		</view>

		<!-- 底部导航栏 -->
		<myTabBar></myTabBar>
	</view>
</template>

<script setup>
import myTabBar from '@/components/myTabBar.vue'
import myTitleBar from '@/components/myTitleBar.vue'
import workList from './components/workList.vue'
import { getSquareWorks, toggleWorkLike, toggleWorkCollect } from '@/apis/square.js'
import { isLoggedIn, ensureLoggedInForAction } from '@/utils/auth.js'
import {
	ref,
	computed,
	nextTick
} from "vue";
import { onShow } from '@dcloudio/uni-app'
import {
	getNaviBarHeight
} from '@/utils/system.js'

onShow(() => {
	uni.hideTabBar()
})

const filterBarTop = computed(() => {
	return getNaviBarHeight() + 'px'
})

const scrollTopValue = computed(() => {
	const navHeight = getNaviBarHeight()
	const filterBarHeight = 44
	return (navHeight + filterBarHeight) + 'px'
})

const scrollHeight = computed(() => {
	const navHeight = getNaviBarHeight()
	const filterBarHeight = 44
	const tabBarHeight = 75
	const safeAreaBottom = uni.getSystemInfoSync().safeAreaInsets?.bottom || 0
	const totalHeight = navHeight + filterBarHeight + tabBarHeight + safeAreaBottom
	return `calc(100vh - ${totalHeight}px)`
})

const scrollPosition = ref(0)
const showBackTop = ref(false)
const currentScrollTop = ref(0)

const onScroll = (e) => {
	currentScrollTop.value = e.detail.scrollTop
	showBackTop.value = e.detail.scrollTop > 300
}

const handleBackTop = () => {
	scrollPosition.value = currentScrollTop.value
	nextTick(() => {
		scrollPosition.value = 0
	})
}

const formatTimeAgo = (timestamp) => {
	if (!timestamp) return '刚刚'

	const now = Date.now()
	const diff = now - new Date(timestamp).getTime()

	if (diff <= 0) return '刚刚'

	const minutes = Math.floor(diff / 60000)
	const hours = Math.floor(diff / 3600000)
	const days = Math.floor(diff / 86400000)

	if (minutes < 1) {
		return '刚刚'
	} else if (minutes < 60) {
		return `${minutes}分钟前`
	} else if (hours < 24) {
		return `${hours}小时前`
	} else if (days < 30) {
		return `${days}天前`
	} else {
		const date = new Date(timestamp)
		const y = date.getFullYear()
		const m = String(date.getMonth() + 1).padStart(2, '0')
		const d = String(date.getDate()).padStart(2, '0')
		const currentYear = new Date().getFullYear()
		return y === currentYear ? `${m}-${d}` : `${y}-${m}-${d}`
	}
}

const transformWorkData = (workVO) => {
	return {
		id: workVO.id,
		author: workVO.author || {
			id: 0,
			name: '未知用户',
			avatar: '/static/images/1.jpg'
		},
		type: workVO.type,
		coverUrl: workVO.coverUrl || workVO.thumbnailUrl || '/static/images/1.jpg',
		images: workVO.wallpapers ? workVO.wallpapers.map(wp => ({
			id: wp.id,
			type: wp.type,
			url: wp.thumbnailUrl || wp.url || '/static/images/1.jpg'
		})) : [],
		publishTimestamp: workVO.publishTime ? new Date(workVO.publishTime).getTime() : Date.now(),
		publishTime: formatTimeAgo(workVO.publishTime),
		likes: workVO.likes || 0,
		collects: workVO.collects || 0,
		views: workVO.views || 0,
		isLiked: workVO.isLiked || false,
		isCollected: workVO.isCollected || false
	}
}

const worksList = ref([])
const loading = ref(false)
const noMore = ref(false)
const refreshing = ref(false)
const currentPage = ref(1)
const pageSize = 10
const total = ref(0)
const sortType = ref('time')

const loadStatus = computed(() => {
	if (loading.value) return 'loading'
	if (noMore.value) return 'noMore'
	return 'more'
})

const getBackendSortType = () => {
	switch (sortType.value) {
		case 'time':
			return null
		case 'likes':
			return 1
		case 'collects':
			return 2
		default:
			return null
	}
}

const loadWorks = async (isRefresh = false) => {
	if (loading.value) return

	if (isRefresh) {
		currentPage.value = 1
		noMore.value = false
	}

	loading.value = true

	try {
		const userId = uni.getStorageSync('userId')

		console.log('开始加载作品列表，参数:', {
			page: currentPage.value,
			pageSize: pageSize,
			sortType: getBackendSortType(),
			status: 1,
			userId: userId || '未登录'
		})

		const params = {
			page: currentPage.value,
			pageSize: pageSize,
			sortType: getBackendSortType(),
			status: 1
		}

		if (userId !== undefined && userId !== null && userId !== '') {
			params.userId = userId
		}

		const res = await getSquareWorks(params)

		console.log('API响应数据:', res)

		if (res.code === 200 && res.data) {
			console.log('响应中的total:', res.data.total)

			const newWorks = res.data.list.map(transformWorkData)
			console.log('转换后的works数据:', newWorks)

			if (isRefresh) {
				worksList.value = newWorks
			} else {
				worksList.value.push(...newWorks)
			}

			total.value = res.data.total

			if (worksList.value.length >= total.value) {
				noMore.value = true
			}

			console.log('当前worksList长度:', worksList.value.length)
		} else {
			uni.showToast({
				title: res.message || '加载失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('加载作品列表失败:', error)
		uni.showToast({
			title: '网络异常，请检查网络连接',
			icon: 'none',
			duration: 2000
		})
	} finally {
		loading.value = false
	}
}

const sortedWorksList = computed(() => {
	return worksList.value
})

const changeSortType = (type) => {
	if (sortType.value === type) return
	sortType.value = type
	currentPage.value = 1
	noMore.value = false
	loading.value = false

	uni.showLoading({
		title: '加载中...',
		mask: true
	})

	loadWorks(true).then(() => {
		uni.hideLoading()
		handleBackTop()
	}).catch(() => {
		uni.hideLoading()
	})
}

const onRefresh = () => {
	refreshing.value = true

	loadWorks(true).then(() => {
		refreshing.value = false

		uni.showToast({
			title: '刷新成功',
			icon: 'success',
			duration: 1500
		})
	}).catch(() => {
		refreshing.value = false
	})
}

const loadMore = () => {
	if (loading.value || noMore.value) return

	currentPage.value++
	loadWorks(false)
}

const goPreview = (workIndex) => {
	const work = sortedWorksList.value[workIndex]
	uni.navigateTo({
		url: `/pages/preview/preview-detail?workId=${work.id}`
	})
}

const toggleLike = async (work) => {
	if (!isLoggedIn()) {
		await ensureLoggedInForAction({
			redirectPath: '/pages/square/index',
			content: '点赞需要先登录，是否前往登录？'
		})
		return
	}

	try {
		const res = await toggleWorkLike(work.id)
		if (res.code === 200) {
			work.isLiked = !work.isLiked
			work.likes += work.isLiked ? 1 : -1
		} else {
			uni.showToast({
				title: res.message || '操作失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('点赞失败:', error)
		uni.showToast({
			title: '网络异常，请稍后重试',
			icon: 'none'
		})
	}
}

const toggleCollect = async (work) => {
	if (!isLoggedIn()) {
		await ensureLoggedInForAction({
			redirectPath: '/pages/square/index',
			content: '收藏需要先登录，是否前往登录？'
		})
		return
	}

	try {
		const res = await toggleWorkCollect(work.id)
		if (res.code === 200) {
			work.isCollected = !work.isCollected
			work.collects += work.isCollected ? 1 : -1
		} else {
			uni.showToast({
				title: res.message || '操作失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('收藏失败:', error)
		uni.showToast({
			title: '网络异常，请稍后重试',
			icon: 'none'
		})
	}
}

const goAuthorPage = (author) => {
	uni.navigateTo({
		url: `/pagesAuthor/author-detail?userId=${author.id}`
	})
}

loadWorks(true)
</script>

<style lang="scss" scoped>
.square {
	width: 100vw;
	height: 100vh;
	background: $self-background-color;

	.filter-bar {
		position: fixed;
		top: v-bind(filterBarTop);
		left: 0;
		right: 0;
		background: #fff;
		display: flex;
		align-items: center;
		height: 88rpx;
		border-bottom: 1rpx solid #f0f0f0;
		z-index: 99;

		.filter-item {
			flex: 1;
			height: 100%;
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			position: relative;
			transition: all 0.3s;

			.filter-text {
				font-size: 30rpx;
				color: #666;
				font-weight: 400;
				transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
			}

			.filter-indicator {
				position: absolute;
				bottom: 0;
				width: 40rpx;
				height: 6rpx;
				background: linear-gradient(90deg, #a89187, #d4c4b8);
				border-radius: 3rpx 3rpx 0 0;
				animation: scaleIn 0.3s ease-out;
			}

			&.active {
				.filter-text {
					color: #333;
					font-weight: 600;
					font-size: 32rpx;
				}
			}

			&:active {
				.filter-text {
					transform: scale(0.92);
					opacity: 0.6;
				}
			}
		}
	}

	@keyframes scaleIn {
		from {
			transform: scaleX(0);
			opacity: 0;
		}

		to {
			transform: scaleX(1);
			opacity: 1;
		}
	}

	.square-box {
		position: fixed;
		left: 0;
		right: 0;
		width: 100%;
	}

	.back-top-btn {
		position: fixed;
		right: 30rpx;
		bottom: calc(env(safe-area-inset-bottom) + 180rpx);
		width: 80rpx;
		height: 80rpx;
		border-radius: 50%;
		background: #ffffff;
		box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.15);
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 36rpx;
		color: #7a544d;
		z-index: 100;
		transition: all 0.2s;

		&:active {
			transform: scale(0.9);
			background: #f5f5f5;
		}
	}
}
</style>
