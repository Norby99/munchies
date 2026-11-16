<script setup lang="ts">
// Customer's table reservations (the gateway scopes GET /reservations/ to the session user),
// split into upcoming and past/canceled. An upcoming reservation can be changed (time, guests) or
// canceled until it starts; while it is active the customer can order to the table from the
// restaurant's menu (CheckoutView offers "At my table").
//
// Changing reuses the booking dialog, which needs the restaurant's hours and largest table: they
// are read when the customer asks to change, not for every reservation up front.

import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { CalendarDaysIcon, PencilIcon, RotateCwIcon, UsersIcon, UtensilsIcon, XIcon } from '@lucide/vue'

import ConfirmDialog from '@/components/ConfirmDialog.vue'
import PageHeader from '@/components/PageHeader.vue'
import ReservationDialog from '@/components/ReservationDialog.vue'
import StateBlock from '@/components/StateBlock.vue'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { Spinner } from '@/components/ui/spinner'
import { ApiError } from '@/api/client'
import * as reservationsApi from '@/api/reservations'
import type { ReservationInput } from '@/api/reservations'
import * as restaurantsApi from '@/api/restaurants'
import { formatDay, formatTimeRange, isActive, isChangeable, isOngoing, splitInstant } from '@/utils/reservations'
import { SCHEDULER_DEFAULT_POLICY } from '@/utils/schedulerDefaults'
import type { LoadStatus, Reservation } from '@/types'

const router = useRouter()

const reservations = ref<Reservation[]>([])
const restaurantNames = ref<Record<string, string>>({})
const status = ref<LoadStatus>('loading')
const errorMessage = ref<string | null>(null)
// Errors of one card's action (cancel), keyed by reservation id.
const cardErrors = ref<Record<string, string>>({})
// Announced through an aria-live region after a change or a cancellation.
const announcement = ref('')

// Upcoming soonest first; past and canceled most recent first.
const groups = computed(() => [
  {
    id: 'upcoming',
    title: 'Upcoming',
    list: reservations.value.filter((r) => isActive(r)).sort((a, b) => a.start.localeCompare(b.start)),
  },
  {
    id: 'past',
    title: 'Past and canceled',
    list: reservations.value.filter((r) => !isActive(r)).sort((a, b) => b.start.localeCompare(a.start)),
  },
])

async function resolveRestaurantNames(list: Reservation[]): Promise<void> {
  const missing = [...new Set(list.map((r) => r.restaurantId))].filter((id) => !restaurantNames.value[id])
  const resolved = await Promise.allSettled(missing.map((id) => restaurantsApi.getRestaurant(id)))
  resolved.forEach((result) => {
    if (result.status === 'fulfilled') restaurantNames.value[result.value.id] = result.value.name
  })
}

async function load(): Promise<void> {
  status.value = 'loading'
  errorMessage.value = null
  cardErrors.value = {}
  try {
    reservations.value = await reservationsApi.listReservations()
    await resolveRestaurantNames(reservations.value)
    status.value = reservations.value.length === 0 ? 'empty' : 'ready'
  } catch (err) {
    errorMessage.value = err instanceof ApiError ? err.message : null
    status.value = 'error'
  }
}

onMounted(load)

function restaurantName(reservation: Reservation): string {
  return restaurantNames.value[reservation.restaurantId] ?? 'Restaurant'
}

function replace(updated: Reservation): void {
  reservations.value = reservations.value.map((r) => (r.id === updated.id ? updated : r))
}

// ---- Change ----

const editing = ref<{
  reservation: Reservation
  opening: string
  closing: string
  maxGuests: number
  submitting: boolean
  error: string | null
  confirmed: Reservation | null
} | null>(null)
const preparing = ref<string | null>(null)

async function openChange(reservation: Reservation): Promise<void> {
  preparing.value = reservation.id
  delete cardErrors.value[reservation.id]
  try {
    const [policy, tables] = await Promise.all([
      restaurantsApi.getCapacityPolicy(reservation.restaurantId),
      restaurantsApi.getTables(reservation.restaurantId),
    ])
    const hours = policy ?? SCHEDULER_DEFAULT_POLICY
    editing.value = {
      reservation,
      opening: hours.opening,
      closing: hours.closing,
      maxGuests: Math.max(reservation.numberOfGuests, ...tables.map((t) => t.seats)),
      submitting: false,
      error: null,
      confirmed: null,
    }
  } catch (err) {
    cardErrors.value[reservation.id] =
      err instanceof ApiError ? err.message : 'The reservation cannot be changed right now. Try again.'
  } finally {
    preparing.value = null
  }
}

const editingInitial = computed(() => {
  if (!editing.value) return null
  const { reservation } = editing.value
  return { ...splitInstant(reservation.start), guests: reservation.numberOfGuests }
})

async function saveChange(input: ReservationInput): Promise<void> {
  if (!editing.value) return
  editing.value.submitting = true
  editing.value.error = null
  try {
    const updated = await reservationsApi.modifyReservation(editing.value.reservation.id, input)
    replace(updated)
    editing.value.confirmed = updated
    announcement.value = `Reservation at ${restaurantName(updated)} updated.`
  } catch (err) {
    editing.value.error =
      err instanceof ApiError && err.code === 409
        ? 'No table is free for your party at that time, or the reservation has already started.'
        : err instanceof ApiError
          ? err.message
          : 'The reservation could not be changed.'
  } finally {
    if (editing.value) editing.value.submitting = false
  }
}

// ---- Cancel ----

const cancelTarget = ref<Reservation | null>(null)
const canceling = ref(false)

