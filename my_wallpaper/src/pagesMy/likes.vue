<template>
	<view class="wallpaper-list">
		<myTitleBar title="我的喜欢">
			<template #default>
				<uni-icons type="left" size="20" @click="goBack()"></uni-icons>
			</template>
		</myTitleBar>
		<view class="type-tabs">
			<view class="tab-item" :class="{ active: currentType === 'wallpaper' }" @click="switchType('wallpaper')">
				壁纸
			</view>
			<view class="tab-item" :class="{ active: currentType === 'work' }" @click="switchType('work')">
				作品集
			</view>
		</view>
		<scroll-view class="scroll-area" scroll-y lower-threshold="120" :refresher-enabled="true"
			:refresher-triggered="refreshing" refresher-background="transparent" @scrolltolower="onScrollToLower"
			@refresherrefresh="onPullRefresh">
			<view class="content">
				<view class="count-bar" v-if="list.length > 0">共 {{ list.length }} {{ currentType === 'wallpaper' ? '张壁纸' :
					'个作品集'
				}}</view>
				<view class="grid" v-if="list.length > 0">
					<view class="grid-item" v-for="(item, index) in list" :key="item.id" @click="goPreview(item)">
						<image class="thumb" :src="item.thumbnailUrl || item.url" mode="aspectFill" lazy-load></image>
						<view class="item-info">
							<view class="item-title">{{ item.title }}</view>
							<view class="item-meta">
								<text class="meta-num">{{ item.likeTime || '' }}</text>
							</view>
						</view>
						<view class="remove-btn" @click.stop="removeLike(index)">
							<uni-icons type="heart-filled" size="16" color="#e2b9b3"></uni-icons>
						</view>
					</view>
				</view>
				<view class="load-footer">
					<text v-if="loading" class="load-text">加载中...</text>
					<text v-else-if="!hasMore && list.length > 0" class="load-text">没有更多了</text>
					<text v-else-if="!loading && list.length === 0 && loaded" class="load-text">暂无喜欢记录</text>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import myTitleBar from '@/components/myTitleBar.vue'
import { isLoggedIn, redirectToLoginReplace } from '@/utils/auth.js'
import { getUserLikes } from '@/apis/user.js'
import { likeWallpaper } from '@/apis/wallpaper.js'
import { toggleWorkLike } from '@/apis/square.js'
import { formatDate } from '@/utils/date.js'
import { safeNavigateBack } from '@/utils/navigation.js'

const list = ref([])
const page = ref(1)
const pageSize = 20
const hasMore = ref(true)
const loading = ref(false)
const loaded = ref(false)
const refreshing = ref(false)
const total = ref(0)
const currentType = ref('wallpaper')

const fetchList = async (reset = false) => {
	if (loading.value) return
	if (!reset && !hasMore.value) return

	const userId = uni.getStorageSync('userId')
	if (!userId) return

	loading.value = true
	const currentPage = reset ? 1 : page.value + 1

	try {
		const res = await getUserLikes(userId, {
			page: currentPage,
			pageSize,
			type: currentType.value
		})
		if (res.code === 200 && res.data) {
			const records = res.data.records || res.data.list || res.data || []
			const mapped = records.map((item) => ({
				id: item.wallpaperId || item.id,
				title: item.title || '未命名',
				url: item.url || item.thumbnailUrl || '',
				thumbnailUrl: item.thumbnailUrl || item.url || '',
				imageWidth: item.imageWidth,
				imageHeight: item.imageHeight,
				fileFormat: item.fileFormat,
				fileSize: item.fileSize,
				type: item.type,
				categoryId: item.categoryId,
				category: item.category,
				likeCount: item.likeCount || 0,
				collectCount: item.collectCount || 0,
				downloadCount: item.downloadCount || 0,
				isLiked: item.isLiked ?? false,
				isCollected: item.isCollected ?? false,
				author: item.author || null,
				likeTime: formatDate(item.likeTime || item.createdAt || item.createTime, 'YYYY-MM-DD'),
				dataType: currentType.value
			}))
			if (reset || currentPage === 1) {
				list.value = mapped
			} else {
				list.value.push(...mapped)
			}
			page.value = currentPage
			total.value = res.data.total || mapped.length
			hasMore.value = mapped.length >= pageSize
			loaded.value = true
		}
	} catch (error) {
		console.error('获取喜欢列表失败:', error)
		uni.showToast({ title: '加载失败', icon: 'none' })
	} finally {
		loading.value = false
	}
}

const switchType = (type) => {
	if (currentType.value === type) return
	currentType.value = type
	page.value = 0
	hasMore.value = true
	loaded.value = false
	fetchList(true)
}

