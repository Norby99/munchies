package com.munchies.order.application.port.inbound.command

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderId

/**
 * Command to discard an order. This command is used when a customer wants to cancel their order
 * before it is processed.
 *
 * @property orderId The order to discard.
 * @property customerId The caller, set by the gateway from the session: the order must be theirs.
 * Null only for trusted internal callers acting for no customer.
 */
data class DiscardOrderCommand(val orderId: OrderId, val customerId: CustomerId? = null)
