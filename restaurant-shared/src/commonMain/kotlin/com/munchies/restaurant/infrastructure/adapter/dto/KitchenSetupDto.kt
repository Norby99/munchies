package com.munchies.restaurant.infrastructure.adapter.dto

import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

/**
 * Wire form of a cook station: an oven, a grill, anything that cooks several items at once.
 *
 * @property name The label the staff use for it, unique within the kitchen.
 * @property places How many items it cooks at the same time.
 */
@JsExport
@Serializable
class CookStationDto(
  val name: String,
  val places: Int,
) {
  fun toJson(): String = wireJson.encodeToString(this)
}

/**
 * Wire form of a restaurant's kitchen, as the staff describe it: input for the capacity agent of
 * scheduler-service. Times are mean minutes for one item.
 *
 * @property route `PREP_THEN_COOK` (e.g. a pizza: topped, then baked) or `COOK_THEN_PREP` (e.g. a
 * burger: grilled, then assembled).
 * @property prepWorkers How many people prepare items, one item each at a time.
 * @property cookStations Where items are cooked; at least one.
 * @property prepMinutes Minutes one worker takes to prepare one item.
 * @property loadingMinutes Minutes a worker takes to put one item into a cook station.
 * @property cookMinutes Minutes one item cooks.
 */
@JsExport
@Serializable
class KitchenSetupDto(
  val route: String,
  val prepWorkers: Int,
  val cookStations: Array<CookStationDto>,
  val prepMinutes: Double,
  val loadingMinutes: Double,
  val cookMinutes: Double,
) {
  fun toJson(): String = wireJson.encodeToString(this)
}
