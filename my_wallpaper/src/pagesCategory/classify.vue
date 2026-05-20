<template>
	<view class="classify">
		<!-- 标题栏 -->
		<myTitleBar :title="categoryName">
			<view class="back-btn" @click="goBack()">
				<uni-icons type="left" size="20" color="#534747"></uni-icons>
			</view>
		</myTitleBar>

		<view class="classify-phone-box">
			<!-- 导航区 -->
			<view class="category-nav-wrap" :style="{ top: categoryNavTop }">
				<categoryNav v-model="currentNav" :items="navItems" :scrollable="false" @change="handleClick" />
			</view>

			<!-- 壁纸列表 -->
			<scroll-view class="phone-list" scroll-y scroll-with-animation :style="{ height: scrollHeight, top: scrollTopValue }" :scroll-top="scrollPosition"
				:refresher-enabled="true" :refresher-triggered="refreshing"
				@refresherrefresh="onRefresh" @scrolltolower="loadMore" @scroll="onScroll"
				refresher-background="#fafafa" lower-threshold="100">
				<boxItem :items="filteredList"></boxItem>

				<!-- 加载状态提示 -->
				<view class="load-more-wrap">
					<view v-if="loading" class="loading-text">
						<text>加载中...</text>
					</view>
					<view v-else-if="noMore" class="no-more-text">
						<text>没有更多了</text>
					</view>
				</view>
			</scroll-view>
		</view>

		<!-- 返回顶部按钮 -->
		<view class="back-top-btn" v-if="showBackTop" @click="handleBackTop">
			<text>↑</text>
		</view>
	</view>
</template>

<script setup>
import {
	ref,
	computed,
	nextTick
} from 'vue'
import {
	onLoad
} from '@dcloudio/uni-app'

import myTitleBar from '@/components/myTitleBar.vue'
import boxItem from '@/components/boxItem.vue'
import categoryNav from '@/components/categoryNav.vue'
import { loadingBeforeNavigate, showSuccess } from '@/utils/loading.js'
import { getWallpaperList } from '@/apis/wallpaper.js'
import { getNaviBarHeight } from '@/utils/system.js'
import { safeNavigateBack } from '@/utils/navigation.js'

const imgList = ref([])
const categoryId = ref(null)
const categoryName = ref('')
const currentNav = ref('全部')
const loading = ref(false)
const noMore = ref(false)
const refreshing = ref(false)
const currentPage = ref(1)
const pageSize = 10
const total = ref(0)
const keyword = ref('')

const scrollPosition = ref(0)
const showBackTop = ref(false)
const currentScrollTop = ref(0)

const scrollHeight = computed(() => {
	const navHeight = getNaviBarHeight()
	const categoryNavHeight = 44
	const totalHeight = navHeight + categoryNavHeight
	return `calc(100vh - ${totalHeight}px)`
})

const scrollTopValue = computed(() => {
	const navHeight = getNaviBarHeight()
	const categoryNavHeight = 44
	return (navHeight + categoryNavHeight) + 'px'
})

const categoryNavTop = computed(() => {
	return getNaviBarHeight() + 'px'
})

const onScroll = (e) => {
	currentScrollTop.value = e.detail.scrollTop
	showBackTop.value = e.detail.scrollTop > 100
}

const handleBackTop = () => {
	scrollPosition.value = currentScrollTop.value
	nextTick(() => {
		scrollPosition.value = 0
	})
}

const navItems = ref([
	{ label: '全部', value: '全部' },
	{ label: '手机', value: '手机' },
	{ label: '平板', value: '平板' },
	{ label: '头像', value: '头像' }
])

const loadStatus = computed(() => {
	if (loading.value) return 'loading'
	if (noMore.value) return 'noMore'
	return 'more'
})

const transformWallpaperData = (wallpaper) => {
	// 小程序端不使用外部placeholder，直接使用空字符串让骨架屏显示
	let imageUrl = wallpaper.url || wallpaper.thumbnailUrl || ''
	let thumbUrl = wallpaper.thumbnailUrl || wallpaper.url || ''
	
	// 验证URL格式
	if (imageUrl && !imageUrl.startsWith('http') && !imageUrl.startsWith('/static')) {
		console.warn('Invalid image URL:', imageUrl)
		imageUrl = ''
	}
	if (thumbUrl && !thumbUrl.startsWith('http') && !thumbUrl.startsWith('/static')) {
		console.warn('Invalid thumbnail URL:', thumbUrl)
		thumbUrl = ''
	}
	
	return {
		...wallpaper,
		name: wallpaper.title || '壁纸',
		url: imageUrl,
		thumbnailUrl: thumbUrl,
		author: wallpaper.author || {
			id: 0,
			name: '未知用户',
			avatarUrl: '/static/images/1.jpg'
		}
	}
}

