<template>
  <div class="categories">
    <el-card>
      <!-- 筛选栏 + 新增按钮 -->
      <div class="header" style="margin-bottom: 20px">
        <div class="filter-group">
          <!-- 分类名模糊搜索框 -->
          <el-input
            v-model="filters.keyword"
            placeholder="请输入分类名搜索"
            clearable
            style="width: 250px"
            @clear="loadData"
            @keyup.enter="loadData"
            :suffix-icon="Search"
          />
        </div>

        <el-button type="primary" @click="handleAdd">添加分类</el-button>
      </div>

      <el-table
        :data="tableData"
        style="margin-top: 20px"
        v-loading="loading"
        border
        align="center"
      >
        <el-table-column type="index" label="序号" width="120" />
        <el-table-column prop="coverUrl" label="分类封面">
          <template #default="{ row }">
            <el-image
              :src="row.coverUrl"
              style="width: 80px; height: 60px"
              fit="cover"
              fallback="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png"
              :preview-src-list="[row.coverUrl]"
              :preview-teleported="true"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="分类名"/>
        <el-table-column prop="alias" label="别名"/>
        <el-table-column prop="sortOrder" label="排序"/>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button type="primary" circle :icon="Edit" @click="handleEdit(row)"/>
            <el-button type="danger" circle :icon="Delete" @click="handleDelete(row)"/>
          </template>
        </el-table-column>
      </el-table>
      
      <Pagination
        v-if="pagination.total"
        :page="pagination.page"
        :limit="pagination.limit"
        :total="pagination.total"
        @pagination="loadData"
      />
    </el-card>

    <!-- 添加/编辑分类对话框 -->
    <CategoryFormDialog
      v-model="dialogVisible"
      :title="dialogTitle"
      :data="dialogData"
      @confirm="handleDialogConfirm"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import CategoryFormDialog from '@/components/CategoryDialog.vue'
import { getCategoryList, getCategoryDetail, addCategory, updateCategory, deleteCategory } from '@/api/category'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogData = ref({})

const filters = ref({
  keyword: ''
})

const pagination = ref({
  page: 1,
  limit: 20,
  total: 0
})

const loadData = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.value.page,
      pageSize: pagination.value.limit
    }
    if (filters.value.keyword) {
      params.name = filters.value.keyword
    }
    const res = await getCategoryList(params)
    if (res.code === 200) {
      tableData.value = res.data?.list || []
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

const handleAdd = () => {
  dialogTitle.value = '添加分类'
  dialogData.value = {}
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    loading.value = true
    const res = await getCategoryDetail(row.id)
    if (res.code === 200) {
      dialogTitle.value = '编辑分类'
      dialogData.value = { ...res.data }
      dialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取分类详情失败')
  } finally {
    loading.value = false
  }
}

const handleDialogConfirm = async (data) => {
  try {
    if (data.id) {
      await updateCategory(data.id, data)
      ElMessage.success('编辑成功')
    } else {
      await addCategory(data)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这个分类吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCategory(row.id)
      ElMessage.success('删除成功')
      await loadData()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.categories {
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .filter-group {
      display: flex;
      align-items: center;
    }
  }

  // 表格单元格样式优化
  .el-table :deep(.el-table__cell) {
    padding: 12px 0;
    text-align: center;
  }
}
</style>