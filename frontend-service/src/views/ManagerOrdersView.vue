<script setup lang="ts">
// Manager order board for one restaurant: the kitchen load of the day (from scheduler-service),
// then incoming orders grouped by status (one column per step of the lifecycle), each with a single
// "move to <next step>" action. Advancing goes through the scheduler (POST
// /schedule/orders/{id}/advance), which forwards it to order-service and keeps its booking in step,
// so its prep-time timer does not advance the same order a second time. Completed and cancelled orders are kept out of the board and listed on
// demand.
//
// The selected restaurant lives in the URL query (?restaurant=<id>), so the board can be
// bookmarked and the nav can deep-link into it. Like the customer screens, the board refreshes on
// demand: live updates will come from the notification WebSocket, not from polling.

import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import {
  ArrowRightIcon,
  BikeIcon,
  ChevronDownIcon,
  CircleAlertIcon,
  ClipboardListIcon,
  ClockIcon,
  RotateCwIcon,
  StoreIcon,
  UserRoundIcon,
} from '@lucide/vue'

import KitchenLoadPanel from '@/components/KitchenLoadPanel.vue'
import OrderStatusBadge from '@/components/OrderStatusBadge.vue'
import PageHeader from '@/components/PageHeader.vue'
import StateBlock from '@/components/StateBlock.vue'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardFooter, CardHeader } from '@/components/ui/card'
import { Field, FieldLabel } from '@/components/ui/field'
import { NativeSelect, NativeSelectOption } from '@/components/ui/native-select'
import { Spinner } from '@/components/ui/spinner'
import { ApiError } from '@/api/client'
import * as ordersApi from '@/api/orders'
import * as scheduleApi from '@/api/schedule'
import { useRestaurantCatalog } from '@/composables/useRestaurantCatalog'
import { useRestaurantsStore } from '@/stores/restaurants'
import { expectedTime, isActive, nextStatus, STATUS_LABELS, TYPE_LABELS } from '@/utils/orderStatus'
import { formatEpoch } from '@/utils/time'
import type { LoadStatus, Order, OrderStatus } from '@/types'

// Board columns, in lifecycle order. ON_THE_WAY only ever holds delivery orders.
const COLUMNS: OrderStatus[] = ['PENDING', 'PREPARING', 'READY', 'ON_THE_WAY']

const route = useRoute()
const router = useRouter()
const restaurants = useRestaurantsStore()
const catalog = useRestaurantCatalog()

const orders = ref<Order[]>([])
const status = ref<LoadStatus>('loading')
const errorMessage = ref<string | null>(null)
const refreshing = ref(false)
const showCompleted = ref(false)
const advancing = ref<string | null>(null)
// Bumped on every refresh so the kitchen load panel re-reads too.
const loadRefreshKey = ref(0)
const cardErrors = ref<Record<string, string>>({})
// Announced through an aria-live region after every successful advance.
const announcement = ref('')

const restaurantId = computed(() => {
  const fromQuery = route.query.restaurant
  return typeof fromQuery === 'string' && fromQuery ? fromQuery : null
})

const columns = computed(() =>
  COLUMNS.map((columnStatus) => ({
    status: columnStatus,
    orders: orders.value
      .filter((o) => o.status === columnStatus)
      .sort((a, b) => expectedTime(a) - expectedTime(b)),
  })),
)
const completed = computed(() =>
  orders.value.filter((o) => !isActive(o)).sort((a, b) => expectedTime(b) - expectedTime(a)),
)
const activeCount = computed(() => orders.value.filter(isActive).length)

function itemName(menuItemId: string): string {
  return catalog.itemsById.value.get(menuItemId)?.name ?? 'Item no longer on the menu'
}

function shortId(order: Order): string {
  return order.orderId.slice(0, 8)
}

async function fetchOrders(id: string): Promise<void> {
  orders.value = await ordersApi.listOrders({ restaurantId: id })
}

async function load(): Promise<void> {
  status.value = 'loading'
  errorMessage.value = null
  cardErrors.value = {}
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
    await fetchOrders(restaurantId.value)
    status.value = 'ready'
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : null
    status.value = 'error'
    return
  }
  try {
    await catalog.load(restaurantId.value)
  } catch {
    // Item names fall back to a placeholder; the board stays usable.
  }
}

