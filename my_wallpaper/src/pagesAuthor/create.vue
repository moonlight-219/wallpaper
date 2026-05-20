<template>
	<view class="create">
		<!-- 导航栏 -->
		<myTitleBar title="上传作品">
			<template #default>
				<uni-icons type="left" size="20" @click="goBack()"></uni-icons>
			</template>
		</myTitleBar>

		<!-- 裁剪组件 -->
		<imageCropper v-if="showCropModal" :imageUrl="cropData.imageUrl" :imageInfo="cropData.imageInfo"
			:targetWidth="cropData.targetWidth" :targetHeight="cropData.targetHeight" :categoryType="currentTabConfig.type"
			@close="closeCropModal" @confirm="handleCropConfirm" />

		<!-- 分类选择弹窗 -->
		<view class="category-modal" v-if="showCategoryModal" @click="closeCategoryModal">
			<view class="modal-content" @click.stop>
				<view class="modal-header">
					<text class="modal-title">选择图片分类</text>
					<uni-icons type="close" size="24" @click="closeCategoryModal"></uni-icons>
				</view>
				<view class="category-list">
					<view class="category-item" :class="{ selected: selectedCategory === category.value }"
						v-for="category in categories" :key="category.value" @click="selectCategory(category.value)">
						<text class="category-name">{{ category.label }}</text>
						<uni-icons v-if="selectedCategory === category.value" type="checkmarkempty" size="20"
							color="#a89187"></uni-icons>
					</view>
				</view>
				<view class="modal-footer">
					<button class="cancel-btn" @click="closeCategoryModal">取消</button>
					<button class="confirm-btn" :class="{ disabled: !selectedCategory }" :disabled="!selectedCategory"
						@click="confirmSubmit">
						确认提交
					</button>
				</view>
			</view>
		</view>

		<view class="content">
			<!-- Tab切换区 -->
			<view class="tabs">
				<view class="tab-item" :class="{ active: activeTab === index }" v-for="(tab, index) in tabs" :key="index"
					@click="switchTab(index)">
					<text class="tab-text">{{ tab.name }}</text>
					<view class="tab-indicator" v-if="activeTab === index"></view>
				</view>
			</view>

			<!-- 上传区域 -->
			<view class="upload-section">
				<view class="upload-tip">
					<uni-icons type="info" size="16" color="#999"></uni-icons>
					<text class="tip-text">{{ currentTabConfig.tip }}</text>
				</view>

				<!-- 图片网格 -->
				<view class="image-grid">
					<!-- 已上传的图片 -->
					<view class="image-item" v-for="(img, index) in uploadedImages" :key="index" 
						@touchstart="onTouchStart($event, index)" 
						@touchmove="onTouchMove($event, index)" 
						@touchend="onTouchEnd"
						:class="{ 'dragging': dragIndex === index }">
						<image class="preview-image" :src="img.url" mode="aspectFill" lazy-load @click="previewImage(index)"></image>
						<view class="delete-btn" @click.stop="deleteImage(index)">
							<uni-icons type="close" size="16" color="#fff"></uni-icons>
						</view>
						<!-- 图片信息标签 -->
						<view class="image-info">
							<text class="info-text">{{ img.width }}×{{ img.height }}</text>
						</view>
						<!-- 拖动提示 -->
						<view class="drag-handle" v-if="uploadedImages.length > 1">
							<uni-icons type="bars" size="14" color="#fff"></uni-icons>
						</view>
					</view>

					<!-- 上传按钮 -->
					<view class="upload-btn" v-if="uploadedImages.length < 6" @click="chooseImage">
						<view class="upload-btn-content">
							<uni-icons type="plusempty" size="40" color="#999"></uni-icons>
							<text class="upload-text">上传图片</text>
							<text class="count-text">{{ uploadedImages.length }}/6</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 作品信息表单 -->
			<view class="form-section">
				<view class="form-item">
					<view class="form-label">作品标题</view>
					<input class="form-input" v-model="workTitle" placeholder="请输入作品标题（选填）" maxlength="50" />
				</view>
				<view class="form-item">
					<view class="form-label">作品描述</view>
					<textarea class="form-textarea" v-model="workDescription" placeholder="请输入作品描述（选填）" maxlength="200" />
				</view>
			</view>

			<!-- 底部按钮区 -->
			<view class="bottom-actions">
				<button class="submit-btn btn" :class="{ disabled: !canSubmit }" :disabled="!canSubmit" @click="handleSubmit">
					提交作品
				</button>
				<button class="history-btn btn" @click="goToHistory">
					我的上传记录
				</button>
			</view>
		</view>

		<!-- 隐藏的Canvas用于图片裁剪 -->
		<canvas canvas-id="cropCanvas" :style="{
			position: 'fixed',
			left: '-9999px',
			top: '-9999px',
			width: '2160px',
			height: '2160px'
		}"></canvas>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import myTitleBar from '@/components/myTitleBar.vue'
