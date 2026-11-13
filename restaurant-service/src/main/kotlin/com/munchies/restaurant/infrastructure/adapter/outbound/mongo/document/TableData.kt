package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document

import io.micronaut.serde.annotation.Serdeable

/** Mongo form of a table of the restaurant's dining room. */
@Serdeable
data class TableData(
  val number: Int,
  val seats: Int,
)
