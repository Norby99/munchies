package com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant

import com.munchies.commons.infrastructure.adapter.AuthenticatedRequest
import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.wireJson
import com.munchies.restaurant.infrastructure.adapter.dto.CapacityPolicyDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("GetCapacityPolicyResponse")
class GetCapacityPolicyResponse(
  override val result: CapacityPolicyDto,
  override val code: Int = 200,
) : WebResponse<CapacityPolicyDto>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun getCapacityPolicyResponseFromJson(json: String): GetCapacityPolicyResponse =
  Json.decodeFromString(json)

@JsExport
@Serializable
@SerialName("UpdateCapacityPolicyRequest")
data class UpdateCapacityPolicyRequest(
  val managerId: String = "",
  val slotMinutes: Int,
  val maxCapacityUnits: Int,
  val opening: String,
  val closing: String,
  val prepLeadMinutes: Int,
  val maxAdvanceDays: Int,
  val maxTripsPerSlot: Int,
  val maxTripKmBudget: Double,
  val maxDetourKm: Double,
) : AuthenticatedRequest<UpdateCapacityPolicyRequest>, JsonEncodable() {
  override fun toJson(): String = wireJson.encodeToString(this)
  override fun addId(userId: String): UpdateCapacityPolicyRequest = copy(managerId = userId)
}

@JsExport
fun updateCapacityPolicyRequestFromJson(json: String): UpdateCapacityPolicyRequest =
  Json.decodeFromString(json)

@JsExport
@Serializable
@SerialName("UpdateCapacityPolicyResponse")
class UpdateCapacityPolicyResponse(
  override val result: CapacityPolicyDto,
  override val code: Int = 200,
) : WebResponse<CapacityPolicyDto>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun updateCapacityPolicyResponseFromJson(json: String): UpdateCapacityPolicyResponse =
  Json.decodeFromString(json)
