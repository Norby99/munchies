package com.munchies.tablereservation.infrastructure.adapter.inbound.request

import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Request to cancel a reservation.
 *
 * @property customerId The requesting customer, filled by the gateway from the authentication
 * token ([addId]): only the customer who made a reservation can cancel it.
 */
@JsExport
@Serializable
data class CancelTableReservationRequest(
  val customerId: String = "",
) {
  fun toJson(): String = wireJson.encodeToString(this)

  /** See [ReserveTableRequest.addId]. */
  fun addId(userId: String): CancelTableReservationRequest = copy(customerId = userId)
}

@JsExport
fun cancelTableReservationRequestFromJson(json: String): CancelTableReservationRequest =
  Json.decodeFromString(json)
