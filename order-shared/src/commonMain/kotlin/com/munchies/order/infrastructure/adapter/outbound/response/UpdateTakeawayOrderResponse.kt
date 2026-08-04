package com.munchies.order.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.WebResponse
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("UpdateTakeawayOrderResponse")
open class UpdateTakeawayOrderResponse(
  val type: UpdateTakeawayOrderResponseType,
  override val code: Int = 200,
) : WebResponse<UpdateTakeawayOrderResponseType>() {
  override val result: UpdateTakeawayOrderResponseType get() = type

  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun updateTakeawayOrderResponseFromJson(json: String): UpdateTakeawayOrderResponse =
  Json.decodeFromString(json)

@Serializable
enum class UpdateTakeawayOrderResponseType {
  SUCCESS,
  ORDER_NOT_FOUND,
  UNAUTHORIZED,
  INVALID_DATE,
}
