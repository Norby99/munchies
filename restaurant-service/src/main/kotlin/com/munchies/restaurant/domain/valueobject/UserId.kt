package com.munchies.restaurant.domain.valueobject

import com.munchies.commons.UUIDEntityId

data class UserId(
  override val value: String = newId(),
) : UUIDEntityId(value) {
  init {
    require(value.isNotBlank()) { "UserId cannot be blank" }
  }

  companion object {
    fun of(value: String): UserId = UserId(value)
  }

  override fun toString(): String = value
}
