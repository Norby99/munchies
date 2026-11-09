<script setup lang="ts">
// Customer order detail and tracking. Shows the status as a stepper following the order type's
// chain (order-service domain: DeliveryOrder / TakeawayOrder / DineInOrder), the ordered lines
// (names and prices joined from the restaurant's menus, since the order stores only ids and
// quantities) and the per-type details.
//
// Status updates are not pushed yet: until notification-service delivers them over the gateway
// WebSocket, the user refreshes explicitly. No polling loop on purpose (see CLAUDE.md).
//
// Cancelling is allowed only while PENDING and deletes the order server-side (a later GET answers
// 404), so after a cancel the order is forgotten locally and the user goes back to the menu.

import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import ConfirmDialog from '@/components/ConfirmDialog.vue'
import NeedsBackendNote from '@/components/NeedsBackendNote.vue'
import StateBlock from '@/components/StateBlock.vue'
import { ApiError } from '@/api/client'
import * as ordersApi from '@/api/orders'
import { useRestaurantCatalog } from '@/composables/useRestaurantCatalog'
import { useOrdersStore } from '@/stores/orders'
import { formatEUR, multiplyDecimalString, addDecimalStrings } from '@/utils/money'
import { formatEpoch } from '@/utils/time'
import type { LoadStatus, Order, OrderStatus, OrderType } from '@/types'

const props = defineProps<{ orderId: string }>()

const STATUS_CHAINS: Record<OrderType, OrderStatus[]> = {
  DELIVERY: ['PENDING', 'PREPARING', 'READY', 'ON_THE_WAY', 'COMPLETED'],
  TAKEAWAY: ['PENDING', 'PREPARING', 'READY', 'COMPLETED'],
  DINE_IN: ['PENDING', 'PREPARING', 'READY', 'COMPLETED'],
}

const STATUS_LABELS: Record<OrderStatus, string> = {
  PENDING: 'Received',
  PREPARING: 'Preparing',
  READY: 'Ready',
  ON_THE_WAY: 'On the way',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
}

const TYPE_LABELS: Record<OrderType, string> = {
  DELIVERY: 'Delivery',
  TAKEAWAY: 'Takeaway',
  DINE_IN: 'Dine-in',
}

const router = useRouter()
const orders = useOrdersStore()
const catalog = useRestaurantCatalog()

const order = ref<Order | null>(null)
const status = ref<LoadStatus>('loading')
const errorMessage = ref<string | null>(null)
const refreshing = ref(false)
const cancelOpen = ref(false)
const cancelError = ref<string | null>(null)

const chain = computed(() => (order.value ? STATUS_CHAINS[order.value.orderType] : []))
const currentStep = computed(() => (order.value ? chain.value.indexOf(order.value.status) : -1))
const canCancel = computed(() => order.value?.status === 'PENDING')

const lines = computed(() =>
  (order.value?.items ?? []).map((line) => {
    const item = catalog.itemsById.value.get(line.menuItemId)
    return {
      id: line.menuItemId,
      quantity: line.quantity,
      // An item removed from the menu after ordering can no longer be resolved.
      name: item?.name ?? 'Item no longer on the menu',
      total: item ? multiplyDecimalString(item.price, line.quantity) : null,
    }
  }),
)
const totalLabel = computed(() => {
  if (lines.value.some((line) => line.total === null)) return null
  return formatEUR(lines.value.reduce((sum, line) => addDecimalStrings(sum, line.total ?? '0'), '0.00'))
})

async function fetchOrder(): Promise<void> {
  order.value = await ordersApi.getOrder(props.orderId)
}

async function load(): Promise<void> {
  status.value = 'loading'
  errorMessage.value = null
  try {
    await fetchOrder()
    status.value = 'ready'
  } catch (err) {
    if (err instanceof ApiError && err.code === 404) {
      orders.forget(props.orderId)
      status.value = 'empty'
      return
    }
    errorMessage.value = err instanceof ApiError ? err.message : null
    status.value = 'error'
    return
  }
  // Menu names are a nice-to-have: a failure here must not hide the order itself.
  try {
    if (order.value) await catalog.load(order.value.restaurantId)
  } catch {
    // Lines fall back to "Item no longer on the menu" and the total is hidden.
  }
}

async function refresh(): Promise<void> {
  refreshing.value = true
  try {
    await fetchOrder()
  } catch {
    // Keep showing the last known state; the shell handles a 401 globally.
  } finally {
    refreshing.value = false
  }
}

