// Order placement and lifecycle. customerId is always injected server-side
// from the session cookie (PlaceOrderRequest.addId etc. in order-shared) —
// the client never sends its own id, even though the request DTOs carry the
// field.
//
// Times (estimatedDeliveryTime, pickupTime) travel as epoch-milliseconds strings: order-service
// parses them with toLongOrNull() and rejects anything else.

import { ApiError, http, unwrap } from '@/api/client'
import type { Order, OrderLineItem, OrderStatus, OrderType } from '@/types'

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

export interface OrderFilters {
  restaurantId?: string
  status?: OrderStatus
}

// GET /orders/ with optional filters. For a customer the gateway always scopes the list to the
// caller (customerId comes from the session cookie), so customers pass no customer filter; a
// manager filters by restaurant. order-service answers 404 when nothing matches: that is an
// empty list for the UI, not an error.
export async function listOrders(filters: OrderFilters = {}): Promise<Order[]> {
  try {
    return await unwrap(http.get(BASE, { params: filters }))
  } catch (err) {
    if (err instanceof ApiError && err.code === 404) return []
    throw err
  }
}

export async function getOrder(orderId: string): Promise<Order> {
  return unwrap(http.get(`${BASE}${orderId}`))
}

// Moves one step forward in the order type's status chain
// (see ui_specification.md §6.4 for the per-type chain).
export async function advanceOrderStatus(orderId: string): Promise<string> {
  return unwrap(http.post(BASE + 'advance', { orderId }))
}

// Only works while status === PENDING. The order is event-sourced, so it is not deleted: it stays
// readable with status CANCELLED.
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
