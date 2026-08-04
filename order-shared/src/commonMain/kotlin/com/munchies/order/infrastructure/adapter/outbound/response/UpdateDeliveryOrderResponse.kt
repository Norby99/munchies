package com.munchies.order.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.WebResponse
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("UpdateDeliveryOrderResponse")
open class UpdateDeliveryOrderResponse(
  val type: UpdateDeliveryOrderResponseType,
  override val code: Int = 200,
) : WebResponse<UpdateDeliveryOrderResponseType>() {
  override val result: UpdateDeliveryOrderResponseType get() = type

  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun updateDeliveryOrderResponseFromJson(json: String): UpdateDeliveryOrderResponse =
  Json.decodeFromString(json)

@Serializable
enum class UpdateDeliveryOrderResponseType {
  SUCCESS,
  ORDER_NOT_FOUND,
  UNAUTHORIZED,
  INVALID_DATE,
}
