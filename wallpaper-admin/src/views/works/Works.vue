<template>
  <div class="works">
    <el-card>
      <div class="header">
        <el-button type="primary" @click="handleAdd">添加壁纸</el-button>

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
        <el-table-column prop="title" label="标题"/>
        <el-table-column prop="type" label="类型">
          <template #default="{ row }">
            <el-tag :type="getTypeStyle(row.type)">{{ getTypeValue(row.type) }}</el-tag>
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
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">查看详情</el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(row)"
            >
              删除
            </el-button>
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

    <!-- 添加/编辑弹窗 -->
    <WallpaperDialog
      v-model="dialogVisible"
      :title="dialogTitle"
      :data="dialogData"
      @confirm="handleDialogConfirm"
      @cancel="handleDialogCancel"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getSquareWorks } from '@/api/square'
import { getCategoryList } from '@/api/category'
import { createWork, updateWorkStatus } from '@/api/work'
import { useUserStore } from '@/stores/user'
import Pagination from '@/components/Pagination.vue'
import WallpaperDialog from '@/components/WallpaperDialog.vue'
import WorkDetailDrawer from '@/components/WorkDetailDrawer.vue'
import { formatTime } from '@/utils'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const categories = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogData = ref({})
const detailDrawerVisible = ref(false)
const currentDetail = ref(null)

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

// 查询已通过的作品集
const status = ref(1)

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.value.page,
      pageSize: pagination.value.limit,
      status: status.value
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

const handleAdd = () => {
  dialogTitle.value = '添加壁纸'
  dialogData.value = {}
  dialogVisible.value = true
}

const handleViewDetail = (row) => {
  currentDetail.value = row.detail
  detailDrawerVisible.value = true
}

const handleDialogConfirm = async (data) => {
  try {
    const submitData = {
      ...data,
      userId: userStore.userInfo.id,
      status: userStore.userInfo.role === 'admin' ? 1 : 0
    }
    const res = await createWork(submitData)
    if (res.code === 200) {
      const message = userStore.userInfo.role === 'admin' 
        ? '作品集创建成功！管理员上传的作品集会直接发布'
        : '作品集创建成功！等待管理员审核'
      ElMessage.success(message)
      dialogVisible.value = false
      loadData()
    }
  } catch (error) {
    ElMessage.error('创建失败，请重试')
  }
}

const handleDialogCancel = () => {
  dialogVisible.value = false
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
.works {
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
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
