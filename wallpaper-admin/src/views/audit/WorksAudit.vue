<template>
  <div class="works-audit">
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

          <el-select
            v-model="filters.status"
            placeholder="审核状态"
            clearable
            style="width: 150px"
            @change="handleFilter"
          >
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>

          <el-input
            v-model="filters.keyword"
            placeholder="搜索创作者"
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
        <el-table-column prop="title" label="标题"/>
        <el-table-column prop="type" label="类型">
          <template #default="{ row }">
            <el-tag :type="getTypeStyle(row.type)">{{ getTypeValue(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" />
        <el-table-column prop="creator" label="创作者" />
        <el-table-column prop="status" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusStyle(row.status)">{{ getStatusValue(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="collectCount" label="收藏量" />
        <el-table-column prop="likeCount" label="点赞量" />
        <el-table-column prop="publishTime" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              :icon="View"
              @click="handleViewDetail(row)" 
              circle
            />
            <el-button 
              type="success" 
              :icon="Select"
              @click="handleApprove(row)" 
              v-if="row.status === 0"
              circle
            />
            <el-button 
              type="danger" 
              :icon="Close"
              @click="handleReject(row)" 
              v-if="row.status === 0"
              circle
            />
            <el-button 
              type="danger" 
              :icon="Delete"
              @click="handleDelete(row)" 
              v-if="row.status === 1"
              circle
            />
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

    <!-- 详情抽屉 -->
    <WorkDetailDrawer
      v-model="detailDrawerVisible"
      :detail="currentDetail"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, View, Delete, Select, Close  } from '@element-plus/icons-vue'
import { getSquareWorks, auditWork } from '@/api/square'
import { getCategoryList } from '@/api/category'
import Pagination from '@/components/Pagination.vue'
import WorkDetailDrawer from '@/components/WorkDetailDrawer.vue'
import { formatTime } from '@/utils'

const loading = ref(false)
const tableData = ref([])
const categories = ref([])
const detailDrawerVisible = ref(false)
const currentDetail = ref(null)

// 筛选条件
const filters = ref({
  sortType: null,
  categoryId: null,
  status: null,
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

const getStatusStyle = (status) => {
  const styles = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return styles[status] || ''
}

const getStatusValue = (status) => {
  const values = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
  }
  return values[status] || '未知'
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

    if (filters.value.status !== null) {
      params.status = filters.value.status
    }

    const res = await getSquareWorks(params)
    if (res.code === 200) {
      const works = res.data?.list || []
      tableData.value = works.map(work => ({
        id: work.id,
        title: work.title,
        imageUrl: work.coverUrl,
        type: work.type,
        categoryName: '作品集',
        creator: work.author?.name || '未知',
        status: work.status ?? 1,
        collectCount: work.collects,
        likeCount: work.likes,
        downloadCount: 0,
        publishTime: work.publishTime,
        detail: work
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

const handleViewDetail = (row) => {
  currentDetail.value = row.detail
  detailDrawerVisible.value = true
}

// 审核通过
const handleApprove = async (row) => {
  try {
    ElMessageBox.confirm('确定要通过这个作品集的审核吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      const res = await auditWork(row.id, 1, '')
      if (res.code === 200) {
        ElMessage.success('审核通过')
        loadData()
      }
    }).catch(() => {})
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 审核拒绝
const handleReject = async (row) => {
  try {
    ElMessageBox.prompt('请输入拒绝原因', '审核拒绝', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '拒绝原因不能为空',
      type: 'error'
    }).then(async ({ value }) => {
      const res = await auditWork(row.id, 2, value)
      if (res.code === 200) {
        ElMessage.success('已拒绝审核')
        loadData()
      }
    }).catch(() => {})
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 删除壁纸
const handleDelete = async (row) => {
  try {
    ElMessageBox.confirm('确定要删除这个作品集吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      ElMessage.success('删除成功')
      loadData()
    }).catch(() => {})
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped lang="scss">
.works-audit {
  .header {
    margin-bottom: 20px;
    
    .filter-section {
      display: flex;
      gap: 12px;
      align-items: center;
    }
  }
  
  .detail-content {
    padding: 20px;
    
    .detail-section {
      margin-bottom: 30px;
      
      h3 {
        margin-bottom: 16px;
        font-size: 16px;
        font-weight: 500;
        color: #303133;
      }
      
      .info-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
        gap: 16px;
      }
      
      .info-item {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .label {
          color: #909399;
          font-size: 14px;
        }
        
        .value {
          color: #303133;
          font-size: 14px;
          font-weight: 500;
        }
      }
    }
    
    .wallpaper-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
      gap: 12px;
    }
    
    .wallpaper-item {
      position: relative;
      padding-bottom: 100%;
      border-radius: 4px;
      overflow: hidden;
      border: 1px solid #dcdfe6;
      
      .el-image {
        position: absolute;
        top: 0;
        left: 0;
      }
      
      .wallpaper-info {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        background: rgba(0, 0, 0, 0.6);
        color: #fff;
        font-size: 12px;
        padding: 4px 8px;
        text-align: center;
      }
    }
  }
}
</style>
