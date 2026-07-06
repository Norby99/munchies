package com.munchies.order.infrastructure.adapter.dto.factory

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.DeliveryInfo
import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.domain.model.DineInOrder
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.domain.model.TableInfo
import com.munchies.order.domain.model.TakeawayInfo
import com.munchies.order.domain.model.TakeawayOrder
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createDeliveryOrderDto
import com.munchies.order.fixtures.createDineInOrder
import com.munchies.order.fixtures.createDineInOrderDto
import com.munchies.order.fixtures.createTakeawayOrder
import com.munchies.order.fixtures.createTakeawayOrderDto
import com.munchies.order.infrastructure.adapter.dto.OrderDto
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDomain
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class OrderDtoFactoryTest {

  @Test
  fun `toDto should map DeliveryOrder to OrderDto-Delivery`() {
    val order = createDeliveryOrder()

    val dto = order.toDto()

    dto.shouldBeInstanceOf<OrderDto.Delivery>()
    dto as OrderDto.Delivery
    dto.orderId shouldBe order.id.value
    dto.restaurantId shouldBe order.restaurantId.value
    dto.customerId shouldBe order.customerId.value
    dto.status shouldBe order.status.name
    dto.items.size shouldBe order.items.size
    dto.estimatedDeliveryTime shouldBe order.deliveryInfo.estimatedDeliveryTime
    dto.deliveryAddress shouldBe order.deliveryInfo.deliveryAddress
    dto.bellName shouldBe order.deliveryInfo.bellName
    dto.customerPhone shouldBe order.deliveryInfo.customerPhone
  }

  @Test
  fun `toDto should map TakeawayOrder to OrderDto-Takeaway`() {
    val order = createTakeawayOrder()

    val dto = order.toDto()

    dto.shouldBeInstanceOf<OrderDto.Takeaway>()
    dto as OrderDto.Takeaway
    dto.orderId shouldBe order.id.value
    dto.restaurantId shouldBe order.restaurantId.value
    dto.customerId shouldBe order.customerId.value
    dto.status shouldBe order.status.name
    dto.items.size shouldBe order.items.size
    dto.pickupTime shouldBe order.takeawayInfo.pickupTime
    dto.customerName shouldBe order.takeawayInfo.customerName
  }

  @Test
  fun `toDto should map DineInOrder to OrderDto-DineIn`() {
    val order = createDineInOrder()

    val dto = order.toDto()

    dto.shouldBeInstanceOf<OrderDto.DineIn>()
    dto as OrderDto.DineIn
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
    order as DeliveryOrder
    order.id shouldBe OrderId(dto.orderId)
    order.restaurantId shouldBe RestaurantId(dto.restaurantId)
    order.customerId shouldBe CustomerId(dto.customerId)
    order.status shouldBe OrderStatus.valueOf(dto.status)
    order.items.size shouldBe dto.items.size
    order.deliveryInfo shouldBe DeliveryInfo(
      estimatedDeliveryTime = dto.estimatedDeliveryTime,
      deliveryAddress = dto.deliveryAddress,
      bellName = dto.bellName,
      customerPhone = dto.customerPhone,
    )
  }

  @Test
  fun `toDomain should map OrderDto-Takeaway to TakeawayOrder`() {
    val dto = createTakeawayOrderDto()

    val order = dto.toDomain()

    order.shouldBeInstanceOf<TakeawayOrder>()
    order as TakeawayOrder
    order.id shouldBe OrderId(dto.orderId)
    order.restaurantId shouldBe RestaurantId(dto.restaurantId)
    order.customerId shouldBe CustomerId(dto.customerId)
    order.status shouldBe OrderStatus.valueOf(dto.status)
    order.items.size shouldBe dto.items.size
    order.takeawayInfo shouldBe TakeawayInfo(
      pickupTime = dto.pickupTime,
      customerName = dto.customerName,
    )
  }

  @Test
  fun `toDomain should map OrderDto-DineIn to DineInOrder`() {
    val dto = createDineInOrderDto()

    val order = dto.toDomain()

    order.shouldBeInstanceOf<DineInOrder>()
    order as DineInOrder
    order.id shouldBe OrderId(dto.orderId)
    order.restaurantId shouldBe RestaurantId(dto.restaurantId)
    order.customerId shouldBe CustomerId(dto.customerId)
    order.status shouldBe OrderStatus.valueOf(dto.status)
    order.items.size shouldBe dto.items.size
    order.tableInfo shouldBe TableInfo(
      tableNumber = dto.tableNumber,
      numberOfGuests = dto.numberOfGuests,
    )
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
