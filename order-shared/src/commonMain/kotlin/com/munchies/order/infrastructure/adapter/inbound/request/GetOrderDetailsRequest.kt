package com.munchies.order.infrastructure.adapter.inbound.request

/**
 * Request data class for retrieving the details of an order.
 *
 * @property orderId The unique identifier of the order whose details are to be retrieved.
 */
data class GetOrderDetailsRequest(val orderId: String)
