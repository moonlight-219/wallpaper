<template>
	<view class="author-detail">
		<myTitleBar title="用户详情">
			<template #default>
				<uni-icons type="left" @click="goBack()" size="18"></uni-icons>
			</template>
		</myTitleBar>
		<view class="author-detail-box">
			<!-- 用户信息+简介区域 - 骨架屏 -->
			<view class="userinfo-skeleton" v-if="pageLoading">
				<view class="skeleton-card">
					<view class="skeleton-user-row">
						<skeleton shape="circle" width="150rpx" />
						<view class="skeleton-user-col">
							<skeleton width="200rpx" height="42rpx" radius="10rpx" />
							<view class="skeleton-stats-row">
								<skeleton width="80rpx" height="32rpx" radius="6rpx" />
								<skeleton width="60rpx" height="26rpx" radius="6rpx" />
								<skeleton width="80rpx" height="32rpx" radius="6rpx" />
								<skeleton width="60rpx" height="26rpx" radius="6rpx" />
								<skeleton width="80rpx" height="32rpx" radius="6rpx" />
								<skeleton width="60rpx" height="26rpx" radius="6rpx" />
							</view>
						</view>
					</view>
					<skeleton width="60%" height="28rpx" radius="8rpx" />
				</view>
			</view>
			<!-- 用户信息区域 - 实际内容 -->
			<template v-else>
				<view class="userinfo-area">
					<view class="user-avatar">
						<image class="avatar-img" :src="authorInfo.avatarUrl || defaultAvatar" mode="aspectFill" @error="handleAvatarError"></image>
					</view>
					<view class="userinfo-right">
						<view class="username">{{ authorInfo.name }}</view>
						<view class="userinfo-items">
							<view class="item">
								<view class="item-num">{{ authorInfo.workCount }}</view>
								<view class="item-name">作品</view>
							</view>
							<view class="item">
								<view class="item-num">{{ authorInfo.totalLikes }}</view>
								<view class="item-name">点赞</view>
							</view>
							<view class="item">
								<view class="item-num">{{ authorInfo.totalCollects }}</view>
								<view class="item-name">收藏</view>
							</view>
						</view>
					</view>
				</view>
				<!-- 个人简介 - 独立区域 -->
				<view class="bio-section" v-if="authorInfo.bio">
					<text class="bio-text">" {{ authorInfo.bio }} "</text>
				</view>
			</template>
			<view class="author-bottom-box">
				<view class="tab-wrap">
					<CategoryNav v-model="activeTab" :items="tabItems" :scrollable="false" @change="switchTab" />
				</view>
				<scroll-view
					class="author-scroll"
					scroll-y
					lower-threshold="120"
					:refresher-enabled="true"
					:refresher-triggered="refreshing"
					refresher-background="transparent"
					@scrolltolower="onScrollToLower"
					@refresherrefresh="onPullRefresh"
				>
					<view class="imgs-list" v-if="currentTabState.loaded && !pageLoading">
						<imgItem v-if="tabNum === 1" :items="displayList"></imgItem>
						<tabletItem v-else-if="tabNum === 2" :items="displayList"></tabletItem>
						<avatarItem v-else-if="tabNum === 3" :items="displayList"></avatarItem>
					</view>
					<view class="skeleton-list" v-else>
						<!-- 手机壁纸骨架：3列竖向 -->
						<view class="skeleton-grid-phone" v-if="tabNum === 1">
							<view class="skeleton-grid-item" v-for="i in 6" :key="i">
								<skeleton width="100%" height="380rpx" radius="20rpx" />
							</view>
						</view>
						<!-- 平板壁纸骨架：2列横向 -->
						<view class="skeleton-grid-tablet" v-else-if="tabNum === 2">
							<view class="skeleton-grid-item" v-for="i in 4" :key="i">
								<skeleton width="100%" height="180rpx" radius="20rpx" />
							</view>
						</view>
						<!-- 头像骨架：2列方形 -->
						<view class="skeleton-grid-avatar" v-else>
							<view class="skeleton-grid-item" v-for="i in 6" :key="i">
								<skeleton width="100%" height="330rpx" radius="20rpx" />
							</view>
						</view>
					</view>
					<view class="load-footer">
						<text v-if="currentTabState.loading" class="load-text">加载中...</text>
						<text v-else-if="!currentTabState.hasMore && displayList.length > 0" class="load-text">没有更多了</text>
						<text v-else-if="!currentTabState.loading && displayList.length === 0 && currentTabState.loaded" class="load-text">暂无作品</text>
					</view>
				</scroll-view>
			</view>
		</view>
	</view>
</template>



