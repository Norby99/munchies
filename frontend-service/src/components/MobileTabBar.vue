<script setup lang="ts">
// Bottom tab bar replacing the header links on phones (hidden from md up): fixed, one equal-width
// tab per nav item with icon and label, 56px tall (WCAG 2.2 target size), and padded for the
// home indicator on notched devices.

import { useRoute } from 'vue-router'

import { useNavItems } from '@/composables/useNavItems'
import { cn } from '@/lib/utils'

const navItems = useNavItems()
const route = useRoute()

function isActive(matches: string[]): boolean {
  return matches.includes(String(route.name))
}
</script>

<template>
  <nav
    aria-label="Main"
    class="fixed inset-x-0 bottom-0 z-40 border-t bg-background/95 pb-[env(safe-area-inset-bottom)] backdrop-blur md:hidden"
  >
    <ul class="flex">
      <li v-for="item in navItems" :key="item.label" class="flex-1">
        <span
          v-if="item.disabled"
          class="flex h-14 flex-col items-center justify-center gap-1 text-[11px] font-medium text-muted-foreground/50"
          aria-disabled="true"
        >
          <component :is="item.icon" class="size-5" aria-hidden="true" />
          <span class="max-w-full truncate px-1">{{ item.label }}</span>
        </span>
        <RouterLink
          v-else
          :to="item.to"
          :aria-current="isActive(item.matches) ? 'page' : undefined"
          :class="
            cn(
              'flex h-14 flex-col items-center justify-center gap-1 text-[11px] font-medium outline-none focus-visible:bg-muted',
              isActive(item.matches) ? 'text-primary' : 'text-muted-foreground',
            )
          "
        >
          <component :is="item.icon" class="size-5" aria-hidden="true" />
          <span class="max-w-full truncate px-1">{{ item.label }}</span>
        </RouterLink>
      </li>
    </ul>
  </nav>
</template>
