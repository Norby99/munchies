<script setup lang="ts">
// Header bell: the live notifications received since the app was opened (stores/live.ts history), so a toast
// that disappeared while the user was looking elsewhere can still be found. Session-only by design:
// the list is in memory and is lost on reload or sign-out.
//
// Unread entries keep their dot while the menu is open, and become read when it closes: the user
// sees what is new, and the counter is clear afterwards. Choosing an entry opens its screen.
//
// Built on the same DropdownMenu as the account menu, for arrow-key navigation and focus handling.

import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { BellIcon } from '@lucide/vue'

import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useLiveStore, type HistoryEntry } from '@/stores/live'
import { cn } from '@/lib/utils'

const live = useLiveStore()
const router = useRouter()

// Relative times are computed when the menu opens, not ticking while it is open.
const now = ref(Date.now())

const badge = computed(() => (live.unread > 9 ? '9+' : String(live.unread)))
const triggerLabel = computed(() =>
  live.unread === 0 ? 'Notifications' : `Notifications, ${live.unread} unread`,
)

const relative = new Intl.RelativeTimeFormat('en', { numeric: 'auto' })

function ago(date: Date): string {
  const seconds = Math.round((date.getTime() - now.value) / 1000)
  if (seconds > -60) return 'just now'
  const minutes = Math.round(seconds / 60)
  if (minutes > -60) return relative.format(minutes, 'minute')
  return relative.format(Math.round(minutes / 60), 'hour')
}

function onOpenChange(open: boolean): void {
  if (open) now.value = Date.now()
  else live.markAllRead()
}

function follow(entry: HistoryEntry): void {
  live.markRead(entry.id)
  void router.push(entry.to)
}
</script>

<template>
  <DropdownMenu @update:open="onOpenChange">
    <DropdownMenuTrigger as-child>
      <Button variant="ghost" size="icon" class="relative rounded-full" :aria-label="triggerLabel">
        <BellIcon />
        <span
          v-if="live.unread > 0"
          class="absolute -top-0.5 -right-0.5 grid h-4.5 min-w-4.5 place-items-center rounded-full bg-primary px-1 text-[0.625rem] leading-none font-semibold text-primary-foreground ring-2 ring-background"
          aria-hidden="true"
        >
          {{ badge }}
        </span>
      </Button>
    </DropdownMenuTrigger>

    <DropdownMenuContent align="end" class="w-[min(22rem,calc(100vw-2rem))]">
      <DropdownMenuLabel class="flex items-baseline justify-between gap-2">
        <span class="font-semibold text-foreground">Notifications</span>
        <span v-if="live.history.length > 0" class="text-xs font-normal text-muted-foreground">
          Since you opened the app
        </span>
      </DropdownMenuLabel>
      <DropdownMenuSeparator />

      <p v-if="live.history.length === 0" class="px-2 py-6 text-center text-sm text-muted-foreground">
        Nothing yet. Updates on your orders and bookings appear here while the app is open.
      </p>

      <template v-else>
        <DropdownMenuGroup class="max-h-[min(24rem,60vh)] overflow-y-auto">
          <DropdownMenuItem
            v-for="entry in live.history"
            :key="entry.id"
            class="items-start gap-3 py-2.5"
            @select="follow(entry)"
          >
            <span
              :class="cn('mt-1.5 size-2 shrink-0 rounded-full', entry.read ? 'bg-transparent' : 'bg-primary')"
              aria-hidden="true"
            />
            <span class="flex min-w-0 flex-1 flex-col gap-0.5">
              <span class="flex items-baseline justify-between gap-2">
                <span :class="cn('truncate text-sm', entry.read ? 'font-medium' : 'font-semibold')">
                  {{ entry.title }}
                </span>
                <time :datetime="entry.receivedAt.toISOString()" class="shrink-0 text-xs text-muted-foreground">
                  {{ ago(entry.receivedAt) }}
                </time>
              </span>
              <span class="line-clamp-2 text-xs text-muted-foreground">{{ entry.body }}</span>
              <span v-if="!entry.read" class="sr-only">Unread.</span>
            </span>
          </DropdownMenuItem>
        </DropdownMenuGroup>
        <DropdownMenuSeparator />
        <DropdownMenuItem class="justify-center text-muted-foreground" @select="live.clearHistory()">
          Clear all
        </DropdownMenuItem>
      </template>
    </DropdownMenuContent>
  </DropdownMenu>
</template>
