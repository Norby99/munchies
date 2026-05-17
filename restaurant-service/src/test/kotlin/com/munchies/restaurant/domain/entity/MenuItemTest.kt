package com.munchies.restaurant.domain.entity

import com.munchies.restaurant.domain.aggregate.MenuItem
import com.munchies.restaurant.domain.aggregate.VariationId
import com.munchies.restaurant.domain.aggregate.VariationOption
import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.menu.MenuItemDescription
import com.munchies.restaurant.domain.valueobject.menu.MenuItemName
import com.munchies.restaurant.domain.valueobject.menu.Validity
import com.munchies.restaurant.domain.valueobject.menu.VariationName
import com.munchies.restaurant.domain.valueobject.menu.VariationOptionName
import java.math.BigDecimal
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MenuItemTest {
  @Test
  fun `should update menu item details correctly`() {
    val item = MenuItem(
      name = MenuItemName.of("Pizza"),
      description = MenuItemDescription.of("Cheese pizza"),
      price = Money(BigDecimal("10.0")),
    )

    val newName = MenuItemName.of("Vegan Pizza")
    val newDescription = MenuItemDescription.of("No cheese")
    val newPrice = Money(BigDecimal("12.0"))

    item.update(newName, newDescription, newPrice)

    assertEquals(newName, item.name)
    assertEquals(newDescription, item.description)
    assertEquals(newPrice, item.price)
  }

  @Test
  fun `should add variation to menu item`() {
    val item = MenuItem(
      name = MenuItemName.of("Pizza"),
      description = MenuItemDescription.of("Cheese pizza"),
      price = Money(BigDecimal("10.0")),
    )

    val options = listOf(
      VariationOption(
        VariationOptionName.of("Large"),
        Money(BigDecimal("3.0")),
      ),
    )
    val variation = item.addVariation(VariationName.of("Size"), options)

    assertTrue(item.variations.contains(variation))
    assertEquals(1, item.variations.size)
  }

  @Test
  fun `should update variation of menu item`() {
    val item = MenuItem(
      name = MenuItemName.of("Pizza"),
      description = MenuItemDescription.of("Cheese pizza"),
      price = Money(BigDecimal("10.0")),
    )
    val variation = item.addVariation(VariationName.of("Size"), emptyList())

    val newOptions =
      listOf(VariationOption(VariationOptionName.of("Large"), Money(BigDecimal("3.0"))))
    item.updateVariation(variation.id, VariationName.of("New Size"), newOptions)

    val updatedVariation = item.variations.first { it.id == variation.id }
    assertEquals(VariationName.of("New Size"), updatedVariation.name)
    assertEquals(newOptions, updatedVariation.options)
  }

  @Test
  fun `should throw exception when updating non existent variation in menu item`() {
    val item = MenuItem(
      name = MenuItemName.of("Pizza"),
      description = MenuItemDescription.of("Cheese pizza"),
      price = Money(BigDecimal("10.0")),
    )

    assertThrows<IllegalArgumentException> {
      item.updateVariation(VariationId(), VariationName.of("Size"), emptyList())
    }
  }

  @Test
  fun `should remove variation from menu item`() {
    val item = MenuItem(
      name = MenuItemName.of("Pizza"),
      description = MenuItemDescription.of("Cheese pizza"),
      price = Money(BigDecimal("10.0")),
    )
    val variation = item.addVariation(VariationName.of("Size"), emptyList())

    item.removeVariation(variation.id)

    assertFalse(item.variations.contains(variation))
    assertEquals(0, item.variations.size)
  }

  @Test
  fun `should check validity correctly`() {
    val item = MenuItem(
      name = MenuItemName.of("Pizza"),
      description = MenuItemDescription.of("Cheese pizza"),
      price = Money(BigDecimal("10.0")),
      validity = Validity.from(LocalDate.of(2025, 1, 1)),
    )

    assertTrue(item.isValid(LocalDate.of(2025, 1, 2).atStartOfDay()))
    assertFalse(item.isValid(LocalDate.of(2024, 12, 31).atStartOfDay()))
  }

  @Test
  fun `should update validity`() {
    val item = MenuItem(
      name = MenuItemName.of("Pizza"),
      description = MenuItemDescription.of("Cheese pizza"),
      price = Money(BigDecimal("10.0")),
    )

    val newValidity = Validity.from(LocalDate.of(2025, 1, 1))
    item.updateValidity(newValidity)

    assertEquals(newValidity, item.validity)
  }
}
