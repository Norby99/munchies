package com.munchies.restaurant.domain.valueobject.restaurant

/**
 * A table of the restaurant's dining room, as configured by the manager.
 *
 * It is output configuration consumed by table-reservation-service, which books reservations
 * against these tables: a party is seated at one table with at least as many [seats].
 *
 * @property number The number the staff identifies the table with, unique within the restaurant.
 * @property seats How many guests the table can seat.
 */
data class Table(
  val number: Int,
  val seats: Int,
) {
  init {
    require(number > 0) { "table number must be positive" }
    require(seats > 0) { "table seats must be positive" }
  }
}
