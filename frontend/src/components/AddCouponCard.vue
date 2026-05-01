<template>
  <div class="add-card" :class="{ open: isOpen }">
    <div v-if="!isOpen" class="plus-btn" @click="isOpen = true">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <line x1="12" y1="5" x2="12" y2="19" />
        <line x1="5" y1="12" x2="19" y2="12" />
      </svg>
    </div>

    <form v-else @submit.prevent="submit" class="add-form">
      <button type="button" class="close-btn" @click="reset">✕</button>
      <h3>New Coupon</h3>

      <label>
        Code
        <input v-model="form.code" type="text" required placeholder="SUMMER25" />
      </label>
      <label>
        Discount (%)
        <input v-model.number="form.discount" type="number" required min="1" max="100" placeholder="25" />
      </label>
      <label>
        Description
        <input v-model="form.description" type="text" required placeholder="Limited time offer" />
      </label>

      <button type="submit" :disabled="loading" class="submit-btn">
        {{ loading ? 'Creating…' : 'Create' }}
      </button>
      <p v-if="error" class="error">{{ error }}</p>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { createCoupon } from '../api/coupons'

const emit = defineEmits<{ created: [] }>()

const isOpen = ref(false)
const loading = ref(false)
const error = ref('')

const form = reactive({ code: '', discount: 0, description: '' })

function reset() {
  isOpen.value = false
  loading.value = false
  error.value = ''
  form.code = ''
  form.discount = 0
  form.description = ''
}

async function submit() {
  loading.value = true
  error.value = ''
  try {
    await createCoupon({ code: form.code, discount: form.discount, description: form.description })
    reset()
    emit('created')
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Unknown error'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.add-card {
  width: 220px;
  min-height: 248px;
  height: 100%;
  border-radius: 16px;
  border: 2px dashed var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.2s, background 0.2s;
  background: var(--card-add-bg);
  position: relative;
}

.add-card:not(.open):hover {
  border-color: var(--accent);
  background: var(--card-add-hover-bg);
}

.plus-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--accent);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #fff;
  transition: transform 0.15s;
}

.plus-btn:hover {
  transform: scale(1.1);
}

.plus-btn svg {
  width: 28px;
  height: 28px;
}

.add-form {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.add-form h3 {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 700;
  color: var(--text);
}

.add-form label {
  display: flex;
  flex-direction: column;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-muted);
  gap: 4px;
}

.add-form input {
  padding: 7px 10px;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  background: var(--input-bg);
  color: var(--text);
  font-size: 13px;
  outline: none;
  transition: border-color 0.2s;
}

.add-form input:focus {
  border-color: var(--accent);
}

.submit-btn {
  padding: 9px;
  border-radius: 10px;
  background: var(--accent);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: opacity 0.15s;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  color: var(--text-muted);
  padding: 2px 6px;
}

.error {
  font-size: 11px;
  color: #e53935;
  margin: 0;
}
</style>
