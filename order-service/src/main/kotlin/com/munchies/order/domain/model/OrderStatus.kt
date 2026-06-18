package com.munchies.order.domain.model

/**
 * Represents the status of an order in the system.
 *
 * The possible statuses are:
 * - PENDING: The order has been created but not yet processed.
 * - PREPARING: The order is being prepared by the restaurant.
 * - READY: The order is ready for pickup or delivery.
 * - ON_THE_WAY: The order is currently being delivered to the customer.
 * - COMPLETED: The order has been successfully completed and delivered.
 * - CANCELLED: The order has been canceled and will not be processed further.
 */
enum class OrderStatus {
  PENDING,
  PREPARING,
  READY,
  ON_THE_WAY,
  COMPLETED,
  CANCELLED,
}
