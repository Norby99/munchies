<script setup lang="ts">
// Manager's Delivery page for one restaurant: everything scheduler-service needs to plan driver
// trips, on one screen around a map.
// - Restaurant position: the start and end of every trip (pin on the map, or coordinates).
// - Drivers and trips: trips per slot, max km per trip, max detour (part of the capacity policy).
// - Delivery zones: areas whose addresses may share a driver.
//
// How the three trip rules combine, as explained on the page: zones decide whether two deliveries
// MAY share a driver (the manager's knowledge of rivers, railways, ring roads, which straight-line
// distances miss), the detour whether it is WORTH adding the stop, and the trip budget whether one
// driver CAN do the whole round. Without zones, only detour and budget apply (the behaviour before
// zones existed). With zones, an address outside every zone still gets a trip of its own.
//
// The capacity policy also holds opening hours, slots and kitchen units, edited in the scheduling
// dialog: this page reads the whole policy and writes it back with only the delivery fields
// changed. Saving sends the three settings together; deliveries are offered once the position and
// a capacity policy are saved.

import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowLeftIcon,
  CircleAlertIcon,
  CircleCheckIcon,
  LocateFixedIcon,
  MapPinIcon,
  PentagonIcon,
  TrashIcon,
} from '@lucide/vue'

import MapZoneEditor from '@/components/MapZoneEditor.vue'
import PageHeader from '@/components/PageHeader.vue'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Field, FieldDescription, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Spinner } from '@/components/ui/spinner'
import { ApiError } from '@/api/client'
import * as restaurantsApi from '@/api/restaurants'
import type { CapacityPolicy, GeoLocation } from '@/api/restaurants'
import { useRestaurantsStore } from '@/stores/restaurants'
import { draftsFrom, MAX_ZONE_NAME_LENGTH, toWire, zonesProblem, zoneStyle, type ZoneDraft } from '@/utils/deliveryZones'
import { SCHEDULER_DEFAULT_POLICY } from '@/utils/schedulerDefaults'

const props = defineProps<{ restaurantId: string }>()

const router = useRouter()
const restaurants = useRestaurantsStore()

const restaurantName = ref('')
const loading = ref(true)
const loadError = ref<string | null>(null)
const saving = ref(false)
const locating = ref(false)
const error = ref<string | null>(null)
const saved = ref(false)

// The whole saved policy (or the scheduler's default when there is none yet), so that saving
// here leaves the hours, slots and kitchen units as they are.
const basePolicy = ref<CapacityPolicy>({ ...SCHEDULER_DEFAULT_POLICY })
const trips = ref({
  maxTripsPerSlot: SCHEDULER_DEFAULT_POLICY.maxTripsPerSlot,
  maxTripKmBudget: SCHEDULER_DEFAULT_POLICY.maxTripKmBudget,
  maxDetourKm: SCHEDULER_DEFAULT_POLICY.maxDetourKm,
})
const origin = ref<GeoLocation | null>(null)
// The coordinate fields, kept as text so that a field being typed in can be empty or partial.
const latText = ref('')
const lngText = ref('')
const zones = ref<ZoneDraft[]>([])
const selectedKey = ref<string | null>(null)
const placingOrigin = ref(false)

// The pin and the fields show the same position: each follows the other. A field already holding
// the same number keeps its text, so typing "44." is not rewritten to "44" mid-entry.
watch(origin, (point) => {
  if (!point) {
    latText.value = ''
    lngText.value = ''
    return
  }
  if (latText.value.trim() === '' || Number(latText.value) !== point.lat) latText.value = String(point.lat)
  if (lngText.value.trim() === '' || Number(lngText.value) !== point.lng) lngText.value = String(point.lng)
})

function onCoordinatesInput(): void {
  const lat = Number(latText.value)
  const lng = Number(lngText.value)
  const valid =
    latText.value.trim() !== '' &&
    lngText.value.trim() !== '' &&
    Number.isFinite(lat) &&
    Number.isFinite(lng) &&
    Math.abs(lat) <= 90 &&
    Math.abs(lng) <= 180
  if (valid && (lat !== origin.value?.lat || lng !== origin.value?.lng)) origin.value = { lat, lng }
}

// Any edit hides the "saved" confirmation, so it never describes an older state.
watch([trips, origin, zones], () => (saved.value = false), { deep: true })

