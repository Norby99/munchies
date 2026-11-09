// Ids of the orders placed from this browser, newest first. This is a stopgap for the missing
// order-history endpoint (see api/orders.ts): the order itself always comes from the server,
// only the list of ids is local. Scoped per user so that a shared browser does not leak one
// account's orders to the next one. Storage access is guarded because it can throw (private
// mode, blocked site data); the app keeps working, it just forgets the list.

import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

import { useSessionStore } from '@/stores/session'

const STORAGE_PREFIX = 'munchies.orders.'
const MAX_REMEMBERED = 20

function read(userId: string): string[] {
  try {
    const raw = localStorage.getItem(STORAGE_PREFIX + userId)
    const parsed: unknown = raw ? JSON.parse(raw) : []
    return Array.isArray(parsed) ? parsed.filter((id): id is string => typeof id === 'string') : []
  } catch {
    return []
  }
}

function write(userId: string, ids: string[]): void {
  try {
    localStorage.setItem(STORAGE_PREFIX + userId, JSON.stringify(ids))
  } catch {
    // Storage unavailable: the list simply is not persisted.
  }
}

export const useOrdersStore = defineStore('orders', () => {
  const session = useSessionStore()
  const recentIds = ref<string[]>([])

  // Reload the list whenever the signed-in user changes (login, logout, account switch).
  watch(
    () => session.user?.id ?? null,
    (userId) => {
      recentIds.value = userId ? read(userId) : []
    },
    { immediate: true },
  )

  function remember(orderId: string): void {
    const userId = session.user?.id
    if (!userId) return
    recentIds.value = [orderId, ...recentIds.value.filter((id) => id !== orderId)].slice(0, MAX_REMEMBERED)
    write(userId, recentIds.value)
  }

  // A discarded order is deleted server-side (GET then answers 404), so it is dropped here too.
  function forget(orderId: string): void {
    const userId = session.user?.id
    if (!userId) return
    recentIds.value = recentIds.value.filter((id) => id !== orderId)
    write(userId, recentIds.value)
  }

  return { recentIds, remember, forget }
})
