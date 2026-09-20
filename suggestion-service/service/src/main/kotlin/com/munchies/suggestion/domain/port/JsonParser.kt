package com.munchies.suggestion.domain.port

import com.munchies.suggestion.domain.model.SuggestionResponse

interface JsonParser {
  fun parse(raw: String): JsonParserResult

  sealed interface JsonParserResult {
    companion object {
      data class JsonParserSuccess(val result: SuggestionResponse) : JsonParserResult
      data object MalformedJsonParserResult : JsonParserResult
    }
  }
}
