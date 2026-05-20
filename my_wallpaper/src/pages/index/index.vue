<template>
	<view class="container">
		<myTitleBar></myTitleBar>

		<view class="content-wrapper">
			<!-- 骨架屏 -->
			<view class="skeleton-wrapper" v-if="pageLoading">
				<view class="skeleton-section">
					<skeleton width="100%" height="300rpx" radius="20rpx" />
				</view>

				<view class="skeleton-section">
					<skeleton width="200rpx" height="36rpx" radius="18rpx" />
					<view class="skeleton-row">
						<skeleton shape="circle" width="120rpx" v-for="i in 5" :key="'a' + i" />
					</view>
				</view>

				<view class="skeleton-section">
					<skeleton width="200rpx" height="36rpx" radius="18rpx" />
					<view class="skeleton-row">
						<skeleton width="200rpx" height="200rpx" radius="30rpx" v-for="i in 4" :key="'b' + i" />
					</view>
				</view>

				<view class="skeleton-section">
					<skeleton width="200rpx" height="36rpx" radius="18rpx" />
					<view class="skeleton-row">
						<skeleton width="320rpx" height="200rpx" radius="20rpx" v-for="i in 3" :key="'c' + i" />
					</view>
				</view>

				<view class="skeleton-section">
					<skeleton width="200rpx" height="36rpx" radius="18rpx" />
					<view class="skeleton-grid">
						<skeleton width="calc(50% - 8rpx)" :height="i % 2 === 0 ? '450rpx' : '350rpx'" radius="30rpx" v-for="i in 6" :key="'d' + i" />
					</view>
				</view>
			</view>

			<!-- 真实内容 -->
			<view v-else>
				<!-- 轮播图 -->
				<view class="swiper">
					<mySwiper :items="homeData.swiperList"></mySwiper>
				</view>

				<!-- 推荐创作者 -->
				<view class="hot-author">
					<myTitle>
						<template #left>
							<view class="left">
								推荐创作者
							</view>
						</template>
						<template #right>
							<view class="right" @click="handleMoreAuthor()">
								More+
							</view>
						</template>
					</myTitle>
					<!-- 创作者 -->
					<scroll-view :show-scrollbar="false" scroll-x class="author">
						<view @click="goAuthorDetailPage(item.id)" class="author-item" v-for="item in homeData.authors"
							:key="item.id">
							<image lazy-load class="pic" :src="item.avatarUrl" mode="aspectFill"></image>
						</view>
					</scroll-view>
				</view>

				<!-- 热门头像 -->
				<view class="hot-avatar">
					<myTitle>
						<template #left>
							<view class="left">
								热门头像
							</view>
						</template>
						<template #right>
							<view class="right" @click="handleMoreAvatar()">
								More+
							</view>
						</template>
					</myTitle>
					<scroll-view scroll-x class="avatars">
						<view class="avatar-item" v-for="(item, index) in homeData.avatars" :key="item.id">
							<image @click="goAvatarPreview(item, index)" lazy-load class="avatar-img"
								:src="item.thumbnailUrl || item.url" mode="aspectFill">
							</image>
						</view>
					</scroll-view>
				</view>

				<!-- 电脑平板 -->
				<view class="hot-pcs">
					<myTitle>
						<template #left>
							<view class="left">
								电脑平板
							</view>
						</template>
						<template #right>
							<view class="right" @click="handleMorePcs()">
								More+
							</view>
						</template>
					</myTitle>
					<scroll-view scroll-x class="pics">
						<view class="pc-item" v-for="(item, index) in homeData.tablets" :key="item.id">
							<image @click="goPreviewPage(item, index)" class="pc-img" lazy-load :src="item.thumbnailUrl || item.url"
								mode="aspectFill">
							</image>
						</view>
					</scroll-view>
				</view>

				<!-- 每日推荐 -->
				<view class="theme-likes">
					<myTitle>
						<template #left>
							<view class="left">
								每日推荐
							</view>
						</template>
						<template #right>
							<view class="right" @click="handleMoreCategory()">
								More+
							</view>
						</template>
					</myTitle>

					<view class="list">
						<imgItem :items="dailyRecommendList"></imgItem>
					</view>
				</view>

				<!-- 加载状态提示 -->
				<view class="load-more-wrap">
					<view v-if="loading" class="loading-text">
						<text>加载中...</text>
					</view>
					<view v-else-if="noMore" class="no-more-text">
						<text>没有更多了</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 返回顶部按钮 -->
		<view class="back-top-btn" v-if="showBackTop" @click="handleBackTop">
			<text>↑</text>
		</view>

		<!-- 底部导航 -->
		<myTabBar></myTabBar>
	</view>
