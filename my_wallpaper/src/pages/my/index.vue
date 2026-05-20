<template>
	<view class="my">
		<myTitleBar></myTitleBar>

		<view class="my-box">
			<!-- 骨架屏 -->
			<view v-if="pageLoading" class="skeleton-wrapper">
				<view class="profile-skeleton">
					<view class="profile-header-skeleton">
						<skeleton shape="circle" width="120rpx" height="120rpx" />
						<view class="info-skeleton">
							<skeleton width="200rpx" height="36rpx" radius="18rpx" />
							<skeleton width="150rpx" height="24rpx" radius="12rpx" />
						</view>
					</view>
					<view class="stat-skeleton">
						<view class="stat-item-skeleton" v-for="i in 3" :key="i">
							<skeleton width="80rpx" height="40rpx" radius="20rpx" />
							<skeleton width="100rpx" height="24rpx" radius="12rpx" />
						</view>
					</view>
				</view>
				<view class="menu-skeleton">
					<view class="menu-item-skeleton" v-for="i in 6" :key="i">
						<skeleton shape="circle" width="80rpx" height="80rpx" />
						<skeleton width="100rpx" height="26rpx" radius="13rpx" />
					</view>
				</view>
			</view>

			<!-- 实际内容 -->
			<template v-else>
				<!-- 用户信息卡片 -->
				<view class="profile-card">
					<view class="profile-header">
						<view class="avatar-wrap" @click="handleAvatarClick">
							<image class="avatar-img" :src="userInfo.avatar" mode="aspectFill" lazy-load></image>
							<view class="edit-icon" v-if="isLoggedIn">
								<uni-icons type="camera-filled" size="14" color="#fff"></uni-icons>
							</view>
						</view>
						<view class="info" @click="handleUserInfoClick">
							<view class="username" :class="{ 'clickable': !isLoggedIn }">{{ userInfo.nickname }}</view>
							<view class="userid" :class="{ 'clickable': !isLoggedIn }">ID: {{ userInfo.id }}</view>
						</view>
					</view>

					<!-- 我的作品 / 喜欢 / 收藏 三栏 -->
					<view class="stat-row">
						<view class="stat-item" v-for="item in statItems" :key="item.key" @click="handleStat(item)">
							<view class="stat-num">{{ item.num }}</view>
							<view class="stat-name">{{ item.name }}</view>
						</view>
					</view>
				</view>

				<!-- 功能列表 -->
				<view class="menu-section">
					<myTitle>
						<template #left>
							<view class="left">我的服务</view>
						</template>
					</myTitle>

					<view class="menu-grid">
						<view class="menu-item" v-for="item in menuItems" :key="item.key" @click="handleMenu(item)">
							<view class="icon-wrap" :style="{ backgroundColor: item.bgColor }">
								<uni-icons :type="item.icon" size="22" color="#fff"></uni-icons>
							</view>
							<view class="menu-label">
								<text v-if="item.key !== 'contact'">{{ item.label }}</text>
							</view>
							<view v-if="item.badge" class="badge">{{ item.badge }}</view>
						</view>
						<!-- #ifdef MP -->
						<view class="menu-item" @click="handleMenu({ key: 'contact', label: '联系客服' })">
							<button open-type="contact" class="menu-item-overlay"></button>
							<view class="icon-wrap" :style="{ backgroundColor: '#d18b8b' }">
								<uni-icons type="auth-filled" size="22" color="#fff"></uni-icons>
							</view>
							<view class="menu-label">
								<text>联系客服</text>
							</view>
						</view>
						<!-- #endif -->
					</view>
				</view>

				<!-- 退出登录 -->
				<view v-if="isLoggedIn" class="logout-btn" @click="handleLogout()">退出登录</view>
			</template>
		</view>

		<myTabBar></myTabBar>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import myTabBar from '@/components/myTabBar.vue'
