<template>
  <div class="seckill-detail-container">
    <header class="detail-header">
      <button class="back-btn" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </button>
      <h1 class="header-title">Flash Sale</h1>
      <div class="header-spacer"></div>
    </header>

    <main class="detail-content">
      <div class="product-section">
        <div class="product-image">
          <img v-if="product.mainImage" :src="product.mainImage" :alt="product.productName" />
          <div v-else class="image-placeholder">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <rect x="3" y="3" width="18" height="18" rx="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <path d="M21 15l-5-5L5 21"/>
            </svg>
          </div>
        </div>

        <div class="product-info">
          <h2 class="product-name">{{ product.productName }}</h2>
          <p class="product-desc">Premium quality product with limited availability</p>

          <div class="pricing-section">
            <div class="price-group">
              <span class="label">Original Price</span>
              <span class="original-price">¥{{ product.originalPrice }}</span>
            </div>
            <div class="price-group highlight">
              <span class="label">Flash Sale Price</span>
              <span class="seckill-price">¥{{ product.seckillPrice }}</span>
              <span class="discount">{{ Math.round((1 - product.seckillPrice / product.originalPrice) * 100) }}% OFF</span>
            </div>
          </div>

          <div class="timer-section">
            <div class="timer-label">Sale Ends In</div>
            <div class="countdown-timer">
              <div class="time-unit">
                <span class="time-value">{{ timer.hours }}</span>
                <span class="time-label">H</span>
              </div>
              <span class="separator">:</span>
              <div class="time-unit">
                <span class="time-value">{{ timer.minutes }}</span>
                <span class="time-label">M</span>
              </div>
              <span class="separator">:</span>
              <div class="time-unit">
                <span class="time-value">{{ timer.seconds }}</span>
                <span class="time-label">S</span>
              </div>
              <span class="separator">:</span>
              <div class="time-unit">
                <span class="time-value">{{ timer.milliseconds }}</span>
                <span class="time-label">MS</span>
              </div>
            </div>
          </div>

          <div class="inventory-section">
            <div class="inventory-header">
              <span class="inventory-label">Inventory</span>
              <span class="inventory-text">{{ product.availableStock }} / {{ product.totalStock }} remaining</span>
            </div>
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: inventoryPercent + '%' }"></div>
            </div>
            <div class="inventory-percentage">{{ inventoryPercent }}% Available</div>
          </div>

          <button
            :class="['purchase-btn', `state-${buttonState}`]"
            :disabled="buttonState !== 'active'"
            @click="handlePurchase"
          >
            <span v-if="buttonState === 'upcoming'" class="btn-text">
              Starts in {{ timer.hours }}:{{ String(timer.minutes).padStart(2, '0') }}
            </span>
            <span v-else-if="buttonState === 'active'" class="btn-text">Buy Now</span>
            <span v-else-if="buttonState === 'processing'" class="btn-text">
              <span class="spinner"></span>
              Processing...
            </span>
            <span v-else-if="buttonState === 'sold-out'" class="btn-text">Sold Out</span>
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const product = ref({
  seckillId: 1,
  productName: 'Premium Wireless Headphones',
  originalPrice: 1999,
  seckillPrice: 999,
  mainImage: null,
  availableStock: 45,
  totalStock: 500,
  endTime: new Date(Date.now() + 3600000)
})

const timer = ref({ hours: 0, minutes: 0, seconds: 0, milliseconds: 0 })
const buttonState = ref('active')
const isProcessing = ref(false)
let timerInterval = null

const inventoryPercent = computed(() => {
  return Math.round((product.value.availableStock / product.value.totalStock) * 100)
})

const updateTimer = () => {
  const now = new Date()
  const diff = product.value.endTime - now

  if (diff <= 0) {
    buttonState.value = 'sold-out'
    clearInterval(timerInterval)
    return
  }

  const hours = Math.floor(diff / 3600000)
  const minutes = Math.floor((diff % 3600000) / 60000)
  const seconds = Math.floor((diff % 60000) / 1000)
  const milliseconds = Math.floor((diff % 1000) / 10)

  timer.value = { hours, minutes, seconds, milliseconds }

  if (hours === 0 && minutes === 0 && seconds === 0) {
    buttonState.value = 'sold-out'
  }
}

