<script setup lang="ts">
// Customer order history: every order of the signed-in customer (the gateway scopes GET /orders/
// to the session user), split into active and past, each linking to its detail page. Restaurant
// names are resolved with one GET per distinct restaurant, since orders only carry the id.

import { computed, onMounted, ref } from 'vue'

import StateBlock from '@/components/StateBlock.vue'
import { ApiError } from '@/api/client'
import * as ordersApi from '@/api/orders'
import * as restaurantsApi from '@/api/restaurants'
import { useRestaurantsStore } from '@/stores/restaurants'
import { expectedTime, isActive, STATUS_LABELS, TYPE_LABELS } from '@/utils/orderStatus'
import { formatEpoch } from '@/utils/time'
import type { LoadStatus, Order } from '@/types'

const restaurants = useRestaurantsStore()

const orders = ref<Order[]>([])
const restaurantNames = ref<Record<string, string>>({})
const status = ref<LoadStatus>('loading')
const errorMessage = ref<string | null>(null)

// Active orders soonest first (what the customer is waiting for); past orders most recent first.
const activeOrders = computed(() =>
  orders.value.filter(isActive).sort((a, b) => expectedTime(a) - expectedTime(b)),
)
const pastOrders = computed(() =>
  orders.value.filter((o) => !isActive(o)).sort((a, b) => expectedTime(b) - expectedTime(a)),
)

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
  <div class="my-orders">
    <div class="my-orders__header">
      <div>
        <h6 class="my-orders__kicker">Customer</h6>
        <h2 class="my-orders__title">My orders</h2>
      </div>
      <button v-if="status === 'ready'" class="btn btn-secondary" type="button" @click="load">Refresh</button>
    </div>

    <StateBlock
      :status="status"
      :error-message="errorMessage"
      empty-title="No orders yet"
      empty-body="Orders you place will show up here, together with their status."
      empty-action-label="Reload"
      @retry="load"
      @empty-action="load"
    >
      <section v-for="group in [
          { id: 'active', title: 'In progress', list: activeOrders },
          { id: 'past', title: 'Past orders', list: pastOrders },
        ]"
        :key="group.id"
        class="my-orders__group"
        :aria-labelledby="`my-orders-${group.id}`"
      >
        <template v-if="group.list.length">
          <h3 :id="`my-orders-${group.id}`" class="my-orders__group-title">
            {{ group.title }} <span class="my-orders__count">({{ group.list.length }})</span>
          </h3>
          <ul class="my-orders__list">
            <li v-for="order in group.list" :key="order.orderId">
              <router-link
                class="my-orders__card"
                :to="{ name: 'order-detail', params: { orderId: order.orderId } }"
              >
                <span class="my-orders__name">{{ restaurantName(order) }}</span>
                <span class="tag" :class="{ 'tag-accent': isActive(order) }">{{ STATUS_LABELS[order.status] }}</span>
                <span class="my-orders__meta text-muted">
                  {{ TYPE_LABELS[order.orderType] }} &middot;
                  {{ itemCount(order) }} {{ itemCount(order) === 1 ? 'item' : 'items' }} &middot;
                  {{ formatEpoch(order.estimatedDeliveryTime ?? order.pickupTime) || 'no time set' }}
                  <template v-if="!order.payed"> &middot; not paid</template>
                </span>
              </router-link>
            </li>
          </ul>
        </template>
      </section>
    </StateBlock>
  </div>
</template>

<style scoped lang="scss">
.my-orders {
  max-width: 760px;

  &__header {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: space(3);
    margin-bottom: space(5);
  }

  &__kicker {
    color: $color-accent;
  }

  &__title {
    margin: 0;
  }

  &__group {
    margin-bottom: space(5);
  }

  &__group-title {
    @include eyebrow;
    font-size: 13px;
    margin: 0 0 space(2);
  }

  &__count {
    font-weight: 400;
  }

  &__list {
    list-style: none;
    margin: 0;
    padding: 0;
  }

  // The whole card is the link, so the touch target is the full row.
  &__card {
    display: grid;
    grid-template-columns: 1fr auto;
    gap: space(1) space(3);
    align-items: center;
    padding: space(3) 0;
    border-bottom: 1px solid $color-divider;
    color: inherit;
    text-decoration: none;
    min-height: $touch-target;

    &:hover .my-orders__name,
    &:focus-visible .my-orders__name {
      color: $color-accent;
      text-decoration: underline;
    }
  }

  &__name {
    font-weight: 800;
  }

  &__meta {
    grid-column: 1 / -1;
    font-size: 13px;
  }
}
</style>
