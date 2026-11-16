<script setup lang="ts">
// Order tracking stepper: the steps of the order type's status chain, with the reached ones
// highlighted. Vertical on phones, horizontal from the sm breakpoint. The current step is exposed
// to assistive technology with aria-current="step".

import { computed, type Component } from 'vue'
import { BikeIcon, ChefHatIcon, CircleCheckBigIcon, ClipboardCheckIcon, PackageCheckIcon } from '@lucide/vue'

import { cn } from '@/lib/utils'
import { STATUS_CHAINS, STATUS_LABELS } from '@/utils/orderStatus'
import type { OrderStatus, OrderType } from '@/types'

const props = defineProps<{ type: OrderType; status: OrderStatus }>()

const ICONS: Partial<Record<OrderStatus, Component>> = {
  PENDING: ClipboardCheckIcon,
  PREPARING: ChefHatIcon,
  READY: PackageCheckIcon,
  ON_THE_WAY: BikeIcon,
  COMPLETED: CircleCheckBigIcon,
}

const chain = computed(() => STATUS_CHAINS[props.type])
const current = computed(() => chain.value.indexOf(props.status))

function state(index: number): 'done' | 'current' | 'todo' {
  if (index < current.value) return 'done'
  return index === current.value ? 'current' : 'todo'
}
</script>

<template>
  <ol class="flex flex-col gap-4 sm:flex-row sm:gap-0">
    <li
      v-for="(step, index) in chain"
      :key="step"
      :aria-current="state(index) === 'current' ? 'step' : undefined"
      class="relative flex items-center gap-3 sm:flex-1 sm:flex-col sm:gap-2 sm:text-center"
    >
      <!-- Connector to the previous step (horizontal layout only). -->
      <span
        v-if="index > 0"
        aria-hidden="true"
        :class="
          cn(
            'absolute top-5 right-1/2 hidden h-0.5 w-full -translate-x-5 sm:block',
            state(index) === 'todo' ? 'bg-border' : 'bg-primary',
          )
        "
      ></span>
      <span
        :class="
          cn(
            'relative z-10 grid size-10 shrink-0 place-items-center rounded-full border-2 transition-colors',
            state(index) === 'todo' && 'border-border bg-card text-muted-foreground',
            state(index) === 'done' && 'border-primary bg-primary/10 text-primary',
            state(index) === 'current' && 'border-primary bg-primary text-primary-foreground ring-4 ring-primary/15',
          )
        "
      >
        <component :is="ICONS[step]" class="size-4" aria-hidden="true" />
      </span>
      <span :class="cn('text-sm', state(index) === 'todo' ? 'text-muted-foreground' : 'font-semibold')">
        {{ STATUS_LABELS[step] }}
        <span v-if="state(index) === 'done'" class="sr-only">(done)</span>
      </span>
    </li>
  </ol>
</template>
