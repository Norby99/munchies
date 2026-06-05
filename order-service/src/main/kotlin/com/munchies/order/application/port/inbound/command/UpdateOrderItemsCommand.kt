package com.munchies.order.application.port.inbound.command

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderItem

data class UpdateOrderItemsCommand(
  val orderId: OrderId,
  val customerId: CustomerId,
  val items: List<OrderItem>,
)
