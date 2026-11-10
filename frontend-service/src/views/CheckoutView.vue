<script setup lang="ts">
// Customer checkout: review the cart, pick how to receive the order, fill the per-type details,
// then POST /orders/place. Only DELIVERY and TAKEAWAY are offered: DINE_IN needs a table, and
// table assignment belongs to table-reservation-service, which is not implemented yet.
//
// On success the cart is emptied and the user lands on the order detail page.

import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import StateBlock from '@/components/StateBlock.vue'
import { ApiError } from '@/api/client'
import * as ordersApi from '@/api/orders'
import { useOnline } from '@/composables/useOnline'
import { useRestaurantCatalog } from '@/composables/useRestaurantCatalog'
import { useCartStore } from '@/stores/cart'
import { useSessionStore } from '@/stores/session'
import { fromDateTimeLocal, toDateTimeLocal } from '@/utils/time'
import type { LoadStatus } from '@/types'

const props = defineProps<{ restaurantId: string }>()

type CheckoutType = 'DELIVERY' | 'TAKEAWAY'

// Lead time suggested by default: an order cannot be ready the instant it is placed.
const DEFAULT_LEAD_MINUTES = 45

const router = useRouter()
const cart = useCartStore()
const session = useSessionStore()
const { isOnline } = useOnline()
const catalog = useRestaurantCatalog()

const status = ref<LoadStatus>('loading')
const submitting = ref(false)
const formError = ref<string | null>(null)

const defaultTime = toDateTimeLocal(new Date(Date.now() + DEFAULT_LEAD_MINUTES * 60_000))
const form = reactive({
  orderType: 'DELIVERY' as CheckoutType,
  time: defaultTime,
  deliveryAddress: '',
  bellName: '',
  customerPhone: '',
  customerName: session.user?.username ?? '',
})
const fieldErrors = reactive<Partial<Record<keyof typeof form, string>>>({})

const lines = computed(() => cart.lines(catalog.mergedMenu.value))
const totalLabel = computed(() => cart.totalLabel(catalog.mergedMenu.value))
const timeLabel = computed(() => (form.orderType === 'DELIVERY' ? 'Delivery time' : 'Pickup time'))

async function load(): Promise<void> {
  status.value = 'loading'
  try {
    // The cart belongs to one restaurant: reaching another restaurant's checkout by URL must
    // not submit items that do not belong to it.
    if (cart.restaurantId !== props.restaurantId || cart.isEmpty) {
      status.value = 'empty'
      return
    }
    await catalog.load(props.restaurantId)
    status.value = lines.value.length === 0 ? 'empty' : 'ready'
  } catch {
    status.value = 'error'
  }
}

onMounted(load)

function validate(): boolean {
  for (const key of Object.keys(fieldErrors) as (keyof typeof form)[]) delete fieldErrors[key]

  const when = fromDateTimeLocal(form.time)
  if (when === null) fieldErrors.time = 'Choose a date and time.'
  else if (when <= Date.now()) fieldErrors.time = 'The time must be in the future.'

  if (form.orderType === 'DELIVERY') {
    if (!form.deliveryAddress.trim()) fieldErrors.deliveryAddress = 'Enter the delivery address.'
    if (!form.bellName.trim()) fieldErrors.bellName = 'Enter the name on the doorbell.'
    if (!/^\+?[0-9 ]{6,20}$/.test(form.customerPhone.trim())) {
      fieldErrors.customerPhone = 'Enter a valid phone number (digits, optional leading +).'
    }
  } else if (!form.customerName.trim()) {
    fieldErrors.customerName = 'Enter the name to give at pickup.'
  }

  return Object.keys(fieldErrors).length === 0
}

