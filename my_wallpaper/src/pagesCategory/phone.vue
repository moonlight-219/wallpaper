<template>
	<view class="category-phone">
		<!-- 标题栏 -->
		<myTitleBar :title="title">
			<view class="back-btn" @click="goBack()">
				<uni-icons type="left" size="20" color="#534747"></uni-icons>
			</view>
		</myTitleBar>

		<view class="category-phone-box">
			<!-- 导航区 -->
			<categoryNav v-model="currentCategory" :items="categoryItems" :scrollable="true" @change="switchCategory" />

			<!-- 壁纸列表 -->
			<scroll-view class="phone-list" scroll-y :refresher-enabled="true" :refresher-triggered="refreshing"
				@refresherrefresh="onRefresh" @scrolltolower="loadMore" refresher-background="#fafafa" lower-threshold="100">
				<imgItem :items="displayList" v-if="type === 1"></imgItem>
				<tabletItem :items="displayList" v-if="type === 2"></tabletItem>
				<avatarItem :items="displayList" v-if="type === 3"></avatarItem>

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
	</view>
</template>

<script setup>
import {
	ref,
	computed
} from "vue"
import {
	onLoad
} from '@dcloudio/uni-app'
import myTitleBar from '@/components/myTitleBar.vue'
import imgItem from '@/components/imgItem.vue'
import tabletItem from '@/components/tabletItem.vue'
import avatarItem from '@/components/avatarItem.vue'
import categoryNav from '@/components/categoryNav.vue'
import { getCategoryList } from '@/apis/category'
import { getWallpaperList } from '@/apis/wallpaper'
import { loadingBeforeNavigate, showSuccess } from '@/utils/loading.js'
import { safeNavigateBack } from '@/utils/navigation.js'

// 分类列表
const categories = ref([])
const currentCategory = ref(null)  // 默认选中"最新"（id为null）

// 壁纸列表数据
const imgList = ref([])
const title = ref('')
const type = ref('')
const loading = ref(false)
const noMore = ref(false)
const refreshing = ref(false)
const currentPage = ref(1)
const pageSize = 10
const maxPages = 5

// 导航项配置
const categoryItems = computed(() => {
	return categories.value.map(category => ({
		label: category.name,
		value: category.id
	}))
})

// 加载分类列表
const loadCategories = async () => {
	try {
		const res = await getCategoryList()
		console.log('分类API响应:', res)

		if (res.code === 200 && res.data) {
			// 将API返回的分类数据设置到categories
			categories.value = res.data.list || []
			categories.value.unshift({
				name: '最新',
				id: null
			})
			console.log('分类加载成功:', categories.value)
		}
	} catch (error) {
		console.error('加载分类失败:', error)
		uni.showToast({
			title: '加载分类失败',
			icon: 'none'
		})
	}
}

// 加载壁纸列表
const loadWallpapers = async (isRefresh = false) => {
	if (loading.value) return

	try {
		loading.value = true

		// 如果是刷新，重置页码
		if (isRefresh) {
			currentPage.value = 1
			noMore.value = false
		}

		const params = {
			page: currentPage.value,
			pageSize: pageSize,
			type: type.value || 1
		}

		const userInfo = uni.getStorageSync('userInfo')
		if (userInfo?.id) {
			params.userId = userInfo.id
		}

		// 如果选择了具体分类（不是"最新"），则添加分类ID
		if (currentCategory.value !== null) {
			params.categoryId = currentCategory.value
		}

		console.log('加载壁纸参数:', params)

		const res = await getWallpaperList(params)
		console.log('壁纸API响应:', res)

		if (res.code === 200 && res.data) {
			const newList = res.data.list || []

			if (isRefresh) {
				// 刷新：替换数据
				imgList.value = newList
			} else {
				// 加载更多：追加数据
				imgList.value.push(...newList)
			}

			// 判断是否还有更多数据
			const total = res.data.total || 0
			const loadedCount = currentPage.value * pageSize
			noMore.value = loadedCount >= total

			console.log('壁纸加载成功，当前数量:', imgList.value.length, '总数:', total)
		} else {
			uni.showToast({
				title: '加载壁纸失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('加载壁纸失败:', error)
		uni.showToast({
			title: '加载壁纸失败',
			icon: 'none'
		})
	} finally {
		loading.value = false
	}
}

// 初始化为空数组，等待API加载数据
imgList.value = []

const displayList = computed(() => {
	return imgList.value.map(item => ({
		...item,
		type: type.value || 1
	}))
})

onLoad((options) => {
	title.value = options.name
	type.value = Number(options.type) || 1
	// 加载分类列表
	loadCategories()
	// 加载壁纸列表
	loadWallpapers()
})

// 切换分类
const switchCategory = async (categoryId) => {
	console.log('切换分类:', categoryId)

	await loadingBeforeNavigate(async () => {
		// 重置所有状态
		currentPage.value = 1
		noMore.value = false
		loading.value = false

		// 立即清空数据，避免显示旧数据
		imgList.value = []

		// 加载新分类的壁纸数据
		await loadWallpapers(true)

		console.log('分类切换完成，当前壁纸数量:', imgList.value.length)
	})
}

// 下拉刷新
const onRefresh = async () => {
	refreshing.value = true

	// 刷新壁纸数据
	await loadWallpapers(true)

	refreshing.value = false

	showSuccess('刷新成功')
}

// 加载更多
const loadMore = async () => {
	if (loading.value || noMore.value) {
		return
	}

	// 增加页码
	currentPage.value++

	// 加载更多数据
	await loadWallpapers(false)
}

// 返回上一级
const goBack = () => {
	safeNavigateBack()
}
</script>

<style lang="scss" scoped>
.category-phone {
	width: 100%;
	min-height: 100vh;
	background: #fafafa;
	display: flex;
	flex-direction: column;

	.back-btn {
		width: 56rpx;
		height: 56rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.category-phone-box {
		flex: 1;
		display: flex;
		flex-direction: column;
		overflow: hidden;
		width: 100%;

		// 壁纸列表
		.phone-list {
			flex: 1;
			padding: 0 30rpx 30rpx;
			width: 100%;
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
						border-top-color: #7a544d;
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
}
</style>