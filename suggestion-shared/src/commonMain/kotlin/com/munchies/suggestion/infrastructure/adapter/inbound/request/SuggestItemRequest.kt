package com.munchies.suggestion.infrastructure.adapter.inbound.request

import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.wireJson
import com.munchies.suggestion.infrastructure.adapter.dto.SuggestionRequestDTO
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("SuggestItemRequest")
data class SuggestItemRequest(
  val dto: SuggestionRequestDTO,
) : JsonEncodable() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun suggestItemRequestFromJson(json: String): SuggestItemRequest = Json.decodeFromString(json)
