package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category

import com.munchies.restaurant.infrastructure.adapter.dto.CategoryDto
import com.munchies.restaurant.infrastructure.adapter.dto.VariationDto
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
class UpdateCategoryRequest(
  val name: String,
  val variations: List<VariationDto> = emptyList(),
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun updateCategoryRequestFromJson(json: String): UpdateCategoryRequest = Json.decodeFromString(json)

@JsExport
@Serializable
class UpdateCategoryResponse(
  val category: CategoryDto,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun updateCategoryResponseFromJson(json: String): UpdateCategoryResponse =
  Json.decodeFromString(json)
