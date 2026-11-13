package com.munchies.order.infrastructure.adapter.outbound.reservation

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.domain.port.TableReservations
import com.munchies.order.domain.port.TableReservations.Lookup
import com.munchies.tablereservation.infrastructure.adapter.dto.ReservationStatus
import com.munchies.tablereservation.infrastructure.adapter.dto.TableReservationDto
import com.munchies.tablereservation.infrastructure.adapter.outbound.response.TableReservationResponse
import io.kotest.matchers.shouldBe
import io.micronaut.http.client.exceptions.HttpClientException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class RestTableReservationsUnitTest {

  private val client = mockk<TableReservationServiceClient>()
  private val reservations = RestTableReservations(client)
  private val customer = CustomerId("customer-1")

  private fun body(status: ReservationStatus) = TableReservationResponse(
    TableReservationDto(
      id = "reservation-1",
      restaurantId = "restaurant-1",
      customerId = customer.value,
      start = "2026-11-20T19:00:00.000Z",
      end = "2026-11-20T21:00:00.000Z",
      numberOfGuests = 4,
      tableNumber = 7,
      status = status,
    ),
  ).toJson()

  @Test
  fun `find maps an accepted reservation of the customer`() {
    every { client.getReservation("reservation-1", "customer-1") } returns
      body(ReservationStatus.ACCEPTED)

    reservations.find("reservation-1", customer) shouldBe Lookup.Found(
      TableReservations.ReservedTable(
        restaurantId = RestaurantId("restaurant-1"),
        tableNumber = 7,
        numberOfGuests = 4,
        accepted = true,
      ),
    )
  }

  @Test
  fun `find reports a canceled reservation as not accepted`() {
    every { client.getReservation(any(), any()) } returns body(ReservationStatus.CANCELED)

    val lookup = reservations.find("reservation-1", customer) as Lookup.Found

    lookup.reservation.accepted shouldBe false
  }

  @Test
  fun `find reports a missing reservation`() {
    every { client.getReservation(any(), any()) } returns null

    reservations.find("missing", customer) shouldBe Lookup.NotFound
  }

  @Test
  fun `find is unavailable when the service cannot be reached`() {
    every { client.getReservation(any(), any()) } throws HttpClientException("connection refused")

    reservations.find("reservation-1", customer) shouldBe Lookup.Unavailable
  }

  @Test
  fun `find is unavailable when the answer cannot be decoded`() {
    every { client.getReservation(any(), any()) } returns "<html>"

    reservations.find("reservation-1", customer) shouldBe Lookup.Unavailable
  }
}
