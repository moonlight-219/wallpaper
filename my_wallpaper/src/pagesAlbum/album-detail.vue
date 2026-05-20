<template>
	<view class="album-detail">
		<myTitleBar :title="pageTitle">
			<view class="back-btn" @click="goBack">
				<uni-icons type="left" size="20" color="#534747"></uni-icons>
			</view>
		</myTitleBar>

		<view v-if="pageError" class="page-error">
			<text class="page-error-text">{{ pageError }}</text>
			<button class="retry-btn" @click="retryLoad">重新加载</button>
		</view>

		<view v-else class="album-content">
			<view class="album-area">
				<albumItem v-if="albumDisplay.id" :album="albumDisplay" :clickable="false"></albumItem>
				<view v-else-if="pageLoading" class="album-skeleton">加载中...</view>
			</view>

			<view class="tab-bar">
				<view 
					v-for="tab in tabList" 
					:key="tab.value" 
					class="tab-item" 
					:class="{ active: currentTab === tab.value }" 
					@click="switchTab(tab.value)"
				>
					{{ tab.label }}
				</view>
			</view>

			<scroll-view 
				class="album-scroll" 
				scroll-y 
				:refresher-enabled="true"
				:refresher-triggered="refreshing" 
				refresher-background="#fafafa" 
				lower-threshold="100"
				@refresherrefresh="onRefresh" 
				@scrolltolower="loadMore"
			>
				<view class="album-list">
					<imgItem v-if="currentTab === 1" :items="phoneImages"></imgItem>
					<tabletItem v-if="currentTab === 2" :items="tabletImages"></tabletItem>
					<avatarItem v-if="currentTab === 3" :items="avatarImages"></avatarItem>
				</view>
				<view class="load-more-wrap">
					<view v-if="tabLoading" class="loading-text">
						<text>加载中...</text>
					</view>
					<view v-else-if="tabNoMore && currentListLength > 0" class="no-more-text">
						<text>没有更多了</text>
					</view>
					<view v-else-if="!tabLoading && currentListLength === 0 && tabLoaded" class="no-more-text">
						<text>暂无壁纸</text>
					</view>
				</view>
			</scroll-view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import myTitleBar from '@/components/myTitleBar.vue'
import albumItem from '@/components/albumItem.vue'
import imgItem from '@/components/imgItem.vue'
import tabletItem from '@/components/tabletItem.vue'
import avatarItem from '@/components/avatarItem.vue'
import { getCategoryDetail } from '@/apis/category.js'
import { safeNavigateBack } from '@/utils/navigation.js'

const DEFAULT_COVER = '/pagesCategory/static/images/bgs/8.jpg'

const tabList = [
	{ label: '手机', value: 1 },
	{ label: '平板', value: 2 },
	{ label: '头像', value: 3 }
]

const categoryId = ref(null)
const pageError = ref('')
const pageLoading = ref(true)
const refreshing = ref(false)
const currentTab = ref(1)

const albumDisplay = ref({
	id: 0,
	name: '',
	url: DEFAULT_COVER,
	likeCount: 0,
	wallpaperCount: 0
})

const tabs = reactive({
	1: { items: [], page: 1, finished: false, loading: false, loaded: false },
	2: { items: [], page: 1, finished: false, loading: false, loaded: false },
	3: { items: [], page: 1, finished: false, loading: false, loaded: false }
})

const pageTitle = computed(() => albumDisplay.value.name || '壁纸专辑')
const phoneImages = computed(() => tabs[1].items)
const tabletImages = computed(() => tabs[2].items)
const avatarImages = computed(() => tabs[3].items)
const activeTab = computed(() => tabs[currentTab.value])
const tabLoading = computed(() => activeTab.value.loading)
const tabNoMore = computed(() => activeTab.value.finished)
const tabLoaded = computed(() => activeTab.value.loaded)
const currentListLength = computed(() => activeTab.value.items.length)

const mapWallpaperItem = (wallpaper, fallbackType) => {
	const typeNum = wallpaper.type != null ? Number(wallpaper.type) : fallbackType
	const raw = wallpaper.thumbnailUrl || wallpaper.url
	return {
		...wallpaper,
		id: wallpaper.id,
		url: wallpaper.url || raw || DEFAULT_COVER,
		thumbnailUrl: raw || DEFAULT_COVER,
		type: typeNum,
	}
}

const applyCategoryHeader = (data) => {
	if (!data) return
	const cover = data.coverUrl || DEFAULT_COVER
	albumDisplay.value = {
		id: data.id,
		name: data.name || '壁纸专辑',
		url: cover,
		likeCount: data.likeCount ?? 0,
		wallpaperCount: activeTab.value.items.length
	}
}

