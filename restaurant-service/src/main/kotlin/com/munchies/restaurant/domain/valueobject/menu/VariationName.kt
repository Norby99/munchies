package com.munchies.restaurant.domain.valueobject.menu

@JvmInline
value class VariationName private constructor(val value: String) {
  init {
    require(value.isNotBlank()) { "Variation name cannot be blank" }
    require(value.length <= MAX_NAME_LENGTH) {
      "Variation name cannot exceed $MAX_NAME_LENGTH characters"
    }
  }

  companion object {
    private const val MAX_NAME_LENGTH = 100

    operator fun invoke(value: String): VariationName = VariationName(value.trim())
  }
}
