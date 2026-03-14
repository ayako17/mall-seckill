import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // 当请求路径以 /api/users 开头时，转发到用户服务 (8080)
      '/api/users': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 当请求路径以 /api/products 开头时，转发到商品服务 (8081)
      '/api/products': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      // 后续如果加上库存和订单服务，也是在这里加代理规则！
    }
  }
})