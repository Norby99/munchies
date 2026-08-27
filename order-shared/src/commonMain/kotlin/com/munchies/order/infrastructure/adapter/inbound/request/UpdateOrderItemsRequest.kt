package com.munchies.order.infrastructure.adapter.inbound.request

import com.munchies.commons.infrastructure.adapter.AuthenticatedRequest
import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.wireJson
import com.munchies.order.infrastructure.adapter.dto.OrderItemDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Request data class for updating the items of an order.
 *
 * @property orderId The unique identifier of the order whose items are to be updated.
 * @property customerId The unique identifier of the customer associated with the order.
 * @property items The list of updated order items.
 */
@JsExport
@Serializable
@SerialName("UpdateOrderItemsRequest")
data class UpdateOrderItemsRequest(
  val orderId: String,
  val customerId: String = "",
  val items: List<OrderItemDto>,
) : AuthenticatedRequest<UpdateOrderItemsRequest>, JsonEncodable() {
  override fun toJson(): String = wireJson.encodeToString(this)
  override fun addId(userId: String): UpdateOrderItemsRequest = copy(customerId = userId)
}

@JsExport
fun updateOrderItemsRequestFromJson(json: String): UpdateOrderItemsRequest =
  Json.decodeFromString(json)
