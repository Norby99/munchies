package com.munchies.order.infrastructure.adapter.inbound.request

data class DiscardOrderRequest(
  val orderId: String,
  val customerId: String,
)
