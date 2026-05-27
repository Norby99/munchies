package com.munchies.suggestion.application.port.inbound

import com.munchies.suggestion.domain.model.SuggestionRequest
import com.munchies.suggestion.domain.port.SuggestionEngine

interface SuggestMenuItem {
  val engine: SuggestionEngine

  fun execute(suggestionRequest: SuggestionRequest): SuggestionEngine.SuggestionResult =
    engine.suggest(suggestionRequest)
}
