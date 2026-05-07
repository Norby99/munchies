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

class RemoveCategoryTest {

  private lateinit var menuRepository: MenuRepository
  private lateinit var removeCategoryUseCase: RemoveCategoryUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    removeCategoryUseCase = RemoveCategoryUseCase(menuRepository)
  }

  @Test
  fun `Should remove category successfully when menu exists`() = runBlocking {
    val categoryId = CategoryId()
    val category = spyk(Category(id = categoryId, name = CategoryName.of("Desserts")))
    val menuId = MenuId()
    val menu =
      spyk(
        Menu(id = menuId, restaurantId = RestaurantId(), categories = listOf(category)),
      )

    val command = RemoveCategoryCommand(menuId = menuId.value, categoryId = categoryId.value)

    coEvery { menuRepository.findById(any()) } returns menu
    coEvery { menuRepository.save(any()) } returns Unit

    when (val result = removeCategoryUseCase(command)) {
      is RemoveCategoryResult.Success -> {
        coVerify(exactly = 1) { menuRepository.save(menu) }
        assertEquals(0, menu.categories.size)
      }
      else -> {
        assert(false) { "Expected Success, but got $result" }
      }
    }
  }

  @Test
  fun `Should fail when menu does not exist`() = runBlocking {
    val command =
      RemoveCategoryCommand(menuId = MenuId().value, categoryId = CategoryId().value)

    coEvery { menuRepository.findById(any()) } returns null

    when (val result = removeCategoryUseCase(command)) {
      is RemoveCategoryResult.MenuNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected Error, but got $result" }
      }
    }
  }
}
