<template>
	<view class="cropper-modal" @click="handleClose">
		<view class="cropper-container" @click.stop>
			<view class="cropper-header">
				<text class="cropper-title">裁剪图片</text>
				<view class="close-btn" @click="handleClose">
					<uni-icons type="close" size="20" color="#666"></uni-icons>
				</view>
			</view>

			<view class="cropper-content">
				<!-- 裁剪提示 -->
				<view class="cropper-tip">
					<uni-icons type="info" size="14" color="#ff9800"></uni-icons>
					<text class="tip-text">拖动裁剪框调整区域，比例已锁定为 {{ targetWidth }}:{{ targetHeight }}</text>
				</view>

				<!-- 裁剪预览区 -->
				<view class="cropper-preview" :style="{ height: previewHeight + 'px' }">
					<!-- 固定的图片 -->
					<view class="image-container" :style="{ width: previewWidth + 'px', height: previewHeight + 'px' }">
						<image 
							class="crop-image" 
							:src="imageUrl" 
							:style="{
								width: imageDisplayWidth + 'px',
								height: imageDisplayHeight + 'px',
								left: imageLeft + 'px',
								top: imageTop + 'px'
							}"
							lazy-load="false"
							show-menu-by-longpress="false"
						></image>
						
						<!-- 可拖动的裁剪蒙版 -->
						<view 
							class="crop-mask-wrapper"
							:style="{ 
								width: imageDisplayWidth + 'px', 
								height: imageDisplayHeight + 'px', 
								left: imageLeft + 'px', 
								top: imageTop + 'px' 
							}"
							@touchstart="handleTouchStart"
							@touchmove="handleTouchMove"
							@touchend="handleTouchEnd"
						>
							<view 
								class="crop-mask"
								:style="{
									width: cropFrameWidth + 'px',
									height: cropFrameHeight + 'px',
									left: cropMaskX + 'px',
									top: cropMaskY + 'px'
								}"
							>
								<view class="frame-border"></view>
								<view class="frame-corner top-left"></view>
								<view class="frame-corner top-right"></view>
								<view class="frame-corner bottom-left"></view>
								<view class="frame-corner bottom-right"></view>
							</view>
						</view>
					</view>
					
					<!-- 尺寸标注 -->
					<view class="size-label">
						<text>{{ targetWidth }}:{{ targetHeight }}</text>
					</view>
				</view>

				<!-- 操作提示 -->
				<view class="cropper-hint">
					<uni-icons type="hand-up" size="16" color="#666"></uni-icons>
					<text class="hint-text">拖动裁剪框调整裁剪区域</text>
				</view>
			</view>

			<view class="cropper-actions">
				<button class="cancel-btn" @click="handleClose">取消</button>
				<button class="confirm-btn" @click="handleConfirm">确认裁剪</button>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
	imageUrl: {
		type: String,
		required: true
	},
	imageInfo: {
		type: Object,
		required: true
	},
	targetWidth: {
		type: Number,
		required: true
	},
	targetHeight: {
		type: Number,
		required: true
	},
	categoryType: {
		type: Number,
		default: 1
	}
})

const emit = defineEmits(['close', 'confirm'])

// 预览区域尺寸
const previewWidth = ref(0)
const previewHeight = ref(0)

// 图片显示尺寸（适配预览区）
const imageDisplayWidth = ref(0)
const imageDisplayHeight = ref(0)
// 图片在预览区的位置（居中）
const imageLeft = ref(0)
const imageTop = ref(0)

// 裁剪蒙版尺寸（最大合规范围）
const cropFrameWidth = ref(0)
const cropFrameHeight = ref(0)
// 裁剪蒙版位置（初始居中）
const cropMaskX = ref(0)
const cropMaskY = ref(0)
// 裁剪蒙版移动边界
const maskMinX = ref(0)
const maskMaxX = ref(0)
const maskMinY = ref(0)
const maskMaxY = ref(0)

// 原图中的最大裁剪范围（像素）
const maxCropWidth = ref(0)
const maxCropHeight = ref(0)

