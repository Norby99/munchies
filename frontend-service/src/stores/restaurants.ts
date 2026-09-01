// Manager's own restaurant list — full CRUD, keyed by nothing but the
// manager's session id.

import { defineStore } from 'pinia'
import { ref } from 'vue'

import { ApiError } from '@/api/client'
import * as restaurantsApi from '@/api/restaurants'
import type { RestaurantInput } from '@/api/restaurants'
import type { LoadStatus, Restaurant } from '@/types'

export const useRestaurantsStore = defineStore('restaurants', () => {
  const items = ref<Restaurant[]>([])
  const activeId = ref<string | null>(null)
  const status = ref<LoadStatus>('ready')
  const errorMessage = ref<string | null>(null)

  async function fetchMine(): Promise<void> {
    status.value = 'loading'
    errorMessage.value = null
    try {
      items.value = await restaurantsApi.listMyRestaurants()
      status.value = items.value.length === 0 ? 'empty' : 'ready'
    } catch (err) {
      errorMessage.value = err instanceof ApiError ? err.message : 'Something went wrong.'
      status.value = 'error'
    }
  }

  async function create(input: RestaurantInput): Promise<string> {
    const id = await restaurantsApi.createRestaurant(input)
    items.value.push({ id, ...input })
    status.value = 'ready'
    return id
  }

  async function update(restaurantId: string, input: RestaurantInput): Promise<void> {
    await restaurantsApi.updateRestaurant(restaurantId, input)
    const index = items.value.findIndex((r) => r.id === restaurantId)
    if (index !== -1) items.value[index] = { id: restaurantId, ...input }
  }

  async function remove(restaurantId: string): Promise<void> {
    await restaurantsApi.deleteRestaurant(restaurantId)
    items.value = items.value.filter((r) => r.id !== restaurantId)
    if (items.value.length === 0) status.value = 'empty'
  }

  function setActive(restaurantId: string): void {
    activeId.value = restaurantId
  }

  // Caches a restaurant fetched by id (customer detail screen) alongside the
  // manager's own list, so AppNav can resolve a name without re-fetching.
  function cacheOne(restaurant: Restaurant): void {
    const index = items.value.findIndex((r) => r.id === restaurant.id)
    if (index !== -1) items.value[index] = restaurant
    else items.value.push(restaurant)
  }

  // Wipes this manager's data on logout — otherwise the next signed-in
  // account (even on the same browser tab) would see the previous manager's
  // restaurants until a fetch happens to overwrite them.
  function reset(): void {
    items.value = []
    activeId.value = null
    status.value = 'ready'
    errorMessage.value = null
  }

  return {
    items,
    activeId,
    status,
    errorMessage,
    fetchMine,
    create,
    update,
    remove,
    setActive,
    cacheOne,
    reset,
  }
})
