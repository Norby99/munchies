package com.munchies.order.application.port.inbound.command

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.RestaurantId

/**
 * Command for retrieving orders based on specific criteria.
 *
 * @property restaurantId The ID of the restaurant to filter orders by (optional).
 * @property customerId The ID of the customer to filter orders by (optional).
 * @property orderStatus The status of the orders to filter by (optional).
 */
data class GetOrdersCommand(
  val restaurantId: RestaurantId?,
  val customerId: CustomerId?,
  val orderStatus: OrderStatus?,
)
