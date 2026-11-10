// Route table + the global auth guard. Two independent route flags:
// - `public`: reachable without a session (login, register); a signed-in user is sent home.
// - `bare`: rendered without the app shell (see App.vue). The verify screen is bare but NOT
//   public: the gateway requires the session cookie to verify an email, and registration
//   has just created that session.
// Every non-public route requires an active session, and `/manager/*` additionally requires
// role === MANAGER (the backend enforces
// this regardless — the guard just avoids offering entry points a customer
// role would be rejected from).

import { createRouter, createWebHistory, type RouteLocationRaw } from 'vue-router'

import { useSessionStore } from '@/stores/session'
import { useCartStore } from '@/stores/cart'
import type { Role } from '@/types'

declare module 'vue-router' {
  interface RouteMeta {
    public?: boolean
    bare?: boolean
    role?: Role
  }
}

// Client-side paths must never start with an API prefix proxied to the gateway (/users,
// /restaurants, /restaurant, /orders, /payments: see vite.config.ts and nginx/default.conf.template),
// otherwise reloading such a page would be proxied to the gateway and show its JSON.
const routes = [
  {
    path: '/',
    redirect: '/login',
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true, bare: true },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { public: true, bare: true },
  },
  {
    path: '/verify',
    name: 'verify',
    component: () => import('@/views/VerifyEmailView.vue'),
    meta: { bare: true },
  },
  {
    path: '/manager/restaurants',
    name: 'manager-restaurants',
    component: () => import('@/views/ManagerRestaurantsView.vue'),
    meta: { role: 'MANAGER' as Role },
  },
  {
    path: '/manager/orders',
    name: 'manager-orders',
    component: () => import('@/views/ManagerOrdersView.vue'),
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
    path: '/r/:restaurantId/checkout',
    name: 'checkout',
    component: () => import('@/views/CheckoutView.vue'),
    meta: { role: 'CUSTOMER' as Role },
    props: true,
  },
  {
    path: '/my-orders',
    name: 'my-orders',
    component: () => import('@/views/MyOrdersView.vue'),
    meta: { role: 'CUSTOMER' as Role },
  },
  {
    path: '/my-orders/:orderId',
    name: 'order-detail',
    component: () => import('@/views/OrderDetailView.vue'),
    meta: { role: 'CUSTOMER' as Role },
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
  return { name: 'my-orders' }
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
