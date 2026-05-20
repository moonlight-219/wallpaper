<template>
	<view class="author">
		<myTitleBar></myTitleBar>
		<view class="author-box">
			<!-- 创作者分享区 -->
			<view class="author-share" @click="handleCreate">
				<view class="left">
					<view class="author-title">欢迎创作者分享您的壁纸</view>
					<view class="detail">点击查看详情</view>
				</view>
				<view class="right">
					<image class="author-logo" src="@/static/思考.png" mode="aspectFill"></image>
				</view>
			</view>

			<!-- 创作者 -->
			<view class="authors">
				<view v-if="pageLoading" class="skeleton-wrapper">
					<view v-for="i in 3" :key="'s' + i" class="author-skeleton">
						<view class="skeleton-top">
							<skeleton shape="circle" width="72rpx" height="72rpx" />
							<skeleton width="150rpx" height="28rpx" radius="14rpx" />
						</view>
						<view class="skeleton-list">
							<skeleton v-for="j in 3" :key="'i' + i + j" width="100%" height="400rpx" radius="20rpx" />
						</view>
					</view>
				</view>
				<authorItem v-else v-for="author in authors" :key="author.id" :author="author"></authorItem>
			</view>

			
			<!-- 返回顶部按钮 -->
			<view class="back-top-btn" v-if="showBackTop" @click="handleBackTop">
				<text>↑</text>
			</view>

			<!-- 底部导航栏 -->
			<myTabBar></myTabBar>
		</view>
	</view>
</template>

<script setup>
import myTabBar from '@/components/myTabBar.vue'
import authorItem from '@/components/authorItem.vue'
import myTitleBar from '@/components/myTitleBar.vue'
import skeleton from '@/components/skeleton.vue'
import { ref, onMounted } from 'vue'
import { onShow, onPullDownRefresh, onPageScroll } from '@dcloudio/uni-app'
import { getAuthorList } from '@/apis/author'
import { isLoggedIn, navigateToLogin } from '@/utils/auth.js'

onShow(() => {
	uni.hideTabBar()
})

const pageLoading = ref(true)
const authors = ref([])
const showBackTop = ref(false)

onPageScroll((e) => {
	showBackTop.value = e.scrollTop > 300
})

const loadAuthors = async () => {
	try {
		const res = await getAuthorList({
			page: 1,
			pageSize: 10
		})
		if (res.code === 200 && res.data) {
			authors.value = res.data.list || []
		}
	} catch (error) {
		console.error('加载创作者列表失败:', error)
	}
}

const handleBackTop = () => {
	uni.pageScrollTo({
		scrollTop: 0,
		duration: 300
	})
}

onMounted(async () => {
	await loadAuthors()
	pageLoading.value = false
})

onPullDownRefresh(async () => {
	pageLoading.value = true
	try {
		await loadAuthors()
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

const handleCreate = () => {
	if (!isLoggedIn()) {
		uni.showModal({
			title: '需要登录',
			content: '上传作品需要先登录，是否前往登录？',
			confirmText: '去登录',
			cancelText: '取消',
			confirmColor: '#7a544d',
			success: (res) => {
				if (res.confirm) navigateToLogin('/pagesAuthor/create')
			}
		})
		return
	}
	uni.navigateTo({
		url: '/pagesAuthor/create'
	})
}
</script>

<style lang="scss" scoped>
.author {
	width: 100%;
	min-height: 100vh;
	background: $self-background-color;
	padding-bottom: calc(env(safe-area-inset-bottom) + 150rpx);
	box-sizing: border-box;

	.author-box {
		padding: 30rpx;
		width: 100%;
		box-sizing: border-box;
	}

	.author-share {
		padding: 40rpx 30rpx;
		display: flex;
		justify-content: space-between;
		align-items: center;
		border-radius: 20rpx;
		background: rgba(255, 255, 255, 0.6);
		backdrop-filter: blur(10rpx);
		box-shadow: 0 4rpx 16rpx rgba(125, 84, 66, 0.08);
		transition: transform 0.2s ease;

		&:active {
			transform: scale(0.98);
		}

		.left {
			display: flex;
			flex-direction: column;
			gap: 12rpx;

			.author-title {
				color: #7d5442;
				font-size: 36rpx;
				font-weight: 700;
			}

			.detail {
				background-color: #a89187;
				color: #fff;
				padding: 8rpx 20rpx;
				border-radius: 30rpx;
				font-size: 24rpx;
				align-self: flex-start;
			}
		}

		.right {
			.author-logo {
				width: 160rpx;
				height: 160rpx;
				object-fit: cover;
			}
		}
	}

	.authors {
		width: 100%;

		.skeleton-wrapper {
			display: flex;
			flex-direction: column;
			gap: 20rpx;

			.author-skeleton {
				background: #fff;
				border-radius: 20rpx;
				margin: 20rpx 0;
				overflow: hidden;
				box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);

				.skeleton-top {
					display: flex;
					align-items: center;
					gap: 16rpx;
					padding: 20rpx;
				}

				.skeleton-list {
					display: flex;
					padding: 0 20rpx 20rpx;
					gap: 12rpx;

					> view {
						flex: 1;
					}
				}
			}
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
