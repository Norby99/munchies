// scheduler-service, through the gateway's /schedule/* pass-through routes. The scheduler sits
// between the customer and order-service: it offers the time slots the kitchen can still take,
// books one, and only then places the order in order-service. Its responses use the same
// { result, code } envelope as every other service.
//
// Slot times ("HH:MM") are already in the restaurant's local time; epoch values are milliseconds.
// customerId is injected by the gateway from the session cookie on POST /schedule/{id}/orders.

import { http, unwrap } from '@/api/client'
import type { OrderLineItem, OrderType } from '@/types'

export interface GeoPoint {
  lat: number
  lng: number
}

export interface AvailableSlot {
  start: string
  end: string
  startEpochMs: number
  endEpochMs: number
  /** Kitchen units (item quantities) the slot can still take. */
  remaining: number
}

export interface Availability {
  restaurantId: string
  date: string
  orderType: OrderType | null
  /** 0 when the order type is not offered at all (e.g. delivery without location and policy). */
  slotMinutes: number
  maxCapacityUnits: number
  ranges: AvailableSlot[]
}

export interface LoadSlot {
  start: string
  end: string
  startEpochMs: number
  bookedUnits: number
  /** The admission quota of the slot: kitchen units it may take in total. */
  capacityUnits: number
  remainingUnits: number
  tripCount: number
}

export interface KitchenLoad {
  restaurantId: string
  date: string
  maxCapacityUnits: number
  maxTripsPerSlot: number
  totalBookedUnits: number
  totalCapacityUnits: number
  slots: LoadSlot[]
}

interface ScheduleOrderBase {
  items: OrderLineItem[]
  slotStartEpochMs: number
}

export interface ScheduleDeliveryInput extends ScheduleOrderBase {
  orderType: 'DELIVERY'
  deliveryAddress: string
  deliveryLocation: GeoPoint
  bellName: string
  customerPhone: string
}

export interface ScheduleTakeawayInput extends ScheduleOrderBase {
  orderType: 'TAKEAWAY'
  customerName: string
}

export type ScheduleOrderInput = ScheduleDeliveryInput | ScheduleTakeawayInput

export interface ScheduledOrder {
  bookingId: string
  orderId: string
  restaurantId: string
  orderType: OrderType
  slotStartEpochMs: number
  prepareAtEpochMs: number
  status: 'CONFIRMED' | 'PREPARING' | 'CANCELLED' | 'FAILED'
  tripId: string | null
}

const BASE = '/schedule/'

// Bookable slots of one day for an order type. DELIVERY also needs the customer's position: a
// slot is offered only if a delivery trip can still reach that address in it.
export async function getAvailability(
  restaurantId: string,
  date: string,
  orderType: 'DELIVERY' | 'TAKEAWAY',
  location?: GeoPoint,
): Promise<Availability> {
  const params: Record<string, string | number> = { date, orderType }
  if (location) {
    params.lat = location.lat
    params.lng = location.lng
  }
  return unwrap(http.get(`${BASE}${restaurantId}/availability`, { params }))
}

// Manager view of one day: kitchen units booked against the admission quota, slot by slot.
export async function getKitchenLoad(restaurantId: string, date: string): Promise<KitchenLoad> {
  return unwrap(http.get(`${BASE}${restaurantId}/load`, { params: { date } }))
}

// Books the slot and places the order. 409 means the slot (or its last delivery trip) has just
// been taken: ask for availability again.
export async function scheduleOrder(restaurantId: string, input: ScheduleOrderInput): Promise<ScheduledOrder> {
  return unwrap(http.post(`${BASE}${restaurantId}/orders`, input))
}

// Advances the order in order-service and keeps the scheduler's booking in step with it. Managers
// must advance through here rather than POST /orders/advance: otherwise the scheduler, still seeing
// the booking as CONFIRMED, would advance the order a second time when its prep time comes.
export async function advanceScheduledOrder(orderId: string): Promise<void> {
  await unwrap(http.post(`${BASE}orders/${orderId}/advance`, {}))
}
