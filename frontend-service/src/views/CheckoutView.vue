<script setup lang="ts">
// Customer checkout: pick how to receive the order, the day and a time slot, fill the per-type
// details, review the bag, then book it through scheduler-service (POST /schedule/{id}/orders),
// which places the order in order-service once the kitchen (and, for delivery, a driver trip) can
// take it. Only DELIVERY and TAKEAWAY are offered: DINE_IN needs a table, and table assignment
// belongs to table-reservation-service, which is not implemented yet.
//
// Slots come from GET /schedule/{id}/availability for the chosen day and type. Delivery slots also
// depend on where the customer is (a trip must be able to reach them), so the customer's position
// is asked for before delivery slots are shown; a restaurant that has not set its location and
// capacity policy offers no delivery at all.
//
// On success the cart is emptied and the user lands on the order page, where they can pay.

import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowLeftIcon,
  BikeIcon,
  CalendarXIcon,
  CircleAlertIcon,
  LocateFixedIcon,
  ShoppingBagIcon,
  StoreIcon,
} from '@lucide/vue'

import ChoiceCard from '@/components/ChoiceCard.vue'
import PageHeader from '@/components/PageHeader.vue'
import StateBlock from '@/components/StateBlock.vue'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel, FieldLegend, FieldSet } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { RadioGroup } from '@/components/ui/radio-group'
import { Separator } from '@/components/ui/separator'
import { Spinner } from '@/components/ui/spinner'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'
import { ApiError } from '@/api/client'
import * as scheduleApi from '@/api/schedule'
import type { AvailableSlot, GeoPoint } from '@/api/schedule'
import { useOnline } from '@/composables/useOnline'
import { useRestaurantCatalog } from '@/composables/useRestaurantCatalog'
import { useCartStore } from '@/stores/cart'
import { useSessionStore } from '@/stores/session'
import { toDateInput } from '@/utils/time'
import type { LoadStatus } from '@/types'

const props = defineProps<{ restaurantId: string }>()

type CheckoutType = 'DELIVERY' | 'TAKEAWAY'

// Days offered at checkout. The restaurant may allow fewer (its maxAdvanceDays): those days then
// simply come back with no slots.
const DAYS_AHEAD = 7

// Shared look of a selected day/slot toggle.
const TOGGLE_ON = 'data-[state=on]:border-primary data-[state=on]:bg-primary data-[state=on]:text-primary-foreground'

const router = useRouter()
const cart = useCartStore()
const session = useSessionStore()
const { isOnline } = useOnline()
const catalog = useRestaurantCatalog()

const status = ref<LoadStatus>('loading')
const submitting = ref(false)
const formError = ref<string | null>(null)

const dayFormat = new Intl.DateTimeFormat('en-GB', { weekday: 'short', day: 'numeric' })
const days = Array.from({ length: DAYS_AHEAD }, (_, i) => {
  const d = new Date()
  d.setDate(d.getDate() + i)
  return { value: toDateInput(d), label: i === 0 ? 'Today' : i === 1 ? 'Tomorrow' : dayFormat.format(d) }
})

const form = reactive({
  orderType: 'DELIVERY' as CheckoutType,
  date: days[0].value,
  // Epoch ms of the chosen slot start, as a string (ToggleGroup values are strings); '' = none.
  slot: '',
  deliveryAddress: '',
  bellName: '',
  customerPhone: '',
  customerName: session.user?.username ?? '',
  // Kept as strings: an empty field must stay empty, not become 0.
  lat: '',
  lng: '',
})
const fieldErrors = reactive<Partial<Record<keyof typeof form, string>>>({})

const slots = ref<AvailableSlot[]>([])
const slotsLoading = ref(false)
const slotsError = ref<string | null>(null)
// False when the restaurant does not offer the chosen order type (delivery not set up).
const typeOffered = ref(true)
const locating = ref(false)

const lines = computed(() => cart.lines(catalog.mergedMenu.value))
const totalLabel = computed(() => cart.totalLabel(catalog.mergedMenu.value))
const slotLegend = computed(() => (form.orderType === 'DELIVERY' ? 'Delivery time' : 'Pickup time'))

