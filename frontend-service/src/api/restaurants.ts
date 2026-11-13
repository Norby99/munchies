// Restaurant CRUD. Routes are plural (`/restaurants/...`), unlike the menu
// family below — a real inconsistency in the API, kept as-is rather than
// "fixed" client-side (see ui_specification.md §7.1).
//
// managerId is always injected server-side from the session cookie
// (CreateRestaurantRequest.addId / UpdateRestaurantRequest.addId /
// DeleteRestaurantRequest.addId / GetManagerRestaurantsRequest.addId, all
// applied in gateway-service via req.user!!.id) — the client never sends its
// own manager id, even though the request DTOs carry the field. Listing is a
// GET with a body (`{}`), same trick already used by verify-email/ — Express
// only parses a body via express.raw() when Content-Type + a body are both
// present, and the internal gateway->restaurant-service axios client only
// forwards a body on methods it's told to (see internal-client.ts).

import { ApiError, http, unwrap } from '@/api/client'
import type { DiningTable, Restaurant } from '@/types'

const BASE = '/restaurants/'

export interface RestaurantInput {
  name: string
  address: string
  phone: string
  email: string
}

export async function createRestaurant(input: RestaurantInput): Promise<string> {
  return unwrap(http.post(BASE, { managerId: '', ...input }))
}

export async function getRestaurant(restaurantId: string): Promise<Restaurant> {
  return unwrap(http.get(`${BASE}${restaurantId}/`))
}

// Customer search by name or address, matched ignoring case; an empty query lists every
// restaurant. Sorted by name and capped server-side at SEARCH_LIMIT results.
export const SEARCH_LIMIT = 50

export async function searchRestaurants(query: string): Promise<Restaurant[]> {
  return unwrap(http.get(BASE, { params: { q: query } }))
}

export async function listMyRestaurants(): Promise<Restaurant[]> {
  return unwrap(http.request({ method: 'get', url: `${BASE}manager/`, data: {} }))
}

export async function updateRestaurant(restaurantId: string, input: RestaurantInput): Promise<string> {
  return unwrap(http.put(`${BASE}${restaurantId}/`, { managerId: '', ...input }))
}

export async function deleteRestaurant(restaurantId: string): Promise<string> {
  return unwrap(http.delete(`${BASE}${restaurantId}/`))
}

// Scheduling settings read by scheduler-service. Both answer 404 while the restaurant has not set
// them: that is "not configured" (null) for the UI, not an error. Deliveries are offered only once
// both are set.
//
// Customers may read the capacity policy too (its opening/closing are the restaurant's hours);
// only managers may change either setting.

export interface CapacityPolicy {
  slotMinutes: number
  maxCapacityUnits: number
  /** Restaurant-local "HH:MM". */
  opening: string
  closing: string
  prepLeadMinutes: number
  maxAdvanceDays: number
  maxTripsPerSlot: number
  maxTripKmBudget: number
  maxDetourKm: number
}

export interface GeoLocation {
  lat: number
  lng: number
}

async function orNullOn404<T>(call: Promise<T>): Promise<T | null> {
  try {
    return await call
  } catch (err) {
    if (err instanceof ApiError && err.code === 404) return null
    throw err
  }
}

export async function getCapacityPolicy(restaurantId: string): Promise<CapacityPolicy | null> {
  return orNullOn404(unwrap(http.get(`${BASE}${restaurantId}/capacity-policy/`)))
}

export async function updateCapacityPolicy(restaurantId: string, policy: CapacityPolicy): Promise<CapacityPolicy> {
  return unwrap(http.put(`${BASE}${restaurantId}/capacity-policy/`, { managerId: '', ...policy }))
}

export async function getLocation(restaurantId: string): Promise<GeoLocation | null> {
  return orNullOn404(unwrap(http.get(`${BASE}${restaurantId}/location/`)))
}

export async function updateLocation(restaurantId: string, location: GeoLocation): Promise<GeoLocation> {
  return unwrap(http.put(`${BASE}${restaurantId}/location/`, { managerId: '', ...location }))
}

// The dining room tables, used by table-reservation-service to seat each party. Anyone signed in
// may read them (an empty list means the restaurant takes no reservations); only the restaurant's
// manager may replace them, always as a whole layout.
export async function getTables(restaurantId: string): Promise<DiningTable[]> {
  return unwrap(http.get(`${BASE}${restaurantId}/tables/`))
}

export async function updateTables(restaurantId: string, tables: DiningTable[]): Promise<DiningTable[]> {
  return unwrap(http.put(`${BASE}${restaurantId}/tables/`, { managerId: '', tables }))
}
