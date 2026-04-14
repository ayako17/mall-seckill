<template>
  <div class="seckill-list-container">
    <header class="list-header">
      <button class="back-btn" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </button>
      <h1 class="header-title">Flash Sales</h1>
      <div class="header-spacer"></div>
    </header>

    <main class="list-content">
      <div class="products-grid">
        <div
          v-for="item in seckillProducts"
          :key="item.seckillId"
          class="product-card"
          @click="goToDetail(item.seckillId)"
        >
          <div class="card-image">
            <img v-if="item.mainImage" :src="item.mainImage" :alt="item.productName" />
            <div v-else class="image-placeholder">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <rect x="3" y="3" width="18" height="18" rx="2"/>
                <circle cx="8.5" cy="8.5" r="1.5"/>
                <path d="M21 15l-5-5L5 21"/>
              </svg>
            </div>
            <div class="stock-badge" :class="{ 'low-stock': item.availableStock <= 10 }">
              {{ item.availableStock > 0 ? `${item.availableStock} left` : 'Sold Out' }}
            </div>
          </div>

          <div class="card-content">
            <h3 class="product-name">{{ item.productName }}</h3>
            <div class="pricing">
              <span class="seckill-price">¥{{ item.seckillPrice }}</span>
              <span class="original-price">¥{{ item.originalPrice }}</span>
            </div>
            <div class="discount-badge">
              {{ Math.round((1 - item.seckillPrice / item.originalPrice) * 100) }}% OFF
            </div>
          </div>
        </div>
      </div>

      <div v-if="seckillProducts.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <circle cx="12" cy="12" r="10"/>
          <path d="M12 6v6l4 2"/>
        </svg>
        <p>No flash sales available</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getSeckillProductList } from '../../api/product'

const router = useRouter()
const seckillProducts = ref([])

const fetchSeckillProducts = async () => {
  try {
    const res = await getSeckillProductList()
    seckillProducts.value = res.data || res
  } catch (error) {
    console.error('Failed to fetch seckill products')
  }
}

const goBack = () => {
  router.back()
}

const goToDetail = (seckillId) => {
  router.push(`/seckill/${seckillId}`)
}

onMounted(() => {
  fetchSeckillProducts()
})
</script>

<style scoped>
.seckill-list-container {
  min-height: 100vh;
  background: var(--bg-primary);
  color: var(--text-primary);
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid var(--border);
  background: var(--bg-secondary);
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-primary);
  transition: all 0.2s ease;
}

.back-btn:hover {
  background: var(--border-light);
  border-radius: 8px;
}

.back-btn svg {
  width: 24px;
  height: 24px;
}

.header-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
}

.header-spacer {
  width: 40px;
}

.list-content {
  padding: 32px 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 24px;
}

.product-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.product-card:hover {
  border-color: var(--accent);
  box-shadow: var(--shadow-md);
  transform: translateY(-4px);
}

.card-image {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  background: var(--border-light);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .card-image img {
  transform: scale(1.05);
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
}

.image-placeholder svg {
  width: 60px;
  height: 60px;
  opacity: 0.3;
}

.stock-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: var(--accent);
  color: var(--primary);
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  backdrop-filter: blur(4px);
}

.stock-badge.low-stock {
  background: var(--error);
}

.card-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex-grow: 1;
}

.product-name {
  font-size: 0.95rem;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pricing {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.seckill-price {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--accent);
}

.original-price {
  font-size: 0.85rem;
  color: var(--text-muted);
  text-decoration: line-through;
}

.discount-badge {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--error);
  background: rgba(231, 76, 60, 0.1);
  padding: 4px 8px;
  border-radius: 4px;
  width: fit-content;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 24px;
  color: var(--text-muted);
}

.empty-state svg {
  width: 80px;
  height: 80px;
  margin-bottom: 16px;
  opacity: 0.3;
}

.empty-state p {
  font-size: 1rem;
  margin: 0;
}

@media (max-width: 768px) {
  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 16px;
  }

  .list-content {
    padding: 16px 16px;
  }

  .card-content {
    padding: 12px;
  }

  .product-name {
    font-size: 0.85rem;
  }

  .seckill-price {
    font-size: 1.25rem;
  }
}
</style>
