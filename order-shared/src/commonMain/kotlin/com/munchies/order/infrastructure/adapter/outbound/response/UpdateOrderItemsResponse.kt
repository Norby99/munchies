package com.munchies.order.infrastructure.adapter.outbound.response

import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JsExport
@Serializable
@SerialName("UpdateOrderItemsResponse")
class UpdateOrderItemsResponse(
  val code: Int,
  val type: UpdateOrderItemsResponseType,
)

@Serializable
enum class UpdateOrderItemsResponseType {
  SUCCESS,
  ORDER_NOT_FOUND,
  UNAUTHORIZED,
  EMPTY_ITEMS,
}
