<template>
	<view class="tab-box">
		<view class="tab-bar">
			<view class="tab-item" @click="goIndex(item, index)" v-for="(item, index) in list" :key="item.text">
				<image lazy-load class="image" :src="getIconPath(index)" mode="aspectFill"></image>
				<text class="text" :class="{active:currentIndex === index}">{{item.text}}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref, onMounted } from "vue"
	import { onShow } from '@dcloudio/uni-app'

	import shanzhu1 from '@/static/山竹1.png'
	import shanzhu2 from '@/static/山竹2.png'
	import putao1 from '@/static/葡萄1.png'
	import putao2 from '@/static/葡萄2.png'
	import huolongguo1 from '@/static/火龙果1.png'
	import huolongguo2 from '@/static/火龙果2.png'
	import pingguo1 from '@/static/苹果1.png'
	import pingguo2 from '@/static/苹果2.png'
	import ningmeng1 from '@/static/柠檬1.png'
	import ningmeng2 from '@/static/柠檬2.png'

	const icons = [
		{ normal: shanzhu1, active: shanzhu2 },
		{ normal: putao1, active: putao2 },
		{ normal: huolongguo1, active: huolongguo2 },
		{ normal: pingguo1, active: pingguo2 },
		{ normal: ningmeng1, active: ningmeng2 }
	]

	const tabList = [
		{ pagePath: "/pages/index/index", text: "首页" },
		{ pagePath: "/pages/category/index", text: "分类" },
		{ pagePath: "/pages/author/index", text: "创作者" },
		{ pagePath: "/pages/square/index", text: "广场" },
		{ pagePath: "/pages/my/index", text: "我的" }
	]

	const list = ref(tabList)
	const currentIndex = ref(0)

	const getIconPath = (index) => {
		const icon = icons[index]
		return currentIndex.value === index ? icon.active : icon.normal
	}

	const getCurrentPageIndex = () => {
		const pages = getCurrentPages()
		if (pages.length > 0) {
			const currentPage = pages[pages.length - 1]
			const route = '/' + currentPage.route
			return tabList.findIndex(item => item.pagePath === route)
		}
		return 0
	}

	onMounted(() => {
		currentIndex.value = getCurrentPageIndex()
	})

	onShow(() => {
		currentIndex.value = getCurrentPageIndex()
	})

	const goIndex = (item, index) => {
		currentIndex.value = index
		uni.switchTab({
			url: item.pagePath
		})
	}
</script>

<style scoped lang="scss">
.tab-box {
	position: fixed;
	bottom: 0;
	left: 15rpx;
	right: 15rpx;
	z-index: 999;
	padding-bottom: constant(safe-area-inset-bottom);
	padding-bottom: env(safe-area-inset-bottom);
	background: transparent;

	.tab-bar {
		display: flex;
		justify-content: space-around;
		align-items: center;
		width: 100%;
		height: 100rpx;
		padding: 10rpx 30rpx;
		background-color: #ffffff;
		border-radius: 20px;
		box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
		margin: 20rpx 0;

		.tab-item {
			color: #999999;
			font-size: 20rpx;
			display: flex;
			flex-direction: column;
			align-items: center;
			height: 100%;

			.image {
				width: 50rpx;
				height: 50rpx;
			}

			.text {
				color: #999999;
			}

			.active {
				color: #333333;
			}
		}
	}
}
</style>
