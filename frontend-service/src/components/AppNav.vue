<script setup lang="ts">
// App header, sticky on every page: brand, the role's nav links (from md up; phones get
// MobileTabBar instead, see AppShell), and the account menu with theme choice and logout.

import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { LogOutIcon, MonitorIcon, MoonIcon, SunIcon, UserRoundIcon } from '@lucide/vue'

import BrandMark from '@/components/BrandMark.vue'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useNavItems } from '@/composables/useNavItems'
import { useTheme } from '@/composables/useTheme'
import { homeFor } from '@/router'
import { useSessionStore } from '@/stores/session'
import { cn } from '@/lib/utils'

const session = useSessionStore()
const route = useRoute()
const router = useRouter()
const navItems = useNavItems()
const { choice } = useTheme()

const initials = computed(() => (session.user?.username ?? '?').slice(0, 2).toUpperCase())
const roleLabel = computed(() => (session.role === 'MANAGER' ? 'Manager' : 'Customer'))

function isActive(matches: string[]): boolean {
  return matches.includes(String(route.name))
}

async function logout(): Promise<void> {
  await session.logout()
  await router.push({ name: 'login' })
}
</script>

<template>
  <header class="sticky top-0 z-40 border-b bg-background/85 backdrop-blur supports-backdrop-filter:bg-background/70">
    <div class="mx-auto flex h-16 max-w-7xl items-center gap-6 px-4 md:px-6">
      <RouterLink :to="homeFor(session.role)" class="rounded-lg focus-visible:ring-3 focus-visible:ring-ring/50 focus-visible:outline-none">
        <BrandMark />
        <span class="sr-only">, home</span>
      </RouterLink>

      <nav aria-label="Main" class="hidden items-center gap-1 md:flex">
        <template v-for="item in navItems" :key="item.label">
          <span
            v-if="item.disabled"
            class="inline-flex h-9 items-center gap-2 rounded-lg px-3 text-sm font-medium text-muted-foreground/60"
            aria-disabled="true"
          >
            <component :is="item.icon" class="size-4" aria-hidden="true" />
            {{ item.label }}
          </span>
          <RouterLink
            v-else
            :to="item.to"
            :aria-current="isActive(item.matches) ? 'page' : undefined"
            :class="
              cn(
                'inline-flex h-9 max-w-56 items-center gap-2 rounded-lg px-3 text-sm font-medium transition-colors outline-none focus-visible:ring-3 focus-visible:ring-ring/50',
                isActive(item.matches)
                  ? 'bg-accent text-accent-foreground'
                  : 'text-muted-foreground hover:bg-muted hover:text-foreground',
              )
            "
          >
            <component :is="item.icon" class="size-4 shrink-0" aria-hidden="true" />
            <span class="truncate">{{ item.label }}</span>
          </RouterLink>
        </template>
      </nav>

      <div class="ml-auto flex items-center gap-2">
        <DropdownMenu>
          <DropdownMenuTrigger as-child>
            <Button variant="ghost" class="h-10 gap-2 rounded-full pr-3 pl-1" aria-label="Account menu">
              <Avatar class="size-8">
                <AvatarFallback class="bg-accent text-xs font-semibold text-accent-foreground">
                  {{ initials }}
                </AvatarFallback>
              </Avatar>
              <span class="hidden max-w-32 truncate text-sm font-medium sm:inline">
                {{ session.user?.username }}
              </span>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" class="w-60">
            <DropdownMenuLabel class="flex flex-col gap-1">
              <span class="flex items-center justify-between gap-2">
                <span class="truncate font-semibold text-foreground">{{ session.user?.username }}</span>
                <Badge variant="secondary">{{ roleLabel }}</Badge>
              </span>
              <span class="truncate text-xs font-normal text-muted-foreground">{{ session.user?.email }}</span>
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuGroup>
              <DropdownMenuItem @select="router.push({ name: 'account' })">
                <UserRoundIcon />
                Account settings
              </DropdownMenuItem>
            </DropdownMenuGroup>
            <DropdownMenuSeparator />
            <DropdownMenuLabel class="text-xs text-muted-foreground">Theme</DropdownMenuLabel>
            <DropdownMenuRadioGroup v-model="choice">
              <DropdownMenuRadioItem value="light"><SunIcon /> Light</DropdownMenuRadioItem>
              <DropdownMenuRadioItem value="dark"><MoonIcon /> Dark</DropdownMenuRadioItem>
              <DropdownMenuRadioItem value="auto"><MonitorIcon /> System</DropdownMenuRadioItem>
            </DropdownMenuRadioGroup>
            <DropdownMenuSeparator />
            <DropdownMenuGroup>
              <DropdownMenuItem variant="destructive" @select="logout">
                <LogOutIcon />
                Log out
              </DropdownMenuItem>
            </DropdownMenuGroup>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </div>
  </header>
</template>
