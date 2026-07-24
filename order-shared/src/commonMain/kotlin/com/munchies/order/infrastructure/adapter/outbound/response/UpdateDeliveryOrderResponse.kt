package com.munchies.order.infrastructure.adapter.outbound.response

import com.munchies.order.infrastructure.adapter.dto.OrderDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JsExport
@Serializable
@SerialName("UpdateDeliveryOrderResponse")
class UpdateDeliveryOrderResponse(
  val code: Int,
  val type: UpdateDeliveryOrderResponseType,
)

@Serializable
enum class UpdateDeliveryOrderResponseType {
  SUCCESS, ORDER_NOT_FOUND, UNAUTHORIZED, INVALID_DATE
}
