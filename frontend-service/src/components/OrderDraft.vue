<script setup lang="ts">
// Order draft sidebar. The cart lives entirely in the Pinia store — there is
// no server-side cart — and stores only menuItemId + quantity; every price
// and total shown here is computed by joining against the loaded menu.

import { computed } from 'vue'
import { Minus, Plus } from 'lucide-vue-next'

import { useCartStore } from '@/stores/cart'
import type { Menu } from '@/types'

const props = defineProps<{ menu: Menu; checkoutDisabled: boolean }>()
const emit = defineEmits<{ checkout: [] }>()

const cart = useCartStore()
const lines = computed(() => cart.lines(props.menu))
const totalLabel = computed(() => cart.totalLabel(props.menu))
</script>

<template>
  <aside class="order-draft">
    <h6 style="margin-bottom: 12px">Order draft</h6>

    <p v-if="cart.isEmpty" class="text-muted" style="font-size: 13px">
      Nothing added yet. The cart lives entirely in the Pinia store — there is no server-side cart.
    </p>

    <div v-for="line in lines" :key="line.itemId" class="order-draft__line">
      <div style="flex: 1">
        <div style="font-size: 13px; font-weight: 800">{{ line.item.name }}</div>
        <div class="text-muted" style="font-size: 11px">{{ line.unitLabel }} each</div>
      </div>
      <button
        class="btn btn-ghost"
        type="button"
        aria-label="Decrease quantity"
        style="min-width: 32px"
        @click="cart.decrement(line.itemId)"
      >
        <Minus :size="14" />
      </button>
      <span style="font-variant-numeric: tabular-nums; min-width: 16px; text-align: center">
        {{ line.quantity }}
      </span>
      <button
        class="btn btn-ghost"
        type="button"
        aria-label="Increase quantity"
        style="min-width: 32px"
        @click="cart.increment(line.itemId)"
      >
        <Plus :size="14" />
      </button>
      <strong style="font-variant-numeric: tabular-nums; min-width: 56px; text-align: right">
        {{ line.totalLabel }}
      </strong>
    </div>

    <div style="display: flex; justify-content: space-between; align-items: baseline; margin-top: 14px">
      <span class="text-muted" style="font-size: 12px; text-transform: uppercase; letter-spacing: 0.08em">
        Total
      </span>
      <strong style="font-size: 22px; font-variant-numeric: tabular-nums">{{ totalLabel }}</strong>
    </div>
    <p class="text-muted" style="font-size: 11px; margin: 6px 0 0">
      Computed client-side. The order domain stores only menuItemId and quantity — no prices, no
      total.
    </p>
    <button
      class="btn btn-primary btn-block"
      type="button"
      :disabled="props.checkoutDisabled"
      style="min-height: 44px"
      @click="emit('checkout')"
    >
      Checkout
    </button>
  </aside>
</template>

<style scoped>
.order-draft {
  border-left: 2px solid var(--color-divider);
  padding-left: 24px;
  position: sticky;
  top: 24px;
}

.order-draft__line {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid var(--color-divider);
}

@media (max-width: 1149px) {
  .order-draft {
    border-left: none;
    border-top: 2px solid var(--color-divider);
    padding-left: 0;
    padding-top: 16px;
    position: static;
  }
}
</style>
