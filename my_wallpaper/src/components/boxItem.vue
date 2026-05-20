<template>
	<view class="boxItem">
		<!-- 左列 -->
		<view class="column left-column">
			<view v-for="(item, index) in leftColumnItems" :key="item.id" class="box-img">
				<view class="wrapper">
					<imageSkeleton @click="goPreview(item, item.originalIndex)" :src="item.thumbnailUrl || item.url" 
						mode="aspectFill" :width="'100%'" :height="getImageHeight(item.type)" 
						border-radius="16rpx" image-class="imges" />
					<view class="text">
						<view class="text-bottom">
							<view class="left">
								<image lazy-load :src="getAuthorAvatarUrl(item)" mode="aspectFill"></image>
								<view class="left-text">{{ item.author.name || '未知作者' }}</view>
							</view>
							<view class="right" @click.stop="toggleLike(item)">
								<uni-icons type="heart" v-if="!item.isLiked" color="#999"></uni-icons>
								<uni-icons type="heart-filled" v-else color="#e88b8b"></uni-icons>
								<view class="number">{{ formatLikeCount(item.likeCount) || 0 }}</view>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 右列 -->
		<view class="column right-column">
			<view v-for="(item, index) in rightColumnItems" :key="item.id" class="box-img">
				<view class="wrapper">
					<imageSkeleton @click="goPreview(item, item.originalIndex)" :src="item.thumbnailUrl || item.url" 
						mode="aspectFill" :width="'100%'" :height="getImageHeight(item.type)" 
						border-radius="16rpx" image-class="imges" />
					<view class="text">
						<view class="text-bottom">
							<view class="left">
								<image lazy-load :src="getAuthorAvatarUrl(item)" mode="aspectFill"></image>
								<view class="left-text">{{ item.author.name || '未知作者' }}</view>
							</view>
							<view class="right" @click.stop="toggleLike(item)">
								<uni-icons type="heart" v-if="!item.isLiked" color="#999"></uni-icons>
								<uni-icons type="heart-filled" v-else color="#e88b8b"></uni-icons>
								<view class="number">{{ formatLikeCount(item.likeCount) || 0 }}</view>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, watch } from 'vue'
import imageSkeleton from '@/components/imageSkeleton.vue'
import { likeWallpaper } from '@/apis/wallpaper.js'
import { isLoggedIn, ensureLoggedInForAction } from '@/utils/auth.js'
import { setPreviewList } from '@/store/previewStore.js'

const props = defineProps({
	items: {
		type: Array,
		default: () => []
	}
})

const leftColumnItems = ref([])
const rightColumnItems = ref([])

// 根据type获取图片高度
const getImageHeight = (type) => {
	const heightMap = {
		1: '600rpx',  // 手机壁纸 9:20
		2: '450rpx',  // 平板壁纸 16:21
		3: '345rpx'   // 头像 1:1
	}
	return heightMap[type] || '600rpx'
}

// 获取图片高度的数值（用于计算）
const getImageHeightValue = (type) => {
	const heightMap = {
		1: 600,  // 手机壁纸
		2: 450,  // 平板壁纸
		3: 345   // 头像
	}
	return heightMap[type] || 600
}

// 瀑布流布局算法
const layoutWaterfall = () => {
	if (!props.items || props.items.length === 0) {
		leftColumnItems.value = []
		rightColumnItems.value = []
		return
	}

	const left = []
	const right = []
	let leftHeight = 0
	let rightHeight = 0

	// 固定的底部信息高度
	const bottomInfoHeight = 120

	props.items.forEach((item, index) => {
		// 添加原始索引，用于预览
		const itemWithIndex = { ...item, originalIndex: index }
		
		// 获取图片高度
		const imageHeight = getImageHeightValue(item.type)
		// 总高度 = 图片高度 + 底部信息高度 + 间距
		const totalHeight = imageHeight + bottomInfoHeight + 15

		// 放到高度较小的那一列
		if (leftHeight <= rightHeight) {
			left.push(itemWithIndex)
			leftHeight += totalHeight
		} else {
			right.push(itemWithIndex)
			rightHeight += totalHeight
		}
	})

	leftColumnItems.value = left
	rightColumnItems.value = right
}

