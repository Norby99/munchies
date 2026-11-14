// Shared nav-item list for AppNav (desktop) and MobileTabBar (mobile): one shell, nav changes by
// role, so both components read the same source instead of duplicating the role branching.

import { computed, type Component } from 'vue'
import { useRoute, type RouteLocationRaw } from 'vue-router'
import {
  CalendarDaysIcon,
  ClipboardListIcon,
  ReceiptTextIcon,
  SearchIcon,
  SquareMenuIcon,
  StoreIcon,
  UserRoundIcon,
} from '@lucide/vue'

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
          label: 'Reservations',
          icon: CalendarDaysIcon,
          to: activeRestaurantId
            ? { name: 'manager-reservations', query: { restaurant: activeRestaurantId } }
            : { name: 'manager-reservations' },
          matches: ['manager-reservations'],
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

    // No entry for a single restaurant: a customer owns none, and an item named after one would read
    // as "my restaurant". Browsing a restaurant and its checkout live under "Restaurants"; the way
    // back to the bag's restaurant is the "Your bag" badge in the search results.
    return [
      {
        label: 'Restaurants',
        icon: SearchIcon,
        to: { name: 'explore' },
        matches: ['explore', 'restaurant-detail', 'checkout'],
      },
      { label: 'My orders', icon: ReceiptTextIcon, to: { name: 'my-orders' }, matches: ['my-orders', 'order-detail'] },
      {
        label: 'Reservations',
        icon: CalendarDaysIcon,
        to: { name: 'my-reservations' },
        matches: ['my-reservations'],
      },
      { label: 'Account', icon: UserRoundIcon, to: { name: 'account' }, matches: ['account'] },
    ]
  })
}
