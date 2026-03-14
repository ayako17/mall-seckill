import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '',
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('seckill_token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    console.log('请求:', config.method.toUpperCase(), config.url, config.data)
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    console.log('响应:', response.data)
    return response.data
  },
  error => {
    console.error('错误:', error.response || error)
    
    if (error.response) {
      // 获取后端返回的错误信息
      let errorMsg = '请求失败'
      
      if (typeof error.response.data === 'string') {
        // 如果返回的是字符串
        errorMsg = error.response.data
      } else if (error.response.data && error.response.data.message) {
        // 如果返回的是 { message: '...' }
        errorMsg = error.response.data.message
      } else if (error.response.data && error.response.data.error) {
        // 如果返回的是 { error: '...' }
        errorMsg = error.response.data.error
      } else {
        // 默认错误信息
        errorMsg = `请求失败 (${error.response.status})`
      }
      
      ElMessage.error(errorMsg)
    } else if (error.message === 'Network Error') {
      ElMessage.error('网络连接失败，请检查后端服务是否启动')
    } else {
      ElMessage.error(error.message || '系统内部错误，请稍后再试！')
    }
    
    return Promise.reject(error)
  }
)

export default service