const onScrollToLower = () => {
	if (loading.value || !hasMore.value) return
	fetchList(false)
}

const onPullRefresh = async () => {
	if (refreshing.value) return
	refreshing.value = true
	try {
		await fetchList(true)
	} finally {
		refreshing.value = false
	}
}

const goPreview = (item) => {
	if (!item.id) {
		uni.showToast({ title: 'ID不存在', icon: 'none' })
		return
	}

	if (item.dataType === 'work') {
		uni.navigateTo({
			url: `/pages/preview/preview-detail?workId=${item.id}`,
			fail: (err) => {
				console.error('跳转失败:', err)
				uni.showToast({ title: '跳转失败', icon: 'none' })
			}
		})
	} else {
		uni.navigateTo({
			url: `/pages/preview/preview-detail?wallpaperId=${item.id}&type=${item.type || 1}&from=likes`,
			fail: (err) => {
				console.error('跳转失败:', err)
				uni.showToast({ title: '跳转失败', icon: 'none' })
			}
		})
	}
}

const removeLike = async (index) => {
	const item = list.value[index]

	try {
		let res
		if (item.dataType === 'work') {
			res = await toggleWorkLike(item.id)
		} else {
			res = await likeWallpaper(item.id)
		}

		if (res.code === 200) {
			// 从列表中移除
			list.value.splice(index, 1)
			total.value = Math.max(0, total.value - 1)
			uni.showToast({ title: '已取消喜欢', icon: 'success' })
		} else {
			uni.showToast({ title: res.message || '操作失败', icon: 'none' })
		}
	} catch (error) {
		console.error('取消喜欢失败:', error)
		uni.showToast({ title: '操作失败，请稍后重试', icon: 'none' })
	}
}

const goBack = () => {
	safeNavigateBack()
}

onLoad(() => {
	if (!isLoggedIn()) {
		redirectToLoginReplace('/pagesMy/likes')
		return
	}
	fetchList(true)
})

onShow(() => {
	if (!isLoggedIn()) {
		redirectToLoginReplace('/pagesMy/likes')
		return
	}
	if (loaded.value) {
		fetchList(true)
	}
})
</script>

<style lang="scss" scoped>
.wallpaper-list {
	width: 100vw;
	height: 100vh;
	background: $self-background-color;
	display: flex;
	flex-direction: column;
	overflow: hidden;

	.type-tabs {
		display: flex;
		background: #fff;
		padding: 20rpx 24rpx;
		gap: 20rpx;

		.tab-item {
			flex: 1;
			text-align: center;
			padding: 18rpx 32rpx;
			font-size: 28rpx;
			color: #999;
			background: #fafafa;
			border-radius: 32rpx;
			transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
			position: relative;

			&.active {
				color: #7d5442;
				background: #f5ebe8;
				font-weight: 500;
				transform: scale(1.02);
			}
		}
	}

	.scroll-area {
		flex: 1;
		height: 0;
	}

	.content {
		padding: 20rpx 24rpx;
		padding-bottom: 60rpx;

		.count-bar {
			font-size: 24rpx;
			color: #999;
			padding: 10rpx 0 20rpx;
		}

		.grid {
			display: grid;
			grid-template-columns: repeat(2, 1fr);
			gap: 20rpx;

			.grid-item {
				position: relative;
				background: #fff;
				border-radius: 16rpx;
				overflow: hidden;
				box-shadow: 0 2rpx 12rpx rgba(125, 84, 66, 0.08);

				.thumb {
					width: 100%;
					height: 320rpx;
					display: block;
				}

				.item-info {
					padding: 14rpx 16rpx;
					display: flex;
					align-items: center;
					justify-content: space-between;

					.item-title {
						font-size: 24rpx;
						color: #534747;
						overflow: hidden;
						text-overflow: ellipsis;
						white-space: nowrap;
						flex: 1;
					}

					.item-meta {
						display: flex;
						align-items: center;
						gap: 4rpx;
						flex-shrink: 0;

						.meta-num {
							font-size: 22rpx;
							color: #ccc;
						}
					}
				}

				.remove-btn {
					position: absolute;
					top: 12rpx;
					right: 12rpx;
					width: 52rpx;
					height: 52rpx;
					background: rgba(255, 255, 255, 0.85);
					border-radius: 50%;
					display: flex;
					align-items: center;
					justify-content: center;
					backdrop-filter: blur(4px);
				}
			}
		}

		.load-footer {
			padding: 80rpx 0;
			display: flex;
			justify-content: center;

			.load-text {
				font-size: 28rpx;
				color: #bbb;
			}
		}
	}
}
</style>
