package com.munchies.restaurant.domain.valueobject.menu

@JvmInline
value class MenuItemDescription private constructor(val value: String) {
  init {
    require(value.isNotBlank()) { "Menu item description cannot be blank" }
    require(value.length <= MAX_DESCRIPTION_LENGTH) {
      "Menu item description cannot exceed $MAX_DESCRIPTION_LENGTH characters"
    }
  }

  companion object {
    private const val MAX_DESCRIPTION_LENGTH = 500

    fun of(value: String): MenuItemDescription = MenuItemDescription(value.trim())
  }
}
