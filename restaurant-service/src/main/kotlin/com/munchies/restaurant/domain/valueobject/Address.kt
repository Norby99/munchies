package com.munchies.restaurant.domain.valueobject

@JvmInline
value class Address private constructor(val value: String) {
  init {
    require(value.isNotBlank()) { "Restaurant address cannot be blank" }
    require(value.length <= MAX_ADDRESS_LENGTH) {
      "Restaurant address cannot exceed $MAX_ADDRESS_LENGTH characters"
    }
  }

  companion object {
    private const val MAX_ADDRESS_LENGTH = 500

    fun of(value: String): Address {
      val normalized = value.trim()
      return Address(normalized)
    }
  }
}
