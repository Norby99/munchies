package com.munchies.suggestion.application.usecase

import com.munchies.suggestion.application.port.inbound.SuggestMenuItem
import com.munchies.suggestion.domain.model.SuggestionRequest
import com.munchies.suggestion.domain.port.SuggestionEngine
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.SuggestionSuccess

class MultipleAttemptSuggestMenuItemUseCase(
  override val engine: SuggestionEngine,
  private val maxAttempts: Int = 3,
  private val useCase: SuggestMenuItem = SuggestMenuItemUseCase(engine),
) : SuggestMenuItem {
  init {
      assert(maxAttempts > 0)
  }

  override fun execute(suggestionRequest: SuggestionRequest): SuggestionResult {
    var attempts = 0
    var result: SuggestionResult
    do {
      result = useCase.execute(suggestionRequest)
      attempts++
      println("attempt: $attempts")
    } while (
      (result is SuggestionSuccess).not() &&
      attempts < maxAttempts
    )
    return result
  }
}
