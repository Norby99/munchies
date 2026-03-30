package com.munchies.suggestion.adapter.dto

data class SuggestionRequestDTO(
  val userId: String,
  val menu: List<MenuItemDTO>,
  val userPreferences: List<UserPreferenceDTO>,
)
