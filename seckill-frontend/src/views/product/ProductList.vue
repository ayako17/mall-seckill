<template>
  <div class="product-list-container">
    <div class="bg-shape shape-1"></div>
    <div class="bg-shape shape-2"></div>

    <el-container class="layout-container">
      <el-header class="glass-header">
        <div class="header-left" @click="router.push('/home')">
          <el-icon class="back-icon"><ArrowLeft /></el-icon>
          <span class="logo">极速秒杀 / <small>NORMAL PROD</small></span>
        </div>
        <div class="user-info">{{ username }}</div>
      </el-header>

      <el-main class="main-content">
        <div class="page-title-section">
          <h2 class="glow-title">全部商品 · 品质精选</h2>
          <p class="sub-title">海量好物，为您严选</p>
        </div>

        <el-row :gutter="25">
          <el-col 
            :xs="24" :sm="12" :md="8" :lg="6" 
            v-for="product in products" 
            :key="product.id"
          >
            <div class="glass-card product-card">
              <div class="product-cover" :style="!product.mainImage ? getRandomGradient(product.id) : ''">
                <img v-if="product.mainImage" :src="product.mainImage" class="real-img" alt="商品主图">
              </div>

              <div class="product-detail">
                <h3 class="product-name">{{ product.name }}</h3>
                <p class="product-desc">{{ product.description || '暂无描述' }}</p>
                
                <div class="price-row">
                  <div class="current-price">
                    <span class="symbol">￥</span>
                    <span class="amount">{{ product.price }}</span>
                  </div>
                </div>

                <el-button class="buy-btn" type="primary" @click="handleBuy(product.id)">
                  立即购买
                </el-button>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getNormalProductList } from '../../api/product'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '访客')
const products = ref([])

const fetchProducts = async () => {
  try {
    const res = await getNormalProductList()
    products.value = res.data || res // 兼容 axios 拦截器不同返回格式
  } catch (error) {
    ElMessage.error('获取商品列表失败')
  }
}

const handleBuy = (id) => {
  ElMessage.success('加入购物车成功！')
}

const getRandomGradient = (id) => {
  const gradients = [
    'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)',
    'linear-gradient(135deg, #3b82f6 0%, #2dd4bf 100%)',
    'linear-gradient(135deg, #f43f5e 0%, #fb923c 100%)'
  ]
  return { background: gradients[id % gradients.length] }
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
/* 样式与原先基本一致，新增了对真实图片的支持 */
.product-list-container { min-height: 100vh; background-color: #0f172a; color: #f8fafc; position: relative; overflow-x: hidden; }
.bg-shape { position: fixed; border-radius: 50%; filter: blur(80px); z-index: 0; }
.shape-1 { width: 500px; height: 500px; background: rgba(99, 102, 241, 0.15); top: -200px; right: -100px; }
.shape-2 { width: 400px; height: 400px; background: rgba(236, 72, 153, 0.1); bottom: -100px; left: -100px; }
.layout-container { position: relative; z-index: 1; }
.glass-header { background: rgba(15, 23, 42, 0.7); backdrop-filter: blur(12px); border-bottom: 1px solid rgba(255, 255, 255, 0.1); display: flex; justify-content: space-between; align-items: center; padding: 0 5%; }
.header-left { cursor: pointer; display: flex; align-items: center; gap: 10px; }
.logo { font-weight: 800; font-size: 18px; color: #fff; }
.logo small { color: #94a3b8; font-weight: 400; }
.main-content { padding: 40px 5%; }
.page-title-section { margin-bottom: 40px; text-align: left; }
.glow-title { font-size: 32px; font-weight: 800; margin-bottom: 8px; background: linear-gradient(to right, #fff, #94a3b8); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.sub-title { color: #64748b; font-size: 16px; }
.product-card { background: rgba(255, 255, 255, 0.03); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 20px; overflow: hidden; margin-bottom: 25px; transition: all 0.4s; }
.product-card:hover { transform: translateY(-10px); border-color: rgba(99, 102, 241, 0.5); box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4); }
.product-cover { height: 180px; position: relative; display: flex; justify-content: center; align-items: center; overflow: hidden; }
.real-img { width: 100%; height: 100%; object-fit: cover; }
.product-detail { padding: 20px; }
.product-name { font-size: 18px; font-weight: 700; margin-bottom: 8px; color: #fff; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.product-desc { font-size: 13px; color: #94a3b8; line-height: 1.5; height: 40px; margin-bottom: 15px; }
.price-row { display: flex; align-items: baseline; gap: 10px; margin-bottom: 20px; }
.current-price .symbol { color: #38bdf8; font-size: 14px; font-weight: 800; }
.current-price .amount { color: #38bdf8; font-size: 24px; font-weight: 800; }
.buy-btn { width: 100%; height: 45px; border-radius: 12px; border: none; font-weight: 700; background: #334155; transition: all 0.3s; }
.buy-btn:hover { background: #475569; }
</style>