</template>

<script setup>
import {
	ref, computed, onMounted
} from "vue";
import { onShow, onPullDownRefresh, onReachBottom, onPageScroll } from '@dcloudio/uni-app'
import myTabBar from '@/components/myTabBar.vue'
import myTitle from '@/components/myTitle.vue'
import mySwiper from '@/components/mySwiper.vue'
import imgItem from '@/components/imgItem.vue'
import myTitleBar from '@/components/myTitleBar.vue'
import skeleton from '@/components/skeleton.vue'
import { getHomeData, getDailyRecommend } from '@/apis/home.js'
import { setPreviewList } from '@/store/previewStore.js'

onShow(() => {
	uni.hideTabBar()
})

const loading = ref(false)
const noMore = ref(false)
const pageLoading = ref(true)
const currentPage = ref(1)
const pageSize = 10
const maxPages = 5

const showBackTop = ref(false)

onPageScroll((e) => {
	showBackTop.value = e.scrollTop > 300
})

const handleBackTop = () => {
	uni.pageScrollTo({
		scrollTop: 0,
		duration: 300
	})
}

const homeData = ref({
	swiperList: [],
	authors: [],
	avatars: [],
	tablets: []
})
const dailyRecommendList = ref([])
const imgList = ref([])

const loadHomeData = async () => {
	try {
		const res = await getHomeData()
		if (res.code === 200) {
			homeData.value = {
				swiperList: (res.data.carousel || []).map(item => ({
					...item,
					url: item.coverUrl
				})),
				authors: res.data.hotAuthors || [],
				avatars: res.data.hotAvatars || [],
				tablets: res.data.hotPcs || []
			}
			imgList.value = [
				...homeData.value.authors.map(item => ({
					id: item.id,
					url: item.avatarUrl,
					type: 'avatar'
				})),
				...homeData.value.avatars.map(item => ({
					...item
				})),
				...homeData.value.tablets.map(item => ({
					...item
				}))
			]
		}
	} catch (e) {
		console.error('加载首页数据失败:', e)
	}
}

const loadDailyRecommend = async () => {
	try {
		const res = await getDailyRecommend(1, 10)
		if (res.code === 200) {
			const list = res.data.list || []
			dailyRecommendList.value = list.map(item => ({
				...item
			}))
		}
	} catch (e) {
		console.error('加载每日推荐失败:', e)
	}
}

onMounted(async () => {
	try {
		await Promise.all([loadHomeData(), loadDailyRecommend()])
	} catch (e) {
		console.error('加载数据失败:', e)
	} finally {
		pageLoading.value = false
	}
})

const handleMoreAuthor = () => {
	uni.reLaunch({
		url: '/pages/author/index'
	})
}

const goAuthorDetailPage = (authorId) => {
	uni.navigateTo({
		url: `/pagesAuthor/author-detail?id=${authorId}`
	})
}

const goAvatarPreview = (item, index) => {
	setPreviewList([...homeData.value.avatars])
	uni.navigateTo({
		url: `/pages/preview/preview-detail?wallpaperId=${item.id}&type=${item.type}`
	})
}

const goPreviewPage = (item, index) => {
	setPreviewList([...homeData.value.tablets])
	uni.navigateTo({
		url: `/pages/preview/preview-detail?wallpaperId=${item.id}&type=${item.type}`
	})
}

const handleMoreAvatar = () => {
	const data = {
		name: '头像',
		type: 1
	}
	uni.navigateTo({
		url: `/pagesCategory/phone?type=${data.type}&name=${data.name}`
	})
}

