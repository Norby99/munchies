package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.application.MenuService
import com.munchies.restaurant.application.usecase.menu.CreateCategoryCommand
import com.munchies.restaurant.application.usecase.menu.CreateCategoryResult
import com.munchies.restaurant.application.usecase.menu.DeleteCategoryCommand
import com.munchies.restaurant.application.usecase.menu.DeleteCategoryResult
import com.munchies.restaurant.application.usecase.menu.UpdateCategoryCommand
import com.munchies.restaurant.application.usecase.menu.UpdateCategoryResult
import com.munchies.restaurant.domain.aggregate.Category
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.NotFoundException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ValidationException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.CreateCategoryRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.UpdateCategoryRequest
import io.micronaut.http.HttpStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CategoryControllerTest {

  private lateinit var menuService: MenuService
  private lateinit var controller: CategoryController

  @BeforeEach
  fun setUp() {
    menuService = mockk()
    controller = CategoryController(menuService)
  }

  private fun sampleCategory(): Category =
    Menu.create(RestaurantId()).createCategory(CategoryName.of("Pizzas"))

  // --- Create ---

  @Test
  fun `should return 201 with the created category on success`() = runBlocking {
    val category = sampleCategory()
    coEvery { menuService.createCategory(any()) } returns CreateCategoryResult.Success(category)

    val response = controller.createCategory(
      "restaurant-1",
      "menu-1",
      CreateCategoryRequest(name = "Pizzas"),
    )

    assertEquals(HttpStatus.CREATED, response.status)
    assertEquals(category.id.value, response.body().result.id)
    coVerify {
      menuService.createCategory(
        CreateCategoryCommand(restaurantId = "restaurant-1", menuId = "menu-1", name = "Pizzas"),
      )
    }
  }

  @Test
  fun `should throw NotFoundException when the menu does not exist on create`() = runBlocking {
    coEvery { menuService.createCategory(any()) } returns CreateCategoryResult.MenuNotFound

    val exception = assertThrows<NotFoundException> {
      controller.createCategory("restaurant-1", "menu-1", CreateCategoryRequest(name = "Pizzas"))
    }

    assertEquals("Menu not found", exception.message)
  }

  @Test
  fun `should throw ValidationException with the use case error on invalid create`() = runBlocking {
    coEvery { menuService.createCategory(any()) } returns
      CreateCategoryResult.InvalidCategory("blank name")

    val exception = assertThrows<ValidationException> {
      controller.createCategory("restaurant-1", "menu-1", CreateCategoryRequest(name = ""))
    }

    assertEquals("blank name", exception.message)
  }

  // --- Update ---

  @Test
  fun `should return 200 with the updated category on success`() = runBlocking {
    val category = sampleCategory()
    coEvery { menuService.updateCategory(any()) } returns UpdateCategoryResult.Success(category)

    val response = controller.updateCategory(
      "restaurant-1",
      "menu-1",
      category.id.value,
      UpdateCategoryRequest(name = "Wood Fired Pizzas"),
    )

    assertEquals(HttpStatus.OK, response.status)
    assertEquals(category.id.value, response.body().result.id)
    coVerify {
      menuService.updateCategory(
        UpdateCategoryCommand(
          restaurantId = "restaurant-1",
          menuId = "menu-1",
          categoryId = category.id.value,
          name = "Wood Fired Pizzas",
        ),
      )
    }
  }

  @Test
  fun `should throw NotFoundException when the menu does not exist on update`() = runBlocking {
    coEvery { menuService.updateCategory(any()) } returns UpdateCategoryResult.MenuNotFound

    val exception = assertThrows<NotFoundException> {
      controller.updateCategory(
        "restaurant-1",
        "menu-1",
        "category-1",
        UpdateCategoryRequest(name = "Pizzas"),
      )
    }

    assertEquals("Menu not found", exception.message)
  }

  @Test
  fun `should throw ValidationException with the use case error on invalid update`() = runBlocking {
    coEvery { menuService.updateCategory(any()) } returns
      UpdateCategoryResult.InvalidCategory("blank name")

    val exception = assertThrows<ValidationException> {
      controller.updateCategory(
        "restaurant-1",
        "menu-1",
        "category-1",
        UpdateCategoryRequest(name = ""),
      )
    }

    assertEquals("blank name", exception.message)
  }

  // --- Delete ---

  @Test
  fun `should return 200 with the deleted category id on success`() = runBlocking {
    coEvery { menuService.deleteCategory(any()) } returns DeleteCategoryResult.Success("category-1")

    val response = controller.deleteCategory("restaurant-1", "menu-1", "category-1")

    assertEquals(HttpStatus.OK, response.status)
    assertEquals("category-1", response.body().result)
    coVerify {
      menuService.deleteCategory(DeleteCategoryCommand("restaurant-1", "menu-1", "category-1"))
    }
  }

  @Test
  fun `should throw NotFoundException when the menu does not exist on delete`() = runBlocking {
    coEvery { menuService.deleteCategory(any()) } returns DeleteCategoryResult.MenuNotFound

    val exception = assertThrows<NotFoundException> {
      controller.deleteCategory("restaurant-1", "menu-1", "category-1")
    }

    assertEquals("Menu not found", exception.message)
  }

  @Test
  fun `should throw ValidationException with the use case error on invalid delete`() = runBlocking {
    coEvery { menuService.deleteCategory(any()) } returns
      DeleteCategoryResult.InvalidCategory("cannot delete")

    val exception = assertThrows<ValidationException> {
      controller.deleteCategory("restaurant-1", "menu-1", "category-1")
    }

    assertEquals("cannot delete", exception.message)
  }
}
