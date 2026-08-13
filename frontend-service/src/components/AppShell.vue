<script setup lang="ts">
// One shell, nav changes by role — not two apps. Wraps every non-auth route:
// header, offline banner, main content slot, mobile tab bar, and the
// session-expired dialog that the axios interceptor can raise from anywhere.

import { useBreakpoints } from '@/composables/useBreakpoints'
import { useOnline } from '@/composables/useOnline'
import AppNav from '@/components/AppNav.vue'
import MobileTabBar from '@/components/MobileTabBar.vue'
import SessionExpiredDialog from '@/components/SessionExpiredDialog.vue'

const { isMobile, isWide } = useBreakpoints()
const { isOnline } = useOnline()
</script>

<template>
  <div class="app-shell">
    <AppNav />

    <div v-if="!isOnline" class="app-shell__offline">
      <span class="app-shell__offline-dot" aria-hidden="true"></span>
      <span>Offline. Showing the last cached menu data. Writes are disabled until the connection returns.</span>
    </div>

    <main class="app-shell__main" :class="{ 'app-shell__main--mobile': isMobile, 'app-shell__main--wide': isWide }">
      <slot />
    </main>

    <MobileTabBar v-if="isMobile" />

    <SessionExpiredDialog />
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-shell__offline {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  background: var(--color-text);
  color: var(--color-bg);
  font-size: 13px;
}

.app-shell__offline-dot {
  width: 8px;
  height: 8px;
  flex: none;
  background: var(--color-accent-500);
}

.app-shell__main {
  flex: 1;
  width: 100%;
  max-width: 1440px;
  margin: 0 auto;
  padding: 32px 24px 56px;
}

.app-shell__main--wide {
  padding: 32px 40px 56px;
}

.app-shell__main--mobile {
  padding: 20px 16px 8px;
}
</style>
