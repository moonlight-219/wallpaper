<template>
  <div>
    <el-dialog
      v-model="visible"
      :title="title"
      width="700px"
      :close-on-click-modal="false"
      @close="handleClose"
    >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      style="margin-top: 20px"
    >
      <el-form-item label="作品标题" prop="title">
        <el-input v-model="formData.title" placeholder="请输入作品集标题（如：治愈系星空）" />
      </el-form-item>
      
      <el-form-item label="壁纸类型" prop="type">
        <el-select 
          v-model="formData.type" 
          placeholder="请选择壁纸类型" 
          style="width: 100%"
          @change="handleTypeChange"
        >
          <el-option label="手机壁纸" :value="1" />
          <el-option label="平板壁纸" :value="2" />
          <el-option label="头像" :value="3" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="所属分类" prop="categoryId">
        <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%">
          <el-option 
            v-for="category in categories" 
            :key="category.id" 
            :label="category.name" 
            :value="category.id" 
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="描述">
        <el-input 
          v-model="formData.description" 
          type="textarea" 
          :rows="3" 
          placeholder="请输入作品描述（可选）" 
        />
      </el-form-item>
      
      <el-form-item label="壁纸图片" prop="wallpapers">
        <div class="upload-section">
          <div class="upload-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>{{ currentTypeTip }}</span>
          </div>
          
          <div class="image-grid">
            <div 
              v-for="(img, index) in formData.wallpapers" 
              :key="index"
              class="image-item"
            >
              <el-image 
                :src="img.url" 
                fit="cover" 
                style="width: 100%; height: 100%"
                :preview-src-list="formData.wallpapers.map(w => w.url)"
                :initial-index="index"
              />
              <div class="image-overlay">
                <div class="image-info">
                  <span>{{ img.width }}×{{ img.height }}</span>
                </div>
                <el-button 
                  type="danger" 
                  :icon="Delete" 
                  circle 
                  size="small"
                  @click="removeImage(index)"
                />
              </div>
            </div>
            
            <el-upload
              v-if="formData.wallpapers.length < 6 && formData.type"
              class="upload-btn"
              action="#"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :http-request="handleUpload"
              accept="image/*"
            >
              <div class="upload-content">
                <el-icon><Plus /></el-icon>
                <span>上传图片</span>
                <span class="count-text">{{ formData.wallpapers.length }}/6</span>
              </div>
            </el-upload>
          </div>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleConfirm" :loading="loading">确定</el-button>
    </template>
    </el-dialog>

    <ImageCropper
      v-if="showCropper"
      :image-url="cropperData.imageUrl"
      :image-info="cropperData.imageInfo"
      :target-width="cropperData.targetWidth"
      :target-height="cropperData.targetHeight"
      @close="closeCropper"
      @confirm="handleCropConfirm"
    />
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, InfoFilled } from '@element-plus/icons-vue'
import { getCategoryList } from '@/api/category'
import { uploadCover } from '@/api/category'
import ImageCropper from './ImageCropper.vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '添加壁纸'
  },
  data: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const formRef = ref(null)
const loading = ref(false)
const categories = ref([])
const uploading = ref(false)

const typeConfig = {
  1: {
    name: '手机壁纸',
    tip: '建议比例：9:20，大小不超过5MB',
    width: 9,
    height: 20,
    maxSize: 5 * 1024 * 1024,
    maxWidth: 1080
  },
  2: {
    name: '平板壁纸',
    tip: '建议比例：16:21，大小不超过8MB',
    width: 16,
    height: 21,
    maxSize: 8 * 1024 * 1024,
    maxWidth: 1440
  },
  3: {
    name: '头像',
    tip: '建议比例：1:1，大小不超过2MB',
    width: 1,
    height: 1,
    maxSize: 2 * 1024 * 1024,
    maxWidth: 1024
  }
}

const formData = ref({
  title: '',
  type: null,
  categoryId: null,
  description: '',
  wallpapers: []
})

const showCropper = ref(false)
const cropperData = ref({
  imageUrl: '',
  imageInfo: {},
  targetWidth: 0,
  targetHeight: 0
})

const currentTypeTip = computed(() => {
  if (!formData.value.type) {
    return '请先选择壁纸类型'
  }
  return typeConfig[formData.value.type].tip
})

