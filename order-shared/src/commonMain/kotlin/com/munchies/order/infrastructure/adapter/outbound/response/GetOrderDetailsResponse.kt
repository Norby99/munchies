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
@SerialName("GetOrderDetailsResponse")
open class GetOrderDetailsResponse(
  val type: GetOrderDetailsResponseType,
  val order: OrderDto? = null,
  override val code: Int = 200,
) : WebResponse<OrderDto?>() {
  override val result: OrderDto? get() = order

  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun getOrderDetailsResponseFromJson(json: String): GetOrderDetailsResponse =
  Json.decodeFromString(json)

@Serializable
enum class GetOrderDetailsResponseType {
  SUCCESS,
  ORDER_NOT_FOUND,
}
