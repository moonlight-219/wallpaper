<template>
	<view class="tabletItem">
		<view class="tab-item" v-for="(item, index) in items" :key="item.id" @click="goPreview(item, index)">
			<imageSkeleton :src="item.thumbnailUrl || item.url" mode="aspectFill" width="100%" height="100%"
				border-radius="20rpx" image-class="tab-img" />
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
.tabletItem {
	width: 100%;
	display: flex;
	flex-wrap: wrap;

	.tab-item {
		width: calc(50%);
		height: 200rpx;
		border-radius: 20px;
		overflow: hidden;
		padding: 10rpx;

		:deep(.tab-img) {
			width: 100%;
			height: 100%;
			object-fit: cover;
			border-radius: 20rpx;
		}
	}
}
</style>
