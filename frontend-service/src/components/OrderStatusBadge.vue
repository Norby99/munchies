<script setup lang="ts">
// Order status as a colored pill. The color is never the only cue: the label is always shown.
import { computed } from 'vue'

import { Badge } from '@/components/ui/badge'
import { STATUS_LABELS, STATUS_TONES, type StatusTone } from '@/utils/orderStatus'
import type { OrderStatus } from '@/types'

const props = defineProps<{ status: OrderStatus }>()

const TONE_CLASSES: Record<StatusTone, string> = {
  warning: 'bg-warning/12 text-warning',
  info: 'bg-info/12 text-info',
  primary: 'bg-primary/10 text-primary',
  success: 'bg-success/12 text-success',
  muted: 'bg-muted text-muted-foreground',
}

const toneClass = computed(() => TONE_CLASSES[STATUS_TONES[props.status]])
</script>

<template>
  <Badge :class="['gap-1.5 border-transparent font-semibold', toneClass]">
    <span class="size-1.5 rounded-full bg-current" aria-hidden="true"></span>
    {{ STATUS_LABELS[status] }}
  </Badge>
</template>
