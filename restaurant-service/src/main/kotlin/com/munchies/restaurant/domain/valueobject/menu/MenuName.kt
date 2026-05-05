package com.munchies.restaurant.domain.valueobject.menu

@JvmInline
value class MenuName private constructor(val value: String) {
  init {
    require(value.isNotBlank()) { "Menu name cannot be blank" }
    require(value.length <= MAX_NAME_LENGTH) { "Menu name must be at most 50 characters" }
  }

  companion object {
    private const val MAX_NAME_LENGTH = 50

    fun of(value: String): MenuName = MenuName(value)
  }
}
