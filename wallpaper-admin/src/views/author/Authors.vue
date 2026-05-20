<template>
  <div class="authors">
    <el-card>
      <div class="header">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名或昵称"
          clearable
          style="width: 300px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
          :suffix-icon="Search"
        >
        </el-input>
      </div>

      <el-table :data="tableData" style="margin-top: 20px" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="120" />
        <el-table-column label="头像">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="用户昵称" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">
            {{ row.role === 'user' ? '普通用户' : '管理员' }}
          </template>
        </el-table-column>
        <el-table-column prop="bio" label="简介" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.bio || '--' }}
          </template>
        </el-table-column>
        <el-table-column prop="workCount" label="作品数"/>
        <el-table-column prop="totalLikes" label="点赞数"/>
        <el-table-column prop="totalCollects" label="收藏数"/>
        <el-table-column label="是否允许上传作品">
          <template #default="{ row }">
            <el-switch v-model="row.isCreator" @change="handleCreatorStatusChange(row)"/>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { getAllUsers, updateCreatorStatus, deleteAuthor } from '@/api/author'

const loading = ref(false)
const tableData = ref([])
const searchKeyword = ref('')

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
      pageSize: pagination.value.limit,
      keyword: searchKeyword.value
    }
    const res = await getAllUsers(params)
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

const handleSearch = () => {
  pagination.value.page = 1
  loadData()
}

const handleCreatorStatusChange = async (row) => {
  try {
    await updateCreatorStatus(row.id, { isCreator: row.isCreator })
    ElMessage.success('修改成功')
  } catch (error) {
    ElMessage.error('修改失败')
    row.isCreator = !row.isCreator
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这个创作者吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAuthor(row.id)
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
.authors {
  padding: 20px;

  .header {
    display: flex;
    justify-content: flex-start;
    margin-bottom: 20px;
  }

  .el-table :deep(.el-table__cell) {
    text-align: center;
  }
}
</style>