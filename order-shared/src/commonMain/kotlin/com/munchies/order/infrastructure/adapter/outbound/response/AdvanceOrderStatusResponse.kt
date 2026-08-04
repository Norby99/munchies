package com.munchies.order.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.WebResponse
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("AdvanceOrderStatusResponse")
open class AdvanceOrderStatusResponse(
  val type: AdvanceOrderStatusResponseType,
  override val code: Int = 200,
) : WebResponse<AdvanceOrderStatusResponseType>() {
  override val result: AdvanceOrderStatusResponseType get() = type

  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun advanceOrderStatusResponseFromJson(json: String): AdvanceOrderStatusResponse =
  Json.decodeFromString(json)

@Serializable
enum class AdvanceOrderStatusResponseType {
  SUCCESS,
  ORDER_NOT_FOUND,
  INVALID_TRANSACTION,
}
