package com.munchies.order.application.port.inbound.command

import com.munchies.order.domain.model.OrderId

/**
 * Command to retrieve the details of a specific order.
 *
 * @property orderId The unique identifier of the order for which details are being requested.
 */
data class GetOrderDetailsCommand(val orderId: OrderId)