import myTitle from '@/components/myTitle.vue'
import myTitleBar from '@/components/myTitleBar.vue'
import skeleton from '@/components/skeleton.vue'
import { getUserStats } from '@/apis/user.js'
import { useUserStore } from '@/stores/user.js'
import { navigateToLogin } from '@/utils/auth.js'

onShow(async () => {
	uni.hideTabBar()
	await loadUserStats()
})

const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isLoggedIn.value)

const pageLoading = ref(true)

const userInfo = ref({
	id: "xxxxxx",
	username: '未登录',
	nickname: '未登录',
	avatar: '/static/images/1.jpg',
	bio: ''
})

const defaultUserInfo = {
	id: "xxxxxx",
	username: '未登录',
	nickname: '未登录',
	avatar: '/static/images/1.jpg',
	bio: ''
}

const defaultStatItems = [
	{ key: 'works', name: '我的作品', num: 0 },
	{ key: 'likes', name: '我的喜欢', num: 0 },
	{ key: 'collect', name: '我的收藏', num: 0 },
]

const statItems = ref([
	{ key: 'works', name: '我的作品', num: 0 },
	{ key: 'likes', name: '我的喜欢', num: 0 },
	{ key: 'collect', name: '我的收藏', num: 0 },
])

const menuItems = ref([
	{ key: 'download', label: '下载记录', icon: 'cloud-download-filled', bgColor: '#e2b9b3', badge: '' },
	{ key: 'upload', label: '上传记录', icon: 'upload-filled', bgColor: '#839973', badge: '' },
	// { key: 'contact', label: '联系客服', icon: 'auth-filled', bgColor: '#d18b8b', badge: '' },
	{ key: 'feedback', label: '意见反馈', icon: 'chat-filled', bgColor: '#e8dbcb', badge: '' },
	{ key: 'faq', label: '常见问题', icon: 'help-filled', bgColor: '#613942', badge: '' },
])

const resetToDefault = () => {
	userInfo.value = { ...defaultUserInfo }
	statItems.value = defaultStatItems.map(item => ({ ...item }))
}

const loadUserStats = async () => {
	if (!userStore.isLoggedIn.value) {
		resetToDefault()
		return
	}

	try {
		const userId = uni.getStorageSync('userId')
		if (!userId) {
			resetToDefault()
			return
		}

		console.log('开始加载用户信息，userId:', userId)
		const res = await getUserStats(userId)
		console.log('用户信息API响应:', res)

		if (res.code === 200 && res.data) {
			userInfo.value = {
				id: res.data.id,
				username: res.data.username || '用户',
				nickname: res.data.nickname || res.data.username || '用户',
				avatar: res.data.avatar || '/static/images/1.jpg',
				bio: res.data.bio || ''
			}

			statItems.value = [
				{ key: 'works', name: '我的作品', num: res.data.workCount || 0 },
				{ key: 'likes', name: '我的喜欢', num: res.data.myLikeCount || 0 },
				{ key: 'collect', name: '我的收藏', num: res.data.myCollectCount || 0 },
			]

			console.log('用户信息更新成功:', userInfo.value)
			console.log('统计数据更新成功:', statItems.value)
		}
	} catch (error) {
		console.error('加载用户信息失败:', error)
		resetToDefault()
	}
}

onMounted(async () => {
	await loadUserStats()
	pageLoading.value = false
})

