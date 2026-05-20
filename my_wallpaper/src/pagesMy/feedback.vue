<template>
	<view class="feedback">
		<view class="content">
			<view class="icon-wrap">
				<uni-icons type="chat-filled" size="60" color="#e2b9b3"></uni-icons>
			</view>
			
			<view class="title">意见反馈</view>
			
			<view class="message">
				<text class="text">有任何问题可发送信息到邮箱</text>
				<text class="email">3336292719@qq.com</text>
				<text class="text">我们将尽快回复，感谢您的宝贵意见</text>
			</view>
			
			<view class="btn-group">
				<button class="copy-btn" @click="copyEmail">复制邮箱</button>
				<button class="back-btn" @click="goBack">返回</button>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { safeNavigateBack } from '@/utils/navigation.js'
	
	// 复制邮箱
	const copyEmail = () => {
		uni.setClipboardData({
			data: '3336292719@qq.com',
			success: () => {
				uni.showToast({
					title: '邮箱已复制',
					icon: 'success'
				})
			}
		})
	}
	
	// 返回
	const goBack = () => {
		safeNavigateBack()
	}
</script>

<style lang="scss" scoped>
	.feedback {
		min-height: 100vh;
		width: 100vw;
		background: $self-background-color;
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 60rpx 30rpx;
		
		.content {
			background: #fff;
			border-radius: 30rpx;
			padding: 80rpx 50rpx;
			width: 100%;
			box-shadow: 0 8rpx 30rpx rgba(122, 84, 77, 0.1);
			display: flex;
			flex-direction: column;
			align-items: center;
			
			.icon-wrap {
				width: 140rpx;
				height: 140rpx;
				background: rgba(226, 185, 179, 0.15);
				border-radius: 50%;
				display: flex;
				align-items: center;
				justify-content: center;
				margin-bottom: 40rpx;
			}
			
			.title {
				font-size: 40rpx;
				font-weight: 600;
				color: #613942;
				margin-bottom: 50rpx;
			}
			
			.message {
				text-align: center;
				line-height: 50rpx;
				margin-bottom: 60rpx;
				
				.text {
					font-size: 28rpx;
					color: #666;
					display: block;
				}
				
				.email {
					font-size: 32rpx;
					color: #7a544d;
					font-weight: 600;
					display: block;
					margin: 20rpx 0;
					word-break: break-all;
				}
			}
			
			.btn-group {
				width: 100%;
				display: flex;
				gap: 20rpx;
				
				button {
					flex: 1;
					height: 88rpx;
					border-radius: 20rpx;
					font-size: 30rpx;
					border: none;
					
					&::after {
						border: none;
					}
				}
				
				.copy-btn {
					background: #7a544d;
					color: #fff;
				}
				
				.back-btn {
					background: #f5f5f5;
					color: #666;
				}
			}
		}
	}
</style>
