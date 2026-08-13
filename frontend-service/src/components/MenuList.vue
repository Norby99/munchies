<script setup lang="ts">
// Builder's left column: the menu picker. The list endpoint only returns
// id + name (ui_specification.md §5.3) — the validity description under each
// name only appears once that menu has been fetched in full at least once,
// rather than guessing or hiding the limitation.

import { describe } from '@/utils/validity'
import type { Menu, MenuSummary } from '@/types'

const props = defineProps<{
  menus: MenuSummary[]
  cache: Record<string, Menu>
  activeId: string | null
}>()

const emit = defineEmits<{ select: [menuId: string] }>()
</script>

<template>
  <div>
    <h6 style="margin-bottom: 10px">Menus</h6>
    <div style="display: flex; flex-direction: column; gap: 2px">
      <button
        v-for="menu in props.menus"
        :key="menu.id"
        type="button"
        class="menu-list__row"
        :class="{ 'menu-list__row--active': menu.id === props.activeId }"
        :aria-pressed="menu.id === props.activeId"
        @click="emit('select', menu.id)"
      >
        <span style="font-weight: 800; font-size: 14px">{{ menu.name }}</span>
        <span style="font-size: 11px; opacity: 0.7">
          {{ props.cache[menu.id] ? describe(props.cache[menu.id].validity) : '—' }}
        </span>
      </button>
    </div>
    <p class="text-muted" style="font-size: 11px; margin-top: 12px">
      List returns id + name only. Categories and validity need the full menu fetch.
    </p>
  </div>
</template>

<style scoped>
.menu-list__row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  width: 100%;
  text-align: left;
  font: inherit;
  cursor: pointer;
  padding: 10px 12px;
  border: 0;
  border-left: 2px solid transparent;
  background: transparent;
  color: inherit;
}

.menu-list__row--active {
  border-left-color: var(--color-accent);
  background: var(--color-neutral-200);
}
</style>
