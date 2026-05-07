package com.munchies.restaurant.application.usecases.menu

import com.munchies.restaurant.domain.aggregate.Category
import com.munchies.restaurant.domain.aggregate.CategoryId
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateCategoryTest {

  private lateinit var menuRepository: MenuRepository
  private lateinit var updateCategoryUseCase: UpdateCategoryUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    updateCategoryUseCase = UpdateCategoryUseCase(menuRepository)
  }

  @Test
  fun `Should update category successfully when menu exists`() = runBlocking {
    val categoryId = CategoryId()
    val category = spyk(Category(id = categoryId, name = CategoryName.of("Old Name")))
    val menuId = MenuId()
    val menu =
      spyk(
        Menu(id = menuId, restaurantId = RestaurantId(), categories = listOf(category)),
      )

    val command = UpdateCategoryCommand(
      menuId = menuId.value,
      categoryId = categoryId.value,
      name = "New Name",
    )

    coEvery { menuRepository.findById(any()) } returns menu
    coEvery { menuRepository.save(any()) } returns Unit

    when (val result = updateCategoryUseCase(command)) {
      is UpdateCategoryResult.Success -> {
        coVerify(exactly = 1) { menuRepository.save(menu) }
        assertEquals("New Name", category.name)
      }
      else -> {
        assert(false) { "Expected Success, but got $result" }
      }
    }
  }

  @Test
  fun `Should fail when menu does not exist`() = runBlocking {
    val command = UpdateCategoryCommand(
      menuId = MenuId().value,
      categoryId = CategoryId().value,
      name = "New Name",
    )

    coEvery { menuRepository.findById(any()) } returns null

    when (val result = updateCategoryUseCase(command)) {
      is UpdateCategoryResult.MenuNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected Error, but got $result" }
      }
    }
  }
}