async function submit(): Promise<void> {
  formError.value = null
  if (!validate()) {
    formError.value = 'Some fields need attention.'
    return
  }

  const items = lines.value.map((line) => ({ menuItemId: line.itemId, quantity: line.quantity }))
  const time = String(fromDateTimeLocal(form.time))

  submitting.value = true
  try {
    const order = await ordersApi.placeOrder(
      form.orderType === 'DELIVERY'
        ? {
            orderType: 'DELIVERY',
            restaurantId: props.restaurantId,
            items,
            estimatedDeliveryTime: time,
            deliveryAddress: form.deliveryAddress.trim(),
            bellName: form.bellName.trim(),
            customerPhone: form.customerPhone.trim(),
          }
        : {
            orderType: 'TAKEAWAY',
            restaurantId: props.restaurantId,
            items,
            pickupTime: time,
            customerName: form.customerName.trim(),
          },
    )
    cart.clear()
    await router.replace({ name: 'order-detail', params: { orderId: order.orderId } })
  } catch (err) {
    formError.value = err instanceof ApiError ? err.message : 'The order could not be placed.'
  } finally {
    submitting.value = false
  }
}

function backToMenu(): void {
  router.push({ name: 'restaurant-detail', params: { restaurantId: props.restaurantId } })
}
</script>

<template>
  <div class="checkout">
    <h6 class="checkout__kicker">Checkout</h6>
    <h2 class="checkout__title">{{ catalog.restaurant.value?.name ?? 'Your order' }}</h2>

    <StateBlock
      :status="status"
      empty-title="Your cart is empty"
      empty-body="Add something from this restaurant's menu before checking out."
      empty-action-label="Back to the menu"
      @retry="load"
      @empty-action="backToMenu"
    >
      <div class="checkout__grid">
        <form class="checkout__form" novalidate @submit.prevent="submit">
          <fieldset class="checkout__fieldset">
            <legend class="checkout__legend">How do you want it?</legend>
            <div class="seg">
              <label class="seg-opt">
                <input v-model="form.orderType" type="radio" name="order-type" value="DELIVERY" />
                <span>Delivery</span>
              </label>
              <label class="seg-opt">
                <input v-model="form.orderType" type="radio" name="order-type" value="TAKEAWAY" />
                <span>Takeaway</span>
              </label>
            </div>
            <p class="text-muted checkout__hint">
              Dine-in will be available together with table reservations.
            </p>
          </fieldset>

          <div class="field checkout__field">
            <label for="checkout-time">{{ timeLabel }}</label>
            <input
              id="checkout-time"
              v-model="form.time"
              class="input"
              type="datetime-local"
              :min="toDateTimeLocal(new Date())"
              required
              :aria-invalid="!!fieldErrors.time"
              :aria-describedby="fieldErrors.time ? 'checkout-time-error' : undefined"
            />
            <p v-if="fieldErrors.time" id="checkout-time-error" class="checkout__field-error">
              {{ fieldErrors.time }}
            </p>
          </div>

          <template v-if="form.orderType === 'DELIVERY'">
            <div class="field checkout__field">
              <label for="checkout-address">Delivery address</label>
              <input
                id="checkout-address"
                v-model="form.deliveryAddress"
                class="input"
                type="text"
                autocomplete="street-address"
                required
                :aria-invalid="!!fieldErrors.deliveryAddress"
                :aria-describedby="fieldErrors.deliveryAddress ? 'checkout-address-error' : undefined"
              />
              <p v-if="fieldErrors.deliveryAddress" id="checkout-address-error" class="checkout__field-error">
                {{ fieldErrors.deliveryAddress }}
              </p>
            </div>

            <div class="checkout__row">
              <div class="field checkout__field">
                <label for="checkout-bell">Name on the doorbell</label>
                <input
                  id="checkout-bell"
                  v-model="form.bellName"
                  class="input"
                  type="text"
                  autocomplete="family-name"
                  required
                  :aria-invalid="!!fieldErrors.bellName"
                  :aria-describedby="fieldErrors.bellName ? 'checkout-bell-error' : undefined"
                />
                <p v-if="fieldErrors.bellName" id="checkout-bell-error" class="checkout__field-error">
                  {{ fieldErrors.bellName }}
                </p>
              </div>

              <div class="field checkout__field">
                <label for="checkout-phone">Phone</label>
                <input
                  id="checkout-phone"
                  v-model="form.customerPhone"
                  class="input"
                  type="tel"
                  autocomplete="tel"
                  inputmode="tel"
                  required
                  :aria-invalid="!!fieldErrors.customerPhone"
                  :aria-describedby="fieldErrors.customerPhone ? 'checkout-phone-error' : undefined"
                />
                <p v-if="fieldErrors.customerPhone" id="checkout-phone-error" class="checkout__field-error">
                  {{ fieldErrors.customerPhone }}
                </p>
              </div>
            </div>
          </template>

          <div v-else class="field checkout__field">
            <label for="checkout-name">Name for pickup</label>
            <input
              id="checkout-name"
              v-model="form.customerName"
              class="input"
              type="text"
              autocomplete="name"
              required
              :aria-invalid="!!fieldErrors.customerName"
              :aria-describedby="fieldErrors.customerName ? 'checkout-name-error' : undefined"
            />
            <p v-if="fieldErrors.customerName" id="checkout-name-error" class="checkout__field-error">
              {{ fieldErrors.customerName }}
            </p>
          </div>

          <p v-if="formError" role="alert" class="checkout__form-error">{{ formError }}</p>

          <div class="checkout__actions">
            <button class="btn btn-secondary" type="button" @click="backToMenu">Back to the menu</button>
            <button class="btn btn-primary checkout__submit" type="submit" :disabled="submitting || !isOnline">
              {{ submitting ? 'Placing order...' : 'Place order' }}
            </button>
          </div>
          <p v-if="!isOnline" class="text-muted checkout__hint" role="status">
            You are offline: the order can be placed once the connection is back.
          </p>
        </form>

        <aside class="checkout__summary" aria-labelledby="checkout-summary-title">
          <h3 id="checkout-summary-title" class="checkout__summary-title">Summary</h3>
          <ul class="checkout__lines">
            <li v-for="line in lines" :key="line.itemId" class="checkout__line">
              <span class="checkout__qty">{{ line.quantity }}&times;</span>
              <span class="checkout__name">{{ line.item.name }}</span>
              <span class="checkout__price">{{ line.totalLabel }}</span>
            </li>
          </ul>
          <div class="checkout__total">
            <span>Total</span>
            <strong>{{ totalLabel }}</strong>
          </div>
          <p class="text-muted checkout__hint">
            Payment happens after the order is placed, from the order page.
          </p>
        </aside>
      </div>
    </StateBlock>
  </div>
