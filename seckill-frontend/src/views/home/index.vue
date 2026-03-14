<template>
  <div class="home-container">
    <el-header class="header">
      <span class="logo">🔥 极速秒杀商城</span>
      <el-button type="danger" link @click="handleLogout">退出登录</el-button>
    </el-header>

    <el-main>
      <el-row :gutter="20">
        <el-col :span="8" v-for="item in productList" :key="item.id">
          <el-card class="product-card" shadow="hover">
            <img src="https://via.placeholder.com/300x200?text=iPhone+15+Pro" class="image" />
            <div style="padding: 14px">
              <span class="p-name">{{ item.name }}</span>
              <div class="price-box">
                <span class="seckill-price">￥{{ item.seckillPrice }}</span>
                <span class="old-price">￥{{ item.price }}</span>
              </div>
              <div class="time-info">秒杀时间: {{ item.startTime }}</div>
              <el-button type="danger" class="buy-btn" @click="handleSeckill(item.id)">立即抢购</el-button>
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

const productList = ref([])
const router = useRouter()

const loadProducts = async () => {
  try {
    const res = await getProductList()
    productList.value = res
  } catch (error) { console.error(error) }
}

const handleSeckill = (productId) => {
  ElMessage.warning(`触发秒杀！正在请求后端逻辑 (商品ID: ${productId})`)
}

const handleLogout = () => {
  router.push('/login')
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.header { background: #fff; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; }
.logo { font-size: 20px; font-weight: bold; color: #f56c6c; }
.product-card { margin-bottom: 20px; border-radius: 8px; }
.image { width: 100%; display: block; }
.p-name { font-size: 18px; font-weight: bold; }
.price-box { margin: 10px 0; }
.seckill-price { color: #f56c6c; font-size: 24px; font-weight: bold; }
.old-price { color: #999; text-decoration: line-through; margin-left: 10px; }
.time-info { font-size: 12px; color: #666; margin-bottom: 10px; }
.buy-btn { width: 100%; }
</style>