async function confirmCancel(): Promise<void> {
  cancelError.value = null
  const restaurantId = order.value?.restaurantId
  try {
    await ordersApi.cancelOrder(props.orderId)
    orders.forget(props.orderId)
    cancelOpen.value = false
    if (restaurantId) await router.replace({ name: 'restaurant-detail', params: { restaurantId } })
  } catch (err) {
    cancelOpen.value = false
    cancelError.value = err instanceof ApiError ? err.message : 'The order could not be cancelled.'
  }
}

function stepState(index: number): 'done' | 'current' | 'todo' {
  if (index < currentStep.value) return 'done'
  return index === currentStep.value ? 'current' : 'todo'
}

onMounted(load)
watch(() => props.orderId, load)
</script>

<template>
  <div class="order-detail">
    <h6 class="order-detail__kicker">Order</h6>

    <StateBlock
      :status="status"
      :error-message="errorMessage"
      empty-title="This order no longer exists"
      empty-body="It was cancelled, or it belongs to another account."
      empty-action-label="Go to my account"
      @retry="load"
      @empty-action="router.push({ name: 'account' })"
    >
      <template v-if="order">
        <header class="order-detail__header">
          <div>
            <h2 class="order-detail__title">{{ catalog.restaurant.value?.name ?? 'Your order' }}</h2>
            <p class="text-muted order-detail__meta">
              {{ TYPE_LABELS[order.orderType] }} &middot; <span class="order-detail__id">#{{ order.orderId.slice(0, 8) }}</span>
            </p>
          </div>
          <button class="btn btn-secondary" type="button" :disabled="refreshing" @click="refresh">
            {{ refreshing ? 'Refreshing...' : 'Refresh status' }}
          </button>
        </header>

        <section aria-labelledby="order-status-title" class="order-detail__section">
          <h3 id="order-status-title" class="order-detail__section-title">Status</h3>
          <!-- aria-live so that a refreshed status is announced to screen-reader users. -->
          <p class="order-detail__status" aria-live="polite">
            {{ STATUS_LABELS[order.status] }}
          </p>
          <ol class="order-detail__steps">
            <li
              v-for="(step, index) in chain"
              :key="step"
              class="order-detail__step"
              :class="`order-detail__step--${stepState(index)}`"
              :aria-current="stepState(index) === 'current' ? 'step' : undefined"
            >
              <span class="order-detail__dot" aria-hidden="true"></span>
              <span>{{ STATUS_LABELS[step] }}</span>
            </li>
          </ol>
        </section>

        <div class="order-detail__grid">
          <section aria-labelledby="order-items-title" class="order-detail__section">
            <h3 id="order-items-title" class="order-detail__section-title">Items</h3>
            <ul class="order-detail__lines">
              <li v-for="line in lines" :key="line.id" class="order-detail__line">
                <span class="order-detail__num">{{ line.quantity }}&times;</span>
                <span class="order-detail__name">{{ line.name }}</span>
                <span v-if="line.total" class="order-detail__num">{{ formatEUR(line.total) }}</span>
              </li>
            </ul>
            <div v-if="totalLabel" class="order-detail__total">
              <span>Total</span>
              <strong>{{ totalLabel }}</strong>
            </div>
          </section>

          <section aria-labelledby="order-info-title" class="order-detail__section">
            <h3 id="order-info-title" class="order-detail__section-title">Details</h3>
            <dl class="order-detail__info">
              <template v-if="order.orderType === 'DELIVERY'">
                <dt>Delivery time</dt>
                <dd>{{ formatEpoch(order.estimatedDeliveryTime) }}</dd>
                <dt>Address</dt>
                <dd>{{ order.deliveryAddress }}</dd>
                <dt>Doorbell</dt>
                <dd>{{ order.bellName }}</dd>
                <dt>Phone</dt>
                <dd>{{ order.customerPhone }}</dd>
              </template>
              <template v-else-if="order.orderType === 'TAKEAWAY'">
                <dt>Pickup time</dt>
                <dd>{{ formatEpoch(order.pickupTime) }}</dd>
                <dt>Name</dt>
                <dd>{{ order.customerName }}</dd>
              </template>
              <template v-else>
                <dt>Table</dt>
                <dd>{{ order.tableNumber }}</dd>
                <dt>Guests</dt>
                <dd>{{ order.numberOfGuests }}</dd>
              </template>
              <dt>Payment</dt>
              <dd>{{ order.payed ? 'Paid' : 'Not paid yet' }}</dd>
            </dl>
          </section>
        </div>

        <div class="order-detail__actions">
          <button class="btn btn-primary order-detail__action" type="button" disabled>Pay now</button>
          <button
            class="btn btn-secondary order-detail__action"
            type="button"
            :disabled="!canCancel"
            @click="cancelOpen = true"
          >
            Cancel order
          </button>
        </div>
        <NeedsBackendNote>
          Online payment is not available yet: the gateway's pay route does not work with the current
          order-shared contract.
        </NeedsBackendNote>
        <p v-if="!canCancel" class="text-muted order-detail__hint">
          An order can be cancelled only before the restaurant starts preparing it.
        </p>
        <p v-if="cancelError" role="alert" class="order-detail__error">{{ cancelError }}</p>

        <ConfirmDialog
          :open="cancelOpen"
          title="Cancel this order?"
          body="The restaurant has not started preparing it yet. Cancelling deletes the order and cannot be undone."
          confirm-label="Cancel order"
          cancel-label="Keep order"
          @confirm="confirmCancel"
          @cancel="cancelOpen = false"
        />
      </template>
    </StateBlock>
  </div>
