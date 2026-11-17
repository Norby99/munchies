<script setup lang="ts">
// Shared frame for the auth screens (login, register, verify), rendered without AppShell: a brand
// panel on large screens, and the form card centered on the right (alone on phones).

import { BikeIcon, ChefHatIcon, ClockIcon } from '@lucide/vue'

import BrandMark from '@/components/BrandMark.vue'
import { Card, CardContent, CardDescription, CardHeader } from '@/components/ui/card'

defineProps<{ heading: string; description?: string }>()

const highlights = [
  { icon: ChefHatIcon, text: 'Menus straight from the kitchen, always up to date.' },
  { icon: ClockIcon, text: 'Choose when you want it: delivery or pickup.' },
  { icon: BikeIcon, text: 'Follow your order from the oven to your door.' },
]
</script>

<template>
  <div class="grid min-h-svh lg:grid-cols-[minmax(0,5fr)_minmax(0,6fr)]">
    <aside
      class="relative hidden overflow-hidden bg-primary p-10 text-primary-foreground lg:flex lg:flex-col lg:justify-between"
    >
      <!-- Decorative rings: pure ornament, hidden from assistive technology. -->
      <div aria-hidden="true" class="pointer-events-none absolute -right-24 -bottom-24 size-96 rounded-full border-[48px] border-primary-foreground/10"></div>
      <div aria-hidden="true" class="pointer-events-none absolute -top-16 right-24 size-40 rounded-full bg-primary-foreground/10"></div>

      <BrandMark size="lg" inverted />

      <div class="relative flex max-w-md flex-col gap-8">
        <p class="font-heading text-4xl leading-tight font-extrabold tracking-tight xl:text-5xl">
          Good food, right on time.
        </p>
        <ul class="flex flex-col gap-4">
          <li v-for="item in highlights" :key="item.text" class="flex items-center gap-3 text-base">
            <span class="grid size-9 shrink-0 place-items-center rounded-full bg-primary-foreground/15">
              <component :is="item.icon" class="size-4" aria-hidden="true" />
            </span>
            {{ item.text }}
          </li>
        </ul>
      </div>

      <p class="relative text-sm text-primary-foreground/80">For hungry customers and busy kitchens.</p>
    </aside>

    <main class="flex flex-col items-center justify-center gap-8 px-4 py-10 sm:px-6">
      <BrandMark size="lg" class="lg:hidden" />
      <Card class="w-full max-w-md shadow-sm">
        <CardHeader>
          <h1 class="text-2xl font-extrabold">{{ heading }}</h1>
          <CardDescription v-if="description">{{ description }}</CardDescription>
        </CardHeader>
        <CardContent>
          <slot />
        </CardContent>
      </Card>
      <div v-if="$slots.footer" class="text-sm text-muted-foreground">
        <slot name="footer" />
      </div>
    </main>
  </div>
</template>
