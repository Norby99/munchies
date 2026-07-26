package com.munchies.order.infrastructure.adapter.outbound.response

import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JsExport
@Serializable
@SerialName("UpdateTakeawayOrderResponse")
class UpdateTakeawayOrderResponse(
  val code: Int,
  val type: UpdateTakeawayOrderResponseType,
)

@Serializable
enum class UpdateTakeawayOrderResponseType {
  SUCCESS,
  ORDER_NOT_FOUND,
  UNAUTHORIZED,
  INVALID_DATE,
}
