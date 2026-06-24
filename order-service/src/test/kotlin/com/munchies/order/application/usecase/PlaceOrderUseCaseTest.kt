package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.PlaceOrder
import com.munchies.order.domain.factory.OrderCreationResult
import com.munchies.order.domain.factory.OrderFactory
import com.munchies.order.domain.model.*
import com.munchies.order.domain.ports.OrderRepository
import com.munchies.order.fixtures.deliveryCommand
import com.munchies.order.fixtures.dineInCommand
import com.munchies.order.fixtures.takeawayCommand
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PlaceOrderUseCaseTest {

  private val repository = mockk<OrderRepository>()
  private val useCase = PlaceOrderUseCase(repository)

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
  fun `execute creates and saves a delivery order on success`() {
    val command = deliveryCommand()
    val order = defaultDeliveryOrder()
    every { OrderFactory.createDelivery(any(), any(), any(), any(), any()) } returns
      OrderCreationResult.Success(order)
    every { repository.save(order) } just Runs

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
    verify(exactly = 1) { repository.save(order) }
  }

  // ---------- TAKEAWAY ----------

  @Test
  fun `execute creates and saves a takeaway order on success`() {
    val command = takeawayCommand()
    val order = defaultTakeawayOrder()
    every { OrderFactory.createTakeaway(any(), any(), any(), any(), any()) } returns
      OrderCreationResult.Success(order)
    every { repository.save(order) } just Runs

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
    verify(exactly = 1) { repository.save(order) }
  }

  // ---------- DINE IN ----------

  @Test
  fun `execute creates and saves a dine-in order on success`() {
    val command = dineInCommand()
    val order = defaultDineInOrder()
    every { OrderFactory.createDineIn(any(), any(), any(), any(), any()) } returns
      OrderCreationResult.Success(order)
    every { repository.save(order) } just Runs

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
    verify(exactly = 1) { repository.save(order) }
  }

  // ---------- FAILURE MAPPING ----------

  @Test
  fun `execute maps EmptyItems failure and does not save`() {
    every { OrderFactory.createDelivery(any(), any(), any(), any(), any()) } returns
      OrderCreationResult.Failure.EmptyItems

    val result = useCase.execute(deliveryCommand())

    result shouldBe PlaceOrder.Result.Failure.EmptyItems
    verify(exactly = 1) { OrderFactory.createDelivery(any(), any(), any(), any(), any()) }
    verify(exactly = 0) { repository.save(any()) }
  }

  @Test
  fun `execute maps InvalidItemQuantity failure and does not save`() {
    every { OrderFactory.createDelivery(any(), any(), any(), any(), any()) } returns
      OrderCreationResult.Failure.InvalidItemQuantity

    val result = useCase.execute(deliveryCommand())

    result shouldBe PlaceOrder.Result.Failure.InvalidItemQuantity
    verify(exactly = 1) { OrderFactory.createDelivery(any(), any(), any(), any(), any()) }
    verify(exactly = 0) { repository.save(any()) }
  }

  @Test
  fun `execute maps InvalidDate failure and does not save`() {
    every { OrderFactory.createTakeaway(any(), any(), any(), any(), any()) } returns
      OrderCreationResult.Failure.InvalidDate

    val result = useCase.execute(takeawayCommand())

    result shouldBe PlaceOrder.Result.Failure.InvalidDate
    verify(exactly = 1) { OrderFactory.createTakeaway(any(), any(), any(), any(), any()) }
    verify(exactly = 0) { repository.save(any()) }
  }
}
