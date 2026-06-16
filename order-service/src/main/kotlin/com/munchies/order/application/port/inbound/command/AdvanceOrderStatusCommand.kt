package com.munchies.order.application.port.inbound.command

import com.munchies.order.domain.model.OrderId

data class AdvanceOrderStatusCommand(val orderId: OrderId)
