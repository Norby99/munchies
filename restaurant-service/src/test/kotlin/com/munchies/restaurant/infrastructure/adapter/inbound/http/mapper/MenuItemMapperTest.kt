package com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper

import com.munchies.restaurant.application.usecase.menu.CreateMenuItemResult
import com.munchies.restaurant.application.usecase.menu.UpdateMenuItemResult
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuItem
import com.munchies.restaurant.domain.aggregate.MenuItemDetails
import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import com.munchies.restaurant.domain.valueobject.menu.MenuItemDescription
import com.munchies.restaurant.domain.valueobject.menu.MenuItemName
import com.munchies.restaurant.infrastructure.adapter.dto.VariationDto
import com.munchies.restaurant.infrastructure.adapter.dto.VariationOptionDto
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.CreateMenuItemRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.UpdateMenuItemRequest
import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MenuItemMapperTest {

  private fun sampleItem(): MenuItem = Menu.create(RestaurantId())
    .createCategory(CategoryName.of("Pizzas"))
    .createItem(
      MenuItemDetails(MenuItemName.of("Margherita"), MenuItemDescription.of("Classic")),
      Money(BigDecimal("8.0")),
    )

  // --- Create ---

  @Test
  fun `should build a CreateMenuItemCommand from every id, the fields and the parsed price`() {
    val request = CreateMenuItemRequest(
      name = "Margherita",
      description = "Classic",
      price = "8.50",
      variations = arrayOf(VariationDto("Size", arrayOf(VariationOptionDto("Large", "2.0")))),
    )

    val command = request.toCommand(
      restaurantId = "restaurant-1",
      menuId = "menu-1",
      categoryId = "category-1",
    )

    assertEquals("restaurant-1", command.restaurantId)
    assertEquals("menu-1", command.menuId)
    assertEquals("category-1", command.categoryId)
    assertEquals("Margherita", command.name)
    assertEquals("Classic", command.description)
    assertEquals(BigDecimal("8.50"), command.price)
    assertEquals(1, command.variations.size)
    assertEquals("Size", command.variations[0].name)
  }

  @Test
  fun `should wrap the new item id in a 201 response`() {
    val result = CreateMenuItemResult.Success(itemId = "item-1")

    val response = result.toResponse()

    assertEquals("item-1", response.result)
    assertEquals(201, response.code)
  }

  // --- Update ---

  @Test
  fun `should build an UpdateMenuItemCommand from every id, the fields and the parsed price`() {
    val request = UpdateMenuItemRequest(
      name = "Super Margherita",
      description = "With extra cheese",
      price = "10.00",
    )

    val command = request.toCommand(
      restaurantId = "restaurant-1",
      menuId = "menu-1",
      categoryId = "category-1",
      itemId = "item-1",
    )

    assertEquals("restaurant-1", command.restaurantId)
    assertEquals("menu-1", command.menuId)
    assertEquals("category-1", command.categoryId)
    assertEquals("item-1", command.itemId)
    assertEquals("Super Margherita", command.name)
    assertEquals("With extra cheese", command.description)
    assertEquals(BigDecimal("10.00"), command.price)
  }

  @Test
  fun `should wrap the updated menu item dto in a 200 response`() {
    val item = sampleItem()

    val response = UpdateMenuItemResult.Success(item).toResponse()

    assertEquals(item.id.value, response.result.id)
    assertEquals(200, response.code)
  }
}
