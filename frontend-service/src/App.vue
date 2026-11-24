<script setup lang="ts">
// Routes flagged `bare` (login/register/verify) render without AppShell: no header, no nav, no
// offline banner. Every other route gets the full shell.
//
// The live-notification connection follows the session: opened once the user is signed in,
// closed when they sign out or the session expires. (A manager's connection is subscribed to the
// restaurants they own when it opens: stores/restaurants reopens it when one is created or
// deleted.)

import { watch } from 'vue'
import { useRoute } from 'vue-router'

import AppShell from '@/components/AppShell.vue'
import LiveToasts from '@/components/LiveToasts.vue'
import { useLiveStore } from '@/stores/live'
import { useSessionStore } from '@/stores/session'

const route = useRoute()
const session = useSessionStore()
const live = useLiveStore()

watch(
  () => session.isAuthenticated,
  (signedIn) => (signedIn ? live.connect() : live.disconnect()),
  { immediate: true },
)
</script>

<template>
  <AppShell v-if="!route.meta.bare">
    <RouterView />
  </AppShell>
  <RouterView v-else />
  <LiveToasts />
</template>
