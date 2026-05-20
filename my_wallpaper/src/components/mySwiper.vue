<template>
	<view class="my-swiper">
		<swiper class="swiper-container" @change="handleChange" :autoplay="true" :interval="3000" :duration="1000" circular>
			<swiper-item class="swiper-item" v-for="item in items" :key="item.id">
				<albumItem :album="item" :count="items.length"></albumItem>
			</swiper-item>
		</swiper>

		<view class="indicator-line">
			<view :class="{ active: current === index }" class="line" v-for="(item, index) in 5" :key="item"></view>
		</view>
	</view>
</template>

<script setup>
import {
	ref
} from "vue"
import albumItem from '@/components/albumItem.vue'

const props = defineProps({
	items: {
		type: Array,
		required: true
	}
})

const current = ref(0)
const handleChange = (e) => {
	current.value = e.detail.current % 5
}
</script>

<style lang="scss" scoped>
// 轮播图
.my-swiper {
	width: 100%;
	height: 100%;
	position: relative;
	border: 2rpx solid #f0f0f0;
	border-radius: 20rpx;
	overflow: hidden;
	background-color: #fff;

	.swiper-container {
		width: 100%;
		height: 100%;

		.swiper-item {
			width: 100%;
			height: 100%;
			border-radius: 20rpx;
			overflow: hidden;
		}

		.swiper-item:last-child {
			margin-right: 0;
		}
	}

	// 指示器
	.indicator-line {
		width: 100%;
		height: 6rpx;
		position: absolute;
		bottom: 20rpx;
		display: flex;
		align-items: center;
		justify-content: center;

		.line {
			margin: 0 5rpx;
			height: 100%;
			width: 15rpx;
			background-color: rgba(255, 255, 255, .3);
			border-radius: 10rpx;
			transition: all .3s;

			&.active {
				width: 35rpx;
				background-color: #fff;
			}
		}
	}

}
</style>