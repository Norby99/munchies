package com.munchies.order.application.port.inbound.command

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderId

data class UpdateDeliveryOrderCommand(
  val orderId: OrderId,
  val customerId: CustomerId,
  val estimatedDeliveryTime: Long,
  val deliveryAddress: String,
  val bellName: String,
  val customerPhone: String,
)
