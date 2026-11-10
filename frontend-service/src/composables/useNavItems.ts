// Shared nav-item list for AppNav (desktop) and MobileTabBar (mobile) — one
// shell, nav changes by role, so both components read the same source
// instead of duplicating the role branching.

import { computed } from 'vue'
import { useRoute, type RouteLocationRaw } from 'vue-router'

import { useCartStore } from '@/stores/cart'
import { useRestaurantsStore } from '@/stores/restaurants'
import { useSessionStore } from '@/stores/session'

export interface NavItem {
  label: string
  to: RouteLocationRaw
  disabled?: boolean
}

export function useNavItems() {
  const session = useSessionStore()
  const restaurants = useRestaurantsStore()
  const cart = useCartStore()
  const route = useRoute()

  return computed<NavItem[]>(() => {
    if (session.role === 'MANAGER') {
      const activeRestaurantId = (route.params.restaurantId as string | undefined) ?? restaurants.activeId

      return [
        { label: 'Restaurants', to: { name: 'manager-restaurants' } },
        {
          label: 'Orders',
          to: activeRestaurantId
            ? { name: 'manager-orders', query: { restaurant: activeRestaurantId } }
            : { name: 'manager-orders' },
        },
        {
          label: 'Menu builder',
          to: activeRestaurantId
            ? { name: 'menu-builder', params: { restaurantId: activeRestaurantId } }
            : { name: 'manager-restaurants' },
          disabled: !activeRestaurantId,
        },
        { label: 'Account', to: { name: 'account' } },
      ]
    }

    const restaurantId = (route.params.restaurantId as string | undefined) ?? cart.restaurantId ?? undefined
    const restaurantName = restaurantId
      ? restaurants.items.find((r) => r.id === restaurantId)?.name
      : undefined

    return [
      restaurantId
        ? { label: restaurantName ?? 'Restaurant', to: { name: 'restaurant-detail', params: { restaurantId } } }
        : { label: 'Restaurant', to: { name: 'account' }, disabled: true },
      { label: 'My orders', to: { name: 'my-orders' } },
      { label: 'Account', to: { name: 'account' } },
    ]
  })
}
