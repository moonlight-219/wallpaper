<template>
  <el-dialog
    v-model="visible"
    title="裁剪图片"
    width="1100px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="cropper-container">
      <div class="cropper-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>拖动裁剪框调整区域，比例已锁定为 {{ targetWidth }}:{{ targetHeight }}</span>
      </div>

      <div class="cropper-main" ref="cropperMain">
        <div class="cropper-content">
          <div class="image-container" ref="imageContainer">
            <img 
              ref="imageRef"
              :src="imageUrl" 
              class="crop-image"
              @load="onImageLoad"
            />
            
            <div 
              class="crop-mask"
              v-if="imageLoaded"
            >
              <div class="mask-top" :style="{ height: cropRect.top + 'px' }"></div>
              <div class="mask-middle">
                <div class="mask-left" :style="{ width: cropRect.left + 'px' }"></div>
                <div 
                  class="crop-box"
                  :style="{
                    width: cropRect.width + 'px',
                    height: cropRect.height + 'px',
                    left: cropRect.left + 'px',
                    top: cropRect.top + 'px'
                  }"
                  @mousedown="startDrag"
                >
                  <div class="crop-border"></div>
                  <div class="crop-grid">
                    <div class="grid-line horizontal" style="top: 33.33%"></div>
                    <div class="grid-line horizontal" style="top: 66.66%"></div>
                    <div class="grid-line vertical" style="left: 33.33%"></div>
                    <div class="grid-line vertical" style="left: 66.66%"></div>
                  </div>
                  <div class="crop-handle top-left"></div>
                  <div class="crop-handle top-right"></div>
                  <div class="crop-handle bottom-left"></div>
                  <div class="crop-handle bottom-right"></div>
                </div>
                <div class="mask-right" :style="{ width: (displaySize.width - cropRect.left - cropRect.width) + 'px' }"></div>
              </div>
              <div class="mask-bottom" :style="{ height: (displaySize.height - cropRect.top - cropRect.height) + 'px' }"></div>
            </div>
          </div>
          
          <div class="preview-panel">
            <div class="preview-header">
              <span class="preview-title">实时预览</span>
              <span class="preview-size">{{ previewWidth }} × {{ previewHeight }}</span>
            </div>
            <div class="preview-container">
              <canvas ref="previewCanvas" class="preview-canvas"></canvas>
            </div>
            <div class="size-info">
              <div class="info-item">
                <span class="label">目标比例：</span>
                <span class="value">{{ targetWidth }}:{{ targetHeight }}</span>
              </div>
              <div class="info-item">
                <span class="label">裁剪尺寸：</span>
                <span class="value">{{ Math.round(cropRect.width * scale) }} × {{ Math.round(cropRect.height * scale) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="cropper-hint">
        <el-icon><Pointer /></el-icon>
        <span>拖动裁剪框调整裁剪区域，右侧实时预览裁剪效果</span>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleConfirm" :loading="loading">确认裁剪</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled, Pointer } from '@element-plus/icons-vue'

const props = defineProps({
  imageUrl: {
    type: String,
    required: true
  },
  imageInfo: {
    type: Object,
    required: true
  },
  targetWidth: {
    type: Number,
    required: true
  },
  targetHeight: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['close', 'confirm'])

const visible = ref(true)
const loading = ref(false)
const imageLoaded = ref(false)
const cropperMain = ref(null)
const imageContainer = ref(null)
const imageRef = ref(null)
const previewCanvas = ref(null)

const naturalSize = ref({ width: 0, height: 0 })
const displaySize = ref({ width: 0, height: 0 })
const cropRect = ref({ x: 0, y: 0, width: 0, height: 0, left: 0, top: 0 })
const scale = ref(1)
const previewWidth = ref(0)
const previewHeight = ref(0)

let isDragging = false
let dragStartX = 0
let dragStartY = 0
let dragStartLeft = 0
let dragStartTop = 0
let previewDebounceTimer = null

const targetRatio = computed(() => props.targetWidth / props.targetHeight)

const onImageLoad = () => {
  const img = imageRef.value
  if (!img) return
  
  naturalSize.value = {
    width: img.naturalWidth,
    height: img.naturalHeight
  }
  
  nextTick(() => {
    displaySize.value = {
      width: img.width,
      height: img.height
    }
    
    scale.value = naturalSize.value.width / displaySize.value.width
    
    imageLoaded.value = true
    
    initCropRect()
  })
}

const initCropRect = () => {
  const imgWidth = displaySize.value.width
  const imgHeight = displaySize.value.height
  const ratio = targetRatio.value
  const imgRatio = imgWidth / imgHeight
  
  let cropWidth, cropHeight
  
  if (imgRatio > ratio) {
    cropHeight = imgHeight
    cropWidth = cropHeight * ratio
  } else {
    cropWidth = imgWidth
    cropHeight = cropWidth / ratio
  }
  
  cropRect.value = {
    width: cropWidth,
    height: cropHeight,
    left: (imgWidth - cropWidth) / 2,
    top: (imgHeight - cropHeight) / 2
  }
  
  updatePreview()
}

const updatePreview = () => {
  if (!previewCanvas.value || !imageLoaded.value) return
  
  const canvas = previewCanvas.value
  const ctx = canvas.getContext('2d')
  
  const outputWidth = Math.round(cropRect.value.width * scale.value)
  const outputHeight = Math.round(cropRect.value.height * scale.value)
  
  previewWidth.value = outputWidth
  previewHeight.value = outputHeight
  
  let maxWidth = 300
  let previewScale = 1
  
  if (outputWidth > maxWidth) {
    previewScale = maxWidth / outputWidth
    canvas.width = maxWidth
    canvas.height = Math.round(outputHeight * previewScale)
  } else {
    canvas.width = outputWidth
    canvas.height = outputHeight
  }
  
  const img = new Image()
  img.crossOrigin = 'anonymous'
  img.src = props.imageUrl
  
  img.onload = () => {
    ctx.drawImage(
      img,
      cropRect.value.left * scale.value,
      cropRect.value.top * scale.value,
      cropRect.value.width * scale.value,
      cropRect.value.height * scale.value,
      0,
      0,
      canvas.width,
      canvas.height
    )
  }
}

const debouncedUpdatePreview = () => {
  if (previewDebounceTimer) {
    clearTimeout(previewDebounceTimer)
  }
  previewDebounceTimer = setTimeout(() => {
    updatePreview()
  }, 50)
}

watch(cropRect, () => {
  debouncedUpdatePreview()
}, { deep: true })

const startDrag = (e) => {
  isDragging = true
  dragStartX = e.clientX
  dragStartY = e.clientY
  dragStartLeft = cropRect.value.left
  dragStartTop = cropRect.value.top
  e.preventDefault()
  e.stopPropagation()
}

const handleDrag = (e) => {
  if (!isDragging) return
  
  const deltaX = e.clientX - dragStartX
  const deltaY = e.clientY - dragStartY
  
  let newLeft = dragStartLeft + deltaX
  let newTop = dragStartTop + deltaY
  
  const maxLeft = displaySize.value.width - cropRect.value.width
  const maxTop = displaySize.value.height - cropRect.value.height
  
  newLeft = Math.max(0, Math.min(maxLeft, newLeft))
  newTop = Math.max(0, Math.min(maxTop, newTop))
  
  cropRect.value = {
    ...cropRect.value,
    left: newLeft,
    top: newTop
  }
}

const stopDrag = () => {
  isDragging = false
}

const handleConfirm = () => {
  loading.value = true
  
  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')
  
  const outputWidth = Math.round(cropRect.value.width * scale.value)
  const outputHeight = Math.round(cropRect.value.height * scale.value)
  
  let maxWidth
  if (props.targetWidth === 9 && props.targetHeight === 20) {
    maxWidth = 1080
  } else if (props.targetWidth === 16 && props.targetHeight === 21) {
    maxWidth = 1440
  } else {
    maxWidth = 1024
  }
  
  let finalWidth = outputWidth
  let finalHeight = outputHeight
  
  if (outputWidth > maxWidth) {
    const resizeScale = maxWidth / outputWidth
    finalWidth = maxWidth
    finalHeight = Math.round(outputHeight * resizeScale)
  }
  
  canvas.width = finalWidth
  canvas.height = finalHeight
  
  const img = new Image()
  img.crossOrigin = 'anonymous'
  img.src = props.imageUrl
  
  img.onload = () => {
    ctx.drawImage(
      img,
      cropRect.value.left * scale.value,
      cropRect.value.top * scale.value,
      cropRect.value.width * scale.value,
      cropRect.value.height * scale.value,
      0,
      0,
      finalWidth,
      finalHeight
    )
    
    canvas.toBlob((blob) => {
      loading.value = false
      const file = new File([blob], 'cropped.jpg', { type: 'image/jpeg' })
      emit('confirm', {
        file,
        width: finalWidth,
        height: finalHeight
      })
    }, 'image/jpeg', 0.92)
  }
  
  img.onerror = () => {
    loading.value = false
    ElMessage.error('图片加载失败')
  }
}

const handleClose = () => {
  removeEventListeners()
  emit('close')
}

const setupEventListeners = () => {
  document.addEventListener('mousemove', handleDrag)
  document.addEventListener('mouseup', stopDrag)
}

const removeEventListeners = () => {
  document.removeEventListener('mousemove', handleDrag)
  document.removeEventListener('mouseup', stopDrag)
}

onMounted(() => {
  setupEventListeners()
})

onUnmounted(() => {
  removeEventListeners()
  if (previewDebounceTimer) {
    clearTimeout(previewDebounceTimer)
  }
})
</script>

<style scoped lang="scss">
.cropper-container {
  .cropper-tip {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;
    padding: 10px 12px;
    background-color: #fff7e6;
    border: 1px solid #ffd591;
    border-radius: 4px;
    color: #e6a23c;
    font-size: 13px;
  }
  
  .cropper-main {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
  }
  
  .cropper-content {
    display: flex;
    gap: 24px;
    align-items: flex-start;
  }
  
  .image-container {
    position: relative;
    display: inline-block;
    background-color: #f5f7fa;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    overflow: hidden;
    max-width: 100%;
  }
  
  .crop-image {
    display: block;
    max-width: 100%;
    max-height: 600px;
    width: auto;
    height: auto;
    object-fit: contain;
  }
  
  .crop-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    pointer-events: none;
  }
  
  .mask-top,
  .mask-bottom {
    position: absolute;
    left: 0;
    right: 0;
    background-color: rgba(0, 0, 0, 0.5);
  }
  
  .mask-top {
    top: 0;
  }
  
  .mask-bottom {
    bottom: 0;
  }
  
  .mask-middle {
    position: absolute;
    top: 0;
    bottom: 0;
    display: flex;
  }
  
  .mask-left,
  .mask-right {
    position: absolute;
    top: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
  }
  
  .mask-left {
    left: 0;
  }
  
  .mask-right {
    right: 0;
  }
  
  .crop-box {
    position: absolute;
    cursor: move;
    border: 2px solid #409eff;
    box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.3);
    pointer-events: auto;
  }
  
  .crop-border {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    border: 1px dashed rgba(255, 255, 255, 0.8);
  }
  
  .crop-grid {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    pointer-events: none;
  }
  
  .grid-line {
    position: absolute;
    background-color: rgba(255, 255, 255, 0.3);
  }
  
  .grid-line.horizontal {
    left: 0;
    right: 0;
    height: 1px;
  }
  
  .grid-line.vertical {
    top: 0;
    bottom: 0;
    width: 1px;
  }
  
  .crop-handle {
    position: absolute;
    width: 10px;
    height: 10px;
    background-color: #409eff;
    border: 2px solid #fff;
    border-radius: 2px;
  }
  
  .crop-handle.top-left {
    top: -6px;
    left: -6px;
    cursor: nwse-resize;
  }
  
  .crop-handle.top-right {
    top: -6px;
    right: -6px;
    cursor: nesw-resize;
  }
  
  .crop-handle.bottom-left {
    bottom: -6px;
    left: -6px;
    cursor: nesw-resize;
  }
  
  .crop-handle.bottom-right {
    bottom: -6px;
    right: -6px;
    cursor: nwse-resize;
  }
  
  .preview-panel {
    flex-shrink: 0;
    width: 320px;
    background-color: #fff;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    padding: 16px;
  }
  
  .preview-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    padding-bottom: 12px;
    border-bottom: 1px solid #ebeef5;
  }
  
  .preview-title {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
  }
  
  .preview-size {
    font-size: 12px;
    color: #909399;
  }
  
  .preview-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 200px;
    background-color: #f5f7fa;
    border-radius: 4px;
    margin-bottom: 16px;
    padding: 16px;
  }
  
  .preview-canvas {
    max-width: 100%;
    height: auto;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
  
  .size-info {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 12px 16px;
    background-color: #f4f4f5;
    border-radius: 4px;
  }
  
  .info-item {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: #606266;
    
    .label {
      color: #909399;
    }
    
    .value {
      font-weight: 500;
      color: #303133;
    }
  }
  
  .cropper-hint {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 16px;
    padding: 10px 12px;
    background-color: #f4f4f5;
    border-radius: 4px;
    color: #909399;
    font-size: 13px;
  }
}
</style>
