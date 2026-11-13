package com.munchies.restaurant.infrastructure.adapter.dto

import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

/**
 * Wire form of a table of the restaurant's dining room.
 *
 * @property number The number the staff identifies the table with, unique within the restaurant.
 * @property seats How many guests the table can seat.
 */
@JsExport
@Serializable
class TableDto(
  val number: Int,
  val seats: Int,
) {
  fun toJson(): String = wireJson.encodeToString(this)
}