<script setup>
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import myTitleBar from '@/components/myTitleBar.vue'
import imgItem from '@/components/imgItem.vue'
import tabletItem from '@/components/tabletItem.vue'
import avatarItem from '@/components/avatarItem.vue'
import CategoryNav from '@/components/categoryNav.vue'
import skeleton from '@/components/skeleton.vue'
import { getAuthorDetail } from '@/apis/author'
import { safeNavigateBack } from '@/utils/navigation.js'

const authorId = ref(null)
const pageSize = 20
const pageLoading = ref(true)
const skeletonCount = computed(() => tabNum.value === 1 ? 6 : tabNum.value === 2 ? 4 : 6)

const parseAuthorRouteId = (options) => {
	const raw = options?.id ?? options?.userId
	if (raw === undefined || raw === null || raw === '') return null
	const s = String(raw).trim()
	if (!s || s.toLowerCase() === 'null' || s.toLowerCase() === 'undefined') return null
	const n = Number(s)
	if (!Number.isFinite(n) || n <= 0) return null
	return String(Math.trunc(n))
}

const authorInfo = ref({
	id: 0,
	name: '',
	avatarUrl: '',
	bio: '',
	workCount: 0,
	totalLikes: 0,
	totalCollects: 0
})

const defaultAvatar = '/static/images/1.jpg'

const handleAvatarError = (e) => {
	console.warn('头像加载失败，使用默认头像')
	authorInfo.value.avatarUrl = defaultAvatar
}

const activeTab = ref(1)
const tabNum = computed(() => Number(activeTab.value) || 1)

const tabItems = ref([
	{ label: '手机', value: 1 },
	{ label: '平板', value: 2 },
	{ label: '头像', value: 3 }
])

const createTabState = () => {
	return {
		items: [],
		page: 0,
		hasMore: true,
		loading: false,
		loaded: false
	}
}

const tabs = reactive({
	1: createTabState(),
	2: createTabState(),
	3: createTabState()
})

const displayList = computed(() => tabs[tabNum.value].items)
const currentTabState = computed(() => tabs[tabNum.value])

const refreshing = ref(false)

const mapWallpaper = (w, type) => {
	return {
		id: w.id,
		type,
		url: w.url,
		thumbnailUrl: w.thumbnailUrl || w.url,
		coverUrl: w.thumbnailUrl,
		title: w.title,
		likeCount: w.likeCount,
		collectCount: w.collectCount
	}
}

const fetchTabPage = async (tab, reset) => {
	if (!authorId.value) return

	const t = tabs[tab]
	if (t.loading) return
	if (!reset && !t.hasMore) return

	const nextPage = reset ? 1 : t.page + 1
	if (!reset && nextPage < 1) return

	t.loading = true
	try {
		const res = await getAuthorDetail(authorId.value, {
			page: nextPage,
			pageSize,
			type: tab
		})

		if (res.code !== 200 || !res.data) {
			return
		}

		// 只在第一次加载时设置作者基本信息
		if (!authorInfo.value.id || authorInfo.value.id === 0) {
			authorInfo.value = {
				id: res.data.id,
				name: res.data.name,
				avatarUrl: res.data.avatarUrl,
				bio: res.data.bio || '',
				workCount: res.data.workCount || 0,
				totalLikes: res.data.totalLikes || 0,
				totalCollects: res.data.totalCollects || 0
			}
			pageLoading.value = false
		}

		const wallpapers = res.data.wallpapers || []
		const mapped = wallpapers.map((w) => mapWallpaper(w, tab))

		if (reset || nextPage === 1) {
			t.items = mapped
		} else {
			t.items.push(...mapped)
		}

		t.items = t.items.map(item => ({...item, author: authorInfo.value}))

		t.page = nextPage
		t.hasMore = wallpapers.length >= pageSize
		t.loaded = true
	} catch (error) {
		console.error('加载作者作品失败:', error)
		uni.showToast({ title: '加载失败', icon: 'none' })
	} finally {
		t.loading = false
	}
}

const switchTab = async (tab) => {
	const key = Number(tab) || 1
	activeTab.value = key
	const t = tabs[key]
	if (!t.loaded) {
		await fetchTabPage(key, true)
	}
}

const onScrollToLower = () => {
	const key = tabNum.value
	const t = tabs[key]
	if (t.loading || !t.hasMore) return
	fetchTabPage(key, false)
}

const onPullRefresh = async () => {
	if (refreshing.value) return
	refreshing.value = true
	try {
		await fetchTabPage(tabNum.value, true)
	} finally {
		refreshing.value = false
	}
}

onLoad((options) => {
	const resolved = parseAuthorRouteId(options)
	if (!resolved) {
		uni.showToast({ title: '缺少作者参数', icon: 'none' })
		return
	}
	authorId.value = resolved
	fetchTabPage(tabNum.value, true)
})

