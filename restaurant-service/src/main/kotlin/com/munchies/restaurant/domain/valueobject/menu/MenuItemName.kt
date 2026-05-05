package com.munchies.restaurant.domain.valueobject.menu

@JvmInline
value class MenuItemName private constructor(val value: String) {
  init {
    require(value.isNotBlank()) { "Menu item name cannot be blank" }
    require(value.length <= MAX_NAME_LENGTH) {
      "Menu item name cannot exceed $MAX_NAME_LENGTH characters"
    }
  }

  companion object {
    private const val MAX_NAME_LENGTH = 150

    fun of(value: String): MenuItemName = MenuItemName(value.trim())
  }
}
