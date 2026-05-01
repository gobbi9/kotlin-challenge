<template>
  <div
    class="coupon-wrapper"
    @mousemove="onMouseMove"
    @mouseleave="onMouseLeave"
    @click="onCardClick"
    :style="wrapperStyle"
  >
    <div class="particles" ref="particlesEl"></div>

    <div class="coupon-body" :style="{ background: palette.body }">
      <div class="bg-pattern" :style="patternStyle"></div>
      <div class="coupon-top">
        <span class="coupon-code">{{ coupon.code }}</span>
        <div class="discount-block">
          <span class="discount-shadow">{{ discountInt }}</span>
          <span class="discount-main">{{ discountInt }}%</span>
          <span class="discount-off">OFF</span>
        </div>
      </div>
    </div>

    <div class="coupon-notch" :style="{ background: palette.footer }">
      <div class="notch-circle notch-left"></div>
      <div class="notch-dashes"></div>
      <div class="notch-circle notch-right"></div>
    </div>

    <div class="coupon-footer" :style="{ background: palette.footer }">
      <span class="coupon-description">{{ coupon.description }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { CouponDto } from '../types/CouponDto'

const props = defineProps<{ coupon: CouponDto; colorIndex: number }>()

const particlesEl = ref<HTMLElement | null>(null)

const PALETTES = [
  { body: '#e91e8c', footer: '#c2186e' },
  { body: '#00bcd4', footer: '#0097a7' },
  { body: '#9c27b0', footer: '#7b1fa2' },
  { body: '#ff5722', footer: '#e64a19' },
  { body: '#43a047', footer: '#2e7d32' },
  { body: '#1e88e5', footer: '#1565c0' },
  { body: '#f4511e', footer: '#bf360c' },
  { body: '#00897b', footer: '#00695c' },
]
const palette = computed(() => PALETTES[props.colorIndex % PALETTES.length])

const ICONS = [
  `<svg xmlns='http://www.w3.org/2000/svg' width='40' height='40' viewBox='0 0 24 24'><path fill='rgba(255,255,255,0.13)' d='M19 6h-2c0-2.76-2.24-5-5-5S7 3.24 7 6H5c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-7-3c1.66 0 3 1.34 3 3H9c0-1.66 1.34-3 3-3zm0 10c-1.66 0-3-1.34-3-3h2c0 .55.45 1 1 1s1-.45 1-1h2c0 1.66-1.34 3-3 3z'/></svg>`,
  `<svg xmlns='http://www.w3.org/2000/svg' width='40' height='40' viewBox='0 0 24 24'><path fill='rgba(255,255,255,0.13)' d='M17.63 5.84C17.27 5.33 16.67 5 16 5L5 5.01C3.9 5.01 3 5.9 3 7v10c0 1.1.9 1.99 2 1.99L16 19c.67 0 1.27-.33 1.63-.84L22 12l-4.37-6.16z'/></svg>`,
  `<svg xmlns='http://www.w3.org/2000/svg' width='40' height='40' viewBox='0 0 24 24'><path fill='rgba(255,255,255,0.13)' d='M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z'/></svg>`,
  `<svg xmlns='http://www.w3.org/2000/svg' width='40' height='40' viewBox='0 0 24 24'><path fill='rgba(255,255,255,0.13)' d='M18.99 5.99L5 19l1.01 1.01L20 7l-1.01-1.01zM7.5 11C9.43 11 11 9.43 11 7.5S9.43 4 7.5 4 4 5.57 4 7.5 5.57 11 7.5 11zm0-5C8.33 6 9 6.67 9 7.5S8.33 9 7.5 9 6 8.33 6 7.5 6.67 6 7.5 6zm9 7c-1.93 0-3.5 1.57-3.5 3.5S14.57 20 16.5 20s3.5-1.57 3.5-3.5S18.43 13 16.5 13zm0 5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5z'/></svg>`,
  `<svg xmlns='http://www.w3.org/2000/svg' width='40' height='40' viewBox='0 0 24 24'><path fill='rgba(255,255,255,0.13)' d='M20 6h-2.18c.07-.23.18-.45.18-.7C18 3.48 16.52 2 14.7 2c-.96 0-1.86.4-2.52 1.05L12 3.21l-.19-.17C11.15 2.4 10.26 2 9.3 2 7.48 2 6 3.48 6 5.3c0 .25.11.47.18.7H4c-1.11 0-1.99.89-1.99 2L2 19c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2zm-5.3-2c.72 0 1.3.58 1.3 1.3 0 .72-.58 1.3-1.3 1.3H13V5.3c0-.72.58-1.3 1.3-1.3H14.7zm-5.4 0C10.02 4 10.6 4.58 10.6 5.3V6.6H9.3c-.72 0-1.3-.58-1.3-1.3C8 4.58 8.58 4 9.3 4zM11 19H4v-2h7v2zm0-4H4v-2h7v2zm0-4H4V9h7v2zm9 8h-7v-2h7v2zm0-4h-7v-2h7v2zm0-4h-7V9h7v2z'/></svg>`,
]

const patternStyle = computed(() => {
  const svg = ICONS[props.colorIndex % ICONS.length]
  return {
    backgroundImage: `url("data:image/svg+xml,${encodeURIComponent(svg)}")`,
    backgroundSize: '48px 48px',
  }
})

const discountInt = computed(() => Math.floor(props.coupon.discount))

const tiltX = ref(0)
const tiltY = ref(0)
const lifted = ref(false)

function onMouseMove(e: MouseEvent) {
  const el = e.currentTarget as HTMLElement
  const rect = el.getBoundingClientRect()
  tiltX.value = ((e.clientY - (rect.top + rect.height / 2)) / (rect.height / 2)) * -12
  tiltY.value = ((e.clientX - (rect.left + rect.width / 2)) / (rect.width / 2)) * 12
  lifted.value = true
}

function onMouseLeave() {
  tiltX.value = 0
  tiltY.value = 0
  lifted.value = false
}

const wrapperStyle = computed(() => ({
  transform: `perspective(800px) rotateX(${tiltX.value}deg) rotateY(${tiltY.value}deg) translateY(${lifted.value ? '-6px' : '0'})`,
  boxShadow: lifted.value
    ? '0 20px 40px var(--coupon-shadow-hover), 0 8px 16px var(--coupon-shadow-hover-secondary)'
    : '0 4px 12px var(--coupon-shadow-normal)',
}))

function onCardClick(e: MouseEvent) {
  spawnParticles(e)
}

const SPARK_COLORS = ['#fff', '#ffe082', '#f48fb1', '#80deea', '#a5d6a7', '#ce93d8']

function spawnParticles(e: MouseEvent) {
  const container = particlesEl.value
  if (!container) return
  const rect = (e.currentTarget as HTMLElement).getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top

  for (let i = 0; i < 20; i++) {
    const p = document.createElement('span')
    p.className = 'spark-particle'
    const angle = Math.random() * 360
    const dist = 50 + Math.random() * 70
    const size = 4 + Math.random() * 7
    p.style.cssText = `
      left:${x}px;top:${y}px;
      width:${size}px;height:${size}px;
      --dx:${Math.cos((angle * Math.PI) / 180) * dist}px;
      --dy:${Math.sin((angle * Math.PI) / 180) * dist}px;
      background:${SPARK_COLORS[Math.floor(Math.random() * SPARK_COLORS.length)]};
      animation-duration:${1.5 + Math.random() * 0.5}s;
    `
    container.appendChild(p)
    p.addEventListener('animationend', () => p.remove(), { once: true })
  }
}
</script>

<style scoped>
.coupon-wrapper {
  position: relative;
  width: 220px;
  cursor: pointer;
  user-select: none;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  height: 100%;
  /* transition only on leave (reset), not during active tracking */
  transition: box-shadow 0.2s ease;
}

.coupon-wrapper:not(:hover) {
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: visible;
  z-index: 20;
}

/* ── Body ──────────────────────────────────── */
.coupon-body {
  position: relative;
  border-radius: 16px 16px 0 0;
  padding: 20px 20px 28px;
  overflow: hidden;
  min-height: 165px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.bg-pattern {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.coupon-top {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.coupon-code {
  color: rgba(255, 255, 255, 0.9);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  text-align: center;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.discount-block {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 4px;
}

.discount-shadow {
  position: absolute;
  font-size: 96px;
  font-weight: 900;
  color: rgba(255, 255, 255, 0.14);
  line-height: 1;
  top: -8px;
  left: 50%;
  transform: translateX(-50%);
  pointer-events: none;
  white-space: nowrap;
  letter-spacing: -4px;
}

.discount-main {
  font-size: 54px;
  font-weight: 900;
  color: #fff;
  line-height: 1.05;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.25);
  position: relative;
  z-index: 1;
}

.discount-off {
  font-size: 13px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
  letter-spacing: 3px;
  position: relative;
  z-index: 1;
}

/* ── Notch ─────────────────────────────────── */
.coupon-notch {
  display: flex;
  align-items: center;
  height: 24px;
  overflow: hidden;
}

.notch-circle {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--bg-color);
  flex-shrink: 0;
}

.notch-left  { margin-left:  -12px; }
.notch-right { margin-right: -12px; }

.notch-dashes {
  flex: 1;
  border-top: 2px dashed rgba(255, 255, 255, 0.35);
}

/* ── Footer ────────────────────────────────── */
.coupon-footer {
  border-radius: 0 0 16px 16px;
  padding: 12px 20px 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 50px;
  flex: 1;
}

.coupon-description {
  color: rgba(255, 255, 255, 0.92);
  font-size: 12px;
  font-weight: 500;
  text-align: center;
  line-height: 1.4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style>

<style>
.spark-particle {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  transform: translate(-50%, -50%);
  animation: spark-burst linear forwards;
}

@keyframes spark-burst {
  0%   { opacity: 1; transform: translate(-50%, -50%) translate(0, 0) scale(1); }
  100% { opacity: 0; transform: translate(-50%, -50%) translate(var(--dx), var(--dy)) scale(0.1); }
}
</style>
