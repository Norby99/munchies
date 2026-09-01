package com.munchies.order.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.wireJson
import com.munchies.order.infrastructure.adapter.dto.OrderDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("PlaceOrderResponse")
open class PlaceOrderResponse(
  override val result: OrderDto,
  override val code: Int = 200,
) : WebResponse<OrderDto>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun placeOrderResponseFromJson(json: String): PlaceOrderResponse = Json.decodeFromString(json)
