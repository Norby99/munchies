package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category

import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("DeleteCategoryResponse")
class DeleteCategoryResponse(
  override val result: String,
  override val code: Int = 200,
) : WebResponse<String>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun deleteCategoryResponseFromJson(json: String): DeleteCategoryResponse =
  Json.decodeFromString(json)
