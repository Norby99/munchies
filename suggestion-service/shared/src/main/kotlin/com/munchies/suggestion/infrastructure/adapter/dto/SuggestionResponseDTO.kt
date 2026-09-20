package com.munchies.suggestion.infrastructure.adapter.dto

data class SuggestionResponseDTO(
  val rationale: String,
  val confidence: SuggestionConfidenceDTO,
  val suggestedMenuItems: List<SuggestedMenuItemDTO>,
)
