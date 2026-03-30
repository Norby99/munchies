package com.munchies.suggestion.domain.port

import com.munchies.suggestion.domain.model.SuggestionRequest
import com.munchies.suggestion.domain.model.SuggestionResponse

interface SuggestionEngine {
  fun suggest(request: SuggestionRequest): SuggestionResult

  sealed interface SuggestionResult {
    companion object : SuggestionResult {
      data class SuggestionSuccess(val result: SuggestionResponse) : SuggestionResult
      data object EmptySuggestion : SuggestionResult
      data object MalformedSuggestion : SuggestionResult
      data object TimeoutSuggestion : SuggestionResult
    }
  }
}
