package com.munchies.suggestion.domain.model

import com.munchies.commons.AggregateRoot
import com.munchies.commons.Entity
import com.munchies.commons.UUIDEntityId

data class SuggestionRequest(
  val user: UUIDEntityId,
  val menu: List<Entity<*>>,
  val userPreferences: List<Entity<*>>,
  override val id: SuggestionRequestId,
) : AggregateRoot<SuggestionRequestId>(id = id)
