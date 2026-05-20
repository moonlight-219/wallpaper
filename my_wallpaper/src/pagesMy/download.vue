<template>
	<view class="wallpaper-list">
		<myTitleBar title="下载记录">
			<template #default>
				<uni-icons type="left" size="20" @click="goBack()"></uni-icons>
			</template>
		</myTitleBar>
		<scroll-view class="scroll-area" scroll-y lower-threshold="120" :refresher-enabled="true"
			:refresher-triggered="refreshing" refresher-background="transparent" @scrolltolower="onScrollToLower"
			@refresherrefresh="onPullRefresh">
			<view class="content">
				<view class="count-bar">共 {{ total }} 条下载记录</view>
				<view class="list" v-if="list.length > 0">
					<view class="record-card" v-for="(item, index) in list" :key="item.id" @click="goPreview(item)">
						<image class="thumb" :src="item.thumbnailUrl || item.url" mode="aspectFill" lazy-load></image>
						<view class="info">
							<view class="title">{{ item.title }}</view>
							<view class="meta">
								<text class="category">{{ item.category || '壁纸' }}</text>
								<text class="sep">·</text>
								<text class="time">{{ item.downloadTime || item.createdAt }}</text>
							</view>
							<view class="size-row" v-if="item.size">
								<uni-icons type="cloud-download" size="12" color="#a8c8e8"></uni-icons>
								<text class="size-text">{{ item.size }}</text>
							</view>
						</view>
						<view class="remove-btn" @click.stop="removeRecord(index)">
							<uni-icons type="trash" size="16" color="#ccc"></uni-icons>
						</view>
					</view>
				</view>
				<view class="load-footer">
					<text v-if="loading" class="load-text">加载中...</text>
					<text v-else-if="!hasMore && list.length > 0" class="load-text">没有更多了</text>
					<text v-else-if="!loading && list.length === 0 && loaded" class="load-text">暂无下载记录</text>
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
import { getUserDownloads, deleteDownloadRecord } from '@/apis/user.js'
import { safeNavigateBack } from '@/utils/navigation.js'

const list = ref([])
const page = ref(1)
const pageSize = 20
const hasMore = ref(true)
const loading = ref(false)
const loaded = ref(false)
const refreshing = ref(false)
const total = ref(0)

const formatFileSize = (bytes) => {
	if (!bytes) return ''
	const k = 1024
	const sizes = ['B', 'KB', 'MB', 'GB']
	const i = Math.floor(Math.log(bytes) / Math.log(k))
	return (bytes / Math.pow(k, i)).toFixed(i > 0 ? 1 : 0) + sizes[i]
}

const fetchList = async (reset = false) => {
	if (loading.value) return
	if (!reset && !hasMore.value) return

	const userId = uni.getStorageSync('userId')
	if (!userId) return

	loading.value = true
	const currentPage = reset ? 1 : page.value + 1

	try {
		const res = await getUserDownloads(userId, {
			page: currentPage,
			pageSize
		})
		if (res.code === 200 && res.data) {
			const records = res.data.records || res.data.list || res.data || []
			const mapped = records.map((item) => ({
				id: item.id,
				wallpaperId: item.wallpaperId,
				title: item.title || '未命名',
				url: item.url || item.thumbnailUrl || '',
				thumbnailUrl: item.thumbnailUrl || item.url || '',
				category: item.category || '壁纸',
				type: item.type,
				downloadTime: item.downloadTime || '',
				size: item.fileSize ? formatFileSize(item.fileSize) : ''
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
		console.error('获取下载记录失败:', error)
		uni.showToast({ title: '加载失败', icon: 'none' })
	} finally {
		loading.value = false
	}
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

const removeRecord = async (index) => {
	const target = list.value[index]

	uni.showModal({
		title: '提示',
		content: '确定删除该下载记录？',
		confirmColor: '#7d5442',
		success: async (res) => {
			if (res.confirm) {
				uni.showLoading({ title: '删除中...' })

				try {
					const result = await deleteDownloadRecord(target.id)

					if (result.code === 200) {
						// 从列表中移除
						list.value.splice(index, 1)
						total.value = Math.max(0, total.value - 1)

						uni.showToast({
							title: '删除成功',
							icon: 'success',
							duration: 1500
						})
					} else {
						uni.showToast({
							title: result.message || '删除失败',
							icon: 'none',
							duration: 2000
						})
					}
				} catch (error) {
					console.error('删除失败:', error)
					uni.showToast({
						title: '删除失败，请重试',
						icon: 'none',
						duration: 2000
					})
				} finally {
					uni.hideLoading()
				}
			}
		}
	})
}

const goPreview = (item) => {
	if (!item.wallpaperId) {
		uni.showToast({ title: '壁纸信息不存在', icon: 'none' })
		return
	}
	uni.navigateTo({
		url: `/pages/preview/preview-detail?wallpaperId=${item.wallpaperId}&type=${item.type || 1}&from=download`
	})
}

const goBack = () => {
	safeNavigateBack()
}

onLoad(() => {
	if (!isLoggedIn()) {
		redirectToLoginReplace('/pagesMy/download')
		return
	}
	fetchList(true)
})

onShow(() => {
	if (!isLoggedIn()) {
		redirectToLoginReplace('/pagesMy/download')
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

		.list {
			display: flex;
			flex-direction: column;
			gap: 16rpx;

			.record-card {
				display: flex;
				align-items: center;
				gap: 20rpx;
				background: #fff;
				border-radius: 16rpx;
				padding: 20rpx;
				box-shadow: 0 2rpx 12rpx rgba(125, 84, 66, 0.08);
				position: relative;

				.thumb {
					width: 100rpx;
					height: 140rpx;
					border-radius: 10rpx;
					flex-shrink: 0;
				}

				.info {
					flex: 1;
					display: flex;
					flex-direction: column;
					gap: 10rpx;
					min-width: 0;

					.title {
						font-size: 28rpx;
						font-weight: 600;
						color: #534747;
						overflow: hidden;
						text-overflow: ellipsis;
						white-space: nowrap;
					}

					.meta {
						display: flex;
						align-items: center;
						gap: 8rpx;

						.category {
							font-size: 22rpx;
							color: #7d5442;
							background: #f9dad8;
							padding: 2rpx 14rpx;
							border-radius: 20rpx;
						}

						.sep {
							font-size: 22rpx;
							color: #ddd;
						}

						.time {
							font-size: 22rpx;
							color: #bbb;
						}
					}

					.size-row {
						display: flex;
						align-items: center;
						gap: 6rpx;

						.size-text {
							font-size: 22rpx;
							color: #a8c8e8;
						}
					}
				}

				.remove-btn {
					flex-shrink: 0;
					width: 52rpx;
					height: 52rpx;
					display: flex;
					align-items: center;
					justify-content: center;
					background: #f8f8f8;
					border-radius: 50%;
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
