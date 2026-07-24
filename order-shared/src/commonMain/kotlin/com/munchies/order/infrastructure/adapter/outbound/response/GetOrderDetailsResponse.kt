package com.munchies.order.infrastructure.adapter.outbound.response

import com.munchies.order.infrastructure.adapter.dto.OrderDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JsExport
@Serializable
@SerialName("GetOrderDetailsResponse")
class GetOrderDetailsResponse(
  val code: Int,
  val type: GetOrderDetailsResponseType,
  val order: OrderDto? = null,
)

@Serializable
enum class GetOrderDetailsResponseType {
  SUCCESS, ORDER_NOT_FOUND
}
