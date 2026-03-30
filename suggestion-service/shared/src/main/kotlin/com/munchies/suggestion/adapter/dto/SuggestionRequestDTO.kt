package com.munchies.suggestion.adapter.dto

data class SuggestionRequestDTO(
  val userId: String,
  val menu: List<Int>,
  val userPreferences: List<String>,
)