const handleMorePcs = () => {
	const data = {
		name: '平板壁纸',
		type: 2
	}
	uni.navigateTo({
		url: `/pagesCategory/phone?type=${data.type}&name=${data.name}`
	})
}

const handleMoreCategory = () => {
	const data = {
		name: '手机壁纸',
		type: 3
	}
	uni.navigateTo({
		url: `/pagesCategory/phone?type=${data.type}&name=${data.name}`
	})
}

onPullDownRefresh(async () => {
	try {
		await Promise.all([loadHomeData(), loadDailyRecommend()])
		currentPage.value = 1
		noMore.value = false
		uni.showToast({
			title: '刷新成功',
			icon: 'success',
			duration: 1500
		})
	} catch (e) {
		console.error('刷新失败:', e)
	} finally {
		uni.stopPullDownRefresh()
	}
})

onReachBottom(async () => {
	if (loading.value || noMore.value) {
		return
	}

	if (currentPage.value >= maxPages) {
		noMore.value = true
		return
	}

	loading.value = true

	try {
		const res = await getDailyRecommend(currentPage.value + 1, pageSize)
		if (res.code === 200) {
			const list = res.data.list || []
			if (list.length === 0) {
				noMore.value = true
			} else {
				const newData = list.map(item => ({
					...item
				}))
				dailyRecommendList.value.push(...newData)
				currentPage.value++

				if (currentPage.value >= maxPages) {
					noMore.value = true
				}
			}
		}
	} catch (e) {
		console.error('加载更多失败:', e)
	} finally {
		loading.value = false
	}
})
</script>

<style scoped lang="scss">
.container {
	width: 100%;
	min-height: 100vh;
	background: $self-background-color;
	padding-bottom: calc(env(safe-area-inset-bottom) + 150rpx);

	.content-wrapper {
		padding: 0 30rpx;
	}

	// 骨架屏样式
	.skeleton-wrapper {
		padding-top: 20rpx;

		.skeleton-section {
			margin-bottom: 30rpx;
		}

		.skeleton-row {
			display: flex;
			gap: 20rpx;
			margin-top: 20rpx;
		}

		.skeleton-grid {
			display: flex;
			flex-wrap: wrap;
			gap: 16rpx;
			margin-top: 20rpx;

			> view {
				flex-shrink: 0;
			}
		}
	}

	// 轮播图
	.swiper {
		margin-bottom: 20rpx;
		height: 350rpx;
		margin-top: 15rpx;
	}

	// 推荐创作者
	.hot-author {
		margin-bottom: 20rpx;

		.author {
			white-space: nowrap;
			margin-top: 20rpx;

			.author-item {
				display: inline-block;
				margin-right: 20rpx;

				.pic {
					width: 120rpx;
					height: 120rpx;
					border-radius: 50%;
				}
			}
		}
	}

	// 热门头像
	.hot-avatar {
		margin-bottom: 20rpx;

		.avatars {
			white-space: nowrap;
			margin-top: 20rpx;

			.avatar-item {
				display: inline-block;
				margin-right: 20rpx;

				.avatar-img {
					width: 200rpx;
					height: 200rpx;
					border-radius: 30rpx;
				}
			}
		}
	}

	// 电脑平板
	.hot-pcs {
		margin-bottom: 20rpx;

		.pics {
			white-space: nowrap;
			margin-top: 20rpx;

			.pc-item {
				display: inline-block;
				margin-right: 20rpx;

				.pc-img {
					width: 320rpx;
					height: 200rpx;
					border-radius: 20rpx;
				}
			}
		}
	}

	// 每日推荐
	.theme-likes {
		margin-bottom: 20rpx;

		.list {
			margin-top: 20rpx;
		}
	}

	// 加载状态
	.load-more-wrap {
		text-align: center;
		padding: 20rpx 0;

		.loading-text,
		.no-more-text {
			color: #999;
			font-size: 24rpx;
		}
	}

	// 返回顶部按钮
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
