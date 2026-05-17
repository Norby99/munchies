package com.munchies.restaurant.domain.valueobject

import com.munchies.commons.UUIDEntityId

data class RestaurantId(
  override val value: String = newId(),
) : UUIDEntityId(value) {
  init {
    require(value.isNotBlank()) { "RestaurantId cannot be blank" }
  }

  companion object {
    fun of(value: String): RestaurantId = RestaurantId(value)
  }

  override fun toString(): String = value
}
