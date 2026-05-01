<template>
  <div class="page">
    <div class="toolbar">
      <button class="bulk-btn" @click="generateBulk" :disabled="bulkLoading">
        {{ bulkLoading ? 'Generating…' : 'Generate 100 Coupons' }}
      </button>
      <ThemeToggle />
    </div>

    <p v-if="fetchError" class="fetch-error">{{ fetchError }}</p>

    <div class="grid">
      <AddCouponCard @created="resetAndLoad" />
      <CouponCard
        v-for="(coupon, i) in coupons"
        :key="coupon.id ?? i"
        :coupon="coupon"
        :color-index="i"
      />
    </div>

    <div ref="sentinel" class="sentinel">
      <p v-if="loading">Loading more coupons…</p>
      <p v-else-if="!hasMore && coupons.length > 0">No more coupons</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import CouponCard from './components/CouponCard.vue'
import AddCouponCard from './components/AddCouponCard.vue'
import ThemeToggle from './components/ThemeToggle.vue'
import { fetchCoupons, createCouponsBulk, generateRandomCoupons } from './api/coupons'
import type { CouponDto } from './types/CouponDto'

const coupons = ref<CouponDto[]>([])
const fetchError = ref('')
const bulkLoading = ref(false)
const loading = ref(false)
const page = ref(0)
const hasMore = ref(true)
const sentinel = ref<HTMLElement | null>(null)
let observer: IntersectionObserver | null = null
let pollTimer: ReturnType<typeof setInterval> | null = null

async function loadMoreCoupons() {
  if (loading.value || !hasMore.value) return
  loading.value = true
  fetchError.value = ''
  try {
    const newCoupons = await fetchCoupons(page.value, 100)
    if (newCoupons.length < 100) {
      hasMore.value = false
    }
    // Filter out coupons we already have to avoid duplicates when polling + infinite scroll interact
    const existingIds = new Set(coupons.value.map(c => c.id))
    const filteredNew = newCoupons.filter(c => !existingIds.has(c.id))
    coupons.value.push(...filteredNew)
    page.value++
  } catch (e) {
    fetchError.value = e instanceof Error ? e.message : 'Failed to load coupons'
  } finally {
    loading.value = false
  }
}

async function pollFirstPage() {
  try {
    const firstPage = await fetchCoupons(0, 100)
    
    // We only reconcile the first page of coupons (up to 100).
    const firstPageIds = new Set(firstPage.map(c => c.id))
    const couponsBeyondFirstPage = coupons.value.slice(100)
    
    // Remove coupons from the tail that might have moved to the first page
    const beyondFirstPageFiltered = couponsBeyondFirstPage.filter(c => !firstPageIds.has(c.id))
    
    // The backend's first page is the source of truth for the first 100 items.
    coupons.value = [...firstPage, ...beyondFirstPageFiltered]
    
    // If backend returns fewer than 100 items, it means there are no more items beyond this.
    if (firstPage.length < 100) {
      hasMore.value = false
    }
  } catch (e) {
    console.error('Polling failed:', e)
  }
}

async function resetAndLoad() {
  page.value = 0
  coupons.value = []
  hasMore.value = true
  await loadMoreCoupons()
}

async function generateBulk() {
  bulkLoading.value = true
  try {
    await createCouponsBulk(generateRandomCoupons(100))
    await resetAndLoad()
  } catch (e) {
    fetchError.value = e instanceof Error ? e.message : 'Bulk generation failed'
  } finally {
    bulkLoading.value = false
  }
}

onMounted(() => {
  loadMoreCoupons()
  pollTimer = setInterval(pollFirstPage, 10_000)

  observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting) {
        loadMoreCoupons()
      }
    },
    { rootMargin: '200px' }
  )

  if (sentinel.value) {
    observer.observe(sentinel.value)
  }
})

onUnmounted(() => {
  if (observer) observer.disconnect()
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 24px;
  background: var(--bg-color);
  transition: background 0.3s;
}

.toolbar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
}

.bulk-btn {
  padding: 9px 18px;
  border-radius: 10px;
  background: var(--accent);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: opacity 0.15s;
}

.bulk-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.grid {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}

.fetch-error {
  color: #e53935;
  font-size: 13px;
  margin-bottom: 16px;
}

.sentinel {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 14px;
}
</style>
