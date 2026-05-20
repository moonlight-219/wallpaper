<template>
  <el-drawer
    v-model="visible"
    title="作品集详情"
    size="50%"
    @close="handleClose"
  >
    <div v-if="detail" class="detail-content">
      <div class="detail-section">
        <h3>作品集信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">标题：</span>
            <span class="value">{{ detail.title }}</span>
          </div>
          <div class="info-item">
            <span class="label">类型：</span>
            <el-tag :type="getTypeStyle(detail.type)">{{ getTypeValue(detail.type) }}</el-tag>
          </div>
          <div class="info-item">
            <span class="label">审核状态：</span>
            <el-tag :type="getStatusStyle(detail.status)">{{ getStatusValue(detail.status) }}</el-tag>
          </div>
          <div class="info-item">
            <span class="label">点赞数：</span>
            <span class="value">{{ detail.likes }}</span>
          </div>
          <div class="info-item">
            <span class="label">收藏数：</span>
            <span class="value">{{ detail.collects }}</span>
          </div>
          <div class="info-item">
            <span class="label">发布时间：</span>
            <span class="value">{{ formatTime(detail.publishTime) }}</span>
          </div>
        </div>
      </div>

      <div class="detail-section">
        <h3>创作者信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">用户名：</span>
            <span class="value">{{ detail.author?.name || '未知' }}</span>
          </div>
          <div class="info-item">
            <span class="label">头像：</span>
            <el-avatar :src="detail.author?.avatar" :size="50" />
          </div>
        </div>
      </div>

      <div class="detail-section">
        <h3>壁纸列表</h3>
        <div class="wallpaper-grid">
          <div 
            v-for="(wallpaper, index) in detail.wallpapers" 
            :key="index"
            class="wallpaper-item"
          >
            <el-image 
              :src="wallpaper.url" 
              :preview-src-list="detail.wallpapers.map(w => w.url)"
              :initial-index="index"
              fit="cover"
              style="width: 100%; height: 100%"
            />
            <div class="wallpaper-info">
              <span>{{ wallpaper.imageWidth }}×{{ wallpaper.imageHeight }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { computed } from 'vue'
import { formatTime } from '@/utils'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  detail: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'close'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const handleClose = () => {
  emit('close')
}

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
</script>

<style scoped lang="scss">
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
</style>
