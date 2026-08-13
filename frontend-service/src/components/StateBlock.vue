<script setup lang="ts">
// Every data-backed screen (restaurants list, menu builder, restaurant
// detail) renders one of four fetch states through this single component
// instead of branching per-view. Offline is a separate, app-wide axis — see
// AppShell's banner and useOnline() — not one of these four.

import { useRouter } from 'vue-router'

import type { LoadStatus } from '@/types'

const props = defineProps<{
  status: LoadStatus
  emptyTitle: string
  emptyBody: string
  emptyActionLabel: string
  errorMessage?: string | null
}>()

const emit = defineEmits<{ retry: []; emptyAction: [] }>()

const router = useRouter()
</script>

<template>
  <div>
    <div v-if="props.status === 'loading'" style="display: flex; flex-direction: column; gap: 16px">
      <div class="state-block__skeleton-title"></div>
      <div class="hr" style="margin: 0"></div>
      <div v-for="row in 5" :key="row" class="state-block__skeleton-row">
        <div class="state-block__skeleton-bar"></div>
        <div class="state-block__skeleton-bar"></div>
        <div class="state-block__skeleton-bar"></div>
      </div>
      <p class="text-muted" style="font-size: 12px; margin: 0">
        Skeleton rows keep the grid from reflowing when the envelope resolves.
      </p>
    </div>

    <div v-else-if="props.status === 'error'" style="max-width: 520px">
      <span class="tag tag-outline">error</span>
      <h2 style="margin: 12px 0 8px">The request failed</h2>
      <p class="text-muted" style="font-size: 14px">
        {{ props.errorMessage ?? 'The gateway returned an error. Nothing was saved.' }}
      </p>
      <div class="hr"></div>
      <div style="display: flex; gap: 10px">
        <button class="btn btn-primary" type="button" @click="emit('retry')">Try again</button>
        <button class="btn btn-secondary" type="button" @click="router.back()">Go back</button>
      </div>
    </div>

    <div v-else-if="props.status === 'empty'" style="max-width: 560px">
      <h2 style="margin: 0 0 8px">{{ props.emptyTitle }}</h2>
      <p class="text-muted" style="font-size: 14px">{{ props.emptyBody }}</p>
      <div class="hr"></div>
      <button class="btn btn-primary" type="button" @click="emit('emptyAction')">
        {{ props.emptyActionLabel }}
      </button>
    </div>

    <slot v-else />
  </div>
</template>

<style scoped>
.state-block__skeleton-title {
  height: 38px;
  width: 280px;
  background: var(--color-neutral-200);
  animation: skel 1.4s ease-in-out infinite;
}

.state-block__skeleton-row {
  display: grid;
  grid-template-columns: 2fr 3fr 1fr;
  gap: 16px;
}

.state-block__skeleton-bar {
  height: 16px;
  background: var(--color-neutral-200);
  animation: skel 1.4s ease-in-out infinite;
}

@keyframes skel {
  0% { opacity: 0.35; }
  50% { opacity: 0.8; }
  100% { opacity: 0.35; }
}
</style>
