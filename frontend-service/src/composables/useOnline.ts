// Global online/offline signal, backed by a single shared listener pair.
// Every data-backed store's `offline` state derives from this rather than
// each maintaining its own navigator.onLine listener.

import { onScopeDispose, ref } from 'vue'

const isOnline = ref(typeof navigator === 'undefined' ? true : navigator.onLine)
let listeners = 0

function handleOnline(): void {
  isOnline.value = true
}

function handleOffline(): void {
  isOnline.value = false
}

export function useOnline() {
  if (typeof window !== 'undefined') {
    if (listeners === 0) {
      window.addEventListener('online', handleOnline)
      window.addEventListener('offline', handleOffline)
    }
    listeners += 1

    onScopeDispose(() => {
      listeners -= 1
      if (listeners === 0) {
        window.removeEventListener('online', handleOnline)
        window.removeEventListener('offline', handleOffline)
      }
    })
  }

  return { isOnline }
}