async function load(): Promise<void> {
  loading.value = true
  loadError.value = null
  restaurants.setActive(props.restaurantId)
  try {
    const cached = restaurants.items.find((r) => r.id === props.restaurantId)
    const [restaurant, policy, location, savedZones] = await Promise.all([
      cached ?? restaurantsApi.getRestaurant(props.restaurantId),
      restaurantsApi.getCapacityPolicy(props.restaurantId),
      restaurantsApi.getLocation(props.restaurantId),
      restaurantsApi.getDeliveryZones(props.restaurantId),
    ])
    if (!cached) restaurants.cacheOne(restaurant)
    restaurantName.value = restaurant.name
    basePolicy.value = policy ?? { ...SCHEDULER_DEFAULT_POLICY }
    trips.value = {
      maxTripsPerSlot: basePolicy.value.maxTripsPerSlot,
      maxTripKmBudget: basePolicy.value.maxTripKmBudget,
      maxDetourKm: basePolicy.value.maxDetourKm,
    }
    origin.value = location
    zones.value = draftsFrom(savedZones)
    saved.value = false
  } catch (err) {
    loadError.value = err instanceof ApiError ? err.message : 'The delivery settings could not be loaded.'
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(() => props.restaurantId, load)

function useCurrentPosition(): void {
  error.value = null
  if (!navigator.geolocation) {
    error.value = 'This browser cannot share its position: place the pin on the map or enter the coordinates.'
    return
  }
  locating.value = true
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      origin.value = { lat: Number(pos.coords.latitude.toFixed(6)), lng: Number(pos.coords.longitude.toFixed(6)) }
      locating.value = false
    },
    () => {
      error.value = 'The position could not be read: place the pin on the map or enter the coordinates.'
      locating.value = false
    },
    { timeout: 10_000 },
  )
}

function renameZone(key: string, name: string): void {
  zones.value = zones.value.map((z) => (z.key === key ? { ...z, name } : z))
}

function removeZone(key: string): void {
  zones.value = zones.value.filter((z) => z.key !== key)
  if (selectedKey.value === key) selectedKey.value = null
}

const tripsProblem = computed(() => {
  const t = trips.value
  if (!Number.isInteger(t.maxTripsPerSlot) || t.maxTripsPerSlot < 1) return 'Trips per slot must be a whole number, at least 1.'
  if (!(t.maxTripKmBudget > 0)) return 'Max km per trip must be greater than 0.'
  if (!(t.maxDetourKm >= 0)) return 'Max detour cannot be negative.'
  return null
})

