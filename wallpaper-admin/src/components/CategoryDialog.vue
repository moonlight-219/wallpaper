<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item label="名称：" prop="name">
        <el-input v-model="formData.name" placeholder="请输入分类名称" />
      </el-form-item>
      <el-form-item label="别名：" prop="alias">
        <el-input v-model="formData.alias" placeholder="请输入分类别名（可选）" />
      </el-form-item>
      <el-form-item label="封面：" prop="coverUrl">
        <el-upload
          class="cover-uploader"
          action="#"
          :show-file-list="false"
          :before-upload="beforeUpload"
          :http-request="handleUploadSuccess"
        >
          <div v-if="uploading" class="uploading-overlay">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>上传中...</span>
          </div>
          <img v-else-if="formData.coverUrl" :src="formData.coverUrl" class="cover" />
          <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
        </el-upload>
      </el-form-item>
      <el-form-item label="缩略图：" prop="iconPath">
        <el-input v-model="formData.iconPath" placeholder="上传封面后自动填充（可选）" disabled />
      </el-form-item>
      <el-form-item label="排序：" prop="sortOrder">
        <el-input-number v-model="formData.sortOrder" :min="0" placeholder="排序值（可选）" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleConfirm" :loading="loading">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Loading } from '@element-plus/icons-vue'
import { uploadCover } from '@/api/category'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '添加分类'
  },
  data: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const formRef = ref(null)
const loading = ref(false)
const uploading = ref(false)

const formData = ref({
  id: '',
  name: '',
  alias: '',
  iconPath: '',
  coverUrl: '',
  sortOrder: 0
})

const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 20, message: '分类名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  coverUrl: [
    { required: true, message: '请上传分类封面', trigger: 'change' }
  ]
}

const visible = ref(props.modelValue)
watch(() => props.modelValue, (val) => {
  visible.value = val
})
watch(visible, (val) => {
  emit('update:modelValue', val)
})

watch(() => props.data, (val) => {
  if (val && Object.keys(val).length > 0) {
    formData.value = { ...val }
  }
}, { immediate: true, deep: true })

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('请上传图片格式文件！')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB！')
    return false
  }
  return true
}

const handleUploadSuccess = async (options) => {
  try {
    uploading.value = true
    const res = await uploadCover(options.file)
    if (res.code === 200) {
      formData.value.coverUrl = res.data.url
      if (res.data.thumbnailUrl) {
        formData.value.iconPath = res.data.thumbnailUrl
      }
      ElMessage.success('上传成功')
    }
  } catch (error) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  formData.value = {
    id: '',
    name: '',
    alias: '',
    iconPath: '',
    coverUrl: '',
    sortOrder: 0
  }
}

const handleClose = () => {
  resetForm()
  emit('cancel')
}

const handleCancel = () => {
  visible.value = false
  emit('cancel')
}

const handleConfirm = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    emit('confirm', { ...formData.value })
  } catch (error) {
    ElMessage.error('表单填写有误，请检查！')
  } finally {
    loading.value = false
  }
}

defineExpose({
  resetForm
})
</script>

<style scoped lang="scss">
.cover-uploader {
  :deep(.el-upload) {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);

    &:hover {
      border-color: var(--el-color-primary);
    }
  }
}

.uploading-overlay {
  width: 178px;
  height: 178px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 8px;
  color: var(--el-color-primary);
  font-size: 14px;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}

.cover {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}
</style>
