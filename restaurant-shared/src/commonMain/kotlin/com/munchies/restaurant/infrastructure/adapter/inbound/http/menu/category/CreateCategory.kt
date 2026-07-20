package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category

import com.munchies.restaurant.infrastructure.adapter.dto.CategoryDto
import com.munchies.restaurant.infrastructure.adapter.dto.VariationDto
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
class CreateCategoryRequest(
  val name: String,
  val variations: List<VariationDto> = emptyList(),
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun createCategoryRequestFromJson(json: String): CreateCategoryRequest = Json.decodeFromString(json)

@JsExport
@Serializable
class CreateCategoryResponse(
  val category: CategoryDto,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun createCategoryResponseFromJson(json: String): CreateCategoryResponse =
  Json.decodeFromString(json)
