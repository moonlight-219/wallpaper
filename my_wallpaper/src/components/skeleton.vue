<template>
	<view class="skeleton-base" :class="shapeClass" :style="customStyle"></view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
	shape: {
		type: String,
		default: 'rect',
		validator: (v) => ['rect', 'circle', 'round'].includes(v)
	},
	width: {
		type: String,
		default: '100%'
	},
	height: {
		type: String,
		default: '40rpx'
	},
	radius: {
		type: String,
		default: ''
	}
})

const shapeClass = computed(() => `skeleton-${props.shape}`)

const customStyle = computed(() => {
	const style = {}
	if (props.shape === 'circle') {
		const size = props.width
		style.width = size
		style.height = size
	} else {
		style.width = props.width
		style.height = props.height
		if (props.radius) {
			style.borderRadius = props.radius
		}
	}
	return style
})
</script>

<style lang="scss" scoped>
.skeleton-base {
	background-color: #f0f0f0;
	flex-shrink: 0;
}

.skeleton-rect {
	border-radius: 8rpx;
}

.skeleton-round {
	border-radius: 20rpx;
}

.skeleton-circle {
	border-radius: 50%;
}
</style>
