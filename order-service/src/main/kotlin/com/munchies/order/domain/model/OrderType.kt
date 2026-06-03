package com.munchies.order.domain.model

/**
 * This enum represents the type of an order in the system. It can be one of three types:
 * - DELIVERY: The order will be delivered to the customer's address.
 * - TAKEAWAY: The customer will pick up the order from the restaurant.
 * - DINE_IN: The customer will dine in at the restaurant.
 */
enum class OrderType {
  DELIVERY,
  TAKEAWAY,
  DINE_IN,
}