import imageCropper from '@/components/imageCropper.vue'
import { uploadImage, createWork } from '@/apis/upload'
import { getCategoryList } from '@/apis/category'
import { isLoggedIn, promptUploadAuth, navigateToLogin } from '@/utils/auth.js'
import { safeNavigateBack } from '@/utils/navigation.js'

const CREATE_PAGE = '/pagesAuthor/create'

/** 未登录时用户选择「仅浏览」后，禁止上传相关操作 */
const browseOnly = ref(false)
/** 避免每次 onShow 重复弹出登录说明 */
const uploadAuthPrompted = ref(false)

// Tab配置
const tabs = ref([
	{
		name: '手机壁纸',
		tip: '建议比例：9:20，大小不超过5MB',
		width: 9,
		height: 20,
		maxSize: 5 * 1024 * 1024,
		type: 1
	},
	{
		name: '平板壁纸',
		tip: '建议比例：16:21，大小不超过8MB',
		width: 16,
		height: 21,
		maxSize: 8 * 1024 * 1024,
		type: 2
	},
	{
		name: '头像分享',
		tip: '建议比例：1:1，大小不超过2MB',
		width: 1,
		height: 1,
		maxSize: 2 * 1024 * 1024,
		type: 3
	}
])

const activeTab = ref(0)
const uploadedImages = ref([])
const showCropModal = ref(false)
const cropData = ref({
	imageUrl: '',
	imageInfo: {},
	targetWidth: 0,
	targetHeight: 0
})

// 拖动排序相关
const dragIndex = ref(-1)
const dragStartX = ref(0)
const dragStartY = ref(0)
const dragOffsetX = ref(0)
const dragOffsetY = ref(0)

// 分类选择相关
const showCategoryModal = ref(false)
const selectedCategory = ref('')
const categories = ref([])

// 作品信息
const workTitle = ref('')
const workDescription = ref('')

// 当前Tab配置
const currentTabConfig = computed(() => tabs.value[activeTab.value])

// 是否可以提交
const canSubmit = computed(() => {
	return uploadedImages.value.length > 0
})

// 加载分类列表
const loadCategories = async () => {
	try {
		const res = await getCategoryList({ page: 1, pageSize: 100 })
		if (res.code === 200 && res.data && res.data.list) {
			categories.value = res.data.list.map(cat => ({
				label: cat.name,
				value: cat.id
			}))
		}
	} catch (error) {
		console.error('加载分类失败:', error)
		uni.showToast({
			title: '加载分类失败',
			icon: 'none'
		})
	}
}

// 页面加载时获取分类列表
onMounted(() => {
	loadCategories()
})

onShow(() => {
	if (isLoggedIn()) {
		browseOnly.value = false
		return
	}
	if (uploadAuthPrompted.value) return
	uploadAuthPrompted.value = true
	promptUploadAuth(CREATE_PAGE).then((choice) => {
		if (choice === 'browse') browseOnly.value = true
	})
})

// 切换Tab
const switchTab = (index) => {
	if (uploadedImages.value.length > 0) {
		uni.showModal({
			title: '提示',
			content: '切换分类将清空已上传的图片，是否继续？',
			success: (res) => {
				if (res.confirm) {
					activeTab.value = index
					uploadedImages.value = []
				}
			}
		})
	} else {
		activeTab.value = index
	}
}

