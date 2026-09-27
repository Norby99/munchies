package com.munchies.suggestion.domain.model

data class SuggestionResponse(
  val rationale: String,
  val confidence: SuggestionConfidence,
  val suggestedMenuItems: List<SuggestedMenuItem>,
)
