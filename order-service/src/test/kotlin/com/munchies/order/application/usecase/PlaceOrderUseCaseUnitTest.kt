package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.PlaceOrder
import com.munchies.order.domain.factory.OrderCreationResult
import com.munchies.order.domain.factory.OrderFactory
import com.munchies.order.domain.model.*
import com.munchies.order.domain.port.OrderEventStore
import com.munchies.order.fixtures.asHistory
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createDineInOrder
import com.munchies.order.fixtures.createTakeawayOrder
import com.munchies.order.fixtures.deliveryCommand
import com.munchies.order.fixtures.dineInCommand
import com.munchies.order.fixtures.takeawayCommand
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PlaceOrderUseCaseUnitTest {

  private val eventStore = mockk<OrderEventStore>()
  private val useCase = PlaceOrderUseCase(eventStore)

  @BeforeEach
  fun setUp() {
    mockkObject(OrderFactory)
  }

  @AfterEach
  fun tearDown() {
    confirmVerified(OrderFactory)
    unmockkObject(OrderFactory)
  }

  // ---------- DELIVERY ----------

  @Test
  fun `execute creates and appends a delivery order on success`() {
    val command = deliveryCommand()
    val order = createDeliveryOrder()
    every { OrderFactory.createDelivery(any(), any(), any(), any(), any()) } returns
      OrderCreationResult.Success(order.asHistory())
    every { eventStore.append(any(), 0, any()) } just Runs

    val result = useCase.execute(command)

    result.shouldBeInstanceOf<PlaceOrder.Result.Success>()
    verify(exactly = 1) {
      OrderFactory.createDelivery(
        any(),
        command.restaurantId,
        command.customerId,
        command.items,
        DeliveryInfo(
          deliveryAddress = command.deliveryAddress,
          bellName = command.bellName,
          customerPhone = command.customerPhone,
          estimatedDeliveryTime = command.estimatedDeliveryTime,
        ),
      )
    }
    verify(exactly = 0) { OrderFactory.createTakeaway(any(), any(), any(), any(), any()) }
    verify(exactly = 0) { OrderFactory.createDineIn(any(), any(), any(), any(), any()) }
    verify(exactly = 1) { eventStore.append(any(), 0, any()) }
    verify(exactly = 1) { OrderFactory.fromHistory(any()) }
  }

  // ---------- TAKEAWAY ----------

  @Test
  fun `execute creates and appends a takeaway order on success`() {
    val command = takeawayCommand()
    val order = createTakeawayOrder()
    every { OrderFactory.createTakeaway(any(), any(), any(), any(), any()) } returns
      OrderCreationResult.Success(order.asHistory())
    every { eventStore.append(any(), 0, any()) } just Runs

    val result = useCase.execute(command)

    result.shouldBeInstanceOf<PlaceOrder.Result.Success>()
    verify(exactly = 1) {
      OrderFactory.createTakeaway(
        any(),
        command.restaurantId,
        command.customerId,
        command.items,
        TakeawayInfo(
          pickupTime = command.pickupTime,
          customerName = command.customerName,
        ),
      )
    }
    verify(exactly = 0) { OrderFactory.createDelivery(any(), any(), any(), any(), any()) }
    verify(exactly = 0) { OrderFactory.createDineIn(any(), any(), any(), any(), any()) }
    verify(exactly = 1) { eventStore.append(any(), 0, any()) }
    verify(exactly = 1) { OrderFactory.fromHistory(any()) }
  }

  // ---------- DINE IN ----------

  @Test
  fun `execute creates and appends a dine-in order on success`() {
    val command = dineInCommand()
    val order = createDineInOrder()
    every { OrderFactory.createDineIn(any(), any(), any(), any(), any()) } returns
      OrderCreationResult.Success(order.asHistory())
    every { eventStore.append(any(), 0, any()) } just Runs

    val result = useCase.execute(command)

    result.shouldBeInstanceOf<PlaceOrder.Result.Success>()
    verify(exactly = 1) {
      OrderFactory.createDineIn(
        any(),
        command.restaurantId,
        command.customerId,
        command.items,
        TableInfo(
          tableNumber = command.tableNumber,
          numberOfGuests = command.numberOfGuests,
        ),
      )
    }
    verify(exactly = 0) { OrderFactory.createDelivery(any(), any(), any(), any(), any()) }
    verify(exactly = 0) { OrderFactory.createTakeaway(any(), any(), any(), any(), any()) }
    verify(exactly = 1) { eventStore.append(any(), 0, any()) }
    verify(exactly = 1) { OrderFactory.fromHistory(any()) }
  }

  // ---------- FAILURE MAPPING ----------

  @Test
  fun `execute maps EmptyItems failure and does not append`() {
    every { OrderFactory.createDelivery(any(), any(), any(), any(), any()) } returns
      OrderCreationResult.Failure.EmptyItems

    val result = useCase.execute(deliveryCommand())

    result shouldBe PlaceOrder.Result.Failure.EmptyItems
    verify(exactly = 1) { OrderFactory.createDelivery(any(), any(), any(), any(), any()) }
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute maps InvalidItemQuantity failure and does not append`() {
    every { OrderFactory.createDelivery(any(), any(), any(), any(), any()) } returns
      OrderCreationResult.Failure.InvalidItemQuantity

    val result = useCase.execute(deliveryCommand())

    result shouldBe PlaceOrder.Result.Failure.InvalidItemQuantity
    verify(exactly = 1) { OrderFactory.createDelivery(any(), any(), any(), any(), any()) }
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute maps InvalidDate failure and does not append`() {
    every { OrderFactory.createTakeaway(any(), any(), any(), any(), any()) } returns
      OrderCreationResult.Failure.InvalidDate

    val result = useCase.execute(takeawayCommand())

    result shouldBe PlaceOrder.Result.Failure.InvalidDate
    verify(exactly = 1) { OrderFactory.createTakeaway(any(), any(), any(), any(), any()) }
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }
}
