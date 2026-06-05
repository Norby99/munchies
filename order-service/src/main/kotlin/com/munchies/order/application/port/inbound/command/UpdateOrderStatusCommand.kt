package com.munchies.order.application.port.inbound.command

import com.munchies.order.domain.model.OrderStatus

data class UpdateOrderStatusCommand(val orderId: String, val status: OrderStatus)
