package com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper

import com.munchies.restaurant.application.usecase.menu.CreateMenuResult
import com.munchies.restaurant.application.usecase.menu.DeleteMenuResult
import com.munchies.restaurant.application.usecase.menu.GetMenuResult
import com.munchies.restaurant.application.usecase.menu.GetRestaurantMenusResult
import com.munchies.restaurant.application.usecase.menu.UpdateMenuResult
import com.munchies.restaurant.application.usecase.menu.ValidityInput
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import com.munchies.restaurant.infrastructure.adapter.dto.ValidityDto
import com.munchies.restaurant.infrastructure.adapter.dto.ValidityType
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.CreateMenuRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.DeleteMenuRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.UpdateMenuRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MenuMapperTest {

  // --- Create ---

  @Test
  fun `should build a CreateMenuCommand with an Always validity by default`() {
    val request = CreateMenuRequest(managerId = "manager-1", name = "Dinner")

    val command = request.toCommand(restaurantId = "restaurant-1")

    assertEquals("restaurant-1", command.restaurantId)
    assertEquals("manager-1", command.managerId)
    assertEquals("Dinner", command.name)
    assertEquals(ValidityInput.Always, command.validity)
  }

  @Test
  fun `should wrap the menu dto in a 201 response`() {
    val menu = Menu.create(RestaurantId(), MenuName.of("Dinner"))

    val response = CreateMenuResult.Success(menu).toResponse()

    assertEquals(menu.id.value, response.result.id)
    assertEquals(201, response.code)
  }

  // --- Get ---

  @Test
  fun `should wrap the requested menu dto in a 200 response`() {
    val menu = Menu.create(RestaurantId(), MenuName.of("Dinner"))

    val response = GetMenuResult.Success(menu).toResponse()

    assertEquals(menu.id.value, response.result.id)
    assertEquals(200, response.code)
  }

  @Test
  fun `should map every restaurant menu to its summary dto in a 200 response`() {
    val restaurantId = RestaurantId()
    val first = Menu.create(restaurantId, MenuName.of("Lunch"))
    val second = Menu.create(restaurantId, MenuName.of("Dinner"))

    val response = GetRestaurantMenusResult.Success(listOf(first, second)).toResponse()

    assertEquals(2, response.result.size)
    assertEquals("Lunch", response.result[0].name)
    assertEquals("Dinner", response.result[1].name)
    assertEquals(200, response.code)
  }

  // --- Update ---

  @Test
  fun `should build an UpdateMenuCommand from every id, the manager id, name and validity`() {
    val request = UpdateMenuRequest(
      managerId = "manager-1",
      name = "Dinner",
      validity = arrayOf(ValidityDto(ValidityType.ALWAYS)),
    )

    val command = request.toCommand(restaurantId = "restaurant-1", menuId = "menu-1")

    assertEquals("restaurant-1", command.restaurantId)
    assertEquals("menu-1", command.menuId)
    assertEquals("manager-1", command.managerId)
    assertEquals("Dinner", command.name)
    assertEquals(ValidityInput.Always, command.validity)
  }

  @Test
  fun `should wrap the updated menu dto in a 200 response`() {
    val menu = Menu.create(RestaurantId(), MenuName.of("Dinner"))

    val response = UpdateMenuResult.Success(menu).toResponse()

    assertEquals(menu.id.value, response.result.id)
    assertEquals(200, response.code)
  }

  // --- Delete ---

  @Test
  fun `should build a DeleteMenuCommand from the restaurant id, menu id and manager id`() {
    val request = DeleteMenuRequest(managerId = "manager-1")

    val command = request.toCommand(restaurantId = "restaurant-1", menuId = "menu-1")

    assertEquals("restaurant-1", command.restaurantId)
    assertEquals("menu-1", command.menuId)
    assertEquals("manager-1", command.managerId)
  }

  @Test
  fun `should wrap the deleted menu id in a 200 response`() {
    val result = DeleteMenuResult.Success(menuId = "menu-1")

    val response = result.toResponse()

    assertEquals("menu-1", response.result)
    assertEquals(200, response.code)
  }
}