// 选择图片
const chooseImage = () => {
	if (!isLoggedIn()) {
		if (browseOnly.value) {
			uni.showToast({ title: '当前为仅浏览，请先登录后再上传', icon: 'none' })
			return
		}
		promptUploadAuth(CREATE_PAGE).then((choice) => {
			if (choice === 'browse') browseOnly.value = true
		})
		return
	}
	const remainCount = 6 - uploadedImages.value.length
	uni.chooseImage({
		count: remainCount,
		sizeType: ['original'],
		sourceType: ['album', 'camera'],
		success: (res) => {
			res.tempFilePaths.forEach((path) => {
				validateAndAddImage(path)
			})
		}
	})
}

// 校验并添加图片
const validateAndAddImage = (path) => {
	const config = currentTabConfig.value

	uni.getImageInfo({
		src: path,
		success: (info) => {
			// 校验比例（允许10%误差）
			const imageRatio = info.width / info.height
			const targetRatio = config.width / config.height
			const ratioDiff = Math.abs(imageRatio - targetRatio) / targetRatio

			const needsCrop = ratioDiff > 0.1

			// 校验文件大小
			uni.getFileInfo({
				filePath: path,
				success: (fileInfo) => {
					if (fileInfo.size > config.maxSize) {
						const sizeMB = (fileInfo.size / 1024 / 1024).toFixed(2)
						const maxSizeMB = (config.maxSize / 1024 / 1024).toFixed(0)
						uni.showToast({
							title: `上传失败：图片大小超过${maxSizeMB}MB（当前${sizeMB}MB）`,
							icon: 'none',
							duration: 3000
						})
						return
					}

					if (needsCrop) {
						openCropModal(path)
					} else {
						// 先上传到 OSS，再添加到列表
						uni.showLoading({
							title: '上传中...'
						})
						
						uploadImage(path, config.type).then(uploadRes => {
							uni.hideLoading()
							if (uploadRes.code === 200) {
								uploadedImages.value.push({
									url: uploadRes.data.url,
									thumbnailUrl: uploadRes.data.thumbnailUrl || uploadRes.data.url,
									width: uploadRes.data.width || info.width,
									height: uploadRes.data.height || info.height,
									fileFormat: uploadRes.data.format || 'jpg',
									fileSize: uploadRes.data.size || fileInfo.size
								})
								uni.showToast({
									title: '上传成功',
									icon: 'success',
									duration: 1500
								})
							} else {
								uni.showToast({
									title: uploadRes.message || '上传失败',
									icon: 'none',
									duration: 2000
								})
							}
						}).catch(error => {
							uni.hideLoading()
							console.error('上传失败:', error)
							uni.showToast({
								title: '上传失败，请重试',
								icon: 'none',
								duration: 2000
							})
						})
					}
				},
				fail: () => {
					if (needsCrop) {
						uni.showModal({
							title: '比例不符',
							content: `图片比例为${imageRatio.toFixed(2)}，建议比例为${targetRatio.toFixed(2)}，是否进行裁剪？`,
							confirmText: '去裁剪',
							cancelText: '放弃',
							success: (res) => {
								if (res.confirm) {
									openCropModal(path)
								}
							}
						})
					} else {
						// 先上传到 OSS，再添加到列表
						uni.showLoading({
							title: '上传中...'
						})
						
						uploadImage(path, config.type).then(uploadRes => {
							uni.hideLoading()
							if (uploadRes.code === 200) {
								uploadedImages.value.push({
									url: uploadRes.data.url,
									thumbnailUrl: uploadRes.data.thumbnailUrl || uploadRes.data.url,
									width: uploadRes.data.width || info.width,
									height: uploadRes.data.height || info.height,
									fileFormat: uploadRes.data.format || 'jpg',
									fileSize: uploadRes.data.size || 0
								})
								uni.showToast({
									title: '上传成功',
									icon: 'success',
									duration: 1500
								})
							} else {
								uni.showToast({
									title: uploadRes.message || '上传失败',
									icon: 'none',
									duration: 2000
								})
							}
						}).catch(error => {
							uni.hideLoading()
							console.error('上传失败:', error)
							uni.showToast({
								title: '上传失败，请重试',
								icon: 'none',
								duration: 2000
							})
						})
					}
				}
			})
		},
		fail: () => {
			uni.showToast({
				title: '上传失败：无法读取图片信息',
				icon: 'none',
				duration: 2000
			})
		}
	})
}

