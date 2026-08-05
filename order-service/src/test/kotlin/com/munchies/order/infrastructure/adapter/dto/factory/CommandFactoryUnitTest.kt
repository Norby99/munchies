package com.munchies.order.infrastructure.adapter.dto.factory

import com.munchies.order.application.port.inbound.command.PlaceOrderCommand
import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.fixtures.Address1
import com.munchies.order.fixtures.createTestPlaceOrderRequest
import com.munchies.order.fixtures.futureTime
import com.munchies.order.infrastructure.adapter.dto.OrderType
import com.munchies.order.infrastructure.adapter.dto.factory.CommandFactory.toCommand
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CommandFactoryUnitTest {

  private fun baseDeliveryRequest() = createTestPlaceOrderRequest(
    orderType = OrderType.DELIVERY,
    estimatedDeliveryTime = futureTime,
    deliveryAddress = Address1.deliveryAddress,
    bellName = Address1.bellName,
    customerPhone = Address1.customerPhone,
  )

  private fun baseTakeawayRequest() = createTestPlaceOrderRequest(
    orderType = OrderType.TAKEAWAY,
    pickupTime = futureTime,
    customerName = Address1.bellName,
  )

  private fun baseDineInRequest() = createTestPlaceOrderRequest(
    orderType = OrderType.DINE_IN,
    tableNumber = 5,
    numberOfGuests = 2,
  )

  // ==========================================
  // TEST: DELIVERY
  // ==========================================

  @Test
  fun `toCommand should map DELIVERY request to PlaceOrderCommand-Delivery`() {
    val request = baseDeliveryRequest()

    val command = request.toCommand()

    command.shouldBeInstanceOf<PlaceOrderCommand.Delivery>()
    command as PlaceOrderCommand.Delivery
    command.restaurantId shouldBe RestaurantId(request.restaurantId)
    command.customerId shouldBe CustomerId(request.customerId)
    command.items.size shouldBe request.items.size
    command.estimatedDeliveryTime shouldBe request.estimatedDeliveryTime?.toLong()
    command.deliveryAddress shouldBe request.deliveryAddress
    command.bellName shouldBe request.bellName
    command.customerPhone shouldBe request.customerPhone
  }

  @Test
  fun `toCommand should throw when estimatedDeliveryTime is null for DELIVERY`() {
    val request = baseDeliveryRequest().copy(estimatedDeliveryTime = null)

    val exception = assertThrows(IllegalArgumentException::class.java) {
      request.toCommand()
    }

    exception.message shouldBe "Estimated delivery time is required for delivery orders"
  }

  @Test
  fun `toCommand should throw when deliveryAddress is null for DELIVERY`() {
    val request = baseDeliveryRequest().copy(deliveryAddress = null)

    val exception = assertThrows(IllegalArgumentException::class.java) {
      request.toCommand()
    }

    exception.message shouldBe "Delivery address is required for delivery orders"
  }

  @Test
  fun `toCommand should throw when bellName is null for DELIVERY`() {
    val request = baseDeliveryRequest().copy(bellName = null)

    val exception = assertThrows(IllegalArgumentException::class.java) {
      request.toCommand()
    }

    exception.message shouldBe "Bell name is required for delivery orders"
  }

  @Test
  fun `toCommand should throw when customerPhone is null for DELIVERY`() {
    val request = baseDeliveryRequest().copy(customerPhone = null)

    val exception = assertThrows(IllegalArgumentException::class.java) {
      request.toCommand()
    }

    exception.message shouldBe "Customer phone is required for delivery orders"
  }

  // ==========================================
  // TEST: TAKEAWAY
  // ==========================================

  @Test
  fun `toCommand should map TAKEAWAY request to PlaceOrderCommand-Takeaway`() {
    val request = baseTakeawayRequest()

    val command = request.toCommand()

    command.shouldBeInstanceOf<PlaceOrderCommand.Takeaway>()
    command as PlaceOrderCommand.Takeaway
    command.restaurantId shouldBe RestaurantId(request.restaurantId)
    command.customerId shouldBe CustomerId(request.customerId)
    command.items.size shouldBe request.items.size
    command.pickupTime shouldBe request.pickupTime?.toLong()
    command.customerName shouldBe request.customerName
  }

  @Test
  fun `toCommand should throw when pickupTime is null for TAKEAWAY`() {
    val request = baseTakeawayRequest().copy(pickupTime = null)

    val exception = assertThrows(IllegalArgumentException::class.java) {
      request.toCommand()
    }

    exception.message shouldBe "Pickup time is required for takeaway orders"
  }

  @Test
  fun `toCommand should throw when customerName is null for TAKEAWAY`() {
    val request = baseTakeawayRequest().copy(customerName = null)

    val exception = assertThrows(IllegalArgumentException::class.java) {
      request.toCommand()
    }

    exception.message shouldBe "Customer name is required for takeaway orders"
  }

  // ==========================================
  // TEST: DINE_IN
  // ==========================================

  @Test
  fun `toCommand should map DINE_IN request to PlaceOrderCommand-DineIn`() {
    val request = baseDineInRequest()

    val command = request.toCommand()

    command.shouldBeInstanceOf<PlaceOrderCommand.DineIn>()
    command as PlaceOrderCommand.DineIn
    command.restaurantId shouldBe RestaurantId(request.restaurantId)
    command.customerId shouldBe CustomerId(request.customerId)
    command.items.size shouldBe request.items.size
    command.tableNumber shouldBe request.tableNumber
    command.numberOfGuests shouldBe request.numberOfGuests
  }

  @Test
  fun `toCommand should throw when tableNumber is null for DINE_IN`() {
    val request = baseDineInRequest().copy(tableNumber = null)

    val exception = assertThrows(IllegalArgumentException::class.java) {
      request.toCommand()
    }

    exception.message shouldBe "Table number is required for dine-in orders"
  }

  @Test
  fun `toCommand should throw when numberOfGuests is null for DINE_IN`() {
    val request = baseDineInRequest().copy(numberOfGuests = null)

    val exception = assertThrows(IllegalArgumentException::class.java) {
      request.toCommand()
    }

    exception.message shouldBe "Number of guests is required for dine-in orders"
  }
}
