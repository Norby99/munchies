package com.munchies.order.infrastructure.adapter.inbound.request

data class UpdateDeliveryOrderRequest(
  val orderId: String,
  val customerId: String,
  val estimatedDeliveryTime: Long,
  val deliveryAddress: String,
  val bellName: String,
  val customerPhone: String,
)
