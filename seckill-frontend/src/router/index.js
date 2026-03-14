import { createRouter, createWebHistory } from 'vue-router'
// 修正导入路径 - 添加 product/ 目录
import ProductList from '@/views/product/ProductList.vue' // 普通商品列表
import SeckillList from '@/views/product/SeckillList.vue'   // 限时秒杀列表

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/Login.vue')
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/home/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/products',
    name: 'ProductList',
    component: ProductList,  // 直接使用导入的组件
    meta: { requiresAuth: true }
  },
  {
    path: '/seckill',
    name: 'SeckillList',
    component: SeckillList,  // 直接使用导入的组件
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('seckill_token')
  console.log('路由守卫 - 目标:', to.path, 'token:', token)
  
  if (to.meta.requiresAuth) {
    if (token) {
      next()
    } else {
      console.log('需要登录，跳转到登录页')
      next('/login')
    }
  } else {
    if (to.path === '/login' && token) {
      console.log('已登录访问登录页，跳转到首页')
      next('/home')
    } else {
      next()
    }
  }
})

export default router