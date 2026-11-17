<script setup lang="ts">
// The Munchies logo: a utensils glyph on a primary tile, plus the wordmark.
import { UtensilsCrossedIcon } from '@lucide/vue'

withDefaults(defineProps<{ size?: 'sm' | 'lg'; inverted?: boolean }>(), { size: 'sm', inverted: false })
</script>

<template>
  <span class="inline-flex items-center gap-2.5">
    <span
      aria-hidden="true"
      :class="[
        'grid place-items-center rounded-lg',
        size === 'lg' ? 'size-10' : 'size-8',
        inverted ? 'bg-primary-foreground text-primary' : 'bg-primary text-primary-foreground',
      ]"
    >
      <UtensilsCrossedIcon :class="size === 'lg' ? 'size-5' : 'size-4'" />
    </span>
    <span :class="['font-heading font-extrabold tracking-tight', size === 'lg' ? 'text-2xl' : 'text-lg']">
      Munchies
    </span>
  </span>
</template>
