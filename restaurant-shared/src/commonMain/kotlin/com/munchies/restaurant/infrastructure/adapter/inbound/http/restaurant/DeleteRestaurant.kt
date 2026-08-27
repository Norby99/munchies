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
@SerialName("DeleteRestaurantRequest")
data class DeleteRestaurantRequest(
  val managerId: String = "",
) : AuthenticatedRequest<DeleteRestaurantRequest>, JsonEncodable() {
  override fun toJson(): String = wireJson.encodeToString(this)
  override fun addId(userId: String): DeleteRestaurantRequest = copy(managerId = userId)
}

@JsExport
fun deleteRestaurantRequestFromJson(json: String): DeleteRestaurantRequest =
  Json.decodeFromString(json)

@JsExport
@Serializable
@SerialName("DeleteRestaurantResponse")
open class DeleteRestaurantResponse(
  override val result: String,
  override val code: Int = 200,
) : WebResponse<String>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun deleteRestaurantResponseFromJson(json: String): DeleteRestaurantResponse =
  Json.decodeFromString(json)
