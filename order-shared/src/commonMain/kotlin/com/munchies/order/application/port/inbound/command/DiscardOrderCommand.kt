package com.munchies.order.application.port.inbound.command

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderId

data class DiscardOrderCommand(
  val orderId: OrderId,
  val customerId: CustomerId,
)
