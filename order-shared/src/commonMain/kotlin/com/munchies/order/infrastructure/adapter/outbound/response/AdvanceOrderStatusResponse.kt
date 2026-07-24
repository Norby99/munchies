package com.munchies.order.infrastructure.adapter.outbound.response

import com.munchies.order.infrastructure.adapter.dto.OrderDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JsExport
@Serializable
@SerialName("AdvanceOrderStatusResponse")
class AdvanceOrderStatusResponse(
  val code: Int,
  val type: AdvanceOrderStatusResponseType,
)

@Serializable
enum class AdvanceOrderStatusResponseType {
  SUCCESS, ORDER_NOT_FOUND, INVALID_TRANSACTION
}
