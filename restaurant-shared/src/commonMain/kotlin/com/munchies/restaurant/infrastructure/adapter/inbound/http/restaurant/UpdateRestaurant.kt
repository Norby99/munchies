package com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant

import com.munchies.commons.infrastructure.adapter.AuthenticatedRequest
import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("UpdateRestaurantRequest")
data class UpdateRestaurantRequest(
  val managerId: String = "",
  val name: String,
  val address: String,
  val phone: String,
  val email: String,
) : AuthenticatedRequest<UpdateRestaurantRequest>, JsonEncodable() {
  override fun toJson(): String = wireJson.encodeToString(this)
  override fun addId(userId: String): UpdateRestaurantRequest = copy(managerId = userId)
}

@JsExport
fun updateRestaurantRequestFromJson(json: String): UpdateRestaurantRequest =
  Json.decodeFromString(json)

@JsExport
@Serializable
@SerialName("UpdateRestaurantResponse")
class UpdateRestaurantResponse(
  override val result: String,
  override val code: Int = 200,
) : WebResponse<String>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun updateRestaurantResponseFromJson(json: String): UpdateRestaurantResponse =
  Json.decodeFromString(json)
