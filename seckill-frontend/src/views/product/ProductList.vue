<template>
  <div class="product-container">
    <el-header class="header">
      <el-button type="primary" link @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon> 返回首页
      </el-button>
      <span class="logo">🔥 秒杀商品列表</span>
      <el-button type="danger" link @click="handleLogout">退出登录</el-button>
    </el-header>

    <el-main>
      <el-row :gutter="20">
        <el-col :span="8" v-for="item in productList" :key="item.id">
          <el-card class="product-card" shadow="hover">
            <img :src="item.image || 'https://via.placeholder.com/300x200?text=商品图片'" class="image" />
            <div style="padding: 14px">
              <span class="p-name">{{ item.name }}</span>
              <div class="price-box">
                <span class="seckill-price">￥{{ item.seckillPrice }}</span>
                <span class="old-price">￥{{ item.price }}</span>
              </div>
              <div class="time-info">秒杀时间: {{ item.startTime || '即将开始' }}</div>
              <div class="stock-info" :class="{ 'low-stock': item.stock && item.stock < 10 }">
                剩余库存: {{ item.stock || '充足' }}
              </div>
              <el-button 
                type="danger" 
                class="buy-btn" 
                @click="handleSeckill(item.id)"
                :disabled="!item.stock || item.stock === 0"
              >
                {{ item.stock && item.stock > 0 ? '立即抢购' : '已抢光' }}
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProductList } from '../../api/product'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

const productList = ref([])
const router = useRouter()

const loadProducts = async () => {
  try {
    const res = await getProductList()
    productList.value = res
  } catch (error) { 
    console.error(error)
    ElMessage.error('加载商品列表失败')
  }
}

const handleSeckill = (productId) => {
  // 实际项目中这里会跳转到秒杀详情页或直接触发秒杀逻辑
  ElMessage.success(`正在抢购商品ID: ${productId}`)
  // router.push(`/seckill/${productId}`)
}

const goBack = () => {
  router.push('/home')
}

const handleLogout = () => {
  localStorage.removeItem('seckill_token')
  router.push('/login')
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.product-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}
.header { 
  background: #fff; 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  border-bottom: 1px solid #eee;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}
.back-btn {
  display: flex;
  align-items: center;
  gap: 5px;
}
.logo { 
  font-size: 20px; 
  font-weight: bold; 
  color: #f56c6c; 
}
.el-main {
  margin-top: 60px;
  padding: 20px;
}
.product-card { 
  margin-bottom: 20px; 
  border-radius: 8px;
  transition: transform 0.3s;
}
.product-card:hover {
  transform: translateY(-5px);
}
.image { 
  width: 100%; 
  height: 200px;
  object-fit: cover;
  display: block; 
}
.p-name { 
  font-size: 18px; 
  font-weight: bold;
  display: block;
  margin-bottom: 10px;
}
.price-box { 
  margin: 10px 0; 
}
.seckill-price { 
  color: #f56c6c; 
  font-size: 24px; 
  font-weight: bold; 
}
.old-price { 
  color: #999; 
  text-decoration: line-through; 
  margin-left: 10px; 
}
.time-info { 
  font-size: 12px; 
  color: #666; 
  margin-bottom: 5px; 
}
.stock-info {
  font-size: 12px;
  color: #67C23A;
  margin-bottom: 10px;
}
.low-stock {
  color: #f56c6c;
}
.buy-btn { 
  width: 100%; 
}
</style>