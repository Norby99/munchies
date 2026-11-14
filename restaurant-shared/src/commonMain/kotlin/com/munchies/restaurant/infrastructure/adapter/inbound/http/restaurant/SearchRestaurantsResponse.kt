package com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant

import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.wireJson
import com.munchies.restaurant.infrastructure.adapter.dto.RestaurantDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Restaurants matching a customer search (`GET /restaurants/?q=`), sorted by name. The query
 * travels as the `q` query parameter, so this endpoint has no request body.
 */
@JsExport
@Serializable
@SerialName("SearchRestaurantsResponse")
class SearchRestaurantsResponse(
  override val result: Array<RestaurantDto> = emptyArray(),
  override val code: Int = 200,
) : WebResponse<Array<RestaurantDto>>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun searchRestaurantsResponseFromJson(json: String): SearchRestaurantsResponse =
  Json.decodeFromString(json)
