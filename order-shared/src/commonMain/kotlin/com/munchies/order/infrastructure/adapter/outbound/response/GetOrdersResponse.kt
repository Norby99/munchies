package com.munchies.order.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.order.infrastructure.adapter.dto.OrderDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("GetOrdersResponse")
open class GetOrdersResponse(
  override val result: List<OrderDto>,
  override val code: Int,
) : WebResponse<List<OrderDto>>() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun getOrdersResponseFromJson(json: String): GetOrdersResponse = Json.decodeFromString(json)
