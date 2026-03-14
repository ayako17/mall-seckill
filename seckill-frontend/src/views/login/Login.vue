<template>
  <div class="login-container">
    <div class="bg-shape shape-1"></div>
    <div class="bg-shape shape-2"></div>
    <div class="bg-shape shape-3"></div>

    <div class="glass-card">
      <div class="card-header">
        <h2 class="title">{{ isLogin ? 'WELCOME BACK' : 'JOIN US' }}</h2>
        <p class="subtitle">{{ isLogin ? '登录极速秒杀商城，抢购属于你的心动好物' : '注册成为新用户，开启秒杀之旅' }}</p>
      </div>

      <el-form :model="loginForm" label-width="0" class="custom-form">
        <el-form-item>
          <el-input 
            v-model="loginForm.username" 
            placeholder="Username / 用户名" 
            prefix-icon="User" 
            class="glass-input"
          />
        </el-form-item>
        
        <el-form-item>
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="Password / 密码" 
            prefix-icon="Lock" 
            show-password 
            class="glass-input"
          />
        </el-form-item>

        <el-form-item v-if="!isLogin" class="slide-fade-enter-active">
          <el-input 
            v-model="loginForm.phone" 
            placeholder="Phone / 手机号码" 
            prefix-icon="Iphone" 
            class="glass-input"
          />
        </el-form-item>

        <el-button type="primary" class="login-btn" @click="handleSubmit" :loading="loading">
          <span class="btn-text">{{ isLogin ? '立 即 登 录' : '立 即 注 册' }}</span>
        </el-button>
        
        <div class="footer-links">
          <span class="toggle-text">
            {{ isLogin ? "Don't have an account?" : "Already have an account?" }}
          </span>
          <el-link :underline="false" class="toggle-link" @click="toggleMode">
            {{ isLogin ? '去注册' : '去登录' }}
          </el-link>
        </div>
      </el-form>
    </div>
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

// 切换登录/注册模式时清空表单
const toggleMode = () => {
  isLogin.value = !isLogin.value
  loginForm.username = ''
  loginForm.password = ''
  loginForm.phone = ''
}

const handleSubmit = async () => {
  if (!loginForm.username || !loginForm.password) {
    return ElMessage.warning('请填写用户名和密码')
  }
  
  if (!isLogin.value && !loginForm.phone) {
    return ElMessage.warning('请填写手机号码')
  }

  loading.value = true
  try {
    if (isLogin.value) {
      // 登录
      console.log('发送登录请求:', loginForm.username, loginForm.password)
      const res = await login(loginForm.username, loginForm.password)
      console.log('登录响应:', res)
      
      if (res && res.token) {
        localStorage.setItem('seckill_token', res.token)
        localStorage.setItem('userId', res.userId)
        localStorage.setItem('username', res.username)
        localStorage.setItem('phone', res.phone)
        
        ElMessage.success('登录成功')
        setTimeout(() => {
          router.push('/home')
        }, 500)
      } else {
        ElMessage.error('登录失败：响应数据格式错误')
      }
    } else {
      // 注册
      console.log('发送注册请求:', loginForm)
      const res = await register({
        username: loginForm.username,
        password: loginForm.password,
        phone: loginForm.phone
      })
      console.log('注册响应:', res)
      
      if (res && res.userId) {
        ElMessage.success('注册成功，请登录')
        isLogin.value = true
        loginForm.username = ''
        loginForm.password = ''
        loginForm.phone = ''
      }
    }
  } catch (error) {
    console.error('操作失败:', error)
    // 错误已经在拦截器中处理，这里不需要再显示
    // 但可以添加调试信息
    if (error.response) {
      console.log('错误状态码:', error.response.status)
      console.log('错误数据:', error.response.data)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 样式保持不变 */
.login-container {
  position: relative;
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #0f172a;
  overflow: hidden;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 0;
  animation: float 20s infinite ease-in-out alternate;
}

.shape-1 {
  width: 500px;
  height: 500px;
  background: rgba(236, 72, 153, 0.4);
  top: -10%;
  left: -10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 600px;
  height: 600px;
  background: rgba(139, 92, 246, 0.4);
  bottom: -20%;
  right: -10%;
  animation-delay: -5s;
}

.shape-3 {
  width: 400px;
  height: 400px;
  background: rgba(56, 189, 248, 0.3);
  bottom: 20%;
  left: 20%;
  animation-delay: -10s;
}

@keyframes float {
  0% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(100px, 50px) scale(1.1); }
  100% { transform: translate(-50px, 100px) scale(0.9); }
}

.glass-card {
  position: relative;
  z-index: 1;
  width: 420px;
  padding: 50px 40px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  transition: all 0.3s ease;
}

.card-header {
  text-align: center;
  margin-bottom: 40px;
}

.title {
  font-size: 32px;
  font-weight: 800;
  color: #ffffff;
  margin: 0 0 10px 0;
  letter-spacing: 2px;
  background: linear-gradient(to right, #fff, #a5b4fc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  font-size: 14px;
  color: #94a3b8;
  margin: 0;
}

.custom-form {
  margin-top: 20px;
}

:deep(.glass-input .el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.08) !important;
  box-shadow: none !important;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 8px 15px;
  transition: all 0.3s ease;
}

:deep(.glass-input .el-input__wrapper.is-focus) {
  background-color: rgba(255, 255, 255, 0.15) !important;
  border-color: #ec4899;
  box-shadow: 0 0 15px rgba(236, 72, 153, 0.3) !important;
}

:deep(.glass-input .el-input__inner) {
  color: #ffffff !important;
  height: 35px;
}

:deep(.glass-input .el-input__inner::placeholder) {
  color: #64748b !important;
}

:deep(.el-input__prefix-inner .el-icon) {
  color: #94a3b8;
  font-size: 18px;
}

.login-btn {
  width: 100%;
  height: 50px;
  margin-top: 15px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  border: none;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 1px;
  overflow: hidden;
  position: relative;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -10px rgba(236, 72, 153, 0.8);
}

.btn-text {
  position: relative;
  z-index: 1;
}

.footer-links {
  margin-top: 25px;
  text-align: center;
  font-size: 14px;
}

.toggle-text {
  color: #94a3b8;
  margin-right: 8px;
}

.toggle-link {
  color: #ec4899;
  font-weight: 600;
  transition: color 0.3s;
  cursor: pointer;
}

.toggle-link:hover {
  color: #f472b6;
}

.slide-fade-enter-active {
  transition: all 0.4s ease-out;
}
.slide-fade-enter-from {
  transform: translateY(-15px);
  opacity: 0;
}
</style>