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
@SerialName("CreateCategoryRequest")
data class CreateCategoryRequest(
  val name: String,
  val variations: Array<VariationDto> = emptyArray(),
) : JsonEncodable() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun createCategoryRequestFromJson(json: String): CreateCategoryRequest = Json.decodeFromString(json)

@JsExport
@Serializable
@SerialName("CreateCategoryResponse")
open class CreateCategoryResponse(
  override val result: CategoryDto,
  override val code: Int = 201,
) : WebResponse<CategoryDto>() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun createCategoryResponseFromJson(json: String): CreateCategoryResponse =
  Json.decodeFromString(json)
