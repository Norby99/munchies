package com.munchies.order.infrastructure.adapter.inbound.request

import com.munchies.commons.infrastructure.adapter.AuthenticatedRequest
import com.munchies.commons.infrastructure.adapter.JsonEncodable
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
@JsExport
@Serializable
@SerialName("UpdateDeliveryOrderRequest")
data class UpdateDeliveryOrderRequest(
  val orderId: String,
  val customerId: String = "",
  val estimatedDeliveryTime: String,
  val deliveryAddress: String,
  val bellName: String,
  val customerPhone: String,
) : AuthenticatedRequest<UpdateDeliveryOrderRequest>, JsonEncodable() {
  override fun toJson(): String = Json.encodeToString(this)
  override fun addId(userId: String): UpdateDeliveryOrderRequest = copy(customerId = userId)
}

@JsExport
fun updateDeliveryOrderRequestFromJson(json: String): UpdateDeliveryOrderRequest =
  Json.decodeFromString(json)