async function refresh(): Promise<void> {
  if (!restaurantId.value) return
  refreshing.value = true
  loadRefreshKey.value++
  try {
    await fetchOrders(restaurantId.value)
  } catch {
    // Keep the last known board; a 401 is handled globally by the shell.
  } finally {
    refreshing.value = false
  }
}

async function advance(order: Order): Promise<void> {
  const target = nextStatus(order)
  if (!target) return
  advancing.value = order.orderId
  delete cardErrors.value[order.orderId]
  try {
    await scheduleApi.advanceScheduledOrder(order.orderId)
    loadRefreshKey.value++
    // Re-read the order instead of assuming the transition: the server is the source of truth.
    const updated = await ordersApi.getOrder(order.orderId)
    orders.value = orders.value.map((o) => (o.orderId === updated.orderId ? updated : o))
    announcement.value = `Order ${shortId(order)} moved to ${STATUS_LABELS[updated.status]}.`
  } catch (err) {
    cardErrors.value[order.orderId] =
      err instanceof ApiError ? err.message : 'The status could not be changed.'
  } finally {
    advancing.value = null
  }
}

function selectRestaurant(id: unknown): void {
  if (typeof id === 'string' && id) router.push({ query: { ...route.query, restaurant: id } })
}

function itemCount(order: Order): number {
  return order.items.reduce((n, line) => n + line.quantity, 0)
}

onMounted(load)
watch(restaurantId, (next, previous) => {
  if (next && next !== previous) load()
})
</script>

