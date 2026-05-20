<template>
	<view class="search">
		<!-- 导航栏 -->
		<myTitleBar title="搜索">
			<template #default>
				<uni-icons type="left" size="20" @click="goBack()"></uni-icons>
			</template>
		</myTitleBar>

		<view class="search-box">
			<!-- 搜索区 -->
			<view class="search-bar">
				<!-- 搜索框 -->
				<view class="search-item">
					<uni-icons type="search" size="20" color="#ccc"></uni-icons>
					<input @focus="showClear = true" @blur="inpValue ?showClear = true:showClear = false" v-model="inpValue"
						class="input" type="text" placeholder="请输入关键字" placeholder-style="color:#ccc">

					<!-- 清除按钮 -->
					<view @click="clearAll()" class="clear-icon" v-if="showClear">
						<uni-icons type="clear" size="22"></uni-icons>
					</view>
				</view>
				<view class="sumbit" @click="goSearch(inpValue)">搜索</view>
			</view>

			<!-- 搜索历史 -->
			<view class="search-history" v-if="historyList.length">
				<view class="top">
					<view class="title">搜索历史</view>
					<uni-icons type="trash-filled" size="22" color="#ccc" @click="clearHistoryList()"></uni-icons>
				</view>
				<view class="bottom">
					<view @click="goSearch(item.name)" class="item" v-for="item in historyList" :key="item.id">{{item.name}}
					</view>
				</view>
			</view>

			<!-- 热门搜索 -->
			<view class="search-popular">
				<view class="top">
					<view class="title">热门搜索</view>
				</view>
				<view class="bottom">
					<view @click="goSearch(item.name)" class="item" v-for="item in hotList" :key="item.id">{{item.name}}
					</view>

				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		getNaviBarHeight
	} from '@/utils/system.js'
	import myTitleBar from '@/components/myTitleBar.vue'
	import {
		ref
	} from "vue";
	import { safeNavigateBack } from '@/utils/navigation.js'

	const showClear = ref(false)
	const inpValue = ref('')
	const historyList = ref(uni.getStorageSync('search-history') || [{
		id: 1,
		name: '可爱'
	}, {
		id: 2,
		name: '卡通动漫'
	}, {
		id: 3,
		name: '真人'
	}, {
		id: 4,
		name: '美女'
	}, {
		id: 5,
		name: '卡哇伊'
	}, {
		id: 6,
		name: '聊天背景'
	}, ])

	// 热门搜索
	const hotList = ref([{
		id: 1,
		name: '可爱'
	}])

	// 去搜索
	const goSearch = (key) => {
		const history = historyList.value.find(item => item.name === key)
		if (history) {
			historyList.value = historyList.value.filter(item => item.id !== history.id)
		}

		if (key.trim()) { 
			historyList.value.unshift({
				id: Date.now(),
				name: key
			})
		}
		uni.setStorageSync('search-history', historyList.value)
		uni.navigateTo({
			url: `/pagesCategory/classify?key=${key}`
		})
	}

	// 返回上一级
	const goBack = () => {
		safeNavigateBack()
	}

	// 清除所有
	const clearAll = () => {
		console.log('清除所有');
		inpValue.value = ''
		showClear.value = false
	}

	// 清除历史记录
	const clearHistoryList = () => {
		console.log('清除历史记录');
		historyList.value = []
		uni.setStorageSync('search-history', historyList.value)
	}
</script>

<style lang="scss" scoped>
	.search {
		width: 100vw;
		height: 100vh;
		background: $self-background-color;
		color: #534747;

		.search-box {
			height: 100%;
			width: 100%;
			padding: 0 30rpx;

			// 搜索框
			.search-bar {
				width: 100%;
				padding: 20rpx 0;
				display: flex;
				align-items: center;

				.search-item {
					flex: 1;
					height: 100%;
					display: flex;
					align-items: center;
					gap: 15rpx;
					height: 70rpx;
					padding: 0 30rpx;
					border-radius: 40rpx;
					background-color: #fff;
					position: relative;


					:deep(.uni-icons) {
						font-size: 24rpx !important;
					}

					.clear-icon {
						position: absolute;
						right: 15rpx;

						:deep(.uni-icons) {
							color: #ccc !important;
							text-align: right !important;
							font-size: 32rpx !important;
						}
					}
				}

				.input {
					color: #534747;
					background-color: transparent;
					flex: 1;
					height: 100%;
					padding: 20rpx 0;
					padding-right: 35rpx;
					font-size: 20rpx;
				}

				.sumbit {
					padding: 20rpx;
					color: #534747;
					font-size: 28rpx;
				}
			}

			// 搜索历史、热门搜索
			.search-history,
			.search-popular {
				padding: 25rpx 0;
				font-size: 28rpx;

				:deep(.uni-icons) {
					color: #ccc !important;
					font-size: 32rpx !important;
				}

				.top {
					display: flex;
					justify-content: space-between;
					align-items: center;
				}

				.bottom {
					display: flex;
					flex-wrap: wrap;
					align-items: center;
					align-content: flex-start;
					padding: 20rpx 0;
					font-size: 24rpx;

					.item {
						padding: 15rpx 40rpx;
						background-color: #fcfcfc;
						margin: 10rpx;
						border-radius: 40rpx;
						color: #534747;
						position: relative;

						.clear {
							position: absolute;
							right: -10rpx;
							top: -10rpx;
						}
					}
				}
			}

		}
	}
</style>