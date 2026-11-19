package com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant

import com.munchies.commons.infrastructure.adapter.AuthenticatedRequest
import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.wireJson
import com.munchies.restaurant.infrastructure.adapter.dto.DeliveryZoneDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * The delivery zones of a restaurant; empty until the manager draws them, which means the
 * scheduler batches deliveries on detour and trip budget alone.
 */
@JsExport
@Serializable
@SerialName("GetDeliveryZonesResponse")
class GetDeliveryZonesResponse(
  override val result: Array<DeliveryZoneDto> = emptyArray(),
  override val code: Int = 200,
) : WebResponse<Array<DeliveryZoneDto>>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun getDeliveryZonesResponseFromJson(json: String): GetDeliveryZonesResponse =
  Json.decodeFromString(json)

/**
 * Replaces every delivery zone of a restaurant with [zones]; an empty array removes them all.
 *
 * @property managerId The requesting manager. Clients leave it empty: the gateway fills it from
 * the authentication token ([addId]).
 */
@JsExport
@Serializable
@SerialName("UpdateDeliveryZonesRequest")
class UpdateDeliveryZonesRequest(
  val managerId: String = "",
  val zones: Array<DeliveryZoneDto> = emptyArray(),
) : AuthenticatedRequest<UpdateDeliveryZonesRequest>, JsonEncodable() {
  override fun toJson(): String = wireJson.encodeToString(this)
  override fun addId(userId: String): UpdateDeliveryZonesRequest =
    UpdateDeliveryZonesRequest(userId, zones)
}

@JsExport
fun updateDeliveryZonesRequestFromJson(json: String): UpdateDeliveryZonesRequest =
  Json.decodeFromString(json)

/** The zones as saved, with the ids generated for the new ones. */
@JsExport
@Serializable
@SerialName("UpdateDeliveryZonesResponse")
class UpdateDeliveryZonesResponse(
  override val result: Array<DeliveryZoneDto> = emptyArray(),
  override val code: Int = 200,
) : WebResponse<Array<DeliveryZoneDto>>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun updateDeliveryZonesResponseFromJson(json: String): UpdateDeliveryZonesResponse =
  Json.decodeFromString(json)
