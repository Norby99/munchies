// Order lifecycle knowledge shared by the customer and manager screens. The chains mirror the
// order-service domain (DeliveryOrder / TakeawayOrder / DineInOrder advance one step at a time);
// the frontend never decides a transition, it only shows where an order is and what comes next.

import type { Order, OrderStatus, OrderType } from '@/types'

export const STATUS_CHAINS: Record<OrderType, OrderStatus[]> = {
  DELIVERY: ['PENDING', 'PREPARING', 'READY', 'ON_THE_WAY', 'COMPLETED'],
  TAKEAWAY: ['PENDING', 'PREPARING', 'READY', 'COMPLETED'],
  DINE_IN: ['PENDING', 'PREPARING', 'READY', 'COMPLETED'],
}

export const STATUS_LABELS: Record<OrderStatus, string> = {
  PENDING: 'Received',
  PREPARING: 'Preparing',
  READY: 'Ready',
  ON_THE_WAY: 'On the way',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
}

export const TYPE_LABELS: Record<OrderType, string> = {
  DELIVERY: 'Delivery',
  TAKEAWAY: 'Takeaway',
  DINE_IN: 'Dine-in',
}

// The status the order would move to on the next advance, or null when it is final.
export function nextStatus(order: Order): OrderStatus | null {
  const chain = STATUS_CHAINS[order.orderType]
  const index = chain.indexOf(order.status)
  return index >= 0 && index < chain.length - 1 ? chain[index + 1] : null
}

export function isActive(order: Order): boolean {
  return order.status !== 'COMPLETED' && order.status !== 'CANCELLED'
}

// The time the customer expects the order (delivery or pickup), as epoch ms. Orders carry no
// creation timestamp, so this is also the sort key of every order list.
export function expectedTime(order: Order): number {
  const raw = order.estimatedDeliveryTime ?? order.pickupTime
  const ms = raw ? Number(raw) : Number.NaN
  return Number.isNaN(ms) ? Number.MAX_SAFE_INTEGER : ms
}

// Visual tone of each status, mapped to the theme's status colors by OrderStatusBadge.
export type StatusTone = 'warning' | 'info' | 'primary' | 'success' | 'muted'

export const STATUS_TONES: Record<OrderStatus, StatusTone> = {
  PENDING: 'warning',
  PREPARING: 'info',
  READY: 'primary',
  ON_THE_WAY: 'info',
  COMPLETED: 'success',
  CANCELLED: 'muted',
}
