package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document

import io.micronaut.serde.annotation.Serdeable

/** Mongo form of a cook station. */
@Serdeable
data class CookStationData(
  val name: String,
  val places: Int,
)

/** Mongo form of the kitchen setup. The route is stored by its enum name, e.g. "PREP_THEN_COOK". */
@Serdeable
data class KitchenSetupData(
  val route: String,
  val prepWorkers: Int,
  val cookStations: List<CookStationData>,
  val prepMinutes: Double,
  val loadingMinutes: Double,
  val cookMinutes: Double,
)
