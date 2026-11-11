<script setup lang="ts">
// Customer checkout: pick how to receive the order, fill the per-type details, review the bag, then
// POST /orders/place. Only DELIVERY and TAKEAWAY are offered: DINE_IN needs a table, and table
// assignment belongs to table-reservation-service, which is not implemented yet.
//
// On success the cart is emptied and the user lands on the order page, where they can pay.

import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeftIcon, BikeIcon, CircleAlertIcon, ShoppingBagIcon, StoreIcon } from '@lucide/vue'

import ChoiceCard from '@/components/ChoiceCard.vue'
import PageHeader from '@/components/PageHeader.vue'
import StateBlock from '@/components/StateBlock.vue'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { Field, FieldError, FieldGroup, FieldLabel, FieldLegend, FieldSet } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { RadioGroup } from '@/components/ui/radio-group'
import { Separator } from '@/components/ui/separator'
import { Spinner } from '@/components/ui/spinner'
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
    formError.value = 'Some fields need your attention.'
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
  <div class="mx-auto max-w-5xl">
    <Button variant="ghost" class="mb-4 -ml-2.5" @click="backToMenu">
      <ArrowLeftIcon data-icon="inline-start" /> Back to the menu
    </Button>
    <PageHeader eyebrow="Checkout" :title="catalog.restaurant.value?.name ?? 'Your order'" />

    <StateBlock
      :status="status"
      empty-title="Your bag is empty"
      empty-body="Add something from this restaurant's menu before checking out."
      :empty-icon="ShoppingBagIcon"
      empty-action-label="Back to the menu"
      @retry="load"
      @empty-action="backToMenu"
    >
      <form class="grid items-start gap-6 lg:grid-cols-[minmax(0,1fr)_360px]" novalidate @submit.prevent="submit">
        <Card>
          <CardHeader>
            <CardTitle class="text-lg">How do you want it?</CardTitle>
            <CardDescription>Dine-in will be available together with table reservations.</CardDescription>
          </CardHeader>
          <CardContent>
            <FieldGroup>
              <FieldSet>
                <FieldLegend class="sr-only">Order type</FieldLegend>
                <RadioGroup v-model="form.orderType" class="grid gap-3 sm:grid-cols-2">
                  <ChoiceCard id="checkout-delivery" value="DELIVERY" title="Delivery" description="We bring it to your door" :icon="BikeIcon" />
                  <ChoiceCard id="checkout-takeaway" value="TAKEAWAY" title="Takeaway" description="Pick it up yourself" :icon="StoreIcon" />
                </RadioGroup>
              </FieldSet>

              <Field :data-invalid="!!fieldErrors.time || undefined">
                <FieldLabel for="checkout-time">{{ timeLabel }}</FieldLabel>
                <Input
                  id="checkout-time"
                  v-model="form.time"
                  class="h-10 sm:max-w-64"
                  type="datetime-local"
                  :min="toDateTimeLocal(new Date())"
                  required
                  :aria-invalid="!!fieldErrors.time || undefined"
                />
                <FieldError v-if="fieldErrors.time">{{ fieldErrors.time }}</FieldError>
              </Field>

              <template v-if="form.orderType === 'DELIVERY'">
                <Field :data-invalid="!!fieldErrors.deliveryAddress || undefined">
                  <FieldLabel for="checkout-address">Delivery address</FieldLabel>
                  <Input
                    id="checkout-address"
                    v-model="form.deliveryAddress"
                    class="h-10"
                    type="text"
                    autocomplete="street-address"
                    placeholder="Via Roma 1, Cesena"
                    required
                    :aria-invalid="!!fieldErrors.deliveryAddress || undefined"
                  />
                  <FieldError v-if="fieldErrors.deliveryAddress">{{ fieldErrors.deliveryAddress }}</FieldError>
                </Field>
                <div class="grid gap-6 sm:grid-cols-2">
                  <Field :data-invalid="!!fieldErrors.bellName || undefined">
                    <FieldLabel for="checkout-bell">Name on the doorbell</FieldLabel>
                    <Input
                      id="checkout-bell"
                      v-model="form.bellName"
                      class="h-10"
                      type="text"
                      autocomplete="family-name"
                      required
                      :aria-invalid="!!fieldErrors.bellName || undefined"
                    />
                    <FieldError v-if="fieldErrors.bellName">{{ fieldErrors.bellName }}</FieldError>
                  </Field>
                  <Field :data-invalid="!!fieldErrors.customerPhone || undefined">
                    <FieldLabel for="checkout-phone">Phone</FieldLabel>
                    <Input
                      id="checkout-phone"
                      v-model="form.customerPhone"
                      class="h-10"
                      type="tel"
                      autocomplete="tel"
                      inputmode="tel"
                      placeholder="+39 333 1234567"
                      required
                      :aria-invalid="!!fieldErrors.customerPhone || undefined"
                    />
                    <FieldError v-if="fieldErrors.customerPhone">{{ fieldErrors.customerPhone }}</FieldError>
                  </Field>
                </div>
              </template>

              <Field v-else :data-invalid="!!fieldErrors.customerName || undefined">
                <FieldLabel for="checkout-name">Name for pickup</FieldLabel>
                <Input
                  id="checkout-name"
                  v-model="form.customerName"
                  class="h-10"
                  type="text"
                  autocomplete="name"
                  required
                  :aria-invalid="!!fieldErrors.customerName || undefined"
                />
                <FieldError v-if="fieldErrors.customerName">{{ fieldErrors.customerName }}</FieldError>
              </Field>
            </FieldGroup>
          </CardContent>
        </Card>

        <Card class="lg:sticky lg:top-24">
          <CardHeader>
            <CardTitle class="text-lg">Order summary</CardTitle>
          </CardHeader>
          <CardContent class="flex flex-col gap-4">
            <ul class="flex flex-col gap-3">
              <li v-for="line in lines" :key="line.itemId" class="flex gap-3 text-sm">
                <span class="font-semibold tabular-nums">{{ line.quantity }}&times;</span>
                <span class="min-w-0 flex-1">{{ line.item.name }}</span>
                <span class="tabular-nums">{{ line.totalLabel }}</span>
              </li>
            </ul>
            <Separator />
            <div class="flex items-baseline justify-between">
              <span class="font-semibold">Total</span>
              <strong class="text-2xl tabular-nums">{{ totalLabel }}</strong>
            </div>
            <Alert v-if="formError" variant="destructive" role="alert">
              <CircleAlertIcon />
              <AlertDescription>{{ formError }}</AlertDescription>
            </Alert>
          </CardContent>
          <CardFooter class="flex-col items-stretch gap-2">
            <Button type="submit" size="lg" class="h-11 w-full" :disabled="submitting || !isOnline">
              <Spinner v-if="submitting" data-icon="inline-start" />
              Place order
            </Button>
            <p class="text-center text-xs text-muted-foreground" role="status">
              {{ isOnline ? 'You will pay on the next page.' : 'You are offline: you can order once you are back online.' }}
            </p>
          </CardFooter>
        </Card>
      </form>
    </StateBlock>
  </div>
</template>