// 打开裁剪弹窗
const openCropModal = (path) => {
	const config = currentTabConfig.value

	// 获取图片信息
	uni.getImageInfo({
		src: path,
		success: (info) => {
			cropData.value = {
				imageUrl: path,
				imageInfo: {
					width: info.width,
					height: info.height
				},
				targetWidth: config.width,
				targetHeight: config.height
			}

			showCropModal.value = true
		}
	})
}

// 关闭裁剪弹窗
const closeCropModal = () => {
	showCropModal.value = false
}

// 裁剪确认
const handleCropConfirm = async (cropInfo) => {
	console.log('收到裁剪信息:', cropInfo)

	uni.showLoading({
		title: '裁剪中...'
	})

	const config = currentTabConfig.value

	// 智能计算输出尺寸：保持高质量，但控制在合理范围
	// 手机壁纸最大宽度 1080px，平板壁纸 1440px，头像 1024px
	let maxWidth
	if (config.name === '手机壁纸') {
		maxWidth = 1080
	} else if (config.name === '平板壁纸') {
		maxWidth = 1440
	} else {
		maxWidth = 1024 // 头像
	}

	// 如果裁剪区域宽度超过最大宽度，按比例缩放；否则保持原尺寸
	let outputWidth, outputHeight
	if (cropInfo.width > maxWidth) {
		const scale = maxWidth / cropInfo.width
		outputWidth = maxWidth
		outputHeight = Math.round(cropInfo.height * scale)
	} else {
		outputWidth = Math.round(cropInfo.width)
		outputHeight = Math.round(cropInfo.height)
	}

	console.log('裁剪区域:', cropInfo)
	console.log('输出尺寸:', { width: outputWidth, height: outputHeight })

	const ctx = uni.createCanvasContext('cropCanvas')

	// 绘制裁剪后的图片
	// 参数：原图路径, 裁剪起点x, 裁剪起点y, 裁剪宽度, 裁剪高度, 画布起点x, 画布起点y, 画布宽度, 画布高度
	ctx.drawImage(
		cropData.value.imageUrl,
		Math.round(cropInfo.x),
		Math.round(cropInfo.y),
		Math.round(cropInfo.width),
		Math.round(cropInfo.height),
		0,
		0,
		outputWidth,
		outputHeight
	)

	ctx.draw(false, () => {
		// 延迟一下确保绘制完成
		setTimeout(async () => {
			// 导出裁剪后的图片
			uni.canvasToTempFilePath({
				canvasId: 'cropCanvas',
				x: 0,
				y: 0,
				width: outputWidth,
				height: outputHeight,
				destWidth: outputWidth,
				destHeight: outputHeight,
				fileType: 'jpg',
				quality: 0.92, // 提高质量到 92%，减少失真
				success: async (res) => {
					uni.hideLoading()

					// 上传图片到服务器
					uni.showLoading({
						title: '上传中...'
					})

					try {
						const uploadRes = await uploadImage(res.tempFilePath, config.type)
						if (uploadRes.code === 200) {
							uploadedImages.value.push({
								url: uploadRes.data.url,
								thumbnailUrl: uploadRes.data.thumbnailUrl || uploadRes.data.url,
								width: uploadRes.data.width || outputWidth,
								height: uploadRes.data.height || outputHeight,
								fileFormat: uploadRes.data.format || 'jpg',
								fileSize: uploadRes.data.size || 0,
								isCropped: true
							})

							uni.showToast({
								title: '上传成功',
								icon: 'success',
								duration: 1500
							})

							closeCropModal()
						} else {
							uni.showToast({
								title: uploadRes.message || '上传失败',
								icon: 'none',
								duration: 2000
							})
						}
					} catch (error) {
						console.error('提交失败:', error)
						uni.hideLoading()

						// 提取错误信息
						let errorMessage = '提交失败，请重试'
						if (error.message) {
							errorMessage = error.message
						} else if (error.data && error.data.message) {
							errorMessage = error.data.message
						} else if (typeof error === 'string') {
							errorMessage = error
						}

						uni.showToast({
							title: errorMessage,
							icon: 'none',
							duration: 2000
						})
					}
				},
				fail: (err) => {
					console.error('裁剪失败:', err)
					uni.hideLoading()
					uni.showToast({
						title: '裁剪失败，请重试',
						icon: 'none',
						duration: 2000
					})
				}
			})
		}, 100)
	})
}

