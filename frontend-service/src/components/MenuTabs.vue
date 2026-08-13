<script setup lang="ts">
// Customer detail screen's menu picker. Unlike the manager builder's menu
// list, every tab needs its availability dot up front, so the view fetches
// each menu in full rather than showing name-only summaries here.

import { describe, isValid } from '@/utils/validity'
import type { Menu } from '@/types'

const props = defineProps<{ menus: Menu[]; activeId: string | null }>()
const emit = defineEmits<{ select: [menuId: string] }>()
</script>

<template>
  <div class="menu-tabs">
    <button
      v-for="menu in props.menus"
      :key="menu.id"
      type="button"
      class="menu-tabs__tab"
      :class="{ 'menu-tabs__tab--active': menu.id === props.activeId }"
      :aria-pressed="menu.id === props.activeId"
      @click="emit('select', menu.id)"
    >
      <span style="font-weight: 800; font-size: 14px">{{ menu.name }}</span>
      <span class="menu-tabs__dot" :class="{ 'menu-tabs__dot--open': isValid(menu.validity) }"></span>
      <span style="font-size: 11px; opacity: 0.75">
        {{ isValid(menu.validity) ? 'Available now' : describe(menu.validity) }}
      </span>
    </button>
  </div>
</template>

<style scoped>
.menu-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
  margin-bottom: 20px;
}

.menu-tabs__tab {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
  flex: none;
  font: inherit;
  cursor: pointer;
  padding: 10px 14px;
  min-height: 44px;
  border: 1px solid var(--color-divider);
  background: transparent;
  color: var(--color-text);
}

.menu-tabs__tab--active {
  border-color: var(--color-text);
  background: var(--color-text);
  color: var(--color-bg);
}

.menu-tabs__dot {
  width: 7px;
  height: 7px;
  flex: none;
  background: transparent;
  border: 1px solid currentColor;
}

.menu-tabs__dot--open {
  background: var(--color-accent);
  border-color: var(--color-accent);
}
</style>
