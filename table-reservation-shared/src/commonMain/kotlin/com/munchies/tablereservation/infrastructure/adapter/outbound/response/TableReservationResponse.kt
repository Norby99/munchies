package com.munchies.tablereservation.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.wireJson
import com.munchies.tablereservation.infrastructure.adapter.dto.TableReservationDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** A single reservation: the answer to reserving, reading, modifying and canceling one. */
@JsExport
@Serializable
@SerialName("TableReservationResponse")
class TableReservationResponse(
  override val result: TableReservationDto,
  override val code: Int = 200,
) : WebResponse<TableReservationDto>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun tableReservationResponseFromJson(json: String): TableReservationResponse =
  Json.decodeFromString(json)

/** A list of reservations, ordered by the start of their time slot. */
@JsExport
@Serializable
@SerialName("TableReservationsResponse")
class TableReservationsResponse(
  override val result: Array<TableReservationDto> = emptyArray(),
  override val code: Int = 200,
) : WebResponse<Array<TableReservationDto>>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun tableReservationsResponseFromJson(json: String): TableReservationsResponse =
  Json.decodeFromString(json)
