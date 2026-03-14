import { createApp } from 'vue'
import App from './App.vue'

// 引入 Element Plus UI 框架及样式
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 引入 Pinia 状态管理
import { createPinia } from 'pinia'

// 这里预留引入 router (下一步我们建了路由表就会生效)
import router from './router' // 引入路由配置

const app = createApp(App)

// 全局注册插件
app.use(ElementPlus)
app.use(createPinia())
app.use(router) 

app.mount('#app')