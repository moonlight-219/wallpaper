<template>
	<view class="image-skeleton" :class="{ loaded: imageLoaded }" @click="handleClick">
		<!-- 骨架屏 -->
		<view v-if="!imageLoaded && !loadError" class="skeleton-box" :style="skeletonStyle">
			<!-- 多层渐变背景 -->
			<view class="skeleton-bg">
				<view class="bg-layer layer-1"></view>
				<view class="bg-layer layer-2"></view>
				<view class="bg-layer layer-3"></view>
			</view>
			
			<!-- 波浪效果 -->
			<view class="skeleton-wave">
				<view class="wave wave-1"></view>
				<view class="wave wave-2"></view>
				<view class="wave wave-3"></view>
			</view>
			
			<!-- 光晕扫过效果 -->
			<view class="skeleton-shimmer"></view>
			
			<!-- 中心装饰 -->
			<view class="skeleton-center">
				<view class="center-icon">
					<uni-icons type="image" size="36" color="rgba(122, 84, 77, 0.2)"></uni-icons>
				</view>
				<view class="loading-dots">
					<view class="dot dot-1"></view>
					<view class="dot dot-2"></view>
					<view class="dot dot-3"></view>
				</view>
			</view>
		</view>
		
		<!-- 实际图片 -->
		<image 
			:src="src" 
			:mode="mode"
			:lazy-load="lazyLoad"
			:class="imageClass"
			:style="imageStyle"
			@load="onImageLoad"
			@error="onImageError"
		></image>
		
		<!-- 加载失败提示 -->
		<view v-if="loadError" class="error-placeholder" :style="skeletonStyle">
			<view class="error-content">
				<view class="error-icon-wrapper">
					<uni-icons type="image-filled" size="52" color="#d4b5a8"></uni-icons>
				</view>
				<text class="error-text">图片加载失败</text>
				<text class="error-hint">请检查网络连接</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref, computed } from 'vue'
	
	const emit = defineEmits(['click'])
	
	const props = defineProps({
		src: {
			type: String,
			required: true
		},
		mode: {
			type: String,
			default: 'aspectFill'
		},
		width: {
			type: String,
			default: '100%'
		},
		height: {
			type: String,
			default: '400rpx'
		},
		borderRadius: {
			type: String,
			default: '16rpx'
		},
		lazyLoad: {
			type: Boolean,
			default: true
		},
		imageClass: {
			type: String,
			default: ''
		}
	})
	
	const imageLoaded = ref(false)
	const loadError = ref(false)
	
	const skeletonStyle = computed(() => ({
		width: props.width,
		height: props.height,
		borderRadius: props.borderRadius
	}))
	
	const imageStyle = computed(() => ({
		width: props.width,
		height: props.height,
		borderRadius: props.borderRadius
	}))
	
	const onImageLoad = () => {
		imageLoaded.value = true
		loadError.value = false
	}
	
	const onImageError = () => {
		imageLoaded.value = true
		loadError.value = true
	}
	
	const handleClick = (e) => {
		emit('click', e)
	}
</script>

