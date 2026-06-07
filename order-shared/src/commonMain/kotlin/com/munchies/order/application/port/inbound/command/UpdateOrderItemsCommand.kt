package com.munchies.order.application.port.inbound.command

import com.munchies.order.infrastructure.adapter.dto.OrderItemDto

data class UpdateOrderItemsCommand(
  val orderId: String,
  val customerId: String,
  val items: List<OrderItemDto>,
)
