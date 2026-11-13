<script setup lang="ts">
// Customer restaurant page: header, menu picker, dishes grouped by category, and the cart.
// Reached from the restaurant search (ExploreView) or by direct link (e.g. a QR code). The header
// shows the opening hours when the restaurant has set them (its capacity policy) and, when the
// restaurant has configured its dining room tables, lets a customer book a table
// (table-reservation-service): the booking dialog offers arrival times within those hours.
// Every menu is fetched in full up front, since each tab shows its availability.
//
// The cart sits in a sticky side panel from the lg breakpoint; on smaller screens a floating
// "view bag" bar opens it in a bottom sheet. Checkout continues on its own route (CheckoutView).

import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { CalendarPlusIcon, ClockIcon, MapPinIcon, PhoneIcon, ShoppingBagIcon, SquareMenuIcon } from '@lucide/vue'

import MenuItemRow from '@/components/MenuItemRow.vue'
import MenuTabs from '@/components/MenuTabs.vue'
import OrderDraft from '@/components/OrderDraft.vue'
import ReservationDialog from '@/components/ReservationDialog.vue'
import StateBlock from '@/components/StateBlock.vue'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from '@/components/ui/sheet'
import { useOnline } from '@/composables/useOnline'
import { ApiError } from '@/api/client'
import * as reservationsApi from '@/api/reservations'
import type { ReservationInput } from '@/api/reservations'
import * as restaurantsApi from '@/api/restaurants'
import { useCartStore } from '@/stores/cart'
import { useMenusStore } from '@/stores/menus'
import { useRestaurantsStore } from '@/stores/restaurants'
import { useSessionStore } from '@/stores/session'
import { openingStatus, type OpeningStatus } from '@/utils/openingHours'
import { SCHEDULER_DEFAULT_POLICY } from '@/utils/schedulerDefaults'
import { describe, isValid } from '@/utils/validity'
import type { DiningTable, LoadStatus, Menu, Reservation, Restaurant } from '@/types'

const props = defineProps<{ restaurantId: string }>()

const router = useRouter()

const restaurants = useRestaurantsStore()
const menus = useMenusStore()
const cart = useCartStore()
const session = useSessionStore()
const { isOnline } = useOnline()

const restaurant = ref<Restaurant | null>(null)
const status = ref<LoadStatus>('loading')
const loadedMenus = ref<Menu[]>([])
const bagOpen = ref(false)
// Opening hours from the capacity policy, or the scheduler's default (marked indicative) while the
// restaurant has not set its own; null only while loading or if the hours could not be read.
const hours = ref<OpeningStatus | null>(null)
// The same hours as raw "HH:MM" bounds, for the arrival times offered when booking a table.
const openingHours = ref<{ opening: string; closing: string }>({
  opening: SCHEDULER_DEFAULT_POLICY.opening,
  closing: SCHEDULER_DEFAULT_POLICY.closing,
})
// The dining room; empty means the restaurant takes no reservations.
const tables = ref<DiningTable[]>([])
const booking = ref({ open: false, submitting: false, error: null as string | null, confirmed: null as Reservation | null })

const canBook = computed(() => session.role === 'CUSTOMER' && tables.value.length > 0)
const maxGuests = computed(() => Math.max(1, ...tables.value.map((t) => t.seats)))

const activeMenu = computed(() => loadedMenus.value.find((m) => m.id === menus.activeMenuId) ?? null)
const menuOpen = computed(() => (activeMenu.value ? isValid(activeMenu.value.validity) : false))
const orderingDisabled = computed(() => !menuOpen.value || !isOnline.value)
const checkoutDisabled = computed(() => orderingDisabled.value || cart.isEmpty)
const bagCount = computed(() =>
  activeMenu.value ? cart.lines(activeMenu.value).reduce((sum, line) => sum + line.quantity, 0) : 0,
)
const bagTotal = computed(() => (activeMenu.value ? cart.totalLabel(activeMenu.value) : ''))

