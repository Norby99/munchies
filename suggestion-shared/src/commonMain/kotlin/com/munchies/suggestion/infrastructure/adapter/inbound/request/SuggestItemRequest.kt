package com.munchies.suggestion.infrastructure.adapter.inbound.request

import com.munchies.suggestion.infrastructure.adapter.dto.SuggestionRequestDTO
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("SuggestItemRequest")
class SuggestItemRequest (val dto : SuggestionRequestDTO){

}

@JsExport
fun suggestItemRequestFromJson(json: String): SuggestItemRequest =
  (Json.decodeFromString(json) as SuggestItemRequest)
