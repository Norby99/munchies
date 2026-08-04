package com.munchies.order.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.WebResponse
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("DiscardOrderResponse")
open class DiscardOrderResponse(
  val type: DiscardOrderResponseType,
  override val code: Int = 200,
) : WebResponse<DiscardOrderResponseType>() {
  override val result: DiscardOrderResponseType get() = type

  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun discardOrderResponseFromJson(json: String): DiscardOrderResponse = Json.decodeFromString(json)

@Serializable
enum class DiscardOrderResponseType {
  SUCCESS,
  ORDER_NOT_FOUND,
  ORDER_NOT_CANCELLABLE,
}
