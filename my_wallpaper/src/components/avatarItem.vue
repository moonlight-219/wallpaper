<template>
	<view class="avatar-grid">
		<view class="avatar-item" v-for="(item, index) in items" :key="item.id" @click="goPreview(item, index)">
			<imageSkeleton :src="item.thumbnailUrl || item.url" mode="aspectFill" width="100%" height="100%"
				border-radius="20rpx" image-class="avatar-img" />
		</view>
	</view>
</template>

<script setup>
import imageSkeleton from '@/components/imageSkeleton.vue'
import { setPreviewList } from '@/store/previewStore.js'

const props = defineProps({
	items: {
		type: Array,
		default: () => []
	}
})

const goPreview = (item) => {
	if (item && item.id) {
		// 将完整壁纸列表存入全局变量
		setPreviewList(props.items)
		uni.navigateTo({
			url: `/pages/preview/preview-detail?wallpaperId=${item.id}&type=${item.type}`
		})
	}
}
</script>

<style lang="scss" scoped>
.avatar-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 15rpx;
	padding: 10rpx 0;

	.avatar-item {
		width: 100%;
		aspect-ratio: 1;
		border-radius: 20rpx;
		overflow: hidden;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
		transition: all 0.3s;

		&:active {
			transform: scale(0.98);
		}

		:deep(.avatar-img) {
			width: 100%;
			height: 100%;
			object-fit: cover;
		}
	}
}
</style>
