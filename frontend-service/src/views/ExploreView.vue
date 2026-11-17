<script setup lang="ts">
// Customer restaurant search: a search box over GET /restaurants/?q=, matched by name or address
// (ignoring case), with the results as cards that open the restaurant page. An empty search lists
// every restaurant. Results are sorted by name and capped server-side (SEARCH_LIMIT).
//
// The text lives in the URL (?q=), so a search survives a reload and the back button returns to
// the same results. Typing is debounced; every request carries a sequence number so a slow, stale
// answer never replaces a newer one.

import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDebounceFn } from '@vueuse/core'
import { ChevronRightIcon, MapPinIcon, PhoneIcon, SearchIcon, SearchXIcon, ShoppingBagIcon, StoreIcon, XIcon } from '@lucide/vue'

import PageHeader from '@/components/PageHeader.vue'
import StateBlock from '@/components/StateBlock.vue'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Spinner } from '@/components/ui/spinner'
import { ApiError } from '@/api/client'
import * as restaurantsApi from '@/api/restaurants'
import { useCartStore } from '@/stores/cart'
import type { LoadStatus, Restaurant } from '@/types'

const DEBOUNCE_MS = 300

const route = useRoute()
const router = useRouter()
const cart = useCartStore()

const initial = typeof route.query.q === 'string' ? route.query.q : ''
const query = ref(initial)
// The text the shown results belong to, used by the empty state and the result count.
const searched = ref(initial)
const results = ref<Restaurant[]>([])
const status = ref<LoadStatus>('loading')
const errorMessage = ref<string | null>(null)
// True while a new search runs over results already on screen: they stay visible, dimmed.
const refreshing = ref(false)

const capped = computed(() => results.value.length >= restaurantsApi.SEARCH_LIMIT)
const summary = computed(() => {
  const n = results.value.length
  const what = `${n}${capped.value ? '+' : ''} ${n === 1 ? 'restaurant' : 'restaurants'}`
  return searched.value.trim() ? `${what} matching "${searched.value.trim()}"` : what
})

// "Nothing matches" after a search; "nobody yet" when even the full list is empty.
const emptyState = computed(() => {
  const text = searched.value.trim()
  return text
    ? {
        title: 'No restaurants found',
        body: `Nothing matches "${text}". Try another name or part of the address.`,
        icon: SearchXIcon,
        action: 'Clear search',
      }
    : {
        title: 'No restaurants yet',
        body: 'No restaurant has joined Munchies yet: come back soon.',
        icon: StoreIcon,
        action: undefined,
      }
})

let requestSeq = 0

async function runSearch(text: string): Promise<void> {
  const request = ++requestSeq
  errorMessage.value = null
  if (status.value === 'ready' || status.value === 'empty') refreshing.value = true
  else status.value = 'loading'
  try {
    const found = await restaurantsApi.searchRestaurants(text.trim())
    if (request !== requestSeq) return
    results.value = found
    searched.value = text
    status.value = found.length === 0 ? 'empty' : 'ready'
  } catch (err) {
    if (request !== requestSeq) return
    errorMessage.value = err instanceof ApiError ? err.message : null
    status.value = 'error'
  } finally {
    if (request === requestSeq) refreshing.value = false
  }
}

const debouncedSearch = useDebounceFn((text: string) => {
  // Keep the URL in step without adding a history entry per keystroke.
  router.replace({ query: text.trim() ? { ...route.query, q: text.trim() } : {} })
  runSearch(text)
}, DEBOUNCE_MS)

watch(query, (text) => debouncedSearch(text))
onMounted(() => runSearch(query.value))

function clearSearch(): void {
  query.value = ''
}

function open(restaurant: Restaurant): void {
  router.push({ name: 'restaurant-detail', params: { restaurantId: restaurant.id } })
}
</script>

<template>
  <div>
    <PageHeader eyebrow="Order" title="Find a restaurant" description="Search by name or by address." />

    <div class="relative mb-6 max-w-xl">
      <SearchIcon class="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted-foreground" aria-hidden="true" />
      <Input
        v-model="query"
        type="search"
        class="h-11 pr-10 pl-9 [&::-webkit-search-cancel-button]:hidden"
        placeholder="Pizzeria, sushi, Via Roma..."
        aria-label="Search restaurants by name or address"
        autocomplete="off"
        maxlength="100"
      />
      <Button
        v-if="query"
        type="button"
        variant="ghost"
        size="icon-sm"
        class="absolute top-1/2 right-1.5 -translate-y-1/2"
        aria-label="Clear search"
        @click="clearSearch"
      >
        <XIcon />
      </Button>
    </div>

    <StateBlock
      :status="status"
      :error-message="errorMessage"
      :empty-title="emptyState.title"
      :empty-body="emptyState.body"
      :empty-icon="emptyState.icon"
      :empty-action-label="emptyState.action"
      @retry="runSearch(query)"
      @empty-action="clearSearch"
    >
      <p class="mb-3 flex items-center gap-2 text-sm text-muted-foreground" role="status">
        <Spinner v-if="refreshing" class="size-3.5" />
        {{ summary }}
      </p>
      <ul :class="['grid gap-4 sm:grid-cols-2 xl:grid-cols-3 transition-opacity', refreshing ? 'opacity-60' : '']">
        <li v-for="restaurant in results" :key="restaurant.id">
          <Card
            class="group h-full cursor-pointer transition-colors hover:border-primary/40 focus-within:border-primary/40"
            @click="open(restaurant)"
          >
            <CardHeader>
              <div class="flex items-center gap-3">
                <span class="grid size-10 shrink-0 place-items-center rounded-xl bg-accent text-accent-foreground">
                  <StoreIcon class="size-5" aria-hidden="true" />
                </span>
                <div class="min-w-0 flex-1">
                  <CardTitle class="truncate text-lg font-bold">
                    <!-- The link is the accessible target; the whole card is clickable for the mouse. -->
                    <RouterLink
                      :to="{ name: 'restaurant-detail', params: { restaurantId: restaurant.id } }"
                      class="outline-none focus-visible:underline"
                      @click.stop
                    >
                      {{ restaurant.name }}
                    </RouterLink>
                  </CardTitle>
                  <Badge v-if="cart.restaurantId === restaurant.id && !cart.isEmpty" variant="secondary" class="mt-1">
                    <ShoppingBagIcon data-icon="inline-start" /> Your bag
                  </Badge>
                </div>
                <ChevronRightIcon
                  class="size-5 shrink-0 text-muted-foreground transition-transform group-hover:translate-x-0.5"
                  aria-hidden="true"
                />
              </div>
            </CardHeader>
            <CardContent>
              <ul class="flex flex-col gap-2 text-sm text-muted-foreground">
                <li class="flex items-center gap-2">
                  <MapPinIcon class="size-4 shrink-0" aria-hidden="true" /><span class="truncate">{{ restaurant.address }}</span>
                </li>
                <li class="flex items-center gap-2"><PhoneIcon class="size-4 shrink-0" aria-hidden="true" />{{ restaurant.phone }}</li>
              </ul>
            </CardContent>
          </Card>
        </li>
      </ul>
      <p v-if="capped" class="mt-4 text-sm text-muted-foreground">
        Showing the first {{ restaurantsApi.SEARCH_LIMIT }} by name: type more to narrow the list.
      </p>
    </StateBlock>
  </div>
</template>
