<script setup lang="ts">
// Customer order history: every order of the signed-in customer (the gateway scopes GET /orders/
// to the session user), split into active and past, each linking to its detail page. Restaurant
// names are resolved with one GET per distinct restaurant, since orders only carry the id.

import { computed, onMounted, ref } from 'vue'
import { BikeIcon, ChevronRightIcon, ReceiptTextIcon, RotateCwIcon, StoreIcon } from '@lucide/vue'

import OrderStatusBadge from '@/components/OrderStatusBadge.vue'
import PageHeader from '@/components/PageHeader.vue'
import StateBlock from '@/components/StateBlock.vue'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { ApiError } from '@/api/client'
import * as ordersApi from '@/api/orders'
import * as restaurantsApi from '@/api/restaurants'
import { useRestaurantsStore } from '@/stores/restaurants'
import { formatCents } from '@/utils/money'
import { expectedTime, isActive, TYPE_LABELS } from '@/utils/orderStatus'
import { formatEpoch } from '@/utils/time'
import type { LoadStatus, Order } from '@/types'

const restaurants = useRestaurantsStore()

const orders = ref<Order[]>([])
const restaurantNames = ref<Record<string, string>>({})
const status = ref<LoadStatus>('loading')
const errorMessage = ref<string | null>(null)

// Active orders soonest first (what the customer is waiting for); past orders most recent first.
const groups = computed(() => [
  {
    id: 'active',
    title: 'In progress',
    list: orders.value.filter(isActive).sort((a, b) => expectedTime(a) - expectedTime(b)),
  },
  {
    id: 'past',
    title: 'Past orders',
    list: orders.value.filter((o) => !isActive(o)).sort((a, b) => expectedTime(b) - expectedTime(a)),
  },
])

async function resolveRestaurantNames(list: Order[]): Promise<void> {
  const missing = [...new Set(list.map((o) => o.restaurantId))].filter((id) => !restaurantNames.value[id])
  const resolved = await Promise.allSettled(missing.map((id) => restaurantsApi.getRestaurant(id)))
  resolved.forEach((result) => {
    if (result.status === 'fulfilled') {
      restaurantNames.value[result.value.id] = result.value.name
      restaurants.cacheOne(result.value)
    }
  })
}

async function load(): Promise<void> {
  status.value = 'loading'
  errorMessage.value = null
  try {
    orders.value = await ordersApi.listOrders()
    status.value = orders.value.length === 0 ? 'empty' : 'ready'
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : null
    status.value = 'error'
    return
  }
  // Names are cosmetic: a deleted restaurant must not hide the order list.
  await resolveRestaurantNames(orders.value)
}

function restaurantName(order: Order): string {
  return restaurantNames.value[order.restaurantId] ?? 'Restaurant'
}

function itemCount(order: Order): number {
  return order.items.reduce((sum, line) => sum + line.quantity, 0)
}

onMounted(load)
</script>

<template>
  <div class="mx-auto max-w-3xl">
    <PageHeader eyebrow="Orders" title="My orders" description="Follow what you ordered and pay pending orders.">
      <template #actions>
        <Button v-if="status === 'ready'" variant="outline" @click="load">
          <RotateCwIcon data-icon="inline-start" /> Refresh
        </Button>
      </template>
    </PageHeader>

    <StateBlock
      :status="status"
      :error-message="errorMessage"
      empty-title="No orders yet"
      empty-body="Open a restaurant's link to browse its menu and place your first order."
      :empty-icon="ReceiptTextIcon"
      @retry="load"
    >
      <template v-for="group in groups" :key="group.id">
        <section v-if="group.list.length" class="mb-10" :aria-labelledby="`my-orders-${group.id}`">
          <h2 :id="`my-orders-${group.id}`" class="mb-3 flex items-center gap-2 text-lg font-bold">
            {{ group.title }}
            <Badge variant="secondary" class="tabular-nums">{{ group.list.length }}</Badge>
          </h2>
          <ul class="flex flex-col gap-3">
            <li v-for="order in group.list" :key="order.orderId">
              <RouterLink
                :to="{ name: 'order-detail', params: { orderId: order.orderId } }"
                class="group flex items-center gap-4 rounded-xl border bg-card p-4 transition-all outline-none hover:border-foreground/20 hover:shadow-sm focus-visible:ring-3 focus-visible:ring-ring/50"
              >
                <span class="grid size-11 shrink-0 place-items-center rounded-xl bg-accent text-accent-foreground">
                  <BikeIcon v-if="order.orderType === 'DELIVERY'" class="size-5" aria-hidden="true" />
                  <StoreIcon v-else class="size-5" aria-hidden="true" />
                </span>
                <span class="flex min-w-0 flex-1 flex-col gap-1">
                  <span class="flex flex-wrap items-center gap-2">
                    <span class="truncate font-semibold">{{ restaurantName(order) }}</span>
                    <OrderStatusBadge :status="order.status" />
                    <Badge v-if="!order.payed && order.status !== 'CANCELLED'" variant="outline" class="text-warning">To pay</Badge>
                  </span>
                  <span class="text-sm text-muted-foreground">
                    {{ TYPE_LABELS[order.orderType] }} &middot; {{ itemCount(order) }}
                    {{ itemCount(order) === 1 ? 'item' : 'items' }} &middot;
                    {{ formatEpoch(order.estimatedDeliveryTime ?? order.pickupTime) || 'no time set' }}
                  </span>
                </span>
                <span v-if="order.totalCents" class="hidden font-semibold tabular-nums sm:inline">
                  {{ formatCents(order.totalCents) }}
                </span>
                <ChevronRightIcon
                  class="size-5 shrink-0 text-muted-foreground transition-transform group-hover:translate-x-0.5"
                  aria-hidden="true"
                />
              </RouterLink>
            </li>
          </ul>
        </section>
      </template>
    </StateBlock>
  </div>
</template>
