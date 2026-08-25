package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document

import io.micronaut.serde.annotation.Serdeable

@Serdeable
enum class ValidityType {
  ALWAYS,
  PERIOD,
  YEARLY,
  WEEKLY,
  HOURS,
}

@Serdeable
data class ValidityData(
  val type: ValidityType,
  val value: Map<String, String> = emptyMap(),
)
