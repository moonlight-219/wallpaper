<template>
	<view class="themeItem-list">
		<view @click="goClassifyPage(item)" class="themeItem" v-for="item in items" :key="item.id">
			<image lazy-load class="themeItem-pic" :src="item.url" mode="aspectFill"></image>
			<view class="themeItem-top">
				<view class="date">{{ item.updateTime || '2天前更新' }}</view>
				<view class="theme-name">{{ item.name || '动漫次元' }}</view>
			</view>
		</view>
	</view>
</template>

<script setup>
defineProps({
	items: {
		type: Array,
		required: true
	}
})

const goClassifyPage = (item) => {
	uni.navigateTo({
		url: `/pagesCategory/classify?id=${item.id}&name=${item.name || ''}`
	})
}
</script>

<style lang="scss" scoped>
.themeItem-list {
	display: grid;
	gap: 15rpx;
	grid-template-columns: repeat(3, 1fr);
	
	.themeItem {
		background: #fff;
		position: relative;
		width: 100%;
		height: 320rpx;
		overflow: hidden;
		border-radius: 16rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
		transition: all 0.2s ease;

		&:active {
			transform: scale(0.96);
			box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.1);
		}

		.themeItem-pic {
			width: 100%;
			height: 100%;
			object-fit: cover;
		}

		.themeItem-top {
			overflow: hidden;
			position: absolute;
			top: 0;
			left: 0;
			right: 0;
			bottom: 0;
			align-items: center;
			background: linear-gradient(180deg, rgba(0, 0, 0, 0.02) 0%, rgba(0, 0, 0, 0.08) 100%);

			.date {
				position: absolute;
				left: -10rpx;
				top: -25rpx;
				width: 160rpx;
				font-size: 22rpx;
				color: #fff;
				border-radius: 15px;
				background: linear-gradient(135deg, #e8b9b3 0%, #d9a9a3 100%);
				padding: 30rpx 5rpx 8rpx 20rpx;
				overflow: hidden;
				text-overflow: ellipsis;
				white-space: nowrap;
				box-shadow: 0 2rpx 8rpx rgba(226, 185, 179, 0.4);
				font-weight: 500;
			}

			.theme-name {
				position: absolute;
				bottom: 0;
				left: 0;
				right: 0;
				background: linear-gradient(180deg, transparent 0%, rgba(116, 112, 129, 0.95) 100%);
				color: #fff;
				display: flex;
				justify-content: center;
				align-items: center;
				height: 80rpx;
				font-size: 28rpx;
				font-weight: 600;
			}
		}
	}
}
</style>