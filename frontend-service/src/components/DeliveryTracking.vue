<script setup lang="ts">
// Tracking area of a delivery that is on the way: a map with the restaurant, the delivery address,
// the driver and the streets still to drive, plus the same information as text (distance left, expected arrival, stops
// before this one). Presentational: the parent owns the data (see useDeliveryTracking) and this
// component only draws it.
//
// The facts are plain text next to the map, so nothing is conveyed by the map alone. They are not
// in a live region: the position changes every few seconds and would flood a screen reader; only
// the arrival is announced.
//
// Leaflet is driven directly (no wrapper library). Markers are divIcons styled with the theme's
// semantic classes, so they follow the light and dark themes like the rest of the UI.

import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useResizeObserver } from '@vueuse/core'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { BikeIcon, CircleAlertIcon, MapPinIcon, StoreIcon } from '@lucide/vue'

import { Spinner } from '@/components/ui/spinner'
import type { GeoPoint, OrderTracking } from '@/api/schedule'
import type { TrackingState } from '@/composables/useDeliveryTracking'

const props = defineProps<{ tracking: OrderTracking | null; state: TrackingState }>()

const DOT = 'block size-4 rounded-full border-2 border-background shadow-md'
const MARKER_CLASSES = {
  origin: `${DOT} bg-foreground`,
  destination: `${DOT} bg-primary`,
  driver: 'block size-5 rounded-full border-2 border-background bg-info shadow-md ring-4 ring-info/30',
} as const

const mapElement = ref<HTMLDivElement | null>(null)
let map: L.Map | null = null
let originMarker: L.Marker | null = null
let destinationMarker: L.Marker | null = null
let driverMarker: L.Marker | null = null
let leftToDrive: L.Polyline | null = null
// The view is fitted once per order; afterwards the user's own pan and zoom are respected.
let fittedFor: string | null = null

function icon(kind: keyof typeof MARKER_CLASSES): L.DivIcon {
  const size = kind === 'driver' ? 20 : 16
  return L.divIcon({
    className: '',
    html: `<span class="${MARKER_CLASSES[kind]}"></span>`,
    iconSize: [size, size],
    iconAnchor: [size / 2, size / 2],
  })
}

function place(marker: L.Marker | null, kind: keyof typeof MARKER_CLASSES, point: GeoPoint, label: string): L.Marker {
  if (marker) return marker.setLatLng([point.lat, point.lng])
  // Not keyboard-focusable: the markers add nothing to the text facts below the map.
  return L.marker([point.lat, point.lng], { icon: icon(kind), keyboard: false, interactive: true })
    .bindTooltip(label)
    .addTo(map!)
}

function draw(): void {
  const t = props.tracking
  if (!map || !t) return
  originMarker = place(originMarker, 'origin', t.origin, 'Restaurant')
  destinationMarker = place(destinationMarker, 'destination', t.destination, 'Delivery address')

  if (t.driverPosition) {
    driverMarker = place(driverMarker, 'driver', t.driverPosition, 'Driver').setZIndexOffset(1000)
    // The streets still to drive. On a trip with several deliveries it passes by the earlier
    // ones first, so it is not always the shortest way to this address.
    const line: L.LatLngExpression[] = (t.routeAhead ?? []).map((p) => [p.lat, p.lng])
    if (leftToDrive) leftToDrive.setLatLngs(line)
    else leftToDrive = L.polyline(line, { className: 'stroke-info', weight: 4, opacity: 0.8, interactive: false }).addTo(map)
  } else {
    driverMarker?.remove()
    driverMarker = null
    leftToDrive?.remove()
    leftToDrive = null
  }

  // Nothing to fit while the container is hidden; the resize observer comes back here.
  if (fittedFor !== t.orderId && map.getSize().x > 0) {
    const points: L.LatLngExpression[] = [
      [t.origin.lat, t.origin.lng],
      [t.destination.lat, t.destination.lng],
    ]
    for (const p of t.routeAhead ?? []) points.push([p.lat, p.lng])
    if (t.driverPosition) points.push([t.driverPosition.lat, t.driverPosition.lng])
    map.fitBounds(L.latLngBounds(points), { padding: [32, 32], maxZoom: 16 })
    fittedFor = t.orderId
  }
}

onMounted(() => {
  if (!mapElement.value) return
  map = L.map(mapElement.value, { scrollWheelZoom: false })
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
  }).addTo(map)
  // Neutral view until the first position arrives; draw() fits the real bounds.
  map.setView([0, 0], 2)
  draw()
})

watch(() => props.tracking, draw)