async function save(): Promise<void> {
  error.value = null
  saved.value = false
  const point = origin.value
  if (!point) {
    error.value = 'Place the restaurant on the map, or enter its coordinates: every trip starts there.'
    return
  }
  const problem = tripsProblem.value ?? zonesProblem(zones.value)
  if (problem) {
    error.value = problem
    return
  }
  saving.value = true
  try {
    const policy: CapacityPolicy = { ...basePolicy.value, ...trips.value }
    // One after the other, never in parallel: restaurant-service loads the whole restaurant,
    // changes one field and writes the whole document back, so concurrent updates of the same
    // restaurant overwrite each other (seen in practice: the zones save erased the position and
    // the policy saved a few milliseconds earlier).
    await restaurantsApi.updateLocation(props.restaurantId, point)
    const savedPolicy = await restaurantsApi.updateCapacityPolicy(props.restaurantId, policy)
    const savedZones = await restaurantsApi.updateDeliveryZones(props.restaurantId, toWire(zones.value))
    basePolicy.value = savedPolicy
    // New zones come back with their ids; keep the UI keys so the map and the selection stay put.
    zones.value = zones.value.map((draft, i) => ({ ...draft, id: savedZones[i]?.id ?? draft.id, name: draft.name.trim() }))
    // Let the "any edit hides the confirmation" watcher see the update above first.
    await nextTick()
    saved.value = true
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : 'The delivery settings could not be saved.'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div>
    <Button variant="ghost" class="mb-4 -ml-2.5" @click="router.push({ name: 'manager-restaurants' })">
      <ArrowLeftIcon data-icon="inline-start" /> All restaurants
    </Button>
    <PageHeader
      eyebrow="Delivery"
      :title="restaurantName || 'Delivery'"
      description="Where trips start, how many drivers you have, and which addresses may share a driver."
    >
      <template v-if="!loading && !loadError" #actions>
        <Button type="submit" form="delivery-form" :disabled="saving">
          <Spinner v-if="saving" data-icon="inline-start" />
          Save delivery settings
        </Button>
      </template>
    </PageHeader>

    <div v-if="loading" class="flex justify-center py-16"><Spinner class="size-6" /></div>

    <Alert v-else-if="loadError" variant="destructive" role="alert">
      <CircleAlertIcon />
      <AlertDescription class="flex flex-wrap items-center gap-3">
        {{ loadError }}
        <Button variant="outline" size="sm" @click="load">Try again</Button>
      </AlertDescription>
    </Alert>

    <form v-else id="delivery-form" class="flex flex-col gap-6" novalidate @submit.prevent="save">
      <!-- Outcome of the last save, announced to screen readers. -->
      <div aria-live="polite">
        <Alert v-if="error" variant="destructive">
          <CircleAlertIcon />
          <AlertDescription>{{ error }}</AlertDescription>
        </Alert>
        <Alert v-else-if="saved">
          <CircleCheckIcon class="text-success" />
          <AlertDescription>Delivery settings saved. They apply to the next orders customers place.</AlertDescription>
        </Alert>
      </div>

      <div class="grid items-start gap-6 lg:grid-cols-[minmax(0,1fr)_22rem]">
        <Card>
          <CardHeader>
            <CardTitle class="text-xl font-bold">Map</CardTitle>
            <CardDescription>
              Draw a zone with the polygon tool at the top left; edit, move or remove zones with the tools below it. The
              round pin is the restaurant: drag it, or place it with the button.
            </CardDescription>
          </CardHeader>
          <CardContent class="flex flex-col gap-3">
            <div class="flex flex-wrap items-center gap-2">
              <Button
                type="button"
                :variant="placingOrigin ? 'default' : 'outline'"
                :aria-pressed="placingOrigin"
                @click="placingOrigin = !placingOrigin"
              >
                <MapPinIcon data-icon="inline-start" />
                {{ placingOrigin ? 'Click the map to place the restaurant' : 'Place restaurant on map' }}
              </Button>
              <p v-if="!origin" class="text-sm text-muted-foreground">The restaurant has no position yet.</p>
            </div>
            <MapZoneEditor
              v-model:zones="zones"
              v-model:origin="origin"
              v-model:selected-key="selectedKey"
              v-model:placing-origin="placingOrigin"
            />
          </CardContent>
        </Card>

        <div class="flex flex-col gap-6">
          <Card>
            <CardHeader>
              <CardTitle class="text-base font-bold">Restaurant position</CardTitle>
              <CardDescription>Every delivery trip starts and ends here.</CardDescription>
            </CardHeader>
            <CardContent>
              <FieldGroup class="gap-4">
                <div class="grid grid-cols-2 gap-3">
                  <Field>
                    <FieldLabel for="delivery-lat">Latitude</FieldLabel>
                    <Input id="delivery-lat" v-model="latText" class="h-10" inputmode="decimal" placeholder="44.0000" @input="onCoordinatesInput" />
                  </Field>
                  <Field>
                    <FieldLabel for="delivery-lng">Longitude</FieldLabel>
                    <Input id="delivery-lng" v-model="lngText" class="h-10" inputmode="decimal" placeholder="12.6522" @input="onCoordinatesInput" />
                  </Field>
                </div>
                <Button type="button" variant="outline" :disabled="locating" @click="useCurrentPosition">
                  <Spinner v-if="locating" data-icon="inline-start" />
                  <LocateFixedIcon v-else data-icon="inline-start" />
                  Use my position
                </Button>
              </FieldGroup>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle class="text-base font-bold">Drivers and trips</CardTitle>
              <CardDescription>Distances are measured in a straight line from the restaurant.</CardDescription>
            </CardHeader>
            <CardContent>
              <FieldGroup class="gap-4">
                <Field>
                  <FieldLabel for="delivery-trips">Trips per slot</FieldLabel>
                  <Input id="delivery-trips" v-model.number="trips.maxTripsPerSlot" class="h-10" type="number" min="1" step="1" required />
                  <FieldDescription>How many drivers can leave in the same time slot.</FieldDescription>
                </Field>
                <Field>
                  <FieldLabel for="delivery-km">Max km per trip</FieldLabel>
                  <Input id="delivery-km" v-model.number="trips.maxTripKmBudget" class="h-10" type="number" min="0.5" step="0.5" required />
                  <FieldDescription>The whole round, from the restaurant through every stop and back.</FieldDescription>
                </Field>
                <Field>
                  <FieldLabel for="delivery-detour">Max detour (km)</FieldLabel>
                  <Input id="delivery-detour" v-model.number="trips.maxDetourKm" class="h-10" type="number" min="0" step="0.1" required />
                  <FieldDescription>How many km a new delivery may add to a trip to share its driver.</FieldDescription>
                </Field>
              </FieldGroup>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle class="text-base font-bold">Delivery zones</CardTitle>
              <CardDescription>
                Deliveries share a driver only if they are all in the same zone; detour and km per trip still apply.
                Zones may overlap. An address outside every zone is still delivered, by a driver of its own. Without
                zones, any deliveries can share a driver.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <p v-if="zones.length === 0" class="flex items-center gap-2 text-sm text-muted-foreground">
                <PentagonIcon class="size-4 shrink-0" aria-hidden="true" /> No zones yet: draw one on the map.
              </p>
              <ul v-else class="flex flex-col gap-2" aria-label="Delivery zones">
                <li
                  v-for="(zone, index) in zones"
                  :key="zone.key"
                  :class="[
                    'flex items-center gap-2 rounded-lg border p-2',
                    zone.key === selectedKey ? 'border-primary bg-primary/5' : 'border-border',
                  ]"
                >
                  <span :class="['size-3 shrink-0 rounded-full', zoneStyle(index).swatch]" aria-hidden="true"></span>
                  <Input
                    :model-value="zone.name"
                    :aria-label="`Name of zone ${index + 1}`"
                    :maxlength="MAX_ZONE_NAME_LENGTH"
                    class="h-9"
                    @update:model-value="renameZone(zone.key, String($event))"
                    @focus="selectedKey = zone.key"
                  />
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    :aria-label="`Remove zone ${zone.name || index + 1}`"
                    @click="removeZone(zone.key)"
                  >
                    <TrashIcon />
                  </Button>
                </li>
              </ul>
            </CardContent>
          </Card>
        </div>
      </div>
    </form>
  </div>
</template>
