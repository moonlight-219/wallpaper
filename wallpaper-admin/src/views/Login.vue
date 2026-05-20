<template>
  <div class="login-container">
    <el-card class="login-card" shadow="always">
      <div class="login-header">
        <h2>壁纸后台管理系统</h2>
        <p>管理员登录</p>
      </div>
      
      <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleLogin" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="用户名" 
            prefix-icon="User" 
            size="large"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="密码" 
            prefix-icon="Lock" 
            size="large"
            show-password
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            class="login-button" 
            :loading="loading" 
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = ref({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form.value)
    if (res.code === 200) {
      if (res.data.role !== 'admin') {
        ElMessage.error('只有管理员才能登录')
        loading.value = false
        return
      }
      userStore.setToken(res.data.token)
      userStore.setUserInfo({
        id: res.data.userId,
        username: res.data.username,
        nickname: res.data.nickname,
        avatar: res.data.avatar,
        role: res.data.role,
        isCreator: res.data.isCreator,
        followerCount: res.data.followerCount,
        workCount: res.data.workCount,
        bio: res.data.bio
      })
      ElMessage.success('登录成功')
      router.push('/')
    }
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-image: url('https://picsum.photos/1920/1080?random=1');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.login-card {
  width: 400px;
  padding: 40px;
  border-radius: 8px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
  
  h2 {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }
  
  p {
    font-size: 14px;
    color: #909399;
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 20px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
}
</style>
