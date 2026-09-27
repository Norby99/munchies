package com.munchies.suggestion.infrastructure.adapter.outbound.response

import com.munchies.suggestion.infrastructure.adapter.dto.SuggestionResponseDTO
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("SuggestItemResponse")
class SuggestItemResponse(val response: SuggestionResponseDTO) {
}


@JsExport
fun getSuggestItemResponseFromJson(json: String): SuggestItemResponse = Json.decodeFromString(json)