// 显示比例（原图 -> 显示图）
const displayScale = ref(1)

// 触摸相关变量
let touchStartX = 0
let touchStartY = 0
let maskStartX = 0
let maskStartY = 0
let isTouching = false

onMounted(() => {
	initCropper()
})

onUnmounted(() => {
	// 清理
})

/**
 * 计算原图中符合目标比例的最大裁剪范围（保留原有规则）
 */
const calculateMaxCropSize = () => {
	const originalWidth = props.imageInfo.width
	const originalHeight = props.imageInfo.height
	const targetRatio = props.targetWidth / props.targetHeight
	
	// 平板分类（type=2）且宽度大于高度时，以宽度的80%为基准
	if (props.categoryType === 2 && originalWidth > originalHeight) {
		const baseWidth = originalWidth * 0.8
		maxCropWidth.value = baseWidth
		maxCropHeight.value = Math.min(baseWidth / targetRatio, originalHeight)
	} else if (originalWidth > originalHeight) {
		// 宽 > 高：以高度为基准
		maxCropHeight.value = originalHeight
		maxCropWidth.value = Math.min(originalHeight * targetRatio, originalWidth)
	} else if (originalHeight > originalWidth) {
		// 高 > 宽：以宽度为基准
		maxCropWidth.value = originalWidth
		maxCropHeight.value = Math.min(originalWidth / targetRatio, originalHeight)
	} else {
		// 正方形：同样按目标比例计算最大裁剪范围
		if (targetRatio >= 1) {
			// 目标比例宽 >= 高（如 16:9、1:1），以宽度为基准
			maxCropWidth.value = originalWidth
			maxCropHeight.value = Math.min(originalWidth / targetRatio, originalHeight)
		} else {
			// 目标比例高 > 宽（如 9:20、16:21），以高度为基准
			maxCropHeight.value = originalHeight
			maxCropWidth.value = Math.min(originalHeight * targetRatio, originalWidth)
		}
	}
}

const initCropper = () => {
	const systemInfo = uni.getSystemInfoSync()
	const screenWidth = systemInfo.windowWidth
	
	// 1. 计算原图最大裁剪范围
	calculateMaxCropSize()
	
	// 2. 预览区域尺寸
	const containerWidth = screenWidth - 60 // 左右各30px padding
	previewWidth.value = containerWidth
	previewHeight.value = containerWidth * 1.2 // 预览区高度
	
	// 3. 计算图片显示尺寸（适配预览区，保持原图比例）
	// 让图片完整显示在预览区，且尽可能大
	const fitScale = Math.min(
		previewWidth.value / props.imageInfo.width,
		previewHeight.value / props.imageInfo.height
	)
	imageDisplayWidth.value = props.imageInfo.width * fitScale
	imageDisplayHeight.value = props.imageInfo.height * fitScale
	// 图片居中
	imageLeft.value = (previewWidth.value - imageDisplayWidth.value) / 2
	imageTop.value = (previewHeight.value - imageDisplayHeight.value) / 2
	
	// 4. 计算显示比例（原图 -> 显示图）
	displayScale.value = imageDisplayWidth.value / props.imageInfo.width
	
	// 5. 计算裁剪蒙版尺寸（最大裁剪范围的显示尺寸）
	cropFrameWidth.value = maxCropWidth.value * displayScale.value
	cropFrameHeight.value = maxCropHeight.value * displayScale.value
	
	// 6. 计算裁剪蒙版移动边界（不能超出图片）
	maskMinX.value = 0
	maskMaxX.value = Math.max(0, imageDisplayWidth.value - cropFrameWidth.value)
	maskMinY.value = 0
	maskMaxY.value = Math.max(0, imageDisplayHeight.value - cropFrameHeight.value)
	
	// 7. 裁剪蒙版初始位置（居中）
	cropMaskX.value = maskMaxX.value / 2
	cropMaskY.value = maskMaxY.value / 2
	
	console.log('裁剪区初始化:', {
		容器尺寸: { width: previewWidth.value, height: previewHeight.value },
		图片显示尺寸: { width: imageDisplayWidth.value, height: imageDisplayHeight.value },
		图片位置: { left: imageLeft.value, top: imageTop.value },
		裁剪框尺寸: { width: cropFrameWidth.value, height: cropFrameHeight.value },
		移动边界: { minX: maskMinX.value, maxX: maskMaxX.value, minY: maskMinY.value, maxY: maskMaxY.value },
		初始位置: { x: cropMaskX.value, y: cropMaskY.value }
	})
}