const updateHeaderWallpaperCount = () => {
	albumDisplay.value.wallpaperCount = activeTab.value.items.length
}

const fetchTabWallpapers = async (isRefresh, isLoadMore) => {
	const tabKey = currentTab.value
	const tab = tabs[tabKey]

	if (!categoryId.value) return false
	if (tab.loading) return false
	if (isLoadMore && tab.finished) return false

	if (isRefresh) {
		tab.page = 1
		tab.items = []
		tab.finished = false
	}

	if (isLoadMore) {
		tab.page += 1
	}

	tab.loading = true

	try {
		const res = await getCategoryDetail(categoryId.value, {
			type: tabKey,
			page: tab.page,
			pageSize: 20
		})

		if (res.code !== 200 || !res.data) {
			return false
		}

		const { data } = res
		const list = data.wallpapers || []
		console.log('加载壁纸列表:', list)
		const mapped = list.map((w) => mapWallpaperItem(w, tabKey))

		if (tab.page === 1) {
			tab.items = mapped
		} else {
			tab.items.push(...mapped)
		}

		if (mapped.length < 20) {
			tab.finished = true
		}

		applyCategoryHeader(data)
		updateHeaderWallpaperCount()
		tab.loaded = true
		return true
	} catch (e) {
		console.error('加载分类详情失败:', e)
		if (tab.page > 1) {
			tab.page -= 1
		}
		return false
	} finally {
		tab.loading = false
	}
}

const loadInitial = async () => {
	pageError.value = ''
	pageLoading.value = true

	if (!categoryId.value) {
		pageError.value = '缺少专辑参数'
		pageLoading.value = false
		return
	}

	try {
		const ok = await fetchTabWallpapers(true, false)
		if (!ok) {
			pageError.value = '加载失败，请稍后重试'
		}
	} finally {
		pageLoading.value = false
	}
}

const switchTab = (tab) => {
	if (currentTab.value === tab) return
	currentTab.value = tab
	const t = tabs[tab]
	if (!t.loaded && !t.loading) {
		fetchTabWallpapers(true, false)
	}
	updateHeaderWallpaperCount()
}

const loadMore = async () => {
	if (pageLoading.value || pageError.value) return
	await fetchTabWallpapers(false, true)
}

const onRefresh = async () => {
	refreshing.value = true
	try {
		await fetchTabWallpapers(true, false)
	} finally {
		refreshing.value = false
	}
}

const retryLoad = () => {
	loadInitial()
}

const goBack = () => {
	safeNavigateBack()
}

onLoad((options) => {
	const id = options.id != null && options.id !== '' ? Number(options.id) : NaN
	if (!Number.isFinite(id) || id <= 0) {
		pageError.value = '无效的专辑ID'
		pageLoading.value = false
		uni.showToast({ title: '无效的专辑', icon: 'none' })
		return
	}
	categoryId.value = id
	loadInitial()
})
</script>

<style lang="scss" scoped>
.album-detail {
	background: $self-background-color;
	width: 100vw;
	min-height: 100vh;
	display: flex;
	flex-direction: column;

	.back-btn {
		width: 56rpx;
		height: 56rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.page-error {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 60rpx;

		.page-error-text {
			font-size: 28rpx;
			color: #999;
			margin-bottom: 40rpx;
		}

		.retry-btn {
			font-size: 28rpx;
			background: #7a544d;
			color: #fff;
		}
	}

	.album-content {
		flex: 1;
		display: flex;
		flex-direction: column;
		min-height: 0;

		.album-area {
			height: 400rpx;
			padding: 20rpx 30rpx;
			flex-shrink: 0;

			.album-skeleton {
				height: 100%;
				display: flex;
				align-items: center;
				justify-content: center;
				color: #999;
				font-size: 28rpx;
				border-radius: 20rpx;
				background: #f5f0ed;
			}
		}

		.tab-bar {
			display: flex;
			justify-content: center;
			align-items: center;
			padding: 20rpx 30rpx;
			gap: 30rpx;
			flex-shrink: 0;

			.tab-item {
				padding: 10rpx 40rpx;
				border-radius: 40rpx;
				font-size: 28rpx;
				color: #7a544d;
				background-color: #f5f0ed;
				transition: all 0.3s;

				&.active {
					background-color: #7a544d;
					color: #fff;
				}
			}
		}

		.album-scroll {
			flex: 1;
			height: 0;
			width: 100%;
		}

		.album-list {
			width: 100%;
			padding: 0 30rpx;
		}

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
</style>
