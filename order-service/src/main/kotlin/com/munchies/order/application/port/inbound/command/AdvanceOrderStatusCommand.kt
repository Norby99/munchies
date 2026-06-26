package com.munchies.order.application.port.inbound.command

import com.munchies.order.domain.model.OrderId

/**
 * Command to advance the status of an order to the next stage in the order processing workflow.
 */
data class AdvanceOrderStatusCommand(val orderId: OrderId)
