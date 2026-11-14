package com.munchies.tablereservation.infrastructure.adapter.dto

import kotlin.js.JsExport

/**
 * Lifecycle of a table reservation.
 *
 * - [REQUESTED]: asked by the customer, no table assigned yet. Transient: the service accepts or
 *   refuses a request immediately, so a stored reservation is never in this state.
 * - [ACCEPTED]: a table is booked for the reservation's time slot.
 * - [CANCELED]: the reservation was withdrawn and its table is free again.
 */
@JsExport
enum class ReservationStatus {
  REQUESTED,
  ACCEPTED,
  CANCELED,
  ;

  override fun toString(): String = name
}
