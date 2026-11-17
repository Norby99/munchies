<script setup lang="ts">
// Map editor of a restaurant's delivery setup: the delivery zones, drawn as polygons, and the
// restaurant's own position, the pin every trip starts from and returns to. Presentational: the
// parent owns the zones and the position (props down) and receives every change as an event.
//
// Leaflet is driven directly, like DeliveryTracking.vue. leaflet-geoman adds the toolbar at the
// top left: draw a polygon, edit its corners, move it, remove it. Zones may overlap, so snapping is
// only a help to draw neighbouring zones edge to edge. Self-intersecting outlines are refused while
// drawing: an "inside" would be ambiguous for the scheduler.
//
// The position can be placed by clicking the map after the parent turns on `placingOrigin`, and
// moved by dragging the pin; the parent also offers coordinates and "use my position" as a
// keyboard-friendly alternative. Geoman ignores the pin (pmIgnore), so its edit and removal tools
// never touch it.

import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useResizeObserver } from '@vueuse/core'
import L from 'leaflet'
import '@geoman-io/leaflet-geoman-free'
import 'leaflet/dist/leaflet.css'
import '@geoman-io/leaflet-geoman-free/dist/leaflet-geoman.css'

import type { GeoLocation } from '@/api/restaurants'
import { newZoneKey, nextZoneName, zoneStyle, type ZoneDraft } from '@/utils/deliveryZones'

const props = defineProps<{
  zones: ZoneDraft[]
  origin: GeoLocation | null
  selectedKey: string | null
  /** While true, the next click on the map places the restaurant there. */
  placingOrigin: boolean
}>()

const emit = defineEmits<{
  'update:zones': [zones: ZoneDraft[]]
  'update:origin': [origin: GeoLocation]
  'update:selectedKey': [key: string | null]
  'update:placingOrigin': [placing: boolean]
}>()

// Shown until the restaurant has a position or zones (the area the project is developed in).
const DEFAULT_CENTER: L.LatLngTuple = [44.0, 12.6522]
const DEFAULT_ZOOM = 13
const ORIGIN_ZOOM = 14
// About 10 cm: enough for a hand-drawn outline, and keeps the saved documents small.
const COORD_DECIMALS = 6

const mapElement = ref<HTMLDivElement | null>(null)
let map: L.Map | null = null
let originMarker: L.Marker | null = null
const layers = new Map<string, L.Polygon>()
// The colour classes currently on each polygon, to swap them when zones are reordered or removed.
const appliedClass = new Map<string, string>()

const round = (n: number) => Number(n.toFixed(COORD_DECIMALS))

function boundaryOf(layer: L.Polygon): GeoLocation[] {
  const rings = layer.getLatLngs() as L.LatLng[][]
  return (rings[0] ?? []).map((p) => ({ lat: round(p.lat), lng: round(p.lng) }))
}

function keyOf(layer: L.Layer): string | null {
  for (const [key, candidate] of layers) if (candidate === layer) return key
  return null
}

function emitBoundary(key: string): void {
  const layer = layers.get(key)
  if (!layer) return
  emit(
    'update:zones',
    props.zones.map((z) => (z.key === key ? { ...z, boundary: boundaryOf(layer) } : z)),
  )
}

/** Hooks a polygon (drawn by the user or loaded) to the zone it represents. */
function track(key: string, layer: L.Polygon): void {
  layers.set(key, layer)
  // While placing the restaurant, a click on a zone is meant for the map (the pin goes there),
  // not a selection of that zone.
  layer.on('click', () => {
    if (!props.placingOrigin) emit('update:selectedKey', key)
  })
  layer.on('pm:edit', () => emitBoundary(key))
  layer.on('pm:dragend', () => emitBoundary(key))
}

function setPathClass(key: string, layer: L.Polygon, classes: string): void {
  const element = layer.getElement()
  if (!element) return
  const previous = appliedClass.get(key)
  if (previous === classes) return
  if (previous) element.classList.remove(...previous.split(' '))
  element.classList.add(...classes.split(' '))
  appliedClass.set(key, classes)
}

/** Brings the polygons in line with `props.zones`: adds the missing, drops the removed, restyles all. */
function syncZones(): void {
  if (!map) return
  for (const [key, layer] of layers) {
    if (props.zones.some((z) => z.key === key)) continue
    layer.remove()
    layers.delete(key)
    appliedClass.delete(key)
  }
  props.zones.forEach((zone, index) => {
    let layer = layers.get(zone.key)
    if (!layer) {
      layer = L.polygon(
        zone.boundary.map((p) => [p.lat, p.lng] as L.LatLngTuple),
      ).addTo(map!)
      track(zone.key, layer)
    }
    const selected = zone.key === props.selectedKey
    layer.setStyle({ weight: selected ? 4 : 2, fillOpacity: selected ? 0.35 : 0.15 })
    if (selected) layer.bringToFront()
    setPathClass(zone.key, layer, zoneStyle(index).path)
    const label = zone.name.trim() || 'Unnamed zone'
    if (layer.getTooltip()) layer.setTooltipContent(label)
    else layer.bindTooltip(label, { sticky: true })
  })
}

