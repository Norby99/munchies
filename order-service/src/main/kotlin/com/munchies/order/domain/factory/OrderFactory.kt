package com.munchies.order.domain.factory

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.DeliveryInfo
import com.munchies.order.domain.model.Order
import com.munchies.order.domain.model.OrderDetails
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderItem
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.domain.model.TableInfo
import com.munchies.order.domain.model.TakeawayInfo
import com.munchies.order.domain.model.event.OrderEvent
import com.munchies.order.domain.model.event.OrderPlaced

/**
 * Factory object for the event-sourced [Order] aggregate.
 *
 * It covers both ends of the aggregate's lifecycle:
 * - creation: the `create*` methods are the process step for a brand new order. They validate
 *   the input and return the [OrderPlaced] event that creates it, without building any state;
 * - rehydration: [fromHistory] rebuilds the current state of an existing order by replaying its
 *   event stream.
 */
object OrderFactory {

  /**
   * Validates a new Delivery order and returns its creation events.
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
  ): OrderCreationResult = validate(items)
    ?: if (!info.isValidTime()) {
      OrderCreationResult.Failure.InvalidDate
    } else {
      placed(id, restaurantId, customerId, items, info)
    }

  /**
   * Validates a new Takeaway order and returns its creation events.
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
  ): OrderCreationResult = validate(items)
    ?: if (!info.isValidTime()) {
      OrderCreationResult.Failure.InvalidDate
    } else {
      placed(id, restaurantId, customerId, items, info)
    }

  /**
   * Validates a new DineIn order and returns its creation events.
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
  ): OrderCreationResult = validate(items)
    ?: placed(id, restaurantId, customerId, items, info)

  /**
   * Rebuilds the current state of an order by replaying its event stream.
   * First event must be an [OrderPlaced].
   *
   * @param history The full, ordered event stream of the order.
   * @return The current state of the order, or `null` if the stream is empty (the order does not
   * exist) or does not start with an [OrderPlaced] event.
   */
  fun fromHistory(history: List<OrderEvent>): Order? {
    val placed = history.firstOrNull() as? OrderPlaced ?: return null
    return Order.from(placed).applyAll(history.drop(1))
  }

  private fun placed(
    id: OrderId,
    restaurantId: RestaurantId,
    customerId: CustomerId,
    items: List<OrderItem>,
    details: OrderDetails,
  ): OrderCreationResult =
    OrderCreationResult.Success(listOf(OrderPlaced(id, restaurantId, customerId, items, details)))

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