<template>
  <div>
    <PageHeader eyebrow="Manager" title="Orders">
      <template #description>
        <template v-if="status === 'ready'">
          {{ activeCount }} active {{ activeCount === 1 ? 'order' : 'orders' }}<template v-if="catalog.restaurant.value">
            at {{ catalog.restaurant.value.name }}</template
          >
        </template>
        <template v-else>Incoming orders, from received to completed.</template>
      </template>
      <template #actions>
        <Field v-if="restaurants.items.length > 1" orientation="horizontal" class="w-auto gap-2">
          <FieldLabel for="manager-orders-restaurant" class="sr-only">Restaurant</FieldLabel>
          <NativeSelect
            id="manager-orders-restaurant"
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

    <p class="sr-only" aria-live="polite">{{ announcement }}</p>

    <StateBlock
      :status="status"
      :error-message="errorMessage"
      empty-title="No restaurants yet"
      empty-body="Create a restaurant first: its orders will show up here."
      :empty-icon="StoreIcon"
      empty-action-label="Go to my restaurants"
      @retry="load"
      @empty-action="router.push({ name: 'manager-restaurants' })"
    >
      <KitchenLoadPanel v-if="restaurantId" :restaurant-id="restaurantId" :refresh-key="loadRefreshKey" />

      <!-- Columns stack on phones, two per row on tablets, the full board on wide screens. -->
      <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <section
          v-for="column in columns"
          :key="column.status"
          class="flex flex-col gap-3 rounded-2xl bg-muted/60 p-3"
          :aria-labelledby="`column-${column.status}`"
        >
          <h2 :id="`column-${column.status}`" class="flex items-center justify-between px-1 pt-1">
            <OrderStatusBadge :status="column.status" />
            <span class="text-sm font-semibold text-muted-foreground tabular-nums">
              {{ column.orders.length }}<span class="sr-only"> orders</span>
            </span>
          </h2>

          <p v-if="column.orders.length === 0" class="rounded-xl border border-dashed px-3 py-6 text-center text-sm text-muted-foreground">
            No orders
          </p>

          <ul class="flex flex-col gap-3">
            <li v-for="order in column.orders" :key="order.orderId">
              <Card size="sm" class="gap-3">
                <CardHeader class="flex items-center justify-between gap-2">
                  <span class="flex items-center gap-1.5 text-sm font-semibold">
                    <BikeIcon v-if="order.orderType === 'DELIVERY'" class="size-4" aria-hidden="true" />
                    <StoreIcon v-else class="size-4" aria-hidden="true" />
                    {{ TYPE_LABELS[order.orderType] }}
                  </span>
                  <span class="text-xs text-muted-foreground tabular-nums">#{{ shortId(order) }}</span>
                </CardHeader>
                <CardContent class="flex flex-col gap-2 text-sm">
                  <p class="flex items-center gap-1.5 font-semibold">
                    <ClockIcon class="size-4 text-muted-foreground" aria-hidden="true" />
                    {{ formatEpoch(order.estimatedDeliveryTime ?? order.pickupTime) }}
                  </p>
                  <ul class="flex flex-col gap-0.5">
                    <li v-for="line in order.items" :key="line.menuItemId" class="flex gap-2">
                      <span class="font-semibold tabular-nums">{{ line.quantity }}&times;</span>
                      <span class="min-w-0">{{ itemName(line.menuItemId) }}</span>
                    </li>
                  </ul>
                  <p class="flex items-start gap-1.5 text-xs text-muted-foreground">
                    <UserRoundIcon class="mt-px size-3.5 shrink-0" aria-hidden="true" />
                    <span>
                      <template v-if="order.orderType === 'DELIVERY'">
                        {{ order.deliveryAddress }} &middot; bell: {{ order.bellName }} &middot; {{ order.customerPhone }}
                      </template>
                      <template v-else-if="order.orderType === 'TAKEAWAY'">{{ order.customerName }}</template>
                      <template v-else>Table {{ order.tableNumber }} &middot; {{ order.numberOfGuests }} guests</template>
                    </span>
                  </p>
                  <div>
                    <Badge v-if="order.payed" variant="outline" class="text-success">Paid</Badge>
                    <Badge v-else variant="outline" class="text-warning">Not paid</Badge>
                  </div>
                  <p v-if="cardErrors[order.orderId]" role="alert" class="flex items-center gap-1.5 text-destructive">
                    <CircleAlertIcon class="size-4 shrink-0" aria-hidden="true" />
                    {{ cardErrors[order.orderId] }}
                  </p>
                </CardContent>
                <CardFooter v-if="nextStatus(order)">
                  <Button class="w-full" :disabled="advancing === order.orderId" @click="advance(order)">
                    <Spinner v-if="advancing === order.orderId" data-icon="inline-start" />
                    Mark as {{ STATUS_LABELS[nextStatus(order)!].toLowerCase() }}
                    <ArrowRightIcon v-if="advancing !== order.orderId" data-icon="inline-end" />
                  </Button>
                </CardFooter>
              </Card>
            </li>
          </ul>
        </section>
      </div>

      <section class="mt-8">
        <Button
          variant="ghost"
          class="-ml-2.5"
          :aria-expanded="showCompleted"
          aria-controls="manager-orders-completed"
          @click="showCompleted = !showCompleted"
        >
          <ChevronDownIcon :class="['transition-transform', showCompleted ? 'rotate-180' : '']" data-icon="inline-start" />
          {{ showCompleted ? 'Hide' : 'Show' }} closed orders ({{ completed.length }})
        </Button>
        <ul v-show="showCompleted" id="manager-orders-completed" class="mt-3 flex flex-col divide-y rounded-xl border bg-card">
          <li v-for="order in completed" :key="order.orderId" class="flex flex-wrap items-center gap-x-4 gap-y-1 px-4 py-3 text-sm">
            <OrderStatusBadge :status="order.status" />
            <span class="text-muted-foreground tabular-nums">#{{ shortId(order) }}</span>
            <span>{{ TYPE_LABELS[order.orderType] }}</span>
            <span class="text-muted-foreground">{{ formatEpoch(order.estimatedDeliveryTime ?? order.pickupTime) }}</span>
            <span class="ml-auto text-muted-foreground">{{ itemCount(order) }} items</span>
          </li>
          <li v-if="completed.length === 0" class="flex items-center gap-2 px-4 py-6 text-sm text-muted-foreground">
            <ClipboardListIcon class="size-4" aria-hidden="true" /> No completed or cancelled orders.
          </li>
        </ul>
      </section>
    </StateBlock>
  </div>
</template>
