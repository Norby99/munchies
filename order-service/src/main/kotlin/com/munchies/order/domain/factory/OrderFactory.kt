package com.munchies.order.domain.factory

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.DeliveryInfo
import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.domain.model.DineInOrder
import com.munchies.order.domain.model.Order
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderItem
import com.munchies.order.domain.model.OrderStatus.*
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.domain.model.TableInfo
import com.munchies.order.domain.model.TakeawayInfo
import com.munchies.order.domain.model.TakeawayOrder

/**
 * Factory object for creating different types of orders.
 *
 * This factory provides methods to create Delivery, Takeaway, and DineIn orders.
 * It validates the order items and the provided information before creating the order.
 */
object OrderFactory {

  /**
   * Creates a Delivery order with the specified parameters.
   *
   * @param id The unique identifier for the order.
   * @param restaurantId The unique identifier for the restaurant.
   * @param customerId The unique identifier for the customer.
   * @param items The list of order items.
   * @param info The delivery information.
   * @return An OrderCreationResult indicating success or failure of the order creation.
   */
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

  /**
   * Creates a Takeaway order with the specified parameters.
   *
   * @param id The unique identifier for the order.
   * @param restaurantId The unique identifier for the restaurant.
   * @param customerId The unique identifier for the customer.
   * @param items The list of order items.
   * @param info The takeaway information.
   * @return An OrderCreationResult indicating success or failure of the order creation.
   */
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

  /**
   * Creates a DineIn order with the specified parameters.
   *
   * @param id The unique identifier for the order.
   * @param restaurantId The unique identifier for the restaurant.
   * @param customerId The unique identifier for the customer.
   * @param items The list of order items.
   * @param info The table information.
   * @return An OrderCreationResult indicating success or failure of the order creation.
   */
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

  /**
   * Validates the list of order items.
   *
   * @param items The list of order items to validate.
   * @return An OrderCreationResult.Failure if validation fails, or null if validation succeeds.
   */
  private fun validate(items: List<OrderItem>): OrderCreationResult.Failure? =
    when (Order.validateItems(items)) {
      Order.ItemsValidationError.EmptyItems -> OrderCreationResult.Failure.EmptyItems
      Order.ItemsValidationError.InvalidItemQuantity ->
        OrderCreationResult.Failure.InvalidItemQuantity
      null -> null
    }
}