</template>

<style scoped lang="scss">
.checkout {
  max-width: 1080px;

  &__kicker {
    color: $color-accent;
  }

  &__title {
    margin: 0 0 space(5);
  }

  // Mobile first: summary above nothing, stacked below the form; side by side on wide screens.
  &__grid {
    display: grid;
    gap: space(6);

    @include respond-to(wide) {
      grid-template-columns: minmax(0, 1fr) minmax(280px, 340px);
      align-items: start;
    }
  }

  &__fieldset {
    border: 0;
    margin: 0 0 space(5);
    padding: 0;
  }

  &__legend {
    @include eyebrow;
    padding: 0;
    margin-bottom: space(2);
  }

  &__field {
    margin-bottom: space(4);
  }

  &__row {
    display: grid;
    gap: 0 space(4);

    @include respond-to(tablet) {
      grid-template-columns: 1fr 1fr;
    }
  }

  &__hint {
    font-size: 12px;
    margin: space(2) 0 0;
  }

  &__field-error,
  &__form-error {
    font-size: 13px;
    color: var(--color-accent-700);
    margin: space(1) 0 0;
  }

  &__form-error {
    margin: 0 0 space(3);
  }

  &__actions {
    display: flex;
    flex-wrap: wrap;
    gap: space(3);
    justify-content: flex-end;
  }

  &__submit {
    min-height: $touch-target;
  }

  &__summary {
    border-top: 2px solid $color-divider;
    padding-top: space(4);

    @include respond-to(wide) {
      border-top: 0;
      border-left: 2px solid $color-divider;
      padding: 0 0 0 space(5);
      position: sticky;
      top: space(5);
    }
  }

  &__summary-title {
    @include eyebrow;
    font-size: 13px;
    margin: 0 0 space(3);
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

  &__qty,
  &__price {
    font-variant-numeric: tabular-nums;
  }

  &__name {
    flex: 1;
  }

  &__total {
    display: flex;
    justify-content: space-between;
    align-items: baseline;
    margin-top: space(4);

    span {
      @include eyebrow;
    }

    strong {
      font-size: 22px;
      font-variant-numeric: tabular-nums;
    }
  }
}
</style>
