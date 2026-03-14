<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2 class="title">{{ isLogin ? '用户登录' : '新用户注册' }}</h2>
      </template>

      <el-form :model="loginForm" label-width="0">
        <el-form-item>
          <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" />
        </el-form-item>
        
        <el-form-item>
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
        </el-form-item>

        <el-form-item v-if="!isLogin">
          <el-input v-model="loginForm.phone" placeholder="请输入手机号" prefix-icon="Iphone" />
        </el-form-item>

        <el-button type="primary" class="login-btn" @click="handleSubmit" :loading="loading">
          {{ isLogin ? '立 即 登 录' : '立 即 注 册' }}
        </el-button>
        
        <div class="footer-links">
          <el-link type="info" @click="isLogin = !isLogin">
            {{ isLogin ? '没有账号？去注册' : '已有账号？去登录' }}
          </el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { login, register } from '../../api/user' // 引入之前封装的API
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isLogin = ref(true)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  phone: ''
})

const handleSubmit = async () => {
  if (!loginForm.username || !loginForm.password) {
    return ElMessage.warning('请填写用户名 and 密码')
  }

  loading.value = true // 注意：这里之前代码可能有 typo，应该是 loading.value
  try {
    if (isLogin.value) {
      const res = await login(loginForm.username, loginForm.password)
      ElMessage.success('登录成功')
      
      // 现在这里就不会报错了！
      router.push('/home') 
    } else {
      // 注册逻辑...
      const res = await register({
        username: loginForm.username,
        password: loginForm.password,
        phone: loginForm.phone
      })
      ElMessage.success(res)
      isLogin.value = true
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1890ff 0%, #722ed1 100%);
}
.login-card {
  width: 400px;
  border-radius: 12px;
}
.title {
  text-align: center;
  margin: 0;
  color: #333;
}
.login-btn {
  width: 100%;
  margin-top: 10px;
}
.footer-links {
  margin-top: 15px;
  text-align: right;
}
</style>