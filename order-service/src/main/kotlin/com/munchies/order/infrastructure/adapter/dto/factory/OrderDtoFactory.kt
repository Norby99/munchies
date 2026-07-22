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
import com.munchies.order.infrastructure.adapter.dto.Delivery
import com.munchies.order.infrastructure.adapter.dto.DineIn
import com.munchies.order.infrastructure.adapter.dto.OrderDto
import com.munchies.order.infrastructure.adapter.dto.Takeaway
import com.munchies.order.infrastructure.adapter.dto.factory.OrderItemDtoFactory.toDomain
import com.munchies.order.infrastructure.adapter.dto.factory.OrderItemDtoFactory.toDto

/**
 * Factory object for converting between Order domain models and Order DTOs.
 *
 * This factory provides methods to convert Order objects to their corresponding DTO representations
 * and vice versa. It handles different types of orders, including Delivery, Takeaway, and DineIn orders.
 */
object OrderDtoFactory {
  fun Order.toDto(): OrderDto {
    val itemsDto = items.map { it.toDto() }
    return when (this) {
      is DeliveryOrder -> Delivery(
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
      is TakeawayOrder -> Takeaway(
        orderId = id.value,
        restaurantId = restaurantId.value,
        customerId = customerId.value,
        status = status.name,
        items = itemsDto,
        pickupTime = takeawayInfo.pickupTime,
        customerName = takeawayInfo.customerName,
      )
      is DineInOrder -> DineIn(
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

  /**
   * Converts an OrderDto to its corresponding Order domain model.
   *
   * This function maps the fields of the DTO to the appropriate domain model,
   * handling different types of orders based on the DTO subtype.
   *
   * @return The corresponding Order domain model.
   */
  fun OrderDto.toDomain(): Order {
    val domainItems = items.map { it.toDomain() }
    val domainId = OrderId(orderId)
    val domainRestaurantId = RestaurantId(restaurantId)
    val domainCustomerId = CustomerId(customerId)
    val domainStatus = OrderStatus.valueOf(status)

    return when (this) {
      is Delivery -> DeliveryOrder(
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
      is Takeaway -> TakeawayOrder(
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
      is DineIn -> DineInOrder(
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
