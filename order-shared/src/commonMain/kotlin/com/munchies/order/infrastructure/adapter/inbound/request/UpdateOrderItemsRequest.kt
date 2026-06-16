package com.munchies.order.infrastructure.adapter.inbound.request

import com.munchies.order.infrastructure.adapter.dto.OrderItemDto

data class UpdateOrderItemsRequest(
  val orderId: String,
  val customerId: String,
  val items: List<OrderItemDto>,
)
