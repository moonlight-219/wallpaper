<template>
	<view class="preview-detail">
		<!-- 轮播图（支持多张壁纸） -->
		<swiper :current="currentIndex" @change="onSwiperChange" circular :interval="3000" :duration="500">
			<swiper-item v-for="item in wallpaperList" :key="item.id">
				<image :style="{ height: computedHeight + '%' }" @click="changeMask" class="preview-img" :src="item.url"
					mode="aspectFill" lazy-load @load="onImageLoad" @error="onImageError">
				</image>
			</swiper-item>
		</swiper>

		<!-- 加载状态 -->
		<view class="loading-container" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>

		<!-- 错误状态 -->
		<view class="error-container" v-else-if="error">
			<text class="error-text">{{ error }}</text>
			<button class="retry-btn" @click="retryLoad()">重新加载</button>
		</view>

		<!-- 遮罩层 -->
		<view class="mask" v-if="maskChange && wallpaperDetail && !loading && !error">
			<!-- 返回键：独占一行，仅适配状态栏 -->
			<view class="goBack" :style="{ top: getStatusBarHeight() + 'px' }" @click="goBack()">
				<uni-icons type="left" size="28" color="#534747"></uni-icons>
			</view>
			<!-- 第二行：序号 + 作者信息 -->
			<view class="top-bar" :style="{ top: (getStatusBarHeight() + 56) + 'px' }">
				<view class="count">{{ currentIndex + 1 }} / {{ wallpaperList.length }}</view>
				<view class="author-info" @click="goAuthorDetail()">
					<view class="author-avatar">
						<image class="avatar" :src="wallpaperDetail.author?.avatarUrl || '/static/images/1.jpg'" mode="aspectFill"
							lazy-load>
						</image>
					</view>
					<view class="author-right">
						<view class="author-name">{{ wallpaperDetail.author?.name || '未知作者' }}</view>
						<view class="author-sub">查看主页</view>
					</view>
				</view>
			</view>

			<view class="time" v-if="wallpaperDetail?.type === 1">{{ currentTime }}</view>
			<view class="date" v-if="wallpaperDetail?.type === 1">{{ currentDate }}</view>

			<!-- 平板预览时的时间日期（在图片内部） -->
			<view class="tablet-time-wrapper" v-if="wallpaperDetail?.type === 2">
				<view class="tablet-time">{{ currentTime }}</view>
				<view class="tablet-date">{{ currentDate }}</view>
			</view>

			<view class="footer">
				<view class="box" @click="openPopup()">
					<uni-icons type="info"></uni-icons>
					<view class="text">详情</view>
				</view>
				<view class="box" @click="handleCollect()">
					<uni-icons type="star" v-if="!wallpaperDetail.isCollected"></uni-icons>
					<uni-icons class="star" type="star-filled" v-else color="#f4a460"></uni-icons>
					<view class="text">{{ wallpaperDetail.collectCount || 0 }}</view>
				</view>
				<view class="box" @click="handleLike()">
					<uni-icons type="heart" v-if="!wallpaperDetail.isLiked"></uni-icons>
					<uni-icons type="heart-filled" v-else color="#e88b8b"></uni-icons>
					<view class="text">{{ wallpaperDetail.likeCount || 0 }}</view>
				</view>
				<view class="box" @click="handleDownload()">
					<uni-icons type="download"></uni-icons>
					<view class="text">{{ wallpaperDetail.downloadCount || 0 }}</view>
				</view>
				<view class="box" @click="openReportPopup()">
					<uni-icons type="chat" color="#534747" size="40"></uni-icons>
					<view class="text">举报</view>
				</view>
			</view>
		</view>

		<!-- 举报弹窗 -->
		<uni-popup safe-area ref="reportPopupRef" type="bottom" @change="onReportPopupChange">
			<view class="reportPopup">
				<view class="popupTitle">
					<view class=""></view>
					<view class="title">举报壁纸</view>
					<view class="close" @click="closeReportPopup()">
						<uni-icons type="closeempty" color="#534747"></uni-icons>
					</view>
				</view>
				<view class="report-body">
					<view class="report-label">请填写举报原因</view>
					<textarea class="report-textarea" v-model="reportReason" placeholder="请详细描述举报原因，以便我们快速处理..."
						placeholder-class="report-placeholder" maxlength="200" auto-height></textarea>
					<view class="report-count">{{ reportReason.length }} / 200</view>
					<button class="report-btn" :disabled="!reportReason.trim()" @click="submitReport()">提交举报</button>
				</view>
			</view>
		</uni-popup>

		<!-- 详情弹窗 -->
		<uni-popup safe-area ref="popupRef" type="bottom">
			<view class="prpupInfo">
				<view class="popupTitle">
					<view class=""></view>
					<view class="title">壁纸信息</view>
					<view class="close" @click="closePopup()">
						<uni-icons type="closeempty" color="#534747"></uni-icons>
					</view>
				</view>

				<view class="detail-info">
					<view class="detail-item">
						<view class="left">壁纸ID：</view>
						<view class="info">{{ wallpaperDetail?.id || '-' }}</view>
					</view>
					<view class="detail-item">
						<view class="left">分类：</view>
						<view class="info" v-if="wallpaperDetail?.category">
							{{ wallpaperDetail?.category || '-' }}
						</view>
						<view class="info" v-else>暂无分类信息</view>
					</view>
					<view class="detail-item">
						<view class="left">发布者：</view>
						<view class="info">{{ wallpaperDetail?.author?.name || '-' }}</view>
					</view>
					<view class="detail-item">
						<view class="left">尺寸：</view>
						<view class="info" v-if="wallpaperDetail?.imageWidth && wallpaperDetail?.imageHeight">
							{{ wallpaperDetail?.imageWidth }} × {{ wallpaperDetail?.imageHeight }}
						</view>
						<view class="info" v-else>暂无尺寸信息</view>
					</view>
					<view class="detail-item">
						<view class="left">大小：</view>
						<view class="info">{{ formatFileSize(wallpaperDetail?.fileSize) }}</view>
					</view>
					<view class="detail-item" v-if="wallpaperDetail?.description">
						<view class="left">描述：</view>
						<view class="info">{{ wallpaperDetail.description }}</view>
					</view>

					<view class="description">
						声明：本图片来自用户投稿，非商业使用，用于免费学习交流，如侵犯了您的权益，您可以拷贝壁纸 ID到邮箱 3538345589@qq.com，管理将删除侵权壁纸，维护您的权益。
					</view>
				</view>
			</view>
		</uni-popup>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getWallpaperDetail, getWorkWallpapers, likeWallpaper, collectWallpaper, reportWallpaper, downloadWallpaper } from '@/apis/wallpaper.js'