const handlePurchase = async () => {
  if (isProcessing.value || buttonState.value !== 'active') return

  isProcessing.value = true
  buttonState.value = 'processing'

  try {
    await new Promise(resolve => setTimeout(resolve, 1500))
    buttonState.value = 'active'
    isProcessing.value = false
    product.value.availableStock = Math.max(0, product.value.availableStock - 1)
  } catch (error) {
    buttonState.value = 'active'
    isProcessing.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  updateTimer()
  timerInterval = setInterval(updateTimer, 10)
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
})
</script>

<style scoped>
.seckill-detail-container {
  min-height: 100vh;
  background: var(--bg-primary);
  color: var(--text-primary);
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid var(--border);
  background: var(--bg-secondary);
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

.detail-content {
  padding: 32px 24px;
  max-width: 1000px;
  margin: 0 auto;
}

.product-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 48px;
  align-items: start;
}

.product-image {
  width: 100%;
  aspect-ratio: 1;
  background: var(--bg-secondary);
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
  width: 80px;
  height: 80px;
  opacity: 0.3;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.product-name {
  font-size: 2rem;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
}

.product-desc {
  font-size: 0.95rem;
  color: var(--text-secondary);
  margin: 0;
}

.pricing-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px;
  background: var(--bg-secondary);
  border-radius: 12px;
}

.price-group {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.price-group.highlight {
  padding: 12px;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.1), rgba(212, 175, 55, 0.05));
  border-radius: 8px;
  border: 1px solid rgba(212, 175, 55, 0.2);
}

.label {
  font-size: 0.85rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.original-price {
  font-size: 1.25rem;
  color: var(--text-muted);
  text-decoration: line-through;
}

.seckill-price {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--accent);
}

.discount {
  font-size: 0.9rem;
  color: var(--error);
  font-weight: 600;
  margin-left: auto;
}

.timer-section {
  padding: 20px;
  background: var(--bg-secondary);
  border-radius: 12px;
}

.timer-label {
  font-size: 0.85rem;
  color: var(--text-secondary);
  font-weight: 500;
  margin-bottom: 12px;
}

.countdown-timer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-family: var(--mono);
}

.time-unit {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: var(--bg-primary);
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid var(--border);
  min-width: 60px;
}

.time-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--accent);
}

.time-label {
  font-size: 0.7rem;
  color: var(--text-muted);
  margin-top: 4px;
}

.separator {
  font-size: 1.5rem;
  color: var(--text-muted);
  font-weight: 700;
}

.inventory-section {
  padding: 20px;
  background: var(--bg-secondary);
  border-radius: 12px;
}

.inventory-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.inventory-label {
  font-size: 0.85rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.inventory-text {
  font-size: 0.9rem;
  color: var(--text-primary);
  font-weight: 600;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: var(--border);
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--accent), var(--accent-light));
  transition: width 0.3s ease;
}

.inventory-percentage {
  font-size: 0.8rem;
  color: var(--text-secondary);
  text-align: right;
}

.purchase-btn {
  padding: 16px 24px;
  font-size: 1rem;
  font-weight: 600;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 48px;
}

.purchase-btn.state-active {
  background: var(--accent);
  color: var(--primary);
}

.purchase-btn.state-active:hover {
  background: var(--accent-light);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.purchase-btn.state-processing {
  background: var(--text-muted);
  color: var(--bg-primary);
  cursor: not-allowed;
}

.purchase-btn.state-upcoming,
.purchase-btn.state-sold-out {
  background: var(--border);
  color: var(--text-muted);
  cursor: not-allowed;
}

.spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: currentColor;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .product-section {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .product-name {
    font-size: 1.5rem;
  }

  .seckill-price {
    font-size: 2rem;
  }

  .detail-content {
    padding: 16px 16px;
  }
}
</style>
