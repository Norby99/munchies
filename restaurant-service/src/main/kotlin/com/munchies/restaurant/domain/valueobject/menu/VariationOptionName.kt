package com.munchies.restaurant.domain.valueobject.menu

@JvmInline
value class VariationOptionName private constructor(val value: String) {
  init {
    require(value.isNotBlank()) { "Variation option name cannot be blank" }
    require(value.length <= MAX_NAME_LENGTH) {
      "Variation option name cannot exceed $MAX_NAME_LENGTH characters"
    }
  }

  companion object {
    private const val MAX_NAME_LENGTH = 100

    fun of(value: String): VariationOptionName = VariationOptionName(value.trim())
  }
}