import { getStatusBarHeight } from '@/utils/system.js'
import { isLoggedIn, ensureLoggedInForAction } from '@/utils/auth.js'
import { getPreviewList } from '@/store/previewStore.js'
import { safeNavigateBack } from '@/utils/navigation.js'

// 壁纸ID或作品ID
const wallpaperId = ref(null)
const workId = ref(null)

// 壁纸列表（作品集可能有多个壁纸）
const wallpaperList = ref([])

// 当前显示的壁纸索引
const currentIndex = ref(0)

// 当前显示的壁纸详情
const wallpaperDetail = computed(() => {
	return wallpaperList.value[currentIndex.value] || null
})

// 加载状态
const loading = ref(true)

// 错误信息
const error = ref('')

// 遮罩层的改变
const maskChange = ref(true)

// 计算的高度
const computedHeight = ref(100)

// 不同设备对应不同的尺寸（手机和头像保持固定）
const types = {
	1: 100,
	3: 50
}

// 平板壁纸：根据实际宽高比动态计算高度（%）
// 横屏平板 -> 高度较小，竖屏平板 -> 高度较大
const calcTabletHeight = (item) => {
	if (!item || !item.imageWidth || !item.imageHeight) return 65
	return item.imageWidth > item.imageHeight ? 40 : 70
}

// 统一的高度计算入口
const calcPreviewHeight = (item, fallbackType) => {
	const t = item?.type ?? fallbackType
	if (t === 2) return calcTabletHeight(item)
	return types[t] || 100
}

// 动态时间日期
const _now = new Date()
const currentTime = ref(`${_now.getHours().toString().padStart(2, '0')}:${_now.getMinutes().toString().padStart(2, '0')}`)
const _weeks = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
const currentDate = ref(`${_weeks[_now.getDay()]} ${_now.getMonth() + 1}月${_now.getDate()}日`)

