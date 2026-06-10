package com.munchies.order.domain.factory

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.DeliveryInfo
import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.domain.model.DineInOrder
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderItem
import com.munchies.order.domain.model.OrderStatus.*
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.domain.model.TableInfo
import com.munchies.order.domain.model.TakeawayInfo
import com.munchies.order.domain.model.TakeawayOrder

object OrderFactory {

  fun createDelivery(
    id: OrderId,
    restaurantId: RestaurantId,
    customerId: CustomerId,
    items: List<OrderItem>,
    info: DeliveryInfo,
  ): OrderCreationResult {
    return validate(items) ?: if (!info.isValidTime()) {
      OrderCreationResult.Failure.InvalidDate
    } else {
      OrderCreationResult.Success(
        DeliveryOrder(id, restaurantId, customerId, PENDING, items, info),
      )
    }
  }

  fun createTakeaway(
    id: OrderId,
    restaurantId: RestaurantId,
    customerId: CustomerId,
    items: List<OrderItem>,
    info: TakeawayInfo,
  ): OrderCreationResult {
    return validate(items) ?: if (!info.isValidTime()) {
      OrderCreationResult.Failure.InvalidDate
    } else {
      OrderCreationResult.Success(
        TakeawayOrder(id, restaurantId, customerId, PENDING, items, info),
      )
    }
  }

  fun createDineIn(
    id: OrderId,
    restaurantId: RestaurantId,
    customerId: CustomerId,
    items: List<OrderItem>,
    info: TableInfo,
  ): OrderCreationResult {
    validate(items)?.let { return it }
    return OrderCreationResult.Success(
      DineInOrder(id, restaurantId, customerId, PENDING, items, info),
    )
  }

  private fun validate(items: List<OrderItem>): OrderCreationResult.Failure? {
    if (items.isEmpty()) return OrderCreationResult.Failure.EmptyItems
    if (items.any { it.quantity <= 0 }) return OrderCreationResult.Failure.InvalidItemQuantity
    return null
  }
}
