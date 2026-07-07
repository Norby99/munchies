package com.munchies.order.infrastructure.adapter.dto.factory

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.DeliveryInfo
import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.domain.model.DineInOrder
import com.munchies.order.domain.model.Order
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.domain.model.TableInfo
import com.munchies.order.domain.model.TakeawayInfo
import com.munchies.order.domain.model.TakeawayOrder
import com.munchies.order.infrastructure.adapter.dto.OrderDto
import com.munchies.order.infrastructure.adapter.dto.factory.OrderItemDtoFactory.toDomain
import com.munchies.order.infrastructure.adapter.dto.factory.OrderItemDtoFactory.toDto

object OrderDtoFactory {
  fun Order.toDto(): OrderDto {
    val itemsDto = items.map { it.toDto() }
    return when (this) {
      is DeliveryOrder -> OrderDto.Delivery(
        orderId = id.value,
        restaurantId = restaurantId.value,
        customerId = customerId.value,
        status = status.name,
        items = itemsDto,
        estimatedDeliveryTime = deliveryInfo.estimatedDeliveryTime,
        deliveryAddress = deliveryInfo.deliveryAddress,
        bellName = deliveryInfo.bellName,
        customerPhone = deliveryInfo.customerPhone,
      )
      is TakeawayOrder -> OrderDto.Takeaway(
        orderId = id.value,
        restaurantId = restaurantId.value,
        customerId = customerId.value,
        status = status.name,
        items = itemsDto,
        pickupTime = takeawayInfo.pickupTime,
        customerName = takeawayInfo.customerName,
      )
      is DineInOrder -> OrderDto.DineIn(
        orderId = id.value,
        restaurantId = restaurantId.value,
        customerId = customerId.value,
        status = status.name,
        items = itemsDto,
        tableNumber = tableInfo.tableNumber,
        numberOfGuests = tableInfo.numberOfGuests,
      )
    }
  }

  fun OrderDto.toDomain(): Order {
    val domainItems = items.map { it.toDomain() }
    val domainId = OrderId(orderId)
    val domainRestaurantId = RestaurantId(restaurantId)
    val domainCustomerId = CustomerId(customerId)
    val domainStatus = OrderStatus.valueOf(status)

    return when (this) {
      is OrderDto.Delivery -> DeliveryOrder(
        id = domainId,
        restaurantId = domainRestaurantId,
        customerId = domainCustomerId,
        status = domainStatus,
        items = domainItems,
        deliveryInfo = DeliveryInfo(
          estimatedDeliveryTime = estimatedDeliveryTime,
          deliveryAddress = deliveryAddress,
          bellName = bellName,
          customerPhone = customerPhone,
        ),
      )
      is OrderDto.Takeaway -> TakeawayOrder(
        id = domainId,
        restaurantId = domainRestaurantId,
        customerId = domainCustomerId,
        status = domainStatus,
        items = domainItems,
        takeawayInfo = TakeawayInfo(
          pickupTime = pickupTime,
          customerName = customerName,
        ),
      )
      is OrderDto.DineIn -> DineInOrder(
        id = domainId,
        restaurantId = domainRestaurantId,
        customerId = domainCustomerId,
        status = domainStatus,
        items = domainItems,
        tableInfo = TableInfo(
          tableNumber = tableNumber,
          numberOfGuests = numberOfGuests,
        ),
      )
    }
  }
}