// 底部弹窗
const popupRef = ref(null)

// 举报弹窗
const reportPopupRef = ref(null)

// 举报原因
const reportReason = ref('')

// 下载计数
const downloadCount = ref(0)

// 路由入参中的 type（字符串），用于登录后回跳
const routeTypeParam = ref('')
const fromSource = ref('')

const previewLoginRedirect = computed(() => {
	if (workId.value) {
		return `/pages/preview/preview-detail?workId=${workId.value}`
	}
	if (wallpaperId.value) {
		const t = wallpaperDetail.value?.type ?? routeTypeParam.value ?? ''
		const q = t !== '' && t != null ? `?wallpaperId=${wallpaperId.value}&type=${t}` : `?wallpaperId=${wallpaperId.value}`
		return `/pages/preview/preview-detail${q}`
	}
	return '/pages/preview/preview-detail'
})

// 页面加载时获取参数
onMounted(() => {
	const pages = getCurrentPages()
	const currentPage = pages[pages.length - 1]
	const options = currentPage.$page?.options || currentPage.options || {}

	console.log('页面参数:', options)

	if (options.workId) {
		// 作品集模式：通过API查询，不使用本地数据
		workId.value = options.workId
		loadWorkWallpapers()
	} else if (options.wallpaperId) {
		// 单个壁纸模式
		wallpaperId.value = options.wallpaperId
		fromSource.value = options.from || ''
		// 如果传递了type参数，提前设置显示高度
		if (options.type) {
			routeTypeParam.value = options.type
			computedHeight.value = calcPreviewHeight(null, options.type)
		}
		// 尝试从全局变量获取预览列表数据
		const previewData = getPreviewList()
		if (fromSource.value === 'likes' || fromSource.value === 'collect' || fromSource.value === 'download') {
			loadWallpaperDetail()
		} else if (previewData.hasData && previewData.list.length > 0) {
			wallpaperList.value = previewData.list
			const idx = previewData.list.findIndex(item => String(item.id) === String(wallpaperId.value))
			currentIndex.value = idx >= 0 ? idx : 0
			const currentItem = wallpaperList.value[currentIndex.value]
			if (currentItem && currentItem.type) {
				computedHeight.value = calcPreviewHeight(currentItem)
			}
			loading.value = false
			console.log('使用本地预览列表数据，共', previewData.list.length, '张，当前索引:', currentIndex.value)
		} else {
			loadWallpaperDetail()
		}
	} else {
		error.value = '缺少必要参数'
		loading.value = false
	}
})

// 加载作品集的壁纸列表
const loadWorkWallpapers = async () => {
	if (!workId.value) {
		error.value = '作品ID不存在'
		loading.value = false
		return
	}

	try {
		loading.value = true
		error.value = ''

		const userInfo = uni.getStorageSync('userInfo')
		const userId = userInfo?.id || null

		const res = await getWorkWallpapers(workId.value, userId)
		console.log('作品壁纸列表API响应:', res)

		if (res.code === 200 && res.data && res.data.length > 0) {
			wallpaperList.value = res.data
			currentIndex.value = 0

			if (res.data[0].type) {
				computedHeight.value = calcPreviewHeight(res.data[0])
			}
		} else if (res.code === 401) {
			error.value = '该作品需要登录后查看'
		} else {
			error.value = res.message || '该作品集暂无壁纸'
		}
	} catch (err) {
		console.error('加载作品壁纸列表失败:', err)
		if (err.statusCode === 401) {
			error.value = '该作品需要登录后查看'
		} else {
			error.value = '网络错误，请稍后重试'
		}
	} finally {
		loading.value = false
	}
}

const retryLoad = () => {
	if (workId.value) {
		loadWorkWallpapers()
	} else if (wallpaperId.value) {
		loadWallpaperDetail()
	}
}

