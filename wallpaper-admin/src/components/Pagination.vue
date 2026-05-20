<template>
  <el-pagination
    :current-page="currentPage"
    :page-size="pageSize"
    :page-sizes="pageSizes"
    :size="size"
    :total="total"
    :layout="layout"
    :background="background"
    @current-change="handleCurrentChange"
    @size-change="handleSizeChange"
    :style="paginationStyle"
  />
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 当前页码
  page: {
    type: Number,
    default: 1
  },
  // 每页显示条数
  limit: {
    type: Number,
    default: 20
  },
  // 总条数
  total: {
    type: Number,
    default: 0
  },
  // 组件布局，子组件名用逗号分隔
  layout: {
    type: String,
    default: 'total, sizes, prev, pager, next'
  },
  size: {
    type: Number,
    default: 10
  },
  // 每页显示个数选择器的选项设置
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  // 是否为分页按钮添加背景色
  background: {
    type: Boolean,
    default: true
  },
  // 自定义样式
  customStyle: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:page', 'update:limit', 'pagination'])

// 双向绑定当前页码
const currentPage = computed({
  get: () => props.page,
  set: (val) => emit('update:page', val)
})

// 双向绑定每页条数
const pageSize = computed({
  get: () => props.limit,
  set: (val) => emit('update:limit', val)
})

// 分页样式
const paginationStyle = computed(() => ({
  marginTop: '20px',
  justifyContent: 'center',
  ...props.customStyle
}))

// 页码改变
const handleCurrentChange = (val) => {
  emit('pagination', { page: val, limit: props.limit })
}

// 每页条数改变
const handleSizeChange = (val) => {
  emit('pagination', { page: 1, limit: val })
}
</script>

<style scoped lang="scss">
.el-pagination {
  width: 100%;
  display: flex;
  justify-content: flex-end !important;
}
</style>
