<template>
	<view class="history">
		<myTitleBar title="上传记录">
			<template #default>
				<uni-icons type="left" size="20" @click="goBack()"></uni-icons>
			</template>
		</myTitleBar>
		<scroll-view
			class="scroll-area"
			scroll-y
			lower-threshold="120"
			:refresher-enabled="true"
			:refresher-triggered="refreshing"
			refresher-background="transparent"
			@scrolltolower="onScrollToLower"
			@refresherrefresh="onPullRefresh"
		>
			<view class="content">
				<view class="stat-bar">
					<view class="stat-item">
						<text class="stat-num">{{ countData.total }}</text>
						<text class="stat-label">总上传</text>
					</view>
					<view class="stat-divider"></view>
					<view class="stat-item">
						<text class="stat-num approved-num">{{ countData.approvedCount }}</text>
						<text class="stat-label">已通过</text>
					</view>
					<view class="stat-divider"></view>
					<view class="stat-item">
						<text class="stat-num pending-num">{{ countData.pendingCount }}</text>
						<text class="stat-label">审核中</text>
					</view>
					<view class="stat-divider"></view>
					<view class="stat-item">
						<text class="stat-num rejected-num">{{ countData.rejectedCount }}</text>
						<text class="stat-label">未通过</text>
					</view>
				</view>
				<view class="filter-bar">
					<view
						class="filter-item"
						:class="{ active: activeFilter === f.value }"
						v-for="f in filters"
						:key="f.value"
						@click="switchFilter(f.value)"
					>
						{{ f.label }}
					</view>
				</view>
				<view class="list" v-if="displayList.length > 0">
					<view
						class="record-card"
						v-for="(item, index) in displayList"
						:key="item.id"
					>
						<view class="thumb-wrap">
							<image class="thumb" :src="item.thumbnailUrl || item.url" mode="aspectFill" lazy-load @click="previewImage(item)"></image>
							<view class="category-badge">{{ item.category || '壁纸' }}</view>
						</view>
						<view class="info">
							<view class="info-top">
								<text class="title">{{ item.title }}</text>
								<view class="status-tag" :class="item.status">
									{{ statusLabel(item.status) }}
								</view>
							</view>
							<view class="meta">
								<text class="meta-text">{{ item.width || 0 }}×{{ item.height || 0 }}</text>
								<text class="meta-sep"></text>
								<text class="meta-text">{{ formatSize(item.size) }}</text>
							</view>
							<view class="time">{{ formatTime(item.uploadTime) }}</view>
							<view class="reject-reason" v-if="item.status === 'rejected' && item.reason">
								<uni-icons type="info" size="12" color="#ff4d4f"></uni-icons>
								<text class="reason-text">{{ item.reason }}</text>
							</view>
						</view>
						<view class="actions" v-if="item.status !== 'pending'">
							<view class="action-btn delete" @click="deleteRecord(index)">
								<uni-icons type="trash" size="18" color="#ff4d4f"></uni-icons>
							</view>
						</view>
					</view>
				</view>
				<view class="load-footer">
					<text v-if="loading" class="load-text">加载中...</text>
					<text v-else-if="!hasMore && displayList.length > 0" class="load-text">没有更多了</text>
					<text v-else-if="!loading && displayList.length === 0 && loaded" class="load-text">暂无上传记录</text>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import myTitleBar from '@/components/myTitleBar.vue'
import { isLoggedIn, redirectToLoginReplace } from '@/utils/auth.js'
import { getUserUploads, deleteWork } from '@/apis/user.js'
import { safeNavigateBack } from '@/utils/navigation.js'

const filters = ref([
	{ label: '全部', value: 'all' },
	{ label: '审核中', value: 'pending' },
	{ label: '已通过', value: 'approved' },
	{ label: '未通过', value: 'rejected' }
])
const activeFilter = ref('all')

const list = ref([])
const params = ref({
	page: 1,
	pageSize: 10
})
const hasMore = ref(true)
const loading = ref(false)
const loaded = ref(false)
const refreshing = ref(false)

const displayList = computed(() => {
	if (activeFilter.value === 'all') return list.value
	return list.value.filter((item) => item.status === activeFilter.value)
})

