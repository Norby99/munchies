package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.application.MenuService
import com.munchies.restaurant.application.usecase.menu.CreateMenuCommand
import com.munchies.restaurant.application.usecase.menu.CreateMenuResult
import com.munchies.restaurant.application.usecase.menu.DeleteMenuCommand
import com.munchies.restaurant.application.usecase.menu.DeleteMenuResult
import com.munchies.restaurant.application.usecase.menu.GetMenuCommand
import com.munchies.restaurant.application.usecase.menu.GetMenuResult
import com.munchies.restaurant.application.usecase.menu.GetRestaurantMenusCommand
import com.munchies.restaurant.application.usecase.menu.GetRestaurantMenusResult
import com.munchies.restaurant.application.usecase.menu.UpdateMenuCommand
import com.munchies.restaurant.application.usecase.menu.UpdateMenuResult
import com.munchies.restaurant.application.usecase.menu.ValidityInput
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import com.munchies.restaurant.infrastructure.adapter.dto.ValidityDto
import com.munchies.restaurant.infrastructure.adapter.dto.ValidityType
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.NotFoundException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.UnauthorizedException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ValidationException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.CreateMenuRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.DeleteMenuRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.UpdateMenuRequest
import io.micronaut.http.HttpStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MenuControllerTest {

  private lateinit var menuService: MenuService
  private lateinit var controller: MenuController

  @BeforeEach
  fun setUp() {
    menuService = mockk()
    controller = MenuController(menuService)
  }

  private fun sampleValidity() = arrayOf(ValidityDto(ValidityType.ALWAYS))

  // --- Create ---

  @Test
  fun `should return 201 with the created menu on success`() = runBlocking {
    val menu = Menu.create(RestaurantId(), MenuName.of("Dinner"))
    coEvery { menuService.createMenu(any()) } returns CreateMenuResult.Success(menu)

    val response = controller.createMenu(
      "restaurant-1",
      CreateMenuRequest(managerId = "manager-1", name = "Dinner"),
    )

    assertEquals(HttpStatus.CREATED, response.status)
    assertEquals(menu.id.value, response.body().result.id)
    coVerify {
      menuService.createMenu(
        CreateMenuCommand(
          restaurantId = "restaurant-1",
          managerId = "manager-1",
          name = "Dinner",
          validity = ValidityInput.Always,
        ),
      )
    }
  }

  @Test
  fun `should throw NotFoundException for a missing restaurant on create`() = runBlocking {
    coEvery { menuService.createMenu(any()) } returns CreateMenuResult.RestaurantNotFound

    val exception = assertThrows<NotFoundException> {
      controller.createMenu(
        "restaurant-1",
        CreateMenuRequest(managerId = "manager-1", name = "Dinner"),
      )
    }

    assertEquals("Restaurant not found", exception.message)
  }

  @Test
  fun `should throw UnauthorizedException for an unauthorized manager on create`() = runBlocking {
    coEvery { menuService.createMenu(any()) } returns CreateMenuResult.Unauthorized

    val exception = assertThrows<UnauthorizedException> {
      controller.createMenu(
        "restaurant-1",
        CreateMenuRequest(managerId = "manager-1", name = "Dinner"),
      )
    }

    assertEquals("Unauthorized to create menu", exception.message)
  }

  @Test
  fun `should throw ValidationException with the use case error on invalid create`() = runBlocking {
    coEvery { menuService.createMenu(any()) } returns CreateMenuResult.InvalidMenu("blank name")

    val exception = assertThrows<ValidationException> {
      controller.createMenu("restaurant-1", CreateMenuRequest(managerId = "manager-1", name = ""))
    }

    assertEquals("blank name", exception.message)
  }

  // --- Get ---

  @Test
  fun `should return 200 with the requested menu on success`() = runBlocking {
    val menu = Menu.create(RestaurantId(), MenuName.of("Dinner"))
    coEvery { menuService.getMenu(any()) } returns GetMenuResult.Success(menu)

    val response = controller.getMenu("restaurant-1", menu.id.value)

    assertEquals(HttpStatus.OK, response.status)
    assertEquals(menu.id.value, response.body().result.id)
    coVerify { menuService.getMenu(GetMenuCommand("restaurant-1", menu.id.value)) }
  }

  @Test
  fun `should throw NotFoundException when the menu does not exist on get`() = runBlocking {
    coEvery { menuService.getMenu(any()) } returns GetMenuResult.MenuNotFound

    val exception = assertThrows<NotFoundException> {
      controller.getMenu("restaurant-1", "menu-1")
    }

    assertEquals("Menu not found", exception.message)
  }

  @Test
  fun `should return 200 with every restaurant menu summary`() = runBlocking {
    val restaurantId = RestaurantId()
    val menus = listOf(
      Menu.create(restaurantId, MenuName.of("Lunch")),
      Menu.create(restaurantId, MenuName.of("Dinner")),
    )
    coEvery { menuService.getRestaurantMenus(any()) } returns
      GetRestaurantMenusResult.Success(menus)

    val response = controller.getRestaurantMenus(restaurantId.value)

    assertEquals(HttpStatus.OK, response.status)
    assertEquals(2, response.body().result.size)
    coVerify { menuService.getRestaurantMenus(GetRestaurantMenusCommand(restaurantId.value)) }
  }

  // --- Update ---

  @Test
  fun `should return 200 with the updated menu on success`() = runBlocking {
    val menu = Menu.create(RestaurantId(), MenuName.of("Dinner"))
    coEvery { menuService.updateMenu(any()) } returns UpdateMenuResult.Success(menu)

    val response = controller.updateMenu(
      "restaurant-1",
      menu.id.value,
      UpdateMenuRequest(managerId = "manager-1", name = "Dinner", validity = sampleValidity()),
    )

    assertEquals(HttpStatus.OK, response.status)
    assertEquals(menu.id.value, response.body().result.id)
    coVerify {
      menuService.updateMenu(
        UpdateMenuCommand(
          restaurantId = "restaurant-1",
          menuId = menu.id.value,
          managerId = "manager-1",
          name = "Dinner",
          validity = ValidityInput.Always,
        ),
      )
    }
  }

  @Test
  fun `should throw NotFoundException when the menu does not exist on update`() = runBlocking {
    coEvery { menuService.updateMenu(any()) } returns UpdateMenuResult.MenuNotFound

    val exception = assertThrows<NotFoundException> {
      controller.updateMenu(
        "restaurant-1",
        "menu-1",
        UpdateMenuRequest(managerId = "manager-1", name = "Dinner", validity = sampleValidity()),
      )
    }

    assertEquals("Menu not found", exception.message)
  }

  @Test
  fun `should throw UnauthorizedException for an unauthorized manager on update`() = runBlocking {
    coEvery { menuService.updateMenu(any()) } returns UpdateMenuResult.Unauthorized

    val exception = assertThrows<UnauthorizedException> {
      controller.updateMenu(
        "restaurant-1",
        "menu-1",
        UpdateMenuRequest(managerId = "manager-1", name = "Dinner", validity = sampleValidity()),
      )
    }

    assertEquals("Unauthorized to update menu", exception.message)
  }

  @Test
  fun `should throw ValidationException with the use case error on invalid update`() = runBlocking {
    coEvery { menuService.updateMenu(any()) } returns UpdateMenuResult.InvalidMenu("blank name")

    val exception = assertThrows<ValidationException> {
      controller.updateMenu(
        "restaurant-1",
        "menu-1",
        UpdateMenuRequest(managerId = "manager-1", name = "", validity = sampleValidity()),
      )
    }

    assertEquals("blank name", exception.message)
  }

  // --- Delete ---

  @Test
  fun `should return 200 with the deleted menu id on success`() = runBlocking {
    coEvery { menuService.deleteMenu(any()) } returns DeleteMenuResult.Success("menu-1")

    val response = controller.deleteMenu(
      "restaurant-1",
      "menu-1",
      DeleteMenuRequest(managerId = "manager-1"),
    )

    assertEquals(HttpStatus.OK, response.status)
    assertEquals("menu-1", response.body().result)
    coVerify {
      menuService.deleteMenu(DeleteMenuCommand("restaurant-1", "menu-1", "manager-1"))
    }
  }

  @Test
  fun `should throw NotFoundException when the menu does not exist on delete`() = runBlocking {
    coEvery { menuService.deleteMenu(any()) } returns DeleteMenuResult.MenuNotFound

    val exception = assertThrows<NotFoundException> {
      controller.deleteMenu("restaurant-1", "menu-1", DeleteMenuRequest(managerId = "manager-1"))
    }

    assertEquals("Menu not found", exception.message)
  }

  @Test
  fun `should throw UnauthorizedException for an unauthorized manager on delete`() = runBlocking {
    coEvery { menuService.deleteMenu(any()) } returns DeleteMenuResult.Unauthorized

    val exception = assertThrows<UnauthorizedException> {
      controller.deleteMenu("restaurant-1", "menu-1", DeleteMenuRequest(managerId = "manager-1"))
    }

    assertEquals("Unauthorized to delete menu", exception.message)
  }

  @Test
  fun `should throw ValidationException with the use case error on invalid delete`() = runBlocking {
    coEvery { menuService.deleteMenu(any()) } returns DeleteMenuResult.InvalidMenu("cannot delete")

    val exception = assertThrows<ValidationException> {
      controller.deleteMenu("restaurant-1", "menu-1", DeleteMenuRequest(managerId = "manager-1"))
    }

    assertEquals("cannot delete", exception.message)
  }
}