// 加载单个壁纸详情
const loadWallpaperDetail = async () => {
	if (!wallpaperId.value) {
		error.value = '壁纸ID不存在'
		loading.value = false
		return
	}

	try {
		loading.value = true
		error.value = ''

		// 获取当前用户ID（如果已登录）
		const userInfo = uni.getStorageSync('userInfo')
		const userId = userInfo?.id || null

		const res = await getWallpaperDetail(wallpaperId.value, userId)
		console.log('壁纸详情API响应:', res)

		if (res.code === 200 && res.data) {
			const detail = { ...res.data }
			if (fromSource.value === 'likes') {
				detail.isLiked = true
			} else if (fromSource.value === 'collect') {
				detail.isCollected = true
			}
			wallpaperList.value = [detail]
			currentIndex.value = 0
			downloadCount.value = res.data.downloadCount || 0

			// 根据壁纸类型设置显示高度
			if (res.data.type) {
				computedHeight.value = calcPreviewHeight(res.data)
			}
		} else {
			error.value = res.message || '获取壁纸详情失败'
		}
	} catch (err) {
		console.error('加载壁纸详情失败:', err)
		error.value = '网络错误，请稍后重试'
	} finally {
		loading.value = false
	}
}

// 格式化文件大小
const formatFileSize = (bytes) => {
	if (!bytes) return '0 B'
	const k = 1024
	const sizes = ['B', 'KB', 'MB', 'GB']
	const i = Math.floor(Math.log(bytes) / Math.log(k))
	return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

// 图片加载完成
const onImageLoad = () => {
	console.log('壁纸图片加载完成')
}

// 图片加载失败
const onImageError = () => {
	uni.showToast({
		title: '图片加载失败',
		icon: 'none'
	})
}

// 返回上一页
const goBack = () => {
	safeNavigateBack()
}

// 跳转到作者详情
const goAuthorDetail = () => {
	if (wallpaperDetail.value?.author?.id) {
		uni.navigateTo({
			url: `/pagesAuthor/author-detail?id=${wallpaperDetail.value.author.id}`
		})
	}
}

// 处理喜欢
const handleLike = async () => {
	if (!wallpaperDetail.value) return
	if (!isLoggedIn()) {
		await ensureLoggedInForAction({
			redirectPath: previewLoginRedirect.value,
			content: '点赞需要先登录，是否前往登录？'
		})
		return
	}

	try {
		const res = await likeWallpaper(wallpaperDetail.value.id)
		if (res.code === 200) {
			// 直接更新数组中的对应项
			const currentWallpaper = wallpaperList.value[currentIndex.value]
			currentWallpaper.isLiked = !currentWallpaper.isLiked
			if (currentWallpaper.isLiked) {
				currentWallpaper.likeCount++
			} else {
				currentWallpaper.likeCount--
			}

			uni.showToast({
				title: currentWallpaper.isLiked ? '已添加到喜欢' : '已取消喜欢',
				icon: 'success'
			})
		} else {
			uni.showToast({
				title: res.message || '操作失败',
				icon: 'none'
			})
		}
	} catch (err) {
		console.error('点赞失败:', err)
		uni.showToast({
			title: '操作失败，请稍后重试',
			icon: 'none'
		})
	}
}

// 处理收藏
const handleCollect = async () => {
	if (!wallpaperDetail.value) return
	if (!isLoggedIn()) {
		await ensureLoggedInForAction({
			redirectPath: previewLoginRedirect.value,
			content: '收藏需要先登录，是否前往登录？'
		})
		return
	}

	try {
		const res = await collectWallpaper(wallpaperDetail.value.id)
		if (res.code === 200) {
			// 直接更新数组中的对应项
			const currentWallpaper = wallpaperList.value[currentIndex.value]
			currentWallpaper.isCollected = !currentWallpaper.isCollected
			if (currentWallpaper.isCollected) {
				currentWallpaper.collectCount++
			} else {
				currentWallpaper.collectCount--
			}

			uni.showToast({
				title: currentWallpaper.isCollected ? '已添加到收藏' : '已取消收藏',
				icon: 'success'
			})
		} else {
			uni.showToast({
				title: res.message || '操作失败',
				icon: 'none'
			})
		}
	} catch (err) {
		console.error('收藏失败:', err)
		uni.showToast({
			title: '操作失败，请稍后重试',
			icon: 'none'
		})
	}
}

// 处理下载
const handleDownload = async () => {
	if (!wallpaperDetail.value || !wallpaperDetail.value.url) {
		uni.showToast({
			title: '图片地址无效',
			icon: 'none'
		})
		return
	}
	if (!isLoggedIn()) {
		await ensureLoggedInForAction({
			redirectPath: previewLoginRedirect.value,
			content: '下载需登录账号，是否前往登录？'
		})
		return
	}

	const downloadedKey = `downloaded_${wallpaperDetail.value.id}`
	const hasDownloaded = uni.getStorageSync(downloadedKey)

	const onDownloadSuccess = async () => {
		if (!hasDownloaded) {
			try {
				await downloadWallpaper(wallpaperDetail.value.id)
				const currentWallpaper = wallpaperList.value[currentIndex.value]
				currentWallpaper.downloadCount++
				uni.setStorageSync(downloadedKey, true)
			} catch (err) {
				console.error('下载计数失败:', err)
			}
		}
	}

	uni.showLoading({
		title: '下载中...',
		mask: true
	})

	// #ifdef H5
	fetch(wallpaperDetail.value.url)
		.then(response => {
			if (!response.ok) throw new Error('下载失败')
			return response.blob()
		})
		.then(async (blob) => {
			const url = window.URL.createObjectURL(blob)
			const a = document.createElement('a')
			a.href = url
			a.download = `wallpaper_${wallpaperDetail.value.id}.jpg`
			document.body.appendChild(a)
			a.click()
			document.body.removeChild(a)
			window.URL.revokeObjectURL(url)

			await onDownloadSuccess()

			uni.hideLoading()
			uni.showToast({
				title: '下载成功',
				icon: 'success'
			})
		})
		.catch(err => {
			uni.hideLoading()
			uni.showToast({
				title: '下载失败',
				icon: 'none'
			})
		})
	// #endif

	// #ifndef H5
	uni.downloadFile({
		url: wallpaperDetail.value.url,
		success: async (res) => {
			if (res.statusCode === 200) {
				uni.saveImageToPhotosAlbum({
					filePath: res.tempFilePath,
					success: async () => {
						await onDownloadSuccess()

						uni.hideLoading()
						uni.showToast({
							title: '已保存到相册',
							icon: 'success'
						})
					},
					fail: (err) => {
						uni.hideLoading()
						if (err && (err.errMsg && (err.errMsg.includes('auth deny') || err.errMsg.includes('authorize')) || err.errCode === -1)) {
							uni.showModal({
								title: '提示',
								content: '需要您授权保存到相册，是否前往设置开启权限？',
								confirmText: '去设置',
								success: (res) => {
									if (res.confirm) {
										uni.openSetting({})
									}
								}
							})
						} else {
							uni.showToast({
								title: '保存失败',
								icon: 'none'
							})
						}
					}
				})
			} else {
				uni.hideLoading()
				uni.showToast({
					title: '下载失败',
					icon: 'none'
				})
			}
		},
		fail: () => {
			uni.hideLoading()
			uni.showToast({
				title: '下载失败',
				icon: 'none'
			})
		}
	})
	// #endif
}

// 切换遮罩层
const changeMask = () => {
	maskChange.value = !maskChange.value
}

// 轮播图切换
const onSwiperChange = (e) => {
	currentIndex.value = e.detail.current
	// 根据当前壁纸类型更新显示高度
	const currentItem = wallpaperList.value[currentIndex.value]
	if (currentItem && currentItem.type != null) {
		computedHeight.value = calcPreviewHeight(currentItem)
	}
	console.log('轮播图切换:', currentIndex.value, 'type:', currentItem?.type, 'height:', computedHeight.value)
}

// 打开详情弹窗
const openPopup = () => {
	popupRef.value?.open()
}

// 关闭详情弹窗
const closePopup = () => {
	popupRef.value?.close()
}

// 打开举报弹窗
const openReportPopup = async () => {
	if (!wallpaperDetail.value) return
	if (!isLoggedIn()) {
		await ensureLoggedInForAction({
			redirectPath: previewLoginRedirect.value,
			content: '举报需要先登录，是否前往登录？'
		})
		return
	}
	reportPopupRef.value?.open()
}

// 关闭举报弹窗
const closeReportPopup = () => {
	reportPopupRef.value?.close()
	reportReason.value = ''
}

// 举报弹窗状态改变
const onReportPopupChange = (e) => {
	if (!e.show) {
		reportReason.value = ''
	}
}

// 提交举报
const submitReport = async () => {
	if (!reportReason.value.trim()) {
		uni.showToast({
			title: '请填写举报原因',
			icon: 'none'
		})
		return
	}

	try {
		const res = await reportWallpaper(wallpaperDetail.value.id, reportReason.value.trim())
		if (res.code === 200) {
			uni.showToast({
				title: '举报提交成功',
				icon: 'success'
			})
			closeReportPopup()
		} else {
			uni.showToast({
				title: res.message || '举报提交失败',
				icon: 'none'
			})
		}
	} catch (err) {
		console.error('举报失败:', err)
		uni.showToast({
			title: '举报提交失败，请稍后重试',
			icon: 'none'
		})
	}
}
</script>

<style lang="scss" scoped>
.preview-detail {
	width: 100%;
	height: 100vh;
	position: relative;

	// 轮播图
	swiper {
		width: 100%;
		height: 100%;
		background: #fff;

		swiper-item {
			width: 100%;
			height: 100%;
			display: flex;
			align-items: center;
			justify-content: center;
			overflow: hidden;

			.preview-img {
				width: 100%;
				height: 100%;
				object-fit: cover;
				transition: height 0.5s ease-in-out;
			}
		}
	}

	// 加载状态
	.loading-container {
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		display: flex;
		align-items: center;
		justify-content: center;
		background: rgba(255, 255, 255, 0.9);
		z-index: 100;
	}

	.loading-text {
		font-size: 28rpx;
		color: #999;
	}

	// 错误状态
	.error-container {
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		background: rgba(255, 255, 255, 0.9);
		z-index: 100;
	}

	.error-text {
		font-size: 28rpx;
		color: #999;
		margin-bottom: 40rpx;
		text-align: center;
	}

	.retry-btn {
		padding: 20rpx 60rpx;
		background: #7d5442;
		color: #fff;
		border-radius: 40rpx;
		font-size: 28rpx;
	}

	// 遮罩层
	.mask {

		// 返回键：独占一行，左侧贴边
		.goBack {
			position: absolute;
			left: 0rpx;
			font-weight: 600;
			padding: 10rpx;
			color: #fff;
		}

		// 第二行：序号（居中） + 作者信息（右侧）
		.top-bar {
			position: absolute;
			left: 0;
			right: 0;
			display: flex;
			align-items: center;
			justify-content: center;
			padding: 0 30rpx;
			box-sizing: border-box;

			.count {
				background-color: rgba(0, 0, 0, 0.3);
				padding: 5rpx 20rpx;
				border-radius: 30rpx;
				font-size: 24rpx;
				color: #fff;
				white-space: nowrap;
			}

			.author-info {
				position: absolute;
				right: 24rpx;
				display: flex;
				flex-direction: row;
				align-items: center;
				gap: 14rpx;
				background-color: rgba(0, 0, 0, 0.28);
				border-radius: 60rpx;
				padding: 10rpx 24rpx 10rpx 10rpx;
				backdrop-filter: blur(8px);

				.author-avatar {
					width: 64rpx;
					height: 64rpx;
					flex-shrink: 0;

					.avatar {
						width: 100%;
						height: 100%;
						border-radius: 50%;
						border: 3rpx solid rgba(255, 255, 255, 0.8);
					}
				}

				.author-right {
					display: flex;
					flex-direction: column;
					gap: 4rpx;

					.author-name {
						font-size: 26rpx;
						font-weight: 600;
						color: #fff;
						white-space: nowrap;
					}

					.author-sub {
						font-size: 20rpx;
						color: rgba(255, 255, 255, 0.65);
						white-space: nowrap;
					}
				}
			}
		}

		.time,
		.date {
			position: absolute;
			left: 50%;
			transform: translateX(-50%);
			color: #fff;
		}

		.time {
			top: calc(11vh + 80rpx);
			font-size: 100rpx;
			font-weight: 700;
			text-shadow: 0 4rpx rgba(0, 0, 0, .3);
			white-space: nowrap;
		}

		.date {
			font-size: 40rpx;
			top: calc(11vh + 230rpx);
			text-shadow: 0 4rpx rgba(0, 0, 0, .3);
			white-space: nowrap;
		}

		// 平板时间日期
		.tablet-time-wrapper {
			position: absolute;
			left: 50%;
			top: 50%;
			transform: translate(-50%, -50%);
			text-align: center;
			color: #fff;

			.tablet-time {
				font-size: 80rpx;
				font-weight: 700;
				text-shadow: 0 4rpx rgba(0, 0, 0, .3);
				white-space: nowrap;
			}

			.tablet-date {
				font-size: 32rpx;
				text-shadow: 0 4rpx rgba(0, 0, 0, .3);
				white-space: nowrap;
			}
		}

		.footer {
			position: absolute;
			left: 50%;
			transform: translateX(-50%);
			bottom: 10vh;
			display: flex;
			align-items: center;
			justify-content: space-evenly;
			width: 65vw;
			height: 120rpx;
			background-color: rgba(255, 255, 255, .96);
			color: #534747;
			border-radius: 120rpx;
			box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12), 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
			border: 1rpx solid rgba(255, 255, 255, 0.8);

			.box {
				display: flex;
				flex-direction: column;
				align-items: center;
				justify-content: center;
				padding: 5rpx 10rpx;

				:deep(.uni-icons) {
					font-size: 40rpx !important;
				}

				.text {
					font-size: 26rpx;
				}
			}
		}
	}

	// 举报弹窗
	.reportPopup {
		background-color: #fff;
		border-radius: 30rpx 30rpx 0 0;
		padding: 20rpx 30rpx;
		padding-bottom: calc(env(safe-area-inset-bottom) + 30rpx);

		.popupTitle {
			display: flex;
			align-items: center;
			justify-content: space-between;
			height: 100rpx;
			font-size: 32rpx;

			.title {
				font-weight: 600;
				color: #534747;
			}

			.close {
				padding: 5rpx 0;
			}
		}

		.report-body {
			display: flex;
			flex-direction: column;
			gap: 16rpx;

			.report-label {
				font-size: 28rpx;
				color: #534747;
				font-weight: 500;
			}

			.report-textarea {
				width: 100%;
				min-height: 200rpx;
				background-color: #faf5f5;
				border: 2rpx solid #f0e4e4;
				border-radius: 16rpx;
				padding: 20rpx;
				font-size: 28rpx;
				color: #534747;
				box-sizing: border-box;
				line-height: 1.6;
			}

			.report-count {
				font-size: 22rpx;
				color: #bbb;
				text-align: right;
			}

			.report-btn {
				width: 100%;
				margin-top: 10rpx;
				background-color: #7d5442;
				color: #fff;
				border-radius: 60rpx;
				font-size: 30rpx;
				height: 90rpx;
				line-height: 90rpx;
				border: none;

				&[disabled] {
					background-color: #d4bfb9;
					color: rgba(255, 255, 255, 0.7);
				}

				&::after {
					border: none;
				}
			}
		}
	}

	// 详情弹窗
	.prpupInfo {
		padding: 20rpx;
		background-color: #fff;
		border-radius: 30rpx 30rpx 0 0;
		max-height: 60vh;
		font-size: 30rpx;

		// 标题
		.popupTitle {
			display: flex;
			align-items: center;
			justify-content: space-between;
			height: 100rpx;
			font-size: 32rpx;

			.close {
				padding: 5rpx 0;
			}
		}

		// 详情
		.detail-info {
			padding: 10rpx;
			padding-bottom: env(safe-area-inset-bottom);

			.detail-item {
				display: flex;
				margin: 40rpx;

				.left {
					width: 160rpx;
					color: #999;
				}

				.info {
					flex: 1;
					color: #613942;
				}

				.tag-item {
					display: flex;
					flex-wrap: wrap;
					align-content: flex-start;

					.tag {
						background-color: #f9dad8;
						color: #824c59;
						padding: 5rpx 20rpx;
						border-radius: 30rpx;
						font-size: 24rpx;
						margin: 10rpx;
					}
				}
			}
		}

		// 声明
		.description {
			background-color: #f2f2f2;
			padding: 20rpx;
			color: #ac999e;
			font-size: 28rpx;
		}
	}
}
</style>