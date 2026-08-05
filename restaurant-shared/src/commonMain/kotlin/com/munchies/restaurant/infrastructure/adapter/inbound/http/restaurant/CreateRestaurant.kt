package com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant

import com.munchies.commons.infrastructure.adapter.AuthenticatedRequest
import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.WebResponse
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("CreateRestaurantRequest")
data class CreateRestaurantRequest(
  val managerId: String = "",
  val name: String,
  val address: String,
  val phone: String,
  val email: String,
) : AuthenticatedRequest<CreateRestaurantRequest>, JsonEncodable() {
  override fun toJson(): String = Json.encodeToString(this)
  override fun addId(userId: String): CreateRestaurantRequest = copy(managerId = userId)
}

@JsExport
fun createRestaurantRequestFromJson(json: String): CreateRestaurantRequest =
  Json.decodeFromString(json)

@JsExport
@Serializable
@SerialName("CreateRestaurantResponse")
open class CreateRestaurantResponse(
  override val result: String,
  override val code: Int = 201,
) : WebResponse<String>() {
  val restaurantId: String get() = result
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun createRestaurantResponseFromJson(json: String): CreateRestaurantResponse =
  Json.decodeFromString(json)
