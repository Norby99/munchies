package com.munchies.order.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.WebResponse
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("PayOrderResponse")
open class PayOrderResponse(
  override val result: String,
  override val code: Int,
) : WebResponse<String>() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun payOrderResponseFromJson(json: String): PayOrderResponse = Json.decodeFromString(json)