</template>

<style scoped lang="scss">
// One color per step state, iterated below to generate the modifiers.
$step-states: (
  done: $color-accent,
  current: $color-accent,
  todo: var(--color-neutral-400),
);

.order-detail {
  max-width: 960px;

  &__kicker {
    color: $color-accent;
  }

  &__header {
    display: flex;
    flex-wrap: wrap;
    gap: space(3);
    align-items: flex-end;
    justify-content: space-between;
    margin-bottom: space(5);
  }

  &__title {
    margin: 0;
  }

  &__meta {
    font-size: 13px;
    margin: space(1) 0 0;
  }

  &__id {
    font-variant-numeric: tabular-nums;
  }

  &__section {
    margin-bottom: space(5);
  }

  &__section-title {
    @include eyebrow;
    font-size: 13px;
    margin: 0 0 space(3);
  }

  &__status {
    font-size: 22px;
    font-weight: 800;
    margin: 0 0 space(3);
  }

  // Vertical stepper on phones, horizontal from tablet up.
  &__steps {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: space(2);

    @include respond-to(tablet) {
      flex-direction: row;
      gap: 0;
    }
  }

  &__step {
    display: flex;
    align-items: center;
    gap: space(2);
    font-size: 14px;

    @include respond-to(tablet) {
      flex: 1;
      flex-direction: column;
      align-items: flex-start;
      border-top: 3px solid var(--color-neutral-300);
      padding-top: space(2);
    }

    @each $state, $color in $step-states {
      &--#{$state} .order-detail__dot {
        border-color: $color;
        @if $state == done {
          background: $color;
        }
      }

      @if $state != todo {
        &--#{$state} {
          @include respond-to(tablet) {
            border-top-color: $color;
          }
        }
      }
    }

    &--current {
      font-weight: 800;
    }

    &--todo {
      color: color-mix(in srgb, #{$color-text} 55%, transparent);
    }
  }

  &__dot {
    width: 12px;
    height: 12px;
    border-radius: 50%;
    border: 2px solid;
    flex: none;
  }

  &__grid {
    display: grid;
    gap: 0 space(6);

    @include respond-to(tablet) {
      grid-template-columns: 3fr 2fr;
    }
  }

  &__lines {
    list-style: none;
    margin: 0;
    padding: 0;
  }

  &__line {
    display: flex;
    gap: space(2);
    padding: space(2) 0;
    border-bottom: 1px solid $color-divider;
    font-size: 14px;
  }

  &__num {
    font-variant-numeric: tabular-nums;
  }

  &__name {
    flex: 1;
  }

  &__total {
    display: flex;
    justify-content: space-between;
    align-items: baseline;
    margin-top: space(3);

    strong {
      font-size: 20px;
      font-variant-numeric: tabular-nums;
    }
  }

  &__info {
    display: grid;
    grid-template-columns: max-content 1fr;
    gap: space(2) space(4);
    margin: 0;
    font-size: 14px;

    dt {
      color: color-mix(in srgb, #{$color-text} 55%, transparent);
    }

    dd {
      margin: 0;
    }
  }

  &__actions {
    display: flex;
    flex-wrap: wrap;
    gap: space(3);
  }

  &__action {
    min-height: $touch-target;
  }

  &__hint {
    font-size: 12px;
    margin: space(2) 0 0;
  }

  &__error {
    font-size: 13px;
    color: var(--color-accent-700);
    margin: space(2) 0 0;
  }
}
</style>
