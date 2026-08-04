package com.munchies.order.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.WebResponse
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("UpdateOrderItemsResponse")
open class UpdateOrderItemsResponse(
  val type: UpdateOrderItemsResponseType,
  override val code: Int = 200,
) : WebResponse<UpdateOrderItemsResponseType>() {
  override val result: UpdateOrderItemsResponseType get() = type

  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun updateOrderItemsResponseFromJson(json: String): UpdateOrderItemsResponse =
  Json.decodeFromString(json)

@Serializable
enum class UpdateOrderItemsResponseType {
  SUCCESS,
  ORDER_NOT_FOUND,
  UNAUTHORIZED,
  EMPTY_ITEMS,
}
