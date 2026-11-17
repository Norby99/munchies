package com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant

import com.munchies.commons.infrastructure.adapter.AuthenticatedRequest
import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.wireJson
import com.munchies.restaurant.infrastructure.adapter.dto.GeoPointDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("GetRestaurantLocationResponse")
class GetRestaurantLocationResponse(
  override val result: GeoPointDto,
  override val code: Int = 200,
) : WebResponse<GeoPointDto>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun getRestaurantLocationResponseFromJson(json: String): GetRestaurantLocationResponse =
  Json.decodeFromString(json)

@JsExport
@Serializable
@SerialName("UpdateRestaurantLocationRequest")
data class UpdateRestaurantLocationRequest(
  val managerId: String = "",
  val lat: Double,
  val lng: Double,
) : AuthenticatedRequest<UpdateRestaurantLocationRequest>, JsonEncodable() {
  override fun toJson(): String = wireJson.encodeToString(this)
  override fun addId(userId: String): UpdateRestaurantLocationRequest = copy(managerId = userId)
}

@JsExport
fun updateRestaurantLocationRequestFromJson(json: String): UpdateRestaurantLocationRequest =
  Json.decodeFromString(json)

@JsExport
@Serializable
@SerialName("UpdateRestaurantLocationResponse")
class UpdateRestaurantLocationResponse(
  override val result: GeoPointDto,
  override val code: Int = 200,
) : WebResponse<GeoPointDto>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun updateRestaurantLocationResponseFromJson(json: String): UpdateRestaurantLocationResponse =
  Json.decodeFromString(json)
