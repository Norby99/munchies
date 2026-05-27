package com.munchies.suggestion.infrastructure.adapter.inbound.json

import com.munchies.suggestion.domain.model.SuggestionConfidence
import com.munchies.suggestion.domain.model.SuggestionResponse
import com.munchies.suggestion.domain.port.JsonParser.JsonParserResult.Companion.JsonParserSuccess
import com.munchies.suggestion.domain.port.JsonParser.JsonParserResult.Companion.MalformedJsonParserResult
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class Lang4JSuggestionJsonParserTest {
  @Test
  fun `Parser returns malformed json parser result when string cant be parsed to obj`() {
    val string = "this is not a json string"
    val parser = Lang4JSuggestionJsonParser()

    val result = parser.parse(string)
    result shouldBe MalformedJsonParserResult
  }

  @Test
  fun `Parser returns json parser success when string can be parsed to obj`() {
    val suggestionResponse = SuggestionResponse(
      rationale = "Because it's a good idea",
      confidence = SuggestionConfidence.HIGH,
      suggestedMenuItems = listOf(),
    )

    val suggestionResponseRaw = """
      {
        "rationale": "Because it's a good idea",
        "confidence": "HIGH",
        "suggestedMenuItems": []
      }
    """.trimIndent()

    val parser = Lang4JSuggestionJsonParser()
    val result = parser.parse(suggestionResponseRaw)

    result shouldBe JsonParserSuccess(suggestionResponse)
  }
}