const formRules = {
  title: [
    { required: true, message: '请输入作品标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择壁纸类型', trigger: 'change' }
  ],
  categoryId: [
    { required: true, message: '请选择所属分类', trigger: 'change' }
  ],
  wallpapers: [
    { 
      required: true, 
      validator: (rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('请至少上传一张壁纸图片'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
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

const handleTypeChange = () => {
  formData.value.wallpapers = []
}

const beforeUpload = (file) => {
  if (!formData.value.type) {
    ElMessage.warning('请先选择壁纸类型')
    return false
  }

  const isImage = file.type.startsWith('image/')
  const config = typeConfig[formData.value.type]
  const isLtMaxSize = file.size <= config.maxSize

  if (!isImage) {
    ElMessage.error('请上传图片格式文件！')
    return false
  }
  if (!isLtMaxSize) {
    const maxSizeMB = (config.maxSize / 1024 / 1024).toFixed(0)
    ElMessage.error(`上传图片大小不能超过${maxSizeMB}MB！`)
    return false
  }
  
  return true
}

const handleUpload = async (options) => {
  const file = options.file
  
  const img = new Image()
  img.src = URL.createObjectURL(file)
  
  await new Promise((resolve) => {
    img.onload = resolve
  })
  
  const config = typeConfig[formData.value.type]
  const imageRatio = img.width / img.height
  const targetRatio = config.width / config.height
  const ratioDiff = Math.abs(imageRatio - targetRatio) / targetRatio
  
  const needsCrop = ratioDiff > 0.1
  
  if (needsCrop) {
    ElMessageBox.confirm(
      `图片比例为${imageRatio.toFixed(2)}，建议比例为${targetRatio.toFixed(2)}，是否进行裁剪？`,
      '比例不符',
      {
        confirmButtonText: '去裁剪',
        cancelButtonText: '放弃',
        type: 'warning'
      }
    ).then(() => {
      cropperData.value = {
        imageUrl: img.src,
        imageInfo: {
          width: img.width,
          height: img.height
        },
        targetWidth: config.width,
        targetHeight: config.height
      }
      showCropper.value = true
    }).catch(() => {
      ElMessage.info('已取消上传')
    })
  } else {
    await uploadImage(file, img.width, img.height, false)
  }
}

const uploadImage = async (file, width, height, isCropped) => {
  try {
    uploading.value = true
    
    const res = await uploadCover(file)
    if (res.code === 200) {
      formData.value.wallpapers.push({
        url: res.data.url,
        thumbnailUrl: res.data.thumbnailUrl || res.data.url,
        width: width,
        height: height,
        fileFormat: file.type.split('/')[1].toUpperCase(),
        fileSize: file.size,
        isCropped: isCropped
      })
      ElMessage.success('上传成功')
    }
  } catch (error) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

const handleCropConfirm = async (cropInfo) => {
  await uploadImage(cropInfo.file, cropInfo.width, cropInfo.height, true)
  closeCropper()
}

const closeCropper = () => {
  showCropper.value = false
}

const removeImage = (index) => {
  ElMessageBox.confirm('确定要删除这张图片吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    formData.value.wallpapers.splice(index, 1)
    ElMessage.success('已删除')
  }).catch(() => {})
}

const resetForm = () => {
  formRef.value?.resetFields()
  formData.value = {
    title: '',
    type: null,
    categoryId: null,
    description: '',
    wallpapers: []
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
    
    const submitData = {
      title: formData.value.title,
      type: formData.value.type,
      categoryId: formData.value.categoryId,
      description: formData.value.description,
      wallpapers: formData.value.wallpapers.map(item => ({...item, imageWidth: item.width, imageHeight: item.height}))
    }
    
    emit('confirm', submitData)
  } catch (error) {
    ElMessage.error('表单填写有误，请检查！')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCategories()
})

defineExpose({
  resetForm
})
</script>

<style scoped lang="scss">
.upload-section {
  width: 100%;
  
  .upload-tip {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    padding: 8px 12px;
    background-color: #f4f4f5;
    border-radius: 4px;
    color: #909399;
    font-size: 13px;
  }
  
  .image-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 12px;
  }
  
  .image-item {
    position: relative;
    width: 100%;
    padding-bottom: 100%;
    border-radius: 4px;
    overflow: hidden;
    border: 1px solid #dcdfe6;
    
    .el-image {
      position: absolute;
      top: 0;
      left: 0;
    }
    
    .image-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      padding: 8px;
      opacity: 0;
      transition: opacity 0.3s;
      
      &:hover {
        opacity: 1;
      }
      
      .image-info {
        color: #fff;
        font-size: 12px;
        background: rgba(0, 0, 0, 0.6);
        padding: 2px 6px;
        border-radius: 2px;
        align-self: flex-start;
      }
    }
  }
  
  .upload-btn {
    :deep(.el-upload) {
      width: 100%;
      padding-bottom: 100%;
      border: 2px dashed #dcdfe6;
      border-radius: 4px;
      cursor: pointer;
      position: relative;
      transition: border-color 0.3s;
      
      &:hover {
        border-color: #409eff;
      }
    }
    
    .upload-content {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      color: #909399;
      
      .el-icon {
        font-size: 32px;
      }
      
      .count-text {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}
</style>