const countData = ref({
	total: 0,
	approvedCount: 0,
	pendingCount: 0,
	rejectedCount: 0
})

const fetchList = async (reset = false) => {
	if (loading.value) return
	if (!reset && !hasMore.value) return

	const userId = uni.getStorageSync('userId')
	if (!userId) return

	loading.value = true
	const currentPage = reset ? 1 : params.value.page + 1

	try {
		const res = await getUserUploads(userId, {
			...params.value,
			page: currentPage
		})
		if (res.code === 200 && res.data) {
			const wallpapers = res.data.wallpapers || res.data.list || res.data || []
			countData.value.total = res.data.totalCount || res.data.total || 0
			countData.value.approvedCount = res.data.approvedCount || 0
			countData.value.pendingCount = res.data.pendingCount || 0
			countData.value.rejectedCount = res.data.rejectedCount || 0

			const statusMap = { 0: 'pending', 1: 'approved', 2: 'rejected' }
			const mapped = wallpapers.map((item) => ({
				id: item.id,
				title: item.title || '未命名',
				url: item.url || item.thumbnailUrl || '',
				thumbnailUrl: item.thumbnailUrl || item.url || '',
				category: item.category || '壁纸',
				status: statusMap[item.status] || 'pending',
				width: item.imageWidth || 0,
				height: item.imageHeight || 0,
				size: item.fileSize || 0,
				uploadTime: item.uploadTime || '',
				reason: item.rejectReason || ''
			}))
			if (reset || currentPage === 1) {
				list.value = mapped
			} else {
				list.value.push(...mapped)
			}
			params.value.page = currentPage
			hasMore.value = mapped.length >= params.value.pageSize
			loaded.value = true
		}
	} catch (error) {
		console.error('获取上传记录失败:', error)
		uni.showToast({ title: '加载失败', icon: 'none' })
	} finally {
		loading.value = false
	}
}

