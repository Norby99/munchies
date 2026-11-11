<script setup lang="ts">
// Compact minus / quantity / plus control for one cart line.
import { MinusIcon, PlusIcon } from '@lucide/vue'

import { Button } from '@/components/ui/button'

const props = withDefaults(defineProps<{ quantity: number; label: string; disabled?: boolean }>(), { disabled: false })
const emit = defineEmits<{ increment: []; decrement: [] }>()
</script>

<template>
  <div class="inline-flex items-center rounded-full border bg-card" role="group" :aria-label="`Quantity of ${props.label}`">
    <Button
      variant="ghost"
      size="icon"
      class="size-9 rounded-full"
      :aria-label="`Remove one ${props.label}`"
      @click="emit('decrement')"
    >
      <MinusIcon />
    </Button>
    <span class="min-w-6 text-center text-sm font-semibold tabular-nums" aria-live="polite">{{ props.quantity }}</span>
    <Button
      variant="ghost"
      size="icon"
      class="size-9 rounded-full"
      :disabled="props.disabled"
      :aria-label="`Add one ${props.label}`"
      @click="emit('increment')"
    >
      <PlusIcon />
    </Button>
  </div>
</template>
