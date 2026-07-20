package com.munchies.restaurant.application.usecase.menu

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
  private lateinit var deleteCategoryUseCase: DeleteCategoryUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    deleteCategoryUseCase = DeleteCategoryUseCase(menuRepository)
  }

  @Test
  fun `should remove category successfully when menu exists`() = runBlocking {
    val category = spyk(Category(CategoryId(), CategoryName.of("Desserts")))
    val menu = spyk(
      Menu(
        id = MenuId(),
        restaurantId = RestaurantId(),
        categories = listOf(category),
      ),
    )

    val command = DeleteCategoryCommand(
      menu.restaurantId.value,
      menu.id.value,
      category.id.value,
    )

    coEvery { menuRepository.findById(any()) } returns menu
    coEvery { menuRepository.save(any()) } returns Unit

    when (val result = deleteCategoryUseCase(command)) {
      is DeleteCategoryResult.Success -> {
        coVerify(exactly = 1) { menuRepository.save(menu) }
        assertEquals(0, menu.categories.size)
      }
      else -> {
        assert(false) { "Expected Success, but got $result" }
      }
    }
  }

  @Test
  fun `should fail when menu does not exist`() = runBlocking {
    val command =
      DeleteCategoryCommand(RestaurantId().value, MenuId().value, CategoryId().value)

    coEvery { menuRepository.findById(any()) } returns null

    when (val result = deleteCategoryUseCase(command)) {
      is DeleteCategoryResult.MenuNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected Error, but got $result" }
      }
    }
  }
}