// 监听items变化，重新布局
watch(() => props.items, () => {
	layoutWaterfall()
}, { immediate: true, deep: true })

const getAuthorAvatarUrl = (item) => {
	const avatar = item.author?.avatarUrl
	if (avatar && (avatar.startsWith('http') || avatar.startsWith('/static'))) {
		return avatar
	}
	return '/static/images/1.jpg'
}

// 切换点赞
const toggleLike = async (item) => {
	if (!isLoggedIn()) {
		await ensureLoggedInForAction({
			content: '点赞需要先登录，是否前往登录？'
		})
		return
	}

	try {
		const res = await likeWallpaper(item.id)
		if (res.code === 200) {
			item.isLiked = !item.isLiked
			item.likeCount = item.isLiked ? item.likeCount + 1 : item.likeCount - 1
			uni.showToast({
				title: item.isLiked ? '点赞成功' : '取消点赞成功',
				icon: 'success'
			})
		} else {
			uni.showToast({
				title: res.message || '操作失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('点赞失败:', error)
		uni.showToast({
			title: '网络异常，请稍后重试',
			icon: 'none'
		})
	}
}

// 格式化点赞数
const formatLikeCount = (count) => {
	if (count >= 10000) {
		return (count / 10000).toFixed(1) + 'W'
	}
	if (count >= 1000) {
		return (count / 1000).toFixed(1) + 'K'
	}
	return count
}

// 去预览页
const goPreview = (item, index) => {
	// 将完整壁纸列表存入全局变量，供预览页轮播使用
	setPreviewList([...props.items])
	uni.navigateTo({
		url: `/pages/preview/preview-detail?wallpaperId=${item.id}&type=${item.type}`
	})
}
</script>

<style lang="scss" scoped>
.boxItem {
	width: 100%;
	display: flex;
	justify-content: space-between;
	align-items: flex-start;

	.column {
		width: calc(50% - 7.5rpx);
		display: flex;
		flex-direction: column;

		.box-img {
			width: 100%;
			margin-bottom: 15rpx;

			.wrapper {
				background-color: #fff;
				width: 100%;
				display: flex;
				flex-direction: column;
				overflow: hidden;
				border-radius: 16rpx;
				box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
				transition: all 0.3s;

				&:active {
					transform: scale(0.98);
				}

				:deep(.imges) {
					width: 100%;
					display: block;
				}

				.text {
					height: 120rpx;
					background-color: #fff;
					padding: 0 10rpx;
					font-size: 24rpx;
					color: #613942;
					flex-shrink: 0;
					display: flex;
					align-items: center;

					.text-bottom {
						width: 100%;
						display: flex;
						align-items: center;
						justify-content: space-between;
						gap: 10rpx;

						.left {
							display: flex;
							align-items: center;
							gap: 15rpx;
							flex: 1;
							min-width: 0;

							image {
								width: 60rpx;
								height: 60rpx;
								border-radius: 50%;
								object-fit: cover;
								border: 5rpx solid #f5f5f5;
								flex-shrink: 0;
							}

							.left-text {
								flex: 1;
								overflow: hidden;
								text-overflow: ellipsis;
								white-space: nowrap;
								font-size: 24rpx;
							}
						}

						.right {
							display: flex;
							align-items: center;
							gap: 5rpx;
							font-size: 18rpx;
							flex-shrink: 0;

							.number {
								min-width: 30rpx;
								text-align: center;
							}

							:deep(.uni-icons) {
								font-size: 24rpx !important;
							}
						}
					}
				}
			}
		}
	}
}
</style>