// Leaflet measures its container once, when the map is created, and here that happens while the
// container is still hidden (no position yet) or still growing (dialog opening). Without this the
// map stays grey and the view is fitted to a zero-sized box. Whenever the container changes size,
// Leaflet is told and the view is fitted again.
useResizeObserver(mapElement, () => {
  if (!map) return
  map.invalidateSize()
  fittedFor = null
  draw()
})

onBeforeUnmount(() => {
  map?.remove()
  map = null
})

const headline = computed(() => {
  const t = props.tracking
  if (!t) return ''
  if (t.status === 'ARRIVED') return 'The driver has arrived'
  if (t.status === 'WAITING') return 'The driver has not left the restaurant yet'
  return 'The driver is on the way'
})

const distanceLabel = computed(() => {
  const km = props.tracking?.remainingKm ?? 0
  return km < 1 ? `${Math.round(km * 1000)} m` : `${km.toFixed(1)} km`
})

const etaLabel = computed(() => {
  const eta = props.tracking?.etaEpochMs
  if (!eta) return null
  const time = new Intl.DateTimeFormat('it-IT', { timeStyle: 'short' }).format(eta)
  const minutes = Math.max(1, Math.round((eta - Date.now()) / 60_000))
  return `${time} (about ${minutes} min)`
})

const updatedLabel = computed(() => {
  const at = props.tracking?.updatedAtEpochMs
  return at ? new Intl.DateTimeFormat('it-IT', { timeStyle: 'medium' }).format(at) : null
})
</script>

<template>
  <div class="flex flex-col gap-4">
    <div v-if="state === 'loading'" class="flex items-center gap-2 text-sm text-muted-foreground">
      <Spinner class="size-4" /> Looking for the driver&hellip;
    </div>
    <p v-else-if="state === 'unavailable'" class="text-sm text-muted-foreground">
      Live tracking is not available for this order.
    </p>
    <p v-else-if="state === 'error'" role="alert" class="flex items-center gap-2 text-sm text-destructive">
      <CircleAlertIcon class="size-4 shrink-0" aria-hidden="true" />
      The driver's position could not be loaded. It will be retried automatically.
    </p>

    <template v-if="tracking">
      <!-- Announced only when it changes (left / arrived), unlike the facts below. -->
      <p class="flex items-center gap-2 font-semibold" aria-live="polite">
        <BikeIcon class="size-5 text-info" aria-hidden="true" /> {{ headline }}
      </p>

      <dl
        v-if="tracking.status !== 'ARRIVED'"
        class="grid grid-cols-[max-content_1fr] gap-x-4 gap-y-2 text-sm"
      >
        <dt class="text-muted-foreground">Distance left</dt>
        <dd class="font-medium tabular-nums">{{ distanceLabel }}</dd>
        <template v-if="etaLabel">
          <dt class="text-muted-foreground">Expected arrival</dt>
          <dd class="font-medium tabular-nums">{{ etaLabel }}</dd>
        </template>
        <template v-if="tracking.stopsBefore > 0">
          <dt class="text-muted-foreground">Before this delivery</dt>
          <dd class="font-medium">
            {{ tracking.stopsBefore }} other {{ tracking.stopsBefore === 1 ? 'delivery' : 'deliveries' }}
          </dd>
        </template>
      </dl>
    </template>

    <!-- v-show, not v-if: Leaflet needs its container in the DOM from the start. `isolate` keeps
         Leaflet's high z-indexes below the app's dialogs and menus. -->
    <div v-show="tracking" class="flex flex-col gap-2">
      <div
        ref="mapElement"
        class="isolate h-72 w-full overflow-hidden rounded-xl border"
        role="region"
        aria-label="Map with the restaurant, the delivery address and the driver"
      ></div>
      <ul class="flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted-foreground">
        <!-- Each entry shows the marker itself: naming its colour would be wrong in one of the
             two themes, and the icon tells the three apart without relying on colour. -->
        <li class="flex items-center gap-1.5">
          <span class="size-2.5 rounded-full bg-foreground" aria-hidden="true"></span>
          <StoreIcon class="size-3.5" aria-hidden="true" /> Restaurant
        </li>
        <li class="flex items-center gap-1.5">
          <span class="size-2.5 rounded-full bg-info" aria-hidden="true"></span>
          <BikeIcon class="size-3.5" aria-hidden="true" /> Driver and the route left
        </li>
        <li class="flex items-center gap-1.5">
          <span class="size-2.5 rounded-full bg-primary" aria-hidden="true"></span>
          <MapPinIcon class="size-3.5" aria-hidden="true" /> Delivery address
        </li>
        <li v-if="updatedLabel" class="ml-auto tabular-nums">Updated at {{ updatedLabel }}</li>
      </ul>
    </div>
  </div>
</template>
