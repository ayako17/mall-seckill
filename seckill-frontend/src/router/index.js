import { createRouter, createWebHistory } from 'vue-router'

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
    component: () => import('../views/product/ProductList.vue'),
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