<script setup lang="ts">
// Menu builder's menu picker. The list endpoint only returns id + name, so a menu's availability
// is shown only once it has been fetched in full at least once.

import { describe } from '@/utils/validity'
import { cn } from '@/lib/utils'
import type { Menu, MenuSummary } from '@/types'

const props = defineProps<{
  menus: MenuSummary[]
  cache: Record<string, Menu>
  activeId: string | null
}>()

const emit = defineEmits<{ select: [menuId: string] }>()
</script>

<template>
  <nav aria-label="Menus" class="flex flex-col gap-2">
    <h2 class="px-1 text-xs font-semibold tracking-wider text-muted-foreground uppercase">Menus</h2>
    <ul class="flex gap-2 overflow-x-auto pb-1 xl:flex-col xl:overflow-visible">
      <li v-for="menu in props.menus" :key="menu.id" class="shrink-0">
        <button
          type="button"
          :aria-pressed="menu.id === props.activeId"
          :class="
            cn(
              'flex w-full flex-col items-start gap-0.5 rounded-xl border px-3 py-2.5 text-left transition-colors outline-none focus-visible:ring-3 focus-visible:ring-ring/50',
              menu.id === props.activeId
                ? 'border-primary/30 bg-accent text-accent-foreground'
                : 'border-transparent hover:bg-muted',
            )
          "
          @click="emit('select', menu.id)"
        >
          <span class="text-sm font-semibold">{{ menu.name }}</span>
          <span class="text-xs opacity-80">
            {{ props.cache[menu.id] ? describe(props.cache[menu.id].validity) : 'Not loaded yet' }}
          </span>
        </button>
      </li>
    </ul>
  </nav>
</template>
