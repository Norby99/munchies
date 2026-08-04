package com.munchies.order.infrastructure.adapter.inbound.request

import com.munchies.commons.infrastructure.adapter.AuthenticatedRequest
import com.munchies.commons.infrastructure.adapter.JsonEncodable
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Request data class for updating a takeaway order.
 *
 * @property orderId The unique identifier of the order to be updated.
 * @property customerId The unique identifier of the customer associated with the order.
 * @property pickupTime The updated pickup time for the order, represented as a timestamp in milliseconds.
 * @property customerName The updated name of the customer associated with the order.
 */
@JsExport
@Serializable
@SerialName("UpdateTakeawayOrderRequest")
data class UpdateTakeawayOrderRequest(
  val orderId: String,
  val customerId: String = "",
  val pickupTime: String,
  val customerName: String,
) : AuthenticatedRequest<UpdateTakeawayOrderRequest>, JsonEncodable() {
  override fun toJson(): String = Json.encodeToString(this)
  override fun addId(userId: String): UpdateTakeawayOrderRequest = copy(customerId = userId)
}

@JsExport
fun updateTakeawayOrderRequestFromJson(json: String): UpdateTakeawayOrderRequest =
  Json.decodeFromString(json)
