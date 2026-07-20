package com.munchies.restaurant.infrastructure.adapter.dto

import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
class VariationOptionDto(
  val name: String,
  val additionalPrice: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
@Serializable
class VariationDto(
  val name: String,
  val options: Array<VariationOptionDto>,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
@Serializable
class MenuSummaryDto(
  val id: String,
  val name: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
@Serializable
class MenuItemDto(
  val id: String,
  val name: String,
  val description: String,
  val price: String,
  val variations: Array<VariationDto> = emptyArray(),
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
@Serializable
class CategoryDto(
  val id: String,
  val name: String,
  val items: Array<MenuItemDto>,
  val variations: Array<VariationDto> = emptyArray(),
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
@Serializable
class MenuDto(
  val id: String,
  val name: String,
  val categories: Array<CategoryDto>,
  val validity: ValidityDto,
) {
  fun toJson(): String = Json.encodeToString(this)
}
