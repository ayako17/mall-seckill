import axios from 'axios'
import { ElMessage } from 'element-plus'

// 1. 创建 axios 实例
const service = axios.create({
  baseURL: '', // 因为我们在 vite.config.js 配置了代理，这里留空即可
  timeout: 10000 // 请求超时时间 10 秒
})

// 2. 请求拦截器 (Request Interceptor)
service.interceptors.request.use(
  config => {
    // 【核心重点】如果后续实现了 Token，这里会自动把 Token 塞进请求头里，发给你的 Java 后端！
    const token = localStorage.getItem('seckill_token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 3. 响应拦截器 (Response Interceptor)
service.interceptors.response.use(
  response => {
    // 成功收到后端响应，直接剥离最外层 axios 包装，返回数据给页面
    return response.data
  },
  error => {
    // 全局统一的错误提示，比如后端挂了、500报错，都在这里弹窗提醒
    ElMessage.error(error.message || '系统内部错误，请稍后再试！')
    return Promise.reject(error)
  }
)

export default service