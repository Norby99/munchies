package com.munchies.order.infrastructure.adapter.inbound.request

/**
 * Request data class for updating a takeaway order.
 *
 * @property orderId The unique identifier of the order to be updated.
 * @property customerId The unique identifier of the customer associated with the order.
 * @property pickupTime The updated pickup time for the order, represented as a timestamp in milliseconds.
 * @property customerName The updated name of the customer associated with the order.
 */
data class UpdateTakeawayOrderRequest(
  val orderId: String,
  val customerId: String,
  val pickupTime: String,
  val customerName: String,
)