// 删除图片
const deleteImage = (index) => {
	uni.showModal({
		title: '提示',
		content: '确定要删除这张图片吗？',
		success: (res) => {
			if (res.confirm) {
				uploadedImages.value.splice(index, 1)
				uni.showToast({
					title: '已删除',
					icon: 'success',
					duration: 1500
				})
			}
		}
	})
}

// 拖动排序相关方法
const onTouchStart = (e, index) => {
	if (uploadedImages.value.length <= 1) return
	dragIndex.value = index
	const touch = e.touches[0]
	dragStartX.value = touch.clientX
	dragStartY.value = touch.clientY
}

const onTouchMove = (e, index) => {
	if (dragIndex.value === -1 || dragIndex.value !== index) return
	const touch = e.touches[0]
	const moveX = touch.clientX - dragStartX.value
	const moveY = touch.clientY - dragStartY.value
	
	// 判断移动方向和距离
	if (Math.abs(moveX) > 50 || Math.abs(moveY) > 50) {
		// 计算目标位置
		const itemWidth = uni.upx2px(200) + uni.upx2px(20) // 图片宽度 + 间距
		const cols = 3
		const currentRow = Math.floor(index / cols)
		const currentCol = index % cols
		
		let targetIndex = index
		if (moveX > 50) {
			// 向右移动
			targetIndex = Math.min(index + 1, uploadedImages.value.length - 1)
		} else if (moveX < -50) {
			// 向左移动
			targetIndex = Math.max(index - 1, 0)
		}
		
		if (targetIndex !== index) {
			// 交换位置
			const temp = uploadedImages.value[index]
			uploadedImages.value[index] = uploadedImages.value[targetIndex]
			uploadedImages.value[targetIndex] = temp
			dragIndex.value = targetIndex
			dragStartX.value = touch.clientX
			dragStartY.value = touch.clientY
		}
	}
}

const onTouchEnd = () => {
	dragIndex.value = -1
}

// 预览图片
const previewImage = (index) => {
	const urls = uploadedImages.value.map(img => img.url)
	uni.previewImage({
		current: index,
		urls: urls
	})
}

// 提交作品
const handleSubmit = async () => {
	if (!isLoggedIn()) {
		if (browseOnly.value) {
			uni.showToast({ title: '请先登录后再提交作品', icon: 'none' })
			return
		}
		promptUploadAuth(CREATE_PAGE).then((choice) => {
			if (choice === 'browse') browseOnly.value = true
		})
		return
	}
	// 检查是否已上传图片
	if (uploadedImages.value.length === 0) {
		uni.showToast({
			title: '请至少上传一张图片',
			icon: 'none',
			duration: 2000
		})
		return
	}

	// 打开分类选择弹窗
	showCategoryModal.value = true
}

// 选择分类
const selectCategory = (value) => {
	selectedCategory.value = value
}

// 关闭分类弹窗
const closeCategoryModal = () => {
	showCategoryModal.value = false
}

// 确认提交
const confirmSubmit = async () => {
	if (!isLoggedIn()) {
		showCategoryModal.value = false
		uni.showToast({ title: '登录已失效，请重新登录', icon: 'none' })
		navigateToLogin(CREATE_PAGE)
		return
	}
	if (!selectedCategory.value) {
		uni.showToast({
			title: '请选择分类',
			icon: 'none',
			duration: 2000
		})
		return
	}

	uni.showLoading({
		title: '提交中...'
	})

	try {
		const workData = {
			title: workTitle.value.trim() || '',
			type: currentTabConfig.value.type,
			categoryId: selectedCategory.value,
			description: workDescription.value.trim() || '',
			wallpapers: uploadedImages.value.map(img => ({
				url: img.url,
				thumbnailUrl: img.thumbnailUrl,
				imageWidth: img.width,
				imageHeight: img.height,
				fileFormat: img.fileFormat,
				fileSize: img.fileSize
			}))
		}

		const result = await createWork(workData)

		if (result.code === 200) {
			uni.hideLoading()
			uni.showToast({
				title: '提交成功',
				icon: 'success',
				duration: 1500
			})

			// 清空数据
			workTitle.value = ''
			workDescription.value = ''
			uploadedImages.value = []
			selectedCategory.value = null
			showCategoryModal.value = false
		} else {
			uni.hideLoading()
			uni.showToast({
				title: result.message || '提交失败',
				icon: 'none',
				duration: 2000
			})
		}
	} catch (error) {
		console.error('提交失败:', error)
		uni.hideLoading()

		// 提取错误信息
		let errorMessage = '提交失败，请重试'
		if (error.message) {
			errorMessage = error.message
		} else if (error.data && error.data.message) {
			errorMessage = error.data.message
		} else if (typeof error === 'string') {
			errorMessage = error
		}

		uni.showToast({
			title: errorMessage,
			icon: 'none',
			duration: 2000
		})
	}
}

