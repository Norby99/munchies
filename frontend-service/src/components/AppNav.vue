<script setup lang="ts">
// App header — brand, role tag, logout — always visible. The text nav links
// only render at desktop widths; mobile gets MobileTabBar instead (see
// AppShell), so the two never appear together.

import { useRouter } from 'vue-router'

import { useBreakpoints } from '@/composables/useBreakpoints'
import { useNavItems } from '@/composables/useNavItems'
import { useSessionStore } from '@/stores/session'

const session = useSessionStore()
const router = useRouter()
const navItems = useNavItems()
const { isMobile } = useBreakpoints()

function logout(): void {
  session.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <header class="app-nav" :class="{ 'app-nav--mobile': isMobile }">
    <span class="app-nav__brand">Munchies</span>
    <nav v-if="!isMobile" class="app-nav__links">
      <router-link
        v-for="item in navItems"
        :key="item.label"
        :to="item.to"
        class="app-nav__link"
        :class="{ 'app-nav__link--disabled': item.disabled }"
        active-class="app-nav__link--active"
        :tabindex="item.disabled ? -1 : undefined"
        :aria-disabled="item.disabled || undefined"
      >
        {{ item.label }}
      </router-link>
    </nav>
    <span class="tag tag-neutral app-nav__role">{{ session.role }}</span>
    <button
      class="btn btn-ghost app-nav__logout"
      type="button"
      title="No logout endpoint exists — clears client state only"
      @click="logout"
    >
      Log out
    </button>
  </header>
</template>

<style scoped>
.app-nav {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 40px;
  border-bottom: 2px solid var(--color-divider);
}

@media (max-width: 1439px) {
  .app-nav {
    padding: 14px 24px;
  }
}

.app-nav.app-nav--mobile {
  padding: 14px 16px;
  gap: 8px;
}

.app-nav__brand {
  font-family: var(--font-heading);
  font-weight: 800;
  font-size: 18px;
  margin-right: auto;
}

.app-nav__links {
  display: flex;
  align-items: center;
  gap: 20px;
}

.app-nav__link {
  text-decoration: none;
  white-space: nowrap;
  font-size: 14px;
  padding: 4px 0;
  border-bottom: 2px solid transparent;
  color: var(--color-text);
}

.app-nav__link--active {
  color: var(--color-accent);
  border-bottom-color: var(--color-accent);
}

.app-nav__link--disabled {
  opacity: 0.45;
  pointer-events: none;
}

.app-nav__role {
  margin-left: 16px;
  flex: none;
  white-space: nowrap;
}

.app-nav__logout {
  margin-left: 8px;
  flex: none;
  white-space: nowrap;
}
</style>
