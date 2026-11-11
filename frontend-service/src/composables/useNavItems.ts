// Shared nav-item list for AppNav (desktop) and MobileTabBar (mobile): one shell, nav changes by
// role, so both components read the same source instead of duplicating the role branching.

import { computed, type Component } from 'vue'
import { useRoute, type RouteLocationRaw } from 'vue-router'
import { ClipboardListIcon, ReceiptTextIcon, SquareMenuIcon, StoreIcon, UserRoundIcon, UtensilsIcon } from '@lucide/vue'

import { useCartStore } from '@/stores/cart'
import { useRestaurantsStore } from '@/stores/restaurants'
import { useSessionStore } from '@/stores/session'

export interface NavItem {
  label: string
  icon: Component
  to: RouteLocationRaw
  disabled?: boolean
  // Route names that should highlight this item, besides the one it links to.
  matches: string[]
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
        {
          label: 'Restaurants',
          icon: StoreIcon,
          to: { name: 'manager-restaurants' },
          matches: ['manager-restaurants'],
        },
        {
          label: 'Orders',
          icon: ClipboardListIcon,
          to: activeRestaurantId
            ? { name: 'manager-orders', query: { restaurant: activeRestaurantId } }
            : { name: 'manager-orders' },
          matches: ['manager-orders'],
        },
        {
          label: 'Menus',
          icon: SquareMenuIcon,
          to: activeRestaurantId
            ? { name: 'menu-builder', params: { restaurantId: activeRestaurantId } }
            : { name: 'manager-restaurants' },
          disabled: !activeRestaurantId,
          matches: ['menu-builder'],
        },
        { label: 'Account', icon: UserRoundIcon, to: { name: 'account' }, matches: ['account'] },
      ]
    }

    const restaurantId = (route.params.restaurantId as string | undefined) ?? cart.restaurantId ?? undefined
    const restaurantName = restaurantId
      ? restaurants.items.find((r) => r.id === restaurantId)?.name
      : undefined

    return [
      restaurantId
        ? {
            label: restaurantName ?? 'Restaurant',
            icon: UtensilsIcon,
            to: { name: 'restaurant-detail', params: { restaurantId } },
            matches: ['restaurant-detail', 'checkout'],
          }
        : { label: 'Restaurant', icon: UtensilsIcon, to: { name: 'account' }, disabled: true, matches: [] },
      { label: 'My orders', icon: ReceiptTextIcon, to: { name: 'my-orders' }, matches: ['my-orders', 'order-detail'] },
      { label: 'Account', icon: UserRoundIcon, to: { name: 'account' }, matches: ['account'] },
    ]
  })
}
