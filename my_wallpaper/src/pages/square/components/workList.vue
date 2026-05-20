<template>
	<view>
		<view class="skeleton-container" v-if="loading && worksList.length === 0">
			<view class="skeleton-item" v-for="i in 3" :key="i">
				<view class="skeleton-author">
					<skeleton shape="circle" width="80rpx" />
					<view class="skeleton-info">
						<skeleton width="150rpx" height="24rpx" />
						<skeleton width="100rpx" height="24rpx" />
					</view>
				</view>
				<skeleton width="100%" height="400rpx" radius="12rpx" />
				<view class="skeleton-stats">
					<skeleton width="80rpx" height="30rpx" />
					<skeleton width="80rpx" height="30rpx" />
				</view>
			</view>
		</view>

		<view class="square-list" v-else>
			<view class="work-item" v-for="(work, index) in worksList" :key="work.id">
				<view class="author-info" @click.stop="$emit('author', work.author)">
					<image class="avatar" :src="work.author.avatar" mode="aspectFill" lazy-load></image>
					<view class="author-detail">
						<view class="author-name">{{ work.author.name }}</view>
						<view class="publish-time">{{ work.publishTime }}</view>
					</view>
				</view>

				<view class="work-cover" @click="$emit('preview', index)">
					<image class="cover-img" :src="work.coverUrl" mode="aspectFill" lazy-load></image>
					<view class="work-count" v-if="work.images.length > 1">
						<uni-icons type="images" size="16" color="#fff"></uni-icons>
						<text>{{ work.images.length }}</text>
					</view>
				</view>

				<view class="work-info">
					<view class="work-stats">
						<view class="stat-item" @click.stop="$emit('like', work)">
							<uni-icons :type="work.isLiked ? 'heart-filled' : 'heart'" size="22"
								:color="work.isLiked ? '#e88b8b' : '#999'"></uni-icons>
							<text>{{ work.likes }}</text>
						</view>
						<view class="stat-item" @click.stop="$emit('collect', work)">
							<uni-icons :type="work.isCollected ? 'star-filled' : 'star'" size="22"
								:color="work.isCollected ? '#f4a460' : '#999'"></uni-icons>
							<text>{{ work.collects }}</text>
						</view>
					</view>
				</view>
			</view>
		</view>

		<uni-load-more :status="loadStatus"></uni-load-more>
	</view>
</template>

<script setup>
import skeleton from '@/components/skeleton.vue'

defineProps({
	worksList: {
		type: Array,
		default: () => []
	},
	loading: {
		type: Boolean,
		default: false
	},
	loadStatus: {
		type: String,
		default: 'more'
	}
})

defineEmits(['preview', 'like', 'collect', 'author'])
</script>

<style lang="scss" scoped>
.skeleton-container {
	padding: 20rpx;

	.skeleton-item {
		background: #fff;
		border-radius: 20rpx;
		margin-bottom: 20rpx;
		padding: 20rpx;
		overflow: hidden;

		.skeleton-author {
			display: flex;
			align-items: center;
			gap: 20rpx;
			margin-bottom: 20rpx;

			.skeleton-info {
				flex: 1;
				display: flex;
				flex-direction: column;
				gap: 10rpx;
			}
		}

		.skeleton-stats {
			display: flex;
			gap: 40rpx;
			margin-top: 20rpx;
		}
	}
}

.square-list {
	padding: 20rpx;

	.work-item {
		background: #fff;
		border-radius: 20rpx;
		margin-bottom: 20rpx;
		overflow: hidden;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);

		.author-info {
			display: flex;
			align-items: center;
			padding: 20rpx;
			gap: 20rpx;

			.avatar {
				width: 80rpx;
				height: 80rpx;
				border-radius: 50%;
				border: 4rpx solid #f5f5f5;
			}

			.author-detail {
				flex: 1;

				.author-name {
					font-size: 28rpx;
					font-weight: 600;
					color: #333;
					margin-bottom: 8rpx;
				}

				.publish-time {
					font-size: 24rpx;
					color: #999;
				}
			}
		}

		.work-cover {
			width: 100%;
			height: 400rpx;
			position: relative;
			overflow: hidden;

			.cover-img {
				width: 100%;
				height: 100%;
				object-fit: cover;
			}

			.work-count {
				position: absolute;
				top: 20rpx;
				right: 20rpx;
				background: rgba(0, 0, 0, 0.5);
				color: #fff;
				padding: 8rpx 16rpx;
				border-radius: 30rpx;
				font-size: 24rpx;
				display: flex;
				align-items: center;
				gap: 8rpx;
				backdrop-filter: blur(8rpx);
			}
		}

		.work-info {
			padding: 20rpx;

			.work-stats {
				display: flex;
				align-items: center;
				gap: 40rpx;

				.stat-item {
					display: flex;
					align-items: center;
					gap: 10rpx;
					font-size: 24rpx;
					color: #666;
					transition: all 0.2s ease;

					&:active {
						transform: scale(0.95);
					}

					text {
						transition: all 0.3s ease;
					}
				}
			}
		}
	}
}
</style>
