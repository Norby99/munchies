package com.munchies.suggestion.adapter.inbound.json

import com.munchies.suggestion.domain.model.SuggestionResponse
import com.munchies.suggestion.domain.port.JsonParser
import com.munchies.suggestion.domain.port.JsonParser.JsonParserResult.Companion.JsonParserSuccess
import com.munchies.suggestion.domain.port.JsonParser.JsonParserResult.Companion.MalformedJsonParserResult
import dev.langchain4j.internal.Json

class Lang4JSuggestionJsonParser : JsonParser {

  override fun parse(raw: String): JsonParser.JsonParserResult {
    try {
      val parsed = Json.fromJson(raw, SuggestionResponse::class.java)
      return JsonParserSuccess(parsed)
    } catch (_: Exception) {
      return MalformedJsonParserResult
    }
  }
}
