<template>
	<view class="preview">
		<!-- 轮播图 -->
		<swiper :current="currentIndex" @change="onSwiperChange" circular :interval="3000" :duration="500">
			<swiper-item v-for="item in itemList" :key="item.id">
				<image :style="{ height: computedHeight + '%' }" @click="changeMask" class="preview-img" :src="item.url"
					mode="aspectFill"></image>
			</swiper-item>
		</swiper>

		<!-- 遮罩层 -->
		<view class="mask" v-if="maskChange">
			<!-- 返回键：独占一行，仅适配状态栏 -->
			<view class="goBack" :style="{ top: getStatusBarHeight() + 'px' }" @click="goBack()">
				<uni-icons type="left" size="28" color="#7d5442"></uni-icons>
			</view>
			<!-- 第二行：序号 + 作者信息 -->
			<view class="top-bar" :style="{ top: (getStatusBarHeight() + 56) + 'px' }">
				<view class="count">{{ currentIndex + 1 }} / {{ itemList.length }}</view>
				<view class="author-info" @click="goAuthorDetailPage()">
					<view class="author-avatar">
						<image class="avatar" src="/static/images/1.jpg" mode="aspectFill"></image>
					</view>
					<view class="author-right">
						<view class="author-name">夏天</view>
						<view class="author-sub">查看主页</view>
					</view>
				</view>
			</view>

			<view class="time" v-if="computedHeight === 100">{{ currentTime }}</view>
			<view class="date" v-if="computedHeight === 100">{{ currentDate }}</view>

			<!-- 平板预览时的时间日期（在图片内部） -->
			<view class="tablet-time-wrapper" v-if="computedHeight === 40">
				<view class="tablet-time">{{ currentTime }}</view>
				<view class="tablet-date">{{ currentDate }}</view>
			</view>

			<view class="footer">
				<view class="box" @click="openPopup()">
					<uni-icons type="info"></uni-icons>
					<view class="text">
						详情
					</view>
				</view>
				<view class="box">
					<uni-icons type="star" v-if="false"></uni-icons>
					<uni-icons class="star" type="star-filled" v-else color="#f4a460"></uni-icons>
					<view class="text">
						0
					</view>
				</view>
				<view class="box">
					<uni-icons type="heart" v-if="false"></uni-icons>
					<uni-icons type="heart-filled" v-else color="#e88b8b"></uni-icons>
					<view class="text">
						0
					</view>
				</view>
				<view class="box" @click="handleDownload()">
					<uni-icons type="download"></uni-icons>
					<view class="text">
						{{ downloadCount }}
					</view>
				</view>
				<view class="box" @click="openReportPopup()">
					<uni-icons type="chat" color="#534747" size="40"></uni-icons>
					<view class="text">举报</view>
				</view>
			</view>
		</view>

		<!-- 举报弹窗 -->
		<uni-popup safe-area ref="reportPopupRef" @change="onReportPopupChange">
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
		<uni-popup safe-area ref="popupRef">
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
						<view class="info">xiatian123412</view>
					</view>
					<view class="detail-item">
						<view class="left">分类：</view>
						<view class="info">卡通可爱</view>
					</view>
					<view class="detail-item">
						<view class="left">发布者：</view>
						<view class="info">夏天</view>
					</view>
					<view class="detail-item">
						<view class="left">标签：</view>
						<view class="tag-item">
							<view class="tag">可爱</view>
							<view class="tag">二次元</view>
							<view class="tag">卡通</view>
							<view class="tag">少女心</view>
							<view class="tag">手机壁纸</view>
							<view class="tag">米菲</view>
						</view>
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
import {
	computed,
	ref
} from "vue"
import {
	getStatusBarHeight
} from '@/utils/system.js'
import {
	onLoad
} from '@dcloudio/uni-app'
import { safeNavigateBack } from '@/utils/navigation.js'
const props = defineProps({
	items: {
		type: Array,
		default: () => ([])
	}
})
const itemList = ref(props.items || [])

// 遮罩层的改变
const maskChange = ref(true)

// 底部弹窗
const popupRef = ref(null)

// 计算的高度
const computedHeight = ref(50)

// 是否是单图预览
const isPreview = ref(false)

// 不同设备对应不同的尺寸
const types = {
	1: 100,  // 手机壁纸
	2: 40,   // 平板壁纸
	3: 50    // 头像
}

// 当前下标
const currentIndex = ref(0)

// 动态时间日期
const _now = new Date()
const currentTime = ref(`${_now.getHours().toString().padStart(2, '0')}:${_now.getMinutes().toString().padStart(2, '0')}`)
const _weeks = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
const currentDate = ref(`${_weeks[_now.getDay()]} ${_now.getMonth() + 1}月${_now.getDate()}日`)

// 接收传递的参数
onLoad((options) => {
	const body = JSON.parse(decodeURIComponent(options.data))
	if (!body?.length) {
		return
	}
	currentIndex.value = options?.index ? Number(options.index) : 0
	isPreview.value = true
	// 以数组的第一个图片大小为基准
	computedHeight.value = types[body[0].type]
	itemList.value.push(...body)
})

// swiper 切换时更新序号
const onSwiperChange = (e) => {
	currentIndex.value = e.detail.current
}

// 打开弹窗
const openPopup = () => {
	if (popupRef.value) {
		popupRef.value.open('bottom')
	}
}

// 关闭弹窗
const closePopup = () => {
	popupRef.value.close()
}

