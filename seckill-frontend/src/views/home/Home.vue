<template>
  <div class="home-container">
    <div class="bg-shape shape-1"></div>
    <div class="bg-shape shape-2"></div>
    <div class="bg-shape shape-3"></div>

    <el-header class="glass-header">
      <span class="logo">🔥 极速秒杀商城</span>
      <div class="user-info">
        <el-dropdown @command="handleCommand">
          <span class="user-dropdown">
            {{ username }}<el-icon class="el-icon--right"><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu class="custom-dropdown">
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="orders">我的订单</el-dropdown-item>
              <el-dropdown-item command="products" divided>商品列表</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-main class="main-content">
      <el-carousel :interval="4000" type="card" height="280px" class="carousel">
        <el-carousel-item v-for="(item, index) in banners" :key="index">
          <div class="banner-card" :style="{ background: item.bg }">
            <h3 class="banner-text">{{ item.text }}</h3>
            <p class="banner-subtext">{{ item.subtext }}</p>
          </div>
        </el-carousel-item>
      </el-carousel>

      <el-row :gutter="20" class="quick-entry">
        <el-col :span="6" v-for="entry in quickEntries" :key="entry.name">
          <div class="glass-card entry-card" @click="handleEntryClick(entry.path)">
            <div class="icon-wrapper" :style="{ background: entry.color + '20', color: entry.color }">
              <el-icon :size="28"><component :is="entry.icon" /></el-icon>
            </div>
            <span class="entry-name">{{ entry.name }}</span>
          </div>
        </el-col>
      </el-row>

      <div class="section-title">
        <h2 class="glow-text">🔥 限时秒杀 / HOT</h2>
        <el-button link class="more-btn" @click="goToProductList">查看更多 <el-icon><ArrowRight /></el-icon></el-button>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="8" v-for="item in hotProducts" :key="item.id">
          <div class="glass-card product-card" @click="goToProductList">
            <div class="product-image-placeholder" :style="{ background: item.bg }">
              <span class="mock-img-text">{{ item.name.substring(0, 4) }}</span>
            </div>
            <div class="product-info">
              <span class="product-name">{{ item.name }}</span>
              <div class="product-price">
                <span class="price-symbol">￥</span>
                <span class="seckill-price">{{ item.seckillPrice }}</span>
                <span class="old-price">￥{{ item.price }}</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, ArrowRight, ShoppingCart, Tickets, Timer, Collection } from '@element-plus/icons-vue'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '用户')

// 轮播图数据 (改为 CSS 渐变，彻底解决图片裂开问题)
const banners = ref([
  { bg: 'linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%)', text: '双十一提前抢', subtext: '爆款直降 不玩套路' },
  { bg: 'linear-gradient(135deg, #3b82f6 0%, #2dd4bf 100%)', text: '数码神券发放', subtext: '满 3999 减 500' },
  { bg: 'linear-gradient(135deg, #f59e0b 0%, #ef4444 100%)', text: '全场免息专场', subtext: '最高尊享 24 期免息' }
])


// 快捷入口
// src/views/home/Home.vue (局部修改)
const quickEntries = ref([
  { name: '商品列表', icon: 'ShoppingCart', color: '#38bdf8', path: '/products' },
  // 修改此处：将路径改为 /seckill
  { name: '限时秒杀', icon: 'Timer', color: '#f472b6', path: '/seckill' }, 
  { name: '领券中心', icon: 'Tickets', color: '#34d399', path: '/coupons' },
  { name: '我的订单', icon: 'Collection', color: '#fbbf24', path: '/orders' }
])

const handleEntryClick = (path) => {
  if(path === '/coupons' || path === '/orders') {
    ElMessage.info('模块开发中，敬请期待！')
    return
  }
  router.push(path)  // 现在会正确跳转到 /products 或 /seckill
}

const goToProductList = () => {
  router.push('/products')  // 热门商品点击后进入普通商品列表
}


// 热门秒杀商品 (增加专属渐变背景)
const hotProducts = ref([
  { id: 1, name: 'iPhone 15 Pro Max', price: 9999, seckillPrice: 7999, bg: 'linear-gradient(135deg, #475569 0%, #1e293b 100%)' },
  { id: 2, name: 'MacBook Air M2', price: 8999, seckillPrice: 6999, bg: 'linear-gradient(135deg, #64748b 0%, #334155 100%)' },
  { id: 3, name: 'iPad Pro 11英寸', price: 6999, seckillPrice: 5499, bg: 'linear-gradient(135deg, #94a3b8 0%, #475569 100%)' }
])

// 可以添加一个专门的秒杀跳转函数
const goToSeckillList = () => {
  router.push('/seckill')
}

const handleCommand = (command) => {
  switch(command) {
    case 'profile':
      ElMessage.info('个人中心开发中...')
      break
    case 'orders':
      ElMessage.info('订单页面开发中...')
      break
    case 'products':
      router.push('/products')
      break
    case 'logout':
      handleLogout()
      break
  }
}