// The customer's position, once both coordinates are valid; required for delivery slots.
const position = computed<GeoPoint | null>(() => {
  if (form.lat.trim() === '' || form.lng.trim() === '') return null
  const lat = Number(form.lat)
  const lng = Number(form.lng)
  if (!Number.isFinite(lat) || !Number.isFinite(lng) || Math.abs(lat) > 90 || Math.abs(lng) > 180) return null
  return { lat, lng }
})
const needsPosition = computed(() => form.orderType === 'DELIVERY' && position.value === null)

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

// Every call gets a sequence number, so a slow, stale answer never overwrites a newer one.
let slotsRequest = 0

async function fetchSlots(): Promise<void> {
  const request = ++slotsRequest
  slotsError.value = null
  if (needsPosition.value) {
    slots.value = []
    typeOffered.value = true
    slotsLoading.value = false
    return
  }
  slotsLoading.value = true
  try {
    const availability = await scheduleApi.getAvailability(
      props.restaurantId,
      form.date,
      form.orderType,
      form.orderType === 'DELIVERY' ? (position.value ?? undefined) : undefined,
    )
    if (request !== slotsRequest) return
    typeOffered.value = availability.slotMinutes > 0
    slots.value = availability.ranges
  } catch (err) {
    if (request !== slotsRequest) return
    slots.value = []
    slotsError.value = err instanceof ApiError ? err.message : 'The available times could not be loaded.'
  } finally {
    if (request === slotsRequest) {
      slotsLoading.value = false
      if (!slots.value.some((s) => String(s.startEpochMs) === form.slot)) form.slot = ''
    }
  }
}

onMounted(async () => {
  await load()
  if (status.value === 'ready') await fetchSlots()
})
watch(
  () => [form.orderType, form.date, position.value?.lat, position.value?.lng],
  () => {
    if (status.value === 'ready') fetchSlots()
  },
)

function useCurrentPosition(): void {
  delete fieldErrors.lat
  if (!navigator.geolocation) {
    fieldErrors.lat = 'This browser cannot share your position: enter the coordinates by hand.'
    return
  }
  locating.value = true
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      form.lat = pos.coords.latitude.toFixed(6)
      form.lng = pos.coords.longitude.toFixed(6)
      locating.value = false
    },
    () => {
      fieldErrors.lat = 'Your position could not be read: enter the coordinates by hand.'
      locating.value = false
    },
    { timeout: 10_000 },
  )
}

function validate(): boolean {
  for (const key of Object.keys(fieldErrors) as (keyof typeof form)[]) delete fieldErrors[key]

  if (form.orderType === 'DELIVERY') {
    if (!position.value) fieldErrors.lat = 'Share your position or enter valid coordinates.'
    if (!form.deliveryAddress.trim()) fieldErrors.deliveryAddress = 'Enter the delivery address.'
    if (!form.bellName.trim()) fieldErrors.bellName = 'Enter the name on the doorbell.'
    if (!/^\+?[0-9 ]{6,20}$/.test(form.customerPhone.trim())) {
      fieldErrors.customerPhone = 'Enter a valid phone number (digits, optional leading +).'
    }
  } else if (!form.customerName.trim()) {
    fieldErrors.customerName = 'Enter the name to give at pickup.'
  }
  if (!form.slot) fieldErrors.slot = 'Choose a time.'

  return Object.keys(fieldErrors).length === 0
}

