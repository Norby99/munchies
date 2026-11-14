package com.munchies.suggestion.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.wireJson
import com.munchies.suggestion.infrastructure.adapter.dto.SuggestionResponseDTO
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("SuggestItemResponse")
open class SuggestItemResponse(
  override val result: SuggestionResponseDTO,
  override val code: Int = 200,
) : WebResponse<SuggestionResponseDTO>() {
  val response: SuggestionResponseDTO
    get() = result

  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun suggestItemResponseFromJson(json: String): SuggestItemResponse = Json.decodeFromString(json)

@JsExport
fun getSuggestItemResponseFromJson(json: String): SuggestItemResponse =
  suggestItemResponseFromJson(json)