async function load(): Promise<void> {
  status.value = 'loading'
  hours.value = null
  tables.value = []
  loadHours()
  loadTables()
  try {
    restaurant.value = await restaurantsApi.getRestaurant(props.restaurantId)
    restaurants.cacheOne(restaurant.value)
    cart.setRestaurant(props.restaurantId)

    await menus.fetchSummaries(props.restaurantId)
    const summaries = menus.summaries[props.restaurantId] ?? []
    loadedMenus.value = await Promise.all(
      summaries.map((summary) => menus.fetchFull(props.restaurantId, summary.id)),
    )

    const firstOpen = loadedMenus.value.find((menu) => isValid(menu.validity))
    const initial = firstOpen ?? loadedMenus.value[0]
    if (initial) menus.setActiveMenu(initial.id)

    status.value = loadedMenus.value.length === 0 ? 'empty' : 'ready'
  } catch {
    status.value = 'error'
  }
}

// Loaded on its own: hours are a detail of the header, so a failure only hides that line and
// never blocks the menu. A restaurant without its own hours (404, read as null) shows the
// scheduler's default ones, the hours its pickup slots are actually offered in.
async function loadHours(): Promise<void> {
  const requestedFor = props.restaurantId
  try {
    const policy = await restaurantsApi.getCapacityPolicy(requestedFor)
    if (requestedFor !== props.restaurantId) return
    const source = policy ?? SCHEDULER_DEFAULT_POLICY
    hours.value = openingStatus(source.opening, source.closing, new Date(), policy === null)
    openingHours.value = { opening: source.opening, closing: source.closing }
  } catch {
    // Leave the hours out.
  }
}

// Also on its own: without tables the "Book a table" action is simply not offered.
async function loadTables(): Promise<void> {
  const requestedFor = props.restaurantId
  try {
    const found = await restaurantsApi.getTables(requestedFor)
    if (requestedFor === props.restaurantId) tables.value = found
  } catch {
    // No booking offered.
  }
}

function openBooking(): void {
  booking.value = { open: true, submitting: false, error: null, confirmed: null }
}

async function book(input: ReservationInput): Promise<void> {
  booking.value.submitting = true
  booking.value.error = null
  try {
    booking.value.confirmed = await reservationsApi.reserveTable(props.restaurantId, input)
  } catch (err) {
    booking.value.error =
      err instanceof ApiError && err.code === 409
        ? 'No table is free for your party at that time. Try another time or day.'
        : err instanceof ApiError
          ? err.message
          : 'The table could not be booked.'
  } finally {
    booking.value.submitting = false
  }
}

onMounted(load)
watch(() => props.restaurantId, load)

function checkout(): void {
  bagOpen.value = false
  router.push({ name: 'checkout', params: { restaurantId: props.restaurantId } })
}
</script>

