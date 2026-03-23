package com.munchies.restaurant.domain.valueobject

@JvmInline
value class Email private constructor(val value: String) {
  init {
    require(value.length <= MAX_EMAIL_LENGTH) { "Email cannot exceed $MAX_EMAIL_LENGTH characters" }
    require(value.matches(EMAIL_REGEX)) { "Email format is invalid" }
  }

  companion object {
    private const val MAX_EMAIL_LENGTH = 255
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun of(value: String): Email {
      val normalized = value.trim().lowercase()
      return Email(normalized)
    }
  }
}