// 跳转到上传记录
const goToHistory = () => {
	if (!isLoggedIn()) {
		uni.showModal({
			title: '需要登录',
			content: '查看上传记录需要先登录，是否前往？',
			confirmText: '去登录',
			cancelText: '取消',
			confirmColor: '#7a544d',
			success: (res) => {
				if (res.confirm) navigateToLogin('/pagesAuthor/history')
			}
		})
		return
	}
	uni.navigateTo({
		url: '/pagesAuthor/history'
	})
}

// 返回
const goBack = () => {
	safeNavigateBack()
}
</script>

<style lang="scss" scoped>
.create {
	width: 100vw;
	min-height: 100vh;
	background: $self-background-color;
	padding-bottom: 200rpx;

	.content {
		padding: 30rpx;

		// Tab切换区
		.tabs {
			display: flex;
			justify-content: space-around;
			background: #fff;
			border-radius: 20rpx;
			padding: 20rpx 0;
			margin-bottom: 30rpx;
			box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

			.tab-item {
				flex: 1;
				display: flex;
				flex-direction: column;
				align-items: center;
				position: relative;
				padding: 10rpx 0;

				.tab-text {
					font-size: 28rpx;
					color: #666;
					transition: all 0.3s;
				}

				.tab-indicator {
					width: 40rpx;
					height: 6rpx;
					background: linear-gradient(90deg, #a89187, #d4c4b8);
					border-radius: 3rpx;
					margin-top: 10rpx;
				}

				&.active .tab-text {
					color: #a89187;
					font-weight: 600;
				}
			}
		}

		// 上传区域
		.upload-section {
			background: #fff;
			border-radius: 20rpx;
			padding: 30rpx;
			margin-bottom: 30rpx;

			.upload-tip {
				display: flex;
				align-items: center;
				padding: 20rpx;
				background: #f7f8fa;
				border-radius: 10rpx;
				margin-bottom: 30rpx;

				.tip-text {
					font-size: 24rpx;
					color: #999;
					margin-left: 10rpx;
				}
			}

			// 图片网格
			.image-grid {
				display: grid;
				grid-template-columns: repeat(3, 1fr);
				gap: 20rpx;

				.image-item {
					position: relative;
					width: 100%;
					padding-bottom: 100%;
					border-radius: 10rpx;
					overflow: hidden;
					transition: all 0.3s ease;

					&.dragging {
						opacity: 0.7;
						transform: scale(1.05);
						z-index: 10;
						box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.2);
					}

					.preview-image {
						position: absolute;
						top: 0;
						left: 0;
						width: 100%;
						height: 100%;
						object-fit: cover;
					}

					.delete-btn {
						position: absolute;
						top: 10rpx;
						right: 10rpx;
						width: 40rpx;
						height: 40rpx;
						background: rgba(0, 0, 0, 0.5);
						border-radius: 50%;
						display: flex;
						align-items: center;
						justify-content: center;
						z-index: 2;
					}

					.image-info {
						position: absolute;
						bottom: 0;
						left: 0;
						right: 0;
						padding: 10rpx;
						background: linear-gradient(to top, rgba(0, 0, 0, 0.6), transparent);

						.info-text {
							font-size: 20rpx;
							color: #fff;
						}
					}

					.drag-handle {
						position: absolute;
						top: 10rpx;
						left: 10rpx;
						width: 40rpx;
						height: 40rpx;
						background: rgba(0, 0, 0, 0.5);
						border-radius: 50%;
						display: flex;
						align-items: center;
						justify-content: center;
						z-index: 2;
					}
				}

				.upload-btn {
					width: 100%;
					padding-bottom: 100%;
					position: relative;
					border: 2rpx dashed #d9d9d9;
					border-radius: 10rpx;
					background: #fafafa;

					.upload-btn-content {
						position: absolute;
						top: 0;
						left: 0;
						right: 0;
						bottom: 0;
						display: flex;
						flex-direction: column;
						align-items: center;
						justify-content: center;
					}

					.uni-icons {
						margin-bottom: 15rpx;
					}

					.upload-text {
						font-size: 24rpx;
						color: #999;
						margin-bottom: 10rpx;
					}

					.count-text {
						font-size: 20rpx;
						color: #ccc;
					}
				}
			}
		}

		// 表单区域
		.form-section {
			background: #fff;
			border-radius: 20rpx;
			padding: 30rpx;
			margin-bottom: 30rpx;

			.form-item {
				margin-bottom: 30rpx;

				&:last-child {
					margin-bottom: 0;
				}

				.form-label {
					font-size: 28rpx;
					color: #333;
					margin-bottom: 15rpx;
					font-weight: 500;
				}

				.form-input {
					width: 100%;
					height: 80rpx;
					padding: 0 20rpx;
					background: #f7f8fa;
					border-radius: 10rpx;
					font-size: 28rpx;
					color: #333;
					border: none;

					&::placeholder {
						color: #999;
					}
				}

				.form-textarea {
					width: 100%;
					min-height: 150rpx;
					padding: 20rpx;
					background: #f7f8fa;
					border-radius: 10rpx;
					font-size: 28rpx;
					color: #333;
					border: none;
					box-sizing: border-box;

					&::placeholder {
						color: #999;
					}
				}
			}
		}

		// 底部按钮区
		.bottom-actions {
			position: fixed;
			bottom: 0;
			left: 0;
			right: 0;
			padding: 30rpx;
			background: #fff;
			box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
			z-index: 10;
			overflow: hidden;

			.btn {
				border-radius: 40rpx;

				&::after {
					border: none;
				}
			}


			.submit-btn {
				width: 100%;
				height: 90rpx;
				background: linear-gradient(135deg, #a89187, #d4c4b8);
				color: #fff;
				font-size: 32rpx;
				margin-bottom: 20rpx;
				border: none;

				&.disabled {
					background: #f5f5f5;
					color: #ccc;
				}
			}

			.history-btn {
				width: 100%;
				height: 80rpx;
				background: #fff;
				color: #a89187;
				font-size: 28rpx;
				border: 2rpx solid #a89187;
			}
		}
	}

	// 分类选择弹窗
	.category-modal {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.5);
		display: flex;
		align-items: flex-end;
		z-index: 999;

		.modal-content {
			width: 100%;
			background: #fff;
			border-radius: 30rpx 30rpx 0 0;
			padding: 40rpx 30rpx;
			animation: slideUp 0.3s ease-out;

			.modal-header {
				display: flex;
				justify-content: space-between;
				align-items: center;
				margin-bottom: 30rpx;

				.modal-title {
					font-size: 32rpx;
					font-weight: 600;
					color: #333;
				}
			}

			.category-list {
				max-height: 60vh;
				overflow-y: auto;
				margin-bottom: 30rpx;

				.category-item {
					display: flex;
					justify-content: space-between;
					align-items: center;
					padding: 30rpx 20rpx;
					border-radius: 10rpx;
					margin-bottom: 15rpx;
					background: #f7f8fa;
					transition: all 0.3s;

					.category-name {
						font-size: 28rpx;
						color: #333;
					}

					&.selected {
						background: #f5f0ed;
						border: 2rpx solid #a89187;

						.category-name {
							color: #a89187;
							font-weight: 600;
						}
					}
				}
			}

			.modal-footer {
				display: flex;
				gap: 20rpx;

				button {
					flex: 1;
					height: 80rpx;
					border-radius: 40rpx;
					font-size: 28rpx;
					border: none;

					&::after {
						border: none;
					}
				}

				.cancel-btn {
					background: #f5f5f5;
					color: #666;
				}

				.confirm-btn {
					background: linear-gradient(135deg, #a89187, #d4c4b8);
					color: #fff;

					&.disabled {
						background: #f5f5f5;
						color: #ccc;
					}
				}
			}
		}
	}

	@keyframes slideUp {
		from {
			transform: translateY(100%);
		}

		to {
			transform: translateY(0);
		}
	}
}
</style>