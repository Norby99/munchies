package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category

import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
data class DeleteCategoryResponse(
  val categoryId: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun deleteCategoryResponseFromJson(json: String): DeleteCategoryResponse = Json.decodeFromString(
  json,
)
