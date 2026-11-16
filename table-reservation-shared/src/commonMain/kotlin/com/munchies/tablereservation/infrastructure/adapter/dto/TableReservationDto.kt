package com.munchies.tablereservation.infrastructure.adapter.dto

import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

/**
 * Wire form of a table reservation.
 *
 * @property id The reservation identifier.
 * @property restaurantId The restaurant the table belongs to.
 * @property customerId The customer who made the reservation.
 * @property start Beginning of the reserved time slot, as an ISO-8601 UTC instant
 * (e.g. `2026-11-20T19:30:00.000Z`).
 * @property end End of the reserved time slot, same format as [start]. The slot length is fixed
 * by table-reservation-service, not chosen by the customer.
 * @property numberOfGuests How many people the table is reserved for.
 * @property tableNumber The table booked for the reservation, as numbered by the restaurant.
 * @property status The reservation status.
 */
@JsExport
@Serializable
data class TableReservationDto(
  val id: String,
  val restaurantId: String,
  val customerId: String,
  val start: String,
  val end: String,
  val numberOfGuests: Int,
  val tableNumber: Int,
  val status: ReservationStatus,
) {
  fun toJson(): String = wireJson.encodeToString(this)
}
