<template>
  <div class="home-container">
    <header class="home-header">
      <div class="header-content">
        <h1 class="logo">Flash Sale Hub</h1>
        <nav class="nav-menu">
          <button class="nav-item" @click="router.push('/products')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="9" cy="21" r="1"/>
              <circle cx="20" cy="21" r="1"/>
              <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
            </svg>
            Products
          </button>
          <button class="nav-item" @click="router.push('/seckill')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            Flash Sales
          </button>
          <button class="nav-item" @click="handleLogout">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
              <polyline points="16 17 21 12 16 7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
            Logout
          </button>
        </nav>
      </div>
      <div class="user-greeting">Welcome, {{ username }}</div>
    </header>

    <main class="home-content">
      <section class="hero-section">
        <div class="hero-text">
          <h2>Premium Flash Sales</h2>
          <p>Discover exclusive deals on premium products</p>
        </div>
      </section>

      <section class="quick-links">
        <div class="link-card" @click="router.push('/products')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="9" cy="21" r="1"/>
            <circle cx="20" cy="21" r="1"/>
            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
          </svg>
          <h3>Browse Products</h3>
          <p>Explore our full catalog</p>
        </div>
        <div class="link-card" @click="router.push('/seckill')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
          <h3>Flash Sales</h3>
          <p>Limited time offers</p>
        </div>
      </section>

      <section class="featured-section">
        <h2 class="section-title">Featured Products</h2>
        <div class="featured-grid">
          <div v-for="item in hotProducts" :key="item.id" class="featured-card">
            <div class="featured-image" :style="{ background: item.bg }"></div>
            <div class="featured-info">
              <h4>{{ item.name }}</h4>
              <div class="featured-pricing">
                <span class="featured-price">¥{{ item.seckillPrice }}</span>
                <span class="featured-original">¥{{ item.price }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref(localStorage.getItem('username') || 'Guest')

const hotProducts = ref([
  { id: 1, name: 'Premium Headphones', price: 1999, seckillPrice: 999, bg: 'linear-gradient(135deg, #d4af37 0%, #e8c547 100%)' },
  { id: 2, name: 'Wireless Charger', price: 599, seckillPrice: 299, bg: 'linear-gradient(135deg, #d4af37 0%, #e8c547 100%)' },
  { id: 3, name: 'Phone Stand', price: 299, seckillPrice: 149, bg: 'linear-gradient(135deg, #d4af37 0%, #e8c547 100%)' }
])

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
.home-container {
  min-height: 100vh;
  background: var(--bg-primary);
  color: var(--text-primary);
}

.home-header {
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border);
  padding: 20px 24px;
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 40px;
  flex: 1;
}

.logo {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
  color: var(--accent);
}

.nav-menu {
  display: flex;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: transparent;
  border: 1px solid var(--border);
  border-radius: 6px;
  color: var(--text-primary);
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.2s ease;
}

.nav-item:hover {
  background: var(--border-light);
  border-color: var(--accent);
  color: var(--accent);
}

.nav-item svg {
  width: 18px;
  height: 18px;
}

.user-greeting {
  font-size: 0.95rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.home-content {
  padding: 48px 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.hero-section {
  margin-bottom: 64px;
  padding: 48px 32px;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.1), rgba(212, 175, 55, 0.05));
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 12px;
  text-align: center;
}

.hero-text h2 {
  font-size: 2.5rem;
  margin: 0 0 16px 0;
  color: var(--text-primary);
}

.hero-text p {
  font-size: 1.1rem;
  color: var(--text-secondary);
  margin: 0;
}

.quick-links {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 64px;
}

.link-card {
  padding: 32px 24px;
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.link-card:hover {
  border-color: var(--accent);
  box-shadow: var(--shadow-md);
  transform: translateY(-4px);
}

.link-card svg {
  width: 48px;
  height: 48px;
  color: var(--accent);
}

.link-card h3 {
  font-size: 1.25rem;
  margin: 0;
  color: var(--text-primary);
}

.link-card p {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin: 0;
}

.featured-section {
  margin-bottom: 48px;
}

.section-title {
  font-size: 1.75rem;
  font-weight: 600;
  margin: 0 0 24px 0;
  color: var(--text-primary);
}

.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.featured-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.featured-card:hover {
  border-color: var(--accent);
  box-shadow: var(--shadow-md);
  transform: translateY(-4px);
}

.featured-image {
  width: 100%;
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.featured-info {
  padding: 16px;
}

.featured-info h4 {
  font-size: 0.95rem;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: var(--text-primary);
}

.featured-pricing {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.featured-price {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--accent);
}

.featured-original {
  font-size: 0.8rem;
  color: var(--text-muted);
  text-decoration: line-through;
}

@media (max-width: 768px) {
  .home-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .header-content {
    flex-direction: column;
    gap: 16px;
    width: 100%;
  }

  .nav-menu {
    width: 100%;
    flex-wrap: wrap;
  }

  .hero-text h2 {
    font-size: 1.75rem;
  }

  .featured-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 16px;
  }
}
</style>
