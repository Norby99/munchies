<script setup lang="ts">
// Customer order detail and tracking: status stepper, ordered lines, per-type details, payment and
// cancellation.
//
// Status updates are not pushed yet: until notification-service delivers them over the gateway
// WebSocket, the user refreshes explicitly. No polling loop on purpose (see CLAUDE.md).
//
// Cancelling is allowed only while PENDING and unpaid (there are no refunds yet). The order is not
// deleted (order-service is event-sourced): it stays visible with status CANCELLED.
//
// Prices and total are the ones stored in the order by order-service (frozen when the items were
// ordered), so they stay right even if the menu changes later. Menu data is only used for item
// names. Paying goes through POST /payments/ (see api/payments.ts), which charges that total.

import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowLeftIcon,
  CircleAlertIcon,
  CircleCheckIcon,
  CreditCardIcon,
  ReceiptTextIcon,
  RotateCwIcon,
  SmartphoneIcon,
  XCircleIcon,
} from '@lucide/vue'

import ChoiceCard from '@/components/ChoiceCard.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import OrderProgress from '@/components/OrderProgress.vue'
import StateBlock from '@/components/StateBlock.vue'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { FieldLegend, FieldSet } from '@/components/ui/field'
import { RadioGroup } from '@/components/ui/radio-group'
import { Separator } from '@/components/ui/separator'
import { Spinner } from '@/components/ui/spinner'
import { ApiError } from '@/api/client'
import * as ordersApi from '@/api/orders'
import * as paymentsApi from '@/api/payments'
import type { OnlinePaymentMethod } from '@/api/payments'
import { useRestaurantCatalog } from '@/composables/useRestaurantCatalog'
import { formatCents } from '@/utils/money'
import { STATUS_LABELS, TYPE_LABELS } from '@/utils/orderStatus'
import { formatEpoch } from '@/utils/time'
import type { LoadStatus, Order } from '@/types'

const props = defineProps<{ orderId: string }>()

const router = useRouter()
const catalog = useRestaurantCatalog()

const order = ref<Order | null>(null)
const status = ref<LoadStatus>('loading')
const errorMessage = ref<string | null>(null)
const refreshing = ref(false)
const cancelOpen = ref(false)
const cancelError = ref<string | null>(null)
const paymentMethod = ref<OnlinePaymentMethod>('CARD')
const paying = ref(false)
const paymentError = ref<string | null>(null)
const paymentMessage = ref<string | null>(null)

const canCancel = computed(() => order.value?.status === 'PENDING' && !order.value.payed)

const lines = computed(() =>
  (order.value?.items ?? []).map((line) => {
    const item = catalog.itemsById.value.get(line.menuItemId)
    return {
      id: line.menuItemId,
      quantity: line.quantity,
      // An item removed from the menu after ordering can no longer be resolved.
      name: item?.name ?? 'Item no longer on the menu',
      total: line.unitPriceCents ? line.unitPriceCents * line.quantity : null,
    }
  }),
)
// Orders placed before items carried prices have a zero total: nothing to show or charge.
const totalLabel = computed(() => (order.value?.totalCents ? formatCents(order.value.totalCents) : null))
const canPay = computed(
  () => !!order.value && !order.value.payed && !!order.value.totalCents && order.value.status !== 'CANCELLED',
)

