package com.munchies.order.infrastructure.adapter.outbound.response

import com.munchies.order.infrastructure.adapter.dto.OrderDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JsExport
@Serializable
@SerialName("DiscardOrderResponse")
class DiscardOrderResponse(
  val code: Int,
  val type: DiscardOrderResponseType,
)

@Serializable
enum class DiscardOrderResponseType {
  SUCCESS, ORDER_NOT_FOUND, ORDER_NOT_CANCELLABLE
}
