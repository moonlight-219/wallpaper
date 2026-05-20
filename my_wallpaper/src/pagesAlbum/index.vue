<template>
	<view class="bz-album">
		<myTitleBar title="专辑精选">
			<view class="back-btn" @click="goBack()">
				<uni-icons type="left" size="20" color="#534747"></uni-icons>
			</view>
		</myTitleBar>

		<view class="bz-album-list">
			<view class="bz-album-item" v-for="item in items" :key="item.id">
				<albumItem :album="item"></albumItem>
			</view>
		</view>
	</view>
</template>

<script setup>
import myTitleBar from '@/components/myTitleBar.vue'
import albumItem from '@/components/albumItem.vue'
import { ref, onMounted } from "vue"
import { getCategoryList } from '@/apis/category.js'
import { safeNavigateBack } from '@/utils/navigation.js'

// 专辑列表 - 从API获取
const items = ref([])

// 加载专辑分类列表
const loadAlbums = async () => {
	try {
		const res = await getCategoryList({
			page: 1,
			pageSize: 50 // 获取前50个分类
		})
		console.log('专辑分类API响应:', res)

		if (res.code === 200 && res.data) {
			// 将API返回的分类数据映射到页面数据格式
			items.value = res.data.list.map(category => ({
				id: category.id,
				name: category.name,
				alias: category.alias || '',
				url: category.coverUrl || '/pagesCategory/static/images/bgs/8.jpg',
				likeCount: category.likeCount || 0,
				sortOrder: category.sortOrder || 0,
				wallpaperCount: category.wallpaperCount || 0
			}))

			console.log('专辑分类加载成功，共', items.value.length, '个')
		}
	} catch (error) {
		console.error('加载专辑分类失败:', error)
		uni.showToast({
			title: '加载专辑失败',
			icon: 'none'
		})
	}
}

// 页面加载时获取专辑分类
onMounted(() => {
	loadAlbums()
})

// 返回上一级
const goBack = () => {
	safeNavigateBack()
}
</script>

<style lang="scss" scoped>
.bz-album {
	min-height: 100vh;
	width: 100vw;
	background: linear-gradient(325deg, #fdf4f4, #fff);
	// background: linear-gradient(145deg, #fbf0ef, #fff);

	.back-btn {
		width: 56rpx;
		height: 56rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.bz-album-list {
		padding: 20rpx 30rpx 40rpx;

		.bz-album-item {
			position: relative;
			height: 400rpx;
			margin-bottom: 20rpx;
			border-radius: 20rpx;
			overflow: hidden;
		}
	}
}
</style>