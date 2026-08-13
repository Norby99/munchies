<script setup lang="ts">
// Shared frame for the three auth screens (login/register/verify): split-free
// single column, flush left, max-width 380px, vertically centered on desktop.
// These screens render without AppShell entirely.

import { useBreakpoints } from '@/composables/useBreakpoints'

defineProps<{ kicker: string; heading: string }>()

const { isMobile } = useBreakpoints()
</script>

<template>
  <div class="auth-layout" :class="{ 'auth-layout--mobile': isMobile }">
    <div class="auth-layout__panel">
      <h6 style="color: var(--color-accent); margin-bottom: 16px">{{ kicker }}</h6>
      <h1 class="auth-layout__heading" :class="{ 'auth-layout__heading--mobile': isMobile }">
        {{ heading }}
      </h1>
      <slot />
    </div>
  </div>
</template>

<style scoped>
.auth-layout {
  display: flex;
  align-items: center;
  min-height: 560px;
  padding-left: 24px;
}

.auth-layout--mobile {
  display: flex;
  align-items: stretch;
  min-height: 0;
  padding: 8px 0 0;
}

.auth-layout__panel {
  max-width: 380px;
  width: 100%;
}

.auth-layout__heading {
  margin: 0 0 12px;
  font-size: 46px;
}

.auth-layout__heading--mobile {
  font-size: 34px;
}
</style>
