// Helpers for editing delivery zones on the manager's Delivery page (ManagerDeliveryView): the
// draft shape the map and the zone list share, colours, default names and the checks run before
// saving. The same rules are enforced by restaurant-service (DeliveryZone.kt, Restaurant.kt); they
// are repeated here only to explain a problem next to the field instead of after a round trip.

import type { DeliveryZone, GeoLocation } from '@/api/restaurants'

/** A zone being edited. `key` identifies it in the UI, also before the server has given it an id. */
export interface ZoneDraft {
  key: string
  /** Empty until the zone is first saved. */
  id: string
  name: string
  boundary: GeoLocation[]
}

// Mirrors the limits of restaurant-service.
export const MAX_ZONE_NAME_LENGTH = 60
export const MAX_ZONES = 50

// Zone colours come from the theme's categorical chart tokens, so they follow the light and dark
// themes. `path` styles the SVG polygon Leaflet draws (CSS classes override its presentation
// attributes), `swatch` the matching dot in the zone list. Written out in full so Tailwind finds
// the classes. With more zones than tokens the colours repeat: zones are also named in the list and
// in the map tooltip, so colour is never the only cue.
const ZONE_STYLES = [
  { path: 'stroke-chart-1 fill-chart-1', swatch: 'bg-chart-1' },
  { path: 'stroke-chart-3 fill-chart-3', swatch: 'bg-chart-3' },
  { path: 'stroke-chart-2 fill-chart-2', swatch: 'bg-chart-2' },
  { path: 'stroke-chart-4 fill-chart-4', swatch: 'bg-chart-4' },
  { path: 'stroke-chart-5 fill-chart-5', swatch: 'bg-chart-5' },
] as const

export function zoneStyle(index: number): (typeof ZONE_STYLES)[number] {
  return ZONE_STYLES[index % ZONE_STYLES.length]
}

let keySeed = 0
export function newZoneKey(): string {
  keySeed += 1
  return `zone-${Date.now()}-${keySeed}`
}

/** "Zone N" with the smallest N not already taken (ignoring case). */
export function nextZoneName(zones: readonly ZoneDraft[]): string {
  const taken = new Set(zones.map((z) => z.name.trim().toLowerCase()))
  let n = zones.length + 1
  for (let i = 1; i <= zones.length + 1; i++) {
    if (!taken.has(`zone ${i}`)) {
      n = i
      break
    }
  }
  return `Zone ${n}`
}

export function draftsFrom(zones: readonly DeliveryZone[]): ZoneDraft[] {
  return zones.map((z) => ({ key: z.id || newZoneKey(), id: z.id, name: z.name, boundary: z.boundary.map((p) => ({ ...p })) }))
}

export function toWire(drafts: readonly ZoneDraft[]): DeliveryZone[] {
  return drafts.map((d) => ({ id: d.id, name: d.name.trim(), boundary: d.boundary.map((p) => ({ lat: p.lat, lng: p.lng })) }))
}

/** The first problem that would make restaurant-service refuse the zones, or null. */
export function zonesProblem(drafts: readonly ZoneDraft[]): string | null {
  if (drafts.length > MAX_ZONES) return `A restaurant can have at most ${MAX_ZONES} zones.`
  const seen = new Set<string>()
  for (const zone of drafts) {
    const name = zone.name.trim()
    if (!name) return 'Every zone needs a name.'
    if (name.length > MAX_ZONE_NAME_LENGTH) return `Zone names can be at most ${MAX_ZONE_NAME_LENGTH} characters.`
    if (seen.has(name.toLowerCase())) return `Two zones are named "${name}": give each zone its own name.`
    seen.add(name.toLowerCase())
    const distinct = new Set(zone.boundary.map((p) => `${p.lat},${p.lng}`))
    if (distinct.size < 3) return `Zone "${name}" needs at least three corners.`
  }
  return null
}