// 控制遮罩层的显示
const changeMask = () => {
	maskChange.value = !maskChange.value
}

// 跳转到作者详情页
const goAuthorDetailPage = () => {
	const item = itemList.value[currentIndex.value]
	const aid = item?.userId ?? item?.authorId ?? item?.author?.id
	if (aid === undefined || aid === null || aid === '') {
		uni.showToast({
			title: '暂无作者信息',
			icon: 'none'
		})
		return
	}
	uni.navigateTo({
		url: `/pagesAuthor/author-detail?id=${aid}`
	})
}

// 举报弹窗
const reportPopupRef = ref(null)
const reportReason = ref('')

// 下载计数
const downloadCount = ref(1)
const isDownloading = ref(false)

// 下载壁纸
const handleDownload = () => {
	if (isDownloading.value) {
		uni.showToast({
			title: '正在下载中...',
			icon: 'none'
		})
		return
	}

	const currentItem = itemList.value[currentIndex.value]
	if (!currentItem || !currentItem.url) {
		uni.showToast({
			title: '图片地址无效',
			icon: 'none'
		})
		return
	}

	isDownloading.value = true
	uni.showLoading({
		title: '下载中...',
		mask: true
	})

	// #ifdef H5
	// H5 环境：使用 fetch + Blob 方式下载（更现代的方式）
	fetch(currentItem.url)
		.then(response => {
			if (!response.ok) throw new Error('下载失败')
			return response.blob()
		})
		.then(blob => {
			// 创建 Blob URL
			const url = window.URL.createObjectURL(blob)

			// 创建临时 a 标签触发下载
			const a = document.createElement('a')
			a.href = url
			a.download = `wallpaper_${Date.now()}.jpg`
			document.body.appendChild(a)
			a.click()

			// 清理
			document.body.removeChild(a)
			window.URL.revokeObjectURL(url)

			uni.hideLoading()
			uni.showToast({
				title: '下载成功',
				icon: 'success'
			})
			downloadCount.value++
			saveDownloadRecord(currentItem)
			isDownloading.value = false
		})
		.catch(error => {
			uni.hideLoading()
			isDownloading.value = false
			uni.showToast({
				title: '下载失败',
				icon: 'none'
			})
			console.error('下载错误:', error)
		})
	// #endif

	// #ifndef H5
	// 小程序/App 环境使用原生 API
	uni.downloadFile({
		url: currentItem.url,
		success: (res) => {
			if (res.statusCode === 200) {
				// 保存到相册
				uni.saveImageToPhotosAlbum({
					filePath: res.tempFilePath,
					success: () => {
						uni.hideLoading()
						uni.showToast({
							title: '保存成功',
							icon: 'success'
						})
						downloadCount.value++
						saveDownloadRecord(currentItem)
						isDownloading.value = false
					},
					fail: (err) => {
						uni.hideLoading()
						isDownloading.value = false
						if (err.errMsg.includes('auth deny') || err.errMsg.includes('authorize')) {
							uni.showModal({
								title: '提示',
								content: '需要授权访问相册才能保存图片',
								confirmText: '去设置',
								success: (modalRes) => {
									if (modalRes.confirm) {
										uni.openSetting()
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
				isDownloading.value = false
				uni.showToast({
					title: '下载失败',
					icon: 'none'
				})
			}
		},
		fail: (err) => {
			uni.hideLoading()
			isDownloading.value = false
			uni.showToast({
				title: '下载失败',
				icon: 'none'
			})
		}
	})
	// #endif
}

// 保存下载记录到本地存储
const saveDownloadRecord = (item) => {
	try {
		// 获取现有的下载记录
		const records = uni.getStorageSync('downloadRecords') || []

		// 添加新记录
		const newRecord = {
			id: item.id,
			url: item.url,
			type: item.type,
			downloadTime: new Date().getTime(),
			timestamp: Date.now()
		}

		// 检查是否已存在，避免重复
		const existIndex = records.findIndex(r => r.id === item.id && r.url === item.url)
		if (existIndex > -1) {
			// 更新下载时间
			records[existIndex] = newRecord
		} else {
			// 添加到开头
			records.unshift(newRecord)
		}

		// 只保留最近100条记录
		const limitedRecords = records.slice(0, 100)

		// 保存到本地存储
		uni.setStorageSync('downloadRecords', limitedRecords)
	} catch (error) {
		console.error('保存下载记录失败:', error)
	}
}

const openReportPopup = () => {
	reportReason.value = ''
	reportPopupRef.value?.open('bottom')
}

const closeReportPopup = () => {
	reportPopupRef.value?.close()
}

const onReportPopupChange = (e) => {
	if (!e.show) reportReason.value = ''
}

const submitReport = () => {
	const reason = reportReason.value.trim()
	if (!reason) return
	// TODO: 替换为实际接口调用，此处模拟提交
	uni.showLoading({ title: '提交中...' })
	setTimeout(() => {
		uni.hideLoading()
		closeReportPopup()
		uni.showToast({ title: '感谢您的反馈', icon: 'success' })
	}, 800)
}

// 返回上一层
const goBack = () => {
	safeNavigateBack()
}
</script>

<style lang="scss" scoped>
.preview {
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
				box-shadow: 0 0 0 1rpx rgba(0, 0, 0, 0.05);
				transition: height 0.5s ease-in-out;
			}
		}
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

				.right {
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