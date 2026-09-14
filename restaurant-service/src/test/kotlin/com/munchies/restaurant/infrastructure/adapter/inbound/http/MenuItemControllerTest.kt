package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.application.MenuService
import com.munchies.restaurant.application.usecase.menu.CreateMenuItemCommand
import com.munchies.restaurant.application.usecase.menu.CreateMenuItemResult
import com.munchies.restaurant.application.usecase.menu.DeleteMenuItemCommand
import com.munchies.restaurant.application.usecase.menu.RemoveMenuItemResult
import com.munchies.restaurant.application.usecase.menu.UpdateMenuItemCommand
import com.munchies.restaurant.application.usecase.menu.UpdateMenuItemResult
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuItem
import com.munchies.restaurant.domain.aggregate.MenuItemDetails
import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import com.munchies.restaurant.domain.valueobject.menu.MenuItemDescription
import com.munchies.restaurant.domain.valueobject.menu.MenuItemName
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.NotFoundException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ValidationException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.CreateMenuItemRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.UpdateMenuItemRequest
import io.micronaut.http.HttpStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MenuItemControllerTest {

  private lateinit var menuService: MenuService
  private lateinit var controller: MenuItemController

  @BeforeEach
  fun setUp() {
    menuService = mockk()
    controller = MenuItemController(menuService)
  }

  private fun sampleItem(): MenuItem = Menu.create(RestaurantId())
    .createCategory(CategoryName.of("Pizzas"))
    .createItem(
      MenuItemDetails(MenuItemName.of("Margherita"), MenuItemDescription.of("Classic")),
      Money(BigDecimal("8.0")),
    )

  // --- Create ---

  @Test
  fun `should return 201 with the created item id on success`() = runBlocking {
    coEvery { menuService.createMenuItem(any()) } returns CreateMenuItemResult.Success("item-1")

    val response = controller.createMenuItem(
      "restaurant-1",
      "menu-1",
      "category-1",
      CreateMenuItemRequest(name = "Margherita", description = "Classic", price = "8.50"),
    )

    assertEquals(HttpStatus.CREATED, response.status)
    assertEquals("item-1", response.body().result)
    coVerify {
      menuService.createMenuItem(
        CreateMenuItemCommand(
          restaurantId = "restaurant-1",
          menuId = "menu-1",
          categoryId = "category-1",
          name = "Margherita",
          description = "Classic",
          price = BigDecimal("8.50"),
        ),
      )
    }
  }

  @Test
  fun `should throw NotFoundException when the menu does not exist on create`() = runBlocking {
    coEvery { menuService.createMenuItem(any()) } returns CreateMenuItemResult.MenuNotFound

    val exception = assertThrows<NotFoundException> {
      controller.createMenuItem(
        "restaurant-1",
        "menu-1",
        "category-1",
        CreateMenuItemRequest(name = "Margherita", description = "Classic", price = "8.50"),
      )
    }

    assertEquals("Menu not found", exception.message)
  }

  @Test
  fun `should throw NotFoundException when the category does not exist on create`() = runBlocking {
    coEvery { menuService.createMenuItem(any()) } returns CreateMenuItemResult.CategoryNotFound

    val exception = assertThrows<NotFoundException> {
      controller.createMenuItem(
        "restaurant-1",
        "menu-1",
        "category-1",
        CreateMenuItemRequest(name = "Margherita", description = "Classic", price = "8.50"),
      )
    }

    assertEquals("Category not found", exception.message)
  }

  @Test
  fun `should throw ValidationException with the use case error on invalid create`() = runBlocking {
    coEvery { menuService.createMenuItem(any()) } returns
      CreateMenuItemResult.InvalidItem("blank name")

    val exception = assertThrows<ValidationException> {
      controller.createMenuItem(
        "restaurant-1",
        "menu-1",
        "category-1",
        CreateMenuItemRequest(name = "", description = "Classic", price = "8.50"),
      )
    }

    assertEquals("blank name", exception.message)
  }

  // --- Update ---

  @Test
  fun `should return 200 with the updated item on success`() = runBlocking {
    val item = sampleItem()
    coEvery { menuService.updateMenuItem(any()) } returns UpdateMenuItemResult.Success(item)

    val response = controller.updateMenuItem(
      "restaurant-1",
      "menu-1",
      "category-1",
      item.id.value,
      UpdateMenuItemRequest(
        name = "Super Margherita",
        description = "With extra cheese",
        price = "10.00",
      ),
    )

    assertEquals(HttpStatus.OK, response.status)
    assertEquals(item.id.value, response.body().result.id)
    coVerify {
      menuService.updateMenuItem(
        UpdateMenuItemCommand(
          restaurantId = "restaurant-1",
          menuId = "menu-1",
          categoryId = "category-1",
          itemId = item.id.value,
          name = "Super Margherita",
          description = "With extra cheese",
          price = BigDecimal("10.00"),
        ),
      )
    }
  }

  @Test
  fun `should throw NotFoundException when the menu does not exist on update`() = runBlocking {
    coEvery { menuService.updateMenuItem(any()) } returns UpdateMenuItemResult.MenuNotFound

    val exception = assertThrows<NotFoundException> {
      controller.updateMenuItem(
        "restaurant-1",
        "menu-1",
        "category-1",
        "item-1",
        UpdateMenuItemRequest(name = "Margherita", description = "Classic", price = "8.50"),
      )
    }

    assertEquals("Menu not found", exception.message)
  }

  @Test
  fun `should throw NotFoundException when the category does not exist on update`() = runBlocking {
    coEvery { menuService.updateMenuItem(any()) } returns UpdateMenuItemResult.CategoryNotFound

    val exception = assertThrows<NotFoundException> {
      controller.updateMenuItem(
        "restaurant-1",
        "menu-1",
        "category-1",
        "item-1",
        UpdateMenuItemRequest(name = "Margherita", description = "Classic", price = "8.50"),
      )
    }

    assertEquals("Category not found", exception.message)
  }

  @Test
  fun `should throw ValidationException with the use case error on invalid update`() = runBlocking {
    coEvery { menuService.updateMenuItem(any()) } returns
      UpdateMenuItemResult.InvalidItem("blank name")

    val exception = assertThrows<ValidationException> {
      controller.updateMenuItem(
        "restaurant-1",
        "menu-1",
        "category-1",
        "item-1",
        UpdateMenuItemRequest(name = "", description = "Classic", price = "8.50"),
      )
    }

    assertEquals("blank name", exception.message)
  }

  // --- Delete ---

  @Test
  fun `should return 200 with the deleted item id from the path on success`() = runBlocking {
    coEvery { menuService.deleteMenuItem(any()) } returns RemoveMenuItemResult.Success

    val response = controller.removeMenuItem("restaurant-1", "menu-1", "category-1", "item-1")

    assertEquals(HttpStatus.OK, response.status)
    assertEquals("item-1", response.body().result)
    coVerify {
      menuService.deleteMenuItem(
        DeleteMenuItemCommand("restaurant-1", "menu-1", "category-1", "item-1"),
      )
    }
  }

  @Test
  fun `should throw NotFoundException when the menu does not exist on delete`() = runBlocking {
    coEvery { menuService.deleteMenuItem(any()) } returns RemoveMenuItemResult.MenuNotFound

    val exception = assertThrows<NotFoundException> {
      controller.removeMenuItem("restaurant-1", "menu-1", "category-1", "item-1")
    }

    assertEquals("Menu not found", exception.message)
  }

  @Test
  fun `should throw NotFoundException when the category does not exist on delete`() = runBlocking {
    coEvery { menuService.deleteMenuItem(any()) } returns RemoveMenuItemResult.CategoryNotFound

    val exception = assertThrows<NotFoundException> {
      controller.removeMenuItem("restaurant-1", "menu-1", "category-1", "item-1")
    }

    assertEquals("Category not found", exception.message)
  }

  @Test
  fun `should throw ValidationException with the use case error on invalid delete`() = runBlocking {
    coEvery { menuService.deleteMenuItem(any()) } returns
      RemoveMenuItemResult.InvalidItem("cannot delete")

    val exception = assertThrows<ValidationException> {
      controller.removeMenuItem("restaurant-1", "menu-1", "category-1", "item-1")
    }

    assertEquals("cannot delete", exception.message)
  }
}
