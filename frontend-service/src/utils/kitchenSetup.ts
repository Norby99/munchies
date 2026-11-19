// Helpers for the manager's Kitchen page (ManagerKitchenView): the form's shape, the example
// shown before the kitchen is described, the order of the stages, and the checks run before
// saving. The same rules are enforced by restaurant-service (KitchenSetup.kt); they are repeated
// here only to explain a problem next to the form instead of after a round trip.

import type { CookStation, KitchenRoute, KitchenSetup } from '@/api/restaurants'

// Mirrors the limits of restaurant-service.
export const MAX_STATION_NAME_LENGTH = 60
export const MAX_PREP_WORKERS = 50
export const MAX_COOK_STATIONS = 20
export const MAX_STATION_PLACES = 100
export const MAX_STAGE_MINUTES = 240

/**
 * Shown, and saved if the manager accepts it as is, while the kitchen is not described: a small
 * pizzeria (the case the capacity model was built and tested on).
 */
export const EXAMPLE_KITCHEN: KitchenSetup = {
  route: 'PREP_THEN_COOK',
  prepWorkers: 2,
  cookStations: [{ name: 'Oven', places: 7 }],
  prepMinutes: 3,
  loadingMinutes: 0.5,
  cookMinutes: 5,
}

/** A station being edited. `key` identifies its row; number fields are '' while empty. */
export interface StationDraft {
  key: string
  name: string
  places: number | ''
}

/** The form: number fields are '' while empty (what `v-model.number` gives for a blank input). */
export interface KitchenForm {
  route: KitchenRoute
  prepWorkers: number | ''
  stations: StationDraft[]
  prepMinutes: number | ''
  loadingMinutes: number | ''
  cookMinutes: number | ''
}

export type Stage = 'prep' | 'loading' | 'cook'

/** The stages one item goes through, in order. Loading always comes right before cooking. */
export function stagesOf(route: KitchenRoute): Stage[] {
  return route === 'PREP_THEN_COOK' ? ['prep', 'loading', 'cook'] : ['loading', 'cook', 'prep']
}

let keySeed = 0
export function newStationKey(): string {
  keySeed += 1
  return `station-${Date.now()}-${keySeed}`
}

/** "Station N" with the smallest N not already taken (ignoring case). */
export function nextStationName(stations: readonly StationDraft[]): string {
  const taken = new Set(stations.map((s) => s.name.trim().toLowerCase()))
  let n = 1
  while (taken.has(`station ${n}`)) n += 1
  return `Station ${n}`
}

export function formFrom(setup: KitchenSetup): KitchenForm {
  return {
    route: setup.route,
    prepWorkers: setup.prepWorkers,
    stations: setup.cookStations.map((s) => ({ key: newStationKey(), name: s.name, places: s.places })),
    prepMinutes: setup.prepMinutes,
    loadingMinutes: setup.loadingMinutes,
    cookMinutes: setup.cookMinutes,
  }
}

const isWhole = (n: number | '', min: number, max: number): n is number =>
  typeof n === 'number' && Number.isInteger(n) && n >= min && n <= max

const isStageTime = (n: number | ''): n is number =>
  typeof n === 'number' && Number.isFinite(n) && n > 0 && n <= MAX_STAGE_MINUTES

/** The first problem that would make restaurant-service refuse the kitchen, or null. */
export function kitchenProblem(form: KitchenForm): string | null {
  if (!isWhole(form.prepWorkers, 1, MAX_PREP_WORKERS))
    return `People preparing must be a whole number from 1 to ${MAX_PREP_WORKERS}.`
  if (form.stations.length === 0) return 'Add at least one cook station: an oven, a grill...'
  if (form.stations.length > MAX_COOK_STATIONS) return `A kitchen can have at most ${MAX_COOK_STATIONS} cook stations.`
  const seen = new Set<string>()
  for (const station of form.stations) {
    const name = station.name.trim()
    if (!name) return 'Every cook station needs a name.'
    if (name.length > MAX_STATION_NAME_LENGTH)
      return `Station names can be at most ${MAX_STATION_NAME_LENGTH} characters.`
    if (seen.has(name.toLowerCase())) return `Two stations are named "${name}": give each its own name.`
    seen.add(name.toLowerCase())
    if (!isWhole(station.places, 1, MAX_STATION_PLACES))
      return `"${name}" must hold a whole number of items, from 1 to ${MAX_STATION_PLACES}.`
  }
  const times: [number | '', string][] = [
    [form.prepMinutes, 'Preparing'],
    [form.loadingMinutes, 'Loading'],
    [form.cookMinutes, 'Cooking'],
  ]
  for (const [minutes, label] of times) {
    if (!isStageTime(minutes)) return `${label} time must be more than 0 and at most ${MAX_STAGE_MINUTES} minutes.`
  }
  return null
}

/** The wire form of a form that passed [kitchenProblem]. */
export function toWire(form: KitchenForm): KitchenSetup {
  return {
    route: form.route,
    prepWorkers: Number(form.prepWorkers),
    cookStations: form.stations.map((s): CookStation => ({ name: s.name.trim(), places: Number(s.places) })),
    prepMinutes: Number(form.prepMinutes),
    loadingMinutes: Number(form.loadingMinutes),
    cookMinutes: Number(form.cookMinutes),
  }
}
