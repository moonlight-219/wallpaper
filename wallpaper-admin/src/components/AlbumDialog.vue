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
      <el-form-item label="专辑名称" prop="albumName">
        <el-input v-model="formData.albumName" placeholder="请输入专辑名称" />
      </el-form-item>
      <el-form-item label="专辑封面" prop="cover">
        <el-upload
          class="cover-uploader"
          action="#"
          :show-file-list="false"
          :on-success="handleUploadSuccess"
          :before-upload="beforeUpload"
        >
          <img v-if="formData.cover" :src="formData.cover" class="cover" />
          <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
        </el-upload>
        <div class="upload-tip">建议尺寸：800x800，支持 jpg、png 格式</div>
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
import { Plus } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '添加专辑'
  },
  data: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const formRef = ref(null)
const loading = ref(false)

// 表单数据
const formData = ref({
  id: '',
  albumName: '',
  cover: '',
  wallpaperCount: 0,
  likeCount: 0
})

// 表单校验规则
const formRules = {
  albumName: [
    { required: true, message: '请输入专辑名称', trigger: 'blur' },
    { min: 2, max: 30, message: '专辑名称长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  cover: [
    { required: true, message: '请上传专辑封面', trigger: 'change' }
  ]
}

// 双向绑定 visible
const visible = ref(props.modelValue)
watch(() => props.modelValue, (val) => {
  visible.value = val
})
watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 监听传入的数据
watch(() => props.data, (val) => {
  if (val && Object.keys(val).length > 0) {
    formData.value = { ...val }
  }
}, { immediate: true, deep: true })

// 图片上传前置校验
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

// 图片上传成功处理（模拟）
const handleUploadSuccess = (_response, file) => {
  // 实际项目替换为接口返回的图片URL
  formData.value.cover = URL.createObjectURL(file.raw)
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  formData.value = {
    id: '',
    albumName: '',
    cover: '',
    wallpaperCount: 0,
    likeCount: 0
  }
}

// 关闭对话框
const handleClose = () => {
  resetForm()
  emit('cancel')
}

// 取消
const handleCancel = () => {
  visible.value = false
  emit('cancel')
}

// 确认
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

// 暴露方法给父组件
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

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
</style>