/**
 * 触摸开始
 */
const handleTouchStart = (e) => {
	if (e.touches.length !== 1) return
	
	isTouching = true
	const touch = e.touches[0]
	touchStartX = touch.clientX
	touchStartY = touch.clientY
	maskStartX = cropMaskX.value
	maskStartY = cropMaskY.value
}

/**
 * 触摸移动
 */
const handleTouchMove = (e) => {
	if (!isTouching || e.touches.length !== 1) return
	
	e.preventDefault()
	e.stopPropagation()
	
	const touch = e.touches[0]
	const deltaX = touch.clientX - touchStartX
	const deltaY = touch.clientY - touchStartY
	
	// 计算新位置
	let newX = maskStartX + deltaX
	let newY = maskStartY + deltaY
	
	// 限制在边界内
	newX = Math.max(maskMinX.value, Math.min(maskMaxX.value, newX))
	newY = Math.max(maskMinY.value, Math.min(maskMaxY.value, newY))
	
	// 更新位置
	cropMaskX.value = newX
	cropMaskY.value = newY
}

/**
 * 触摸结束
 */
const handleTouchEnd = () => {
	isTouching = false
}

const handleClose = () => {
	emit('close')
}

const handleConfirm = () => {
	// cropMaskX 和 cropMaskY 是相对于 movable-area 的坐标
	// movable-area 已经对齐到图片，所以这就是裁剪框在显示图片中的位置
	const maskXInDisplay = cropMaskX.value
	const maskYInDisplay = cropMaskY.value
	
	// 转换为原图中的裁剪坐标（通过显示比例换算）
	const cropX = maskXInDisplay / displayScale.value
	const cropY = maskYInDisplay / displayScale.value
	const cropWidth = cropFrameWidth.value / displayScale.value
	const cropHeight = cropFrameHeight.value / displayScale.value
	
	// 取整并确保在原图范围内
	const finalX = Math.round(Math.max(0, Math.min(cropX, props.imageInfo.width - cropWidth)))
	const finalY = Math.round(Math.max(0, Math.min(cropY, props.imageInfo.height - cropHeight)))
	const finalWidth = Math.round(Math.min(cropWidth, maxCropWidth.value))
	const finalHeight = Math.round(Math.min(cropHeight, maxCropHeight.value))
	
	console.log('裁剪参数调试:', {
		显示尺寸: { width: imageDisplayWidth.value, height: imageDisplayHeight.value },
		蒙版位置: { x: maskXInDisplay, y: maskYInDisplay },
		蒙版尺寸: { width: cropFrameWidth.value, height: cropFrameHeight.value },
		显示比例: displayScale.value,
		原图尺寸: { width: props.imageInfo.width, height: props.imageInfo.height },
		裁剪坐标: { x: finalX, y: finalY, width: finalWidth, height: finalHeight }
	})
	
	emit('confirm', {
		x: finalX,
		y: finalY,
		width: finalWidth,
		height: finalHeight
	})
}
</script>

