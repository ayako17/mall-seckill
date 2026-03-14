<template>
  <div class="home-container">
    <el-header class="header">
      <span class="logo">🔥 极速秒杀商城</span>
      <div class="user-info">
        <el-dropdown @command="handleCommand">
          <span class="user-dropdown">
            {{ username }}<el-icon class="el-icon--right"><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="orders">我的订单</el-dropdown-item>
              <el-dropdown-item command="products" divided>商品列表</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-main>
      <!-- 首页轮播图 -->
      <el-carousel :interval="4000" type="card" height="300px" class="carousel">
        <el-carousel-item v-for="item in banners" :key="item">
          <img :src="item.image" class="banner-image" />
        </el-carousel-item>
      </el-carousel>

      <!-- 快捷入口 -->
      <el-row :gutter="20" class="quick-entry">
        <el-col :span="6" v-for="entry in quickEntries" :key="entry.name">
          <el-card class="entry-card" shadow="hover" @click="handleEntryClick(entry.path)">
            <el-icon :size="30" :color="entry.color"><component :is="entry.icon" /></el-icon>
            <span>{{ entry.name }}</span>
          </el-card>
        </el-col>
      </el-row>

      <!-- 秒杀预告 -->
      <div class="section-title">
        <h2>🔥 限时秒杀</h2>
        <el-button type="danger" link @click="goToProductList">查看更多 ></el-button>
      </div>
      <el-row :gutter="20">
        <el-col :span="8" v-for="item in hotProducts" :key="item.id">
          <el-card class="product-card" shadow="hover" @click="goToProductList">
            <img :src="item.image" class="product-image" />
            <div class="product-info">
              <span class="product-name">{{ item.name }}</span>
              <div class="product-price">
                <span class="seckill-price">￥{{ item.seckillPrice }}</span>
                <span class="old-price">￥{{ item.price }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, ShoppingCart, Tickets, Timer, Collection } from '@element-plus/icons-vue'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '用户')

// 轮播图数据
const banners = ref([
  { image: 'https://via.placeholder.com/800x300?text=秒杀活动1' },
  { image: 'https://via.placeholder.com/800x300?text=秒杀活动2' },
  { image: 'https://via.placeholder.com/800x300?text=秒杀活动3' }
])

// 快捷入口
const quickEntries = ref([
  { name: '商品列表', icon: 'ShoppingCart', color: '#409EFF', path: '/products' },
  { name: '限时秒杀', icon: 'Timer', color: '#F56C6C', path: '/products' },
  { name: '优惠券', icon: 'Tickets', color: '#67C23A', path: '/coupons' },
  { name: '我的订单', icon: 'Collection', color: '#E6A23C', path: '/orders' }
])

// 热门秒杀商品（预览）
const hotProducts = ref([
  { id: 1, name: 'iPhone 15 Pro', price: 9999, seckillPrice: 7999, image: 'https://via.placeholder.com/300x200?text=iPhone+15+Pro' },
  { id: 2, name: 'MacBook Air', price: 8999, seckillPrice: 6999, image: 'https://via.placeholder.com/300x200?text=MacBook+Air' },
  { id: 3, name: 'iPad Pro', price: 6999, seckillPrice: 5499, image: 'https://via.placeholder.com/300x200?text=iPad+Pro' }
])

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

const handleEntryClick = (path) => {
  router.push(path)
}

const goToProductList = () => {
  router.push('/products')
}

const handleLogout = () => {
  localStorage.removeItem('seckill_token')
  localStorage.removeItem('username')
  router.push('/login')
}

onMounted(() => {
  // 检查是否登录
  const token = localStorage.getItem('seckill_token')
  if (!token) {
    router.push('/login')
  }
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}
.header {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  padding: 0 20px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}
.logo {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}
.user-dropdown {
  cursor: pointer;
  color: #333;
  display: flex;
  align-items: center;
  gap: 5px;
}
.el-main {
  margin-top: 60px;
  padding: 20px;
}
.carousel {
  margin-bottom: 30px;
}
.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.quick-entry {
  margin-bottom: 40px;
}
.entry-card {
  text-align: center;
  cursor: pointer;
  transition: transform 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px;
}
.entry-card:hover {
  transform: translateY(-5px);
}
.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.product-card {
  cursor: pointer;
  margin-bottom: 20px;
  border-radius: 8px;
  transition: transform 0.3s;
}
.product-card:hover {
  transform: translateY(-5px);
}
.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  display: block;
}
.product-info {
  padding: 14px;
}
.product-name {
  font-size: 16px;
  font-weight: bold;
  display: block;
  margin-bottom: 10px;
}
.product-price {
  display: flex;
  align-items: center;
  gap: 10px;
}
.seckill-price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}
.old-price {
  color: #999;
  text-decoration: line-through;
  font-size: 14px;
}
</style>