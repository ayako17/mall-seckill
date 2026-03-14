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
import { login, register } from '../../api/user'
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
    return ElMessage.warning('请填写用户名和密码')
  }

  loading.value = true
  try {
    if (isLogin.value) {
      console.log('发送登录请求:', loginForm.username, loginForm.password)
      const res = await login(loginForm.username, loginForm.password)
      console.log('登录响应数据:', res) // 打印查看后端返回的数据结构
      
      // 根据你的后端实际返回的数据结构调整
      if (res) {
        // 假设后端返回的数据格式是 { code: 200, data: { token: 'xxx', username: 'xxx' } }
        // 或者是直接返回 { token: 'xxx', username: 'xxx' }
        
        // 存储token - 根据实际返回结构调整
        if (res.token) {
          localStorage.setItem('seckill_token', res.token)
        } else if (res.data && res.data.token) {
          localStorage.setItem('seckill_token', res.data.token)
        } else if (res.code === 200 && res.data) {
          // 如果返回的是标准响应格式
          localStorage.setItem('seckill_token', res.data.token || 'dummy_token')
        } else {
          // 如果登录成功但没有token（比如后端没返回），先设置一个测试token
          localStorage.setItem('seckill_token', 'test_token')
        }
        
        // 存储用户名
        if (res.username) {
          localStorage.setItem('username', res.username)
        } else if (res.data && res.data.username) {
          localStorage.setItem('username', res.data.username)
        } else {
          localStorage.setItem('username', loginForm.username)
        }
        
        ElMessage.success('登录成功')
        console.log('准备跳转到 /home')
        
        // 使用setTimeout确保消息显示后再跳转
        setTimeout(() => {
          router.push('/home').then(() => {
            console.log('跳转成功')
          }).catch(err => {
            console.error('跳转失败:', err)
          })
        }, 500)
      } else {
        ElMessage.error('登录失败：响应数据为空')
      }
    } else {
      // 注册逻辑
      const res = await register({
        username: loginForm.username,
        password: loginForm.password,
        phone: loginForm.phone
      })
      console.log('注册响应:', res)
      ElMessage.success('注册成功，请登录')
      isLogin.value = true
      // 清空表单
      loginForm.username = ''
      loginForm.password = ''
      loginForm.phone = ''
    }
  } catch (error) {
    console.error('登录/注册错误:', error)
    ElMessage.error(error.message || '操作失败，请重试')
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