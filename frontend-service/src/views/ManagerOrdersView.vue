<script setup lang="ts">
// Manager order board for one restaurant: incoming orders grouped by status (one column per step
// of the lifecycle), each with a single "move to <next step>" action that calls
// POST /orders/advance. Completed and cancelled orders are kept out of the board and listed on
// demand.
//
// The selected restaurant lives in the URL query (?restaurant=<id>), so the board can be
// bookmarked and the nav can deep-link into it. Like the customer screens, the board refreshes on
// demand: live updates will come from the notification WebSocket, not from polling.

import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import StateBlock from '@/components/StateBlock.vue'
import { ApiError } from '@/api/client'
import * as ordersApi from '@/api/orders'
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
    await ordersApi.advanceOrderStatus(order.orderId)
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

function selectRestaurant(event: Event): void {
  const id = (event.target as HTMLSelectElement).value
  router.push({ query: { ...route.query, restaurant: id } })
}

onMounted(load)
watch(restaurantId, (next, previous) => {
  if (next && next !== previous) load()
})
</script>

<template>
  <div class="manager-orders">
    <div class="manager-orders__header">
      <div>
        <h6 class="manager-orders__kicker">Manager</h6>
        <h2 class="manager-orders__title">Orders</h2>
      </div>
      <div class="manager-orders__controls">
        <div v-if="restaurants.items.length > 1" class="field">
          <label for="manager-orders-restaurant">Restaurant</label>
          <select
            id="manager-orders-restaurant"
            class="input"
            :value="restaurantId ?? ''"
            @change="selectRestaurant"
          >
            <option v-for="r in restaurants.items" :key="r.id" :value="r.id">{{ r.name }}</option>
          </select>
        </div>
        <button
          v-if="status === 'ready'"
          class="btn btn-secondary"
          type="button"
          :disabled="refreshing"
          @click="refresh"
        >
          {{ refreshing ? 'Refreshing...' : 'Refresh' }}
        </button>
      </div>
    </div>

    <p class="visually-hidden" aria-live="polite">{{ announcement }}</p>

    <StateBlock
      :status="status"
      :error-message="errorMessage"
      empty-title="No restaurants yet"
      empty-body="Create a restaurant first: its orders will show up here."
      empty-action-label="Go to my restaurants"
      @retry="load"
      @empty-action="router.push({ name: 'manager-restaurants' })"
    >
      <p class="text-muted manager-orders__summary">
        {{ activeCount }} active {{ activeCount === 1 ? 'order' : 'orders' }}
        <template v-if="catalog.restaurant.value"> at {{ catalog.restaurant.value.name }}</template>
      </p>

      <div class="manager-orders__board">
        <section
          v-for="column in columns"
          :key="column.status"
          class="manager-orders__column"
          :aria-labelledby="`column-${column.status}`"
        >
          <h3 :id="`column-${column.status}`" class="manager-orders__column-title">
            {{ STATUS_LABELS[column.status] }}
            <span class="manager-orders__count">{{ column.orders.length }}</span>
          </h3>

          <p v-if="column.orders.length === 0" class="text-muted manager-orders__none">No orders</p>

          <ul class="manager-orders__cards">
            <li v-for="order in column.orders" :key="order.orderId" class="manager-orders__card">
              <div class="manager-orders__card-head">
                <strong>{{ TYPE_LABELS[order.orderType] }}</strong>
                <span class="manager-orders__id">#{{ shortId(order) }}</span>
              </div>
              <p class="manager-orders__time">
                {{ order.orderType === 'DELIVERY' ? 'Deliver at' : 'Pickup at' }}
                {{ formatEpoch(order.estimatedDeliveryTime ?? order.pickupTime) }}
              </p>
              <ul class="manager-orders__items">
                <li v-for="line in order.items" :key="line.menuItemId">
                  {{ line.quantity }}&times; {{ itemName(line.menuItemId) }}
                </li>
              </ul>
              <p class="text-muted manager-orders__who">
                <template v-if="order.orderType === 'DELIVERY'">
                  {{ order.deliveryAddress }} &middot; bell: {{ order.bellName }} &middot; {{ order.customerPhone }}
                </template>
                <template v-else-if="order.orderType === 'TAKEAWAY'">{{ order.customerName }}</template>
                <template v-else>Table {{ order.tableNumber }} &middot; {{ order.numberOfGuests }} guests</template>
              </p>
              <span class="tag" :class="order.payed ? 'tag-neutral' : 'tag-outline'">
                {{ order.payed ? 'Paid' : 'Not paid' }}
              </span>

              <button
                v-if="nextStatus(order)"
                class="btn btn-primary manager-orders__advance"
                type="button"
                :disabled="advancing === order.orderId"
                @click="advance(order)"
              >
                {{
                  advancing === order.orderId
                    ? 'Updating...'
                    : `Mark as ${STATUS_LABELS[nextStatus(order)!].toLowerCase()}`
                }}
              </button>
              <p v-if="cardErrors[order.orderId]" role="alert" class="manager-orders__error">
                {{ cardErrors[order.orderId] }}
              </p>
            </li>
          </ul>
        </section>
      </div>

      <div class="manager-orders__completed">
        <button
          class="btn btn-ghost"
          type="button"
          :aria-expanded="showCompleted"
          aria-controls="manager-orders-completed"
          @click="showCompleted = !showCompleted"
        >
          {{ showCompleted ? 'Hide' : 'Show' }} closed orders ({{ completed.length }})
        </button>
        <ul v-show="showCompleted" id="manager-orders-completed" class="manager-orders__done">
          <li v-for="order in completed" :key="order.orderId">
            <span class="manager-orders__id">#{{ shortId(order) }}</span>
            {{ STATUS_LABELS[order.status] }} &middot; {{ TYPE_LABELS[order.orderType] }} &middot;
            {{ formatEpoch(order.estimatedDeliveryTime ?? order.pickupTime) }} &middot;
            {{ order.items.reduce((n, l) => n + l.quantity, 0) }} items
          </li>
          <li v-if="completed.length === 0" class="text-muted">No completed or cancelled orders.</li>
        </ul>
      </div>
    </StateBlock>
  </div>
