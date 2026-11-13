package com.munchies.tablereservation.infrastructure.adapter.inbound.request

import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Request to reserve a table at a restaurant.
 *
 * @property restaurantId The restaurant to reserve a table at.
 * @property customerId The reserving customer. Clients leave it empty: the gateway fills it from
 * the authentication token ([addId]).
 * @property start When the customer arrives, as an ISO-8601 instant. The slot length is fixed by
 * the service.
 * @property numberOfGuests How many people the table is for.
 */
@JsExport
@Serializable
data class ReserveTableRequest(
  val restaurantId: String,
  val customerId: String = "",
  val start: String,
  val numberOfGuests: Int,
) {
  fun toJson(): String = wireJson.encodeToString(this)

  /**
   * Same contract as `AuthenticatedRequest.addId`, without implementing the interface: in the JS
   * typings that interface becomes a `unique symbol` marker, which the service's OpenAPI
   * generator (tsoa) cannot parse.
   */
  fun addId(userId: String): ReserveTableRequest = copy(customerId = userId)
}

@JsExport
fun reserveTableRequestFromJson(json: String): ReserveTableRequest = Json.decodeFromString(json)
