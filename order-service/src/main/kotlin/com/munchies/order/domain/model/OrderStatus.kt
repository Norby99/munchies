package com.munchies.order.domain.model

/**
 * This enum represents the status of an order in the system.
 */
enum class OrderStatus {
  PENDING,
  PREPARING,
  READY,
  ON_THE_WAY,
  COMPLETED,
  CANCELLED,
}