const filteredList = computed(() => {
	return imgList.value
})

const getCurrentType = () => {
	const typeMap = {
		'全部': null,
		'手机': 1,
		'平板': 2,
		'头像': 3
	}
	return typeMap[currentNav.value]
}

onLoad((options) => {
	if (options.id) {
		categoryId.value = parseInt(options.id)
	}
	if (options.name) {
		categoryName.value = options.name
	}
	if (options.key) {
		keyword.value = options.key
		categoryName.value = `${options.key}`
	} else {
		categoryName.value = '全部'
	}
	loadWallpapers(true)
})

const loadWallpapers = async (isRefresh = false) => {
	if (loading.value) return

	if (isRefresh) {
		currentPage.value = 1
		noMore.value = false
	}

	loading.value = true

	try {
		const params = {
			page: currentPage.value,
			pageSize: pageSize
		}

		const userInfo = uni.getStorageSync('userInfo')
		if (userInfo?.id) {
			params.userId = userInfo.id
		}

		const currentType = getCurrentType()
		if (currentType !== null && currentType !== undefined) {
			params.type = currentType
		}

		if (keyword.value) {
			params.keyword = keyword.value
		} else if (categoryId.value) {
			params.categoryId = categoryId.value
		}

		const res = await getWallpaperList(params)

		if (res.code === 200) {
			const newData = res.data.list.map(transformWallpaperData)

			if (isRefresh) {
				imgList.value = newData
			} else {
				imgList.value.push(...newData)
			}

			total.value = res.data.total

			if (imgList.value.length >= total.value) {
				noMore.value = true
			}
		}
	} catch (error) {
		console.error('加载壁纸列表失败:', error)
		uni.showToast({
			title: '加载失败，请重试',
			icon: 'none'
		})
	} finally {
		loading.value = false
	}
}

const handleClick = async (type) => {
	console.log('handleClick called:', type)

	await loadingBeforeNavigate(async () => {
		imgList.value = []
		await loadWallpapers(true)
	})
}

const onRefresh = async () => {
	refreshing.value = true

	try {
		await loadWallpapers(true)
		showSuccess('刷新成功')
	} catch (error) {
		console.error('刷新失败:', error)
	} finally {
		refreshing.value = false
	}
}

const loadMore = () => {
	if (loading.value || noMore.value) {
		return
	}

	currentPage.value++
	loadWallpapers(false)
}

const goBack = () => {
	safeNavigateBack()
}
</script>

<style lang="scss" scoped>
.classify {
	background: #fafafa;
	width: 100vw;
	min-height: 100vh;
	display: flex;
	flex-direction: column;
	overflow-x: hidden;

	.back-btn {
		width: 56rpx;
		height: 56rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.classify-phone-box {
		display: flex;
		flex-direction: column;
		width: 100%;

		.category-nav-wrap {
			position: fixed;
			left: 0;
			right: 0;
			background: #fff;
			z-index: 99;
		}

		.phone-list {
			position: fixed;
			left: 0;
			right: 0;
			width: 100%;
			padding: 0 20rpx 20rpx;
			box-sizing: border-box;

			.load-more-wrap {
				width: 100%;
				padding: 30rpx 0 40rpx;
				display: flex;
				justify-content: center;
				align-items: center;

				.loading-text,
				.no-more-text {
					font-size: 26rpx;
					color: #999;
					text-align: center;
				}

				.loading-text {
					&::before {
						content: '';
						display: inline-block;
						width: 30rpx;
						height: 30rpx;
						border: 3rpx solid #e0e0e0;
						border-top-color: #9b8180;
						border-radius: 50%;
						animation: spin 0.8s linear infinite;
						margin-right: 10rpx;
						vertical-align: middle;
					}
				}
			}

			@keyframes spin {
				to {
					transform: rotate(360deg);
				}
			}
		}
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
