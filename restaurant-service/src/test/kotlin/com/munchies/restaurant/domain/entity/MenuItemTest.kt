package com.munchies.restaurant.domain.entity

import com.munchies.restaurant.domain.aggregate.MenuItem
import com.munchies.restaurant.domain.aggregate.MenuItemDetails
import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.menu.MenuItemDescription
import com.munchies.restaurant.domain.valueobject.menu.MenuItemName
import com.munchies.restaurant.domain.valueobject.menu.Validity
import com.munchies.restaurant.domain.valueobject.menu.Variation
import com.munchies.restaurant.domain.valueobject.menu.VariationName
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import java.math.BigDecimal
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MenuItemTest {
  @Test
  fun `should update menu item details correctly`() {
    val item = MenuItem(
      details = MenuItemDetails(
        MenuItemName.of("Diavola"),
        MenuItemDescription.of("With spicy salami"),
      ),
      price = Money(BigDecimal("10.0")),
    )

    val newName = MenuItemName.of("Vegan Diavola")
    val newDescription = MenuItemDescription.of("No cheese")
    val newPrice = Money(BigDecimal("12.0"))

    item.update(MenuItemDetails(newName, newDescription), newPrice)

    assertEquals(newName, item.details.name)
    assertEquals(newDescription, item.details.description)
    assertEquals(newPrice, item.price)
  }

  @Test
  fun `should add variation to menu item`() {
    val item = MenuItem(
      details = MenuItemDetails(
        MenuItemName.of("Diavola"),
        MenuItemDescription.of("With spicy salami"),
      ),
      price = Money(BigDecimal("10.0")),
    )

    val variation = Variation(VariationName("Size"))
      .option("Large", Money(BigDecimal("2.0")))
    item.update(
      MenuItemDetails(item.name, item.description),
      item.price,
      listOf(variation),
    )

    assertTrue(item.variations.contains(variation))
    assertEquals(1, item.variations.size)
  }

  @Test
  fun `should update variation of menu item`() {
    val variation = Variation(VariationName("Size"))
      .option("Large", Money(2))
    val item = MenuItem(
      details = MenuItemDetails(
        MenuItemName.of("Diavola"),
        description = MenuItemDescription.of("With spicy salami"),
      ),
      price = Money(BigDecimal("10.0")),
      variations = listOf(variation),
    )

    val newVariation = Variation(VariationName("New Size"))
      .option("Large", Money(3))
    item.update(
      item.details,
      item.price,
      listOf(newVariation),
    )

    item.variations shouldContain newVariation
    item.variations shouldNotContain variation
  }

  @Test
  fun `should check validity correctly`() {
    val item = MenuItem(
      details = MenuItemDetails(
        MenuItemName.of("Diavola"),
        MenuItemDescription.of("With spicy salami"),
      ),
      price = Money(BigDecimal("10.0")),
      validity = Validity.from("2025-01-01"),
    )

    assertTrue(item.isValid(LocalDate.of(2025, 1, 2).atStartOfDay()))
    assertFalse(item.isValid(LocalDate.of(2024, 12, 31).atStartOfDay()))
  }

  @Test
  fun `should update validity`() {
    val item = MenuItem(
      details = MenuItemDetails(
        MenuItemName.of("With spicy salami"),
        MenuItemDescription.of("With spicy ham"),
      ),
      price = Money(BigDecimal("10.0")),
    )
    val newValidity = Validity.from("2025-01-01")

    item.update(
      item.details,
      item.price,
      item.variations,
      newValidity,
    )

    assertEquals(newValidity, item.validity)
  }
}
