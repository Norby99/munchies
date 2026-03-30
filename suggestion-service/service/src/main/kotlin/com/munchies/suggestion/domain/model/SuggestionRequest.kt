package com.munchies.suggestion.domain.model

import com.munchies.commons.AggregateRoot
import com.munchies.commons.UUIDEntityId

data class SuggestionRequest(
  val user: UUIDEntityId,
  val menu: List<MenuItem>,
  val userPreferences: List<UserPreference>,
  override val id: SuggestionRequestId = SuggestionRequestId(),
) : AggregateRoot<SuggestionRequestId>(id = id)
