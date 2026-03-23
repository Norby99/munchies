package com.munchies.restaurant.domain.valueobject

@JvmInline
value class RestaurantName private constructor(val value: String) {
  init {
    require(value.isNotBlank()) { "Restaurant name cannot be blank" }
    require(value.length <= MAX_NAME_LENGTH) {
      "Restaurant name cannot exceed $MAX_NAME_LENGTH characters"
    }
  }

  companion object {
    private const val MAX_NAME_LENGTH = 255

    fun of(value: String): RestaurantName {
      val normalized = value.trim()
      return RestaurantName(normalized)
    }
  }
}
