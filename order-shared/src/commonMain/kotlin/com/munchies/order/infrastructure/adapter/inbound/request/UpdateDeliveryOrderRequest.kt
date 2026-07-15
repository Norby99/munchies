package com.munchies.order.infrastructure.adapter.inbound.request

/**
 * Request data class for updating the details of a delivery order.
 *
 * @property orderId The unique identifier of the order to be updated.
 * @property customerId The unique identifier of the customer associated with the order.
 * @property estimatedDeliveryTime The updated estimated delivery time for the order.
 * @property deliveryAddress The updated delivery address for the order.
 * @property bellName The updated name on the doorbell for the delivery.
 * @property customerPhone The updated phone number of the customer for the delivery.
 */
data class UpdateDeliveryOrderRequest(
  val orderId: String,
  val customerId: String,
  val estimatedDeliveryTime: Long,
  val deliveryAddress: String,
  val bellName: String,
  val customerPhone: String,
)