async function submit(): Promise<void> {
  formError.value = null
  if (!validate()) {
    formError.value = 'Some fields need your attention.'
    return
  }

  const items = lines.value.map((line) => ({ menuItemId: line.itemId, quantity: line.quantity }))
  const slotStartEpochMs = Number(form.slot)

  submitting.value = true
  try {
    const booked = await scheduleApi.scheduleOrder(
      props.restaurantId,
      form.orderType === 'DELIVERY'
        ? {
            orderType: 'DELIVERY',
            items,
            slotStartEpochMs,
            deliveryAddress: form.deliveryAddress.trim(),
            deliveryLocation: position.value!,
            bellName: form.bellName.trim(),
            customerPhone: form.customerPhone.trim(),
          }
        : { orderType: 'TAKEAWAY', items, slotStartEpochMs, customerName: form.customerName.trim() },
    )
    cart.clear()
    await router.replace({ name: 'order-detail', params: { orderId: booked.orderId } })
  } catch (err) {
    formError.value = err instanceof ApiError ? err.message : 'The order could not be placed.'
    // The slot (or its last delivery trip) may have just been taken: show what is left.
    if (err instanceof ApiError && err.code === 409) await fetchSlots()
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
            <CardDescription>Pick a time the kitchen can still take. Dine-in will come with table reservations.</CardDescription>
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

              <Field v-if="form.orderType === 'DELIVERY'" :data-invalid="!!fieldErrors.lat || undefined">
                <FieldLabel for="checkout-lat">Your position</FieldLabel>
                <FieldDescription>Needed to check that a driver can reach you at the time you pick.</FieldDescription>
                <div class="grid gap-3 sm:grid-cols-[1fr_1fr_auto]">
                  <Input
                    id="checkout-lat"
                    v-model="form.lat"
                    class="h-10"
                    inputmode="decimal"
                    placeholder="Latitude"
                    aria-label="Latitude"
                    :aria-invalid="!!fieldErrors.lat || undefined"
                  />
                  <Input
                    id="checkout-lng"
                    v-model="form.lng"
                    class="h-10"
                    inputmode="decimal"
                    placeholder="Longitude"
                    aria-label="Longitude"
                    :aria-invalid="!!fieldErrors.lat || undefined"
                  />
                  <Button type="button" variant="outline" class="h-10" :disabled="locating" @click="useCurrentPosition">
                    <Spinner v-if="locating" data-icon="inline-start" />
                    <LocateFixedIcon v-else data-icon="inline-start" />
                    Use my position
                  </Button>
                </div>
                <FieldError v-if="fieldErrors.lat">{{ fieldErrors.lat }}</FieldError>
              </Field>

              <FieldSet>
                <FieldLegend variant="label">Day</FieldLegend>
                <ToggleGroup v-model="form.date" type="single" variant="outline" :spacing="2" class="flex-wrap" aria-label="Day">
                  <ToggleGroupItem v-for="day in days" :key="day.value" :value="day.value" :class="['h-9 px-3', TOGGLE_ON]">
                    {{ day.label }}
                  </ToggleGroupItem>
                </ToggleGroup>
              </FieldSet>

              <FieldSet :data-invalid="!!fieldErrors.slot || undefined">
                <FieldLegend variant="label">{{ slotLegend }}</FieldLegend>
                <div v-if="slotsLoading" class="flex items-center gap-2 py-2 text-sm text-muted-foreground" role="status">
                  <Spinner /> Looking for available times...
                </div>
                <p v-else-if="needsPosition" class="text-sm text-muted-foreground">
                  Share your position above to see the delivery times.
                </p>
                <Alert v-else-if="slotsError" variant="destructive">
                  <CircleAlertIcon />
                  <AlertDescription>{{ slotsError }}</AlertDescription>
                </Alert>
                <Alert v-else-if="!typeOffered">
                  <BikeIcon />
                  <AlertTitle>This restaurant does not deliver yet</AlertTitle>
                  <AlertDescription>You can still order for takeaway.</AlertDescription>
                </Alert>
                <p v-else-if="slots.length === 0" class="flex items-center gap-2 text-sm text-muted-foreground">
                  <CalendarXIcon class="size-4" aria-hidden="true" /> No times left on this day: try another one.
                </p>
                <ToggleGroup
                  v-else
                  v-model="form.slot"
                  type="single"
                  variant="outline"
                  :spacing="2"
                  class="flex-wrap"
                  :aria-label="slotLegend"
                >
                  <ToggleGroupItem
                    v-for="slot in slots"
                    :key="slot.startEpochMs"
                    :value="String(slot.startEpochMs)"
                    :class="['h-9 px-3 tabular-nums', TOGGLE_ON]"
                  >
                    {{ slot.start }}
                  </ToggleGroupItem>
                </ToggleGroup>
                <FieldError v-if="fieldErrors.slot">{{ fieldErrors.slot }}</FieldError>
              </FieldSet>

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
