<script setup lang="ts">
// The customer's cart for the current restaurant. The cart lives only in the Pinia store (there is
// no server-side cart) and holds menuItemId + quantity; prices shown here are joined from the
// loaded menu, and the final total is computed by order-service when the order is placed.

import { computed } from 'vue'
import { ShoppingBagIcon } from '@lucide/vue'

import QuantityStepper from '@/components/QuantityStepper.vue'
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'
import { useCartStore } from '@/stores/cart'
import type { Menu } from '@/types'

const props = defineProps<{ menu: Menu; checkoutDisabled: boolean }>()
const emit = defineEmits<{ checkout: [] }>()

const cart = useCartStore()
const lines = computed(() => cart.lines(props.menu))
const totalLabel = computed(() => cart.totalLabel(props.menu))
</script>

<template>
  <div class="flex flex-col gap-4">
    <div v-if="lines.length === 0" class="flex flex-col items-center gap-2 py-6 text-center">
      <span class="grid size-12 place-items-center rounded-full bg-muted text-muted-foreground">
        <ShoppingBagIcon class="size-5" aria-hidden="true" />
      </span>
      <p class="font-semibold">Your bag is empty</p>
      <p class="text-sm text-muted-foreground">Add a dish from the menu to start an order.</p>
    </div>

    <ul v-else class="flex flex-col divide-y">
      <li v-for="line in lines" :key="line.itemId" class="flex items-center gap-3 py-3 first:pt-0">
        <div class="min-w-0 flex-1">
          <p class="truncate text-sm font-semibold">{{ line.item.name }}</p>
          <p class="text-xs text-muted-foreground tabular-nums">{{ line.unitLabel }} each</p>
        </div>
        <QuantityStepper
          :quantity="line.quantity"
          :label="line.item.name"
          @increment="cart.increment(line.itemId)"
          @decrement="cart.decrement(line.itemId)"
        />
        <span class="w-16 text-right text-sm font-semibold tabular-nums">{{ line.totalLabel }}</span>
      </li>
    </ul>

    <template v-if="lines.length">
      <Separator />
      <div class="flex items-baseline justify-between">
        <span class="text-sm text-muted-foreground">Subtotal</span>
        <strong class="text-xl tabular-nums">{{ totalLabel }}</strong>
      </div>
    </template>
    <Button size="lg" class="h-11 w-full" :disabled="props.checkoutDisabled" @click="emit('checkout')">
      Go to checkout
    </Button>
  </div>
</template>
