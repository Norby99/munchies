package com.munchies.order.infrastructure.adapter.dto.factory

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.DeliveryInfo
import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.domain.model.DineInOrder
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.domain.model.TakeawayInfo
import com.munchies.order.domain.model.TakeawayOrder
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createDeliveryOrderDto
import com.munchies.order.fixtures.createDineInOrder
import com.munchies.order.fixtures.createDineInOrderDto
import com.munchies.order.fixtures.createTakeawayOrder
import com.munchies.order.fixtures.createTakeawayOrderDto
import com.munchies.order.infrastructure.adapter.dto.OrderType
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDomain
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class OrderDtoFactoryUnitTest {

  @Test
  fun `toDto should map DeliveryOrder to OrderDto`() {
    val order = createDeliveryOrder()

    val dto = order.toDto()

    dto.orderType shouldBe OrderType.DELIVERY
    dto.orderId shouldBe order.id.value
    dto.restaurantId shouldBe order.restaurantId.value
    dto.customerId shouldBe order.customerId.value
    dto.status shouldBe order.status.name
    dto.items.size shouldBe order.items.size
    dto.estimatedDeliveryTime shouldBe order.deliveryInfo.estimatedDeliveryTime.toString()
    dto.deliveryAddress shouldBe order.deliveryInfo.deliveryAddress
    dto.bellName shouldBe order.deliveryInfo.bellName
    dto.customerPhone shouldBe order.deliveryInfo.customerPhone
  }

  @Test
  fun `toDto should map TakeawayOrder to OrderDto`() {
    val order = createTakeawayOrder()

    val dto = order.toDto()

    dto.orderType shouldBe OrderType.TAKEAWAY
    dto.orderId shouldBe order.id.value
    dto.restaurantId shouldBe order.restaurantId.value
    dto.customerId shouldBe order.customerId.value
    dto.status shouldBe order.status.name
    dto.items.size shouldBe order.items.size
    dto.pickupTime shouldBe order.takeawayInfo.pickupTime.toString()
    dto.customerName shouldBe order.takeawayInfo.customerName
  }

  @Test
  fun `toDto should map DineInOrder to OrderDto`() {
    val order = createDineInOrder()

    val dto = order.toDto()

    dto.orderType shouldBe OrderType.DINE_IN
    dto.orderId shouldBe order.id.value
    dto.restaurantId shouldBe order.restaurantId.value
    dto.customerId shouldBe order.customerId.value
    dto.status shouldBe order.status.name
    dto.items.size shouldBe order.items.size
    dto.tableNumber shouldBe order.tableInfo.tableNumber
    dto.numberOfGuests shouldBe order.tableInfo.numberOfGuests
  }

  @Test
  fun `toDomain should map OrderDto-Delivery to DeliveryOrder`() {
    val dto = createDeliveryOrderDto()

    val order = dto.toDomain()

    order.shouldBeInstanceOf<DeliveryOrder>()
    order.id shouldBe OrderId(dto.orderId)
    order.restaurantId shouldBe RestaurantId(dto.restaurantId)
    order.customerId shouldBe CustomerId(dto.customerId)
    order.status shouldBe OrderStatus.valueOf(dto.status)
    order.items.size shouldBe dto.items.size
    order.deliveryInfo shouldBe DeliveryInfo(
      estimatedDeliveryTime = dto.estimatedDeliveryTime.orEmpty().toLong(),
      deliveryAddress = dto.deliveryAddress.orEmpty(),
      bellName = dto.bellName.orEmpty(),
      customerPhone = dto.customerPhone.orEmpty(),
    )
  }

  @Test
  fun `toDomain should map OrderDto-Takeaway to TakeawayOrder`() {
    val dto = createTakeawayOrderDto()

    val order = dto.toDomain()

    order.shouldBeInstanceOf<TakeawayOrder>()
    order.id shouldBe OrderId(dto.orderId)
    order.restaurantId shouldBe RestaurantId(dto.restaurantId)
    order.customerId shouldBe CustomerId(dto.customerId)
    order.status shouldBe OrderStatus.valueOf(dto.status)
    order.items.size shouldBe dto.items.size
    order.takeawayInfo shouldBe TakeawayInfo(
      pickupTime = dto.pickupTime.orEmpty().toLong(),
      customerName = dto.customerName.orEmpty(),
    )
  }

  @Test
  fun `toDomain should map OrderDto-DineIn to DineInOrder`() {
    val dto = createDineInOrderDto()

    val order = dto.toDomain()

    order.shouldBeInstanceOf<DineInOrder>()
    order.id shouldBe OrderId(dto.orderId)
    order.restaurantId shouldBe RestaurantId(dto.restaurantId)
    order.customerId shouldBe CustomerId(dto.customerId)
    order.status shouldBe OrderStatus.valueOf(dto.status)
    order.items.size shouldBe dto.items.size
    order.tableInfo.tableNumber shouldBe dto.tableNumber
    order.tableInfo.numberOfGuests shouldBe dto.numberOfGuests
  }

  @Test
  fun `toDto followed by toDomain should return equivalent DeliveryOrder`() {
    val order = createDeliveryOrder()

    val roundTripped = order.toDto().toDomain()

    roundTripped shouldBe order
  }

  @Test
  fun `toDto followed by toDomain should return equivalent TakeawayOrder`() {
    val order = createTakeawayOrder()

    val roundTripped = order.toDto().toDomain()

    roundTripped shouldBe order
  }

  @Test
  fun `toDto followed by toDomain should return equivalent DineInOrder`() {
    val order = createDineInOrder()

    val roundTripped = order.toDto().toDomain()

    roundTripped shouldBe order
  }
}
