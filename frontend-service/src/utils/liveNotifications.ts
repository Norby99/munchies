// What a live notification means for the person who receives it: the toast to show, if any.
//
// Toasts are only for what the user did not do themselves and would want to know while looking at
// another screen:
// - a customer hears about their order moving on (preparing, ready, on its way, completed), not
//   about placing, paying or discarding it (they just did) nor about their own bookings;
// - a manager hears about new and paid orders, cancellations and table bookings, not about the
//   status changes they (or the kitchen timer) made on the order board.
// Every notification still refreshes the screens showing that data (composables/useLiveUpdates).

import type { RouteLocationRaw } from 'vue-router'

import { formatDay, formatTimeRange } from '@/utils/reservations'
import type { LiveNotification, OrderStatus, Role } from '@/types'

export interface LiveToast {
  title: string
  body: string
  to: RouteLocationRaw
  linkLabel: string
}

const CUSTOMER_ORDER_TITLES: Partial<Record<OrderStatus, string>> = {
  PREPARING: 'Your order is being prepared',
  READY: 'Your order is ready',
  ON_THE_WAY: 'Your order is on its way',
  COMPLETED: 'Your order is completed',
}

function guests(n: number): string {
  return `${n} ${n === 1 ? 'guest' : 'guests'}`
}

/**
 * @param restaurantName Name of a restaurant by id, when known (shown in the text).
 */
export function toastFor(
  notification: LiveNotification,
  role: Role | null,
  restaurantName: (id: string) => string | undefined,
): LiveToast | null {
  const name = restaurantName(notification.restaurantId)
  const at = name ? ` at ${name}` : ''

  if (role === 'CUSTOMER') {
    if (notification.type !== 'ORDER_CHANGED') return null
    const title = CUSTOMER_ORDER_TITLES[notification.status]
    if (!title) return null
    return {
      title,
      body: name ? `Your order from ${name}.` : 'Open it to follow its progress.',
      to: { name: 'order-detail', params: { orderId: notification.orderId } },
      linkLabel: 'View order',
    }
  }

  if (role === 'MANAGER') {
    if (notification.type === 'ORDER_CHANGED') {
      const board = {
        to: { name: 'manager-orders', query: { restaurant: notification.restaurantId } },
        linkLabel: 'Open the board',
      }
      if (notification.status === 'PENDING' && !notification.payed)
        return { title: 'New order', body: `A new order${at}, waiting for payment.`, ...board }
      if (notification.status === 'PENDING' && notification.payed)
        return { title: 'Order paid', body: `An order${at} was paid: it can be prepared.`, ...board }
      if (notification.status === 'CANCELLED')
        return { title: 'Order cancelled', body: `The customer cancelled an order${at}.`, ...board }
      return null
    }

    const when = `${formatDay(notification.start)}, ${formatTimeRange(notification)}`
    const details = `${guests(notification.numberOfGuests)}, ${when}, table ${notification.tableNumber}${at}.`
    const titles = { ACCEPTED: 'New table booking', MODIFIED: 'Booking changed', CANCELED: 'Booking canceled' }
    return {
      title: titles[notification.change],
      body: details,
      to: { name: 'manager-reservations', query: { restaurant: notification.restaurantId } },
      linkLabel: 'See reservations',
    }
  }

  return null
}
