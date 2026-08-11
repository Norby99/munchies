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

import { http, unwrap } from '@/api/client'
import type { Restaurant } from '@/types'

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

export async function listMyRestaurants(): Promise<Restaurant[]> {
  return unwrap(http.request({ method: 'get', url: BASE, data: {} }))
}

export async function updateRestaurant(restaurantId: string, input: RestaurantInput): Promise<string> {
  return unwrap(http.put(`${BASE}${restaurantId}/`, { managerId: '', ...input }))
}

export async function deleteRestaurant(restaurantId: string): Promise<string> {
  return unwrap(http.delete(`${BASE}${restaurantId}/`))
}
