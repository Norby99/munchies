package com.munchies.order.application.port.inbound.command

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderItem

/**
 * Command to update the items of an existing order.
 *
 * @property orderId The unique identifier of the order to be updated.
 * @property customerId The unique identifier of the customer who placed the order.
 * @property items The new list of items to be associated with the order.
 */
data class UpdateOrderItemsCommand(
  val orderId: OrderId,
  val customerId: CustomerId,
  val items: List<OrderItem>,
)
