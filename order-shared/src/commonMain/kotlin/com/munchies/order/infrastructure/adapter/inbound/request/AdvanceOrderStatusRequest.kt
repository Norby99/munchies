package com.munchies.order.infrastructure.adapter.inbound.request

/**
 * Request data class for advancing the status of an order.
 *
 * @property orderId The unique identifier of the order whose status is to be advanced.
 */
data class AdvanceOrderStatusRequest(val orderId: String)
