package com.munchies.suggestion.domain.model

data class SuggestionResponse(
  val rationale: String,
  val confidence: SuggestionConfidence,
  val suggestedMenuItems: List<SuggestedMenuItems>,
)

data class SuggestedMenuItems(
  val itemId: Int,
  val reason: String,
)
