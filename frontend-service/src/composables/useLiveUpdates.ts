// Keeps a screen up to date with the live notifications: `refresh` runs when a notification
// matching `concerns` arrives, and after every reconnection (anything may have changed while the
// connection was down). The subscription lives as long as the component.
//
// Bursts are coalesced: several notifications within DEBOUNCE_MS trigger one refresh, so a manager
// advancing three orders in a row does not reload the board three times.

import { onMounted, onUnmounted, watch } from 'vue'
import { useDebounceFn } from '@vueuse/core'

import { useLiveStore } from '@/stores/live'
import type { LiveNotification } from '@/types'

const DEBOUNCE_MS = 300

export function useLiveUpdates(
  concerns: (notification: LiveNotification) => boolean,
  refresh: () => void | Promise<void>,
): void {
  const live = useLiveStore()
  const debounced = useDebounceFn(() => refresh(), DEBOUNCE_MS)
  let unsubscribe: (() => void) | null = null

  onMounted(() => {
    unsubscribe = live.subscribe((notification) => {
      if (concerns(notification)) void debounced()
    })
  })
  onUnmounted(() => unsubscribe?.())

  watch(
    () => live.resyncs,
    () => void debounced(),
  )
}
