package com.munchies.restaurant.domain.valueobject

@JvmInline
value class Phone private constructor(val value: String) {
  init {
    require(value.isNotBlank()) { "Phone number cannot be blank" }
    require(value.matches(PHONE_REGEX)) { "Phone number format is invalid" }
    require(value.length <= MAX_PHONE_LENGTH) {
      "Phone number cannot exceed $MAX_PHONE_LENGTH characters"
    }
  }

  companion object {
    private const val MAX_PHONE_LENGTH = 20
    private val PHONE_REGEX = Regex("^[+]?[0-9\\s\\-()]+$")

    fun of(value: String): Phone {
      val normalized = value.trim()
      return Phone(normalized)
    }
  }
}
