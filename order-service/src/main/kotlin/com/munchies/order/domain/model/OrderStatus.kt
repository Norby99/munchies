package com.munchies.order.domain.model

enum class OrderStatus {
  PENDING,
  CONFIRMED,
  PREPARING,
  READY_FOR_PICKUP,
  OUT_FOR_DELIVERY,
  DELIVERED,
  CANCELLED;

  fun canTransitionTo(next: OrderStatus): Boolean = when (this) {
    PENDING           -> next in setOf(CONFIRMED, CANCELLED)
    CONFIRMED         -> next in setOf(PREPARING, CANCELLED)
    PREPARING         -> next in setOf(READY_FOR_PICKUP, CANCELLED)
    READY_FOR_PICKUP  -> next in setOf(OUT_FOR_DELIVERY)
    OUT_FOR_DELIVERY  -> next in setOf(DELIVERED)
    DELIVERED         -> false
    CANCELLED         -> false
  }
}
