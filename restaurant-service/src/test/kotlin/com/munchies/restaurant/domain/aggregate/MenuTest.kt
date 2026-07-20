package com.munchies.restaurant.domain.aggregate

import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import com.munchies.restaurant.domain.valueobject.menu.Validity
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MenuTest {
  @Test
  fun `should create menu using factory methods`() {
    val restaurantId = RestaurantId()

    val menu1 = Menu.create(restaurantId)
    assertEquals(MenuName.of("Menu Default"), menu1.name)
    assertEquals(restaurantId, menu1.restaurantId)

    val customName = MenuName.of("My Menu")
    val menu2 = Menu.create(restaurantId, customName)
    assertEquals(customName, menu2.name)

    val validity = Validity.from("2025-1-1")
    val menu3 = Menu.create(restaurantId, customName, validity)
    assertEquals(validity, menu3.validity)
  }

  @Test
  fun `should update menu name`() {
    val menu = Menu.create(RestaurantId())

    val newName = MenuName.of("Dinner Menu")
    menu.updateName(newName)

    assertEquals(newName, menu.name)
  }

  @Test
  fun `should add category to menu`() {
    val menu = Menu.create(RestaurantId())

    val category = menu.createCategory(CategoryName.of("Desserts"))

    assertTrue(menu.categories.contains(category))
    assertEquals(1, menu.categories.size)
  }

  @Test
  fun `should update category in menu`() {
    val menu = Menu.create(RestaurantId())
    val category = menu.createCategory(CategoryName.of("Desserts"))

    val newName = CategoryName.of("Sweet Desserts")
    menu.updateCategory(category.id, newName)

    val updatedCategory = menu.categories.first { it.id == category.id }
    assertEquals(newName, updatedCategory.name)
  }

  @Test
  fun `should throw exception when updating non existent category in menu`() {
    val menu = Menu.create(RestaurantId())

    assertThrows<IllegalArgumentException> {
      menu.updateCategory(CategoryId(), CategoryName.of("Test"))
    }
  }

  @Test
  fun `should remove category from menu`() {
    val menu = Menu.create(RestaurantId())
    val category = menu.createCategory(CategoryName.of("Desserts"))

    menu.deleteCategory(category.id)

    assertFalse(menu.categories.contains(category))
    assertEquals(0, menu.categories.size)
  }

  @Test
  fun `should get category by id`() {
    val menu = Menu.create(RestaurantId())
    val category = menu.createCategory(CategoryName.of("Desserts"))

    val foundCategory = menu.getCategory(category.id)
    assertEquals(category, foundCategory)

    val notFoundCategory = menu.getCategory(CategoryId())
    assertNull(notFoundCategory)
  }

  @Test
  fun `should check menu validity correctly`() {
    val menu = Menu.create(
      RestaurantId(),
      MenuName.of("Test"),
      Validity.until("2025-12-31"),
    )

    assertTrue(menu.isValid(LocalDate.of(2025, 12, 30).atStartOfDay()))
    assertFalse(menu.isValid(LocalDate.of(2026, 1, 1).atStartOfDay()))
  }

  @Test
  fun `should update menu validity`() {
    val menu = Menu.create(RestaurantId())

    val newValidity = Validity.until("2025-12-31")
    menu.updateValidity(newValidity)

    assertEquals(newValidity, menu.validity)
  }
}
