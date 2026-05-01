import type { CouponDto, CouponListResponse } from '../types/CouponDto'

export async function fetchCoupons(page = 0, pageSize = 100): Promise<CouponDto[]> {
  const res = await fetch(`/coupons?page=${page}&pageSize=${pageSize}`)
  if (!res.ok) throw new Error(`Failed to fetch coupons: ${res.status}`)
  const data: CouponListResponse = await res.json()
  return data.coupons
}

export async function createCoupon(coupon: Omit<CouponDto, 'id'>): Promise<CouponDto> {
  const res = await fetch('/coupons', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(coupon),
  })
  if (!res.ok) throw new Error(`Failed to create coupon: ${res.status}`)
  return res.json()
}

export async function createCouponsBulk(coupons: Omit<CouponDto, 'id'>[]): Promise<void> {
  const res = await fetch('/coupons/bulk', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(coupons),
  })
  if (!res.ok) throw new Error(`Failed to bulk create coupons: ${res.status}`)
}

const ADJECTIVES = ['Summer', 'Winter', 'Spring', 'Flash', 'Mega', 'Super', 'Holiday', 'Weekend', 'Daily', 'Exclusive']
const NOUNS = ['Sale', 'Deal', 'Offer', 'Discount', 'Promo', 'Savings', 'Special', 'Bonus']
const DESCRIPTIONS = [
  'Limited time offer on selected items',
  'Exclusive discount for our members',
  'Save big on your next purchase',
  'Special promotional pricing',
  "Don't miss this amazing deal",
  'Valid on all products storewide',
  'One-time use discount code',
  'Seasonal clearance savings',
]

function randomInt(min: number, max: number) {
  return Math.floor(Math.random() * (max - min + 1)) + min
}

function randomItem<T>(arr: T[]): T {
  return arr[randomInt(0, arr.length - 1)]
}

export function generateRandomCoupons(count: number): Omit<CouponDto, 'id'>[] {
  const coupons: Omit<CouponDto, 'id'>[] = []
  const codes = new Set<string>()

  while (coupons.length < count) {
    const code = `${randomItem(ADJECTIVES).toUpperCase()}${randomItem(NOUNS).toUpperCase()}${randomInt(10, 99)}`
    if (!codes.has(code)) {
      codes.add(code)
      coupons.push({
        code,
        discount: randomInt(5, 75),
        description: randomItem(DESCRIPTIONS),
      })
    }
  }
  return coupons
}
