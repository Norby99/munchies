package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category

import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.restaurant.infrastructure.adapter.dto.CategoryDto
import com.munchies.restaurant.infrastructure.adapter.dto.VariationDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("UpdateCategoryRequest")
data class UpdateCategoryRequest(
  val name: String,
  val variations: Array<VariationDto> = emptyArray(),
) : JsonEncodable() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun updateCategoryRequestFromJson(json: String): UpdateCategoryRequest = Json.decodeFromString(json)

@JsExport
@Serializable
@SerialName("UpdateCategoryResponse")
open class UpdateCategoryResponse(
  override val result: CategoryDto,
  override val code: Int = 200,
) : WebResponse<CategoryDto>() {
  val category: CategoryDto get() = result
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun updateCategoryResponseFromJson(json: String): UpdateCategoryResponse =
  Json.decodeFromString(json)
