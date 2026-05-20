<template>
  <div class="wallpapers">
    <el-card>
      <div class="header">
        <div class="filter-section">
          <el-select
            v-model="filters.sortType"
            placeholder="排序方式"
            clearable
            style="width: 150px"
            @change="handleFilter"
          >
            <el-option label="按点赞" :value="1" />
            <el-option label="按收藏" :value="2" />
          </el-select>

          <el-select
            v-model="filters.categoryId"
            placeholder="所属分类"
            clearable
            style="width: 150px"
            @change="handleFilter"
          >
            <el-option 
              v-for="category in categories" 
              :key="category.id" 
              :label="category.name" 
              :value="category.id" 
            />
          </el-select>

          <el-input
            v-model="filters.keyword"
            placeholder="搜索标题或创作者"
            clearable
            style="width: 250px"
            @clear="handleFilter"
            @keyup.enter="handleFilter"
          >
            <template #append>
              <el-button :icon="Search" @click="handleFilter" />
            </template>
          </el-input>
        </div>
      </div>
      
      <el-table 
        :data="tableData" 
        style="margin-top: 20px" 
        v-loading="loading"
        border
        align="center"
        :scrollbar-always-on="false"
      >
        <el-table-column label="序号" type="index" width="80" />
        <el-table-column label="预览" width="120">
          <template #default="{ row }">
            <el-image 
              :src="row.imageUrl" 
              :preview-src-list="[row.imageUrl]" 
              style="width: 80px; height: 80px" 
              fit="cover" 
              fallback="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png"
              :preview-teleported="true"
            />
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型">
          <template #default="{ row }">
            <el-tag :type="getTypeStyle(row.type)">{{ getTypeValue(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创作者头像" width="100">
          <template #default="{ row }">
            <el-avatar 
              :src="row.avatar" 
              :size="50"
              fit="cover"
              fallback="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"
            />
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" />
        <el-table-column prop="creator" label="创作者" />
        <el-table-column prop="collectCount" label="收藏量" />
        <el-table-column prop="likeCount" label="点赞量" />
        <el-table-column prop="downloadCount" label="下载量" />
        <el-table-column prop="publishTime" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.publishTime) }}
          </template>
        </el-table-column>
      </el-table>
      
      <Pagination
        :page="pagination.page"
        :limit="pagination.limit"
        :total="pagination.total"
        @pagination="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getWallpaperList } from '@/api/wallpaper'
import { getCategoryList } from '@/api/category'
import Pagination from '@/components/Pagination.vue'
import { formatTime } from '@/utils'

const loading = ref(false)
const tableData = ref([])
const categories = ref([])

// 筛选条件
const filters = ref({
  sortType: null,
  categoryId: null,
  keyword: ''
})

// 分页参数
const pagination = ref({
  page: 1,
  limit: 20,
  total: 0
})

const getTypeStyle = (type) => {
  const types = {
    1: 'primary',
    2: 'success',
    3: 'warning'
  }
  return types[type] || ''
}

const getTypeValue = (type) => {
  const types = {
    1: '手机壁纸',
    2: '平板壁纸',
    3: '头像'
  }
  return types[type] || '未知'
}

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    if (res.code === 200) {
      categories.value = res.data?.list || []
    }
  } catch (error) {
    ElMessage.error('加载分类列表失败')
  }
}

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.value.page,
      pageSize: pagination.value.limit
    }

    if (filters.value.sortType !== null) {
      params.sortType = filters.value.sortType
    }

    if (filters.value.categoryId) {
      params.categoryId = filters.value.categoryId
    }

    if (filters.value.keyword) {
      params.keyword = filters.value.keyword
    }

    const res = await getWallpaperList(params)
    if (res.code === 200) {
      const wallpapers = res.data?.list || []
      tableData.value = wallpapers.map(wallpaper => ({
        id: wallpaper.id,
        imageUrl: wallpaper.url,
        type: wallpaper.type,
        categoryName: wallpaper.category || '未知',
        creator: wallpaper.author?.name || '未知',
        avatar: wallpaper.author?.avatarUrl || '',
        collectCount: wallpaper.collectCount || 0,
        likeCount: wallpaper.likeCount || 0,
        downloadCount: wallpaper.downloadCount || 0,
        publishTime: wallpaper.createTime,
        detail: wallpaper
      }))
      pagination.value.total = res.data?.total || 0
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
    tableData.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

// 处理筛选
const handleFilter = () => {
  pagination.value.page = 1
  loadData()
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped lang="scss">
.wallpapers {
  .header {
    margin-bottom: 20px;
    
    .filter-section {
      display: flex;
      gap: 12px;
      align-items: center;
    }
  }
}
</style>