<style lang="scss" scoped>
	.image-skeleton {
		position: relative;
		display: block;
		width: 100%;
		height: 100%;
		overflow: hidden;
		
		image {
			display: block;
			width: 100%;
			height: 100%;
			opacity: 0;
			transform: scale(1.08);
			filter: blur(4rpx);
			transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
		}
		
		&.loaded {
			image {
				opacity: 1;
				transform: scale(1);
				filter: blur(0);
			}
		}
		
		.skeleton-box {
			position: absolute;
			top: 0;
			left: 0;
			width: 100%;
			height: 100%;
			overflow: hidden;
			
			// 多层渐变背景
			.skeleton-bg {
				position: absolute;
				top: 0;
				left: 0;
				width: 100%;
				height: 100%;
				
				.bg-layer {
					position: absolute;
					top: 0;
					left: 0;
					width: 100%;
					height: 100%;
				}
				
				.layer-1 {
					background: linear-gradient(135deg, #fcfbfa 0%, #f9f7f6 100%);
					animation: bgPulse 3s ease-in-out infinite;
				}
				
				.layer-2 {
					background: radial-gradient(circle at 30% 30%, rgba(250, 245, 242, 0.3) 0%, transparent 70%);
					animation: bgMove1 4s ease-in-out infinite;
				}
				
				.layer-3 {
					background: radial-gradient(circle at 70% 70%, rgba(252, 248, 245, 0.4) 0%, transparent 60%);
					animation: bgMove2 5s ease-in-out infinite reverse;
				}
			}
			
			// 波浪效果
			.skeleton-wave {
				position: absolute;
				top: 0;
				left: 0;
				width: 100%;
				height: 100%;
				
				.wave {
					position: absolute;
					width: 200%;
					height: 200%;
					top: -50%;
					left: -50%;
					background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
					border-radius: 40%;
				}
				
				.wave-1 {
					animation: wave 8s ease-in-out infinite;
				}
				
				.wave-2 {
					animation: wave 10s ease-in-out infinite reverse;
					animation-delay: -2s;
				}
				
				.wave-3 {
					animation: wave 12s ease-in-out infinite;
					animation-delay: -4s;
				}
			}
			
			// 光晕扫过效果
			.skeleton-shimmer {
				position: absolute;
				top: -100%;
				left: -100%;
				width: 200%;
				height: 200%;
				background: linear-gradient(
					120deg,
					transparent 0%,
					transparent 35%,
					rgba(255, 255, 255, 0.4) 45%,
					rgba(255, 255, 255, 0.7) 50%,
					rgba(255, 255, 255, 0.4) 55%,
					transparent 65%,
					transparent 100%
				);
				animation: shimmer 3s ease-in-out infinite;
				transform: rotate(25deg);
			}
			
			// 中心装饰
			.skeleton-center {
				position: absolute;
				top: 0;
				left: 0;
				width: 100%;
				height: 100%;
				display: flex;
				flex-direction: column;
				align-items: center;
				justify-content: center;
				gap: 20rpx;
				
				.center-icon {
					animation: iconFloat 2.5s ease-in-out infinite;
				}
				
				.loading-dots {
					display: flex;
					gap: 12rpx;
					
					.dot {
						width: 8rpx;
						height: 8rpx;
						border-radius: 50%;
						background: rgba(122, 84, 77, 0.25);
					}
					
					.dot-1 {
						animation: dotBounce 1.4s ease-in-out infinite;
					}
					
					.dot-2 {
						animation: dotBounce 1.4s ease-in-out infinite;
						animation-delay: 0.2s;
					}
					
					.dot-3 {
						animation: dotBounce 1.4s ease-in-out infinite;
						animation-delay: 0.4s;
					}
				}
			}
		}
		
		.error-placeholder {
			position: absolute;
			top: 0;
			left: 0;
			width: 100%;
			height: 100%;
			display: flex;
			align-items: center;
			justify-content: center;
			background: linear-gradient(135deg, #fcf9f7 0%, #f7f3f0 100%);
			
			.error-content {
				display: flex;
				flex-direction: column;
				align-items: center;
				gap: 16rpx;
				animation: errorFadeIn 0.4s ease;
				
				.error-icon-wrapper {
					animation: errorShake 0.5s ease;
				}
				
				.error-text {
					font-size: 24rpx;
					color: #b89a8d;
					font-weight: 500;
					letter-spacing: 0.5rpx;
				}
				
				.error-hint {
					font-size: 20rpx;
					color: #d4b5a8;
					letter-spacing: 0.3rpx;
				}
			}
		}
		
		// 背景脉冲动画
		@keyframes bgPulse {
			0%, 100% {
				opacity: 1;
			}
			50% {
				opacity: 0.85;
			}
		}
		
		// 背景移动动画1
		@keyframes bgMove1 {
			0%, 100% {
				transform: translate(0, 0) scale(1);
			}
			50% {
				transform: translate(10%, 10%) scale(1.1);
			}
		}
		
		// 背景移动动画2
		@keyframes bgMove2 {
			0%, 100% {
				transform: translate(0, 0) scale(1);
			}
			50% {
				transform: translate(-8%, -8%) scale(1.08);
			}
		}
		
		// 波浪动画
		@keyframes wave {
			0%, 100% {
				transform: translate(0, 0) rotate(0deg);
			}
			25% {
				transform: translate(5%, 5%) rotate(5deg);
			}
			50% {
				transform: translate(0, 10%) rotate(0deg);
			}
			75% {
				transform: translate(-5%, 5%) rotate(-5deg);
			}
		}
		
		// 光晕扫过动画
		@keyframes shimmer {
			0% {
				transform: translateX(-100%) translateY(-100%) rotate(25deg);
			}
			100% {
				transform: translateX(100%) translateY(100%) rotate(25deg);
			}
		}
		
		// 图标浮动动画
		@keyframes iconFloat {
			0%, 100% {
				transform: translateY(0) scale(1);
				opacity: 0.4;
			}
			50% {
				transform: translateY(-8rpx) scale(1.05);
				opacity: 0.6;
			}
		}
		
		// 点跳动动画
		@keyframes dotBounce {
			0%, 80%, 100% {
				transform: scale(1);
				opacity: 0.3;
			}
			40% {
				transform: scale(1.3);
				opacity: 0.8;
			}
		}
		
		// 错误淡入动画
		@keyframes errorFadeIn {
			from {
				opacity: 0;
				transform: scale(0.9) translateY(10rpx);
			}
			to {
				opacity: 1;
				transform: scale(1) translateY(0);
			}
		}
		
		// 错误抖动动画
		@keyframes errorShake {
			0%, 100% {
				transform: translateX(0);
			}
			25% {
				transform: translateX(-4rpx);
			}
			75% {
				transform: translateX(4rpx);
			}
		}
	}
</style>
