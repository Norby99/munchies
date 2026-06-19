package com.munchies.order.application.port.inbound.command

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderId

/**
 * Command to discard an order. This command is used when a customer wants to cancel their order
 * before it is processed.
 */
data class DiscardOrderCommand(
  val orderId: OrderId,
  val customerId: CustomerId,
)
