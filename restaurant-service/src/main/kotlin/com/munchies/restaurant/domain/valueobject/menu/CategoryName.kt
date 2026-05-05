package com.munchies.restaurant.domain.valueobject.menu

@JvmInline
value class CategoryName private constructor(val value: String) {
  init {
    require(value.isNotBlank()) { "Category name cannot be blank" }
    require(value.length <= MAX_NAME_LENGTH) {
      "Category name cannot exceed $MAX_NAME_LENGTH characters"
    }
  }

  companion object {
    private const val MAX_NAME_LENGTH = 100

    fun of(value: String): CategoryName = CategoryName(value.trim())
  }
}