const handleLogout = () => {
  localStorage.removeItem('seckill_token')
  localStorage.removeItem('username')
  router.push('/login')
}

onMounted(() => {
  const token = localStorage.getItem('seckill_token')
  if (!token) {
    router.push('/login')
  }
})
</script>

<style scoped>
/* 容器基础设置 */
.home-container {
  min-height: 100vh;
  background-color: #0f172a;
  position: relative;
  overflow-x: hidden;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', sans-serif;
}

/* --- 动态极光背景 --- */
.bg-shape {
  position: fixed;
  border-radius: 50%;
  filter: blur(90px);
  z-index: 0;
  animation: float 25s infinite ease-in-out alternate;
}
.shape-1 {
  width: 500px; height: 500px;
  background: rgba(236, 72, 153, 0.25);
  top: -10%; left: -10%;
}
.shape-2 {
  width: 600px; height: 600px;
  background: rgba(139, 92, 246, 0.25);
  bottom: -20%; right: -10%;
  animation-delay: -5s;
}
.shape-3 {
  width: 400px; height: 400px;
  background: rgba(56, 189, 248, 0.2);
  top: 40%; left: 30%;
  animation-delay: -10s;
}

@keyframes float {
  0% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(100px, 50px) scale(1.1); }
  100% { transform: translate(-50px, 100px) scale(0.9); }
}

/* --- 顶部导航栏 --- */
.glass-header {
  position: fixed;
  top: 0; left: 0; right: 0;
  height: 64px;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  z-index: 1000;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 22px;
  font-weight: 800;
  background: linear-gradient(to right, #ec4899, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 1px;
}

.user-dropdown {
  cursor: pointer;
  color: #e2e8f0;
  display: flex;
  align-items: center;
  gap: 5px;
  font-weight: 500;
  transition: color 0.3s;
}
.user-dropdown:hover {
  color: #f472b6;
}

/* --- 主体内容区 --- */
.main-content {
  margin-top: 84px;
  padding: 20px 40px 60px;
  position: relative;
  z-index: 1;
}

/* --- 轮播图 --- */
.carousel {
  margin-bottom: 40px;
}
.banner-card {
  width: 100%;
  height: 100%;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #fff;
  box-shadow: 0 10px 30px rgba(0,0,0,0.3);
}
.banner-text {
  font-size: 36px;
  font-weight: 900;
  margin: 0 0 10px 0;
  letter-spacing: 2px;
  text-shadow: 0 4px 10px rgba(0,0,0,0.2);
}
.banner-subtext {
  font-size: 18px;
  margin: 0;
  opacity: 0.9;
}

/* --- 玻璃卡片公共样式 --- */
.glass-card {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

/* --- 快捷入口 --- */
.quick-entry {
  margin-bottom: 50px;
}
.entry-card {
  text-align: center;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  padding: 30px 20px;
}
.entry-card:hover {
  transform: translateY(-8px);
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.2);
  box-shadow: 0 20px 40px -10px rgba(0, 0, 0, 0.5);
}
.icon-wrapper {
  width: 60px;
  height: 60px;
  border-radius: 18px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.entry-name {
  color: #e2e8f0;
  font-weight: 600;
  font-size: 15px;
}

/* --- 标题区域 --- */
.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}
.glow-text {
  font-size: 24px;
  color: #fff;
  margin: 0;
  font-weight: 800;
  text-shadow: 0 0 20px rgba(236, 72, 153, 0.5);
}
.more-btn {
  color: #94a3b8 !important;
  font-size: 14px;
}
.more-btn:hover {
  color: #f472b6 !important;
}

/* --- 商品卡片 --- */
.product-card {
  cursor: pointer;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.product-card:hover {
  transform: translateY(-8px);
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(236, 72, 153, 0.3);
  box-shadow: 0 20px 40px -10px rgba(0, 0, 0, 0.5), 
              0 0 20px rgba(139, 92, 246, 0.2);
}
.product-image-placeholder {
  width: 100%;
  height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.mock-img-text {
  font-size: 32px;
  font-weight: 900;
  color: rgba(255, 255, 255, 0.2);
  letter-spacing: 2px;
}
.product-info {
  padding: 20px;
}
.product-name {
  font-size: 17px;
  font-weight: 600;
  color: #f8fafc;
  display: block;
  margin-bottom: 12px;
}
.product-price {
  display: flex;
  align-items: baseline;
}
.price-symbol {
  color: #f472b6;
  font-size: 14px;
  font-weight: bold;
}
.seckill-price {
  color: #f472b6;
  font-size: 26px;
  font-weight: 800;
  margin-right: 10px;
}
.old-price {
  color: #64748b;
  text-decoration: line-through;
  font-size: 13px;
}
</style>