// Live notifications: the one WebSocket connection of the signed-in user, the screens listening to
// it, the toasts it raises and the bell's history.
//
// The history (NotificationBell) lists what was toasted since the app was opened, so a toast that
// vanished while they were looking elsewhere is not lost. It lives only in memory: it is gone on
// reload or sign-out, and there is no server-side inbox (notification-service keeps no state).
//
// App.vue opens the connection after sign-in and closes it on sign-out. Screens subscribe through
// composables/useLiveUpdates, which refreshes them when a notification concerns their data, and
// again after a reconnection, since notifications pushed while the connection was down are lost
// (the server keeps no backlog: this is a live channel, the REST API stays the source of truth).

import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

import { openLiveConnection, type LiveConnection, type LiveStatus } from '@/api/live'
import { useRestaurantsStore } from '@/stores/restaurants'
import { useSessionStore } from '@/stores/session'
import { toastFor, type LiveToast } from '@/utils/liveNotifications'
import type { LiveNotification } from '@/types'

/** How long a toast stays when the user does not interact with it. */
export const TOAST_MS = 8_000
/** Older toasts are dropped beyond this many, so a burst never covers the screen. */
const MAX_TOASTS = 3
/** The bell keeps the most recent entries only. */
const MAX_HISTORY = 30

export interface ShownToast extends LiveToast {
  id: number
}

/** A bell entry: the same id as the toast it was shown as. */
export interface HistoryEntry extends ShownToast {
  receivedAt: Date
  read: boolean
}

type Listener = (notification: LiveNotification) => void

export const useLiveStore = defineStore('live', () => {
  const session = useSessionStore()
  const restaurants = useRestaurantsStore()

  const status = ref<LiveStatus | 'closed'>('closed')
  const toasts = ref<ShownToast[]>([])
  // Newest first.
  const history = ref<HistoryEntry[]>([])
  const unread = computed(() => history.value.filter((e) => !e.read).length)
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
      const shown = { ...toast, id: nextToastId++ }
      toasts.value = [...toasts.value, shown].slice(-MAX_TOASTS)
      history.value = [{ ...shown, receivedAt: new Date(), read: false }, ...history.value].slice(0, MAX_HISTORY)
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

  function close(): void {
    connection?.close()
    connection = null
    status.value = 'closed'
  }

  /** On sign-out: the next user must not see this one's notifications. */
  function disconnect(): void {
    close()
    toasts.value = []
    history.value = []
  }

  /** Reopens the connection, e.g. when a manager's restaurants change (they are read on connect). */
  function reconnect(): void {
    if (!connection) return
    close()
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

  /** Following a toast's or an entry's link counts as reading it. */
  function markRead(id: number): void {
    const entry = history.value.find((e) => e.id === id)
    if (entry) entry.read = true
  }

  function markAllRead(): void {
    history.value.forEach((e) => (e.read = true))
  }

  function clearHistory(): void {
    history.value = []
  }

  return {
    status,
    toasts,
    history,
    unread,
    resyncs,
    connect,
    disconnect,
    reconnect,
    subscribe,
    dismiss,
    markRead,
    markAllRead,
    clearHistory,
  }
})
