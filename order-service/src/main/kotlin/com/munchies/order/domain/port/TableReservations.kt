package com.munchies.order.domain.port

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.RestaurantId

/**
 * Outbound port to the table reservations, owned by table-reservation-service.
 *
 * order-service uses it when a dine-in order is placed for a reservation: the table the order is
 * served at, and the number of guests, come from the reservation, never from the client.
 */
interface TableReservations {

  /**
   * Looks up the reservation [reservationId] of [customerId]. A reservation made by another
   * customer is reported as [Lookup.NotFound], exactly like one that does not exist.
   */
  fun find(reservationId: String, customerId: CustomerId): Lookup

  /** Outcome of [find]. */
  sealed interface Lookup {
    data class Found(val reservation: ReservedTable) : Lookup

    /** The customer has no such reservation. */
    data object NotFound : Lookup

    /** table-reservation-service could not be reached or answered with an unexpected error. */
    data object Unavailable : Lookup
  }

  /**
   * What order-service needs to know about a reservation.
   *
   * @property restaurantId The restaurant the table belongs to.
   * @property tableNumber The table booked for the reservation.
   * @property numberOfGuests How many people the table is reserved for.
   * @property accepted Whether the reservation currently holds its table (it is not canceled).
   */
  data class ReservedTable(
    val restaurantId: RestaurantId,
    val tableNumber: Int,
    val numberOfGuests: Int,
    val accepted: Boolean,
  )
}
