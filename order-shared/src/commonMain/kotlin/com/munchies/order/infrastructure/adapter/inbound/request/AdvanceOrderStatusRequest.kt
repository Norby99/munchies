package com.munchies.order.infrastructure.adapter.inbound.request

import com.munchies.commons.infrastructure.adapter.JsonEncodable
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Request data class for advancing the status of an order.
 *
 * @property orderId The unique identifier of the order whose status is to be advanced.
 */
@JsExport
@Serializable
@SerialName("AdvanceOrderStatusRequest")
data class AdvanceOrderStatusRequest(val orderId: String) : JsonEncodable() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun advanceOrderStatusRequestFromJson(json: String): AdvanceOrderStatusRequest =
  Json.decodeFromString(json)
