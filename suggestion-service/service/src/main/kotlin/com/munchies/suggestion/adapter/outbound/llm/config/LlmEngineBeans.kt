package com.munchies.suggestion.adapter.outbound.llm.config

import com.munchies.suggestion.adapter.outbound.llm.engine.LLMSuggestionEngine
import com.munchies.suggestion.domain.port.SuggestionEngine
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
class LlmEngineBeans {
  @Singleton
  fun getLlmEngine(): SuggestionEngine = LLMSuggestionEngine()
}
