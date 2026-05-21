package com.munchies.restaurant.domain.entity

import com.munchies.restaurant.domain.aggregate.Category
import com.munchies.restaurant.domain.aggregate.MenuItemId
import com.munchies.restaurant.domain.aggregate.VariationId
import com.munchies.restaurant.domain.aggregate.VariationOption
import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import com.munchies.restaurant.domain.valueobject.menu.MenuItemDescription
import com.munchies.restaurant.domain.valueobject.menu.MenuItemName
import com.munchies.restaurant.domain.valueobject.menu.VariationName
import com.munchies.restaurant.domain.valueobject.menu.VariationOptionName
import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CategoryTest {
  @Test
  fun `should update category name`() {
    val category = Category(name = CategoryName.of("Pizzas"))

    val newName = CategoryName.of("Wood Fired Pizzas")
    category.updateName(newName)

    assertEquals(newName, category.name)
  }

  @Test
  fun `should add item to category`() {
    val category = Category(name = CategoryName.of("Pizzas"))

    val item = category.addItem(
      MenuItemName.of("Margherita"),
      MenuItemDescription.of("Classic"),
      Money(BigDecimal("8.0")),
    )

    assertTrue(category.items.contains(item))
    assertEquals(1, category.items.size)
  }

  @Test
  fun `should update item in category`() {
    val category = Category(name = CategoryName.of("Pizzas"))
    val item = category.addItem(
      MenuItemName.of("Margherita"),
      MenuItemDescription.of("Classic"),
      Money(BigDecimal("8.0")),
    )

    val newName = MenuItemName.of("Super Margherita")
    val newDesc = MenuItemDescription.of("With extra cheese")
    val newPrice = Money(BigDecimal("10.0"))

    category.updateItem(item.id, newName, newDesc, newPrice)

    val updatedItem = category.items.first { it.id == item.id }
    assertEquals(newName, updatedItem.name)
    assertEquals(newDesc, updatedItem.description)
    assertEquals(newPrice, updatedItem.price)
  }

  @Test
  fun `should throw exception when updating non existent item in category`() {
    val category = Category(name = CategoryName.of("Pizzas"))

    assertThrows<IllegalArgumentException> {
      category.updateItem(
        MenuItemId(),
        MenuItemName.of("Test"),
        MenuItemDescription.of("Desc"),
        Money(BigDecimal("1.0")),
      )
    }
  }

  @Test
  fun `should remove item from category`() {
    val category = Category(name = CategoryName.of("Pizzas"))
    val item = category.addItem(
      MenuItemName.of("Margherita"),
      MenuItemDescription.of("Classic"),
      Money(BigDecimal("8.0")),
    )

    category.removeItem(item.id)

    assertFalse(category.items.contains(item))
    assertEquals(0, category.items.size)
  }

  @Test
  fun `should add variation to category`() {
    val category = Category(name = CategoryName.of("Pizzas"))

    val variation = category.addVariation(
      VariationName.of("Dough"),
      emptyList<VariationOption>(),
    )

    assertTrue(category.variations.contains(variation))
    assertEquals(1, category.variations.size)
  }

  @Test
  fun `should update variation in category`() {
    val category = Category(name = CategoryName.of("Pizzas"))
    val variation = category.addVariation(
      VariationName.of("Dough"),
      emptyList<VariationOption>(),
    )

    val newOptions =
      listOf(VariationOption(VariationOptionName.of("Whole Wheat"), Money(BigDecimal("1.5"))))
    category.updateVariation(variation.id, VariationName.of("Pizza Dough"), newOptions)

    val updatedVariation = category.variations.first { it.id == variation.id }
    assertEquals(VariationName.of("Pizza Dough"), updatedVariation.name)
    assertEquals(newOptions, updatedVariation.options)
  }

  @Test
  fun `should throw exception when updating non existent variation in category`() {
    val category = Category(name = CategoryName.of("Pizzas"))

    assertThrows<IllegalArgumentException> {
      category.updateVariation(
        VariationId(),
        VariationName.of("Dough"),
        emptyList<VariationOption>(),
      )
    }
  }

  @Test
  fun `should remove variation from category`() {
    val category = Category(name = CategoryName.of("Pizzas"))
    val variation = category.addVariation(
      VariationName.of("Dough"),
      emptyList(),
    )

    category.removeVariation(variation.id)

    assertFalse(category.variations.contains(variation))
    assertEquals(0, category.variations.size)
  }
}
