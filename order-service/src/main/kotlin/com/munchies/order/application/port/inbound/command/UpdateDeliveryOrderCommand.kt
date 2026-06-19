package com.munchies.order.application.port.inbound.command

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderId

/**
 * Command object for updating the details of a delivery order.
 *
 * This command encapsulates all the necessary information required to update a delivery order,
 * including the order ID, customer ID, estimated delivery time, delivery address, bell name, and
 * customer phone number.
 *
 * @property orderId The unique identifier of the order to be updated.
 * @property customerId The unique identifier of the customer associated with the order.
 * @property estimatedDeliveryTime The new estimated delivery time for the order, represented as a
 * timestamp in milliseconds.
 * @property deliveryAddress The new delivery address for the order.
 * @property bellName The new bell name associated with the delivery address.
 * @property customerPhone The new phone number of the customer for contact purposes.
 */
data class UpdateDeliveryOrderCommand(
  val orderId: OrderId,
  val customerId: CustomerId,
  val estimatedDeliveryTime: Long,
  val deliveryAddress: String,
  val bellName: String,
  val customerPhone: String,
)
