<template>
  <div class="seckill-list-container">
    <div class="bg-shape shape-1"></div>
    <div class="bg-shape shape-2"></div>

    <el-container class="layout-container">
      <el-header class="glass-header">
        <div class="header-left" @click="router.push('/home')">
          <el-icon class="back-icon"><ArrowLeft /></el-icon>
          <span class="logo">极速秒杀 / <small>SECKILL</small></span>
        </div>
        <div class="user-info">{{ username }}</div>
      </el-header>

      <el-main class="main-content">
        <div class="page-title-section">
          <h2 class="glow-title">今日必抢 · 限时特惠</h2>
          <p class="sub-title">全场商品经过 Redis 预热，极致响应速度</p>
        </div>

        <el-row :gutter="25">
          <el-col 
            :xs="24" :sm="12" :md="8" :lg="6" 
            v-for="item in seckillProducts" 
            :key="item.seckillId"
          >
            <div class="glass-card product-card">
              <div class="product-cover" :style="!item.mainImage ? getRandomGradient(item.productId) : ''">
                <img v-if="item.mainImage" :src="item.mainImage" class="real-img">
                <div class="stock-tag" :class="{'sold-out': item.availableStock <= 0}">
                  {{ item.availableStock > 0 ? `仅剩 ${item.availableStock} 件` : '已售罄' }}
                </div>
              </div>

              <div class="product-detail">
                <h3 class="product-name">{{ item.productName }}</h3>
                <p class="product-time">结束时间: {{ item.endTime }}</p>
                
                <div class="price-row">
                  <div class="current-price">
                    <span class="symbol">￥</span>
                    <span class="amount">{{ item.seckillPrice }}</span>
                  </div>
                  <div class="original-price">￥{{ item.originalPrice }}</div>
                </div>

                <el-button 
                  class="seckill-btn" 
                  type="primary" 
                  :disabled="item.availableStock <= 0"
                  @click="handleSeckill(item.seckillId)"
                >
                  {{ item.availableStock > 0 ? '立即秒杀' : '已抢光' }}
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
import { getSeckillProductList } from '../../api/product'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '访客')
const seckillProducts = ref([])

const fetchSeckillProducts = async () => {
  try {
    const res = await getSeckillProductList()
    seckillProducts.value = res.data || res
  } catch (error) {
    ElMessage.error('获取秒杀商品失败')
  }
}

const handleSeckill = (seckillId) => {
  ElMessage.success({
    message: `准备抢购活动ID[${seckillId}]，请求已加入 MQ！`,
    plain: true,
  })
}

const getRandomGradient = (id) => {
  const gradients = [
    'linear-gradient(135deg, #f43f5e 0%, #fb923c 100%)',
    'linear-gradient(135deg, #8b5cf6 0%, #ec4899 100%)'
  ]
  return { background: gradients[(id || 0) % gradients.length] }
}

onMounted(() => {
  fetchSeckillProducts()
})
</script>

<style scoped>
/* 继承之前的发光特效，强化秒杀氛围 */
.seckill-list-container { min-height: 100vh; background-color: #0f172a; color: #f8fafc; position: relative; overflow-x: hidden; }
.bg-shape { position: fixed; border-radius: 50%; filter: blur(80px); z-index: 0; }
.shape-1 { width: 500px; height: 500px; background: rgba(236, 72, 153, 0.15); top: -200px; right: -100px; }
.shape-2 { width: 400px; height: 400px; background: rgba(139, 92, 246, 0.1); bottom: -100px; left: -100px; }
.layout-container { position: relative; z-index: 1; }
.glass-header { background: rgba(15, 23, 42, 0.7); backdrop-filter: blur(12px); border-bottom: 1px solid rgba(255, 255, 255, 0.1); display: flex; justify-content: space-between; align-items: center; padding: 0 5%; }
.header-left { cursor: pointer; display: flex; align-items: center; gap: 10px; }
.logo { font-weight: 800; font-size: 18px; color: #fff; }
.logo small { color: #f43f5e; font-weight: 400; }
.main-content { padding: 40px 5%; }
.page-title-section { margin-bottom: 40px; text-align: left; }
.glow-title { font-size: 32px; font-weight: 800; margin-bottom: 8px; background: linear-gradient(to right, #f43f5e, #f9a8d4); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.sub-title { color: #94a3b8; font-size: 16px; }
.product-card { background: rgba(255, 255, 255, 0.03); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 20px; overflow: hidden; margin-bottom: 25px; transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1); }
.product-card:hover { transform: translateY(-10px); border-color: rgba(236, 72, 153, 0.5); box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4); }
.product-cover { height: 180px; position: relative; display: flex; justify-content: center; align-items: center; overflow: hidden; }
.real-img { width: 100%; height: 100%; object-fit: cover; }
.stock-tag { position: absolute; top: 15px; right: 15px; background: rgba(236, 72, 153, 0.8); padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: bold; backdrop-filter: blur(4px); box-shadow: 0 4px 12px rgba(236, 72, 153, 0.3); }
.sold-out { background: rgba(100, 116, 139, 0.8); box-shadow: none; color: #cbd5e1; }
.product-detail { padding: 20px; }
.product-name { font-size: 18px; font-weight: 700; margin-bottom: 8px; color: #fff; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;}
.product-time { font-size: 13px; color: #fbbf24; margin-bottom: 15px; font-weight: 500;}
.price-row { display: flex; align-items: baseline; gap: 10px; margin-bottom: 20px; }
.current-price .symbol { color: #f43f5e; font-size: 14px; font-weight: 800; }
.current-price .amount { color: #f43f5e; font-size: 24px; font-weight: 800; }
.original-price { color: #64748b; text-decoration: line-through; font-size: 14px; }
.seckill-btn { width: 100%; height: 45px; border-radius: 12px; border: none; font-weight: 700; background: linear-gradient(135deg, #f43f5e 0%, #fb923c 100%) !important; transition: all 0.3s; }
.seckill-btn:hover { filter: brightness(1.2); box-shadow: 0 0 20px rgba(244, 63, 94, 0.4); }
.seckill-btn.is-disabled { background: #334155 !important; color: #64748b !important; }
</style>