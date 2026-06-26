package com.munchies.order.application.port.inbound.command

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderId

/**
 * Command to update an existing takeaway order.
 *
 * This command contains the necessary information to identify the order and update its details,
 * such as the pickup time and customer name.
 *
 * @property orderId The unique identifier of the order to be updated.
 * @property customerId The unique identifier of the customer associated with the order.
 * @property pickupTime The new pickup time for the takeaway order, represented as a timestamp in
 * milliseconds.
 * @property customerName The new name of the customer for the takeaway order.
 */
data class UpdateTakeawayOrderCommand(
  val orderId: OrderId,
  val customerId: CustomerId,
  val pickupTime: Long,
  val customerName: String,
)
