<template>
	<view class="category">
		<myTitleBar></myTitleBar>

		<view class="category-box">

			<!-- 壁纸分类 -->
			<view class="category-bz">
				<myTitle>
					<template #left>
						<view class="left">
							<view>壁纸分类</view>
						</view>
					</template>
				</myTitle>
				<!-- 一级分类 -->
				<view class="categorys">
					<view @click="goClassifyPage(item)" class="category-item" v-for="item in bzItems" :key="item.id">
						<view class="top">{{ item.name }}</view>
						<view class="bottom">{{ item.alias }}</view>
						<view class="icon">
							<uni-icons :type="item.iconPath" color="#eee"></uni-icons>
						</view>
					</view>
				</view>
			</view>

			<!-- 分类精选 -->
			<view class="category-theme">
				<myTitle>
					<template #left>
						<view class="left">
							<view>分类精选</view>
						</view>
					</template>
				</myTitle>

				<!-- 骨架屏 -->
				<view v-if="pageLoading" class="skeleton-wrapper">
					<view class="skeleton-item" v-for="i in 4" :key="i">
						<skeleton width="100%" height="200rpx" radius="20rpx" />
						<view class="skeleton-info">
							<skeleton width="150rpx" height="28rpx" radius="14rpx" />
							<skeleton width="100rpx" height="24rpx" radius="12rpx" />
						</view>
					</view>
				</view>

				<view v-else class="themes">
					<view v-if="imgList.length === 0" class="empty-tip">暂无精选分类</view>
					<themeItem v-else :items="imgList"></themeItem>
				</view>
			</view>

			<!-- 底部导航栏 -->
			<myTabBar></myTabBar>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import myTabBar from '@/components/myTabBar.vue'
import themeItem from '@/components/themeItem.vue'
import myTitle from '@/components/myTitle.vue'
import myTitleBar from '@/components/myTitleBar.vue'
import skeleton from '@/components/skeleton.vue'
import { getHotCategories } from '@/apis/category.js'

onShow(() => {
	uni.hideTabBar()
})

const pageLoading = ref(true)

const bzItems = ref([
	{
		"id": 1,
		"name": "手机壁纸",
		"type": "phone",
		"title": "手机壁纸",
		"alias": "Wallpaper",
		"iconPath": "right"
	},
	{
		"id": 2,
		"name": "专辑",
		"type": "album",
		"title": "专辑精选",
		"alias": "Special subject",
		"iconPath": "right"
	},
	{
		"id": 3,
		"name": "平板壁纸",
		"type": "tablet",
		"title": "平板壁纸",
		"alias": "Tablet computer",
		"iconPath": "right"
	},
	{
		"id": 4,
		"name": "头像",
		"type": "avatar",
		"title": "头像",
		"alias": "Head sculpture",
		"iconPath": "right"
	}
])

const imgList = ref([])

const formatUpdateTime = (updateTime) => {
	if (!updateTime) return '2天前更新'

	const now = new Date()
	const updateDate = new Date(updateTime)
	const diff = now - updateDate

	const days = Math.floor(diff / (1000 * 60 * 60 * 24))
	const hours = Math.floor(diff / (1000 * 60 * 60))

	if (days > 0) {
		return `${days}天前更新`
	} else if (hours > 0) {
		return `${hours}小时前更新`
	} else {
		return '刚刚更新'
	}
}

const loadHotCategories = async () => {
	try {
		const res = await getHotCategories()
		console.log('精选分类API响应:', res)

		if (res.code === 200 && res.data) {
			imgList.value = res.data.map(category => ({
				id: category.id,
				name: category.name,
				alias: category.alias || '',
				url: category.coverUrl || '/static/images/1.jpg',
				updateTime: formatUpdateTime(category.updateTime),
				likeCount: category.likeCount || 0,
				sortOrder: category.sortOrder || 0
			}))

			console.log('精选分类加载成功:', imgList.value)
		}
	} catch (error) {
		console.error('加载精选分类失败:', error)
		uni.showToast({
			title: '加载分类失败',
			icon: 'none'
		})
	}
}

onMounted(async () => {
	await loadHotCategories()
	pageLoading.value = false
})

onPullDownRefresh(async () => {
	pageLoading.value = true
	try {
		await loadHotCategories()
		uni.showToast({
			title: '刷新成功',
			icon: 'success',
			duration: 1500
		})
	} catch (e) {
		console.error('刷新失败:', e)
		uni.showToast({
			title: '刷新失败',
			icon: 'none'
		})
	} finally {
		pageLoading.value = false
		uni.stopPullDownRefresh()
	}
})

const goClassifyPage = (item) => {
	if (item.name === '专辑') {
		uni.navigateTo({
			url: '/pagesAlbum/index'
		})
		return
	}

	const typeMap = {
		'phone': 1,
		'tablet': 2,
		'avatar': 3
	}

	uni.navigateTo({
		url: `/pagesCategory/phone?type=${typeMap[item.type] || 1}&name=${item.name}`
	})
}
</script>

<style lang="scss" scoped>
.category {
	width: 100vw;
	min-height: 100vh;
	background: $self-background-color;
	position: relative;
	padding-bottom: calc(env(safe-area-inset-bottom) + 150rpx);
	display: flex;
	flex-direction: column;

	.category-box {
		padding: 0 30rpx;
	}

	.category-bz {
		font-size: 24rpx;
		margin-bottom: 20rpx;

		.categorys {
			width: 100%;
			gap: 12rpx;
			display: grid;
			grid-template-columns: repeat(2, 1fr);

			.category-item {
				font-size: 32rpx;
				border-radius: 20rpx;
				padding: 32rpx 28rpx;
				position: relative;
				overflow: hidden;
				box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
				transition: all 0.2s ease;

				&:active {
					transform: scale(0.97);
					box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.1);
				}

				.top {
					font-weight: 700;
					margin-bottom: 6rpx;
					position: relative;
					z-index: 1;
				}

				.bottom {
					font-size: 20rpx;
					opacity: 0.85;
					position: relative;
					z-index: 1;
				}

				.icon {
					:deep(.uni-icons) {
						position: absolute;
						right: -8rpx;
						top: -8rpx;
						transform: rotate(-45deg);
						color: rgba(255, 255, 255, 0.3);
						font-size: 80rpx !important;
					}
				}
			}

			.category-item:nth-child(1) {
				background: linear-gradient(135deg, #f5e6d3 0%, #e8dbcb 100%);
				color: #613942;
			}

			.category-item:nth-child(2) {
				background: linear-gradient(135deg, #f0c9c3 0%, #e2b9b3 100%);
				color: #fff;
			}

			.category-item:nth-child(3) {
				background: linear-gradient(135deg, #7d5442 0%, #613942 100%);
				color: #fff;
			}

			.category-item:nth-child(4) {
				background: linear-gradient(135deg, #9aaa85 0%, #839973 100%);
				color: #fff;
			}
		}
	}

	.category-theme {
		width: 100%;
		padding: 20rpx 0;

		.skeleton-wrapper {
			display: flex;
			flex-wrap: wrap;
			gap: 20rpx;

			.skeleton-item {
				width: calc(50% - 10rpx);

				.skeleton-info {
					display: flex;
					justify-content: space-between;
					margin-top: 16rpx;
				}
			}
		}

		.themes {
			width: 100%;
		}

		.empty-tip {
			text-align: center;
			padding: 60rpx 0;
			color: #999;
			font-size: 28rpx;
		}
	}
}
</style>
