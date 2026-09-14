package com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper

import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuItemDetails
import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import com.munchies.restaurant.domain.valueobject.menu.MenuItemDescription
import com.munchies.restaurant.domain.valueobject.menu.MenuItemName
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import com.munchies.restaurant.domain.valueobject.menu.Validity
import com.munchies.restaurant.domain.valueobject.menu.Variation
import com.munchies.restaurant.domain.valueobject.menu.VariationName
import com.munchies.restaurant.domain.valueobject.menu.VariationOption
import com.munchies.restaurant.infrastructure.adapter.dto.ValidityType
import com.munchies.restaurant.infrastructure.adapter.dto.VariationDto
import com.munchies.restaurant.infrastructure.adapter.dto.VariationOptionDto
import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MenuDomainMapperTest {

  // --- VariationOption / Variation ---

  @Test
  fun `should format the additional price as a plain string`() {
    val option = VariationOption(name = "Large", additionalPrice = Money(BigDecimal("2.50")))

    val dto = option.toDto()

    assertEquals("Large", dto.name)
    assertEquals("2.50", dto.additionalPrice)
  }

  @Test
  fun `should map the variation name and every option onto the dto`() {
    val variation = Variation(VariationName("Size"))
      .option("Small", Money(BigDecimal("0")))
      .option("Large", Money(BigDecimal("2.5")))

    val dto = variation.toDto()

    assertEquals("Size", dto.name)
    assertEquals(2, dto.options.size)
    assertEquals("Small", dto.options[0].name)
    assertEquals("Large", dto.options[1].name)
    assertEquals("2.5", dto.options[1].additionalPrice)
  }

  @Test
  fun `should parse the additional price of a VariationOptionDto back into a BigDecimal`() {
    val dto = VariationOptionDto(name = "Large", additionalPrice = "2.50")

    val input = dto.toInput()

    assertEquals("Large", input.name)
    assertEquals(BigDecimal("2.50"), input.additionalPrice)
  }

  @Test
  fun `should map the name and every option of a VariationDto into an input`() {
    val dto = VariationDto(
      name = "Size",
      options = arrayOf(VariationOptionDto("Small", "0"), VariationOptionDto("Large", "2.5")),
    )

    val input = dto.toInput()

    assertEquals("Size", input.name)
    assertEquals(2, input.options.size)
    assertEquals("Large", input.options[1].name)
    assertEquals(BigDecimal("2.5"), input.options[1].additionalPrice)
  }

  // --- MenuItem ---

  @Test
  fun `should map id, name, description, price and variations of a menu item onto the dto`() {
    val category = Menu.create(RestaurantId()).createCategory(CategoryName.of("Pizzas"))
    val item = category.createItem(
      details = MenuItemDetails(MenuItemName.of("Margherita"), MenuItemDescription.of("Classic")),
      price = Money(BigDecimal("8.50")),
      variations = listOf(Variation(VariationName("Size")).option("Large", Money(BigDecimal("2")))),
    )

    val dto = item.toDto()

    assertEquals(item.id.value, dto.id)
    assertEquals("Margherita", dto.name)
    assertEquals("Classic", dto.description)
    assertEquals("8.50", dto.price)
    assertEquals(1, dto.variations.size)
    assertEquals("Size", dto.variations[0].name)
  }

  // --- Category ---

  @Test
  fun `should map id, name, items and variations of a category onto the dto`() {
    val menu = Menu.create(RestaurantId())
    val category = menu.createCategory(
      CategoryName.of("Pizzas"),
      listOf(Variation(VariationName("Dough")).option("Whole Wheat", Money(BigDecimal("1.5")))),
    )
    category.createItem(
      MenuItemDetails(MenuItemName.of("Margherita"), MenuItemDescription.of("Classic")),
      Money(BigDecimal("8.0")),
    )

    val dto = category.toDto()

    assertEquals(category.id.value, dto.id)
    assertEquals("Pizzas", dto.name)
    assertEquals(1, dto.items.size)
    assertEquals("Margherita", dto.items[0].name)
    assertEquals(1, dto.variations.size)
    assertEquals("Dough", dto.variations[0].name)
  }

  // --- Menu ---

  @Test
  fun `should map id, name, categories and validity of a menu onto the dto`() {
    val menu = Menu.create(RestaurantId(), MenuName.of("Dinner"), Validity.always)
    menu.createCategory(CategoryName.of("Pizzas"))

    val dto = menu.toDto()

    assertEquals(menu.id.value, dto.id)
    assertEquals("Dinner", dto.name)
    assertEquals(1, dto.categories.size)
    assertEquals("Pizzas", dto.categories[0].name)
    assertEquals(1, dto.validity.size)
    assertEquals(ValidityType.ALWAYS, dto.validity[0].type)
  }

  @Test
  fun `should map only id and name of a menu onto the summary dto`() {
    val menu = Menu.create(RestaurantId(), MenuName.of("Dinner"))

    val dto = menu.toSummaryDto()

    assertEquals(menu.id.value, dto.id)
    assertEquals("Dinner", dto.name)
  }
}