<style lang="scss" scoped>
.cropper-modal {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.8);
	z-index: 999;
	display: flex;
	align-items: center;
	justify-content: center;
	/* 硬件加速，减少整体卡顿 */
	transform: translateZ(0);
	will-change: transform;

	.cropper-container {
		width: 100%;
		max-width: 650rpx;
		background: #fff;
		border-radius: 20rpx;
		overflow: hidden;
		margin: 0 30rpx;

		.cropper-header {
			display: flex;
			justify-content: space-between;
			align-items: center;
			padding: 30rpx;
			border-bottom: 1rpx solid #f0f0f0;

			.cropper-title {
				font-size: 32rpx;
				font-weight: 600;
				color: #333;
			}

			.close-btn {
				width: 50rpx;
				height: 50rpx;
				display: flex;
				align-items: center;
				justify-content: center;
			}
		}

		.cropper-content {
			padding: 30rpx;

			.cropper-tip {
				display: flex;
				align-items: center;
				padding: 20rpx;
				background: #fff7e6;
				border-radius: 10rpx;
				margin-bottom: 30rpx;

				.tip-text {
					font-size: 24rpx;
					color: #ff9800;
					margin-left: 10rpx;
					flex: 1;
				}
			}

			.cropper-preview {
				position: relative;
				width: 100%;
				background: #000;
				border-radius: 10rpx;
				overflow: hidden;
				margin-bottom: 20rpx;

				.image-container {
					position: relative;
					width: 100%;
					height: 100%;
					
					.crop-image {
						position: absolute;
						display: block;
						z-index: 1;
						transform: translateZ(0);
						will-change: transform;
						backface-visibility: hidden;
					}

					.crop-mask-wrapper {
						position: absolute;
						z-index: 2;
						touch-action: none;
						user-select: none;
					}

					.crop-mask {
						position: absolute;
						z-index: 3;
						box-shadow: 0 0 0 9999rpx rgba(0, 0, 0, 0.5);
						pointer-events: auto;
						transform: translate3d(0, 0, 0);
						will-change: transform;
						backface-visibility: hidden;
						touch-action: none;
						user-select: none;
						-webkit-user-drag: none;
						cursor: move;

						.frame-border {
							position: absolute;
							top: 0;
							left: 0;
							right: 0;
							bottom: 0;
							border: 3rpx solid #fff;
							border-radius: 2rpx;
							
							&::before,
							&::after {
								content: '';
								position: absolute;
								background: rgba(255, 255, 255, 0.3);
							}
							
							&::before {
								left: 33.33%;
								top: 0;
								bottom: 0;
								width: 1rpx;
								box-shadow: 100rpx 0 0 rgba(255, 255, 255, 0.3);
							}
							
							&::after {
								top: 33.33%;
								left: 0;
								right: 0;
								height: 1rpx;
								box-shadow: 0 100rpx 0 rgba(255, 255, 255, 0.3);
							}
						}

						.frame-corner {
							position: absolute;
							width: 30rpx;
							height: 30rpx;
							border: 4rpx solid #a89187;
							background: #fff;
							z-index: 4;

							&.top-left {
								top: -15rpx;
								left: -15rpx;
								border-right: none;
								border-bottom: none;
							}

							&.top-right {
								top: -15rpx;
								right: -15rpx;
								border-left: none;
								border-bottom: none;
							}

							&.bottom-left {
								bottom: -15rpx;
								left: -15rpx;
								border-right: none;
								border-top: none;
							}

							&.bottom-right {
								bottom: -15rpx;
								right: -15rpx;
								border-left: none;
								border-top: none;
							}
						}
					}
				}

				.size-label {
					position: absolute;
					bottom: 20rpx;
					left: 50%;
					transform: translateX(-50%);
					padding: 10rpx 20rpx;
					background: rgba(0, 0, 0, 0.7);
					border-radius: 20rpx;
					z-index: 11;

					text {
						font-size: 22rpx;
						color: #fff;
					}
				}
			}

			.cropper-hint {
				display: flex;
				align-items: center;
				justify-content: center;
				padding: 15rpx;
				background: #f7f8fa;
				border-radius: 10rpx;

				.hint-text {
					font-size: 24rpx;
					color: #666;
					margin-left: 10rpx;
				}
			}
		}

		.cropper-actions {
			display: flex;
			gap: 20rpx;
			padding: 30rpx;
			border-top: 1rpx solid #f0f0f0;

			button {
				flex: 1;
				height: 80rpx;
				border-radius: 35rpx;
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
			}
		}
	}
}
</style>