package com.munchies.suggestion.infrastructure.adapter.dto

import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@JsExport
@Serializable
@SerialName("SuggestionResponseDTO")
data class SuggestionResponseDTO(
  val rationale: String,
  val confidence: SuggestionConfidenceDTO,
  val suggestedMenuItems: List<SuggestedMenuItemDTO>,
)
