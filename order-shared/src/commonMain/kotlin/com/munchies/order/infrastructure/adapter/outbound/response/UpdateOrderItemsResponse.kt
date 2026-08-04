package com.munchies.order.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.WebResponse
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("UpdateOrderItemsResponse")
open class UpdateOrderItemsResponse(
  override val result: String = "Order items updated successfully",
  override val code: Int = 200,
) : WebResponse<String>() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun updateOrderItemsResponseFromJson(json: String): UpdateOrderItemsResponse =
  Json.decodeFromString(json)
