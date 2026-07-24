package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity

@MappedEntity
data class MenuDocument(
  @field:Id
  val id: String,
  val restaurantId: String,
  val name: String,
  val validity: ValidityDocument,
  val categories: List<CategoryDocument>,
)

@MappedEntity
data class CategoryDocument(
  val id: String,
  val name: String,
  val items: List<MenuItemDocument>,
  val variations: List<VariationDocument>,
)

@MappedEntity
data class MenuItemDocument(
  val id: String,
  val name: String,
  val description: String,
  val price: String,
  val validity: ValidityDocument,
  val variations: List<VariationDocument>,
)

@MappedEntity
data class VariationDocument(
  val name: String,
  val options: List<VariationOptionDocument>,
)

@MappedEntity
data class VariationOptionDocument(
  val name: String,
  val additionalPrice: String,
)

@MappedEntity
data class ValidityDocument(
  val type: String,
  val start: String?,
  val end: String?,
  val startMonth: Int?,
  val startDay: Int?,
  val endMonth: Int?,
  val endDay: Int?,
  val days: List<Int>?,
  val startHour: String?,
  val endHour: String?,
  val first: ValidityDocument?,
  val second: ValidityDocument?,
) {
  companion object {
    val always = ValidityDocument(
      type = "always",
      start = null, end = null,
      startMonth = null, startDay = null, endMonth = null, endDay = null,
      days = null, startHour = null, endHour = null,
      first = null, second = null,
    )
  }
}
