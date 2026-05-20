<template>
	<view class="authorItem" @click="goAuthorDetailPage">
		<view class="authorItem-top">
			<view class="left">
				<view class="author-avatar">
					<image lazy-load class="avatar" :src="author.avatarUrl || defaultAvatar" mode="aspectFill" @error="handleAvatarError">
					</image>
				</view>
				<view class="author-info">
					<view class="author-name">{{ author.name || '未知作者' }}</view>
					<view class="author-stats" v-if="author.workCount">
						<text>{{ author.workCount }} 作品</text>
					</view>
				</view>
			</view>
			<view class="right">
				<text>查看更多</text>
				<uni-icons type="right" size="16" color="#999"></uni-icons>
			</view>
		</view>

		<!-- 图片列表：根据数量自动分配宽度 -->
		<view class="author-list" :class="`img-count-${imgList.length}`">
			<view class="author-item" v-for="item in imgList" :key="item.id">
				<imageSkeleton :src="item.thumbnailUrl || item.url" mode="aspectFill" width="100%" height="100%"
					border-radius="16rpx" image-class="item-pic" />
			</view>
		</view>
	</view>
</template>

<script setup>
import { computed, ref } from "vue";
import imageSkeleton from '@/components/imageSkeleton.vue'

const props = defineProps({
	author: {
		type: Object,
		default: () => ({})
	}
})

const defaultAvatar = '/static/images/1.jpg'

const handleAvatarError = (e) => {
	console.warn('作者头像加载失败，使用默认头像')
}

const imgList = computed(() => {
	if (!props.author || !props.author.hotWallpapers) {
		return []
	}
	return props.author.hotWallpapers.map(wallpaper => ({
		id: wallpaper.id,
		url: wallpaper.url,
		thumbnailUrl: wallpaper.thumbnailUrl || wallpaper.url
	}))
})

const goAuthorDetailPage = () => {
	uni.navigateTo({
		url: `/pagesAuthor/author-detail?id=${props.author.id}`
	})
}
</script>

<style lang="scss" scoped>
.authorItem {
	width: 100%;
	background: #fff;
	border-radius: 16rpx;
	padding: 10rpx;
	margin: 20rpx 0;
	overflow: hidden;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
	transition: all 0.3s ease;

	&:active {
		transform: translateY(-2rpx);
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
	}

	.authorItem-top {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 24rpx;

		.left {
			display: flex;
			align-items: center;
			gap: 16rpx;

			.author-avatar {
				width: 80rpx;
				height: 80rpx;
				border-radius: 50%;
				overflow: hidden;
				flex-shrink: 0;
				border: 2rpx solid #f5f5f5;

				.avatar {
					width: 100%;
					height: 100%;
					object-fit: cover;
				}
			}

			.author-info {
				display: flex;
				flex-direction: column;
				gap: 6rpx;

				.author-name {
					font-size: 30rpx;
					font-weight: 600;
					color: #333;
					line-height: 1.2;
				}

				.author-stats {
					font-size: 22rpx;
					color: #999;
				}
			}
		}

		.right {
			display: flex;
			align-items: center;
			gap: 4rpx;
			font-size: 24rpx;
			color: #999;
			padding: 8rpx 16rpx;
			border-radius: 24rpx;
			background: #f8f8f8;
			transition: all 0.2s ease;

			&:active {
				background: #f0f0f0;
			}
		}
	}

	.author-list {
		display: flex;
		padding: 0 24rpx 24rpx;
		gap: 12rpx;

		/* 1张图片：占满 */
		&.img-count-1 {
			.author-item {
				width: 100%;
				min-width: auto;
			}
		}

		/* 2张图片：各占一半 */
		&.img-count-2 {
			.author-item {
				width: calc(50% - 6rpx);
				min-width: auto;
			}
		}

		/* 3张图片：各占1/3 */
		&.img-count-3 {
			.author-item {
				width: calc(33.333% - 8rpx);
				min-width: auto;
			}
		}

		.author-item {
			border-radius: 16rpx;
			overflow: hidden;
			flex-shrink: 0;
			height: 350rpx;
			/* 默认宽度，1/2/3张时会被覆盖 */
			min-width: 200rpx;
			background: #f5f5f5;
			transition: transform 0.2s ease;

			&:active {
				transform: scale(0.96);
			}

			:deep(.item-pic) {
				width: 100%;
				height: 100%;
				object-fit: cover;
				display: block;
			}
		}
	}
}
</style>