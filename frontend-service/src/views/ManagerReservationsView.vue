<script setup lang="ts">
// Manager: the table reservations of one restaurant, day by day, so the staff knows who arrives
// when and at which table. Reservations are read-only here: only the customer who made one can
// change or cancel it (table-reservation-service rule).
//
// The restaurant lives in the URL query (?restaurant=<id>), as on the order board, so the page
// survives a reload and the nav keeps the same restaurant across manager pages. The whole list is
// read once and filtered by day in the browser.

import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArmchairIcon, CalendarDaysIcon, RotateCwIcon, StoreIcon, UsersIcon } from '@lucide/vue'

import PageHeader from '@/components/PageHeader.vue'
import StateBlock from '@/components/StateBlock.vue'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Field, FieldLabel } from '@/components/ui/field'
import { NativeSelect, NativeSelectOption } from '@/components/ui/native-select'
import { Spinner } from '@/components/ui/spinner'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'
import { ApiError } from '@/api/client'
import * as reservationsApi from '@/api/reservations'
import { useRestaurantsStore } from '@/stores/restaurants'
import { bookableDays, formatTimeRange, isOngoing } from '@/utils/reservations'
import { toDateInput } from '@/utils/time'
import type { LoadStatus, Reservation } from '@/types'

const TOGGLE_ON = 'data-[state=on]:border-primary data-[state=on]:bg-primary data-[state=on]:text-primary-foreground'

const route = useRoute()
const router = useRouter()
const restaurants = useRestaurantsStore()

const status = ref<LoadStatus>('loading')
const errorMessage = ref<string | null>(null)
const refreshing = ref(false)
const reservations = ref<Reservation[]>([])
const days = bookableDays()
const day = ref(days[0].value)

const restaurantId = computed(() => {
  const fromQuery = route.query.restaurant
  return typeof fromQuery === 'string' && fromQuery ? fromQuery : null
})
const restaurantName = computed(() => restaurants.items.find((r) => r.id === restaurantId.value)?.name ?? '')

// The chosen day's reservations by arrival, accepted first; canceled ones stay listed, dimmed.
const ofDay = computed(() =>
  reservations.value
    .filter((r) => toDateInput(new Date(r.start)) === day.value)
    .sort((a, b) => a.start.localeCompare(b.start) || a.tableNumber - b.tableNumber),
)
const accepted = computed(() => ofDay.value.filter((r) => r.status === 'ACCEPTED'))
const guests = computed(() => accepted.value.reduce((sum, r) => sum + r.numberOfGuests, 0))
const countByDay = computed(() => {
  const counts: Record<string, number> = {}
  for (const r of reservations.value) {
    if (r.status !== 'ACCEPTED') continue
    const key = toDateInput(new Date(r.start))
    counts[key] = (counts[key] ?? 0) + 1
  }
  return counts
})

async function fetchReservations(id: string): Promise<void> {
  reservations.value = await reservationsApi.listReservations(id)
}

async function load(): Promise<void> {
  status.value = 'loading'
  errorMessage.value = null
  try {
    if (restaurants.items.length === 0) await restaurants.fetchMine()
    if (restaurants.items.length === 0) {
      status.value = 'empty'
      return
    }
    // No (or a foreign) restaurant in the URL: fall back to the active or first own restaurant.
    const own = restaurants.items.some((r) => r.id === restaurantId.value)
    if (!restaurantId.value || !own) {
      const fallback = restaurants.activeId ?? restaurants.items[0].id
      await router.replace({ query: { ...route.query, restaurant: fallback } })
      return // the restaurantId watcher reloads with the new query
    }
    restaurants.setActive(restaurantId.value)
    await fetchReservations(restaurantId.value)
    status.value = 'ready'
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : null
    status.value = 'error'
  }
}

async function refresh(): Promise<void> {
  if (!restaurantId.value) return
  refreshing.value = true
  try {
    await fetchReservations(restaurantId.value)
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : null
    status.value = 'error'
  } finally {
    refreshing.value = false
  }
}

onMounted(load)
watch(restaurantId, load)

function selectRestaurant(id: unknown): void {
  if (typeof id === 'string' && id) router.replace({ query: { ...route.query, restaurant: id } })
}
</script>