onPullDownRefresh(async () => {
	pageLoading.value = true
	try {
		await loadUserStats()
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

const handleStat = (item) => {
	if (!userStore.isLoggedIn.value) {
		const redirectMap = {
			works: '/pagesAuthor/author-detail',
			likes: '/pagesMy/likes',
			collect: '/pagesMy/collect'
		}
		const path = redirectMap[item.key]
		if (!path) return
		uni.showModal({
			title: '需要登录',
			content: '请先登录后查看',
			confirmText: '去登录',
			cancelText: '取消',
			confirmColor: '#7a544d',
			success: (res) => {
				if (res.confirm) navigateToLogin(path)
			}
		})
		return
	}
	if (item.key === 'works') {
		const uid = uni.getStorageSync('userId')
		uni.navigateTo({ url: `/pagesAuthor/author-detail?id=${uid}` })
	} else if (item.key === 'likes') {
		uni.navigateTo({ url: '/pagesMy/likes' })
	} else if (item.key === 'collect') {
		uni.navigateTo({ url: '/pagesMy/collect' })
	}
}

const handleMenu = (item) => {
	// #ifdef MP
	if (item.key === 'contact') {
		uni.makePhoneCall({ phoneNumber: '15364277858' })
		return
	}
	// #endif
	if (item.key === 'download') {
		if (!userStore.isLoggedIn.value) {
			uni.showModal({
				title: '需要登录',
				content: '下载记录需登录后查看，是否前往登录？',
				confirmText: '去登录',
				cancelText: '取消',
				confirmColor: '#7a544d',
				success: (res) => {
					if (res.confirm) navigateToLogin('/pagesMy/download')
				}
			})
			return
		}
		uni.navigateTo({ url: '/pagesMy/download' })
		return
	}
	if (item.key === 'upload') {
		if (!userStore.isLoggedIn.value) {
			uni.showModal({
				title: '需要登录',
				content: '上传记录需登录后查看，是否前往登录？',
				confirmText: '去登录',
				cancelText: '取消',
				confirmColor: '#7a544d',
				success: (res) => {
					if (res.confirm) navigateToLogin('/pagesAuthor/history')
				}
			})
			return
		}
		uni.navigateTo({ url: '/pagesAuthor/history' })
		return
	}
	if (item.key === 'feedback') {
		uni.navigateTo({ url: '/pagesMy/feedback' })
		return
	}
	uni.showToast({ title: '功能开发中', icon: 'none' })
}

const handleAvatarClick = () => {
	if (!userStore.isLoggedIn.value) {
		navigateToLogin('/pages/my/index')
		return
	}
	// 已登录，跳转到编辑资料页面
	uni.navigateTo({ url: '/pagesMy/profile-edit' })
}

const handleUserInfoClick = () => {
	if (!userStore.isLoggedIn.value) {
		navigateToLogin('/pages/my/index')
		return
	}
	// 已登录，跳转到编辑资料页面
	uni.navigateTo({ url: '/pagesMy/profile-edit' })
}

const handleLogout = () => {
	uni.showModal({
		title: '提示',
		content: '确定要退出登录吗？',
		confirmColor: '#7a544d',
		success: (res) => {
			if (res.confirm) {
				userStore.clearAuth()
				resetToDefault()
				uni.showToast({ title: '已退出登录', icon: 'none' })
				setTimeout(() => {
					loadUserStats()
				}, 100)
			}
		}
	})
}
</script>

<style lang="scss" scoped>
.my {
	min-height: 100vh;
	width: 100vw;
	background: $self-background-color;
	padding-bottom: calc(env(safe-area-inset-bottom) + 150rpx);
	overflow-y: auto;

	.my-box {
		padding: 0 30rpx;
	}

	.skeleton-wrapper {
		.profile-skeleton {
			background: #fff;
			border-radius: 20rpx;
			padding: 40rpx 30rpx 30rpx;
			margin-bottom: 30rpx;
			margin-top: 15rpx;

			.profile-header-skeleton {
				display: flex;
				align-items: center;
				gap: 24rpx;
				padding-bottom: 30rpx;

				.info-skeleton {
					display: flex;
					flex-direction: column;
					gap: 12rpx;
				}
			}

			.stat-skeleton {
				display: grid;
				grid-template-columns: repeat(3, 1fr);
				padding-top: 30rpx;

				.stat-item-skeleton {
					display: flex;
					flex-direction: column;
					align-items: center;
					gap: 8rpx;
				}
			}
		}

		.menu-skeleton {
			display: grid;
			grid-template-columns: repeat(3, 1fr);
			gap: 20rpx;

			.menu-item-skeleton {
				background: #fff;
				border-radius: 20rpx;
				padding: 30rpx 20rpx;
				display: flex;
				flex-direction: column;
				align-items: center;
				gap: 16rpx;
			}
		}
	}

	.profile-card {
		background: #fff;
		border-radius: 20rpx;
		padding: 40rpx 30rpx 30rpx;
		margin-bottom: 30rpx;
		margin-top: 15rpx;
		box-shadow: 0 4rpx 20rpx rgba(122, 84, 77, 0.08);

		.profile-header {
			display: flex;
			align-items: center;
			gap: 24rpx;
			padding-bottom: 30rpx;
			border-bottom: 1rpx solid rgba(122, 84, 77, 0.08);

			.avatar-wrap {
				position: relative;
				width: 120rpx;
				height: 120rpx;
				flex-shrink: 0;

				.avatar-img {
					width: 100%;
					height: 100%;
					border-radius: 50%;
					border: 3rpx solid #e8dbcb;
				}

				.edit-icon {
					position: absolute;
					right: 0;
					bottom: 0;
					width: 40rpx;
					height: 40rpx;
					background-color: #7a544d;
					border-radius: 50%;
					display: flex;
					align-items: center;
					justify-content: center;
					border: 2rpx solid #fff;
				}
			}

			.info {
				flex: 1;

				.username {
					font-size: 36rpx;
					font-weight: 600;
					color: #613942;
					margin-bottom: 8rpx;
				}

				.userid {
					font-size: 24rpx;
					color: #999;
				}

				.clickable {
					opacity: 0.7;
					transition: opacity 0.3s;

					&:active {
						opacity: 0.5;
					}
				}
			}
		}

		.stat-row {
			display: grid;
			grid-template-columns: repeat(3, 1fr);
			padding-top: 30rpx;

			.stat-item {
				display: flex;
				flex-direction: column;
				align-items: center;
				gap: 8rpx;
				border-right: 1rpx solid rgba(122, 84, 77, 0.08);

				&:last-child {
					border-right: none;
				}

				.stat-num {
					font-size: 40rpx;
					font-weight: 600;
					color: #7a544d;
				}

				.stat-name {
					font-size: 24rpx;
					color: #999;
				}
			}
		}
	}

	.menu-section {
		margin-bottom: 30rpx;

		.menu-grid {
			display: grid;
			grid-template-columns: repeat(3, 1fr);
			gap: 20rpx;
			margin-top: 10rpx;

			.menu-item {
				background: #fff;
				border-radius: 20rpx;
				padding: 30rpx 20rpx;
				display: flex;
				flex-direction: column;
				align-items: center;
				gap: 16rpx;
				position: relative;
				box-shadow: 0 4rpx 20rpx rgba(122, 84, 77, 0.06);

				.menu-item-overlay {
					position: absolute;
					top: 0;
					left: 0;
					width: 100%;
					height: 100%;
					opacity: 0;
					background: transparent;
					border: none;
					padding: 0;
					margin: 0;
					z-index: 10;
				}

				.icon-wrap {
					width: 80rpx;
					height: 80rpx;
					border-radius: 50%;
					display: flex;
					align-items: center;
					justify-content: center;
					flex-shrink: 0;
				}

				.menu-label {
					font-size: 26rpx;
					color: #613942;
					text-align: center;
				}

				.badge {
					position: absolute;
					top: 20rpx;
					right: 20rpx;
					background-color: #e2b9b3;
					color: #613942;
					font-size: 20rpx;
					font-weight: 600;
					padding: 4rpx 12rpx;
					border-radius: 20rpx;
					min-width: 32rpx;
					text-align: center;
				}
			}
		}
	}

	.logout-btn {
		background-color: #fff;
		border-radius: 20rpx;
		height: 88rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 30rpx;
		color: #d18b8b;
		box-shadow: 0 4rpx 20rpx rgba(122, 84, 77, 0.06);
	}
}
</style>
