package com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant

import com.munchies.commons.infrastructure.adapter.AuthenticatedRequest
import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.wireJson
import com.munchies.restaurant.infrastructure.adapter.dto.TableDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** The tables of a restaurant; empty until the manager configures the dining room. */
@JsExport
@Serializable
@SerialName("GetTablesResponse")
class GetTablesResponse(
  override val result: Array<TableDto> = emptyArray(),
  override val code: Int = 200,
) : WebResponse<Array<TableDto>>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun getTablesResponseFromJson(json: String): GetTablesResponse = Json.decodeFromString(json)

/**
 * Replaces the whole table layout of a restaurant with [tables].
 *
 * @property managerId The requesting manager. Clients leave it empty: the gateway fills it from
 * the authentication token ([addId]).
 */
@JsExport
@Serializable
@SerialName("UpdateTablesRequest")
class UpdateTablesRequest(
  val managerId: String = "",
  val tables: Array<TableDto> = emptyArray(),
) : AuthenticatedRequest<UpdateTablesRequest>, JsonEncodable() {
  override fun toJson(): String = wireJson.encodeToString(this)
  override fun addId(userId: String): UpdateTablesRequest = UpdateTablesRequest(userId, tables)
}

@JsExport
fun updateTablesRequestFromJson(json: String): UpdateTablesRequest = Json.decodeFromString(json)

@JsExport
@Serializable
@SerialName("UpdateTablesResponse")
class UpdateTablesResponse(
  override val result: Array<TableDto> = emptyArray(),
  override val code: Int = 200,
) : WebResponse<Array<TableDto>>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun updateTablesResponseFromJson(json: String): UpdateTablesResponse = Json.decodeFromString(json)
