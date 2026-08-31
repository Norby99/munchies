package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem

import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("DeleteMenuItemResponse")
open class DeleteMenuItemResponse(
  override val result: String,
  override val code: Int = 200,
) : WebResponse<String>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun deleteMenuItemResponseFromJson(json: String): DeleteMenuItemResponse =
  Json.decodeFromString(json)