const goBack = () => {
	safeNavigateBack()
}
</script>
<style lang="scss" scoped>
.author-detail {
	height: 100vh;
	width: 100%;
	background: $self-background-color;
	overflow: hidden;

	.author-detail-box {
		display: flex;
		flex-direction: column;
		height: 100%;

		.userinfo-skeleton {
			padding: 20rpx 30rpx;
			flex-shrink: 0;

			.skeleton-card {
				background: #fff;
				border-radius: 24rpx;
				padding: 30rpx;
				box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

				.skeleton-user-row {
					display: flex;
					align-items: center;
					gap: 24rpx;
					margin-bottom: 20rpx;

					.skeleton-user-col {
						flex: 1;
						display: flex;
						flex-direction: column;
						gap: 18rpx;

						.skeleton-stats-row {
							display: flex;
							gap: 20rpx;
							flex-wrap: wrap;
						}
					}
				}
			}
		}

		.userinfo-area {
			padding: 30rpx 30rpx 10rpx;
			display: flex;
			gap: 28rpx;
			height: 220rpx;
			flex-shrink: 0;

			.user-avatar {
				width: 150rpx;
				height: 150rpx;

				.avatar-img {
					width: 100%;
					height: 100%;
					object-fit: cover;
					border-radius: 50%;
					border: 5rpx solid #fff;
					box-shadow: 0 8rpx 20rpx rgba(125, 84, 66, 0.15), 0 2rpx 8rpx rgba(125, 84, 66, 0.08);
				}
			}

		.userinfo-right {
				padding: 15rpx 0;
				flex: 1;

				.username {
					font-size: 42rpx;
					color: #7d5442;
					padding-bottom: 20rpx;
					font-weight: 800;
					letter-spacing: 1rpx;
				}

				.userinfo-items {
					display: flex;
					font-size: 28rpx;
					gap: 40rpx;

					.item {
						display: flex;
						gap: 10rpx;
						align-items: baseline;

						.item-num {
							color: #7d5442;
							font-weight: 800;
							font-size: 32rpx;
						}

						.item-name {
							color: #999;
							font-size: 26rpx;
						}
					}
				}
			}
		}

		.bio-section {
			padding: 10rpx 30rpx 20rpx;
			display: flex;
			align-items: flex-start;
			gap: 8rpx;
			flex-shrink: 0;

			.bio-text {
				flex: 1;
				font-size: 26rpx;
				color: #888;
				line-height: 1.6;
				font-style: italic;
				letter-spacing: 1rpx;
				display: -webkit-box;
				-webkit-line-clamp: 2;
				-webkit-box-orient: vertical;
				overflow: hidden;
				text-overflow: ellipsis;
			}
		}

		.author-bottom-box {
			border-radius: 36rpx 36rpx 0 0;
			padding: 30rpx;
			background: linear-gradient(180deg, #5d4f4f 0%, #534747 100%);
			flex: 1;
			min-height: 0;
			display: flex;
			flex-direction: column;
			box-shadow: 0 -8rpx 24rpx rgba(0, 0, 0, 0.12), 0 -2rpx 8rpx rgba(0, 0, 0, 0.06);

			.tab-wrap {
				margin-bottom: 24rpx;
				flex-shrink: 0;
			}

			:deep(.category-nav-wrapper) {
				.flex-nav {
					padding: 0;
					gap: 16rpx;

					.nav-item {
						background-color: rgba(255, 255, 255, .2);
						color: rgba(255, 255, 255, 0.7);
						height: 68rpx;
						line-height: 68rpx;
						font-weight: 500;
						transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

						&.active {
							background-color: #9b8180;
							color: #fff;
							font-weight: 700;
							box-shadow: 0 6rpx 16rpx rgba(155, 129, 128, 0.5), 0 2rpx 8rpx rgba(155, 129, 128, 0.3);
							transform: scale(1.02);
						}
					}
				}
			}

			.author-scroll {
				flex: 1;
				height: 0;
				min-height: 0;
				width: 100%;
			}

			.imgs-list {
				width: 100%;
				padding-bottom: 20rpx;
			}

			.load-footer {
				padding: 24rpx 0 40rpx;
				display: flex;
				justify-content: center;

				.load-text {
					font-size: 24rpx;
					color: rgba(255, 255, 255, 0.55);
				}
			}
		}
	}
}

.skeleton-list {
	width: 100%;
	padding: 10rpx;

	.skeleton-grid-phone {
		display: flex;
		flex-wrap: wrap;
		gap: 10rpx;

		.skeleton-grid-item {
			width: calc(33.33% - 7rpx);
			padding: 5rpx;
		}
	}

	.skeleton-grid-tablet {
		display: flex;
		flex-wrap: wrap;
		gap: 10rpx;

		.skeleton-grid-item {
			width: calc(50% - 5rpx);
			padding: 5rpx;
		}
	}

	.skeleton-grid-avatar {
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		gap: 15rpx;
		padding: 10rpx 0;

		.skeleton-grid-item {
			width: 100%;
		}
	}
}
</style>


