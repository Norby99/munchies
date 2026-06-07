package com.munchies.order.application.port.inbound.command

data class DiscardOrderCommand(
  val orderId: String,
  val customerId: String,
)
