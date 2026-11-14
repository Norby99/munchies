package com.munchies.order.application.port.inbound.command

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.RequestedItem

/**
 * Command to update the items of an existing order.
 *
 * @property orderId The unique identifier of the order to be updated.
 * @property customerId The unique identifier of the customer who placed the order.
 * @property items The new list of requested items; they are priced against the restaurant's
 * menus by the use case.
 */
data class UpdateOrderItemsCommand(
  val orderId: OrderId,
  val customerId: CustomerId,
  val items: List<RequestedItem>,
)
