// Live notifications: the one WebSocket connection of the signed-in user, the screens listening to
// it, and the toasts it raises.
//
// App.vue opens the connection after sign-in and closes it on sign-out. Screens subscribe through
// composables/useLiveUpdates, which refreshes them when a notification concerns their data, and
// again after a reconnection, since notifications pushed while the connection was down are lost
// (the server keeps no backlog: this is a live channel, the REST API stays the source of truth).

import { defineStore } from 'pinia'
import { ref } from 'vue'

import { openLiveConnection, type LiveConnection, type LiveStatus } from '@/api/live'
import { useRestaurantsStore } from '@/stores/restaurants'
import { useSessionStore } from '@/stores/session'
import { toastFor, type LiveToast } from '@/utils/liveNotifications'
import type { LiveNotification } from '@/types'

/** How long a toast stays when the user does not interact with it. */
export const TOAST_MS = 8_000
/** Older toasts are dropped beyond this many, so a burst never covers the screen. */
const MAX_TOASTS = 3

export interface ShownToast extends LiveToast {
  id: number
}

type Listener = (notification: LiveNotification) => void

export const useLiveStore = defineStore('live', () => {
  const session = useSessionStore()
  const restaurants = useRestaurantsStore()

  const status = ref<LiveStatus | 'closed'>('closed')
  const toasts = ref<ShownToast[]>([])
  // Bumped on every reconnection: useLiveUpdates watches it to refetch what may have been missed.
  const resyncs = ref(0)

  const listeners = new Set<Listener>()
  let connection: LiveConnection | null = null
  let nextToastId = 0

  function receive(notification: LiveNotification): void {
    listeners.forEach((listener) => listener(notification))
    const toast = toastFor(
      notification,
      session.role,
      (id) => restaurants.items.find((r) => r.id === id)?.name,
    )
    if (toast) {
      toasts.value = [...toasts.value, { ...toast, id: nextToastId++ }].slice(-MAX_TOASTS)
    }
  }

  function connect(): void {
    if (connection) return
    connection = openLiveConnection({
      onNotification: receive,
      onStatus: (next) => (status.value = next),
      onReconnect: () => resyncs.value++,
    })
  }

  function disconnect(): void {
    connection?.close()
    connection = null
    status.value = 'closed'
    toasts.value = []
  }

  /** Reopens the connection, e.g. when a manager's restaurants change (they are read on connect). */
  function reconnect(): void {
    if (!connection) return
    disconnect()
    connect()
    resyncs.value++
  }

  function subscribe(listener: Listener): () => void {
    listeners.add(listener)
    return () => listeners.delete(listener)
  }

  function dismiss(id: number): void {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }

  return { status, toasts, resyncs, connect, disconnect, reconnect, subscribe, dismiss }
})
