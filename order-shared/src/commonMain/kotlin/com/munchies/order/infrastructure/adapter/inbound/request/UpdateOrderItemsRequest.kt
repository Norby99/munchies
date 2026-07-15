package com.munchies.order.infrastructure.adapter.inbound.request

import com.munchies.order.infrastructure.adapter.dto.OrderItemDto

/**
 * Request data class for updating the items of an order.
 *
 * @property orderId The unique identifier of the order whose items are to be updated.
 * @property customerId The unique identifier of the customer associated with the order.
 * @property items The list of updated order items.
 */
data class UpdateOrderItemsRequest(
  val orderId: String,
  val customerId: String,
  val items: List<OrderItemDto>,
)
