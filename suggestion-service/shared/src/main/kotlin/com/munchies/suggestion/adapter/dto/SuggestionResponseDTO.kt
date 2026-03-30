package com.munchies.suggestion.adapter.dto

data class SuggestionResponseDTO(
  val rationale: String,
  val confidence: SuggestionConfidenceDTO,
  val suggestedMenuItems: List<SuggestedMenuItemDTO>,
)
