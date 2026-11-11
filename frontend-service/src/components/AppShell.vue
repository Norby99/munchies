<script setup lang="ts">
// One shell, nav changes by role: not two apps. Wraps every non-auth route: header, offline
// banner, main content, the mobile tab bar, and the session-expired dialog that the axios
// interceptor can raise from anywhere.

import { WifiOffIcon } from '@lucide/vue'

import AppNav from '@/components/AppNav.vue'
import MobileTabBar from '@/components/MobileTabBar.vue'
import SessionExpiredDialog from '@/components/SessionExpiredDialog.vue'
import { useOnline } from '@/composables/useOnline'

const { isOnline } = useOnline()
</script>

<template>
  <div class="flex min-h-svh flex-col">
    <a
      href="#main"
      class="sr-only z-50 rounded-lg bg-primary px-4 py-2 text-primary-foreground focus:not-sr-only focus:fixed focus:top-3 focus:left-3"
    >
      Skip to content
    </a>

    <AppNav />

    <div
      v-if="!isOnline"
      role="status"
      class="flex items-center justify-center gap-2 bg-foreground px-4 py-2 text-center text-sm text-background"
    >
      <WifiOffIcon class="size-4 shrink-0" aria-hidden="true" />
      You are offline. Menus may be out of date, and orders can be placed once you are back online.
    </div>

    <!-- Bottom padding on phones leaves room for the fixed tab bar. -->
    <main id="main" tabindex="-1" class="mx-auto w-full max-w-7xl flex-1 px-4 pt-6 pb-28 outline-none md:px-6 md:pt-10 md:pb-16">
      <slot />
    </main>

    <MobileTabBar />

    <SessionExpiredDialog />
  </div>
</template>
