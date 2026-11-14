<script setup lang="ts">
// Scheduling settings of one restaurant, as read by scheduler-service: where the restaurant is
// (start and end of every delivery trip) and its capacity policy (slots, opening hours, lead time,
// delivery trips). Deliveries are offered to customers only once both are saved; takeaway works
// without them, on the scheduler's default policy.
//
// The kitchen units per slot (maxCapacityUnits) are currently decided by the scheduler's admission
// quota agent, which overrides the value saved here; the field is kept because it is part of the
// policy contract and becomes the fallback when the agent has no answer.

import { reactive, ref, watch } from 'vue'
import { CircleAlertIcon, LocateFixedIcon } from '@lucide/vue'

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
import { Field, FieldDescription, FieldGroup, FieldLabel, FieldLegend, FieldSet } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Spinner } from '@/components/ui/spinner'
import { ApiError } from '@/api/client'
import * as restaurantsApi from '@/api/restaurants'
import type { CapacityPolicy } from '@/api/restaurants'
import { SCHEDULER_DEFAULT_POLICY } from '@/utils/schedulerDefaults'
import type { Restaurant } from '@/types'

const props = defineProps<{ restaurant: Restaurant | null }>()
const emit = defineEmits<{ close: [] }>()

// Pre-filled with the scheduler's own default, so a first save changes nothing but enables
// deliveries.
const DEFAULT_POLICY: CapacityPolicy = { ...SCHEDULER_DEFAULT_POLICY }

const loading = ref(false)
const saving = ref(false)
const locating = ref(false)
const error = ref<string | null>(null)
const policy = reactive<CapacityPolicy>({ ...DEFAULT_POLICY })
// Kept as strings: an empty field must stay empty, not become 0.
const location = reactive({ lat: '', lng: '' })

watch(
  () => props.restaurant?.id,
  async (id) => {
    if (!id) return
    error.value = null
    loading.value = true
    Object.assign(policy, DEFAULT_POLICY)
    Object.assign(location, { lat: '', lng: '' })
    try {
      const [savedPolicy, savedLocation] = await Promise.all([
        restaurantsApi.getCapacityPolicy(id),
        restaurantsApi.getLocation(id),
      ])
      if (savedPolicy) Object.assign(policy, savedPolicy)
      if (savedLocation) Object.assign(location, { lat: String(savedLocation.lat), lng: String(savedLocation.lng) })
    } catch (err) {
      error.value = err instanceof ApiError ? err.message : 'The current settings could not be loaded.'
    } finally {
      loading.value = false
    }
  },
  { immediate: true },
)

function useCurrentPosition(): void {
  if (!navigator.geolocation) {
    error.value = 'This browser cannot share its position: enter the coordinates by hand.'
    return
  }
  locating.value = true
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      location.lat = pos.coords.latitude.toFixed(6)
      location.lng = pos.coords.longitude.toFixed(6)
      locating.value = false
    },
    () => {
      error.value = 'The position could not be read: enter the coordinates by hand.'
      locating.value = false
    },
    { timeout: 10_000 },
  )
}

function parsedLocation(): { lat: number; lng: number } | null {
  const lat = Number(location.lat)
  const lng = Number(location.lng)
  if (location.lat.trim() === '' || location.lng.trim() === '') return null
  if (!Number.isFinite(lat) || !Number.isFinite(lng) || Math.abs(lat) > 90 || Math.abs(lng) > 180) return null
  return { lat, lng }
}

async function save(): Promise<void> {
  if (!props.restaurant) return
  error.value = null
  const point = parsedLocation()
  if (!point) {
    error.value = 'Enter a valid latitude (-90 to 90) and longitude (-180 to 180).'
    return
  }
  if (policy.opening >= policy.closing) {
    error.value = 'Opening time must be before closing time.'
    return
  }
  saving.value = true
  try {
    await Promise.all([
      restaurantsApi.updateLocation(props.restaurant.id, point),
      restaurantsApi.updateCapacityPolicy(props.restaurant.id, { ...policy }),
    ])
    emit('close')
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : 'The settings could not be saved.'
  } finally {
    saving.value = false
  }
}

function onOpenChange(open: boolean): void {
  if (!open) emit('close')
}
</script>

