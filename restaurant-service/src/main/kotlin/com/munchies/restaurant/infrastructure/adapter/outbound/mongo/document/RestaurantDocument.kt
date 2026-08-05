package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.time.LocalDateTime

@MappedEntity
data class RestaurantDocument(
  @field:Id
  val id: String,
  val managerId: String,
  val name: String,
  val address: String,
  val phone: String,
  val email: String,
  val createdAt: LocalDateTime,
  val updatedAt: LocalDateTime,
)
