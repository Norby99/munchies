package com.munchies.order.infrastructure.adapter.outbound.response

import com.munchies.order.infrastructure.adapter.dto.OrderDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JsExport
@Serializable
@SerialName("PlaceOrderResponse")
class PlaceOrderResponse(
  val code: Int,
  val type: PlaceOrderResponseType,
  val order: OrderDto? = null,
)

@Serializable
enum class PlaceOrderResponseType {
  SUCCESS, INVALID_DATE, EMPTY_ITEMS, INVALID_ITEM_QUANTITY
}
