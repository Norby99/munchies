package com.munchies.suggestion.infrastructure.adapter.inbound.web.config

import com.munchies.suggestion.application.port.inbound.SuggestMenuItem
import com.munchies.suggestion.application.usecase.MultipleAttemptSuggestMenuItemUseCase
import com.munchies.suggestion.domain.port.SuggestionEngine
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
class SuggestionBeans {
  @Singleton
  fun getSuggestionMenuItem(engine: SuggestionEngine): SuggestMenuItem =
    MultipleAttemptSuggestMenuItemUseCase(engine)
}
