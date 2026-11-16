<script setup lang="ts">
// Book a table, or change a booking: day, arrival time and number of guests. Presentational: the
// view calls the API and passes back `submitting`, `error` and, once booked, the `confirmed`
// reservation, which turns the dialog into a confirmation.
//
// Only arrival times the service accepts are offered: quarters of an hour, in the future, within
// the restaurant's opening hours (up to an hour before closing). Whether a table is free is only
// known by trying: a 409 comes back as `error`, and the customer picks another time.

import { computed, reactive, watch } from 'vue'
import { CalendarCheckIcon, CircleAlertIcon } from '@lucide/vue'

import QuantityStepper from '@/components/QuantityStepper.vue'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Field, FieldDescription, FieldGroup, FieldLabel } from '@/components/ui/field'
import { NativeSelect, NativeSelectOption } from '@/components/ui/native-select'
import { Spinner } from '@/components/ui/spinner'
import type { ReservationInput } from '@/api/reservations'
import { arrivalTimes, bookableDays, formatDay, formatTimeRange, localDateTime } from '@/utils/reservations'
import type { Reservation } from '@/types'

const props = withDefaults(
  defineProps<{
    open: boolean
    restaurantName: string
    /** Opening hours ("HH:MM"), bounding the arrival times offered. */
    opening: string
    closing: string
    /** Seats of the largest table: no party can be larger. */
    maxGuests: number
    /** Prefilled values when changing a reservation. */
    initial?: { date: string; time: string; guests: number } | null
    submitting?: boolean
    error?: string | null
    confirmed?: Reservation | null
  }>(),
  { initial: null, submitting: false, error: null, confirmed: null },
)
const emit = defineEmits<{ submit: [input: ReservationInput]; close: []; viewReservations: [] }>()

const days = bookableDays()
const form = reactive({ date: days[0].value, time: '', guests: 2 })

const changing = computed(() => props.initial !== null)
const times = computed(() => arrivalTimes(form.date, props.opening, props.closing))

// Reset the form every time the dialog opens.
watch(
  () => props.open,
  (open) => {
    if (!open) return
    form.date = props.initial?.date ?? days[0].value
    form.guests = Math.min(props.initial?.guests ?? 2, props.maxGuests)
    form.time = props.initial?.time ?? ''
  },
  { immediate: true },
)

// Keep the chosen time valid when the day changes (e.g. today's early times are gone).
watch(
  times,
  (list) => {
    if (!list.includes(form.time)) form.time = list[0] ?? ''
  },
  { immediate: true },
)

function submit(): void {
  if (!form.time) return
  emit('submit', {
    start: localDateTime(form.date, form.time).toISOString(),
    numberOfGuests: form.guests,
  })
}

function onOpenChange(open: boolean): void {
  if (!open) emit('close')
}
</script>

<template>
  <Dialog :open="open" @update:open="onOpenChange">
    <DialogContent class="sm:max-w-md">
      <template v-if="confirmed">
        <DialogHeader>
          <span class="mb-1 grid size-10 place-items-center rounded-xl bg-success/12 text-success" aria-hidden="true">
            <CalendarCheckIcon class="size-5" />
          </span>
          <DialogTitle>{{ changing ? 'Reservation updated' : 'Your table is booked' }}</DialogTitle>
          <DialogDescription>
            Table {{ confirmed.tableNumber }} at {{ restaurantName }} for {{ confirmed.numberOfGuests }}
            {{ confirmed.numberOfGuests === 1 ? 'guest' : 'guests' }}.
          </DialogDescription>
        </DialogHeader>
        <p class="rounded-xl bg-muted p-4 text-sm">
          <span class="font-semibold">{{ formatDay(confirmed.start) }}</span><br />
          <span class="tabular-nums">{{ formatTimeRange(confirmed) }}</span>
        </p>
        <DialogFooter>
          <Button v-if="!changing" variant="outline" @click="emit('viewReservations')">My reservations</Button>
          <Button @click="emit('close')">Done</Button>
        </DialogFooter>
      </template>

      <form v-else class="contents" novalidate @submit.prevent="submit">
        <DialogHeader>
          <DialogTitle>{{ changing ? 'Change your reservation' : `Book a table at ${restaurantName}` }}</DialogTitle>
          <DialogDescription>
            Choose when you arrive: the table is yours for two hours. We assign the table that fits your party.
          </DialogDescription>
        </DialogHeader>

        <FieldGroup>
          <div class="grid gap-4 sm:grid-cols-2">
            <Field>
              <FieldLabel for="reservation-day">Day</FieldLabel>
              <NativeSelect id="reservation-day" v-model="form.date" class="w-full">
                <NativeSelectOption v-for="day in days" :key="day.value" :value="day.value">{{ day.label }}</NativeSelectOption>
              </NativeSelect>
            </Field>
            <Field>
              <FieldLabel for="reservation-time">Arrival</FieldLabel>
              <NativeSelect id="reservation-time" v-model="form.time" class="w-full" :disabled="times.length === 0">
                <NativeSelectOption v-if="times.length === 0" value="">No times left</NativeSelectOption>
                <NativeSelectOption v-for="time in times" :key="time" :value="time">{{ time }}</NativeSelectOption>
              </NativeSelect>
            </Field>
          </div>
          <FieldDescription v-if="times.length === 0">
            The restaurant takes no more arrivals on this day: pick another one.
          </FieldDescription>

          <Field orientation="horizontal" class="items-center justify-between">
            <div class="flex flex-col gap-0.5">
              <FieldLabel>Guests</FieldLabel>
              <FieldDescription>Up to {{ maxGuests }} at one table.</FieldDescription>
            </div>
            <QuantityStepper
              :quantity="form.guests"
              label="guests"
              :disabled="form.guests >= maxGuests"
              @increment="form.guests = Math.min(form.guests + 1, maxGuests)"
              @decrement="form.guests = Math.max(form.guests - 1, 1)"
            />
          </Field>
        </FieldGroup>

        <Alert v-if="error" variant="destructive" role="alert">
          <CircleAlertIcon />
          <AlertDescription>{{ error }}</AlertDescription>
        </Alert>

        <DialogFooter>
          <Button type="button" variant="outline" @click="emit('close')">Cancel</Button>
          <Button type="submit" :disabled="submitting || !form.time">
            <Spinner v-if="submitting" data-icon="inline-start" />
            {{ changing ? 'Save changes' : 'Book table' }}
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  </Dialog>
</template>
