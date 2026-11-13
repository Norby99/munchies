package com.munchies.order.infrastructure.adapter.outbound.reservation

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.domain.port.TableReservations
import com.munchies.order.domain.port.TableReservations.Lookup
import com.munchies.tablereservation.infrastructure.adapter.dto.ReservationStatus
import com.munchies.tablereservation.infrastructure.adapter.outbound.response.tableReservationResponseFromJson
import io.micronaut.http.client.exceptions.HttpClientException
import org.slf4j.LoggerFactory

/**
 * [TableReservations] backed by table-reservation-service's REST API.
 *
 * The customer is sent along with the lookup, so table-reservation-service itself hides other
 * customers' reservations (it answers `404`). Every transport, status or decoding failure
 * becomes [Lookup.Unavailable]: the caller cannot tell which table the order is for and must
 * not place it at a made-up one.
 */
class RestTableReservations(private val client: TableReservationServiceClient) : TableReservations {

  override fun find(reservationId: String, customerId: CustomerId): Lookup = try {
    val body = client.getReservation(reservationId, customerId.value)
    if (body == null) {
      Lookup.NotFound
    } else {
      val reservation = tableReservationResponseFromJson(body).result
      Lookup.Found(
        TableReservations.ReservedTable(
          restaurantId = RestaurantId(reservation.restaurantId),
          tableNumber = reservation.tableNumber,
          numberOfGuests = reservation.numberOfGuests,
          accepted = reservation.status == ReservationStatus.ACCEPTED,
        ),
      )
    }
  } catch (e: HttpClientException) {
    unavailable(reservationId, e)
  } catch (e: IllegalArgumentException) {
    // An undecodable body (kotlinx SerializationException is an IllegalArgumentException).
    unavailable(reservationId, e)
  }

  private fun unavailable(reservationId: String, cause: Exception): Lookup {
    logger.error("Cannot read the table reservation {}: {}", reservationId, cause.message)
    return Lookup.Unavailable
  }

  private companion object {
    val logger = LoggerFactory.getLogger(RestTableReservations::class.java)
  }
}
