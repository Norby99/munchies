// Live driver position of one delivery order, read from scheduler-service.
//
// This is the one place in the app that polls, on purpose. Notifications ("your order is on the
// way") are events and will be pushed by the server over the notification WebSocket; the driver
// position is state that only matters while someone is looking at it, so the UI asks for it. The
// loop is as narrow as it can be: it runs only while `orderId` is set (the caller sets it only for
// a delivery that is on the way), only while the page is visible, and it stops for good once the
// driver has arrived or the scheduler has no tracking for the order.

import { onScopeDispose, ref, toValue, watch, type MaybeRefOrGetter } from 'vue'
import { useDocumentVisibility, useIntervalFn } from '@vueuse/core'

import { ApiError } from '@/api/client'
import * as scheduleApi from '@/api/schedule'
import type { OrderTracking } from '@/api/schedule'

export type TrackingState = 'idle' | 'loading' | 'ready' | 'unavailable' | 'error'

// The scheduler moves the driver every 5 seconds by default: asking more often shows nothing new.
const POLL_MS = 5_000

export function useDeliveryTracking(orderId: MaybeRefOrGetter<string | null>) {
  const tracking = ref<OrderTracking | null>(null)
  const state = ref<TrackingState>('idle')
  const visibility = useDocumentVisibility()
  // Guards against a slow answer for a previous order overwriting the current one.
  let generation = 0
  let disposed = false

  async function fetchOnce(): Promise<void> {
    const id = toValue(orderId)
    if (!id) return
    const requested = generation
    try {
      const result = await scheduleApi.getOrderTracking(id)
      if (requested !== generation || disposed) return
      tracking.value = result
      state.value = 'ready'
      if (result.status === 'ARRIVED') pause()
    } catch (err) {
      if (requested !== generation || disposed) return
      if (err instanceof ApiError && err.code === 404) {
        tracking.value = null
        state.value = 'unavailable'
        pause()
      } else if (!tracking.value) {
        // With a position already on screen, a failed refresh keeps showing the last known one.
        state.value = 'error'
      }
    }
  }

  const { pause, resume } = useIntervalFn(fetchOnce, POLL_MS, { immediate: false })

  function settled(): boolean {
    return state.value === 'unavailable' || tracking.value?.status === 'ARRIVED'
  }

  watch(
    () => toValue(orderId),
    (id) => {
      generation++
      pause()
      tracking.value = null
      state.value = id ? 'loading' : 'idle'
      if (!id) return
      void fetchOnce()
      if (visibility.value === 'visible') resume()
    },
    { immediate: true },
  )

  watch(visibility, (now) => {
    if (now !== 'visible') {
      pause()
    } else if (toValue(orderId) && !settled()) {
      void fetchOnce()
      resume()
    }
  })

  onScopeDispose(() => {
    disposed = true
  })

  return { tracking, state, refresh: fetchOnce }
}
