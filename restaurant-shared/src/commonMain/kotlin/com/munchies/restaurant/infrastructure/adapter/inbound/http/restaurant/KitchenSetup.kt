package com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant

import com.munchies.commons.infrastructure.adapter.AuthenticatedRequest
import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.wireJson
import com.munchies.restaurant.infrastructure.adapter.dto.CookStationDto
import com.munchies.restaurant.infrastructure.adapter.dto.KitchenSetupDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** The kitchen of a restaurant. A restaurant whose kitchen is not described yet answers 404. */
@JsExport
@Serializable
@SerialName("GetKitchenSetupResponse")
class GetKitchenSetupResponse(
  override val result: KitchenSetupDto,
  override val code: Int = 200,
) : WebResponse<KitchenSetupDto>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun getKitchenSetupResponseFromJson(json: String): GetKitchenSetupResponse =
  Json.decodeFromString(json)

/**
 * Replaces the kitchen description of a restaurant. Fields as in [KitchenSetupDto].
 *
 * @property managerId The requesting manager. Clients leave it empty: the gateway fills it from
 * the authentication token ([addId]).
 */
@JsExport
@Serializable
@SerialName("UpdateKitchenSetupRequest")
class UpdateKitchenSetupRequest(
  val managerId: String = "",
  val route: String,
  val prepWorkers: Int,
  val cookStations: Array<CookStationDto>,
  val prepMinutes: Double,
  val loadingMinutes: Double,
  val cookMinutes: Double,
) : AuthenticatedRequest<UpdateKitchenSetupRequest>, JsonEncodable() {
  override fun toJson(): String = wireJson.encodeToString(this)
  override fun addId(userId: String): UpdateKitchenSetupRequest = UpdateKitchenSetupRequest(
    managerId = userId,
    route = route,
    prepWorkers = prepWorkers,
    cookStations = cookStations,
    prepMinutes = prepMinutes,
    loadingMinutes = loadingMinutes,
    cookMinutes = cookMinutes,
  )
}

@JsExport
fun updateKitchenSetupRequestFromJson(json: String): UpdateKitchenSetupRequest =
  Json.decodeFromString(json)

/** The kitchen description as saved. */
@JsExport
@Serializable
@SerialName("UpdateKitchenSetupResponse")
class UpdateKitchenSetupResponse(
  override val result: KitchenSetupDto,
  override val code: Int = 200,
) : WebResponse<KitchenSetupDto>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun updateKitchenSetupResponseFromJson(json: String): UpdateKitchenSetupResponse =
  Json.decodeFromString(json)
