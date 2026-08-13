<script setup lang="ts">
// The auth screens (login/register/verify) render without AppShell — no
// header, no nav, no offline banner — everything else gets the full shell.

import { useRoute } from 'vue-router'

import AppShell from '@/components/AppShell.vue'

const route = useRoute()
</script>

<template>
  <AppShell v-if="!route.meta.public">
    <router-view />
  </AppShell>
  <main v-else class="app-auth-main">
    <router-view />
  </main>
</template>

<style scoped>
.app-auth-main {
  min-height: 100vh;
  padding: 32px 24px 56px;
}

@media (min-width: 1440px) {
  .app-auth-main {
    padding: 32px 40px 56px;
  }
}

@media (max-width: 767px) {
  .app-auth-main {
    padding: 20px 16px 8px;
  }
}
</style>
