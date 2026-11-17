package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document

import io.micronaut.serde.annotation.Serdeable

/** Mongo form of a delivery zone: its boundary is the polygon's outer ring, not closed. */
@Serdeable
data class DeliveryZoneData(
  val id: String,
  val name: String,
  val boundary: List<GeoPointData>,
)
