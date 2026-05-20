<template>
	<view class="imgItem-box">
		<view @click="handleClick(item, index)" class="imgItem" v-for="(item, index) in items" :key="item.id">
			<imageSkeleton :src="item.thumbnailUrl || item.url" mode="aspectFill" width="100%" height="100%"
				border-radius="20rpx" image-class="imgItem-pic" />
		</view>
	</view>
</template>

<script setup>
import imageSkeleton from '@/components/imageSkeleton.vue'
import { setPreviewList } from '@/store/previewStore.js'

const props = defineProps({
	items: {
		type: Array,
		required: true
	}
})

const handleClick = (item, index) => {
	const currentPage = getCurrentPages()
	const currentRoute = currentPage[currentPage.length - 1].route
	const includesPreview = ['pages/index/index', 'pagesAlbum/album-detail', 'pagesCategory/phone',
		'pagesAuthor/author-detail', 'pagesCategory/classify'
	]
	if (includesPreview.includes(currentRoute)) {
		// 将完整壁纸列表存入全局变量
		setPreviewList(props.items)
		uni.navigateTo({
			url: `/pages/preview/preview-detail?wallpaperId=${item.id}&type=${item.type}`
		})
		return
	}
	uni.navigateTo({
		url: '/pagesCategory/classify'
	})
}
</script>

<style lang="scss" scoped>
.imgItem-box {
	width: 100%;
	display: flex;
	flex-wrap: wrap;
	align-content: flex-start;


	.imgItem {
		width: calc(33.33%);
		height: 400rpx;
		padding: 10rpx;
		display: inline-block;

		:deep(.imgItem-pic) {
			width: 100%;
			height: 100%;
			border-radius: 20rpx;
			object-fit: cover;
		}
	}

}
</style>