async function confirmCancel(): Promise<void> {
  const target = cancelTarget.value
  if (!target) return
  canceling.value = true
  delete cardErrors.value[target.id]
  try {
    replace(await reservationsApi.cancelReservation(target.id))
    announcement.value = `Reservation at ${restaurantName(target)} canceled.`
  } catch (err) {
    cardErrors.value[target.id] = err instanceof ApiError ? err.message : 'The reservation could not be canceled.'
  } finally {
    canceling.value = false
    cancelTarget.value = null
  }
}

function orderAtTable(reservation: Reservation): void {
  router.push({ name: 'restaurant-detail', params: { restaurantId: reservation.restaurantId } })
}
</script>

<template>
  <div class="mx-auto max-w-3xl">
    <PageHeader eyebrow="Tables" title="My reservations" description="Your booked tables: change or cancel them before they start.">
      <template #actions>
        <Button v-if="status === 'ready'" variant="outline" @click="load">
          <RotateCwIcon data-icon="inline-start" /> Refresh
        </Button>
      </template>
    </PageHeader>

    <p class="sr-only" aria-live="polite">{{ announcement }}</p>

    <StateBlock
      :status="status"
      :error-message="errorMessage"
      empty-title="No reservations yet"
      empty-body="Open a restaurant and choose &quot;Book a table&quot; to reserve one."
      :empty-icon="CalendarDaysIcon"
      empty-action-label="Find a restaurant"
      @retry="load"
      @empty-action="router.push({ name: 'explore' })"
    >
      <template v-for="group in groups" :key="group.id">
        <section v-if="group.list.length" class="mb-10" :aria-labelledby="`my-reservations-${group.id}`">
          <h2 :id="`my-reservations-${group.id}`" class="mb-3 flex items-center gap-2 text-lg font-bold">
            {{ group.title }}
            <Badge variant="secondary" class="tabular-nums">{{ group.list.length }}</Badge>
          </h2>
          <ul class="flex flex-col gap-3">
            <li v-for="reservation in group.list" :key="reservation.id">
              <Card :class="reservation.status === 'CANCELED' ? 'opacity-75' : ''">
                <CardHeader>
                  <div class="flex flex-wrap items-center gap-2">
                    <CardTitle class="text-base font-bold">
                      <RouterLink
                        :to="{ name: 'restaurant-detail', params: { restaurantId: reservation.restaurantId } }"
                        class="underline-offset-4 hover:underline"
                      >
                        {{ restaurantName(reservation) }}
                      </RouterLink>
                    </CardTitle>
                    <Badge v-if="reservation.status === 'CANCELED'" variant="outline" class="text-muted-foreground">Canceled</Badge>
                    <Badge v-else-if="isOngoing(reservation)" variant="outline" class="bg-success/12 text-success">Now</Badge>
                  </div>
                </CardHeader>
                <CardContent class="flex flex-col gap-1.5 text-sm">
                  <p class="flex items-center gap-2">
                    <CalendarDaysIcon class="size-4 shrink-0 text-muted-foreground" aria-hidden="true" />
                    <span>
                      <span class="font-semibold">{{ formatDay(reservation.start) }}</span>,
                      <span class="tabular-nums">{{ formatTimeRange(reservation) }}</span>
                    </span>
                  </p>
                  <p class="flex items-center gap-2">
                    <UsersIcon class="size-4 shrink-0 text-muted-foreground" aria-hidden="true" />
                    {{ reservation.numberOfGuests }} {{ reservation.numberOfGuests === 1 ? 'guest' : 'guests' }} &middot;
                    table {{ reservation.tableNumber }}
                  </p>
                  <p v-if="cardErrors[reservation.id]" class="text-destructive" role="alert">{{ cardErrors[reservation.id] }}</p>
                </CardContent>
                <CardFooter v-if="isActive(reservation)" class="flex flex-wrap gap-2">
                  <Button size="sm" @click="orderAtTable(reservation)">
                    <UtensilsIcon data-icon="inline-start" /> Order to the table
                  </Button>
                  <template v-if="isChangeable(reservation)">
                    <Button size="sm" variant="outline" :disabled="preparing === reservation.id" @click="openChange(reservation)">
                      <Spinner v-if="preparing === reservation.id" data-icon="inline-start" />
                      <PencilIcon v-else data-icon="inline-start" />
                      Change
                    </Button>
                    <Button size="sm" variant="ghost" @click="cancelTarget = reservation">
                      <XIcon data-icon="inline-start" /> Cancel
                    </Button>
                  </template>
                </CardFooter>
              </Card>
            </li>
          </ul>
        </section>
      </template>
    </StateBlock>

    <ReservationDialog
      :open="editing !== null"
      :restaurant-name="editing ? restaurantName(editing.reservation) : ''"
      :opening="editing?.opening ?? SCHEDULER_DEFAULT_POLICY.opening"
      :closing="editing?.closing ?? SCHEDULER_DEFAULT_POLICY.closing"
      :max-guests="editing?.maxGuests ?? 1"
      :initial="editingInitial"
      :submitting="editing?.submitting"
      :error="editing?.error"
      :confirmed="editing?.confirmed"
      @submit="saveChange"
      @close="editing = null"
      @view-reservations="editing = null"
    />

    <ConfirmDialog
      :open="cancelTarget !== null"
      title="Cancel this reservation?"
      :body="cancelTarget ? `Your table at ${restaurantName(cancelTarget)} on ${formatDay(cancelTarget.start)} will be freed for others.` : ''"
      confirm-label="Cancel reservation"
      cancel-label="Keep it"
      :pending="canceling"
      @confirm="confirmCancel"
      @cancel="cancelTarget = null"
    />
  </div>
</template>
