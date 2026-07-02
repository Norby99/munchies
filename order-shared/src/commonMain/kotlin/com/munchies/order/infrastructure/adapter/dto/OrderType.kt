package com.munchies.order.infrastructure.adapter.dto

import kotlinx.serialization.Serializable

@Serializable
enum class OrderType {
  DELIVERY,
  TAKEAWAY,
  DINE_IN,
}
