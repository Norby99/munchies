package com.munchies.order.application.port.inbound.command

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderId

/**
 * Command to retrieve the details of a specific order.
 *
 * @property orderId The unique identifier of the order for which details are being requested.
 * @property customerId The caller when it is a customer, set by the gateway from the session: the
 * order must be theirs. Null for managers and for internal callers (e.g. payment-service).
 */
data class GetOrderDetailsCommand(val orderId: OrderId, val customerId: CustomerId? = null)
