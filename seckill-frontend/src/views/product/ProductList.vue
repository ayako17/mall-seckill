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

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="3" animated />
        </div>

        <!-- 商品列表 -->
        <el-row :gutter="25" v-else>
          <el-col 
            :xs="24" :sm="12" :md="8" :lg="6" 
            v-for="product in products" 
            :key="product.id"
          >
            <div class="glass-card product-card">
              <div class="product-cover" :style="coverStyle(product)">
                <img 
                  v-if="product.mainImage" 
                  :src="product.mainImage" 
                  class="real-img" 
                  :alt="product.name"
                  @load="handleImageLoad(product.id)"
                  @error="handleImageError($event, product.id)"
                  :data-product-id="product.id"
                >
                <!-- 图片加载失败时显示的文字标识（可选） -->
                <span v-if="imageErrors.has(product.id)" class="fallback-text">
                  {{ product.name.substring(0, 2) }}
                </span>
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

        <!-- 空状态 -->
        <el-empty v-if="!loading && products.length === 0" description="暂无商品" />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getNormalProductList } from '../../api/product'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '访客')
const products = ref([])
const loading = ref(true)
const imageErrors = ref(new Set()) // 记录加载失败的图片ID

// 获取商品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await getNormalProductList()
    console.log('获取到的商品数据:', res)
    
    // 兼容 axios 拦截器不同返回格式
    if (Array.isArray(res)) {
      products.value = res
    } else if (res && res.data) {
      products.value = res.data
    } else {
      products.value = []
    }
    
    console.log('处理后的商品数据:', products.value)
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败，请稍后重试')
    
    // 开发环境使用模拟数据
    if (import.meta.env.DEV) {
      products.value = [
        { 
          id: 1, 
          name: 'Apple iPhone 15 Pro (256GB)', 
          mainImage: 'https://mall-seckill-oss.oss-cn-hangzhou.aliyuncs.com/images/products/iphone_15.png',
          price: 8999.00,
          description: 'A17 Pro芯片，钛金属设计'
        },
        { 
          id: 2, 
          name: 'MacBook Air M2', 
          mainImage: 'https://mall-seckill-oss.oss-cn-hangzhou.aliyuncs.com/images/products/macbook-air-m2.png',
          price: 8999.00,
          description: 'M2芯片，8核CPU'
        }
      ]
    }
  } finally {
    loading.value = false
  }
}

// 计算卡片封面样式
const coverStyle = (product) => {
  // 如果图片加载失败，使用渐变背景
  if (imageErrors.value.has(product.id)) {
    return getRandomGradient(product.id)
  }
  // 如果没有主图，也使用渐变背景
  if (!product.mainImage) {
    return getRandomGradient(product.id)
  }
  return {}
}

// 图片加载成功处理
const handleImageLoad = (productId) => {
  // 如果之前有错误记录，清除它
  if (imageErrors.value.has(productId)) {
    imageErrors.value.delete(productId)
  }
}

// 图片加载失败处理
const handleImageError = (event, productId) => {
  console.warn(`商品ID ${productId} 图片加载失败:`, event.target.src)
  
  // 记录该图片加载失败
  imageErrors.value.add(productId)
  
  // 隐藏图片元素
  event.target.style.display = 'none'
  
  // 显示错误提示（可选）
  ElMessage.warning({
    message: `商品图片加载失败，使用默认背景`,
    duration: 2000
  })
}

// 购买按钮处理
const handleBuy = (id) => {
  ElMessage.success('加入购物车成功！')
}

// 随机渐变背景
const getRandomGradient = (id) => {
  const gradients = [
    'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)',
    'linear-gradient(135deg, #3b82f6 0%, #2dd4bf 100%)',
    'linear-gradient(135deg, #f43f5e 0%, #fb923c 100%)',
    'linear-gradient(135deg, #8b5cf6 0%, #ec4899 100%)'
  ]
  return { background: gradients[id % gradients.length] }
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
/* 容器基础设置 */
.product-list-container {
  min-height: 100vh;
  background-color: #0f172a;
  color: #f8fafc;
  position: relative;
  overflow-x: hidden;
}

/* 动态背景 */
.bg-shape {
  position: fixed;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 0;
}
.shape-1 {
  width: 500px;
  height: 500px;
  background: rgba(99, 102, 241, 0.15);
  top: -200px;
  right: -100px;
}
.shape-2 {
  width: 400px;
  height: 400px;
  background: rgba(236, 72, 153, 0.1);
  bottom: -100px;
  left: -100px;
}

/* 布局容器 */
.layout-container {
  position: relative;
  z-index: 1;
}

/* 玻璃头栏 */
.glass-header {
  background: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 5%;
}
.header-left {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
}
.logo {
  font-weight: 800;
  font-size: 18px;
  color: #fff;
}
.logo small {
  color: #94a3b8;
  font-weight: 400;
}

/* 主体内容 */
.main-content {
  padding: 40px 5%;
}

/* 标题区域 */
.page-title-section {
  margin-bottom: 40px;
  text-align: left;
}
.glow-title {
  font-size: 32px;
  font-weight: 800;
  margin-bottom: 8px;
  background: linear-gradient(to right, #fff, #94a3b8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.sub-title {
  color: #64748b;
  font-size: 16px;
}

/* 加载状态 */
.loading-state {
  padding: 40px;
}

/* 商品卡片 */
.product-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 25px;
  transition: all 0.4s;
}
.product-card:hover {
  transform: translateY(-10px);
  border-color: rgba(99, 102, 241, 0.5);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
}

/* 商品封面 */
.product-cover {
  height: 180px;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

/* 真实图片样式 */
.real-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.real-img:hover {
  transform: scale(1.05);
}

/* 图片加载失败时的文字标识 */
.fallback-text {
  font-size: 48px;
  font-weight: 900;
  color: rgba(255, 255, 255, 0.2);
  letter-spacing: 2px;
  text-transform: uppercase;
}

/* 商品详情 */
.product-detail {
  padding: 20px;
}

/* 商品名称 */
.product-name {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 商品描述 */
.product-desc {
  font-size: 13px;
  color: #94a3b8;
  line-height: 1.5;
  height: 40px;
  margin-bottom: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 价格行 */
.price-row {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 20px;
}
.current-price .symbol {
  color: #38bdf8;
  font-size: 14px;
  font-weight: 800;
}
.current-price .amount {
  color: #38bdf8;
  font-size: 24px;
  font-weight: 800;
}

/* 购买按钮 */
.buy-btn {
  width: 100%;
  height: 45px;
  border-radius: 12px;
  border: none;
  font-weight: 700;
  background: #334155;
  transition: all 0.3s;
}
.buy-btn:hover {
  background: #475569;
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -10px rgba(51, 65, 85, 0.8);
}

/* 空状态样式 */
:deep(.el-empty__description p) {
  color: #94a3b8;
}
</style>