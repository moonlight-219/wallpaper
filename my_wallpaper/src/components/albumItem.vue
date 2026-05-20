<template>
	<view class="albumItem" v-if="album" @click="handleClick(album.id)">
		<image lazy-load class="swiper-pic" :src="album.url" mode="aspectFill"></image>
		<view class="pic-top">
			<view class="top">
				<view class="title">
					{{ album.name }}
				</view>
				<view class="num"> {{ album.wallpaperCount }}</view>
			</view>
		</view>
	</view>
</template>

<script setup>
const props = defineProps({
	album: {
		type: Object,
		required: true
	},
	clickable: {
		type: Boolean,
		default: true
	},
	count: {
		type: Number,
		default: 0
	}
})

const handleClick = (id) => {
	if (!props.clickable) {
		return
	}
	uni.navigateTo({
		url: `/pagesAlbum/album-detail?id=${id}`
	})
}
</script>

<style lang="scss" scoped>
.albumItem {
	height: 100%;
	width: 100%;
	position: relative;
	border-radius: 20rpx;
	overflow: hidden;
	background-color: #fff;
	box-shadow: 0 8rpx 16rpx rgba(0, 0, 0, 0.15);

	.swiper-pic {
		width: 100%;
		height: 100%;
		display: block;
	}

	// 顶层标题、数量、喜欢
	.pic-top {
		position: absolute;
		padding: 40rpx;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		color: #444;

		.top {
			display: flex;
			justify-content: space-between;
			align-items: center;

			.title {
				background-color: rgba(255, 255, 255, .5);
				font-size: 60rpx;
				font-weight: 700;
				padding: 0 5rpx;
			}

			.num {
				background-color: rgba(255, 255, 255, .6);
				padding: 10rpx;
				border-radius: 10px;
				font-size: 24rpx;
			}
		}

		.bottom {
			display: flex;
			align-items: center;

			.like {
				background-color: rgba(255, 255, 255, .6);
				padding: 10rpx 15rpx;
				border-radius: 15px;
				font-size: 20rpx;
			}
		}
	}
}
</style>