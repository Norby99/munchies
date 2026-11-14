<script setup lang="ts">
// Routes flagged `bare` (login/register/verify) render without AppShell: no header, no nav, no
// offline banner. Every other route gets the full shell.

import { useRoute } from 'vue-router'

import AppShell from '@/components/AppShell.vue'

const route = useRoute()
</script>

<template>
  <AppShell v-if="!route.meta.bare">
    <RouterView />
  </AppShell>
  <RouterView v-else />
</template>
