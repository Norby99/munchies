package com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper

import com.munchies.restaurant.application.usecase.menu.CreateCategoryResult
import com.munchies.restaurant.application.usecase.menu.DeleteCategoryResult
import com.munchies.restaurant.application.usecase.menu.UpdateCategoryResult
import com.munchies.restaurant.domain.aggregate.Category
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import com.munchies.restaurant.infrastructure.adapter.dto.VariationDto
import com.munchies.restaurant.infrastructure.adapter.dto.VariationOptionDto
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.CreateCategoryRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.UpdateCategoryRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CategoryMapperTest {

  private fun sampleCategory(): Category =
    Menu.create(RestaurantId()).createCategory(CategoryName.of("Pizzas"))

  // --- Create ---

  @Test
  fun `should build a CreateCategoryCommand from every id, the name and the variations`() {
    val request = CreateCategoryRequest(
      name = "Pizzas",
      variations = arrayOf(VariationDto("Size", arrayOf(VariationOptionDto("Large", "2.0")))),
    )

    val command = request.toCommand(restaurantId = "restaurant-1", menuId = "menu-1")

    assertEquals("restaurant-1", command.restaurantId)
    assertEquals("menu-1", command.menuId)
    assertEquals("Pizzas", command.name)
    assertEquals(1, command.variations.size)
    assertEquals("Size", command.variations[0].name)
  }

  @Test
  fun `should wrap the category dto in a 201 response`() {
    val category = sampleCategory()

    val response = CreateCategoryResult.Success(category).toResponse()

    assertEquals(category.id.value, response.result.id)
    assertEquals(201, response.code)
  }

  // --- Update ---

  @Test
  fun `should build an UpdateCategoryCommand from every id, the name and the variations`() {
    val request = UpdateCategoryRequest(name = "Wood Fired Pizzas")

    val command = request.toCommand(
      restaurantId = "restaurant-1",
      menuId = "menu-1",
      categoryId = "category-1",
    )

    assertEquals("restaurant-1", command.restaurantId)
    assertEquals("menu-1", command.menuId)
    assertEquals("category-1", command.categoryId)
    assertEquals("Wood Fired Pizzas", command.name)
    assertEquals(0, command.variations.size)
  }

  @Test
  fun `should wrap the updated category dto in a 200 response`() {
    val category = sampleCategory()

    val response = UpdateCategoryResult.Success(category).toResponse()

    assertEquals(category.id.value, response.result.id)
    assertEquals(200, response.code)
  }

  // --- Delete ---

  @Test
  fun `should wrap the deleted category id in a 200 response`() {
    val result = DeleteCategoryResult.Success(categoryId = "category-1")

    val response = result.toResponse()

    assertEquals("category-1", response.result)
    assertEquals(200, response.code)
  }
}
