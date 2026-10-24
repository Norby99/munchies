package com.munchies.suggestion.infrastructure.adapter.dto

import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@JsExport
@Serializable
@SerialName("SuggestionRequestDTO")
data class SuggestionRequestDTO(
  val userId: String,
  val menu: List<MenuItemDTO>,
  val userPreferences: List<UserPreferenceDTO>,
)
