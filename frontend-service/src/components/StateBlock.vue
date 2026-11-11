<script setup lang="ts">
// Every data-backed screen renders one of four fetch states through this single component instead
// of branching per view: loading (skeleton), error (retry), empty (call to action) and ready (the
// default slot). Offline is a separate, app-wide axis: see AppShell's banner and useOnline().

import { type Component } from 'vue'
import { useRouter } from 'vue-router'
import { CloudAlertIcon, InboxIcon, RotateCwIcon } from '@lucide/vue'

import { Button } from '@/components/ui/button'
import { Empty, EmptyContent, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from '@/components/ui/empty'
import { Skeleton } from '@/components/ui/skeleton'
import type { LoadStatus } from '@/types'

const props = withDefaults(
  defineProps<{
    status: LoadStatus
    emptyTitle: string
    emptyBody: string
    emptyActionLabel?: string
    emptyIcon?: Component
    errorMessage?: string | null
  }>(),
  { emptyActionLabel: undefined, emptyIcon: () => InboxIcon, errorMessage: null },
)

const emit = defineEmits<{ retry: []; emptyAction: [] }>()

const router = useRouter()
</script>

<template>
  <div>
    <div v-if="props.status === 'loading'" class="flex flex-col gap-4" aria-busy="true">
      <span class="sr-only" role="status">Loading...</span>
      <Skeleton class="h-8 w-56" />
      <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <Skeleton v-for="card in 6" :key="card" class="h-32 rounded-xl" />
      </div>
    </div>

    <Empty v-else-if="props.status === 'error'" class="border border-dashed bg-card">
      <EmptyHeader>
        <EmptyMedia variant="icon" class="bg-destructive/10 text-destructive">
          <CloudAlertIcon />
        </EmptyMedia>
        <EmptyTitle>Something went wrong</EmptyTitle>
        <EmptyDescription role="alert">
          {{ props.errorMessage ?? 'We could not load this page. Nothing was changed.' }}
        </EmptyDescription>
      </EmptyHeader>
      <EmptyContent class="flex-row justify-center">
        <Button @click="emit('retry')"><RotateCwIcon data-icon="inline-start" /> Try again</Button>
        <Button variant="outline" @click="router.back()">Go back</Button>
      </EmptyContent>
    </Empty>

    <Empty v-else-if="props.status === 'empty'" class="border border-dashed bg-card">
      <EmptyHeader>
        <EmptyMedia variant="icon">
          <component :is="props.emptyIcon" />
        </EmptyMedia>
        <EmptyTitle>{{ props.emptyTitle }}</EmptyTitle>
        <EmptyDescription>{{ props.emptyBody }}</EmptyDescription>
      </EmptyHeader>
      <EmptyContent v-if="props.emptyActionLabel">
        <Button @click="emit('emptyAction')">{{ props.emptyActionLabel }}</Button>
      </EmptyContent>
    </Empty>

    <slot v-else />
  </div>
</template>
