<template>
	<view>
		<view class="myTitleBar">
			<view class="navbar">
				<view class="statusbar" :style="{height:getStatusBarHeight() + 'px'}"></view>
				<view class="titlebar" :style="{height:getTitleBarHeight() + 'px'}">
					<!-- 返回按钮 -->
					<view v-if="showBack" class="back-btn" @click="goBack">
						<uni-icons type="left" size="20" color="#534747"></uni-icons>
					</view>

					<!-- 默认插槽（logo和搜索框） -->
					<slot v-if="!hideSearch">
						<image class="logo" src="/src/static/666成功.png1.gif" mode="aspectFill"></image>
						<div class="search">
							<uni-icons type="search" color="#ccc"></uni-icons>
							<input @click="goSearch()" class="input" type="text" placeholder="搜索" placeholder-style="color:#ccc;">
						</div>
					</slot>

					<!-- 标题 -->
					<view v-if="title" class="title" :class="{ 'has-back': showBack }">
						<view>{{title}}</view>
					</view>
				</view>

			</view>
		</view>
		<!-- 状态栏的高度加上标题栏的高度 -->
		<view class="fill" :style="{height:getNaviBarHeight() + 'px'}"></view>
	</view>
</template>

<script setup>
	import {
		computed,
		ref
	} from "vue"
	import {
		getStatusBarHeight,
		getTitleBarHeight,
		getNaviBarHeight
	} from '@/utils/system.js'

	const props = defineProps({
		title: {
			type: String,
			default: ''
		},
		showBack: {
			type: Boolean,
			default: false
		},
		hideSearch: {
			type: Boolean,
			default: false
		}
	})

	const goBack = () => {
		uni.navigateBack({
			fail: () => {
				uni.switchTab({ url: '/pages/index/index' })
			}
		})
	}

	// 去搜索页
	const goSearch = () => {
		uni.navigateTo({
			url: '/pages/search/index'
		})
	}
</script>

<style lang="scss" scoped>
	.myTitleBar {
		width: 100%;
		background: linear-gradient(145deg, #fbf0ef, #fff);
		position: fixed;
		z-index: 10;

		.navbar {

			.titlebar {
				padding: 0 20rpx;
				width: 100%;
				display: flex;
				align-items: center;

				.back-btn {
					width: 60rpx;
					height: 60rpx;
					display: flex;
					align-items: center;
					justify-content: center;
					margin-right: 10rpx;

					&:active {
						opacity: 0.6;
					}
				}

				.title {
					width: 100%;
					text-align: center;
					font-size: 28rpx;

					&.has-back {
						padding-right: 70rpx;
					}
				}

				.logo {
					width: 160rpx;
					height: 80rpx;
					object-fit: cover;
				}

				.search {
					display: flex;
					align-items: center;
					margin-left: 15rpx;
					background-color: rgba(255, 255, 255, .9);
					padding: 10rpx 15rpx;
					border-radius: 20px;

					.input {
						padding-left: 15rpx;
						width: 200rpx;
						background-color: transparent;
					}
				}
			}


		}
	}
</style>