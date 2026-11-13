package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.PlaceOrder
import com.munchies.order.application.port.inbound.command.PlaceOrderCommand
import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.Price
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.domain.model.TableInfo
import com.munchies.order.domain.model.event.OrderEvent
import com.munchies.order.domain.model.event.OrderPlaced
import com.munchies.order.domain.port.MenuCatalog
import com.munchies.order.domain.port.OrderEventStore
import com.munchies.order.domain.port.TableReservations
import com.munchies.order.domain.port.TableReservations.Lookup
import com.munchies.order.fixtures.dineInCommand
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/** Placing a dine-in order for a table reservation. */
class PlaceReservedDineInOrderUseCaseUnitTest {

  private val eventStore = mockk<OrderEventStore>()
  private val menuCatalog = mockk<MenuCatalog>()
  private val tableReservations = mockk<TableReservations>()
  private val useCase = PlaceOrderUseCase(eventStore, menuCatalog, tableReservations)

  private val base = dineInCommand()
  private val command = PlaceOrderCommand.ReservedDineIn(
    restaurantId = base.restaurantId,
    customerId = base.customerId,
    items = base.items,
    reservationId = "reservation-1",
  )

  private fun reserved(
    restaurantId: RestaurantId = command.restaurantId,
    accepted: Boolean = true,
  ) = Lookup.Found(
    TableReservations.ReservedTable(
      restaurantId = restaurantId,
      tableNumber = 7,
      numberOfGuests = 4,
      accepted = accepted,
    ),
  )

  @BeforeEach
  fun setUp() {
    every { menuCatalog.findPrices(any(), any()) } returns
      MenuCatalog.PriceLookup.Found(command.items.associate { it.menuItemId to Price(950) })
  }

  private fun neverPlaced() {
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
    verify(exactly = 0) { menuCatalog.findPrices(any(), any()) }
  }

  @Test
  fun `execute places the order at the reservation's table, for its guests`() {
    every { tableReservations.find("reservation-1", command.customerId) } returns reserved()
    val events = slot<List<OrderEvent>>()
    every { eventStore.append(any(), 0, capture(events)) } just Runs

    val result = useCase.execute(command)

    result.shouldBeInstanceOf<PlaceOrder.Result.Success>()
    val placed = events.captured.first().shouldBeInstanceOf<OrderPlaced>()
    placed.details shouldBe TableInfo(tableNumber = 7, numberOfGuests = 4)
  }

  @Test
  fun `execute fails when the customer has no such reservation`() {
    every { tableReservations.find(any(), any()) } returns Lookup.NotFound

    useCase.execute(command) shouldBe PlaceOrder.Result.Failure.ReservationNotFound
    neverPlaced()
  }

  @Test
  fun `execute fails when the reservation is canceled`() {
    every { tableReservations.find(any(), any()) } returns reserved(accepted = false)

    useCase.execute(command).shouldBeInstanceOf<PlaceOrder.Result.Failure.ReservationNotUsable>()
    neverPlaced()
  }

  @Test
  fun `execute fails when the reservation was made at another restaurant`() {
    every { tableReservations.find(any(), any()) } returns
      reserved(restaurantId = RestaurantId("another-restaurant"))

    useCase.execute(command).shouldBeInstanceOf<PlaceOrder.Result.Failure.ReservationNotUsable>()
    neverPlaced()
  }

  @Test
  fun `execute fails when table-reservation-service is unavailable`() {
    every { tableReservations.find(any(), any()) } returns Lookup.Unavailable

    useCase.execute(command) shouldBe PlaceOrder.Result.Failure.ReservationUnavailable
    neverPlaced()
  }

  @Test
  fun `execute does not ask for a reservation for a walk-in dine-in order`() {
    every { eventStore.append(any(), 0, any()) } just Runs

    useCase.execute(base).shouldBeInstanceOf<PlaceOrder.Result.Success>()

    verify(exactly = 0) { tableReservations.find(any(), any<CustomerId>()) }
  }
}