const details = computed<{ label: string; value: string }[]>(() => {
  const o = order.value
  if (!o) return []
  if (o.orderType === 'DELIVERY') {
    return [
      { label: 'Delivery time', value: formatEpoch(o.estimatedDeliveryTime) },
      { label: 'Address', value: o.deliveryAddress ?? '' },
      { label: 'Doorbell', value: o.bellName ?? '' },
      { label: 'Phone', value: o.customerPhone ?? '' },
    ]
  }
  if (o.orderType === 'TAKEAWAY') {
    return [
      { label: 'Pickup time', value: formatEpoch(o.pickupTime) },
      { label: 'Name', value: o.customerName ?? '' },
    ]
  }
  return [
    { label: 'Table', value: String(o.tableNumber ?? '') },
    { label: 'Guests', value: String(o.numberOfGuests ?? '') },
  ]
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
    // Lines fall back to "Item no longer on the menu"; prices and total come from the order.
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
  try {
    await ordersApi.cancelOrder(props.orderId)
    cancelOpen.value = false
    await fetchOrder()
  } catch (err) {
    cancelOpen.value = false
    cancelError.value = err instanceof ApiError ? err.message : 'The order could not be cancelled.'
  }
}

async function pay(): Promise<void> {
  if (!canPay.value || !order.value) return
  paying.value = true
  paymentError.value = null
  paymentMessage.value = null
  try {
    const result = await paymentsApi.payOrder(order.value.orderId, paymentMethod.value)
    if (result.status === 'COMPLETED') {
      paymentMessage.value = `Payment of ${formatCents(result.amount)} completed.`
    } else {
      paymentError.value = `The payment was not completed (status: ${result.status.toLowerCase()}).`
    }
    // order-service is updated by payment-service: re-read the order to show the new state.
    await fetchOrder()
  } catch (err) {
    paymentError.value = err instanceof ApiError ? err.message : 'The payment could not be processed.'
  } finally {
    paying.value = false
  }
}

onMounted(load)
watch(() => props.orderId, load)
</script>

<template>
  <div class="mx-auto max-w-4xl">
    <Button variant="ghost" class="mb-4 -ml-2.5" @click="router.push({ name: 'my-orders' })">
      <ArrowLeftIcon data-icon="inline-start" /> My orders
    </Button>

    <StateBlock
      :status="status"
      :error-message="errorMessage"
      empty-title="Order not found"
      empty-body="It does not exist, or it belongs to another account."
      :empty-icon="ReceiptTextIcon"
      empty-action-label="Go to my orders"
      @retry="load"
      @empty-action="router.push({ name: 'my-orders' })"
    >
      <div v-if="order" class="flex flex-col gap-6">
        <header class="flex flex-wrap items-end justify-between gap-4">
          <div class="flex flex-col gap-1">
            <p class="text-xs font-semibold tracking-wider text-primary uppercase">
              {{ TYPE_LABELS[order.orderType] }} order
            </p>
            <h1 class="text-2xl font-extrabold md:text-3xl">{{ catalog.restaurant.value?.name ?? 'Your order' }}</h1>
            <p class="flex flex-wrap items-center gap-2 text-sm text-muted-foreground">
              <span class="tabular-nums">#{{ order.orderId.slice(0, 8) }}</span>
              <Badge v-if="order.payed" variant="outline" class="text-success">Paid</Badge>
              <Badge v-else-if="order.status !== 'CANCELLED'" variant="outline" class="text-warning">Not paid yet</Badge>
            </p>
          </div>
          <Button variant="outline" :disabled="refreshing" @click="refresh">
            <Spinner v-if="refreshing" data-icon="inline-start" />
            <RotateCwIcon v-else data-icon="inline-start" />
            Refresh status
          </Button>
        </header>

        <Card>
          <CardHeader>
            <CardDescription>Status</CardDescription>
            <!-- aria-live so that a refreshed status is announced to screen-reader users. -->
            <CardTitle class="text-2xl font-extrabold" aria-live="polite">{{ STATUS_LABELS[order.status] }}</CardTitle>
          </CardHeader>
          <CardContent class="pt-2">
            <Alert v-if="order.status === 'CANCELLED'">
              <XCircleIcon />
              <AlertTitle>This order was cancelled</AlertTitle>
              <AlertDescription>It was cancelled before the restaurant started preparing it.</AlertDescription>
            </Alert>
            <OrderProgress v-else :type="order.orderType" :status="order.status" />
          </CardContent>
        </Card>

        <div class="grid gap-6 md:grid-cols-[3fr_2fr]">
          <Card>
            <CardHeader>
              <CardTitle class="text-lg">Items</CardTitle>
            </CardHeader>
            <CardContent class="flex flex-col gap-4">
              <ul class="flex flex-col gap-3">
                <li v-for="line in lines" :key="line.id" class="flex gap-3 text-sm">
                  <span class="font-semibold tabular-nums">{{ line.quantity }}&times;</span>
                  <span class="min-w-0 flex-1">{{ line.name }}</span>
                  <span v-if="line.total" class="tabular-nums">{{ formatCents(line.total) }}</span>
                </li>
              </ul>
              <template v-if="totalLabel">
                <Separator />
                <div class="flex items-baseline justify-between">
                  <span class="font-semibold">Total</span>
                  <strong class="text-2xl tabular-nums">{{ totalLabel }}</strong>
                </div>
              </template>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle class="text-lg">Details</CardTitle>
            </CardHeader>
            <CardContent>
              <dl class="grid grid-cols-[max-content_1fr] gap-x-4 gap-y-3 text-sm">
                <template v-for="row in details" :key="row.label">
                  <dt class="text-muted-foreground">{{ row.label }}</dt>
                  <dd class="font-medium break-words">{{ row.value }}</dd>
                </template>
              </dl>
            </CardContent>
          </Card>
        </div>

        <Card v-if="canPay">
          <form class="contents" @submit.prevent="pay">
            <CardHeader>
              <CardTitle class="text-lg">Pay for your order</CardTitle>
              <CardDescription>The restaurant starts preparing it as soon as it is confirmed.</CardDescription>
            </CardHeader>
            <CardContent>
              <FieldSet>
                <FieldLegend class="sr-only">Payment method</FieldLegend>
                <RadioGroup v-model="paymentMethod" class="grid gap-3 sm:grid-cols-2">
                  <ChoiceCard id="pay-card" value="CARD" title="Card" description="Credit or debit card" :icon="CreditCardIcon" />
                  <ChoiceCard id="pay-mobile" value="MOBILE_PAYMENT" title="Mobile payment" description="Pay with your phone" :icon="SmartphoneIcon" />
                </RadioGroup>
              </FieldSet>
            </CardContent>
            <CardFooter class="justify-end">
              <Button type="submit" size="lg" class="h-11 w-full sm:w-auto" :disabled="paying">
                <Spinner v-if="paying" data-icon="inline-start" />
                Pay {{ totalLabel }}
              </Button>
            </CardFooter>
          </form>
        </Card>
        <p v-else-if="!order.payed && !order.totalCents && order.status !== 'CANCELLED'" class="text-sm text-muted-foreground">
          This order cannot be paid online: it was placed before orders carried prices.
        </p>

        <Alert v-if="paymentMessage" role="status" class="text-success">
          <CircleCheckIcon />
          <AlertTitle>Thank you!</AlertTitle>
          <AlertDescription>{{ paymentMessage }}</AlertDescription>
        </Alert>
        <Alert v-if="paymentError" variant="destructive" role="alert">
          <CircleAlertIcon />
          <AlertTitle>Payment failed</AlertTitle>
          <AlertDescription>{{ paymentError }}</AlertDescription>
        </Alert>

        <div v-if="order.status !== 'CANCELLED'" class="flex flex-col items-start gap-2 border-t pt-6">
          <Button variant="destructive" :disabled="!canCancel" @click="cancelOpen = true">
            <XCircleIcon data-icon="inline-start" /> Cancel order
          </Button>
          <p v-if="!canCancel" class="text-sm text-muted-foreground">
            {{
              order.payed
                ? 'A paid order cannot be cancelled online.'
                : 'An order can be cancelled only before the restaurant starts preparing it.'
            }}
          </p>
          <Alert v-if="cancelError" variant="destructive" role="alert">
            <CircleAlertIcon />
            <AlertDescription>{{ cancelError }}</AlertDescription>
          </Alert>
        </div>

        <ConfirmDialog
          :open="cancelOpen"
          title="Cancel this order?"
          body="The restaurant has not started preparing it yet. This cannot be undone."
          confirm-label="Cancel order"
          cancel-label="Keep order"
          @confirm="confirmCancel"
          @cancel="cancelOpen = false"
        />
      </div>
    </StateBlock>
  </div>
</template>
