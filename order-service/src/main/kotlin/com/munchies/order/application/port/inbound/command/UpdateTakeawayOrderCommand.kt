package com.munchies.order.application.port.inbound.command

data class UpdateTakeawayOrderCommand(
  val orderId: String,
  val customerId: String,
  val pickupTime: Long,
  val customerName: String,
)
