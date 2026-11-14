package com.munchies.tablereservation.infrastructure.adapter.inbound.request

import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Request to move a reservation to another time and/or change its number of guests. Both values
 * are always sent: the ones that do not change repeat the current ones.
 *
 * @property customerId The requesting customer, filled by the gateway from the authentication
 * token ([addId]): only the customer who made a reservation can modify it.
 * @property start The new arrival time, as an ISO-8601 instant.
 * @property numberOfGuests The new number of guests.
 */
@JsExport
@Serializable
data class ModifyTableReservationRequest(
  val customerId: String = "",
  val start: String,
  val numberOfGuests: Int,
) {
  fun toJson(): String = wireJson.encodeToString(this)

  /** See [ReserveTableRequest.addId]. */
  fun addId(userId: String): ModifyTableReservationRequest = copy(customerId = userId)
}

@JsExport
fun modifyTableReservationRequestFromJson(json: String): ModifyTableReservationRequest =
  Json.decodeFromString(json)
