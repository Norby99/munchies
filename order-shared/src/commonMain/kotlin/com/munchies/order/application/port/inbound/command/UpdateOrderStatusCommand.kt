package com.munchies.order.application.port.inbound.command

data class UpdateOrderStatusCommand(
  val orderId: String,
  val status: String,
)
