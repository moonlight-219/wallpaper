<template>
	<view class="title">
		<view class="left">
			<slot name="left"></slot>
		</view>
		<view class="right">
			<slot name="right"></slot>
		</view>
	</view>
</template>

<script setup>

</script>

<style lang="scss" scoped>
	.title {
		width: 100%;
		display: flex;
		justify-content: space-between;
		align-items: center;
		font-size: 40rpx;
		padding: 25rpx 0;

		.left {
			color: $self-text-color-1;
			font-weight: 700;
		}

		.right {
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 28rpx;
			width: 100rpx;
			height: 50rpx;
			color: #bbb;
			border-radius: 10px;
			font-weight: 300;
		}
	}
</style>