package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class GeoPointData(
  val lat: Double,
  val lng: Double,
)