<template>
  <div>
    <header v-if="restaurant" class="mb-8 flex flex-col gap-4 rounded-2xl bg-accent p-6 text-accent-foreground md:p-8">
      <p class="text-xs font-semibold tracking-wider uppercase">Restaurant</p>
      <h1 class="text-3xl font-extrabold text-foreground md:text-4xl">{{ restaurant.name }}</h1>
      <ul class="flex flex-wrap gap-x-6 gap-y-2 text-sm">
        <li class="flex items-center gap-1.5"><MapPinIcon class="size-4" aria-hidden="true" />{{ restaurant.address }}</li>
        <li class="flex items-center gap-1.5">
          <PhoneIcon class="size-4" aria-hidden="true" />
          <a :href="`tel:${restaurant.phone}`" class="underline-offset-4 hover:underline">{{ restaurant.phone }}</a>
        </li>
        <li v-if="hours" class="flex flex-wrap items-center gap-x-1.5 gap-y-1">
          <ClockIcon class="size-4" aria-hidden="true" />
          <span>
            Hours <span class="font-semibold tabular-nums">{{ hours.hours }}</span
            ><span v-if="hours.indicative" class="text-accent-foreground/80"> (indicative)</span>
          </span>
          <Badge
            variant="outline"
            :class="['ml-1 border-current/30 bg-card', hours.open ? 'text-success' : 'text-muted-foreground']"
          >
            {{ hours.label }}
          </Badge>
        </li>
      </ul>
      <div v-if="canBook">
        <Button variant="outline" class="bg-card" @click="openBooking">
          <CalendarPlusIcon data-icon="inline-start" /> Book a table
        </Button>
      </div>
    </header>

    <ReservationDialog
      v-if="restaurant"
      :open="booking.open"
      :restaurant-name="restaurant.name"
      :opening="openingHours.opening"
      :closing="openingHours.closing"
      :max-guests="maxGuests"
      :submitting="booking.submitting"
      :error="booking.error"
      :confirmed="booking.confirmed"
      @submit="book"
      @close="booking.open = false"
      @view-reservations="router.push({ name: 'my-reservations' })"
    />

    <StateBlock
      :status="status"
      empty-title="No menus yet"
      empty-body="This restaurant has not published a menu yet. Check back soon."
      :empty-icon="SquareMenuIcon"
      empty-action-label="Reload"
      error-message="We could not load this restaurant. Check the link, or try again."
      @retry="load"
      @empty-action="load"
    >
      <div class="grid items-start gap-8 lg:grid-cols-[minmax(0,1fr)_360px]">
        <div>
          <MenuTabs :menus="loadedMenus" :active-id="menus.activeMenuId" @select="menus.setActiveMenu" />

          <Alert v-if="activeMenu && !menuOpen" role="status" class="mb-6">
            <ClockIcon />
            <AlertTitle>This menu is not available right now</AlertTitle>
            <AlertDescription>{{ describe(activeMenu.validity) }}. You can browse it, but not order from it.</AlertDescription>
          </Alert>

          <section
            v-for="category in activeMenu?.categories ?? []"
            :key="category.id"
            class="mb-10"
            :aria-labelledby="`category-${category.id}`"
          >
            <div class="mb-4 flex items-baseline gap-3">
              <h2 :id="`category-${category.id}`" class="text-xl font-bold">{{ category.name }}</h2>
              <span class="text-sm text-muted-foreground">
                {{ category.items.length }} {{ category.items.length === 1 ? 'dish' : 'dishes' }}
              </span>
            </div>
            <p v-if="category.variations.length" class="-mt-2 mb-4 text-sm text-muted-foreground">
              Options for every dish: {{ category.variations.map((v) => v.name).join(', ') }}
            </p>
            <div class="grid gap-3 sm:grid-cols-2">
              <MenuItemRow
                v-for="item in category.items"
                :key="item.id"
                :item="item"
                :quantity="cart.items[item.id] ?? 0"
                :disabled="orderingDisabled"
                @add="cart.add(item.id)"
                @remove="cart.decrement(item.id)"
              />
            </div>
          </section>
        </div>

        <Card v-if="activeMenu" class="sticky top-24 hidden lg:flex">
          <CardHeader>
            <CardTitle class="text-lg">Your bag</CardTitle>
          </CardHeader>
          <CardContent>
            <OrderDraft :menu="activeMenu" :checkout-disabled="checkoutDisabled" @checkout="checkout" />
          </CardContent>
        </Card>
      </div>

      <!-- Phones and tablets: a floating bar above the tab bar opens the bag in a bottom sheet. -->
      <div
        v-if="activeMenu && bagCount > 0"
        class="fixed inset-x-4 bottom-[calc(4.5rem+env(safe-area-inset-bottom))] z-30 md:bottom-6 lg:hidden"
      >
        <button
          type="button"
          class="mx-auto flex h-14 w-full max-w-md items-center gap-3 rounded-full bg-primary px-5 text-primary-foreground shadow-lg outline-none focus-visible:ring-3 focus-visible:ring-ring/50"
          @click="bagOpen = true"
        >
          <span class="grid size-8 place-items-center rounded-full bg-primary-foreground/20 text-sm font-bold tabular-nums">
            {{ bagCount }}
          </span>
          <span class="font-semibold">View bag</span>
          <span class="ml-auto font-semibold tabular-nums">{{ bagTotal }}</span>
          <ShoppingBagIcon class="size-5" aria-hidden="true" />
        </button>
      </div>
      <Sheet v-model:open="bagOpen">
        <SheetContent side="bottom" class="max-h-[85svh] overflow-y-auto rounded-t-2xl">
          <SheetHeader>
            <SheetTitle>Your bag</SheetTitle>
            <SheetDescription>{{ restaurant?.name }}</SheetDescription>
          </SheetHeader>
          <div class="px-4 pb-6">
            <OrderDraft v-if="activeMenu" :menu="activeMenu" :checkout-disabled="checkoutDisabled" @checkout="checkout" />
          </div>
        </SheetContent>
      </Sheet>
    </StateBlock>
  </div>
</template>