<template>
  <Dialog :open="restaurant !== null" @update:open="onOpenChange">
    <DialogContent class="max-h-[90dvh] overflow-y-auto sm:max-w-xl">
      <form class="contents" novalidate @submit.prevent="save">
        <DialogHeader>
          <DialogTitle>Scheduling for {{ restaurant?.name }}</DialogTitle>
          <DialogDescription>
            Customers book a time slot at checkout. Deliveries are offered once the location and these settings are saved.
          </DialogDescription>
        </DialogHeader>

        <div v-if="loading" class="flex justify-center py-10"><Spinner class="size-6" /></div>

        <FieldGroup v-else>
          <FieldSet>
            <FieldLegend>Location</FieldLegend>
            <FieldDescription>Every delivery trip starts and ends here.</FieldDescription>
            <div class="grid gap-4 sm:grid-cols-[1fr_1fr_auto] sm:items-end">
              <Field>
                <FieldLabel for="sched-lat">Latitude</FieldLabel>
                <Input id="sched-lat" v-model="location.lat" class="h-10" inputmode="decimal" placeholder="44.1391" />
              </Field>
              <Field>
                <FieldLabel for="sched-lng">Longitude</FieldLabel>
                <Input id="sched-lng" v-model="location.lng" class="h-10" inputmode="decimal" placeholder="12.2431" />
              </Field>
              <Button type="button" variant="outline" class="h-10" :disabled="locating" @click="useCurrentPosition">
                <Spinner v-if="locating" data-icon="inline-start" />
                <LocateFixedIcon v-else data-icon="inline-start" />
                Use my position
              </Button>
            </div>
          </FieldSet>

          <FieldSet>
            <FieldLegend>Opening hours and slots</FieldLegend>
            <div class="grid gap-4 sm:grid-cols-3">
              <Field>
                <FieldLabel for="sched-opening">Opens at</FieldLabel>
                <Input id="sched-opening" v-model="policy.opening" class="h-10" type="time" required />
              </Field>
              <Field>
                <FieldLabel for="sched-closing">Closes at</FieldLabel>
                <Input id="sched-closing" v-model="policy.closing" class="h-10" type="time" required />
              </Field>
              <Field>
                <FieldLabel for="sched-slot">Slot length (min)</FieldLabel>
                <Input id="sched-slot" v-model.number="policy.slotMinutes" class="h-10" type="number" min="5" step="5" required />
              </Field>
              <Field>
                <FieldLabel for="sched-lead">Prep lead time (min)</FieldLabel>
                <Input id="sched-lead" v-model.number="policy.prepLeadMinutes" class="h-10" type="number" min="0" required />
              </Field>
              <Field>
                <FieldLabel for="sched-advance">Bookable days ahead</FieldLabel>
                <Input id="sched-advance" v-model.number="policy.maxAdvanceDays" class="h-10" type="number" min="1" required />
              </Field>
              <Field>
                <FieldLabel for="sched-units">Kitchen units per slot</FieldLabel>
                <Input id="sched-units" v-model.number="policy.maxCapacityUnits" class="h-10" type="number" min="1" required />
              </Field>
            </div>
            <FieldDescription>
              One unit is one item ordered. For now the scheduler's admission quota agent decides the units per slot and
              overrides this value.
            </FieldDescription>
          </FieldSet>

          <FieldSet>
            <FieldLegend>Delivery trips</FieldLegend>
            <div class="grid gap-4 sm:grid-cols-3">
              <Field>
                <FieldLabel for="sched-trips">Trips per slot</FieldLabel>
                <Input id="sched-trips" v-model.number="policy.maxTripsPerSlot" class="h-10" type="number" min="0" required />
              </Field>
              <Field>
                <FieldLabel for="sched-km">Max km per trip</FieldLabel>
                <Input id="sched-km" v-model.number="policy.maxTripKmBudget" class="h-10" type="number" min="0.5" step="0.5" required />
              </Field>
              <Field>
                <FieldLabel for="sched-detour">Max detour (km)</FieldLabel>
                <Input id="sched-detour" v-model.number="policy.maxDetourKm" class="h-10" type="number" min="0" step="0.1" required />
              </Field>
            </div>
          </FieldSet>
        </FieldGroup>

        <Alert v-if="error" variant="destructive" role="alert">
          <CircleAlertIcon />
          <AlertDescription>{{ error }}</AlertDescription>
        </Alert>

        <DialogFooter>
          <Button type="button" variant="outline" @click="emit('close')">Cancel</Button>
          <Button type="submit" :disabled="saving || loading">
            <Spinner v-if="saving" data-icon="inline-start" />
            Save settings
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  </Dialog>
</template>