<template>
  <div>
    <PageHeader eyebrow="Manager" title="Reservations">
      <template #description>
        <template v-if="status === 'ready' && restaurantName">Booked tables at {{ restaurantName }}.</template>
        <template v-else>Who arrives when, and at which table.</template>
      </template>
      <template #actions>
        <Field v-if="restaurants.items.length > 1" orientation="horizontal" class="w-auto gap-2">
          <FieldLabel for="manager-reservations-restaurant" class="sr-only">Restaurant</FieldLabel>
          <NativeSelect
            id="manager-reservations-restaurant"
            :model-value="restaurantId ?? ''"
            class="min-w-48"
            @update:model-value="selectRestaurant"
          >
            <NativeSelectOption v-for="r in restaurants.items" :key="r.id" :value="r.id">{{ r.name }}</NativeSelectOption>
          </NativeSelect>
        </Field>
        <Button v-if="status === 'ready'" variant="outline" :disabled="refreshing" @click="refresh">
          <Spinner v-if="refreshing" data-icon="inline-start" />
          <RotateCwIcon v-else data-icon="inline-start" />
          Refresh
        </Button>
      </template>
    </PageHeader>

    <StateBlock
      :status="status"
      :error-message="errorMessage"
      empty-title="No restaurants yet"
      empty-body="Create a restaurant and set up its tables: its reservations will show up here."
      :empty-icon="StoreIcon"
      empty-action-label="Go to my restaurants"
      @retry="load"
      @empty-action="router.push({ name: 'manager-restaurants' })"
    >
      <ToggleGroup v-model="day" type="single" variant="outline" :spacing="2" class="mb-6 flex-wrap" aria-label="Day">
        <ToggleGroupItem v-for="d in days" :key="d.value" :value="d.value" :class="['h-9 gap-1.5 px-3', TOGGLE_ON]">
          {{ d.label }}
          <Badge v-if="countByDay[d.value]" variant="secondary" class="h-4.5 min-w-4.5 px-1 tabular-nums">
            {{ countByDay[d.value] }}
            <span class="sr-only">{{ countByDay[d.value] === 1 ? 'reservation' : 'reservations' }}</span>
          </Badge>
        </ToggleGroupItem>
      </ToggleGroup>

      <p class="mb-3 text-sm text-muted-foreground" role="status">
        {{ accepted.length }} {{ accepted.length === 1 ? 'table' : 'tables' }} booked, {{ guests }}
        {{ guests === 1 ? 'guest' : 'guests' }}
      </p>

      <div
        v-if="ofDay.length === 0"
        class="flex flex-col items-center gap-2 rounded-2xl border border-dashed p-10 text-center text-sm text-muted-foreground"
      >
        <CalendarDaysIcon class="size-6" aria-hidden="true" />
        No reservations on this day.
      </div>
      <ul v-else class="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
        <li v-for="reservation in ofDay" :key="reservation.id">
          <Card :class="['h-full', reservation.status === 'CANCELED' ? 'opacity-60' : '']">
            <CardHeader>
              <div class="flex flex-wrap items-center gap-2">
                <CardTitle class="text-lg font-bold tabular-nums">{{ formatTimeRange(reservation) }}</CardTitle>
                <Badge v-if="reservation.status === 'CANCELED'" variant="outline" class="text-muted-foreground">Canceled</Badge>
                <Badge v-else-if="isOngoing(reservation)" variant="outline" class="bg-success/12 text-success">Seated now</Badge>
              </div>
            </CardHeader>
            <CardContent class="flex flex-wrap gap-x-5 gap-y-1.5 text-sm">
              <span class="flex items-center gap-1.5">
                <ArmchairIcon class="size-4 text-muted-foreground" aria-hidden="true" /> Table {{ reservation.tableNumber }}
              </span>
              <span class="flex items-center gap-1.5">
                <UsersIcon class="size-4 text-muted-foreground" aria-hidden="true" />
                {{ reservation.numberOfGuests }} {{ reservation.numberOfGuests === 1 ? 'guest' : 'guests' }}
              </span>
              <span class="w-full text-xs text-muted-foreground">Booking #{{ reservation.id.slice(0, 8) }}</span>
            </CardContent>
          </Card>
        </li>
      </ul>
    </StateBlock>
  </div>
</template>
