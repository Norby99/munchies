package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.PlaceOrder
import com.munchies.order.domain.factory.OrderCreationResult
import com.munchies.order.domain.factory.OrderFactory
import com.munchies.order.domain.model.*
import com.munchies.order.domain.port.MenuCatalog
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
  private val menuCatalog = mockk<MenuCatalog>()
  private val useCase = PlaceOrderUseCase(eventStore, menuCatalog, mockk())

  /** The fixture commands order item-1; this is its menu price. */
  private val item1Price = Price(950)

  @BeforeEach
  fun setUp() {
    mockkObject(OrderFactory)
    every { menuCatalog.findPrices(any(), any()) } returns
      MenuCatalog.PriceLookup.Found(mapOf(MenuItemId("item-1") to item1Price))
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
        command.items.map { it.pricedAt(item1Price) },
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
        command.items.map { it.pricedAt(item1Price) },
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
        command.items.map { it.pricedAt(item1Price) },
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

  // ---------- PRICING ----------

  @Test
  fun `execute prices the items with the menu prices, ignoring any client price`() {
    every { OrderFactory.createDelivery(any(), any(), any(), any(), any()) } returns
      OrderCreationResult.Success(createDeliveryOrder().asHistory())
    every { eventStore.append(any(), 0, any()) } just Runs
    val command = deliveryCommand()

    useCase.execute(command)

    verify(exactly = 1) {
      menuCatalog.findPrices(command.restaurantId, setOf(MenuItemId("item-1")))
    }
    verify(exactly = 1) {
      OrderFactory.createDelivery(
        any(),
        any(),
        any(),
        listOf(OrderItem(MenuItemId("item-1"), 2, item1Price)),
        any(),
      )
    }
    verify(exactly = 1) { OrderFactory.fromHistory(any()) }
  }

  @Test
  fun `execute rejects an empty order without asking restaurant-service`() {
    val result = useCase.execute(deliveryCommand(items = emptyList()))

    result shouldBe PlaceOrder.Result.Failure.EmptyItems
    verify(exactly = 0) { menuCatalog.findPrices(any(), any()) }
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute rejects an invalid quantity without asking restaurant-service`() {
    val result = useCase.execute(
      deliveryCommand(items = listOf(RequestedItem(MenuItemId("item-1"), 0))),
    )

    result shouldBe PlaceOrder.Result.Failure.InvalidItemQuantity
    verify(exactly = 0) { menuCatalog.findPrices(any(), any()) }
  }

  @Test
  fun `execute rejects items that are not on the restaurant's menus`() {
    every { menuCatalog.findPrices(any(), any()) } returns
      MenuCatalog.PriceLookup.Found(emptyMap())

    val result = useCase.execute(deliveryCommand())

    result shouldBe PlaceOrder.Result.Failure.UnknownItems(setOf(MenuItemId("item-1")))
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute maps a missing restaurant to RestaurantNotFound`() {
    every { menuCatalog.findPrices(any(), any()) } returns
      MenuCatalog.PriceLookup.RestaurantNotFound

    useCase.execute(deliveryCommand()) shouldBe PlaceOrder.Result.Failure.RestaurantNotFound
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute does not place the order when the menu prices are unavailable`() {
    every { menuCatalog.findPrices(any(), any()) } returns MenuCatalog.PriceLookup.Unavailable

    useCase.execute(deliveryCommand()) shouldBe PlaceOrder.Result.Failure.MenuUnavailable
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }
}
