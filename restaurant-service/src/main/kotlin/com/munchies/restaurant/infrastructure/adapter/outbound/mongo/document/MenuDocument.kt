package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity

@MappedEntity
data class MenuDocument(
  @field:Id
  val id: String,
  val restaurantId: String,
  val name: String,
  val validity: String,
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
  val validity: String,
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
