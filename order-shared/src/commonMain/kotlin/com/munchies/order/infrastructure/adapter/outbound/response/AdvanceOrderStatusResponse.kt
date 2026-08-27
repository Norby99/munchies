package com.munchies.order.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("AdvanceOrderStatusResponse")
open class AdvanceOrderStatusResponse(
  override val result: String = "Order status advanced successfully",
  override val code: Int = 200,
) : WebResponse<String>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun advanceOrderStatusResponseFromJson(json: String): AdvanceOrderStatusResponse =
  Json.decodeFromString(json)
