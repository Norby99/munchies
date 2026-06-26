package com.munchies.order.infrastructure.adapter.inbound.request

data class UpdateTakeawayOrderRequest(
  val orderId: String,
  val customerId: String,
  val pickupTime: Long,
  val customerName: String,
)
