// Order placement and lifecycle. customerId is always injected server-side
// from the session cookie (PlaceOrderRequest.addId etc. in order-shared) —
// the client never sends its own id, even though the request DTOs carry the
// field.
//
// Order history: the gateway declares a GET /orders/ list route, but in the current
// order-shared contract it resolves to the same path as GET /orders/{id} and is shadowed by it,
// so the frontend tracks the ids of the orders it placed itself (see stores/orders.ts).
//
// Times (estimatedDeliveryTime, pickupTime) travel as epoch-milliseconds strings: order-service
// parses them with toLongOrNull() and rejects anything else.

import { http, unwrap } from '@/api/client'
import type { Order, OrderLineItem, OrderType } from '@/types'

const BASE = '/orders/'

interface PlaceOrderBase {
  restaurantId: string
  items: OrderLineItem[]
}

export interface PlaceDeliveryOrderInput extends PlaceOrderBase {
  orderType: 'DELIVERY'
  estimatedDeliveryTime: string
  deliveryAddress: string
  bellName: string
  customerPhone: string
}

export interface PlaceTakeawayOrderInput extends PlaceOrderBase {
  orderType: 'TAKEAWAY'
  pickupTime: string
  customerName: string
}

export interface PlaceDineInOrderInput extends PlaceOrderBase {
  orderType: 'DINE_IN'
  tableNumber: number
  numberOfGuests: number
}

export type PlaceOrderInput = PlaceDeliveryOrderInput | PlaceTakeawayOrderInput | PlaceDineInOrderInput

export async function placeOrder(input: PlaceOrderInput): Promise<Order> {
  return unwrap(http.post(BASE + 'place', { customerId: '', ...input }))
}

export async function getOrder(orderId: string): Promise<Order> {
  return unwrap(http.get(`${BASE}${orderId}`))
}

// Moves one step forward in the order type's status chain
// (see ui_specification.md §6.4 for the per-type chain).
export async function advanceOrderStatus(orderId: string): Promise<string> {
  return unwrap(http.post(BASE + 'advance', { orderId }))
}

// Only works while status === PENDING. Deletes the record outright — a
// cancelled order 404s on subsequent GET rather than surfacing CANCELLED.
export async function cancelOrder(orderId: string): Promise<string> {
  return unwrap(http.delete(`${BASE}${orderId}`))
}

export async function updateOrderItems(orderId: string, items: OrderLineItem[]): Promise<string> {
  return unwrap(http.patch(BASE + 'items', { orderId, customerId: '', items }))
}

export interface UpdateDeliveryInfoInput {
  estimatedDeliveryTime: string
  deliveryAddress: string
  bellName: string
  customerPhone: string
}

export async function updateDeliveryInfo(
  orderId: string,
  info: UpdateDeliveryInfoInput,
): Promise<string> {
  return unwrap(http.patch(BASE + 'delivery', { orderId, customerId: '', ...info }))
}

export interface UpdateTakeawayInfoInput {
  pickupTime: string
  customerName: string
}

export async function updateTakeawayInfo(
  orderId: string,
  info: UpdateTakeawayInfoInput,
): Promise<string> {
  return unwrap(http.patch(BASE + 'takeaway', { orderId, customerId: '', ...info }))
}

export type { OrderType }