</template>

<style scoped lang="scss">
.manager-orders {
  &__header {
    display: flex;
    flex-wrap: wrap;
    align-items: flex-end;
    justify-content: space-between;
    gap: space(3);
    margin-bottom: space(4);
  }

  &__kicker {
    color: $color-accent;
  }

  &__title {
    margin: 0;
  }

  &__controls {
    display: flex;
    flex-wrap: wrap;
    align-items: flex-end;
    gap: space(3);
  }

  &__summary {
    font-size: 13px;
    margin: 0 0 space(4);
  }

  // Columns stack on phones (in lifecycle order), sit side by side from the wide breakpoint.
  &__board {
    display: grid;
    gap: space(5);

    @include respond-to(tablet) {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    @include respond-to(wide) {
      grid-template-columns: repeat(4, minmax(0, 1fr));
      gap: space(4);
    }
  }

  &__column-title {
    @include eyebrow;
    display: flex;
    justify-content: space-between;
    font-size: 13px;
    margin: 0 0 space(2);
    padding-bottom: space(2);
    border-bottom: 2px solid $color-divider;
  }

  &__count {
    font-variant-numeric: tabular-nums;
  }

  &__none {
    font-size: 13px;
  }

  &__cards {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: space(3);
  }

  &__card {
    background: var(--color-surface);
    padding: space(3);
    font-size: 13px;
  }

  &__card-head {
    display: flex;
    justify-content: space-between;
    align-items: baseline;
    margin-bottom: space(1);
  }

  &__id {
    font-variant-numeric: tabular-nums;
    color: color-mix(in srgb, #{$color-text} 55%, transparent);
  }

  &__time {
    margin: 0 0 space(2);
    font-weight: 600;
  }

  &__items {
    margin: 0 0 space(2);
    padding-left: space(4);
  }

  &__who {
    margin: 0 0 space(2);
    font-size: 12px;
  }

  &__advance {
    width: 100%;
    justify-content: center;
    margin-top: space(3);
    min-height: $touch-target;
  }

  &__error {
    color: var(--color-accent-700);
    margin: space(2) 0 0;
  }

  &__completed {
    margin-top: space(6);
  }

  &__done {
    list-style: none;
    padding: 0;
    margin: space(2) 0 0;
    font-size: 13px;

    li {
      padding: space(2) 0;
      border-bottom: 1px solid $color-divider;
    }
  }
}

// Kept in the DOM for screen readers, removed from the visual layout.
.visually-hidden {
  @include visually-hidden;
}
</style>
