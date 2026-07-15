package com.munchies.order.infrastructure.adapter.inbound.request

/**
 * Request data class for discarding an order.
 *
 * @property orderId The unique identifier of the order to be discarded.
 */
data class DiscardOrderRequest(val orderId: String)
