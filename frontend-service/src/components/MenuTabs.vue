<script setup lang="ts">
// Customer menu picker: one pill per menu, each with its availability, since every menu is
// fetched in full up front.

import { describe, isValid } from '@/utils/validity'
import { cn } from '@/lib/utils'
import type { Menu } from '@/types'

const props = defineProps<{ menus: Menu[]; activeId: string | null }>()
const emit = defineEmits<{ select: [menuId: string] }>()
</script>

<template>
  <div class="-mx-4 mb-6 flex gap-2 overflow-x-auto px-4 pb-1 md:mx-0 md:flex-wrap md:px-0" role="group" aria-label="Menus">
    <button
      v-for="menu in props.menus"
      :key="menu.id"
      type="button"
      :aria-pressed="menu.id === props.activeId"
      :class="
        cn(
          'flex shrink-0 flex-col items-start gap-0.5 rounded-xl border px-4 py-2.5 text-left transition-colors outline-none focus-visible:ring-3 focus-visible:ring-ring/50',
          menu.id === props.activeId
            ? 'border-foreground bg-foreground text-background'
            : 'bg-card hover:border-foreground/30 hover:bg-muted',
        )
      "
      @click="emit('select', menu.id)"
    >
      <span class="text-sm font-semibold">{{ menu.name }}</span>
      <span class="flex items-center gap-1.5 text-xs opacity-80">
        <span
          :class="['size-1.5 rounded-full', isValid(menu.validity) ? 'bg-success' : 'bg-current opacity-50']"
          aria-hidden="true"
        ></span>
        {{ isValid(menu.validity) ? 'Open now' : describe(menu.validity) }}
      </span>
    </button>
  </div>
</template>
