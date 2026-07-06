package com.munchies.restaurant.domain.entity

import com.munchies.restaurant.domain.aggregate.Category
import com.munchies.restaurant.domain.aggregate.MenuItemDetails
import com.munchies.restaurant.domain.aggregate.MenuItemId
import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import com.munchies.restaurant.domain.valueobject.menu.MenuItemDescription
import com.munchies.restaurant.domain.valueobject.menu.MenuItemName
import com.munchies.restaurant.domain.valueobject.menu.Validity
import com.munchies.restaurant.domain.valueobject.menu.Variation
import com.munchies.restaurant.domain.valueobject.menu.VariationName
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalTime
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CategoryTest {
  @Test
  fun `should update category name`() {
    val category = Category(name = CategoryName.of("Pizzas"))

    val newName = CategoryName.of("Wood Fired Pizzas")
    category.update(newName, emptyList())

    category.name shouldBe newName
  }

  @Test
  fun `should add item to category`() {
    val category = Category(name = CategoryName.of("Pizzas"))

    val item = category.createItem(
      details = MenuItemDetails(
        MenuItemName.of("Margherita"),
        MenuItemDescription.of("Classic"),
      ),
      price = Money(BigDecimal("8.0")),
    )

    category.items shouldContain item
  }

  @Test
  fun `should update item in category`() {
    val category = Category(name = CategoryName.of("Pizzas"))
    val item = category.createItem(
      MenuItemDetails(
        MenuItemName.of("Margherita"),
        MenuItemDescription.of("Classic"),
      ),
      Money(BigDecimal("8.0")),
      emptyList(),
    )

    val newName = MenuItemName.of("Super Margherita")
    val newDesc = MenuItemDescription.of("With extra cheese")
    val newPrice = Money(BigDecimal("10.0"))
    val newVariations = listOf(
      Variation(VariationName("Size"))
        .option("Large", Money("2.0")),
    )
    val newValidity = Validity.hours(LocalTime.of(10, 0), LocalTime.of(22, 0))

    category.updateItem(
      item.id,
      MenuItemDetails(newName, newDesc),
      newPrice,
      newVariations,
      newValidity,
    )

    val updated = category.items.first { it.id == item.id }
    updated.name shouldBe newName
    updated.description shouldBe newDesc
    updated.price shouldBe newPrice
    updated.variations shouldBe newVariations
    updated.validity shouldBe newValidity
  }

  @Test
  fun `should throw exception when updating non existent item in category`() {
    val category = Category(name = CategoryName.of("Pizzas"))

    assertThrows<IllegalArgumentException> {
      category.updateItem(
        MenuItemId(),
        MenuItemDetails(
          MenuItemName.of("Test"),
          MenuItemDescription.of("Desc"),
        ),
        Money(BigDecimal("1.0")),
        emptyList(),
      )
    }
  }

  @Test
  fun `should remove item from category`() {
    val category = Category(name = CategoryName.of("Pizzas"))
    val item = category.createItem(
      MenuItemDetails(
        MenuItemName.of("Margherita"),
        MenuItemDescription.of("Classic"),
      ),
      Money(BigDecimal("8.0")),
    )

    category.removeItem(item.id)

    category.items shouldNotContain item
  }

  @Test
  fun `should add variation to category`() {
    val category = Category(name = CategoryName.of("Pizzas"))
    val variation = Variation(VariationName("Dough"))
      .option("Whole Wheat", Money(BigDecimal("1.5")))

    category.update(category.name, listOf(variation))

    category.variations shouldContain variation
  }

  @Test
  fun `should remove variation from category`() {
    val variation = Variation(VariationName("Dough"))
      .option("Whole Wheat", Money(BigDecimal("1.5")))
    val category = Category(name = CategoryName.of("Pizzas"), variations = listOf(variation))

    category.update(category.name, emptyList())

    category.variations shouldNotContain variation
  }
}
