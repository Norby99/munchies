<script setup lang="ts">
// Toasts of the live notifications (stores/live.ts), stacked in a corner above the mobile tab bar.
//
// Accessibility: the container is a polite live region that exists from the start, so screen
// readers announce each toast as it is added without interrupting the user. A toast closes by
// itself after TOAST_MS, but the timer pauses while the pointer is over it or focus is inside it
// (WCAG 2.2.1 Timing Adjustable), and the information is never only here: the screen it links to
// shows it too.

import { onUnmounted, watch } from 'vue'
import { BellIcon, XIcon } from '@lucide/vue'

import { Button } from '@/components/ui/button'
import { TOAST_MS, useLiveStore } from '@/stores/live'

const live = useLiveStore()

const timers = new Map<number, ReturnType<typeof setTimeout>>()

function schedule(id: number): void {
  clearTimeout(timers.get(id))
  timers.set(id, setTimeout(() => close(id), TOAST_MS))
}

function pause(id: number): void {
  clearTimeout(timers.get(id))
  timers.delete(id)
}

function close(id: number): void {
  pause(id)
  live.dismiss(id)
}

/** Following the link closes the toast and marks its bell entry as read. */
function open(id: number): void {
  close(id)
  live.markRead(id)
}

// Start a timer for every new toast; forget those of toasts that are gone.
watch(
  () => live.toasts.map((t) => t.id),
  (ids) => {
    ids.filter((id) => !timers.has(id)).forEach(schedule)
    ;[...timers.keys()].filter((id) => !ids.includes(id)).forEach(pause)
  },
)

onUnmounted(() => timers.forEach((timer) => clearTimeout(timer)))
</script>

<template>
  <section
    aria-live="polite"
    aria-label="Notifications"
    class="pointer-events-none fixed inset-x-4 bottom-[calc(5rem+env(safe-area-inset-bottom))] z-50 flex flex-col items-end gap-2 md:right-6 md:bottom-6 md:left-auto md:w-96"
  >
    <TransitionGroup
      enter-from-class="translate-y-2 opacity-0"
      enter-active-class="transition duration-200 motion-reduce:transition-none"
      leave-active-class="transition duration-150 motion-reduce:transition-none"
      leave-to-class="opacity-0"
    >
      <article
        v-for="toast in live.toasts"
        :key="toast.id"
        class="pointer-events-auto flex w-full gap-3 rounded-xl border bg-popover p-4 text-popover-foreground shadow-lg"
        @mouseenter="pause(toast.id)"
        @mouseleave="schedule(toast.id)"
        @focusin="pause(toast.id)"
        @focusout="schedule(toast.id)"
      >
        <span class="grid size-9 shrink-0 place-items-center rounded-lg bg-accent text-accent-foreground" aria-hidden="true">
          <BellIcon class="size-4" />
        </span>
        <div class="flex min-w-0 flex-1 flex-col gap-1">
          <h2 class="text-sm font-semibold">{{ toast.title }}</h2>
          <p class="text-sm text-muted-foreground">{{ toast.body }}</p>
          <RouterLink
            :to="toast.to"
            class="mt-1 self-start text-sm font-semibold text-primary underline-offset-4 hover:underline focus-visible:underline"
            @click="open(toast.id)"
          >
            {{ toast.linkLabel }}
          </RouterLink>
        </div>
        <Button variant="ghost" size="icon-sm" class="-mt-1 -mr-1 shrink-0" aria-label="Dismiss notification" @click="close(toast.id)">
          <XIcon />
        </Button>
      </article>
    </TransitionGroup>
  </section>
</template>
