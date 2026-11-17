// Loads a restaurant together with every one of its menus, and exposes a lookup from menu item id
// to item. Orders only carry menuItemId + quantity, so both the checkout (cart review) and the
// order detail (line names and prices) need this join; prices are never stored on the order.

import { computed, ref } from 'vue'

import * as restaurantsApi from '@/api/restaurants'
import { useMenusStore } from '@/stores/menus'
import { useRestaurantsStore } from '@/stores/restaurants'
import type { Menu, MenuItem, Restaurant } from '@/types'

export function useRestaurantCatalog() {
  const menusStore = useMenusStore()
  const restaurantsStore = useRestaurantsStore()

  const restaurant = ref<Restaurant | null>(null)
  const menus = ref<Menu[]>([])

  const itemsById = computed(() => {
    const index = new Map<string, MenuItem>()
    for (const menu of menus.value) {
      for (const category of menu.categories) {
        for (const item of category.items) index.set(item.id, item)
      }
    }
    return index
  })

  // A synthetic menu holding every category of every menu: the cart store computes lines and
  // totals against a single Menu, while a cart can hold items coming from different menus.
  const mergedMenu = computed<Menu>(() => ({
    id: 'merged',
    name: restaurant.value?.name ?? '',
    categories: menus.value.flatMap((menu) => menu.categories),
    validity: [],
  }))

  async function load(restaurantId: string): Promise<void> {
    const loaded = await restaurantsApi.getRestaurant(restaurantId)
    restaurant.value = loaded
    restaurantsStore.cacheOne(loaded)

    await menusStore.fetchSummaries(restaurantId)
    const summaries = menusStore.summaries[restaurantId] ?? []
    menus.value = await Promise.all(summaries.map((s) => menusStore.fetchFull(restaurantId, s.id)))
  }

  return { restaurant, menus, itemsById, mergedMenu, load }
}
