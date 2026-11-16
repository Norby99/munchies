<script setup lang="ts">
// One dish on the customer menu: name, description, variations, price, and either an "Add" button
// or, once in the cart, a quantity stepper.
import { PlusIcon } from '@lucide/vue'

import QuantityStepper from '@/components/QuantityStepper.vue'
import { Button } from '@/components/ui/button'
import { formatEUR } from '@/utils/money'
import type { MenuItem } from '@/types'

const props = defineProps<{ item: MenuItem; quantity: number; disabled: boolean }>()
const emit = defineEmits<{ add: []; remove: [] }>()
</script>

<template>
  <article class="flex flex-col gap-3 rounded-xl border bg-card p-4 transition-shadow hover:shadow-sm">
    <div class="flex items-start justify-between gap-3">
      <h3 class="text-base font-bold">{{ props.item.name }}</h3>
      <span class="shrink-0 font-semibold tabular-nums">{{ formatEUR(props.item.price) }}</span>
    </div>
    <p v-if="props.item.description" class="line-clamp-3 text-sm text-pretty text-muted-foreground">
      {{ props.item.description }}
    </p>
    <div class="mt-auto flex items-end justify-between gap-3 pt-1">
      <p class="text-xs text-muted-foreground">
        <template v-if="props.item.variations.length">
          Options: {{ props.item.variations.map((v) => v.name).join(', ') }}
        </template>
      </p>
      <QuantityStepper
        v-if="props.quantity > 0"
        :quantity="props.quantity"
        :label="props.item.name"
        :disabled="props.disabled"
        @increment="emit('add')"
        @decrement="emit('remove')"
      />
      <Button v-else variant="outline" class="shrink-0 rounded-full" :disabled="props.disabled" @click="emit('add')">
        <PlusIcon data-icon="inline-start" />
        Add<span class="sr-only"> {{ props.item.name }}</span>
      </Button>
    </div>
  </article>
</template>
