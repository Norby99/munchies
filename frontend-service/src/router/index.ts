// Route table + the global auth guard. Auth screens render without the app
// shell (see App.vue); everything else requires an active session, and
// `/manager/*` additionally requires role === MANAGER (the backend enforces
// this regardless — the guard just avoids offering entry points a customer
// role would be rejected from).

import { createRouter, createWebHistory, type RouteLocationRaw } from 'vue-router'

import { useSessionStore } from '@/stores/session'
import { useCartStore } from '@/stores/cart'
import type { Role } from '@/types'

declare module 'vue-router' {
  interface RouteMeta {
    public?: boolean
    role?: Role
  }
}

const routes = [
  {
    path: '/',
    redirect: '/login',
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { public: true },
  },
  {
    path: '/verify',
    name: 'verify',
    component: () => import('@/views/VerifyEmailView.vue'),
    meta: { public: true },
  },
  {
    path: '/manager/restaurants',
    name: 'manager-restaurants',
    component: () => import('@/views/ManagerRestaurantsView.vue'),
    meta: { role: 'MANAGER' as Role },
  },
  {
    path: '/manager/restaurants/:restaurantId/menus',
    name: 'menu-builder',
    component: () => import('@/views/MenuBuilderView.vue'),
    meta: { role: 'MANAGER' as Role },
    props: true,
  },
  {
    path: '/r/:restaurantId',
    name: 'restaurant-detail',
    component: () => import('@/views/RestaurantDetailView.vue'),
    props: true,
  },
  {
    path: '/account',
    name: 'account',
    component: () => import('@/views/AccountView.vue'),
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})

export function homeFor(role: Role | null): RouteLocationRaw {
  if (role === 'MANAGER') return { name: 'manager-restaurants' }

  const cart = useCartStore()
  if (cart.restaurantId) return { name: 'restaurant-detail', params: { restaurantId: cart.restaurantId } }
  return { name: 'account' }
}

let hydrationAttempted = false

router.beforeEach(async (to) => {
  const session = useSessionStore()

  if (to.meta.public) {
    return session.isAuthenticated ? homeFor(session.role) : true
  }

  if (!session.isAuthenticated && !hydrationAttempted) {
    hydrationAttempted = true
    await session.hydrate()
  }

  if (!session.isAuthenticated) {
    session.returnTo = to.fullPath
    return { name: 'login' }
  }

  if (to.meta.role && session.role !== to.meta.role) {
    return homeFor(session.role)
  }

  return true
})
