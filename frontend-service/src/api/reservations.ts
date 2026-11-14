// Table reservations (table-reservation-service, through the gateway under /reservations/).
//
// Who is asking always comes from the session cookie: the gateway fills customerId (and, for a
// manager's list, managerId) and ignores any value sent by the client, so requests carry an empty
// customerId only because the shared request DTOs declare the field.
//
// Booking rules live in the service: arrival on a quarter of an hour and in the future, a fixed
// length, one table per party (the smallest free one that fits). The UI only offers valid
// arrival times; a 409 means no table was free (or the reservation can no longer change).

import { http, unwrap } from '@/api/client'
import type { Reservation } from '@/types'

const BASE = '/reservations/'

export interface ReservationInput {
  // ISO-8601 instant of the arrival.
  start: string
  numberOfGuests: number
}

export async function reserveTable(restaurantId: string, input: ReservationInput): Promise<Reservation> {
  return unwrap(http.post(BASE, { restaurantId, customerId: '', ...input }))
}

// For a customer: their own reservations, optionally at one restaurant. For a manager: the
// reservations at one of their restaurants (restaurantId required). Ordered by start.
export async function listReservations(restaurantId?: string): Promise<Reservation[]> {
  return unwrap(http.get(BASE, { params: restaurantId ? { restaurantId } : {} }))
}

export async function modifyReservation(reservationId: string, input: ReservationInput): Promise<Reservation> {
  return unwrap(http.put(`${BASE}${reservationId}/`, { customerId: '', ...input }))
}

export async function cancelReservation(reservationId: string): Promise<Reservation> {
  return unwrap(http.patch(`${BASE}${reservationId}/cancel/`, { customerId: '' }))
}
