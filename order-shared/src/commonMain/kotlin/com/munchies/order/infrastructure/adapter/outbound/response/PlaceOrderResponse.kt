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
@SerialName("PlaceOrderResponse")
open class PlaceOrderResponse(
  val type: PlaceOrderResponseType,
  val order: OrderDto? = null,
  override val code: Int = 200,
) : WebResponse<OrderDto?>() {
  override val result: OrderDto? get() = order

  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun placeOrderResponseFromJson(json: String): PlaceOrderResponse = Json.decodeFromString(json)

@Serializable
enum class PlaceOrderResponseType {
  SUCCESS,
  INVALID_DATE,
  EMPTY_ITEMS,
  INVALID_ITEM_QUANTITY,
}
