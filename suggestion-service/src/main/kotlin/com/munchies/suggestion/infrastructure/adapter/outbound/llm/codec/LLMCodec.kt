package com.munchies.suggestion.infrastructure.adapter.outbound.llm.codec

import com.munchies.suggestion.domain.model.SuggestionRequest
import com.munchies.suggestion.domain.port.Codec
import com.munchies.suggestion.domain.port.JsonParser
import com.munchies.suggestion.infrastructure.adapter.inbound.json.Lang4JSuggestionJsonParser

class LLMCodec(
  override val parser: JsonParser = Lang4JSuggestionJsonParser(),
) : Codec<SuggestionRequest> {

  override fun SuggestionRequest.render(): String {
    return """
      You are a menu suggestion engine. Your only job is to select items from the provided menu.

      ### RULES
      - Respond with ONLY a JSON object. No explanation, no markdown, no code blocks.
      - Only use item IDs that appear in the menu below. Never invent IDs.
      - The "confidence" field must be exactly one of: HIGH, MEDIUM, LOW.
      - If no items are appropriate, return an empty suggestions array.

      ### OUTPUT FORMAT
      {
        "rationale": "<one sentence explaining the selection>",
        "confidence": "<HIGH|MEDIUM|LOW>",
        "suggestedMenuItems": [
          { "itemId": <integer>, "reason": "<short reason>" }
        ]
      }

      ### MENU
      {{ ${this.menu} }}

      ### USER CONTEXT
      Preferences: {{ ${this.userPreferences} }}

      JSON:
    """.trimIndent()
  }
}
