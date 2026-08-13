<script setup lang="ts">
// Bottom tab bar replacing the desktop nav on mobile — sticky, one equal-flex
// link per nav item, 44px minimum hit target.

import { useNavItems } from '@/composables/useNavItems'

const navItems = useNavItems()
</script>

<template>
  <nav class="mobile-tab-bar">
    <router-link
      v-for="item in navItems"
      :key="item.label"
      :to="item.to"
      class="mobile-tab-bar__link"
      :class="{ 'mobile-tab-bar__link--disabled': item.disabled }"
      active-class="mobile-tab-bar__link--active"
      :tabindex="item.disabled ? -1 : undefined"
      :aria-disabled="item.disabled || undefined"
    >
      {{ item.label }}
    </router-link>
  </nav>
</template>

<style scoped>
.mobile-tab-bar {
  display: flex;
  border-top: 2px solid var(--color-divider);
  background: var(--color-bg);
  position: sticky;
  bottom: 0;
}

.mobile-tab-bar__link {
  flex: 1;
  text-align: center;
  padding: 14px 8px;
  min-height: 44px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  text-decoration: none;
  color: var(--color-text);
  border-top: 3px solid transparent;
}

.mobile-tab-bar__link--active {
  color: var(--color-accent);
  border-top-color: var(--color-accent);
}

.mobile-tab-bar__link--disabled {
  opacity: 0.45;
  pointer-events: none;
}
</style>
