package com.munchies.order.application.port.inbound.command

import com.munchies.order.domain.model.OrderId

/**
 * Command to initiate the payment process for a specific order.
 *
 * @property orderId The unique identifier of the order to be paid.
 */
data class PayOrderCommand(val orderId: OrderId)
