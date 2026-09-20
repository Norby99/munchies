package com.munchies.suggestion.infrastructure.adapter.outbound.llm.config

import com.munchies.suggestion.domain.port.SuggestionEngine
import com.munchies.suggestion.infrastructure.adapter.outbound.llm.engine.LLMSuggestionEngine
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
class LlmEngineBeans {
  @Singleton
  fun getLlmEngine(): SuggestionEngine = LLMSuggestionEngine()
}
