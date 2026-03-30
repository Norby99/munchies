package com.munchies.suggestion.application.usecase

import com.munchies.suggestion.application.port.inbound.SuggestMenuItem
import com.munchies.suggestion.domain.model.SuggestionRequest
import com.munchies.suggestion.domain.port.SuggestionEngine

class MultipleAttemptSuggestMenuItemUseCase(
  override val engine: SuggestionEngine,
  private val maxAttempts: Int = 3,
  private val useCase: SuggestMenuItemUseCase = SuggestMenuItemUseCase(engine),
) : SuggestMenuItem {
  override fun execute(suggestionRequest: SuggestionRequest): SuggestionEngine.SuggestionResult {
    var attempts = 0
    var result: SuggestionEngine.SuggestionResult
    do {
      result = useCase.execute(suggestionRequest)
      attempts++
      println("attempt: $attempts")
    } while (
      (result is SuggestionEngine.SuggestionResult.Companion.SuggestionSuccess).not() &&
      attempts < maxAttempts
    )
    return result
  }
}