const switchFilter = (value) => {
	activeFilter.value = value
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

const statusLabel = (status) => {
	const map = { approved: '已通过', pending: '审核中', rejected: '未通过' }
	return map[status] || status
}

const formatSize = (bytes) => {
	if (!bytes) return '0KB'
	if (bytes >= 1024 * 1024) return (bytes / 1024 / 1024).toFixed(1) + 'MB'
	return (bytes / 1024).toFixed(0) + 'KB'
}

const formatTime = (time) => {
	if (!time) return ''
	const date = new Date(time)
	if (isNaN(date.getTime())) return time
	const y = date.getFullYear()
	const m = String(date.getMonth() + 1).padStart(2, '0')
	const d = String(date.getDate()).padStart(2, '0')
	const h = String(date.getHours()).padStart(2, '0')
	const min = String(date.getMinutes()).padStart(2, '0')
	return `${y}-${m}-${d} ${h}:${min}`
}

const previewImage = (item) => {
	uni.previewImage({
		current: item.url,
		urls: [item.url]
	})
}

const deleteRecord = async (index) => {
	const target = displayList.value[index]
	
	uni.showModal({
		title: '提示',
		content: '确定要删除该作品集吗？删除后将无法恢复。',
		confirmColor: '#ff4d4f',
		success: async (res) => {
			if (res.confirm) {
				uni.showLoading({ title: '删除中...' })
				
				try {
					const result = await deleteWork(target.id)
					
					if (result.code === 200) {
						// 从列表中移除
						const realIndex = list.value.findIndex((r) => r.id === target.id)
						if (realIndex !== -1) {
							list.value.splice(realIndex, 1)
						}
						
						// 更新统计数据
						countData.value.total = Math.max(0, countData.value.total - 1)
						if (target.status === 'approved') {
							countData.value.approvedCount = Math.max(0, countData.value.approvedCount - 1)
						} else if (target.status === 'pending') {
							countData.value.pendingCount = Math.max(0, countData.value.pendingCount - 1)
						} else if (target.status === 'rejected') {
							countData.value.rejectedCount = Math.max(0, countData.value.rejectedCount - 1)
						}
						
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

const goBack = () => {
	safeNavigateBack()
}

onLoad(() => {
	if (!isLoggedIn()) {
		redirectToLoginReplace('/pagesAuthor/history')
		return
	}
	fetchList(true)
})

onShow(() => {
	if (!isLoggedIn()) {
		redirectToLoginReplace('/pagesAuthor/history')
	}
})
</script>

<style lang="scss" scoped>
.history {
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
		padding: 30rpx;
		padding-bottom: 60rpx;

		.stat-bar {
			display: flex;
			align-items: center;
			background: #fff;
			border-radius: 20rpx;
			padding: 30rpx 0;
			margin-bottom: 24rpx;
			box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

			.stat-item {
				flex: 1;
				display: flex;
				flex-direction: column;
				align-items: center;
				gap: 8rpx;

				.stat-num {
					font-size: 40rpx;
					font-weight: 700;
					color: #333;

					&.approved-num { color: #52c41a; }
					&.pending-num  { color: #fa8c16; }
					&.rejected-num { color: #ff4d4f; }
				}

				.stat-label {
					font-size: 22rpx;
					color: #999;
				}
			}

			.stat-divider {
				width: 2rpx;
				height: 60rpx;
				background: #f0f0f0;
			}
		}

		.filter-bar {
			display: flex;
			gap: 16rpx;
			margin-bottom: 24rpx;
			flex-wrap: wrap;

			.filter-item {
				padding: 12rpx 30rpx;
				border-radius: 40rpx;
				font-size: 24rpx;
				color: #666;
				background: #fff;
				border: 2rpx solid #eee;

				&.active {
					background: linear-gradient(135deg, #a89187, #d4c4b8);
					color: #fff;
					border-color: transparent;
				}
			}
		}

		.list {
			display: flex;
			flex-direction: column;
			gap: 20rpx;

			.record-card {
				display: flex;
				align-items: flex-start;
				gap: 20rpx;
				background: #fff;
				border-radius: 20rpx;
				padding: 24rpx;
				box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

				.thumb-wrap {
					position: relative;
					flex-shrink: 0;
					width: 160rpx;
					height: 200rpx;
					border-radius: 12rpx;
					overflow: hidden;

					.thumb {
						width: 100%;
						height: 100%;
					}

					.category-badge {
						position: absolute;
						bottom: 0;
						left: 0;
						right: 0;
						background: rgba(0, 0, 0, 0.55);
						color: #fff;
						font-size: 18rpx;
						text-align: center;
						padding: 6rpx 0;
					}
				}

				.info {
					flex: 1;
					display: flex;
					flex-direction: column;
					gap: 12rpx;
					min-width: 0;

					.info-top {
						display: flex;
						align-items: center;
						justify-content: space-between;
						gap: 10rpx;

						.title {
							font-size: 28rpx;
							font-weight: 600;
							color: #333;
							flex: 1;
							overflow: hidden;
							text-overflow: ellipsis;
							white-space: nowrap;
						}

						.status-tag {
							flex-shrink: 0;
							font-size: 20rpx;
							padding: 6rpx 16rpx;
							border-radius: 20rpx;

							&.approved {
								background: #f6ffed;
								color: #52c41a;
								border: 2rpx solid #b7eb8f;
							}
							&.pending {
								background: #fff7e6;
								color: #fa8c16;
								border: 2rpx solid #ffd591;
							}
							&.rejected {
								background: #fff1f0;
								color: #ff4d4f;
								border: 2rpx solid #ffa39e;
							}
						}
					}

					.meta {
						display: flex;
						align-items: center;
						gap: 8rpx;

						.meta-text { font-size: 22rpx; color: #999; }
						.meta-sep  { font-size: 22rpx; color: #ccc; }
					}

					.time {
						font-size: 22rpx;
						color: #bbb;
					}

					.reject-reason {
						display: flex;
						align-items: flex-start;
						gap: 8rpx;
						background: #fff1f0;
						border-radius: 8rpx;
						padding: 10rpx 16rpx;

						.reason-text {
							font-size: 22rpx;
							color: #ff4d4f;
							flex: 1;
							line-height: 1.5;
						}
					}
				}

				.actions {
					flex-shrink: 0;
					display: flex;
					flex-direction: column;
					gap: 16rpx;

					.action-btn {
						width: 60rpx;
						height: 60rpx;
						border-radius: 50%;
						display: flex;
						align-items: center;
						justify-content: center;
						background: #fff5f5;
					}
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

