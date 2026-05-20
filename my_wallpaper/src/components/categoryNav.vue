<template>
	<view class="category-nav-wrapper">
		<scroll-view 
			v-if="scrollable"
			class="category-nav scrollable" 
			scroll-x 
			:show-scrollbar="false"
		>
			<view class="nav-content">
				<view 
					class="nav-item" 
					:class="{ active: modelValue === item.value }"
					v-for="item in items" 
					:key="item.value"
					@click="handleClick(item.value)"
				>
					{{ item.label }}
				</view>
			</view>
		</scroll-view>
		<view v-else class="category-nav flex-nav">
			<view 
				class="nav-item" 
				:class="{ active: modelValue === item.value }"
				v-for="item in items" 
				:key="item.value"
				@click="handleClick(item.value)"
			>
				{{ item.label }}
			</view>
		</view>
	</view>
</template>

<script setup>
const props = defineProps({
	modelValue: {
		type: [String, Number],
		required: true
	},
	items: {
		type: Array,
		required: true,
		default: () => []
	},
	scrollable: {
		type: Boolean,
		default: true
	}
})

const emit = defineEmits(['update:modelValue', 'change'])

const handleClick = (value) => {
	if (props.modelValue === value) return
	
	// 先触发 change 事件（带新值），让父组件可以立即响应
	emit('change', value)
	// 再更新 v-model
	emit('update:modelValue', value)
}
</script>

<style lang="scss" scoped>
.category-nav-wrapper {
	width: 100%;
	flex-shrink: 0;
}

.category-nav {
	width: 100%;
	background: transparent !important;

	&.scrollable {
		white-space: nowrap;
		overflow-x: auto;
		overflow-y: hidden;

		&::-webkit-scrollbar {
			display: none;
		}

		.nav-content {
			display: inline-block;
			padding: 20rpx 30rpx;
			white-space: nowrap;
		}

		.nav-item {
			display: inline-block;
			height: 60rpx;
			line-height: 60rpx;
			background-color: #e8e8e8;
			color: #666;
			padding: 0 30rpx;
			margin-right: 20rpx;
			border-radius: 30rpx;
			font-size: 28rpx;
			transition: all 0.3s;

			&:last-child {
				margin-right: 0;
			}

			&.active {
				background-color: #7a544d;
				color: #fff;
				font-weight: 600;
			}
		}
	}

	&.flex-nav {
		display: flex;
		padding: 20rpx 30rpx;
		gap: 20rpx;
		width: 100%;
		box-sizing: border-box;

		.nav-item {
			flex: 1;
			min-width: 0;
			max-width: none;
			height: 60rpx;
			line-height: 60rpx;
			text-align: center;
			background-color: #e8e8e8;
			color: #666;
			border-radius: 30rpx;
			font-size: 28rpx;
			transition: all 0.3s;
			white-space: nowrap;
			overflow: hidden;
			text-overflow: ellipsis;

			&.active {
				background-color: #7a544d;
				color: #fff;
				font-weight: 600;
			}
		}
	}
}
</style>
