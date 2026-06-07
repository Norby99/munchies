package com.munchies.order.application.port.inbound.command

data class UpdateDeliveryOrderCommand(
  val orderId: String,
  val customerId: String,
  val estimatedDeliveryTime: Long,
  val deliveryAddress: String,
  val bellName: String,
  val customerPhone: String,
)