function originIcon(): L.DivIcon {
  return L.divIcon({
    className: '',
    html: '<span class="block size-5 rounded-full border-[3px] border-background bg-foreground shadow-md"></span>',
    iconSize: [20, 20],
    iconAnchor: [10, 10],
  })
}

function syncOrigin(): void {
  if (!map) return
  const origin = props.origin
  if (!origin) {
    originMarker?.remove()
    originMarker = null
    return
  }
  const at: L.LatLngTuple = [origin.lat, origin.lng]
  if (originMarker) {
    originMarker.setLatLng(at)
  } else {
    originMarker = L.marker(at, {
      icon: originIcon(),
      draggable: true,
      keyboard: false,
      pmIgnore: true,
      zIndexOffset: 1000,
    })
      .bindTooltip('Restaurant: drag to move', { direction: 'top', offset: [0, -12] })
      .on('dragend', () => {
        const p = originMarker!.getLatLng()
        emit('update:origin', { lat: round(p.lat), lng: round(p.lng) })
      })
      .addTo(map)
  }
  // Typed coordinates may point anywhere: follow the pin when it leaves the visible area.
  if (!map.getBounds().contains(at)) map.panTo(at)
}

/** Frames the zones and the restaurant once, when the editor opens. */
function fitInitialView(): void {
  if (!map) return
  const bounds = L.latLngBounds([])
  for (const layer of layers.values()) bounds.extend(layer.getBounds())
  if (props.origin) bounds.extend([props.origin.lat, props.origin.lng])
  if (layers.size > 0 && bounds.isValid()) map.fitBounds(bounds, { padding: [24, 24], maxZoom: 16 })
  else if (props.origin) map.setView([props.origin.lat, props.origin.lng], ORIGIN_ZOOM)
  else map.setView(DEFAULT_CENTER, DEFAULT_ZOOM)
}

onMounted(() => {
  if (!mapElement.value) return
  map = L.map(mapElement.value, { scrollWheelZoom: false }).setView(DEFAULT_CENTER, DEFAULT_ZOOM)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap contributors',
  }).addTo(map)

  map.pm.setGlobalOptions({ allowSelfIntersection: false, snappable: true })
  map.pm.addControls({
    position: 'topleft',
    drawPolygon: true,
    drawMarker: false,
    drawCircleMarker: false,
    drawPolyline: false,
    drawRectangle: false,
    drawCircle: false,
    drawText: false,
    editMode: true,
    dragMode: true,
    cutPolygon: false,
    removalMode: true,
    rotateMode: false,
  })

  map.on('pm:create', (e) => {
    const layer = e.layer as L.Polygon
    const key = newZoneKey()
    track(key, layer)
    emit('update:zones', [...props.zones, { key, id: '', name: nextZoneName(props.zones), boundary: boundaryOf(layer) }])
    emit('update:selectedKey', key)
  })

  map.on('pm:remove', (e) => {
    const key = keyOf(e.layer)
    if (!key) return
    layers.delete(key)
    appliedClass.delete(key)
    emit('update:zones', props.zones.filter((z) => z.key !== key))
    if (props.selectedKey === key) emit('update:selectedKey', null)
  })

  map.on('click', (e: L.LeafletMouseEvent) => {
    if (!props.placingOrigin) return
    emit('update:origin', { lat: round(e.latlng.lat), lng: round(e.latlng.lng) })
    emit('update:placingOrigin', false)
  })

  syncZones()
  syncOrigin()
  fitInitialView()
})

watch(() => [props.zones, props.selectedKey], syncZones, { deep: true })
watch(() => props.origin, syncOrigin, { deep: true })

// Leaflet measures its container once; tell it whenever the layout changes its size.
useResizeObserver(mapElement, () => map?.invalidateSize())

onBeforeUnmount(() => {
  map?.remove()
  map = null
  originMarker = null
  layers.clear()
  appliedClass.clear()
})
</script>

<template>
  <!-- `isolate` keeps Leaflet's high z-indexes below the app's dialogs and menus. While placing the
       restaurant, the crosshair cursor shows that the next click sets the position. -->
  <div
    ref="mapElement"
    :class="[
      'isolate h-[28rem] w-full overflow-hidden rounded-xl border md:h-[36rem]',
      placingOrigin && '[&_.leaflet-container]:cursor-crosshair [&_.leaflet-interactive]:cursor-crosshair',
    ]"
    role="region"
    aria-label="Map of the restaurant position and its delivery zones"
  ></div>
</template>
