package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu

import com.munchies.commons.infrastructure.adapter.WebResponse
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("DeleteMenuResponse")
open class DeleteMenuResponse(
  override val result: String,
  override val code: Int = 200,
) : WebResponse<String>() {
  val menuId: String get() = result
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun deleteMenuResponseFromJson(json: String): DeleteMenuResponse = Json.decodeFromString(json)
